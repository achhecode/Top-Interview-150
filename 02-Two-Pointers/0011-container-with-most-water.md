---
problem: 11 - Container With Most Water
url: https://leetcode.com/problems/container-with-most-water/
difficulty: Medium
section: Array / Two Pointers
patterns: [two-pointers, greedy-elimination]
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

**Given:** An integer array `height` of length `n`, where the `i`th vertical line spans from `(i, 0)` to `(i, height[i])`.

**Required:** Choose two lines that, together with the x-axis, form a container holding the maximum possible amount of water. Return that maximum area.

**Constraints that actually matter:**
- `2 <= n <= 10^5` — large enough that an O(n²) brute force (checking every pair) risks up to `~5 * 10^9` operations in the worst case, far too slow; O(n) is expected.
- `0 <= height[i] <= 10^4` — heights can be 0, meaning a line can contribute no height at all to a container (effectively unable to hold any water on its own side).
- **The container's area is determined by the shorter of the two chosen lines** (`width × min(height[left], height[right])`), since water can't rise above the shorter wall without spilling out — this is the same core geometric principle seen in "Trapping Rain Water" (LC 42), though applied differently here (only two lines matter total, not accumulated across every position).
- **You may not slant the container** — the width between the two chosen lines is simply the difference in their indices, and the height is strictly the minimum of the two chosen lines' heights; there's no partial credit for a taller line beyond what the shorter one allows.

## 2. Recognition Signals

- "Choose two lines to maximize the area of a container, height determined by the shorter one" → the classic **two-pointer greedy elimination** technique: start with the widest possible container (both ends of the array) and greedily narrow inward, always discarding the pointer that can't possibly lead to a better result.
- The key greedy insight: at any given pair of pointers, the *shorter* line is the "bottleneck" limiting the current area — moving the pointer at the *taller* line inward could only keep the width the same or shrink it while the height is still capped by the same (now-still-present) shorter line, so it can never improve the area; moving the pointer at the *shorter* line inward is the only move that has any chance of finding a taller line that increases the height, potentially compensating for the reduced width.
- Recognizing that width can only ever decrease as the two pointers converge, so the *only* way a candidate found later in the pointer-convergence process could beat an earlier one is if its height increases enough to compensate for the necessarily smaller width — this is the mathematical justification for why discarding the shorter side's pointer at each step is always safe.

**Pattern:** Two Pointers, Greedy Elimination (start at both extremes for maximum width, and at each step, discard whichever pointer points to the shorter line, since it can never be part of a better solution than what's already been considered).

## 3. Core Idea

Start with two pointers, `left` at index 0 and `right` at index `n-1` — this gives the maximum possible width. Compute the current container's area as `(right - left) * min(height[left], height[right])`, tracking the best area seen so far. Then, move whichever pointer points to the **shorter** of the two lines inward by one step. This is the crucial greedy insight: since the width can only shrink as the pointers converge, any future candidate can only possibly beat the current one if its height increases enough to compensate — and since the current area is bounded by the *shorter* line, keeping that same shorter line in place (by instead moving the taller line's pointer) could never produce a taller bottleneck than what's already fixed by the shorter side; it would just needlessly shrink the width without any chance of raising the height ceiling. So moving the shorter side's pointer is the only choice that has any potential to find a taller line and thus a possibly larger area.

**Invariant:** At every step, every pair of lines *outside* the current `[left, right]` window that includes the pointer just discarded has already been correctly ruled out as unable to produce a better area than what's already been recorded — specifically, any pair involving the just-discarded shorter line and *any* other line still within the remaining window is bounded by that discarded line's height, which is no larger than the height already accounted for in the current best-so-far comparison at a width that was at least as large.

## 4. Approach 1 — Brute Force

**Logic:** Try every pair of lines `(i, j)` with `i < j`, computing the area for each and tracking the maximum.

```java
public int maxArea(int[] height) {
    int n = height.length;
    int maxArea = 0;

    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            int width = j - i;
            int currentHeight = Math.min(height[i], height[j]);
            maxArea = Math.max(maxArea, width * currentHeight);
        }
    }

    return maxArea;
}
```

- **Time:** O(n²) — nested loop checking all pairs.
- **Space:** O(1) auxiliary — just a running maximum.

**Why it is not optimal:** With `n` up to `10^5`, O(n²) risks up to `~5 * 10^9` operations in the worst case, far too slow. It doesn't exploit the geometric insight that a pair involving a shorter line can never beat a pair with the same or greater width and a taller bottleneck line, so many redundant comparisons are performed.

## 5. Approach 2 — Optimized (Two Pointers, Greedy Elimination)

**Algorithm:**
1. Initialize `left = 0`, `right = n - 1`, `maxArea = 0`.
2. While `left < right`:
   - Compute `width = right - left` and `currentHeight = Math.min(height[left], height[right])`.
   - Update `maxArea = Math.max(maxArea, width * currentHeight)`.
   - If `height[left] < height[right]`, increment `left` (the left line is the shorter/bottleneck one; it can never help again at this or any smaller width, so discard it).
   - Otherwise (right is shorter, or they're equal), decrement `right`.
3. Return `maxArea`.

```java
public int maxArea(int[] height) {
    int left = 0;
    int right = height.length - 1;
    int maxArea = 0;

    while (left < right) {
        int width = right - left;
        int currentHeight = Math.min(height[left], height[right]);
        maxArea = Math.max(maxArea, width * currentHeight);

        if (height[left] < height[right]) {
            left++;
        } else {
            right--;
        }
    }

    return maxArea;
}
```

- **Time:** O(n) — `left` and `right` together traverse the array at most once each, converging toward each other; total pointer movements across the entire run are bounded by `n`.
- **Space:** O(1) auxiliary — just the two pointers and a running maximum.

**Why this is optimal:** Every line potentially needs to be examined at least once to determine whether it could be part of the optimal pair, so O(n) is a natural lower bound, and this approach achieves it with a single converging pass. Space is O(1) since the greedy elimination logic relies only on the two current pointer positions and their heights — no need to store or revisit any other pairs once a pointer's line has been ruled out as a bottleneck. This is a genuine asymptotic improvement over the O(n²) brute force, backed by a provable greedy-elimination argument (discussed in the Core Idea) rather than being a mere heuristic.

## 6. Dry Run

Example: `height = [1,8,6,2,5,4,8,3,7]`.
Chosen because it's the canonical example, and it clearly shows the greedy elimination process working through several steps before converging on the optimal area.

`n = 9`. Initial: `left = 0`, `right = 8`, `maxArea = 0`.

| Step | left | right | height[left] | height[right] | width | currentHeight | area | maxArea after | Action |
|------|------|-------|-------------------|--------------------|-------|--------------------|------|--------------------|--------|
| 1 | 0 | 8 | 1 | 7 | 8 | 1 | 8 | 8 | height[left]<height[right] → left++ |
| 2 | 1 | 8 | 8 | 7 | 7 | 7 | 49 | 49 | height[left]>=height[right] → right-- |
| 3 | 1 | 7 | 8 | 3 | 6 | 3 | 18 | 49 | right-- |
| 4 | 1 | 6 | 8 | 8 | 5 | 8 | 40 | 49 | height[left]>=height[right] (equal) → right-- |
| 5 | 1 | 5 | 8 | 4 | 4 | 4 | 16 | 49 | right-- |
| 6 | 1 | 4 | 8 | 5 | 3 | 5 | 15 | 49 | right-- |
| 7 | 1 | 3 | 8 | 2 | 2 | 2 | 4 | 49 | right-- |
| 8 | 1 | 2 | 8 | 6 | 1 | 6 | 6 | 49 | right-- |

Exit condition: `left (1) < right (1)` is now false, loop ends.

**Final answer:** `maxArea = 49`, matching the expected output exactly — found at step 2, with lines at indices 1 (height 8) and 8 (height 7), width 7, height 7, area 49.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Brute force, all pairs | O(n²) | O(1) | Correct but far too slow for n up to 10^5 |
| 2. Two pointers, greedy elimination | O(n) | O(1) | Optimal; provably correct greedy discard of the shorter side |

Input/output space for `height` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Minimum-length array (`n = 2`, as in example 2) | Only one possible pair exists; must be evaluated correctly and returned as the answer | With `left=0, right=1` on the very first iteration, the single possible area is computed and stored in `maxArea`, and the loop ends immediately afterward (`left < right` becomes false once a pointer moves), correctly returning that value |
| All heights identical (e.g. `[5,5,5,5]`) | Every comparison `height[left] < height[right]` is false (they're equal), so the tie-breaking behavior must still make progress | The `else` branch (`right--`) handles ties by convention, and since the array traversal still makes forward progress (the window still shrinks every iteration regardless of which branch is taken), the loop correctly terminates while still having evaluated the true maximum, which for uniform heights is achieved at the widest (initial) window |
| A height of 0 present in the array | A line with height 0 can never contribute positively to any container's area (it forces `min(...)` to 0 for any pair including it) | The `min(height[left], height[right])` calculation naturally yields 0 for any pair involving a height-0 line, contributing an area of 0 to the `maxArea` comparison, which simply never wins against any better candidate — no special-casing needed, and such a line gets correctly and efficiently discarded on the very next comparison since it's guaranteed to be the shorter (or tied) side |
| The maximum-area pair is the very first pair checked (both endpoints of the array) | Should be found and correctly retained even as the pointers continue moving inward afterward | Since `maxArea` is tracked via `Math.max` across every iteration, an early optimal find is correctly preserved even as subsequent (necessarily narrower) candidates are evaluated and found to be no better |
| Strictly increasing or strictly decreasing height sequences | The greedy elimination must still correctly explore enough of the array to find the true optimum, not prematurely terminate | Since one pointer always advances every single iteration regardless of the specific height pattern, the algorithm is guaranteed to fully traverse and converge, correctly considering all the pairs that the greedy-elimination proof guarantees are worth considering, regardless of the specific shape of the height sequence |
| Maximum height and array length values simultaneously (`n=10^5`, heights up to `10^4`) | Potential for the area calculation to reach a large value; must not overflow | Maximum possible area is roughly `width * height <= (10^5) * (10^4) = 10^9`, which fits comfortably within `int` range (~2.1*10^9), so no overflow risk exists even at the extreme constraint boundaries |

## 9. Java Notes

- **`Math.min` / `Math.max` for primitive `int`:** used throughout for computing the bottleneck height and tracking the running best area; allocation-free and idiomatic.
- **No overflow risk:** as analyzed in the edge cases, the maximum possible area (`~10^9`) fits comfortably within `int` range given the problem's constraints, so no `long` arithmetic is needed.
- **Tie-breaking convention when `height[left] == height[right]`:** the code's `else` branch (moving `right` instead of `left`) is an arbitrary but valid choice — moving either pointer when the heights are equal is safe and correct, since both lines are equally "the bottleneck" in that case, and discarding either one is justified by the same greedy-elimination argument.
- **Single unified `while` loop, no nested loops:** unlike the brute-force approach's O(n²) nested structure, the optimized solution's single loop with two converging pointers is what directly achieves the O(n) bound.

## 10. Common Mistakes

- **Moving the pointer at the *taller* line instead of the shorter one.** This is the single most common conceptual error on this problem — moving the taller side's pointer inward can only shrink the width while the height remains capped by the same shorter line still present, which can never improve the area and wastes a step that could have explored a potentially taller replacement on the shorter side instead. Fix: always move the pointer pointing to the *shorter* of the two current lines.
- **Forgetting to update `maxArea` at every iteration, or updating it only conditionally in a way that misses valid candidates.** Since the optimal pair could be found at any point during the pointer convergence, `maxArea` must be recalculated and compared using `Math.max` on every single iteration, not just at the start or end. Fix: always compute the current area and update `maxArea` unconditionally at the top of each loop iteration, before deciding which pointer to move.
- **Assuming the brute-force O(n²) approach is necessary because "you have to check every pair to be sure."** This misses the key mathematical insight (the greedy-elimination proof) that guarantees checking only the specific sequence of pairs visited by the converging two-pointer approach is sufficient to find the true global optimum, without needing to explicitly verify every possible pair. Fix: trust and be able to explain the greedy-elimination argument rather than defaulting to brute force out of an abundance of caution.
- **Confusing this problem with "Trapping Rain Water" (LC 42) and attempting to apply that problem's technique of accumulating water at every individual position.** This problem only involves choosing *two* lines total to form a single container, not accumulating trapped water across the entire array at every position — a fundamentally different objective despite superficial similarity in using two pointers and a "shorter side" concept. Fix: keep clear that this problem seeks a single maximum area from exactly two chosen lines, not a sum of trapped water contributions.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Choose two lines to maximize a container's area, height capped by the shorter line → two pointers starting at both ends, greedily discarding whichever line is currently shorter, since it can never be part of a better solution."
- **90-second explanation:** "I start with two pointers at the very ends of the array, giving the maximum possible width. I compute the area using the shorter of the two lines as the height, since water can never rise above the shorter wall, and track the best area found so far. Then, the key insight: I move whichever pointer points to the *shorter* line inward. This is safe because moving the taller line's pointer instead would only shrink the width while the height stays capped by the same shorter line that's still there — that could never produce a better result. But moving the shorter line's pointer inward has a real chance of finding a taller replacement, which might compensate for the necessarily smaller width with a larger height. I repeat this process, updating my best-area tracker at every step, until the two pointers converge. This greedy elimination is provably correct, not just a heuristic, and it lets me find the true maximum in a single O(n) pass with O(1) extra space, compared to the O(n²) brute force of checking every possible pair."
- **Related problems using this pattern:**
  - LeetCode 42 — Trapping Rain Water (related two-pointer/shorter-side concept, but a fundamentally different accumulation objective)
  - LeetCode 167 — Two Sum II - Input Array Is Sorted (different converging two-pointer criterion, but same overall pattern family)
  - LeetCode 15 — 3Sum (builds on a similar two-pointer convergence technique, layered with an outer loop)

## 12. Recall Questions

**Q:** Why is it always safe to discard the pointer pointing to the *shorter* line, rather than the taller one, at each step of the greedy elimination?
**A:** Moving the taller line's pointer inward would only ever shrink the container's width while the height remains capped by the same shorter line that's still present, which can never produce a larger area than what's already been considered — only moving the shorter line's pointer has any chance of encountering a taller line that could compensate for the reduced width with a greater height.

**Q:** Why does the maximum possible area need to be tracked at every single iteration of the loop, rather than just at the very end?
**A:** The globally optimal pair of lines could be encountered at any point during the pointer convergence process, not necessarily at the very first or very last comparison, so the running maximum must be updated continuously to ensure the true best area is never missed.

**Q:** How is this problem fundamentally different from "Trapping Rain Water" (LC 42), despite both involving a two-pointer approach and a "shorter side" concept?
**A:** This problem seeks the single maximum area achievable from choosing exactly two lines out of the array, while Trapping Rain Water computes the total water accumulated across every individual position in the array, bounded by the tallest walls on each side — the objectives and what's being summed or maximized are fundamentally different.

**Q:** Why doesn't the algorithm need to explicitly check every possible pair of lines to guarantee finding the true maximum area?
**A:** The greedy elimination process is backed by a mathematical proof that discarding the shorter line's pointer at each step never eliminates a potentially optimal pair — any pair involving the just-discarded line can be shown to never exceed what's already been considered, so the reduced sequence of pairs actually visited during the converging scan is provably sufficient to find the global optimum.

**Q:** Why doesn't the algorithm risk integer overflow when computing the area, given the constraints allow both large widths and large heights?
**A:** The maximum possible product of the largest allowed width (up to just under 10^5) and the largest allowed height (up to 10^4) is on the order of 10^9, which fits comfortably within the range of a 32-bit `int`, so no overflow occurs even at the extreme boundary values allowed by the constraints.

## 13. Final Code

```java
public int maxArea(int[] height) {
    int left = 0;
    int right = height.length - 1;
    int maxArea = 0;

    while (left < right) {
        int width = right - left;
        int currentHeight = Math.min(height[left], height[right]);
        maxArea = Math.max(maxArea, width * currentHeight);

        if (height[left] < height[right]) {
            left++;
        } else {
            right--;
        }
    }

    return maxArea;
}
```

## 14. Self-Test

You have an array `height` representing vertical lines. Choose two lines that, with the x-axis, form a container holding the maximum water, where the area is width times the shorter line's height. Think about why greedily discarding whichever pointer currently points to the shorter line, and moving it inward, is guaranteed never to eliminate the true optimal pair.
