---
problem: 80 - Remove Duplicates from Sorted Array II
url: https://leetcode.com/problems/remove-duplicates-from-sorted-array-ii/
difficulty: Medium
section: Array / Two Pointers
patterns: [two-pointers, in-place-bounded-duplicates]
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

**Given:** An integer array `nums`, sorted in non-decreasing order.

**Required:** Remove duplicates in place so each unique value appears **at most twice** (not just once, like LC 26), preserving relative order. Let `k` be the resulting count; the first `k` slots of `nums` must hold the final result, and you return `k`. No extra array allowed; must be O(1) extra memory.

**Constraints that actually matter:**
- `1 <= nums.length <= 3 * 10^4` — moderate size, consistent with an O(n) expectation.
- `nums is sorted in non-decreasing order` — as with LC 26, this guarantees all copies of a value are contiguous, so the "keep at most 2" decision can be made with a small constant amount of lookback rather than counting occurrences globally.
- **"At most twice"** is the key twist versus LC 26's "at most once" — this generalizes the pattern and is the hint that the comparison window needs to grow from 1 element back to 2 elements back.

## 2. Recognition Signals

- "Sorted array" + "keep each value up to **some fixed count** `m`" (here `m = 2`) → generalization of the LC 26 write-pointer pattern, where the comparison target shifts from `nums[k-1]` to `nums[k-m]`.
- "In-place, O(1) extra memory, return `k`" → same overwrite-with-two-pointers shape as LC 26/27.
- Whenever the allowed duplicate count is a small fixed number rather than "zero" or "unbounded" — that number tells you exactly how far back to look (`nums[k - m]`).

**Pattern:** Two Pointers (slow write pointer / fast read pointer), generalized to allow up to `m` copies by comparing against the element `m` positions back in the write region instead of just 1 position back.

## 3. Core Idea

In LC 26, a value was kept only if it differed from the single most recently kept value (`nums[k-1]`), enforcing "at most 1 copy." Here, we want "at most 2 copies." The trick: keep `nums[i]` if it differs from `nums[k-2]` — the element *two positions back* in the write region. If `nums[i] != nums[k-2]`, then even in the worst case where `nums[k-2]` and `nums[k-1]` are both equal to `nums[i]`'s neighbors, adding a third copy is only blocked when `nums[i]` would equal something already appearing twice; comparing to `nums[k-2]` specifically catches exactly that "third copy" case, since sortedness guarantees any third-or-later duplicate equals both of the two immediately preceding kept slots.

**Invariant:** At every point, `nums[0..k-1]` holds a valid result respecting "at most 2 copies per value," in original sorted relative order, and comparing a new candidate to `nums[k-2]` correctly detects whether keeping it would create a third copy.

## 4. Approach 1 — Brute Force

**Logic:** Count occurrences using a frequency scan (exploiting sortedness with run-length counting), building a filtered list capped at 2 per value, then copy back.

```java
public int removeDuplicates(int[] nums) {
    List<Integer> kept = new ArrayList<>();
    int i = 0;
    while (i < nums.length) {
        int j = i;
        while (j < nums.length && nums[j] == nums[i]) {
            j++;
        }
        int count = Math.min(j - i, 2);
        for (int c = 0; c < count; c++) {
            kept.add(nums[i]);
        }
        i = j;
    }
    for (int idx = 0; idx < kept.size(); idx++) {
        nums[idx] = kept.get(idx);
    }
    return kept.size();
}
```

- **Time:** O(n) — each element is visited once during the run-length scan, once during copy-back.
- **Space:** O(n) auxiliary for the `ArrayList`, plus boxing.

**Why it is not optimal:** Correct and still O(n) time, but uses O(n) auxiliary space, which directly violates this problem's explicit "O(1) extra memory" requirement — this problem statement is unusually strict about that, making Approach 1 not just suboptimal but actually disallowed by the stated constraints.

## 5. Approach 2 — Optimized (Two Pointers, Compare Two Back)

**Algorithm:**
1. If `nums.length <= 2`, every element trivially satisfies "at most 2 copies" — return `nums.length` immediately (also avoids an out-of-bounds read on `nums[k-2]` when `k < 2`).
2. Initialize `k = 2` (the first two elements are always kept, since any value can appear at least twice for free).
3. For `i` from `2` to `nums.length - 1`:
   - If `nums[i] != nums[k - 2]`, keeping it can't create more than 2 copies: set `nums[k] = nums[i]`, increment `k`.
   - Otherwise, keeping it would create a 3rd copy — skip it.
4. Return `k`.

```java
public int removeDuplicates(int[] nums) {
    if (nums.length <= 2) {
        return nums.length;
    }

    int k = 2;
    for (int i = 2; i < nums.length; i++) {
        if (nums[i] != nums[k - 2]) {
            nums[k] = nums[i];
            k++;
        }
    }
    return k;
}
```

- **Time:** O(n) — single pass, one read pointer, one write pointer.
- **Space:** O(1) auxiliary — just the `k` counter, matching the problem's explicit requirement.

**Why this is optimal:** Every element must be examined at least once to determine its fate, so O(n) time is a hard lower bound. Space is O(1) because sortedness means checking exactly 2 positions back in the write region is always sufficient — no growing history or hash map is ever needed. This directly satisfies the problem's explicit "O(1) extra memory" constraint, which the brute force violates.

## 6. Dry Run

Example: `nums = [0,0,1,1,1,1,2,3,3]`.
Chosen because value `1` appears four times — more than twice — exercising the core "block the third and later copies" logic, unlike a run of exactly 2 or 1.

Initial: `k = 2` (nums[0]=0, nums[1]=0 are both kept implicitly, since length > 2).

| i | nums[i] | nums[k-2] | Equal? | Action | nums state | k |
|---|---------|-----------|--------|--------|-------------|---|
| 2 | 1 | nums[0]=0 | No | nums[2]=1, k++ | [0,0,1,1,1,1,2,3,3] | 3 |
| 3 | 1 | nums[1]=0 | No | nums[3]=1, k++ | [0,0,1,1,1,1,2,3,3] | 4 |
| 4 | 1 | nums[2]=1 | Yes | skip | [0,0,1,1,1,1,2,3,3] | 4 |
| 5 | 1 | nums[2]=1 | Yes | skip | [0,0,1,1,1,1,2,3,3] | 4 |
| 6 | 2 | nums[2]=1 | No | nums[4]=2, k++ | [0,0,1,1,2,1,2,3,3] | 5 |
| 7 | 3 | nums[3]=1 | No | nums[5]=3, k++ | [0,0,1,1,2,3,2,3,3] | 6 |
| 8 | 3 | nums[4]=2 | No | nums[6]=3, k++ | [0,0,1,1,2,3,3,3,3] | 7 |

Exit condition: `i` reaches `nums.length` (9), loop ends.

**Final answer:** `k = 7`, and `nums[0..6] = [0,0,1,1,2,3,3]`, matching the expected output exactly (each of 0, 1, 3 capped at 2 copies; 2 appears once since it only occurred once in the input).

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Run-length scan into list, copy back | O(n) | O(n) | Violates the problem's explicit O(1) memory requirement |
| 2. Two pointers, compare 2-back | O(n) | O(1) | Optimal and meets the stated memory constraint |

Input/output space for `nums` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Array length 1 or 2 | `nums[k-2]` would be out of bounds if the main loop ran (`k` starts at 2, so `k-2=0` is fine only once length > 2 elements exist to compare against) | Explicit early return `if (nums.length <= 2) return nums.length;` sidesteps the issue entirely — any array this short trivially satisfies "at most 2 copies" |
| All elements identical | Should collapse to exactly 2 kept copies | After the first two are kept, every subsequent comparison `nums[i] != nums[k-2]` is false (since `k` stops growing past 2), so no more than 2 total are ever kept |
| No duplicates at all | Every element should be kept, `k` should equal `nums.length` | Every comparison to `nums[k-2]` is true (since `k-2` never catches up to a matching value when nothing repeats), so `k` advances every iteration |
| Duplicates appearing more than twice consecutively (e.g., four 1s) | Must block exactly the 3rd and later copies, not the 2nd | Comparing against `nums[k-2]` (two back) rather than `nums[k-1]` (one back) is precisely what allows the 2nd copy through while blocking the 3rd — see the dry run above |
| Negative numbers (constraint allows down to `-10^4`) | Comparison logic must not assume non-negative values | Plain `!=` comparison on `int` works identically regardless of sign |
| Boundary values (`-10^4` and `10^4`) | Could tempt an off-by-one if using range checks instead of equality | Code only uses direct equality comparison between array elements, never a range check, so extreme values behave identically to any other value |

## 9. Java Notes

- **`!=` on primitive `int`, comparing `nums[k-2]`:** Straightforward primitive comparison; no boxing or `.equals()` concerns since `nums` is `int[]`.
- **Off-by-one risk in the lookback index:** `nums[k-2]` is only safe once `k >= 2`, which is guaranteed by initializing `k = 2` and guarding the `nums.length <= 2` case up front — worth double-checking this invariant explicitly if extending the pattern to a general "at most `m` copies" version (where you'd initialize `k = m` and guard `nums.length <= m`).
- **No `ArrayList`/boxing needed in the optimized solution:** avoids the overhead the brute force incurs, and more importantly meets the explicit O(1) extra memory requirement stated in the problem itself.

## 10. Common Mistakes

- **Comparing against `nums[k-1]` instead of `nums[k-2]`.** This would enforce "at most 1 copy" (i.e., solve LC 26's stricter version), incorrectly blocking legitimate second copies. Fix: for "at most 2," the lookback distance must be exactly 2, matching the allowed count.
- **Forgetting the `nums.length <= 2` guard and letting `nums[k-2]` go out of bounds on tiny inputs.** Fix: explicitly handle small arrays before entering the main comparison loop, or ensure the loop's starting index and initial `k` value make the first lookback always valid.
- **Trying to generalize by counting occurrences with a running counter variable instead of using the lookback trick.** This works but is more error-prone to implement correctly (resetting the counter at the right moment) and offers no benefit over the simpler direct-comparison approach. Fix: prefer the `nums[k - m]` lookback pattern — it's simpler and less bug-prone.
- **Allocating a `HashMap<Integer, Integer>` to count frequencies.** Works but adds unnecessary O(n) space and violates the problem's explicit O(1) memory constraint, plus complicates preserving sorted order during rebuild. Fix: exploit sortedness directly with the two-pointer lookback instead.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Sorted array + keep each value up to `m` copies, in place, O(1) space → two pointers comparing the candidate to the element `m` positions back in the write region."
- **90-second explanation:** "This generalizes the 'remove duplicates, keep at most once' pattern to 'keep at most twice.' I keep a write pointer `k`, starting at 2 since the first two elements are always safe to keep. For each new element at read pointer `i`, I compare it to `nums[k-2]` — the element two slots back in the already-finalized write region. If they differ, keeping the new element can't create a third copy, so I write it to `nums[k]` and advance `k`. If they're equal, writing it would create a third copy of that value, so I skip it. Sortedness guarantees this 2-back comparison is enough to correctly detect a would-be third copy, without needing to count occurrences explicitly. This is O(n) time and O(1) extra space, exactly matching the problem's stated memory constraint."
- **Related problems using this pattern:**
  - LeetCode 26 — Remove Duplicates from Sorted Array
  - LeetCode 27 — Remove Element
  - LeetCode 83 — Remove Duplicates from Sorted List
  - LeetCode 82 — Remove Duplicates from Sorted List II

## 12. Recall Questions

**Q:** Why does comparing a candidate element to `nums[k-2]` (rather than `nums[k-1]`) correctly enforce "at most 2 copies"?
**A:** If the candidate equals the element two slots back in the finalized write region, that means two copies of that value are already kept, so adding this one would make a third — comparing 1-back would incorrectly block even the legitimate second copy.

**Q:** Why is it safe to initialize `k = 2` and start the read pointer at index 2 without checking anything first?
**A:** Any array can safely keep its first two elements regardless of their values, since "at most 2 copies" is never violated by only 2 elements; the guard for arrays of length ≤ 2 handles the case where there's nothing left to scan.

**Q:** How would this solution change if the problem asked for "at most 3 copies" instead of 2?
**A:** Initialize `k = 3`, guard for `nums.length <= 3`, and compare each candidate to `nums[k-3]` instead of `nums[k-2]` — the lookback distance always equals the allowed copy count.

**Q:** Why doesn't this approach need a frequency counter or hash map to track how many times each value has appeared?
**A:** Because the array is sorted, all copies of a value are contiguous, so checking a fixed number of positions back in the already-built result is enough to know whether keeping the current element would exceed the allowed count — no global counting is needed.

**Q:** What would go wrong if you forgot to guard against `nums.length <= 2` before running the main loop?
**A:** With very short arrays, `nums[k-2]` could index into positions that either don't yet hold meaningful comparison data or, depending on loop bounds, risk being logically incorrect at the boundary — the explicit guard sidesteps needing to reason about that edge case at all.

## 13. Final Code

```java
public int removeDuplicates(int[] nums) {
    if (nums.length <= 2) {
        return nums.length;
    }

    int k = 2;
    for (int i = 2; i < nums.length; i++) {
        if (nums[i] != nums[k - 2]) {
            nums[k++] = nums[i];
        }
    }
    return k;
}
```

## 14. Self-Test

You have a sorted (non-decreasing) array `nums`. Remove duplicates in place so each unique value appears at most twice, preserving relative order, using O(1) extra memory, and return `k`, the count of kept elements, occupying `nums[0..k-1]`. Think about how far back in the already-finalized region you need to look to know whether keeping the current element would create a third copy.
