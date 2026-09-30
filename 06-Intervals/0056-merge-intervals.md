---
problem: 56 - Merge Intervals
url: https://leetcode.com/problems/merge-intervals/
difficulty: Medium
section: Array / Sorting
patterns: [sort-then-sweep, interval-merging]
data_structures: [array, list]
time: O(n log n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An array of intervals, each `[start, end]`.

**Required:** Merge all overlapping intervals, returning a new array of non-overlapping intervals that together cover exactly the same ranges as the original input.

**Constraints that actually matter:**
- `1 <= intervals.length <= 10^4` — large enough that an O(n²) pairwise-merge approach (repeatedly scanning for and merging overlapping pairs until none remain) risks up to `~10^8` operations in the worst case, likely too slow; an O(n log n) approach is expected.
- **Intervals are not guaranteed to be sorted or given in any particular order** — this is the central complexity: without sorting first, determining which intervals overlap would require comparing every pair, since two intervals that overlap could be positioned anywhere relative to each other in the input array.
- **Touching intervals count as overlapping** — explicitly, `[1,4]` and `[4,5]` must merge into `[1,5]`, since they share the boundary point 4 (example 2). This means the overlap-check condition must use a non-strict comparison (`<=`), not a strict one (`<`), when comparing a new interval's start against the current merged interval's end.

## 2. Recognition Signals

- "Merge all overlapping intervals" → the classic **sort-then-sweep** technique: sort intervals by their start value, then do a single linear pass, merging each interval into the current "in-progress" merged interval if it overlaps, or starting a new merged interval if it doesn't.
- Sorting by start value is what transforms an otherwise all-pairs comparison problem into one solvable with a single linear scan — once sorted, any interval that could possibly overlap with the current merged range must appear immediately next in the sorted order (an overlap can never "skip over" an interval in sorted-by-start order without that skipped interval also being part of the same overlap chain), so only adjacent-in-sorted-order comparisons are ever needed.
- The specific detail that **touching intervals (sharing exactly one boundary point) also count as overlapping** is a signal to double check the comparison operator used when merging — this is a common subtle detail that's easy to get backward (using strict `<` instead of `<=`) if not read carefully.

**Pattern:** Sort-Then-Sweep for Interval Merging (sort intervals by start value, then make a single linear pass, extending the current merged interval's end whenever the next interval overlaps or touches it, and starting a new merged interval otherwise).

## 3. Core Idea

Sort all intervals by their start value. Once sorted, walk through them left to right, maintaining a "current merged interval" that starts as the first interval. For each subsequent interval, check whether its start is less than or equal to the current merged interval's end — if so, they overlap (or touch), so extend the current merged interval's end to be the maximum of the two ends (the new interval might extend further, or might be entirely contained within the current merged range — either way, taking the max end is correct). If the next interval's start is strictly greater than the current merged interval's end, there's a genuine gap: finalize the current merged interval (add it to the result), and start a new "current merged interval" from this next interval.

**Invariant:** After processing the first `k` sorted intervals, the result list (plus whatever "current merged interval" is still in progress) correctly and completely represents the merged form of those `k` intervals — because sorting by start guarantees that no later interval (further right in the sorted order) could ever overlap with any *already-finalized* merged interval without also overlapping with the current in-progress one first, so finalizing a merged interval once its extension stops is always safe and permanent.

## 4. Approach 1 — Brute Force (Repeated Pairwise Merging)

**Logic:** Repeatedly scan through the list of intervals, looking for any pair that overlaps; whenever such a pair is found, merge them into one interval and restart the scan, continuing until no more pairs overlap.

```java
public int[][] merge(int[][] intervals) {
    List<int[]> result = new ArrayList<>(Arrays.asList(intervals));
    boolean merged = true;

    while (merged) {
        merged = false;
        outer:
        for (int i = 0; i < result.size(); i++) {
            for (int j = i + 1; j < result.size(); j++) {
                int[] a = result.get(i);
                int[] b = result.get(j);
                if (a[0] <= b[1] && b[0] <= a[1]) {
                    int[] mergedInterval = {Math.min(a[0], b[0]), Math.max(a[1], b[1])};
                    result.remove(j);
                    result.remove(i);
                    result.add(mergedInterval);
                    merged = true;
                    break outer;
                }
            }
        }
    }

    return result.toArray(new int[result.size()][]);
}
```

- **Time:** O(n³) in the worst case — the outer `while` loop can run up to O(n) times (each merge reduces the count by one), and each iteration does an O(n²) all-pairs scan to find a mergeable pair.
- **Space:** O(n) for the working list of intervals.

**Why it is not optimal:** With `n` up to `10^4`, O(n³) is far too slow (potentially `~10^12` operations in the worst case). It also completely ignores the fact that sorting first would make the overlap-detection process vastly simpler and more efficient, turning an all-pairs search into a single linear scan.

## 5. Approach 2 — Optimized (Sort by Start, Then Single-Pass Sweep)

**Algorithm:**
1. Sort `intervals` by each interval's start value (ascending).
2. Initialize a result list, and add the first sorted interval to it as the initial "current merged interval."
3. For each subsequent interval `[start, end]` in sorted order:
   - Let `last` be the most recently added interval in the result list.
   - If `start <= last[1]` (the new interval overlaps or touches the current merged interval), update `last[1] = Math.max(last[1], end)` (extend the merged interval's end as needed).
   - Otherwise, the new interval starts a genuinely new, non-overlapping range: add `[start, end]` to the result list as a new entry.
4. Return the result list, converted to the required array format.

```java
public int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

    List<int[]> result = new ArrayList<>();
    result.add(intervals[0]);

    for (int i = 1; i < intervals.length; i++) {
        int[] current = intervals[i];
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

- **Time:** O(n log n) — dominated by the sort; the subsequent single linear pass through the sorted intervals is O(n).
- **Space:** O(n) for the result list (and O(log n) to O(n) auxiliary space for the sort itself, depending on the sorting algorithm's implementation details) — this is comparable to or better than Approach 1's space usage, with a dramatically better time complexity.

**Why this is optimal:** Every interval must be examined at least once to determine whether it merges into the current range or starts a new one, and comparison-based sorting has a well-known O(n log n) lower bound for general inputs, so O(n log n) overall is essentially optimal for this problem (a fundamentally different, non-comparison-based approach, like bucket sort exploiting the bounded `0 <= start_i <= end_i <= 10^4` range, could theoretically achieve O(n) in this specific problem's constraints, but the standard, broadly-applicable, and expected solution is the O(n log n) sort-then-sweep). This is a massive practical improvement over Approach 1's O(n³) brute force.

## 6. Dry Run

Example: `intervals = [[1,3],[2,6],[8,10],[15,18]]`.
Chosen because it's the canonical example, already conveniently close to sorted order, clearly showing one successful merge and two intervals that remain separate.

**After sorting by start (already sorted in this example):** `[[1,3],[2,6],[8,10],[15,18]]`.

Initial: `result = [[1,3]]`.

| i | current | last (result's last entry) | current[0] <= last[1]? | Action | result after |
|---|---------|-----------------------------------|------------------------------|--------|-------------------|
| 1 | [2,6] | [1,3] | 2<=3, Yes | last[1]=max(3,6)=6 → last becomes [1,6] | [[1,6]] |
| 2 | [8,10] | [1,6] | 8<=6, No | add [8,10] as new entry | [[1,6],[8,10]] |
| 3 | [15,18] | [8,10] | 15<=10, No | add [15,18] as new entry | [[1,6],[8,10],[15,18]] |

Exit condition: all intervals processed.

**Final answer:** `[[1,6],[8,10],[15,18]]`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Repeated pairwise merging | O(n³) worst case | O(n) | Correct but far too slow for n up to 10^4 |
| 2. Sort by start, single-pass sweep | O(n log n) | O(n) | Optimal (for a comparison-sort-based approach); dominated by the sort |

Output space (the result list/array) is required by both approaches.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single interval in the input | Should be returned unchanged, as a one-element result | With only one interval, the `for` loop (starting at `i=1`) never executes, so `result` remains just `[intervals[0]]`, correctly returned unchanged |
| Touching intervals (e.g. `[1,4]` and `[4,5]`, example 2) | Must be merged, not left separate, since they share exactly the boundary value 4 | The non-strict comparison `current[0] <= last[1]` (using `<=`, not `<`) correctly treats `4 <= 4` as an overlap, triggering a merge into `[1,5]` |
| Intervals given out of sorted order, where a later interval in the original array actually starts earlier (e.g. example 3: `[[4,7],[1,4]]`) | Must correctly sort first to detect the overlap, since simply scanning the original unsorted order wouldn't reveal that these two intervals overlap in a straightforward single pass | The initial `Arrays.sort` call reorders the intervals to `[[1,4],[4,7]]` before the merging logic runs, correctly setting up the single-pass sweep to detect the overlap (`4 <= 4`) and merge them into `[1,7]` |
| An interval completely contained within another (e.g. `[1,10]` followed by `[2,5]` after sorting by start) | The contained interval's end is smaller than the current merged interval's end, and must not incorrectly shrink the merged range | The `Math.max(last[1], current[1])` update correctly keeps the larger existing end value when the new interval's end is smaller, ensuring the merged range never incorrectly shrinks |
| No overlapping intervals at all | Should return the intervals unchanged (aside from being sorted by start) | Every comparison `current[0] <= last[1]` evaluates to false throughout the scan, so every interval is added as its own separate entry in the result, correctly producing a sorted, unmerged list |
| All intervals overlapping into one single merged range | Should correctly collapse everything into exactly one output interval | Every comparison succeeds in extending the single "current merged interval," so the result list never grows beyond its initial single entry, correctly producing one final merged interval covering the full combined range |

## 9. Java Notes

- **`Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]))`:** sorts the 2D `int[][]` array using a custom comparator that orders by each interval's first element (the start value); this is necessary because `int[]` arrays don't have a natural ordering that `Arrays.sort` could use without an explicit comparator.
- **Mutating `last[1]` directly (`last[1] = Math.max(last[1], current[1])`):** since `last` is a reference to the actual `int[]` array object stored in the `result` list (not a copy), updating `last[1]` directly modifies that same array in place within the list — this is a convenient, efficient way to "extend" the most recently added interval without needing to remove and re-add a new array object.
- **`result.toArray(new int[result.size()][])`:** the idiomatic way to convert a `List<int[]>` back into the required `int[][]` return type, with the array size hint ensuring the correct target array size is allocated.
- **No overflow risk:** given `0 <= start_i <= end_i <= 10^4`, all comparisons and the `Math.max` operation involve small, bounded values well within `int` range.

## 10. Common Mistakes

- **Forgetting to sort the intervals first, or assuming the input is already sorted.** Without sorting, the single-pass merging logic would fail to detect overlaps between intervals that aren't adjacent in the original (unsorted) array order, as directly demonstrated by example 3 in the problem statement. Fix: always explicitly sort by start value as the very first step.
- **Using a strict `<` comparison instead of `<=` when checking for overlap.** This would incorrectly treat touching intervals (like `[1,4]` and `[4,5]`) as non-overlapping, failing to merge them when the problem explicitly requires it. Fix: always use `<=` (non-strict) when comparing the new interval's start against the current merged interval's end.
- **Forgetting to take the maximum when extending the merged interval's end, and instead just overwriting it with the new interval's end unconditionally.** This would incorrectly shrink the merged range whenever a contained (shorter) interval is encountered after a longer one. Fix: always use `Math.max(last[1], current[1])` when extending, never a direct unconditional overwrite.
- **Mutating the input array's intervals directly without realizing the consequences, or conversely, creating unnecessary new array objects for every single interval even when just extending an existing merged range.** The optimized solution deliberately reuses and mutates the `last` array reference in place for efficiency when extending; creating a brand new array object on every extension instead would work correctly but adds unnecessary allocation overhead. Fix: understand that mutating `last[1]` in place is intentional and safe here, since `last` is the same object already stored in the result list.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Merge all overlapping intervals → sort by start value, then a single pass: extend the current merged interval's end (using max) whenever the next interval's start is at or before that end, otherwise start a new merged interval."
- **90-second explanation:** "Since the intervals could be given in any order, I first sort them by their start value — this is the key step that turns an otherwise all-pairs overlap-checking problem into something solvable with a single linear scan, because once sorted, any interval that could overlap with my current merged range has to appear right next in that sorted order. I then walk through the sorted intervals once, keeping track of the most recently finalized merged interval. For each new interval, if its start is less than or equal to that merged interval's current end — including exactly equal, since touching intervals count as overlapping — I extend the merged interval's end to be the larger of the two ends, since the new interval might stick out further or might be entirely contained within the existing range. If instead there's a genuine gap, I finalize the current merged interval and start a brand new one from this interval. This runs in O(n log n) time, dominated by the initial sort, which is essentially optimal for this problem given the general comparison-based sorting lower bound."
- **Related problems using this pattern:**
  - LeetCode 57 — Insert Interval (a related variant, inserting one new interval into an already-sorted, non-overlapping list)
  - LeetCode 228 — Summary Ranges (a simpler, related "group consecutive values" problem)
  - LeetCode 435 — Non-overlapping Intervals (a related greedy-interval-scheduling problem)

## 12. Recall Questions

**Q:** Why is sorting the intervals by their start value the essential first step that makes a single-pass merging approach possible?
**A:** Once sorted, any interval that could possibly overlap with the current merged range must appear immediately next in that sorted order — an overlap can never "skip over" an interval without that skipped interval also being involved in the same overlap chain — so only comparisons between adjacent, already-sorted intervals are ever needed, rather than checking every possible pair.

**Q:** Why must the overlap check use a non-strict `<=` comparison rather than a strict `<`?
**A:** The problem explicitly defines intervals that merely touch at a single shared boundary point, like `[1,4]` and `[4,5]`, as overlapping and requiring a merge, so a strict less-than comparison would incorrectly fail to detect and merge this exact boundary-touching case.

**Q:** Why must the merged interval's end be updated using `Math.max` rather than simply being overwritten with the new interval's end value?
**A:** A later interval in sorted-by-start order could still have an end value smaller than the current merged interval's end (meaning it's entirely contained within the existing range), and unconditionally overwriting the end would incorrectly shrink the merged interval in that scenario.

**Q:** Why is it safe to permanently finalize a merged interval and move on, once a subsequent interval is found that doesn't overlap with it?
**A:** Because the intervals are processed in sorted-by-start order, any interval encountered later in the scan has a start value at least as large as the current one, so once a gap is found, no future interval could possibly reach back and overlap with the already-finalized merged range, since that would require an earlier start than the interval that just failed to overlap.

**Q:** Why is the overall time complexity of this approach O(n log n) rather than just O(n)?
**A:** The single-pass merging scan itself is O(n), but it depends on the intervals first being sorted by their start values, and general comparison-based sorting has a well-known O(n log n) time complexity, which becomes the dominant cost over the linear scan that follows it.

## 13. Final Code

```java
public int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));

    List<int[]> result = new ArrayList<>();
    result.add(intervals[0]);

    for (int i = 1; i < intervals.length; i++) {
        int[] current = intervals[i];
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

## 14. Self-Test

You have an array of intervals `[start, end]`. Merge all overlapping (including touching) intervals, and return the resulting non-overlapping intervals. Think about why sorting by start value first is essential, and why extending a merged interval's end always needs to take the maximum rather than simply overwriting it.
