---
problem: 1 - Two Sum
url: https://leetcode.com/problems/two-sum/
difficulty: Easy
section: Array / Hash Table
patterns: [hashmap, complement-lookup]
data_structures: [hashmap, array]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** an array of integers `nums` and an integer `target`.

**Required:** return the indices of the two numbers that add up exactly to `target`. Exactly one valid answer is guaranteed to exist, the same element cannot be used twice, and the answer can be returned in any order.

**Constraints that matter:**
- `2 <= nums.length <= 10^4` — small enough that O(n²) would technically pass, but the problem's own follow-up explicitly asks for better than O(n²), signaling the intended answer is O(n).
- `-10^9 <= nums[i], target <= 10^9` — values fit safely in `int`, and summing two of them (max magnitude ~2×10^9) still fits within `int`'s range (~±2.1×10^9), though it's worth a deliberate check rather than an assumption, especially near the boundary.
- "Only one valid answer exists" — this guarantee simplifies the algorithm: no need to search for multiple solutions or handle ambiguity; the first match found during a single pass is the answer.

## 2. Recognition Signals

- "Two numbers that add up to a target" + "return their indices" → the canonical **Two Sum** shape, the most common template for hash-map complement lookups.
- Unsorted array with no structural property to exploit (no sorted-order guarantee) → rules out a pure two-pointer approach without first sorting (and sorting would lose original indices unless tracked separately), pointing instead toward a **hash map for O(1) average lookups**.
- "Exactly one solution exists" + need indices (not values) → a single linear pass checking each element against previously-seen elements is sufficient; no need to consider all pairs.
- Named pattern: **Complement Lookup via Hash Map**.

## 3. Core Idea

For each element, compute what value (the "complement") would need to have appeared earlier in the array for the pair to sum to `target`. Check a hash map of previously-seen values (mapped to their indices) for that complement — if found, the pair is complete; if not, record the current value and index for future lookups.

**Why it's correct:** if `nums[i] + nums[j] == target` for some `i < j`, then by the time the scan reaches index `j`, `nums[i]` will already be in the map (since `i < j` and the map is populated left to right). So checking `target - nums[j]` against the map at index `j` is guaranteed to find `nums[i]` if the pair exists, and since exactly one solution is guaranteed, the first match found is correct.

**Invariant:** at the start of processing index `i`, the map contains exactly the values and indices of `nums[0..i-1]` — every element seen so far and nothing from the future, which is what guarantees indices are never reused and the pair found (if any) uses two genuinely distinct positions.

## 4. Approach 1 — Brute Force

1. For every pair of indices `i < j`, check whether `nums[i] + nums[j] == target`.
2. Return the first pair found, as `[i, j]`.

```java
public int[] twoSumBruteForce(int[] nums, int target) {
    int n = nums.length;
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (nums[i] + nums[j] == target) {
                return new int[] { i, j };
            }
        }
    }
    return new int[0]; // unreachable per problem guarantee, but keeps the method total
}
```

**Time:** O(n²) — every pair of indices is checked in the worst case.
**Space:** O(1) auxiliary.

This is not optimal: with `n` up to `10^4`, O(n²) is up to ~10^8 operations, which is borderline acceptable here but explicitly called out by the problem's own follow-up as beatable — it does unnecessary repeated comparisons instead of reusing information from earlier elements.

## 5. Approach 2 — Optimized (Hash Map of Value → Index, Single Pass)

1. Create `Map<Integer, Integer> seen` (value → index).
2. Iterate `i` from `0` to `n - 1`:
   - Compute `complement = target - nums[i]`.
   - If `seen.containsKey(complement)`, return `new int[] { seen.get(complement), i }` — a match found using an earlier element.
   - Otherwise, record `seen.put(nums[i], i)` for future lookups.
3. (Per the problem's guarantee, this loop always finds and returns an answer before completing.)

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>();

    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (seen.containsKey(complement)) {
            return new int[] { seen.get(complement), i };
        }
        seen.put(nums[i], i);
    }

    return new int[0]; // unreachable per problem guarantee, but keeps the method total
}
```

**Time:** O(n) — a single pass through `nums`, with O(1) average-case map lookups and insertions.
**Space:** O(n) auxiliary — the map can hold up to `n - 1` entries by the time a match is found.

This is optimal: every element must be examined at least once to determine the answer, so O(n) is the best possible time, and the trade-off is O(n) space to avoid the brute force's repeated re-scanning — a textbook time-for-space exchange that directly answers the problem's own follow-up.

## 6. Dry Run

`nums = [3, 2, 4]`, `target = 6`

| i (nums[i]) | complement = target - nums[i] | seen.containsKey(complement)? | action | seen after |
|---|---|---|---|---|
| 0 (3) | 3 | no (`seen` is empty) | record 3→0 | {3:0} |
| 1 (2) | 4 | no | record 2→1 | {3:0, 2:1} |
| 2 (4) | 2 | **yes**, at index 1 | return [1, 2] | — |

Exit: the loop returns as soon as the match is found at `i = 2`. Final answer: `[1, 2]`, matching the expected output (`nums[1] + nums[2] = 2 + 4 = 6`).

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute Force | O(n²) | O(1) aux | Explicitly called out as beatable by the problem's own follow-up |
| Hash Map, Single Pass | O(n) | O(n) aux | Trades space for time; map holds at most n-1 entries |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Minimum array size (`n = 2`) | Only one possible pair exists | Loop checks the complement of `nums[1]` against `nums[0]`, already in the map by then |
| Duplicate values that form the answer (e.g. `nums=[3,3], target=6`) | Must use two distinct indices, not the same element twice | At `i=1`, `complement = 3` is looked up in `seen`, which already holds `3→0` from the previous iteration — index 0 and index 1 are correctly distinct |
| Negative numbers in `nums` or a negative `target` | Sum arithmetic must still work correctly | Plain integer subtraction/addition handles negative values with no special-casing needed |
| Complement equals the current element itself, but only one copy exists in `nums` | Must not incorrectly self-match before the second occurrence appears | The map is only populated with elements *before* index `i`, so `nums[i]` can never match against itself in the same iteration — a genuine second occurrence is required and must already be in `seen` |
| Values near the `±10^9` boundary | Sum could be large, risking overflow if unchecked | `int` addition of two ±10^9 values stays within `int` range (~±2.1×10^9 max magnitude), so no overflow occurs, though this is a boundary worth explicitly verifying rather than assuming |
| No brute-force-style ambiguity: guaranteed exactly one solution | Simplifies the algorithm, no need to search exhaustively for the best or all pairs | The single-pass early return is valid precisely because the problem guarantees uniqueness |

## 9. Java Notes

- `Map<Integer, Integer>` autoboxes both keys and values — for `n` up to `10^4` this overhead is negligible, but it's worth knowing that a primitive-keyed structure (not available built-in in Java without a third-party library) would avoid it in a hotter loop.
- `seen.containsKey(complement)` followed by `seen.get(complement)` is two lookups; since both are needed here (existence check, then the actual index value), this is idiomatic and clear — collapsing to one call via `getOrDefault` doesn't apply cleanly since `-1` (a common sentinel) could itself be a valid array index... actually index sentinels aren't an issue here since indices are always `>= 0`, so `getOrDefault(complement, -1) != -1` would also work as a valid one-lookup alternative.
- The map is populated **after** the complement check within the same iteration, not before — this single ordering detail is what correctly prevents an element from being paired with itself.
- No overflow risk for `int complement = target - nums[i]` given the stated constraints, but this is the kind of subtraction that would be worth double-checking against `Integer.MIN_VALUE`/`MAX_VALUE` boundaries in a variant problem with looser constraints.

## 10. Common Mistakes

- Populating the map with all elements *before* starting the complement-lookup loop (a two-pass approach), instead of checking-then-inserting within a single pass — this can incorrectly match an element with itself when `target == 2 * nums[i]` and there's only one occurrence of that value. Fix: check for the complement first, then insert the current element, all within the same single-pass iteration.
- Returning `seen.get(complement)` and `seen.get(nums[i])` (looking up the current index via the map) instead of using the loop variable `i` directly for the second index — unnecessarily complicates the code, though not incorrect if done carefully, since `i` is already known directly. Fix: use `i` directly rather than looking it up.
- Using a `Set<Integer>` instead of a `Map<Integer, Integer>` — a set can tell you *whether* the complement exists but not *at which index*, and the problem asks for indices, not values. Fix: a map (value → index) is required, not a set.
- Assuming the array is sorted and reaching for two pointers without first handling the fact that sorting would destroy the original index-to-value correspondence — attempting to "fix" this by sorting an array of index-value pairs adds unnecessary complexity compared to the direct hash map approach. Fix: use the hash map approach directly on the unsorted array.

## 11. Interview Takeaway

- **Trigger sentence:** "Find two elements in an unsorted array summing to a target, need their indices → hash map storing value-to-index, check for the complement before inserting each element."
- **90-second explanation:** "I scan the array once, and for each element I compute its complement — the value that would need to already exist earlier in the array for this pair to sum to the target. I check a hash map of previously-seen values and their indices for that complement. If it's there, I've found my answer immediately, using the stored index and the current index. If not, I record the current value and its index in the map before moving to the next element. This ordering — check first, then insert — is what prevents an element from being incorrectly paired with itself. Since exactly one solution is guaranteed, the first match found during this single pass is correct, giving me O(n) time at the cost of O(n) space for the map."
- **Related problems:** 15 (3Sum), 167 (Two Sum II - Input Array Is Sorted), 18 (4Sum), 1099 (Two Sum Less Than K).

## 12. Recall Questions

**Q:** Why must the complement check happen *before* inserting the current element into the map, not after?
**A:** Checking first ensures an element can never be paired with itself in the same iteration — if the insert happened first, an element could look up its own just-inserted entry and incorrectly report a match with itself when the target is exactly double that element's value.

**Q:** Why is a `Map<Integer, Integer>` needed here instead of a `Set<Integer>`?
**A:** The problem requires returning indices, not just confirming that a valid pair exists, and a set only tracks presence of values — a map is required to recover which index each previously-seen value came from.

**Q:** Why does the single-pass approach correctly rely on the guarantee that exactly one solution exists?
**A:** Returning immediately upon the first complement match found is only guaranteed correct because the problem promises there's no ambiguity — with multiple possible valid pairs, a single-pass early return could return a different (though still technically valid) pair than one might expect, which is fine here specifically because uniqueness is guaranteed.

**Q:** Why doesn't sorting the array first and using two pointers work as cleanly here as it does in similar problems?
**A:** Sorting would reorder the elements and destroy the correspondence between value and original index, so the two-pointer approach would need to separately track original indices alongside sorted values, adding complexity that the direct hash map approach avoids entirely.

**Q:** What invariant does the map maintain at the start of each loop iteration, and why does that matter for correctness?
**A:** At the start of processing index `i`, the map contains exactly the values and indices from `nums[0..i-1]` — nothing from the current or future elements — which guarantees that any match found uses two genuinely distinct, valid indices rather than reusing the same element.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            if (seen.containsKey(complement)) {
                return new int[] { seen.get(complement), i };
            }
            seen.put(nums[i], i);
        }

        return new int[0];
    }
}
```

## 14. Self-Test

Given an unsorted integer array and a target, find the indices of the two numbers that sum to the target (exactly one solution guaranteed, can't reuse the same element). Re-derive: scan once with a hash map from value to index; at each element, compute its complement and check the map for it before inserting the current element, returning the stored index and the current index as soon as a match is found.
