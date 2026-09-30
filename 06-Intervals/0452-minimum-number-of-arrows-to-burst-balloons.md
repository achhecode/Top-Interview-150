---
problem: 452 - Minimum Number of Arrows to Burst Balloons
url: https://leetcode.com/problems/minimum-number-of-arrows-to-burst-balloons/
difficulty: Medium
section: Array / Greedy
patterns: [sort-by-end, greedy-interval-covering]
data_structures: [array]
time: O(n log n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An array `points`, where `points[i] = [xstart, xend]` represents a balloon's horizontal span.

**Required:** Return the minimum number of vertical arrows needed so that every balloon is burst by at least one arrow (an arrow at position `x` bursts any balloon where `xstart <= x <= xend`).

**Constraints that actually matter:**
- `1 <= points.length <= 10^5` — large enough that an O(n²) approach (checking every pair of balloons for overlap) risks up to `~10^10` operations, far too slow; O(n log n) is expected.
- `-2^31 <= xstart < xend <= 2^31 - 1` — the coordinate values span the full `int` range, so any arithmetic combining two coordinates (like averaging, or computing a difference) risks overflow; fortunately, the optimal algorithm only ever needs direct comparisons between coordinate values, never arithmetic combinations, sidestepping this risk entirely.
- **This is fundamentally an interval-covering problem in disguise:** finding the minimum number of "points" (arrow positions) that collectively intersect every given interval is a classic greedy problem, mathematically very closely related to (though framed differently from) general interval-merging or interval-scheduling problems.

## 2. Recognition Signals

- "Find the minimum number of points/arrows needed to hit/cover every interval" → the classic **greedy interval-point-covering** technique: sort intervals by their **end** value, then greedily place a point at the end of the first (in sorted order) not-yet-covered interval — this point is guaranteed to cover that interval and, for free, every other interval that overlaps it, before moving on to the next interval that isn't yet covered by any placed point.
- The key greedy insight: sorting by **end** value (not start) is essential — placing an arrow at the earliest possible ending balloon's end coordinate maximizes the chance that this same single arrow also happens to burst other balloons whose ranges extend into that same x-position, since any other still-uncovered balloon that overlaps with this one must have a start value at or before this end value (given the sorted-by-end order, its own end can only be equal or later).
- Whenever a problem provides overlapping ranges and asks for the fewest "hits"/"points"/"arrows" to intersect all of them — this greedy sort-by-end technique is a broadly reusable pattern, distinct from (though closely related to) sort-by-start techniques used in problems like Merge Intervals (LC 56).

**Pattern:** Greedy Interval-Point-Covering via Sort-by-End (sort balloons by their end coordinate, then greedily shoot an arrow at the end of the first uncovered balloon, which automatically also bursts every other overlapping balloon, before moving on to the next balloon whose start exceeds that arrow's position).

## 3. Core Idea

Sort the balloons by their `xend` value in ascending order. Process them in this order, maintaining the position of the most recently shot arrow (initially none). For the first balloon in sorted order, shoot an arrow at its `xend` — this is provably the best possible single choice, since placing the arrow any earlier would risk missing balloons that start later but still overlap this one, and placing it any later isn't beneficial and could miss balloons that end sooner. For each subsequent balloon (still in sorted-by-end order), check whether its `xstart` is at or before the current arrow's position — if so, this balloon is already burst by the existing arrow (no new arrow needed); if not, a new arrow is required, shot at this balloon's `xend` (again, the greedy-optimal choice for covering this and any subsequent overlapping balloons).

**Invariant:** After processing the first `k` balloons in sorted-by-end order, the number of arrows shot so far is the true minimum number needed to burst exactly those `k` balloons — because each new arrow is only introduced when strictly necessary (the current balloon's start exceeds every previously placed arrow's position, meaning no existing arrow could possibly reach it), and each new arrow is placed at the earliest possible position (`xend` of the triggering balloon) that maximizes its chance of also covering future balloons in the remaining sorted sequence.

## 4. Approach 1 — Brute Force (Try Every Possible Arrow Combination)

**Logic:** Conceptually, try increasingly larger numbers of arrows, and for each count, check every possible combination of arrow positions (drawn from the relevant candidate x-coordinates) to see if some combination bursts all balloons — this is exponential and impractical, so it's typically not even attempted directly; a more common "naive but correct" approach instead sorts by start and greedily tries to extend coverage, which can actually fail to be optimal if not done carefully, or a straightforward O(n²) pairwise-overlap-counting approach can be used as a baseline correct-but-slow method.

```java
// A straightforward (but slow) baseline: repeatedly find an unburst balloon,
// shoot an arrow at its end, and mark every balloon it also bursts.
public int findMinArrowShots(int[][] points) {
    int n = points.length;
    boolean[] burst = new boolean[n];
    int arrows = 0;

    for (int i = 0; i < n; i++) {
        if (burst[i]) {
            continue;
        }
        arrows++;
        int arrowPos = points[i][1];
        for (int j = 0; j < n; j++) {
            if (!burst[j] && points[j][0] <= arrowPos && arrowPos <= points[j][1]) {
                burst[j] = true;
            }
        }
    }

    return arrows;
}
```

- **Time:** O(n²) — for each balloon that triggers a new arrow, an O(n) scan checks every other balloon for whether this arrow also bursts it; in the worst case, this happens up to `n` times.
- **Space:** O(n) for the `burst` tracking array.

**Why it is not optimal:** With `n` up to `10^5`, O(n²) risks up to `~10^10` operations, far too slow. It's also not using the sorted order to make the "which balloon to process next" decision efficient — it relies on an unstructured scan to find the next unburst balloon and to check coverage, whereas the sorted approach makes both of these决定 essentially free.

## 5. Approach 2 — Optimized (Sort by End, Single Greedy Pass)

**Algorithm:**
1. Sort `points` by `xend` (ascending).
2. Initialize `arrows = 1` and `currentArrowEnd = points[0][1]` (shoot the first arrow at the end of the first balloon in sorted order).
3. For each subsequent balloon `points[i]` (from index 1 onward, in sorted order):
   - If `points[i][0] > currentArrowEnd`, this balloon isn't covered by the current arrow: increment `arrows`, and update `currentArrowEnd = points[i][1]` (shoot a new arrow at this balloon's end).
   - Otherwise, this balloon is already covered by the current arrow — do nothing (no new arrow needed).
4. Return `arrows`.

```java
public int findMinArrowShots(int[][] points) {
    if (points.length == 0) {
        return 0;
    }

    Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]));

    int arrows = 1;
    int currentArrowEnd = points[0][1];

    for (int i = 1; i < points.length; i++) {
        if (points[i][0] > currentArrowEnd) {
            arrows++;
            currentArrowEnd = points[i][1];
        }
    }

    return arrows;
}
```

- **Time:** O(n log n) — dominated by the sort; the subsequent single linear pass is O(n).
- **Space:** O(1) auxiliary beyond the sort's own internal space requirements — just a couple of tracking variables.

**Why this is optimal:** Every balloon must be examined at least once to determine whether it needs a new arrow, and general comparison-based sorting has an O(n log n) lower bound, so O(n log n) overall is essentially optimal for this approach. This is a decisive improvement over the O(n²) baseline, and the greedy choice (sort by end, always place each new arrow at the triggering balloon's own end) is provably optimal — no other strategy can use fewer arrows, since each arrow is placed as "far left as still valid" for covering the current balloon, maximizing its potential to also cover subsequent balloons in the sorted sequence.

## 6. Dry Run

Example: `points = [[10,16],[2,8],[1,6],[7,12]]`.
Chosen because it's the canonical example, and after sorting by end value, it clearly shows the greedy process correctly identifying that only 2 arrows are needed.

**After sorting by `xend` (ascending):** `[[1,6],[2,8],[7,12],[10,16]]`.

Initial: `arrows = 1`, `currentArrowEnd = 6` (from `[1,6]`, the first sorted balloon).

| i | points[i] | points[i][0] | > currentArrowEnd? | Action | arrows after | currentArrowEnd after |
|---|-----------------|---------------------|---------------------------|--------|-------------------|------------------------------|
| 1 | [2,8] | 2 | No (2>6 false) | already covered | 1 | 6 |
| 2 | [7,12] | 7 | Yes (7>6) | new arrow; currentArrowEnd=12 | 2 | 12 |
| 3 | [10,16] | 10 | No (10>12 false) | already covered | 2 | 12 |

Exit condition: all balloons processed.

**Final answer:** `arrows = 2`, matching the expected output exactly — one arrow at x=6 (covering `[1,6]` and `[2,8]`), and one arrow at x=12 (covering `[7,12]` and `[10,16]`), consistent with the problem's own explanation (which used slightly different but equally valid arrow positions, x=6 and x=11, both within the same respective overlapping ranges).

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Baseline pairwise-marking | O(n²) | O(n) | Correct but far too slow for n up to 10^5 |
| 2. Sort by end, single greedy pass | O(n log n) | O(1) (excl. sort) | Optimal; provably correct greedy strategy |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single balloon (`points.length == 1`) | Trivially needs exactly 1 arrow | `arrows` initializes to 1 directly from the single balloon's end, and the loop (starting at `i=1`) never executes, correctly returning 1 |
| No balloons overlap at all (example 2: `[[1,2],[3,4],[5,6],[7,8]]`) | Should require one arrow per balloon | After sorting (already sorted here), every subsequent balloon's start exceeds the current arrow's end (since there's no overlap anywhere), triggering a new arrow every single time, correctly totaling 4 |
| Balloons that only touch at a single point (example 3: `[[1,2],[2,3],[3,4],[4,5]]`) | Touching balloons should still be counted as burstable by the same arrow, since an arrow at the shared boundary point satisfies `xstart <= x <= xend` for both | The condition `points[i][0] > currentArrowEnd` uses strict `>`, so a touching balloon (`points[i][0] == currentArrowEnd`) correctly does *not* trigger a new arrow, since `2 > 2` is false — this correctly merges touching balloons under the same arrow, matching the example's expected output of 2 |
| All balloons overlap into a single burstable group | Should require exactly 1 arrow | Every balloon's start remains within the current arrow's end throughout the entire sorted sequence, so `arrows` never increments beyond its initial value of 1 |
| Balloons with the full extreme range of coordinate values (`xstart` near `-2^31`, `xend` near `2^31-1`) | Must avoid any arithmetic that could overflow given these extreme values | The algorithm only ever performs direct comparisons (`>`, and the sort's internal comparisons), never any addition, subtraction, or averaging of coordinate values, so there is no overflow risk regardless of how extreme the values are |
| Balloons given in a completely arbitrary (unsorted) order in the input, as is generally assumed since no sortedness is guaranteed by the constraints | Must correctly sort first to enable the greedy single-pass logic | The explicit `Arrays.sort(points, ...)` call at the very start ensures the subsequent greedy pass always operates on a properly end-sorted sequence, regardless of the original input order |

## 9. Java Notes

- **`Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]))`:** sorts the 2D `int[][]` array using a custom comparator ordering by each balloon's end coordinate (`a[1]`); necessary since `int[]` arrays have no natural ordering `Arrays.sort` could use without an explicit comparator.
- **No overflow risk:** as discussed, the algorithm exclusively uses direct comparisons between coordinate values, never arithmetic combinations of them (like summing or averaging two coordinates), so the full `int` range of the constraints poses no special risk here — this is worth explicitly noting as a point of care given how extreme the allowed coordinate values are.
- **Sorting by end (`a[1]`), not start (`a[0]`):** this is the crucial, easy-to-get-backward detail — sorting by start value instead would not produce the same greedy correctness guarantee, since it wouldn't ensure that the earliest-possible "cutoff" point is chosen first.
- **`Integer.compare(a[1], b[1])` vs. a manual subtraction-based comparator (`a[1] - b[1]`):** using `Integer.compare` avoids a subtle overflow risk that a naive subtraction-based comparator would introduce (since `a[1] - b[1]` could itself overflow given the extreme allowed coordinate range), making it the safer, more idiomatic choice here.

## 10. Common Mistakes

- **Sorting by start value (`xstart`) instead of end value (`xend`).** This breaks the greedy algorithm's correctness guarantee — sorting by start doesn't ensure the earliest possible "cutoff" arrow position is chosen first, potentially leading to a suboptimal (too high) arrow count. Fix: always sort by the `xend` (second) coordinate.
- **Using a strict `>=` instead of strict `>` (or vice versa) in the "does this balloon need a new arrow" check, getting the touching-balloon boundary case backward.** Since balloons sharing exactly one boundary point should still be counted as coverable by the same arrow, the condition for "needs a new arrow" must be a *strict* `>` (start strictly exceeds the current arrow's position) — using `>=` here would incorrectly force a new arrow even when the balloons merely touch. Fix: use strict `>` for triggering a new arrow, consistent with the problem's "at least one point" coverage rule.
- **Using a naive subtraction-based comparator (`(a, b) -> a[1] - b[1]`) instead of `Integer.compare`.** Given the extreme allowed coordinate range (`-2^31` to `2^31-1`), a direct subtraction between two such values can itself overflow, producing an incorrect sort order in rare edge cases. Fix: always use `Integer.compare(a[1], b[1])` (or an equivalent overflow-safe comparison) rather than raw subtraction, especially when the value range spans close to the full `int` range.
- **Forgetting to handle the empty `points` array case, if it were possible** (though the given constraints guarantee `points.length >= 1`, so this is more of a general defensive-programming habit) — attempting to access `points[0][1]` on an empty array would throw an exception. Fix: while not strictly required by this problem's constraints, it's good practice to guard against an empty input array before accessing its first element, as shown in the provided solution.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Minimum number of points to intersect every interval → sort intervals by their end value, then greedily place a new point only when the current interval's start exceeds the most recently placed point's position."
- **90-second explanation:** "I sort the balloons by their end coordinate, since this ensures that whenever I need to shoot an arrow, placing it at the current balloon's end position is the greedy-optimal choice — it's the earliest position that still guarantees this balloon is burst, maximizing the chance that this same arrow also happens to burst other balloons whose ranges extend into that same point. I process the balloons in this sorted order, keeping track of my most recently shot arrow's position. For each balloon, if its start is already at or before that position, it's already burst by the existing arrow, so I do nothing. If its start comes after that position, no existing arrow can reach it, so I shoot a new arrow at this balloon's end and update my tracked position. Since sorting by end guarantees I'm always making the most conservative, coverage-maximizing choice for each new arrow, this greedy approach is provably optimal, not just a heuristic. It runs in O(n log n) time, dominated by the initial sort, and only needs a couple of tracking variables beyond that, so O(1) additional space."
- **Related problems using this pattern:**
  - LeetCode 56 — Merge Intervals (a related but distinct problem, sorted by start instead, for a different objective)
  - LeetCode 435 — Non-overlapping Intervals (a closely related greedy problem, often solved with the same sort-by-end technique)
  - LeetCode 57 — Insert Interval (related interval manipulation, though with a different given structure)

## 12. Recall Questions

**Q:** Why is sorting by each balloon's end coordinate specifically essential for this greedy algorithm's correctness, rather than sorting by the start coordinate?
**A:** Sorting by end guarantees that whenever a new arrow is needed, placing it at the current balloon's end position is the earliest valid choice that still bursts that balloon, which maximizes the opportunity for that same arrow to also burst other, later balloons in the sorted sequence that might overlap with it — sorting by start doesn't provide this same guarantee.

**Q:** Why does the condition for needing a new arrow use a strict `>` comparison rather than `>=`?
**A:** The problem defines balloons as burstable by an arrow that touches even a single shared boundary point, so two balloons that merely touch at one coordinate should still be covered by the same arrow, which is exactly what a strict `>` comparison (rather than `>=`) correctly allows for.

**Q:** Why is `Integer.compare(a[1], b[1])` preferred over a subtraction-based comparator like `a[1] - b[1]` for sorting these specific balloons?
**A:** Given that balloon coordinates can span the full extreme range of the 32-bit integer type, directly subtracting two such values in a comparator risks integer overflow, which could produce an incorrect sort order, whereas `Integer.compare` avoids this risk entirely by not relying on a potentially overflowing subtraction.

**Q:** Why is this greedy strategy provably optimal, rather than just a reasonable-sounding heuristic?
**A:** Every time a new arrow is introduced, it's only because no previously placed arrow could possibly reach the current balloon (given the sorted-by-end processing order), and placing that new arrow at the current balloon's own end value is the furthest-right position that still guarantees covering it while maximizing potential future coverage, so no alternative arrow placement strategy could ever achieve full coverage using fewer arrows.

**Q:** Why doesn't this algorithm ever risk integer overflow, despite operating on coordinate values that span the full extreme range of the 32-bit integer type?
**A:** The algorithm only ever performs direct comparisons between coordinate values (checking whether one value is greater than another) — it never adds, subtracts, or otherwise combines two coordinate values arithmetically, so the specific magnitude or extremity of the values themselves poses no risk to the correctness of any operation performed.

## 13. Final Code

```java
public int findMinArrowShots(int[][] points) {
    if (points.length == 0) {
        return 0;
    }

    Arrays.sort(points, (a, b) -> Integer.compare(a[1], b[1]));

    int arrows = 1;
    int currentArrowEnd = points[0][1];

    for (int i = 1; i < points.length; i++) {
        if (points[i][0] > currentArrowEnd) {
            arrows++;
            currentArrowEnd = points[i][1];
        }
    }

    return arrows;
}
```

## 14. Self-Test

You have an array `points` where each `[xstart, xend]` represents a balloon's horizontal span. Return the minimum number of vertical arrows needed to burst every balloon. Think about why sorting by each balloon's end coordinate, then greedily placing a new arrow only when strictly necessary, guarantees the fewest possible arrows.
