---
problem: 27 - Remove Element
url: https://leetcode.com/problems/remove-element/
difficulty: Easy
section: Array / Two Pointers
patterns: [two-pointers, in-place-partition]
data_structures: [array]
time: O(n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An integer array `nums` and a target value `val`.

**Required:** Remove every occurrence of `val` from `nums` in place. Order doesn't matter. Let `k` be the count of elements not equal to `val`; the first `k` positions of `nums` must contain exactly those elements (in any order), and return `k`. Everything after index `k-1` is ignored by the judge.

**Constraints that actually matter:**
- `0 <= nums.length <= 100` — tiny input, so any O(n) or even O(n log n) approach trivially fits in time; the real constraint on approach comes from the **in-place** requirement, not from size.
- The judge explicitly only checks the first `k` elements and ignores order — this is a strong hint that you don't need a stable in-order removal; you're free to use a technique that reorders elements (like swapping with the end) for efficiency.

## 2. Recognition Signals

- "Remove all occurrences of X **in-place**" + "order may change" → don't need stability, so you can swap instead of shift.
- "Return `k`, first `k` elements matter, rest are ignored" → classic **partition into keep / discard** in a single pass, no extra array needed.
- Whenever the number of elements to keep is unknown up front and you must compact them to the front — this is the **overwrite pointer** (aka "slow/fast pointer") technique.

**Pattern:** Two Pointers — specifically the "read pointer scans everything, write pointer only advances for elements you keep" in-place filtering pattern.

## 3. Core Idea

Since order doesn't matter and only the first `k` slots are checked, you don't need to preserve relative positions of kept elements — you only need every kept element pushed somewhere within `nums[0..k-1]`. Use a single write pointer `k` that starts at 0: scan `nums` left to right with a read pointer `i`; whenever `nums[i] != val`, place it at `nums[k]` and increment `k`. Since `k` only advances on a "keep," and `i` always advances, `k <= i` at every step, so writing to `nums[k]` never overwrites an unread element.

**Invariant:** After processing index `i`, `nums[0..k-1]` contains exactly the non-`val` elements seen so far (possibly reordered relative to each other only if you use the swap-with-end variant; the simple overwrite variant actually preserves relative order of kept elements too, as a bonus, though it's not required).

## 4. Approach 1 — Brute Force

**Logic:** Build a new list of all elements not equal to `val`, then copy that list back into `nums`.

```java
public int removeElement(int[] nums, int val) {
    List<Integer> kept = new ArrayList<>();
    for (int num : nums) {
        if (num != val) {
            kept.add(num);
        }
    }
    for (int i = 0; i < kept.size(); i++) {
        nums[i] = kept.get(i);
    }
    return kept.size();
}
```

- **Time:** O(n) — one pass to filter, one pass to copy back.
- **Space:** O(n) auxiliary for the `ArrayList`.

**Why it is not optimal:** It's already O(n) in time, matching the optimal, but it uses O(n) auxiliary space and boxes every `int` into an `Integer`, which the problem's in-place spirit (and any interviewer) would flag as unnecessary — the array itself has all the room needed to do this without a second data structure.

## 5. Approach 2 — Optimized (Overwrite / Two Pointers)

**Algorithm:**
1. Initialize `k = 0` as the write pointer (also doubles as the running count of kept elements).
2. For each `i` from `0` to `nums.length - 1` (read pointer):
   - If `nums[i] != val`, set `nums[k] = nums[i]` and increment `k`.
   - If `nums[i] == val`, do nothing — skip it.
3. After the loop, return `k`.

```java
public int removeElement(int[] nums, int val) {
    int k = 0;
    for (int i = 0; i < nums.length; i++) {
        if (nums[i] != val) {
            nums[k] = nums[i];
            k++;
        }
    }
    return k;
}
```

- **Time:** O(n) — single pass, one read pointer, one write pointer.
- **Space:** O(1) auxiliary — just the `k` counter, no extra structure.

**Why this is optimal:** Every element must be examined at least once to know whether it equals `val`, so O(n) time is a hard lower bound. Space is O(1) because the write pointer never needs to "look ahead" — it always trails or equals the read pointer, so writes never destroy data that hasn't been read yet. There's no trade-off here: this is optimal in both time and space simultaneously. (A swap-with-end variant exists that can do fewer writes when `val` is rare, but it's not simpler or asymptotically better, so it's mentioned only as a known alternative, not implemented here.)

## 6. Dry Run

Example: `nums = [0,1,2,2,3,0,4,2]`, `val = 2`.
Chosen because `val` appears multiple times, non-consecutively, and also appears at the very end — exercising repeated skips and a final skip.

Initial: `k = 0`.

| i | nums[i] | nums[i] != val? | Action | nums state | k |
|---|---------|------------------|--------|-------------|---|
| 0 | 0 | Yes | nums[0] = 0, k++ | [0,1,2,2,3,0,4,2] | 1 |
| 1 | 1 | Yes | nums[1] = 1, k++ | [0,1,2,2,3,0,4,2] | 2 |
| 2 | 2 | No | skip | [0,1,2,2,3,0,4,2] | 2 |
| 3 | 2 | No | skip | [0,1,2,2,3,0,4,2] | 2 |
| 4 | 3 | Yes | nums[2] = 3, k++ | [0,1,3,2,3,0,4,2] | 3 |
| 5 | 0 | Yes | nums[3] = 0, k++ | [0,1,3,0,3,0,4,2] | 4 |
| 6 | 4 | Yes | nums[4] = 4, k++ | [0,1,3,0,4,0,4,2] | 5 |
| 7 | 2 | No | skip | [0,1,3,0,4,0,4,2] | 5 |

Exit condition: `i` reaches `nums.length` (8), loop ends.

**Final answer:** `k = 5`, and `nums[0..4] = [0,1,3,0,4]` — five elements, none equal to 2, matching the example's expected count (the LeetCode example shows `[0,1,4,0,3,...]`, a different valid ordering, since order isn't checked).

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Filter into new list, copy back | O(n) | O(n) | Correct but uses unnecessary extra storage and boxing |
| 2. Overwrite with write pointer | O(n) | O(1) | Optimal; single pass, no extra structure |

Input/output space for `nums` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Empty array (`nums.length == 0`) | Loop body never executes; must still return a valid count | `for` loop condition `i < nums.length` is false immediately, loop is skipped, `k` stays 0 and is returned correctly |
| No occurrences of `val` at all | Every element should be kept; `k` should equal `nums.length` | Every comparison is `true`, so `k` increments every iteration, ending at `nums.length` — nums is effectively unchanged |
| Every element equals `val` | `k` should end at 0, and nothing should be written | Every comparison is `false`, so the write branch never executes; `k` stays 0, which is correctly returned |
| Duplicates of `val` scattered throughout | Naive index-based removal (e.g., shifting) can skip or double-count if implemented incorrectly | The write pointer only advances on a genuine "keep," so duplicates of `val` are transparently skipped regardless of position |
| `val` not present in the given range but appears as boundary value (`nums[i] == 0` or `== 50`) | Off-by-one or sign issues could arise if using `<=`/`>=` instead of equality checks | Code uses strict equality `!=`/`==`, which behaves identically at any value including the boundaries `0` and `50` |
| `val` itself at the boundary of its own range (`val = 0` or `val = 100`, per constraints) | If `val` never appears in `nums` (since `nums[i] <= 50 < val = 100` is possible), must still work correctly | Since the check is a direct equality comparison, a `val` that never matches simply means every element is kept — no special-casing needed |

## 9. Java Notes

- **`!=` on primitive `int`:** This is a direct value comparison, not reference comparison, so there's no `Integer` autoboxing or `==` vs `.equals()` pitfall here — `nums` is a primitive `int[]`, and comparisons behave exactly as expected.
- **No `ArrayList`/`Integer` boxing needed in the optimized solution:** unlike Approach 1, which boxes every value into an `Integer` inside an `ArrayList<Integer>`, the optimized solution stays entirely within the primitive `int[]`, avoiding boxing overhead entirely.
- **In-place mutation via index reuse:** `nums[k] = nums[i]` is a simple primitive array write; no `System.arraycopy` is needed since we're moving one element at a time and `k <= i` always holds, so there's never a need for a bulk block copy.

## 10. Common Mistakes

- **Trying to remove elements by shifting everything left one at a time on each match.** This works but degrades to O(n²) in the worst case (e.g., `val` appearing at nearly every index forces repeated O(n) shifts). Fix: use the single write-pointer overwrite technique instead, which is O(n) total regardless of how many matches occur.
- **Incrementing `k` before checking the condition, or checking `nums[k]` instead of `nums[i]`.** This silently corrupts the write position and can either skip real elements or overwrite ones not yet read. Fix: always check `nums[i] != val` first, and only touch `nums[k]` inside that branch.
- **Assuming the relative order of kept elements must be preserved and adding unnecessary logic to enforce it.** The problem explicitly says order may change, so this wastes effort. Fix: re-read the constraints — no ordering guarantee is required, so the simplest overwrite works.
- **Returning `nums.length` instead of `k`, or forgetting to return `k` at all after mutating the array.** The judge cares only about the returned `k` and `nums[0..k-1]`; returning the wrong count fails even if the array itself is correctly compacted. Fix: track `k` as a dedicated counter and return exactly that.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "In-place removal from an array where order doesn't matter → single write pointer that only advances on elements you keep."
- **90-second explanation:** "I use two pointers: `i` scans every element, and `k` marks where the next kept element should go. Whenever `nums[i]` isn't equal to `val`, I copy it to `nums[k]` and advance `k`; otherwise I just skip it. Because `k` only ever advances when we keep something, and `i` always advances, `k` never gets ahead of `i`, so I'm never overwriting a value before I've read it. At the end, `k` is exactly the count of kept elements, and `nums[0..k-1]` holds all of them. This runs in O(n) time with O(1) extra space, and since order doesn't need to be preserved, there's no need for anything fancier like shifting."
- **Related problems using this pattern:**
  - LeetCode 26 — Remove Duplicates from Sorted Array
  - LeetCode 80 — Remove Duplicates from Sorted Array II
  - LeetCode 283 — Move Zeroes
  - LeetCode 905 — Sort Array By Parity

## 12. Recall Questions

**Q:** Why is it safe for the write pointer `k` to write into `nums[k]` without ever destroying data the read pointer `i` hasn't processed yet?
**A:** Because `k` only increments when an element is kept, while `i` increments every iteration regardless, so `k` is always less than or equal to `i` — the write position never runs ahead of the read position.

**Q:** Why doesn't this solution need to preserve the original relative order of the kept elements?
**A:** The problem statement explicitly says the order of elements may change and the judge only checks that the first `k` elements match the expected set, not their sequence.

**Q:** What would go wrong (performance-wise) if you removed matches by shifting all subsequent elements left by one, every time a match is found?
**A:** Each shift costs O(n) in the worst case, and if `val` appears many times, you'd repeat that shift many times, degrading total time to O(n²) instead of O(n).

**Q:** If `val` never appears anywhere in `nums`, what does `k` end up equal to, and why?
**A:** `k` ends up equal to `nums.length`, because every element passes the `!= val` check and gets "kept," advancing `k` on every iteration.

**Q:** Does this algorithm need any special handling for an empty input array?
**A:** No — the loop simply doesn't execute when `nums.length == 0`, and `k` remains 0, which is already the correct answer.

## 13. Final Code

```java
public int removeElement(int[] nums, int val) {
    int k = 0;
    for (int i = 0; i < nums.length; i++) {
        if (nums[i] != val) {
            nums[k++] = nums[i];
        }
    }
    return k;
}
```

## 14. Self-Test

You have an array `nums` and a value `val`. Remove every occurrence of `val` in place — order of the remaining elements doesn't matter — and return `k`, the count of elements not equal to `val`, with those elements occupying `nums[0..k-1]`. Think about how a single write pointer, advancing only when you decide to keep an element, can do this in one pass with no extra storage.
