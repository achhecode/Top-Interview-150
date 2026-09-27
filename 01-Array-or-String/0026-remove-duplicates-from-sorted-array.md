---
problem: 26 - Remove Duplicates from Sorted Array
url: https://leetcode.com/problems/remove-duplicates-from-sorted-array/
difficulty: Easy
section: Array / Two Pointers
patterns: [two-pointers, in-place-deduplication]
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

**Given:** An integer array `nums`, already sorted in non-decreasing order.

**Required:** Remove duplicates in place so each unique value appears exactly once, **preserving relative order**. Let `k` be the number of unique elements; the first `k` positions must hold those unique values in sorted order, and you return `k`. Everything beyond index `k - 1` is ignored.

**Constraints that actually matter:**
- `1 <= nums.length <= 3 * 10^4` — moderate size; rules out anything worse than roughly O(n log n), but O(n) is clearly reachable and expected given the input is already sorted.
- `nums is sorted in non-decreasing order` — this is the single most important constraint. Because it's sorted, every duplicate of a value is guaranteed to sit immediately adjacent to other copies of the same value, so you only ever need to compare each element to the **most recent unique value kept**, not to all previously seen values.
- Order **must** be preserved here (unlike Remove Element/LC 27) — this rules out swap-based tricks; you must shift, not swap.

## 2. Recognition Signals

- "Sorted array" + "remove duplicates" + "relative order preserved" → sortedness means duplicates are always **adjacent**, so a single comparison against the last kept value is enough — no hash set needed.
- "In-place" + "return count `k`, first `k` slots matter" → the same overwrite-with-write-pointer shape as Remove Element (LC 27), but here the "keep" condition is "different from the last kept value" instead of "not equal to some target."
- Whenever you need to compact a sorted sequence down to just its distinct values without extra storage — this is the **slow/fast two-pointer deduplication** pattern.

**Pattern:** Two Pointers (slow write pointer / fast read pointer), specialized for deduplication using sortedness as the adjacency guarantee.

## 3. Core Idea

Because the array is sorted, all copies of any given value are contiguous. So a value is a duplicate if and only if it equals the *immediately preceding* kept value — you never need to check against anything further back. Use a slow pointer `k` marking the boundary of the unique-elements-so-far region (specifically, `nums[k-1]` is the last unique value kept), and a fast pointer `i` scanning forward. Whenever `nums[i] != nums[k-1]`, it's a new unique value: copy it to `nums[k]` and advance `k`.

**Invariant:** At every point, `nums[0..k-1]` contains exactly the distinct values encountered so far, in their original sorted relative order, and `nums[k-1]` is always the largest (most recently confirmed) unique value.

## 4. Approach 1 — Brute Force

**Logic:** Use a `LinkedHashSet` (or just track "last seen value" while building a new list) to collect unique values in order, then copy them back into `nums`.

```java
public int removeDuplicates(int[] nums) {
    List<Integer> unique = new ArrayList<>();
    for (int num : nums) {
        if (unique.isEmpty() || !unique.get(unique.size() - 1).equals(num)) {
            unique.add(num);
        }
    }
    for (int i = 0; i < unique.size(); i++) {
        nums[i] = unique.get(i);
    }
    return unique.size();
}
```

- **Time:** O(n) — one pass to collect, one pass to copy back.
- **Space:** O(n) auxiliary for the `ArrayList`, plus boxing overhead.

**Why it is not optimal:** It's already O(n) time, same as the optimal, but it allocates an auxiliary list and boxes every value into `Integer`, doing unnecessary work when the array itself has all the space needed to do this truly in place.

## 5. Approach 2 — Optimized (Slow/Fast Two Pointers)

**Algorithm:**
1. If `nums` is empty, return 0 (guards against reading `nums[0]` on an empty array — though constraints guarantee length ≥ 1, it's good defensive practice).
2. Initialize `k = 1` (the first element is always unique/kept by definition — there's nothing before it to duplicate).
3. For `i` from `1` to `nums.length - 1`:
   - If `nums[i] != nums[k - 1]`, it's a new unique value: set `nums[k] = nums[i]` and increment `k`.
   - Otherwise, it's a duplicate of the last kept value — skip it.
4. Return `k`.

```java
public int removeDuplicates(int[] nums) {
    if (nums.length == 0) {
        return 0;
    }

    int k = 1;
    for (int i = 1; i < nums.length; i++) {
        if (nums[i] != nums[k - 1]) {
            nums[k] = nums[i];
            k++;
        }
    }
    return k;
}
```

- **Time:** O(n) — single pass, one read pointer, one write pointer.
- **Space:** O(1) auxiliary — only the `k` counter.

**Why this is optimal:** Every element must be examined at least once to know whether it's a duplicate, so O(n) is a hard lower bound. Space is O(1) because the comparison only ever needs the single most recent kept value (`nums[k-1]`), never a full history — sortedness is what makes this possible, since it guarantees no "new" duplicate of an already-finalized value can appear later. The write pointer `k` never exceeds the read pointer `i` (since `k` only grows on strictly fewer occasions than `i` advances, once past the first element), so no unread data is destroyed.

## 6. Dry Run

Example: `nums = [0,0,1,1,1,2,2,3,3,4]`.
Chosen because it has runs of different lengths (two 0s, three 1s, two 2s, two 3s, one 4), exercising both single-duplicate and multi-duplicate runs.

Initial: `k = 1` (nums[0] = 0 is kept implicitly).

| i | nums[i] | nums[k-1] | Equal? | Action | nums state | k |
|---|---------|-----------|--------|--------|-------------|---|
| 1 | 0 | 0 | Yes | skip | [0,0,1,1,1,2,2,3,3,4] | 1 |
| 2 | 1 | 0 | No | nums[1] = 1, k++ | [0,1,1,1,1,2,2,3,3,4] | 2 |
| 3 | 1 | 1 | Yes | skip | [0,1,1,1,1,2,2,3,3,4] | 2 |
| 4 | 1 | 1 | Yes | skip | [0,1,1,1,1,2,2,3,3,4] | 2 |
| 5 | 2 | 1 | No | nums[2] = 2, k++ | [0,1,2,1,1,2,2,3,3,4] | 3 |
| 6 | 2 | 2 | Yes | skip | [0,1,2,1,1,2,2,3,3,4] | 3 |
| 7 | 3 | 2 | No | nums[3] = 3, k++ | [0,1,2,3,1,2,2,3,3,4] | 4 |
| 8 | 3 | 3 | Yes | skip | [0,1,2,3,1,2,2,3,3,4] | 4 |
| 9 | 4 | 3 | No | nums[4] = 4, k++ | [0,1,2,3,4,2,2,3,3,4] | 5 |

Exit condition: `i` reaches `nums.length` (10), loop ends.

**Final answer:** `k = 5`, and `nums[0..4] = [0,1,2,3,4]`, matching the expected output exactly, in correct sorted order.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Collect uniques into list, copy back | O(n) | O(n) | Correct but ignores that sortedness makes a full history unnecessary |
| 2. Slow/fast two pointers | O(n) | O(1) | Optimal; relies on sortedness so only the last kept value needs checking |

Input/output space for `nums` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single element array (`nums.length == 1`) | Loop body never executes (starts at `i = 1`); must still return correctly | `k` is initialized to 1 and the `for` loop condition `1 < 1` is false, so it's skipped entirely — `k = 1` is returned, correctly reflecting one unique element |
| All elements identical | Should collapse to a single kept element | Every `nums[i] != nums[k-1]` check is false, so `k` never advances past 1 — final array effectively has just the one distinct value at index 0 |
| No duplicates at all | Every element should be kept, `k` should equal `nums.length` | Every comparison is true, so `k` advances every iteration, ending at `nums.length`, and the array is unchanged |
| Duplicates only at the very end (e.g., `[1,2,3,3,3]`) | Must correctly stop advancing `k` once trailing duplicates begin | The comparison against `nums[k-1]` (the last *kept* value, not the last *read* value) correctly keeps rejecting all trailing 3s after the first one is kept |
| Negative numbers (constraint allows `nums[i] >= -100`) | Comparison logic must not assume non-negative values | Plain `!=` comparison on `int` works identically regardless of sign; no special-casing needed |
| Boundary values (`-100` and `100`) | Could tempt an off-by-one if using range checks instead of equality | Code only ever uses direct equality/inequality comparison between two array elements, never a range check, so boundary values behave identically to any other value |

## 9. Java Notes

- **`!=` on primitive `int`:** Since `nums` is `int[]`, comparisons are plain value comparisons — no `Integer` boxing, no `==` vs `.equals()` ambiguity (that pitfall only applies to boxed `Integer[]` or wrapper collections, as seen in the brute-force `ArrayList<Integer>` approach).
- **Comparing against `nums[k-1]`, not `nums[i-1]`:** This is the crux of correctness — you must compare against the last value you actually *kept*, not the last value you merely *read*, since those diverge as soon as any duplicate has been skipped. Mixing these up is the most common subtle bug on this problem.
- **No `ArrayList`/boxing needed in the optimized solution:** staying within the primitive `int[]` avoids the boxing overhead that Approach 1 incurs via `ArrayList<Integer>`.

## 10. Common Mistakes

- **Comparing `nums[i]` to `nums[i-1]` instead of `nums[k-1]`.** Once a duplicate has been skipped, `nums[i-1]` may still hold stale, not-yet-overwritten data that doesn't reflect what was actually kept, silently causing extra values to slip through as "unique." Fix: always compare against the last *written* value at `nums[k-1]`.
- **Starting `k` at 0 instead of 1.** This makes the first comparison `nums[1] != nums[-1]`, an out-of-bounds access, or otherwise mishandles the first element, which is always trivially unique. Fix: initialize `k = 1` and start the read pointer `i` at index 1, treating index 0 as automatically kept.
- **Using a `HashSet` to detect duplicates.** This works but discards the fact that the array is sorted, uses unnecessary O(n) space, and can reorder elements if not carefully rebuilt — it's overkill and easy to get wrong regarding order preservation. Fix: exploit sortedness with the two-pointer approach instead.
- **Forgetting the array must remain sorted/order-preserved and using swap-based removal (like in LC 27's alternate approach).** Swapping can move a later, larger element into an earlier position, breaking the required sorted order of the first `k` elements. Fix: always shift (copy forward), never swap, when order must be preserved.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Sorted array + remove duplicates in place, order preserved → slow/fast two pointers comparing each new element only to the last *kept* value."
- **90-second explanation:** "Since the array is sorted, every duplicate of a value sits right next to other copies of it, so I never need to remember more than the single most recently kept value. I use a write pointer `k` starting at 1, since the first element is trivially unique, and a read pointer `i` starting at 1. For each `i`, I compare `nums[i]` to `nums[k-1]` — the last value I actually kept, not just the last one I read. If they differ, it's a new unique value, so I copy it to `nums[k]` and advance `k`; if they're equal, it's a duplicate and I skip it. This runs in O(n) time and O(1) extra space, and because I always compare to the last *kept* value rather than the last *read* value, duplicates of any length are correctly collapsed."
- **Related problems using this pattern:**
  - LeetCode 27 — Remove Element
  - LeetCode 80 — Remove Duplicates from Sorted Array II
  - LeetCode 83 — Remove Duplicates from Sorted List
  - LeetCode 82 — Remove Duplicates from Sorted List II

## 12. Recall Questions

**Q:** Why is it sufficient to compare each element only to the *last kept* value, rather than checking against all previously seen values?
**A:** Because the array is sorted, every occurrence of a given value is contiguous, so once a value has been finalized as unique, no later duplicate of it can appear — only the immediately preceding kept value is ever relevant.

**Q:** Why must the comparison be against `nums[k-1]` and not `nums[i-1]`?
**A:** `nums[i-1]` may hold stale data that was already overwritten or was itself a duplicate that got skipped, while `nums[k-1]` always reflects the true last value that was actually kept.

**Q:** Why is `k` initialized to 1 instead of 0 at the start?
**A:** The first element of the array has nothing before it to be a duplicate of, so it's automatically unique and already correctly in place at index 0; `k` starts at 1 to reflect that one element is already kept.

**Q:** Why can't you use a swap-based removal technique here the way you might for Remove Element (LC 27)?
**A:** Swapping can pull a larger, later element into an earlier position, which would break the requirement that the first `k` elements remain in sorted order.

**Q:** If the array has no duplicates at all, what value does `k` end up with, and why?
**A:** `k` ends up equal to `nums.length`, because every element differs from the previous kept value, so the write pointer advances on every single iteration.

## 13. Final Code

```java
public int removeDuplicates(int[] nums) {
    int k = 1;
    for (int i = 1; i < nums.length; i++) {
        if (nums[i] != nums[k - 1]) {
            nums[k++] = nums[i];
        }
    }
    return k;
}
```

## 14. Self-Test

You have a sorted (non-decreasing) array `nums`. Remove duplicates in place so each unique value appears once, preserving relative order, and return `k`, the count of unique values, with those values occupying `nums[0..k-1]` in sorted order. Think about why sortedness means you only ever need to remember the single most recently kept value, not a full history of everything you've seen.
