---
problem: 88 - Merge Sorted Array
url: https://leetcode.com/problems/merge-sorted-array/
difficulty: Easy
section: Array / Two Pointers
patterns: [two-pointers, merge-from-the-back]
data_structures: [array]
time: O(m + n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** Two sorted (non-decreasing) integer arrays, `nums1` and `nums2`. `nums1` has length `m + n`: the first `m` slots hold real values, the last `n` slots are placeholder zeros. `nums2` has exactly `n` real values.

**Required:** Merge `nums2` into `nums1` in place so `nums1` becomes one sorted array of length `m + n`. Nothing is returned; the array itself must end up correct.

**Constraints that actually matter:**
- `0 <= m, n <= 200`, so `m + n <= 200` — the input is tiny. Even an O((m+n) log(m+n)) sort would pass, but the problem explicitly asks for O(m + n), which is a hint the intended solution is a linear merge, not a sort.
- `nums1.length == m + n` exactly — there is always exactly enough spare room at the end of `nums1` for `nums2`. This spare capacity is the whole reason an in-place O(1)-space solution is possible.
- Values can be as low as `-10^9`, so don't assume non-negativity when picking sentinel values.

## 2. Recognition Signals

- "Sorted input" + "merge into one sorted array" → classic **merge step of merge sort**.
- The output must be written **into one of the inputs**, and that input has **exactly the extra space** needed at the **end** → this is the tell for **merging from the back**, not the front.
- If you tried to merge from the front in place, you'd overwrite `nums1` values you still need to compare later — that pain is itself a signal to reverse direction.

**Pattern:** Two Pointers (merging two sorted sequences), applied back-to-front to reuse existing storage.

## 3. Core Idea

Merging two sorted arrays forward needs a separate output buffer, because writing to the front of `nums1` would clobber unread elements of `nums1` itself. But `nums1`'s trailing `n` cells are unused padding — so if we fill the array **from the last index backward**, every cell we write to has already been fully consumed as a *read* source (or was never a real value to begin with). We never overwrite a value before we've compared it.

**Invariant:** At every step, the largest not-yet-placed value among the remaining unmerged prefixes of `nums1[0..p1]` and `nums2[0..p2]` is placed at index `p1 + p2 + 1` (the current write pointer), which is always ≥ both read pointers, so no unread data is ever destroyed.

## 4. Approach 1 — Brute Force

**Logic:**
1. Copy the `n` real elements of `nums2` into the tail of `nums1` (positions `m` to `m+n-1`).
2. Sort all `m + n` elements of `nums1`.

```java
public void merge(int[] nums1, int m, int[] nums2, int n) {
    System.arraycopy(nums2, 0, nums1, m, n);
    Arrays.sort(nums1);
}
```

- **Time:** O((m + n) log(m + n)) for the sort.
- **Space:** O(1) auxiliary (primitive `Arrays.sort` uses in-place dual-pivot quicksort), O(log(m+n)) recursion stack in practice — negligible here.

**Why not optimal:** It throws away the fact that both inputs are *already sorted*. Sorting from scratch is strictly more work than a linear merge, and the problem's follow-up explicitly asks for O(m + n). It passes easily given `m + n <= 200`, but it's not the intended pattern and won't satisfy an interviewer asking for the optimal approach.

## 5. Approach 2 — Optimized (Merge From the Back)

**Algorithm:**
1. Set three pointers: `p1 = m - 1` (last real element of `nums1`), `p2 = n - 1` (last element of `nums2`), `write = m + n - 1` (last index of `nums1`, the write cursor).
2. While both `p1 >= 0` and `p2 >= 0`: compare `nums1[p1]` and `nums2[p2]`; copy the larger one to `nums1[write]`; decrement that source pointer and `write`.
3. If `p2` still has remaining elements after `p1` is exhausted, copy the rest of `nums2[0..p2]` directly into `nums1[0..write]`.
4. If `p1` still has remaining elements after `p2` is exhausted, no action needed — those values are already correctly positioned in `nums1`.

```java
public void merge(int[] nums1, int m, int[] nums2, int n) {
    int p1 = m - 1;
    int p2 = n - 1;
    int write = m + n - 1;

    while (p1 >= 0 && p2 >= 0) {
        if (nums1[p1] > nums2[p2]) {
            nums1[write--] = nums1[p1--];
        } else {
            nums1[write--] = nums2[p2--];
        }
    }

    // Only nums2 might have leftovers; leftover nums1 values are already in place.
    while (p2 >= 0) {
        nums1[write--] = nums2[p2--];
    }
}
```

- **Time:** O(m + n) — each element from both arrays is visited exactly once.
- **Space:** O(1) auxiliary — only three index variables; the merge happens directly inside `nums1`.

**Why this is optimal:** Every element must be looked at at least once to determine its final sorted position, so O(m + n) is a hard lower bound — you can't beat it. The space is O(1) auxiliary because the problem hands you the output buffer already sized correctly; writing back-to-front means the write cursor is never behind the read cursors, so no data is lost. There's no time/space trade-off left to make here — this approach is simultaneously optimal in both dimensions.

## 6. Dry Run

Example: `nums1 = [1,2,3,0,0,0]`, `m = 3`, `nums2 = [2,5,6]`, `n = 3`.
Chosen because `nums2` has values interleaved with and tied against `nums1` values (`2 == 2`), exercising the tie-breaking branch (`else` takes `nums2` when not strictly greater).

Initial: `p1 = 2`, `p2 = 2`, `write = 5`.

| Step | nums1[p1] | nums2[p2] | Comparison | Action | nums1 state | p1 | p2 | write |
|------|-----------|-----------|------------|--------|--------------|----|----|-------|
| 1 | 3 | 6 | 3 > 6? No | nums1[5] = nums2[2] = 6 | [1,2,3,0,0,6] | 2 | 1 | 4 |
| 2 | 3 | 5 | 3 > 5? No | nums1[4] = nums2[1] = 5 | [1,2,3,0,5,6] | 2 | 0 | 3 |
| 3 | 3 | 2 | 3 > 2? Yes | nums1[3] = nums1[2] = 3 | [1,2,3,3,5,6] | 1 | 0 | 2 |
| 4 | 2 | 2 | 2 > 2? No | nums1[2] = nums2[0] = 2 | [1,2,2,3,5,6] | 1 | -1 | 1 |

Exit condition: `p2 < 0`, so the main loop stops. The trailing `while (p2 >= 0)` loop does nothing since `p2 = -1`. The remaining `nums1[0..1] = [1,2]` was already correctly in place and untouched.

**Final answer:** `[1,2,2,3,5,6]`.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Sort after copy | O((m+n) log(m+n)) | O(1) aux (input/output space O(m+n) is pre-allocated) | Ignores the pre-sorted structure of the inputs |
| 2. Merge from the back | O(m + n) | O(1) aux | Optimal in both time and space; meets the follow-up requirement |

Input/output space for `nums1` itself is O(m + n) in both approaches — that's given by the problem, not something either algorithm adds.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| `m = 0` (nums1 has no real elements) | `p1` starts at `-1`; must not read `nums1[p1]` | Main loop condition `p1 >= 0` is false immediately, so it skips straight to copying all of `nums2` via the trailing `while (p2 >= 0)` loop |
| `n = 0` (nums2 is empty) | Nothing to merge; `nums1` is already correct | Main loop condition `p2 >= 0` is false immediately; trailing loop also does nothing since `p2 = -1` from the start — array is left untouched, which is correct |
| Duplicate / equal values across arrays | Must decide which equal element goes first without breaking sort order | `nums1[p1] > nums2[p2]` is a **strict** inequality, so ties fall into the `else` branch (take from `nums2`); either choice keeps the result sorted since the values are equal |
| All-same values (e.g. all 5s) | Every comparison is a tie | Same strict-inequality branch handles it uniformly; order among equal elements doesn't affect sortedness |
| Negative numbers | Comparison logic must not assume non-negative values | Plain `>` comparison on `int` works identically for negative values; no sign-based logic is used anywhere |
| Values at `Integer.MIN_VALUE` / `Integer.MAX_VALUE` (within the `-10^9`/`10^9` bound) | Could tempt overflow if someone subtracted values to compare | Code only uses direct `>` comparison, never subtraction, so there is no overflow risk even at the extremes |

## 9. Java Notes

- **`Arrays.sort` on primitives (brute force):** `Arrays.sort(int[])` uses a dual-pivot quicksort with no boxing overhead — relevant only to Approach 1, not the optimized one.
- **`System.arraycopy`:** used in Approach 1 to bulk-copy `nums2` into the tail of `nums1`; this is more efficient and idiomatic than a manual loop for straight array-to-array copies.
- **No overflow risk here:** since the optimized solution only ever compares two `int` values directly (`>`) and never computes a difference (like `a - b`) to determine sign, the classic `Integer.MIN_VALUE`/`MAX_VALUE` overflow trap that shows up in comparator subtraction tricks doesn't apply.
- **Post-decrement in array writes (`nums1[write--] = ...`):** the assignment happens before the decrement takes effect for the *next* read of `write`, but within a single statement Java fully evaluates the index and the right-hand side before performing the decrement side effect after the store — this is safe and standard, but worth being able to explain if asked.

## 10. Common Mistakes

- **Merging from the front instead of the back.** Overwrites `nums1` elements before they've been read, corrupting the merge. Fix: always merge starting from the last index backward when writing into a shared buffer with trailing free space.
- **Using `>=` instead of `>` in the comparison (or vice versa) without thinking about stability.** Either works for correctness here since the array only needs to end up sorted, not stable by source — but flipping it inconsistently mid-code can introduce bugs elsewhere. Fix: pick one strict comparison and apply it uniformly.
- **Forgetting the trailing "copy remaining `nums2`" loop.** If `nums2` still has smaller leading elements left when `nums1` runs out, forgetting this step leaves stale zeros or wrong values at the front of `nums1`. Fix: always drain whichever pointer (only `p2` can have leftovers, since leftover `nums1` values are already correctly placed).
- **Adding a symmetrical "copy remaining `nums1`" loop that isn't needed.** It's harmless but reveals a misunderstanding: any `nums1[0..p1]` left over is *already* in its correct final position, because nothing was written over it. Fix: recognize that only `nums2` leftovers need explicit copying.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "In-place merge into an array with exactly the trailing space you need → merge from the back with two read pointers and one write pointer."
- **90-second explanation:** "I use three pointers: `p1` at the last real element of `nums1`, `p2` at the last element of `nums2`, and `write` at the very last index of `nums1`. I repeatedly compare `nums1[p1]` and `nums2[p2]`, place the larger one at `write`, and step that source pointer and `write` backward. I do this instead of merging forward because `nums1` doesn't have separate scratch space — writing from the back guarantees I never overwrite a value I still need to read, since the write position is always at or ahead of both read positions. Once `p1` runs out, any remaining `nums1` values are already correctly placed, so I only need to flush whatever's left in `nums2`. This is O(m+n) time and O(1) extra space."
- **Related problems using this pattern:**
  - LeetCode 21 — Merge Two Sorted Lists
  - LeetCode 23 — Merge k Sorted Lists
  - LeetCode 977 — Squares of a Sorted Array
  - LeetCode 986 — Interval List Intersections

## 12. Recall Questions

**Q:** Why is it safe to merge starting from the back of `nums1` instead of the front?
**A:** Because the write pointer always lands on or ahead of both read pointers, so every cell is fully read before it's ever overwritten — nothing needed later gets destroyed.

**Q:** After the main comparison loop ends, why do we only need a leftover-copy loop for `nums2` and never for `nums1`?
**A:** If `nums1`'s pointer runs out first, whatever remains at the front of `nums1` is already sitting in its correct final sorted position and was never touched, so there's nothing to copy.

**Q:** Why does this problem specifically require the trailing space in `nums1` to equal `n`, rather than just `nums1.length >= m`?
**A:** The back-to-front technique only works because there's guaranteed to be exactly enough free space at the end to hold every element from `nums2` without ever needing a temporary buffer.

**Q:** What would go wrong if you used `Arrays.sort` after copying `nums2` in, in terms of meeting the problem's intended constraint?
**A:** It would still produce a correct answer, but it runs in O((m+n) log(m+n)) instead of the O(m+n) the follow-up asks for, ignoring that both inputs are already sorted.

**Q:** Does the strict `>` vs a non-strict `>=` comparison change correctness of the final sorted array when values are equal?
**A:** No — since the values are equal, placing either one first still yields a correctly non-decreasing array; only the relative order of equal elements from different sources would differ, which the problem doesn't care about.

## 13. Final Code

```java
public void merge(int[] nums1, int m, int[] nums2, int n) {
    int p1 = m - 1;
    int p2 = n - 1;
    int write = m + n - 1;

    while (p1 >= 0 && p2 >= 0) {
        nums1[write--] = (nums1[p1] > nums2[p2]) ? nums1[p1--] : nums2[p2--];
    }

    while (p2 >= 0) {
        nums1[write--] = nums2[p2--];
    }
}
```

## 14. Self-Test

You have `nums1` (length `m + n`, first `m` slots real, rest zero-padding) and `nums2` (length `n`), both sorted ascending. Merge `nums2` into `nums1` in place, in O(m + n) time and O(1) extra space, without using a separate array or sorting. Think about which direction you must fill from, and why.
