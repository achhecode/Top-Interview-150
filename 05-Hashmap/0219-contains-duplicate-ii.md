---
problem: 219 - Contains Duplicate II
url: https://leetcode.com/problems/contains-duplicate-ii/
difficulty: Easy
section: Array / Hash Table
patterns: [hashmap, last-seen-index]
data_structures: [hashmap]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** an integer array `nums` and an integer `k`.

**Required:** return `true` if there exist two distinct indices `i` and `j` such that `nums[i] == nums[j]` **and** `abs(i - j) <= k` — that is, a repeated value whose two occurrences are within a window of `k` positions of each other.

**Constraints that matter:**
- `1 <= nums.length <= 10^5` — large enough that an O(n²) all-pairs check is too slow; O(n) is expected.
- `-10^9 <= nums[i] <= 10^9` — values fit safely in `int`; no overflow concerns since no arithmetic is performed on the values themselves, only equality comparisons.
- `0 <= k <= 10^5` — `k` can be `0`, meaning the two indices would have to be identical, which is impossible for **distinct** indices, so the answer is always `false` when `k = 0`; this boundary is worth handling correctly rather than assuming `k >= 1`.

## 2. Recognition Signals

- "Two equal elements within a distance of `k`" → this is a **proximity-constrained duplicate check**, a step beyond plain "contains a duplicate" (LeetCode 217), adding an index-distance bound.
- The natural reformulation "for each value, is the gap between consecutive occurrences ever `<= k`?" → only the **most recent** occurrence of each value matters at any point in the scan, not the full history of where it's appeared — this is the tell for tracking **last-seen index** rather than all occurrences.
- A fixed window of allowed distance `k` combined with a duplicate check → also solvable as a **sliding window of size k using a `HashSet`**, an equally valid framing of the same underlying idea.
- Named pattern: **Hash Map of Last-Seen Index** (a specialization of the complement-lookup family, here keyed on equality rather than a computed complement).

## 3. Core Idea

Scan the array once, keeping a map from each value to the index of its most recent occurrence. For each new element, if that value was seen before, check whether the current index minus the last-seen index is `<= k`; if so, a valid pair exists. Otherwise, update the map with the current (more recent) index and continue.

**Why it's correct:** if any valid pair `(i, j)` with `nums[i] == nums[j]` and `j - i <= k` exists, then in particular the pair formed by the two **closest** occurrences of that value (in index terms) is at least as close together as any other pair for that value. So it suffices to compare each occurrence only against the immediately preceding occurrence of the same value — if even the closest pair for a value fails the distance check, no farther-apart pair for that value could possibly succeed either, since farther-apart pairs only increase the gap.

**Invariant:** at the moment index `i` is processed, `lastSeen.get(nums[i])` (if present) holds the largest index `< i` where the same value previously occurred — the closest possible prior match — so the distance check `i - lastSeen.get(nums[i]) <= k` is both necessary and sufficient to determine whether *any* valid pair involving this value and an earlier occurrence exists up to this point.

## 4. Approach 1 — Brute Force

1. For every pair of indices `i < j`, check whether `nums[i] == nums[j]` and `j - i <= k`.
2. Return `true` as soon as such a pair is found.
3. If no pair satisfies both conditions after checking all pairs, return `false`.

```java
public boolean containsNearbyDuplicateBruteForce(int[] nums, int k) {
    int n = nums.length;
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j <= Math.min(i + k, n - 1); j++) {
            if (nums[i] == nums[j]) {
                return true;
            }
        }
    }
    return false;
}
```

**Time:** O(n · k) — for each of n starting indices, up to `k` subsequent indices are checked (bounded by the array length).
**Space:** O(1) auxiliary.

This is not optimal: with `n` and `k` both up to `10^5`, O(n · k) can approach 10^10 in the worst case, far too slow. It also repeatedly re-scans overlapping windows without reusing any information about where values were previously seen.

## 5. Approach 2 — Optimized (Hash Map of Value → Last-Seen Index)

1. Create `Map<Integer, Integer> lastSeen` (value → most recent index it was seen at).
2. Iterate `i` from `0` to `n - 1`:
   - If `lastSeen.containsKey(nums[i])` and `i - lastSeen.get(nums[i]) <= k`, return `true` immediately.
   - Update `lastSeen.put(nums[i], i)` regardless (whether or not a match was found, the current index becomes the new "most recent" for this value).
3. If the loop completes without finding a valid pair, return `false`.

```java
public boolean containsNearbyDuplicate(int[] nums, int k) {
    Map<Integer, Integer> lastSeen = new HashMap<>();

    for (int i = 0; i < nums.length; i++) {
        if (lastSeen.containsKey(nums[i]) && i - lastSeen.get(nums[i]) <= k) {
            return true;
        }
        lastSeen.put(nums[i], i);
    }

    return false;
}
```

**Time:** O(n) — a single pass through `nums`, with O(1) average-case map lookups and insertions.
**Space:** O(min(n, d)) auxiliary, where `d` is the number of distinct values in `nums` — the map holds at most one entry per distinct value.

This is optimal: every element must be inspected at least once, so O(n) is the best possible time, and tracking only the single most recent index per value (rather than every occurrence) is the minimal information needed, since only the closest prior occurrence can ever produce the smallest, most favorable distance for a match.

## 6. Dry Run

`nums = [1, 2, 3, 1, 2, 3]`, `k = 2`

| i (nums[i]) | lastSeen has value? | distance check | action | lastSeen after |
|---|---|---|---|---|
| 0 (1) | no | — | record 1→0 | {1:0} |
| 1 (2) | no | — | record 2→1 | {1:0, 2:1} |
| 2 (3) | no | — | record 3→2 | {1:0, 2:1, 3:2} |
| 3 (1) | yes, at 0 | 3-0=3 > 2 | no match; update 1→3 | {1:3, 2:1, 3:2} |
| 4 (2) | yes, at 1 | 4-1=3 > 2 | no match; update 2→4 | {1:3, 2:4, 3:2} |
| 5 (3) | yes, at 2 | 5-2=3 > 2 | no match; update 3→5 | {1:3, 2:4, 3:5} |

Exit: loop completes all 6 elements without ever satisfying the distance check. Final answer: `false`, matching the expected output.

Contrast with `nums = [1, 2, 3, 1]`, `k = 3`: at `i=3` (value 1), `lastSeen` has `1→0`, and `3 - 0 = 3 <= 3` → returns `true` immediately, matching that example.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute Force | O(n · k) | O(1) aux | Re-checks overlapping windows from scratch at every index |
| Hash Map of Last-Seen Index | O(n) | O(min(n,d)) aux | d = number of distinct values; map holds at most one index per value |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| `k = 0` | Indices must be distinct, so a zero-distance match is impossible | `i - lastSeen.get(nums[i]) <= 0` can never be true for `i != lastSeen index`, since the map always stores a strictly earlier index; correctly always yields `false` for k=0 |
| `k >= nums.length` | Effectively removes the distance constraint entirely; reduces to plain "contains a duplicate anywhere" | The distance check `i - lastSeen.get(...) <= k` becomes trivially true whenever any earlier occurrence exists, since the maximum possible gap is `n - 1 <= k` |
| All elements identical (e.g. `nums = [1,1,1,1]`, small k) | Every consecutive pair is a candidate | The very first repeat (`i=1` vs `lastSeen[1]=0`) is checked against `k`; if `k >= 1` it returns `true` immediately |
| No duplicates at all in `nums` | `lastSeen.containsKey` never true | Loop completes fully, returns `false` |
| Duplicate values far apart (further than k), but a *later* pair close together (e.g. `nums=[1,2,1,2,1]` with only the last two 1's close) | Must find the closest pair, not just any pair | Because `lastSeen` always stores the *most recent* occurrence (overwritten every time a value is seen again, whether or not it matched), the algorithm always compares against the closest possible prior occurrence at each step |
| Negative values and values at the ±10^9 boundary | No arithmetic performed on values themselves | `HashMap<Integer, Integer>` keys handle any `int` value uniformly; only index arithmetic (`i - lastSeen.get(...)`) is performed, which stays well within `int` range since indices are bounded by `n <= 10^5` |

## 9. Java Notes

- `lastSeen.put(nums[i], i)` is called **unconditionally** on every iteration, even when a match was just found (though the method returns before reaching it in that case) or when no match exists — this ensures the map always reflects the most recent index for each value, which is essential for the "closest pair" correctness argument.
- `Map<Integer, Integer>` autoboxes both keys and values; for `n` up to `10^5`, this overhead is acceptable, though a more specialized primitive-keyed structure would avoid it if performance were critical at larger scale.
- `i - lastSeen.get(nums[i])` involves unboxing the `Integer` returned by `get()` for the subtraction — safe here since `containsKey` was already checked, guaranteeing a non-null result.
- No overflow risk in `i - lastSeen.get(...)` since both `i` and the stored index are bounded by `0` to `n - 1 <= 10^5 - 1`, far within `int` range.

## 10. Common Mistakes

- Only updating `lastSeen` when no match is found (skipping the update on a match, since the function is about to return anyway) — technically harmless here since the function returns immediately on a match, but conceptually risky if the logic were ever refactored to continue scanning (e.g., to count all valid pairs) instead of short-circuiting. Fix: always update unconditionally to keep the invariant robust regardless of how the surrounding logic might change.
- Using a `List<Integer>` per value to store *all* occurrences and checking the most recent one manually — functionally correct but unnecessarily complex and memory-heavy compared to simply overwriting a single `Integer` per key, since only the closest prior occurrence ever matters. Fix: store just the single most recent index per value.
- Checking `Math.abs(i - lastSeen.get(nums[i])) <= k` instead of `i - lastSeen.get(nums[i]) <= k` — unnecessary, since the scan always processes indices in increasing order, so `i` is always `>=` the stored last-seen index, making the absolute value redundant (though not incorrect) but a minor sign of not having reasoned about the invariant. Fix: drop the `Math.abs` since the subtraction is always non-negative in this scan order.
- Forgetting that "distinct indices" rules out `k = 0` ever returning `true`, and instead writing `i - lastSeen.get(nums[i]) < k` (strict less-than) — this incorrectly excludes the valid boundary case where the distance is exactly `k`. Fix: use `<=`, matching the problem's explicit `abs(i - j) <= k` condition.

## 11. Interview Takeaway

- **Trigger sentence:** "Duplicate value within a bounded index distance → hash map tracking each value's most recent index, single pass, check the gap before updating."
- **90-second explanation:** "I scan the array once, keeping a map from each value to the index where it was most recently seen. At each new index, if the current value has appeared before, I check whether the gap to that last occurrence is within `k`. Because I always overwrite the map with the most recent index for a value — whether or not a match was found — I'm always comparing against the closest possible prior occurrence, which is the best chance of satisfying the distance bound. If even the closest pair fails the check, no farther-apart pair for that value could succeed either, so I don't need to track every past occurrence, just the latest one. This gives a single O(n) pass instead of checking all pairs."
- **Related problems:** 217 (Contains Duplicate), 220 (Contains Duplicate III), 1 (Two Sum), 3 (Longest Substring Without Repeating Characters).

## 12. Recall Questions

**Q:** Why is it sufficient to compare each element only against the *most recent* prior occurrence of the same value, rather than all prior occurrences?
**A:** If the closest pair of occurrences for a value doesn't satisfy the distance bound, no farther-apart pair for that same value could possibly satisfy it either, since increasing the gap between indices only makes the distance check harder to pass, never easier.

**Q:** Why must `lastSeen` be updated on every iteration, not just when no match was found?
**A:** The correctness argument relies on the map always holding the truly most recent index for each value; if updates were skipped in some cases, later comparisons could end up checking against a stale, farther-away index instead of the closest one, potentially missing a valid pair.

**Q:** Why does the algorithm always return `false` when `k = 0`, and is that the correct behavior?
**A:** The problem requires two *distinct* indices, so a distance of exactly `0` between two occurrences would mean they're the same index, which is disallowed — since `lastSeen` always stores a strictly earlier index than the current one, the gap can never be `0`, correctly making `k=0` always yield `false`.

**Q:** Why doesn't the distance check need `Math.abs()` around the subtraction?
**A:** The scan processes indices in strictly increasing order, and `lastSeen` only ever stores indices that occurred earlier in the scan, so the current index `i` is always greater than or equal to the stored last-seen index, making the subtraction always non-negative without needing an absolute value.

**Q:** How would you describe the relationship between this problem and Contains Duplicate (LeetCode 217)?
**A:** This problem is a generalization of the simpler "does any duplicate exist" question, adding a constraint on how far apart the two occurrences can be — when `k` is large enough to cover the whole array, the two problems become equivalent.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public boolean containsNearbyDuplicate(int[] nums, int k) {
        Map<Integer, Integer> lastSeen = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {
            if (lastSeen.containsKey(nums[i]) && i - lastSeen.get(nums[i]) <= k) {
                return true;
            }
            lastSeen.put(nums[i], i);
        }

        return false;
    }
}
```

## 14. Self-Test

Given an integer array and an integer `k`, determine whether two distinct indices exist holding equal values that are at most `k` apart. Re-derive: scan once with a hash map from value to its most recent index; at each element, if the value was seen before, check whether the current index minus the stored index is within `k`, then always overwrite the stored index with the current one before moving on.
