---
problem: 57 - Insert Interval
url: https://leetcode.com/problems/insert-interval/
difficulty: Medium
section: Array / Single Pass
patterns: [three-phase-scan, interval-merging]
data_structures: [array, list]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An array of `intervals`, already sorted by start value and guaranteed non-overlapping, plus a single `newInterval` to insert.

**Required:** Insert `newInterval` into the correct sorted position, merging it with any existing intervals it overlaps, and return the resulting sorted, non-overlapping list.

**Constraints that actually matter:**
- `0 <= intervals.length <= 10^4` — moderate size; and critically, **`intervals` is already sorted and non-overlapping** — this is the single biggest difference from the closely related "Merge Intervals" (LC 56) problem, and it's exactly what allows this problem to be solved in a single O(n) linear pass, without needing to sort anything.
- **"Two intervals are considered overlapping if they share at least one point"** — explicitly stated, meaning touching intervals (like `[1,2]` and `[2,3]`) also count as overlapping and must be merged, the same subtlety as in LC 56.
- `intervals` can be **empty** — inserting into an empty list should simply produce a single-element result containing just `newInterval`.
- Since the existing array is already sorted and non-overlapping, the intervals naturally fall into exactly three groups relative to `newInterval`: those that end **before** `newInterval` starts (completely unaffected, come first in the output), those that **overlap** with `newInterval` (must all be merged together into one combined interval), and those that start **after** `newInterval` ends (completely unaffected, come last in the output) — recognizing this three-way partition is the key structural insight of the problem.

## 2. Recognition Signals

- "Insert a new interval into an already-sorted, non-overlapping list of intervals, merging as needed" → distinct from the general "Merge Intervals" problem (LC 56) specifically because the **existing sortedness is already given**, which is the signal that a full re-sort is unnecessary — a single linear pass, split into three phases based on each existing interval's relationship to `newInterval`, is sufficient and optimal.
- The three-phase structure — "before" (no overlap, starts strictly after `newInterval` would end... actually ends strictly before `newInterval` starts), "overlapping" (must merge), "after" (no overlap, starts strictly after `newInterval` ends) — is itself the pattern to recognize: since the input is sorted, these three groups of intervals necessarily appear as three *contiguous* blocks in the original array, in exactly this order, making a single left-to-right scan sufficient to correctly classify and process every interval into the right bucket.
- Whenever inserting a new element into an already-sorted structure while maintaining sortedness and merging overlaps — exploit the existing sortedness rather than re-deriving it from scratch, which is exactly what separates this problem's optimal O(n) solution from a "safe but wasteful" O(n log n) solution that ignores the given sortedness.

**Pattern:** Three-Phase Single Pass (exploit the given sortedness of `intervals` to classify each interval, in one linear scan, into "comes before `newInterval`," "overlaps with and must merge into `newInterval`," or "comes after `newInterval`," assembling the result directly from these three phases in order).

## 3. Core Idea

Since `intervals` is already sorted by start value and non-overlapping, walk through it once, left to right, in three distinct phases:

1. **Phase 1 (no overlap, before):** while the current interval's end is strictly less than `newInterval`'s start (`intervals[i][1] < newInterval[0]`), this interval is entirely unaffected and comes before the merged range — add it directly to the result.
2. **Phase 2 (overlap, merge):** while the current interval's start is less than or equal to `newInterval`'s end (`intervals[i][0] <= newInterval[1]`), this interval overlaps with (or touches) the current working `newInterval` — expand `newInterval`'s bounds to absorb it: `newInterval[0] = min(newInterval[0], intervals[i][0])`, `newInterval[1] = max(newInterval[1], intervals[i][1])`. After this phase completes (no more overlapping intervals remain), add the now-fully-expanded `newInterval` to the result.
3. **Phase 3 (no overlap, after):** all remaining intervals start strictly after the (now finalized) `newInterval`'s end — add each directly to the result unchanged.

**Invariant:** Because `intervals` is sorted and non-overlapping, once an interval is found to *not* overlap with `newInterval` in phase 2 (i.e., its start exceeds `newInterval`'s current end), no subsequent interval in the sorted array can overlap with `newInterval` either (since all subsequent intervals have even larger start values) — so the transition from phase 2 to phase 3 is permanent and correct, and the merged `newInterval` can be safely finalized and added to the result at that point.

## 4. Approach 1 — Treat as General Merge Intervals (Add, Sort, Merge)

**Logic:** Simply add `newInterval` to the existing `intervals` array/list, then apply the general "Merge Intervals" (LC 56) algorithm from scratch: sort everything by start value, then do a single merging pass.

```java
public int[][] insert(int[][] intervals, int[] newInterval) {
    List<int[]> all = new ArrayList<>(Arrays.asList(intervals));
    all.add(newInterval);
    all.sort((a, b) -> Integer.compare(a[0], b[0]));

    List<int[]> result = new ArrayList<>();
    result.add(all.get(0));

    for (int i = 1; i < all.size(); i++) {
        int[] current = all.get(i);
        int[] last = result.get(result.size() - 1);
        if (current[0] <= last[1]) {
            last[1] = Math.max(last[1], current[1]);
        } else {
            result.add(current);
        }
    }

    return result.toArray(new int[result.size()][]);
}
```

- **Time:** O(n log n) — dominated by the sort step, even though the merging pass itself is O(n).
- **Space:** O(n) for the combined list and result.

**Why it is not optimal:** This approach is correct, but it completely ignores the problem's explicit guarantee that `intervals` is *already* sorted — re-sorting the entire combined list from scratch is unnecessary work. The problem's own structure (a sorted list plus one new element to insert) is specifically what enables an O(n) solution instead, achieved by exploiting the existing order via the three-phase scan.

## 5. Approach 2 — Optimized (Three-Phase Single Pass)

**Algorithm:**
1. Initialize an empty result list and an index `i = 0`.
2. **Phase 1:** while `i < intervals.length` and `intervals[i][1] < newInterval[0]`, add `intervals[i]` to the result and increment `i`.
3. **Phase 2:** while `i < intervals.length` and `intervals[i][0] <= newInterval[1]`, update `newInterval[0] = Math.min(newInterval[0], intervals[i][0])` and `newInterval[1] = Math.max(newInterval[1], intervals[i][1])`, then increment `i`. After this loop, add the fully-merged `newInterval` to the result.
4. **Phase 3:** while `i < intervals.length`, add `intervals[i]` to the result and increment `i`.
5. Return the result, converted to the required array format.

```java
public int[][] insert(int[][] intervals, int[] newInterval) {
    List<int[]> result = new ArrayList<>();
    int i = 0;
    int n = intervals.length;

    while (i < n && intervals[i][1] < newInterval[0]) {
        result.add(intervals[i]);
        i++;
    }

    while (i < n && intervals[i][0] <= newInterval[1]) {
        newInterval[0] = Math.min(newInterval[0], intervals[i][0]);
        newInterval[1] = Math.max(newInterval[1], intervals[i][1]);
        i++;
    }
    result.add(newInterval);

    while (i < n) {
        result.add(intervals[i]);
        i++;
    }

    return result.toArray(new int[result.size()][]);
}
```

- **Time:** O(n) — a single index `i` advances monotonically through `intervals` across all three phases combined, never resetting, so the total work is linear.
- **Space:** O(n) for the result list — this is unavoidable, since the output itself must hold up to `n+1` intervals; no additional auxiliary space (like a sort) is needed beyond this.

**Why this is optimal:** Every existing interval must be examined at least once to classify it into one of the three phases, so O(n) is a hard lower bound, and this approach achieves it directly by exploiting the given sortedness — a genuine asymptotic improvement over Approach 1's O(n log n), since no sorting is needed at all here. This is the specific lesson of the problem: recognizing that a "insert into an already-sorted structure" scenario should never require a full re-sort when a single linear pass, informed by the existing order, suffices.

## 6. Dry Run

Example: `intervals = [[1,2],[3,5],[6,7],[8,10],[12,16]]`, `newInterval = [4,8]`.
Chosen because it's the second canonical example, and it clearly exercises all three phases, including multiple intervals being absorbed during the merge phase.

`n=5`. Initial: `result=[]`, `i=0`, `newInterval=[4,8]`.

**Phase 1** (`intervals[i][1] < newInterval[0]=4`):

| i | intervals[i] | intervals[i][1] | < 4? | Action |
|---|--------------------|------------------------|-------|--------|
| 0 | [1,2] | 2 | Yes | add [1,2] to result; i=1 |
| 1 | [3,5] | 5 | No | stop phase 1 |

After phase 1: `result=[[1,2]]`, `i=1`.

**Phase 2** (`intervals[i][0] <= newInterval[1]=8`):

| i | intervals[i] | intervals[i][0] | <= 8? | newInterval before | newInterval after |
|---|--------------------|-------------------------|--------|--------------------------|-------------------------|
| 1 | [3,5] | 3 | Yes | [4,8] | min(4,3)=3, max(8,5)=8 → [3,8] |
| 2 | [6,7] | 6 | Yes | [3,8] | min(3,6)=3, max(8,7)=8 → [3,8] |
| 3 | [8,10] | 8 | Yes | [3,8] | min(3,8)=3, max(8,10)=10 → [3,10] |
| 4 | [12,16] | 12 | No | — | stop phase 2 |

After phase 2: `newInterval=[3,10]`, added to result → `result=[[1,2],[3,10]]`, `i=4`.

**Phase 3** (remaining):

| i | intervals[i] | Action |
|---|--------------------|--------|
| 4 | [12,16] | add [12,16] to result; i=5 |

Exit condition: `i=5=n`, phase 3 loop ends.

**Final answer:** `result=[[1,2],[3,10],[12,16]]`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Add, sort, merge (general LC 56 approach) | O(n log n) | O(n) | Correct but ignores the given sortedness, doing unnecessary sort work |
| 2. Three-phase single pass | O(n) | O(n) | Optimal; exploits existing sortedness for a genuine linear-time solution |

Output space (the result list/array) is required by both approaches.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Empty `intervals` array | Should simply return a single-element result containing just `newInterval` | Phase 1's loop condition `i < n` is immediately false (`n=0`), phase 2's loop condition is also immediately false, so `newInterval` is added directly to the (still-empty) result, and phase 3 also does nothing — correctly producing `[newInterval]` |
| `newInterval` doesn't overlap with any existing interval (falls entirely in a gap, or before/after everything) | Should simply be inserted at the correct sorted position without merging with anything | If `newInterval` fits in a gap, phase 1 correctly adds all intervals ending before it, phase 2's loop condition is immediately false (no overlap), so `newInterval` is added as-is, and phase 3 adds the rest — correctly inserting it without any merging |
| `newInterval` overlaps with every existing interval | Should merge everything into one single resulting interval | Phase 1 adds nothing (the very first interval already overlaps), phase 2's loop absorbs every single existing interval into `newInterval`'s expanding bounds, and phase 3 adds nothing (all intervals were consumed in phase 2) — correctly producing a single merged interval as the entire result |
| `newInterval` touches (but doesn't strictly overlap) an existing interval at exactly one point (e.g., `newInterval=[2,5]` and an existing `[5,7]`) | Per the problem's explicit "share at least one point" rule, this must still count as overlapping and be merged | Phase 2's condition `intervals[i][0] <= newInterval[1]` uses non-strict `<=`, so `5 <= 5` correctly triggers a merge rather than being treated as non-overlapping |
| `newInterval` is inserted at the very beginning (starts before all existing intervals) or very end (starts after all existing intervals) | Phase 1 or phase 3 respectively should end up empty, with the other two phases handling everything | If `newInterval` starts before everything, phase 1 immediately finds no intervals ending before it, so it goes straight to phase 2 (absorbing any overlaps) and then phase 3 (the rest); symmetric logic applies if it starts after everything, where phase 1 absorbs everything and phases 2/3 do nothing |
| Maximum constraint size (`n=10^4`) | Should still perform efficiently at the upper bound | The O(n) time complexity comfortably handles the maximum input size, since the single index `i` never revisits any interval |

## 9. Java Notes

- **Single monotonically-advancing index `i` shared across all three phases:** this is the key structural detail that guarantees O(n) time — `i` never resets or moves backward between phases, so the total number of iterations across all three `while` loops combined is bounded by `n`, not `3n` or worse.
- **Mutating `newInterval`'s array elements directly (`newInterval[0] = ...`, `newInterval[1] = ...`) during phase 2:** since `newInterval` is passed in as an `int[]` (a reference to an array), these mutations modify the actual array object; this is intentional and convenient here, allowing the "current working merged interval" to simply be `newInterval` itself, progressively expanded in place, without needing a separate variable.
- **`result.toArray(new int[result.size()][])`:** the idiomatic way to convert the `List<int[]>` result back into the required `int[][]` return type.
- **No overflow risk:** given `0 <= start_i, end_i <= 10^5`, all comparisons and `Math.min`/`Math.max` operations involve small, bounded values well within `int` range.

## 10. Common Mistakes

- **Using strict `<` instead of `<=` in the phase 2 overlap condition (`intervals[i][0] <= newInterval[1]`).** Since the problem explicitly states that intervals sharing even a single point count as overlapping, a strict comparison would incorrectly fail to merge touching intervals. Fix: always use `<=` for the overlap check, consistent with the problem's explicit "at least one point" overlap definition.
- **Forgetting to add the fully-merged `newInterval` to the result after phase 2 completes.** Since phase 2's loop only handles absorbing overlapping existing intervals into `newInterval`'s bounds, a separate explicit step is needed to actually add that finalized interval to the result before moving on to phase 3. Fix: always place `result.add(newInterval);` immediately after the phase 2 loop, before phase 3 begins.
- **Attempting to re-sort the combined array (Approach 1) without recognizing that the problem's given sortedness makes this unnecessary.** While correct, this misses the specific O(n) optimization opportunity that distinguishes this problem from the more general "Merge Intervals" (LC 56). Fix: recognize the explicit "already sorted" guarantee as the signal to use the three-phase linear scan instead of a full re-sort.
- **Getting the phase 1 termination condition backward**, e.g., checking `intervals[i][0] < newInterval[0]` (comparing starts) instead of `intervals[i][1] < newInterval[0]` (comparing the existing interval's end against the new interval's start). The correct condition for "this existing interval is entirely before and doesn't overlap `newInterval`" specifically compares the existing interval's *end* to `newInterval`'s *start*. Fix: carefully verify that phase 1's condition checks `intervals[i][1] < newInterval[0]`, not a comparison of start values.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Insert a new interval into an already-sorted, non-overlapping list, merging as needed → single linear pass in three phases: add intervals ending before the new one unchanged, merge all overlapping intervals into the new one, then add the remaining intervals unchanged."
- **90-second explanation:** "Since the existing intervals are already sorted by start value and guaranteed non-overlapping, I don't need to sort anything — I can process everything in a single left-to-right pass, split into three phases. First, I add every existing interval that ends before my new interval even starts, since those are completely unaffected. Second, for every existing interval whose start is at or before my new interval's current end, I know it overlaps or touches my new interval, so I expand my new interval's bounds to absorb it, taking the min of the starts and the max of the ends, and I continue doing this for every subsequent overlapping interval. Once I hit an interval that no longer overlaps, I know, because everything is sorted, that no later interval will overlap either, so I finalize my now fully-expanded new interval and add it to the result. Third, I add every remaining interval unchanged, since none of them can possibly overlap with my already-finalized new interval. Because a single index pointer advances through the original array exactly once across all three phases combined, this runs in O(n) time, which is a genuine improvement over just treating this as a from-scratch merge-and-sort, since that would ignore the sortedness the problem explicitly guarantees."
- **Related problems using this pattern:**
  - LeetCode 56 — Merge Intervals (the more general version of this problem, without the given sortedness/insertion structure)
  - LeetCode 228 — Summary Ranges (a related, simpler "group consecutive values" problem)
  - LeetCode 435 — Non-overlapping Intervals (a related greedy interval-scheduling problem)

## 12. Recall Questions

**Q:** Why does the fact that `intervals` is already sorted and non-overlapping allow this problem to be solved in O(n) time, unlike the more general "Merge Intervals" problem?
**A:** Because the existing order is already established, a single linear scan can correctly classify every interval relative to `newInterval` without needing to sort anything, whereas the general merge-intervals problem must first sort an arbitrarily-ordered input before any linear merging logic can apply.

**Q:** Why must the phase 2 overlap condition use `<=` rather than a strict `<` comparison?
**A:** The problem explicitly defines two intervals as overlapping if they share even a single point, so a strict inequality would incorrectly treat intervals that merely touch at one shared boundary value as non-overlapping, failing to merge them as required.

**Q:** Why is it guaranteed that once an interval is found not to overlap with the current expanding `newInterval` during phase 2, no later interval in the array could possibly overlap either?
**A:** Since `intervals` is sorted by start value in ascending order, every subsequent interval has a start value at least as large as the one that just failed to overlap, so if that interval's start already exceeded `newInterval`'s current end, every later interval's even-larger start would also exceed it.

**Q:** Why is phase 1's termination condition based on comparing an existing interval's *end* to `newInterval`'s *start*, rather than comparing the two intervals' start values directly?
**A:** An existing interval is only guaranteed to be completely unaffected by (i.e., entirely before, with no possibility of overlap with) `newInterval` if that existing interval's end falls strictly before `newInterval`'s start begins, since comparing only the start values wouldn't account for how far the existing interval actually extends.

**Q:** What specifically would go wrong if the step adding the fully-merged `newInterval` to the result were omitted after phase 2?
**A:** The final result would be missing the newly inserted (and possibly merged) interval entirely, since phase 2's loop only handles expanding `newInterval`'s bounds to absorb overlapping intervals — it never itself adds anything to the result list, so that omission must be handled by an explicit separate step immediately following phase 2.

## 13. Final Code

```java
public int[][] insert(int[][] intervals, int[] newInterval) {
    List<int[]> result = new ArrayList<>();
    int i = 0;
    int n = intervals.length;

    while (i < n && intervals[i][1] < newInterval[0]) {
        result.add(intervals[i]);
        i++;
    }

    while (i < n && intervals[i][0] <= newInterval[1]) {
        newInterval[0] = Math.min(newInterval[0], intervals[i][0]);
        newInterval[1] = Math.max(newInterval[1], intervals[i][1]);
        i++;
    }
    result.add(newInterval);

    while (i < n) {
        result.add(intervals[i]);
        i++;
    }

    return result.toArray(new int[result.size()][]);
}
```

## 14. Self-Test

You have a sorted, non-overlapping array of intervals and one new interval to insert. Insert it, merging with any overlapping (or touching) intervals as needed, and return the sorted, non-overlapping result. Think about why the given sortedness allows a single three-phase linear pass — before, merge, after — rather than needing to sort everything from scratch.
