---
problem: 42 - Trapping Rain Water
url: https://leetcode.com/problems/trapping-rain-water/
difficulty: Hard
section: Array / Two Pointers
patterns: [two-pointers, left-right-max]
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

**Given:** An array `height` of non-negative integers representing an elevation map (each bar has width 1).

**Required:** Compute the total amount of water trapped between the bars after it rains.

**Constraints that actually matter:**
- `1 <= n <= 2 * 10^4` — moderate size; rules out anything worse than roughly O(n log n), and the explicit nature of the problem (no follow-up needed) still points strongly toward an O(n) solution given how classic this problem is.
- `0 <= height[i] <= 10^5` — non-negative heights; a height of 0 is a valid "gap" that could hold water if surrounded by taller bars.
- The water trapped **at any single position `i`** is determined by `min(maxHeightToLeft, maxHeightToRight) - height[i]` (if positive) — this is the core geometric insight: water at a position is bounded by whichever side has the *shorter* tallest wall, not the taller one, since water would simply spill over the shorter side.

## 2. Recognition Signals

- "Elevation map" + "compute trapped water" → the classic **Trapping Rain Water** signature; water at each bar is bounded by the shorter of its left-max and right-max walls.
- Whenever a quantity at each position depends on "the best value seen so far from the left" **and** "the best value seen so far from the right," and the answer takes the **minimum** of those two (not the max, unlike some other left/right combination problems) — this is the specific tell for this problem's variant of the left-max/right-max technique.
- Recognizing that you don't need to know each side's max exactly (via full precomputed arrays) if you process from whichever side currently has the smaller running max — this observation is what unlocks the O(1) space two-pointer refinement over the more straightforward O(n) space prefix/suffix array approach.

**Pattern:** Left-Max / Right-Max Combination, optimized into a Two-Pointer scan (a specialized array technique: track the running maximum from each direction, and let the side with the smaller max dictate how much water is trapped at each step).

## 3. Core Idea

For any bar at position `i`, the amount of water it can hold is capped by `min(leftMax, rightMax) - height[i]`, where `leftMax` is the tallest bar anywhere to the left of (or at) `i`, and `rightMax` is the tallest bar anywhere to the right of (or at) `i` — water can never rise higher than the shorter of the two "walls" bounding it, since it would overflow past the shorter side first.

The two-pointer refinement: maintain pointers `left` and `right` at both ends of the array, along with running maxes `leftMax` and `rightMax`. At each step, process whichever side currently has the smaller running max — if `leftMax < rightMax`, then no matter what lies further to the right (even a wall shorter than the current `rightMax`), the water level at the `left` pointer's position is *already* correctly bounded by `leftMax` alone, since `rightMax` is guaranteed to be at least `leftMax` regardless of unexplored bars beyond `right`. This lets you compute trapped water at each position using only the smaller side's max, without needing to know the other side's exact max in advance.

**Invariant:** Whenever `leftMax <= rightMax` (or symmetrically `rightMax <= leftMax`), the true right-max for any unprocessed position at or before `left` is guaranteed to be at least `leftMax` (since `rightMax`, itself a lower bound on the true right-max from the current `right` pointer onward, already meets or exceeds `leftMax`), so `leftMax` alone is sufficient to correctly compute the trapped water at `left`'s current position.

## 4. Approach 1 — Brute Force

**Logic:** For each position `i`, scan left to find the max height up to `i`, scan right to find the max height from `i` onward, then compute trapped water as `min(leftMax, rightMax) - height[i]` (if positive).

```java
public int trap(int[] height) {
    int n = height.length;
    int totalWater = 0;

    for (int i = 0; i < n; i++) {
        int leftMax = 0;
        for (int j = 0; j <= i; j++) {
            leftMax = Math.max(leftMax, height[j]);
        }

        int rightMax = 0;
        for (int j = i; j < n; j++) {
            rightMax = Math.max(rightMax, height[j]);
        }

        totalWater += Math.min(leftMax, rightMax) - height[i];
    }

    return totalWater;
}
```

- **Time:** O(n²) — for each of the `n` positions, two O(n) scans (left and right) are performed.
- **Space:** O(1) auxiliary — just a few running variables per position.

**Why it is not optimal:** With `n` up to `2 * 10^4`, O(n²) risks up to `4 * 10^8` operations in the worst case, likely too slow. It also recomputes the same left-max and right-max information redundantly across many overlapping positions instead of reusing prior work.

## 5. Approach 2 — Optimized (Two Pointers, Smaller-Side Processing)

**Algorithm:**
1. Initialize `left = 0`, `right = n - 1`, `leftMax = 0`, `rightMax = 0`, `totalWater = 0`.
2. While `left < right`:
   - If `height[left] < height[right]` (equivalently, using tracked maxes: `leftMax <= rightMax` after updating): update `leftMax = Math.max(leftMax, height[left])`, add `leftMax - height[left]` to `totalWater` (this is exactly the trapped water at `left`, since `leftMax` is provably sufficient as discussed in the Core Idea), then move `left` forward.
   - Otherwise: update `rightMax = Math.max(rightMax, height[right])`, add `rightMax - height[right]` to `totalWater`, then move `right` backward.
3. Return `totalWater`.

```java
public int trap(int[] height) {
    int left = 0;
    int right = height.length - 1;
    int leftMax = 0;
    int rightMax = 0;
    int totalWater = 0;

    while (left < right) {
        if (height[left] < height[right]) {
            leftMax = Math.max(leftMax, height[left]);
            totalWater += leftMax - height[left];
            left++;
        } else {
            rightMax = Math.max(rightMax, height[right]);
            totalWater += rightMax - height[right];
            right--;
        }
    }

    return totalWater;
}
```

- **Time:** O(n) — each of `left` and `right` moves inward once per iteration, together covering the array exactly once (`n` total steps across both pointers).
- **Space:** O(1) auxiliary — just the four running variables (`left`, `right`, `leftMax`, `rightMax`), no arrays needed.

**Why this is optimal:** Every position's height must be examined at least once to determine its contribution to trapped water, so O(n) is a hard lower bound. Space is O(1) because the two-pointer technique avoids needing full precomputed left-max/right-max arrays (which would cost O(n) space) — the key insight that processing the side with the smaller running max is always safe means only the single running max on each side needs to be remembered, not the max at every individual position. This is a genuine improvement over an O(n) time / O(n) space prefix-array approach, achieving the same O(n) time with O(1) space instead.

## 6. Dry Run

Example: `height = [4,2,0,3,2,5]`.
Chosen because it's the second canonical example, and it clearly shows the pointer processing switching sides multiple times as the running maxes change relative to each other.

`n = 6`. Initial: `left=0`, `right=5`, `leftMax=0`, `rightMax=0`, `totalWater=0`.

| Step | height[left] | height[right] | Compare | Action | leftMax/rightMax after | totalWater after | left/right after |
|------|---------------|-------------------|---------|--------|------------------------------|------------------------|---------------------|
| 1 | 4 | 5 | 4 < 5 | process left: leftMax=max(0,4)=4; water+=4-4=0 | leftMax=4 | 0 | left=1 |
| 2 | 2 | 5 | 2 < 5 | process left: leftMax=max(4,2)=4; water+=4-2=2 | leftMax=4 | 2 | left=2 |
| 3 | 0 | 5 | 0 < 5 | process left: leftMax=max(4,0)=4; water+=4-0=4 | leftMax=4 | 6 | left=3 |
| 4 | 3 | 5 | 3 < 5 | process left: leftMax=max(4,3)=4; water+=4-3=1 | leftMax=4 | 7 | left=4 |
| 5 | 2 | 5 | 2 < 5 | process left: leftMax=max(4,2)=4; water+=4-2=2 | leftMax=4 | 9 | left=5 |

Exit condition: `left (5) < right (5)` is now false, loop ends.

**Final answer:** `totalWater = 9`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Brute force, per-position left/right scan | O(n²) | O(1) | Correct but recomputes redundant work; too slow for n up to 2*10^4 |
| 2. Two pointers, smaller-side processing | O(n) | O(1) | Optimal in both time and space |

Input/output space for `height` is O(n) in both, as given by the problem. (A middle-ground O(n) time / O(n) space prefix-suffix array approach also exists but is dominated by the two-pointer version.)

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Fewer than 3 bars (`n <= 2`) | No water can ever be trapped with fewer than 3 bars (need at least a left wall, a low point, and a right wall) | The `while (left < right)` loop either never executes (`n=1`) or executes zero meaningful trapping iterations effectively (`n=2`, where `left=0, right=1` immediately processes both boundary bars with `leftMax`/`rightMax` starting at 0, correctly contributing 0 total water since there's no interior gap) |
| Strictly increasing heights (e.g. `[1,2,3,4,5]`) | No water can be trapped since there's no "container" shape; every bar is a new peak | At each step, `height[left] < height[right]` is essentially always true (since the right side, whatever remains, is the tallest overall), so `leftMax` keeps exactly matching `height[left]` at each position, making the trapped water contribution 0 every time |
| Strictly decreasing heights (e.g. `[5,4,3,2,1]`) | Symmetric to the increasing case; no water can be trapped | The right side is processed each time (`height[left] < height[right]` is false, since left is always taller or equal), and `rightMax` keeps exactly matching `height[right]`, again contributing 0 water throughout |
| A single deep valley (e.g. `[5,0,5]`) | Should correctly trap water bounded by the shorter of the two surrounding walls, which happen to be equal here | Both walls are height 5; processing proceeds correctly regardless of which side is chosen at the tie (`height[left] < height[right]` is false when equal, so the right side is processed, but the algorithm remains correct either way since both maxes end up equal to 5), trapping exactly `5 - 0 = 5` units at the middle position |
| Plateaus of equal height (e.g. `[3,3,3]`) | No local dips exist; no water should be trapped | Every position's own height matches the running max on whichever side is being processed, so each contribution is 0 |
| Very tall single peak surrounded by low, uneven bars | Must correctly use the *shorter* side's max, not accidentally use the taller side | The core two-pointer logic inherently always uses whichever side has the smaller running max at each step, which is exactly the geometrically correct choice — no special-casing needed for extreme height differences |

## 9. Java Notes

- **`Math.max` / `Math.min` for primitive `int`:** used throughout to track running maxes and to combine trapped-water calculations; allocation-free and idiomatic.
- **No overflow risk:** with `n` up to `2 * 10^4` and individual heights up to `10^5`, the maximum possible total trapped water is bounded well within `int` range (worst case roughly on the order of `n * maxHeight = 2*10^4 * 10^5 = 2*10^9`, which is close to but should be checked against `Integer.MAX_VALUE ≈ 2.1*10^9` — in practice, actual trapped water totals for valid elevation maps stay comfortably within `int` range for this problem's accepted solutions, but it's worth being aware of this boundary rather than assuming it blindly).
- **Choosing to compare `height[left] < height[right]` directly, rather than `leftMax < rightMax`:** both comparisons are actually equivalent in effect for the two-pointer algorithm's correctness (a well-known subtlety of this technique), since at the moment of comparison, `leftMax` and `rightMax` already reflect the maximum height processed so far on each respective side, and the current raw `height[left]`/`height[right]` values relate consistently to those running maxes — either comparison form is commonly seen in accepted solutions.

## 10. Common Mistakes

- **Using precomputed left-max and right-max arrays (O(n) space) without recognizing the further two-pointer optimization is possible.** This isn't wrong, and is a perfectly valid O(n) time solution, but misses the O(1) space refinement that's the more polished, interview-favored version of this pattern. Fix: recognize that processing the side with the smaller running max avoids needing to precompute or store full max arrays.
- **Forgetting to update the running max (`leftMax`/`rightMax`) *before* computing that position's trapped water contribution.** Computing the water first with a stale max would produce incorrect (too small) values. Fix: always update the relevant running max first, then use it immediately to compute the water contribution for the current position.
- **Confusing this problem with a simpler "container with most water" (LC 11) two-pointer problem, which computes a single maximum area rather than a sum of trapped water across every position.** Applying LC 11's logic here (which stops early once a wider container can't beat the current best) would produce a fundamentally wrong answer, since this problem needs to accumulate contributions from every single position, not just find one optimal pair of walls. Fix: recognize that Trapping Rain Water requires summing per-position contributions, not finding a single maximum.
- **Assuming water can be trapped with only 2 or fewer bars, or forgetting the trapped water at position `i` requires walls strictly on both sides.** Fix: trust the two-pointer loop's natural boundary handling (`left < right`), which correctly produces zero contributions when no true interior gap exists.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Trapped water at each position is bounded by the shorter of the tallest wall to its left and the tallest wall to its right → two pointers from both ends, always processing the side with the smaller running max."
- **90-second explanation:** "Water trapped at any bar is limited by whichever surrounding wall is shorter, since water spills over the shorter side first. I use two pointers starting at both ends of the array, along with two running maxes tracking the tallest bar seen so far from each side. At each step, I process whichever side currently has the smaller running max — this is safe because if my left running max is smaller than my right running max, then no matter what lies further to the right that I haven't looked at yet, the true right-max from that position onward is already guaranteed to be at least as large as my right running max, which is itself at least as large as my left running max. So I can confidently use just the left running max to compute the water trapped at the current left position, add it to my total, and move the left pointer inward. I do the mirror image when the right side has the smaller max. This runs in O(n) time and O(1) space, since I never need to precompute or store a full array of left-max or right-max values — just the two running maxes."
- **Related problems using this pattern:**
  - LeetCode 11 — Container With Most Water
  - LeetCode 407 — Trapping Rain Water II (2D generalization, needs a heap-based approach)
  - LeetCode 84 — Largest Rectangle in Histogram (related elevation/histogram family, different technique)

## 12. Recall Questions

**Q:** Why is the amount of water trapped at a given position determined by the *shorter* of the two surrounding walls, not the taller one?
**A:** Water can only rise as high as the lower of the two walls bounding it before it spills over that shorter side, so the shorter wall is always the binding constraint on how much water that position can hold.

**Q:** Why is it safe to process the side with the smaller running max at each step, without knowing the exact max on the other side yet?
**A:** If the running max on one side is smaller than the running max on the other side, then the true max on the larger side (which may still grow as more of it is explored) is already guaranteed to be at least as large as the smaller side's max, so the smaller side's max alone is sufficient to correctly compute trapped water at that position.

**Q:** How does this two-pointer approach improve on a solution that precomputes full left-max and right-max arrays for every position?
**A:** Both approaches run in O(n) time, but the two-pointer version only needs to track a single running max per side at any given moment rather than storing the max at every individual position, reducing auxiliary space from O(n) to O(1).

**Q:** Why does this problem require summing contributions across every position, unlike a problem that just finds one single maximum value or pair?
**A:** Trapped water can accumulate at many different low points along the elevation map simultaneously, so the total answer requires computing and summing each position's individual contribution, not just identifying one best pair of boundary walls.

**Q:** If the elevation map is strictly increasing from left to right, why does the algorithm correctly compute zero total trapped water?
**A:** In a strictly increasing sequence, every bar processed from the left is itself the new tallest bar seen so far on that side, so its own height always exactly equals the running left max at that moment, leaving no gap for water to fill.

## 13. Final Code

```java
public int trap(int[] height) {
    int left = 0;
    int right = height.length - 1;
    int leftMax = 0;
    int rightMax = 0;
    int totalWater = 0;

    while (left < right) {
        if (height[left] < height[right]) {
            leftMax = Math.max(leftMax, height[left]);
            totalWater += leftMax - height[left];
            left++;
        } else {
            rightMax = Math.max(rightMax, height[right]);
            totalWater += rightMax - height[right];
            right--;
        }
    }

    return totalWater;
}
```

## 14. Self-Test

You have an array `height` representing an elevation map with bars of width 1. Compute the total water trapped after it rains. Think about why the water trapped at any position is bounded by the shorter of the two tallest walls on either side of it, and why processing the side with the smaller running max, using two pointers moving inward, lets you compute the total in a single O(n) pass with O(1) extra space.
