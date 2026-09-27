---
problem: 167 - Two Sum II - Input Array Is Sorted
url: https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/
difficulty: Medium
section: Array / Two Pointers
patterns: [two-pointers, converging-search]
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

**Given:** A 1-indexed array `numbers`, sorted in non-decreasing order, and a `target` integer.

**Required:** Find two distinct indices `index1 < index2` such that `numbers[index1] + numbers[index2] == target`, and return them as `[index1, index2]` (1-indexed).

**Constraints that actually matter:**
- `2 <= numbers.length <= 3 * 10^4` — moderate size; rules out anything worse than roughly O(n log n), but a linear two-pointer scan is both possible and expected given the sortedness.
- **`numbers` is sorted in non-decreasing order** — this is the single most important constraint. Sortedness is exactly what enables a two-pointer approach to work in O(n) time and O(1) space, since moving a pointer in a specific direction has a predictable, monotonic effect on the resulting sum.
- **Exactly one solution is guaranteed to exist**, and the same element can't be used twice — removes the need to handle "no solution" or to worry about picking the same index for both numbers.
- **The solution must use only constant extra space** — this explicit requirement directly rules out the classic unsorted "Two Sum" (LC 1) approach of using a hash map (O(n) space) to achieve O(n) time; here, sortedness must be exploited instead to achieve O(n) time with O(1) space.

## 2. Recognition Signals

- "Sorted array" + "find a pair summing to a target" + "O(1) extra space" → this exact combination is the signature of the **two-pointer converging search** technique — distinct from the unsorted Two Sum problem (LC 1), which uses a hash map instead since it lacks the sortedness to exploit.
- Whenever an array is sorted and you need to find a pair (or evaluate how a pair's sum relates to a target) — starting pointers at both ends and moving them inward based on whether the current sum is too high or too low is a direct, reliable technique, since sortedness guarantees each pointer movement has a predictable, monotonic effect on the sum.
- The explicit "constant extra space" requirement is itself a strong signal steering away from the hash-map approach that would otherwise be the default instinct from having solved the unsorted version of this problem before.

**Pattern:** Two Pointers, Converging from Both Ends (a classic technique for sorted-array pair-sum problems: start at both extremes and move inward based on whether the current sum is too small or too large).

## 3. Core Idea

Since `numbers` is sorted, place one pointer, `left`, at the very first index and another, `right`, at the very last index. Compute `sum = numbers[left] + numbers[right]`. If `sum == target`, the pair is found. If `sum < target`, the current pair's sum is too small — since the array is sorted, the *only* way to increase the sum (while keeping the pointers valid and distinct) is to move `left` rightward to a larger value (moving `right` leftward would only decrease or maintain the sum, never help). Symmetrically, if `sum > target`, move `right` leftward to decrease the sum. Repeat until the pair is found (guaranteed to exist).

**Invariant:** At every step, if a valid solution pair `(i, j)` with `i < j` exists, it's guaranteed to still lie within the current `[left, right]` window — because whichever pointer gets moved is moved specifically because it could never have been part of *any* still-remaining valid pair at its old position (given the current sum is definitively too high or too low, and sortedness makes this determination monotonic and reliable).

## 4. Approach 1 — Brute Force

**Logic:** Try every pair of indices `(i, j)` with `i < j`, checking whether their values sum to `target`.

```java
public int[] twoSum(int[] numbers, int target) {
    int n = numbers.length;

    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (numbers[i] + numbers[j] == target) {
                return new int[]{i + 1, j + 1};
            }
        }
    }

    return new int[]{};
}
```

- **Time:** O(n²) — nested loop checking all pairs.
- **Space:** O(1) auxiliary — no extra data structures beyond the output array.

**Why it is not optimal:** With `n` up to `3 * 10^4`, O(n²) risks up to `~4.5 * 10^8` operations in the worst case, likely too slow. It also completely ignores the fact that the array is sorted, which is specifically given as a exploitable property to reach a better time complexity.

## 5. Approach 2 — Optimized (Two Pointers, Converging)

**Algorithm:**
1. Initialize `left = 0` and `right = numbers.length - 1` (0-indexed internally; convert to 1-indexed only in the final returned result).
2. While `left < right`:
   - Compute `sum = numbers[left] + numbers[right]`.
   - If `sum == target`, return `[left + 1, right + 1]` (converting to 1-indexed).
   - If `sum < target`, increment `left` (need a larger sum, and sortedness guarantees moving `left` rightward is the only way to achieve that while keeping the pair valid).
   - If `sum > target`, decrement `right` (need a smaller sum).
3. (The problem guarantees exactly one solution exists, so the loop is guaranteed to find and return it before `left` and `right` cross.)

```java
public int[] twoSum(int[] numbers, int target) {
    int left = 0;
    int right = numbers.length - 1;

    while (left < right) {
        int sum = numbers[left] + numbers[right];
        if (sum == target) {
            return new int[]{left + 1, right + 1};
        } else if (sum < target) {
            left++;
        } else {
            right--;
        }
    }

    return new int[]{};
}
```

- **Time:** O(n) — `left` and `right` together traverse the array at most once each, converging toward each other; total pointer movements across the whole run are bounded by `n`.
- **Space:** O(1) auxiliary — just the two pointer variables and a sum variable, satisfying the problem's explicit constant-space requirement.

**Why this is optimal:** Every element potentially needs to be examined at least once to determine its role in the sum, so O(n) is a hard lower bound, and this approach achieves it with a single converging pass. Space is O(1) because sortedness alone provides enough information to make a correct, monotonic decision about which pointer to move at each step — no auxiliary hash map or other data structure (which the unsorted LC 1 variant requires) is needed here, directly satisfying the problem's explicit constant-space constraint.

## 6. Dry Run

Example: `numbers = [2,7,11,15]`, `target = 9`.
Chosen because it's the canonical example, and while the answer is found on the very first check, it's worth tracing the pointer logic explicitly to confirm the mechanics.

`n = 4`. Initial: `left = 0`, `right = 3`.

| Step | numbers[left] | numbers[right] | sum | sum vs target | Action | left/right after |
|------|-----------------|--------------------|-----|--------------------|--------|--------------------------|
| 1 | 2 | 15 | 17 | 17 > 9 | right-- | left=0, right=2 |
| 2 | 2 | 11 | 13 | 13 > 9 | right-- | left=0, right=1 |
| 3 | 2 | 7 | 9 | 9 == 9 | return [1,2] | — |

Exit condition: `sum == target` is found at `left=0, right=1`, triggering an immediate return.

**Final answer:** `[1, 2]` (1-indexed), matching the expected output exactly — `numbers[1-indexed 1] = 2` and `numbers[1-indexed 2] = 7`, summing to 9.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Brute force, all pairs | O(n²) | O(1) | Correct but ignores sortedness; too slow for n up to 3*10^4 |
| 2. Two pointers, converging | O(n) | O(1) | Optimal; exploits sortedness to satisfy both the time and explicit space requirement |

Input/output space for `numbers` is O(n) in both, as given by the problem; the returned pair itself is O(1) additional space (a fixed-size 2-element array).

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Minimum-length array (`n = 2`) | Only one possible pair exists; must be checked correctly (and the guarantee ensures it's the answer) | `left=0, right=1` on the first iteration checks the only possible pair directly, and since a solution is guaranteed to exist, this must be the found match |
| Negative numbers involved (e.g. `numbers = [-1,0]`, `target = -1`, per example 3) | Sum comparisons must work correctly with negative values, not just positive ones | Plain integer addition and comparison (`sum < target`, `sum > target`) work identically regardless of sign, with no special-casing needed for negative values |
| The two numbers needed are adjacent in the array | Should be found without issue as the pointers converge | Since the pointers move monotonically toward each other based on sum comparisons, adjacent valid indices are naturally reached as `left` and `right` close in on them, regardless of their specific positions |
| The two numbers needed are the very first and very last elements | Should be found immediately on the first iteration | As shown conceptually in the dry run's structure, if `numbers[0] + numbers[n-1] == target`, the very first sum computed already matches, returning immediately |
| Duplicate values in the array (e.g., multiple entries equal to the same value) | Must correctly use two *different* indices, even if their values happen to be equal | Since `left` and `right` always refer to distinct indices (enforced by the `left < right` loop condition) and the problem guarantees exactly one valid solution exists, the algorithm naturally converges on the correct distinct pair of indices without needing extra logic to avoid reusing the same index |
| Sum exactly at the boundary of `target` on either side (off-by-one proximity) | Comparisons must be precise (`==`, `<`, `>`), not off by one in either direction | The three-way comparison (`sum == target`, `sum < target`, `sum > target`) exhaustively and correctly covers every possible relationship between `sum` and `target`, with no gap or overlap between the cases |

## 9. Java Notes

- **1-indexed output vs. 0-indexed internal array access:** the problem explicitly requires 1-indexed indices in the returned result, even though Java arrays are inherently 0-indexed — the `+1` conversion (`left + 1`, `right + 1`) at the point of returning the answer is essential and a common detail to get right (or forget) in this specific problem.
- **No overflow risk:** given `-1000 <= numbers[i] <= 1000`, the maximum possible sum magnitude is `2000`, comfortably within `int` range with enormous headroom, so no `long` arithmetic is needed.
- **No auxiliary `HashMap` needed, unlike the unsorted Two Sum (LC 1):** this is the key Java-level distinction from the more commonly encountered unsorted variant — here, the sortedness constraint plus the explicit O(1) space requirement together steer the solution away from a hash-map-based approach entirely.
- **Returning `new int[]{}` as a fallback:** technically unreachable given the problem's guarantee of exactly one solution, but included for code completeness/compilability, since Java requires all code paths to return a value.

## 10. Common Mistakes

- **Forgetting to convert the final indices to 1-indexed before returning.** Returning `[left, right]` directly (0-indexed) instead of `[left + 1, right + 1]` would fail against the problem's explicit 1-indexed requirement. Fix: always add 1 to both `left` and `right` at the point of constructing the returned array.
- **Reaching for a `HashMap`-based solution out of habit (carried over from the unsorted Two Sum, LC 1) without noticing the sortedness and explicit O(1) space constraint here.** While a hash-map approach would still be O(n) time and technically correct, it violates the problem's explicit constant-space requirement and misses the intended lesson of exploiting sortedness. Fix: recognize the sorted-array + O(1)-space combination as the specific signal for the two-pointer technique instead.
- **Moving the wrong pointer when the sum doesn't match the target (e.g., decrementing `right` when the sum is too small, or incrementing `left` when the sum is too large).** This breaks the correctness of the converging search, since sortedness only guarantees the correct monotonic effect when the pointer is moved in the right direction relative to the current sum's relationship to the target. Fix: always increment `left` when `sum < target` (to increase the sum) and decrement `right` when `sum > target` (to decrease the sum) — never the reverse.
- **Using `<=` instead of `<` in the main loop's condition (`while (left <= right)`), potentially allowing `left` and `right` to refer to the same index.** The problem explicitly requires two distinct indices, so allowing `left == right` could, in principle, permit an invalid same-element pairing to be considered. Fix: always use strict `left < right` as the loop condition.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Sorted array, find a pair summing to a target, O(1) extra space → two pointers starting at both ends, moving inward based on whether the current sum is too small or too large."
- **90-second explanation:** "Since the array is sorted, I can use two pointers, one starting at the very first element and one at the very last. I compute their sum: if it exactly matches the target, I've found my answer. If the sum is too small, I know moving the right pointer leftward could only make the sum smaller or keep it the same, which doesn't help — so the only productive move is to advance the left pointer rightward to a larger value, increasing the sum. Symmetrically, if the sum is too large, I move the right pointer leftward to decrease it. I repeat this process, and since the problem guarantees exactly one valid solution exists, this converging search is guaranteed to find it before the two pointers cross. Because I'm only ever using two index variables and exploiting the array's existing sorted order rather than any auxiliary data structure, this runs in O(n) time and O(1) extra space — directly satisfying the problem's explicit space constraint, unlike the hash-map approach typically used for the unsorted version of this problem."
- **Related problems using this pattern:**
  - LeetCode 1 — Two Sum (the unsorted variant, solved with a hash map instead since sortedness isn't available)
  - LeetCode 15 — 3Sum (extends this two-pointer technique with an outer loop fixing one element)
  - LeetCode 16 — 3Sum Closest (similar extension, finding the closest sum rather than an exact match)
  - LeetCode 259 — 3Sum Smaller (another extension of the same converging two-pointer idea)

## 12. Recall Questions

**Q:** Why does the array being sorted specifically enable the two-pointer technique to correctly and efficiently find the target pair?
**A:** Sortedness guarantees that moving the left pointer rightward can only increase (or keep equal) the value it points to, and moving the right pointer leftward can only decrease (or keep equal) its value, so each pointer movement has a predictable, monotonic effect on the sum, allowing the search to correctly narrow down toward the answer without missing it.

**Q:** Why is it correct to move the left pointer rightward (rather than the right pointer leftward) when the current sum is smaller than the target?
**A:** Moving the right pointer leftward would only decrease or maintain the sum, which moves further away from the target rather than closer to it, so the only pointer movement that could possibly increase the sum toward the target is advancing the left pointer to a larger value.

**Q:** Why does this problem's explicit "constant extra space" requirement rule out the hash-map approach commonly used for the unsorted version of Two Sum?
**A:** A hash map used to track previously seen values (as in the unsorted variant) requires space proportional to the number of elements stored, which is O(n) auxiliary space, directly violating the O(1) extra space constraint explicitly stated in this problem.

**Q:** Why must the final returned indices be converted from the algorithm's internal 0-indexed positions to 1-indexed positions before returning?
**A:** The problem explicitly defines and requires the output to use 1-indexed positions, even though the underlying Java array itself is naturally accessed using standard 0-indexed positions internally, so an explicit `+1` adjustment is needed to match the required output format.

**Q:** Why is it guaranteed that the converging two-pointer search will never accidentally skip over the correct answer pair?
**A:** At every step, whichever pointer gets moved is moved specifically because its current position could not possibly be part of any remaining valid solution (given the current sum is definitively too high or too low relative to the target), so the guaranteed valid solution pair always remains within the shrinking search window until it's found.

## 13. Final Code

```java
public int[] twoSum(int[] numbers, int target) {
    int left = 0;
    int right = numbers.length - 1;

    while (left < right) {
        int sum = numbers[left] + numbers[right];
        if (sum == target) {
            return new int[]{left + 1, right + 1};
        } else if (sum < target) {
            left++;
        } else {
            right--;
        }
    }

    return new int[]{};
}
```

## 14. Self-Test

You have a 1-indexed array `numbers`, sorted in non-decreasing order, and a `target`. Find two distinct indices whose values sum to `target`, using only constant extra space. Think about why sortedness lets you start with two pointers at both ends and move them inward based on whether the current sum is too small or too large, without ever needing a hash map.
