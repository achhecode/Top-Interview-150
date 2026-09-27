---
problem: 135 - Candy
url: https://leetcode.com/problems/candy/
difficulty: Hard
section: Array / Greedy
patterns: [two-pass-greedy, left-right-scan]
data_structures: [array]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** `n` children in a line, each with a rating `ratings[i]`.

**Required:** Distribute candies such that every child gets at least 1 candy, and any child with a **strictly higher rating than a neighbor** gets **more candy than that neighbor**. Return the minimum total candies needed.

**Constraints that actually matter:**
- `1 <= n <= 5 * 10^4` — large enough that any approach re-scanning neighbors repeatedly (like naive local fix-up loops that might not converge quickly) needs to be bounded; a fixed number of linear passes (specifically two) is the expected complexity class.
- `0 <= ratings[i] <= 5 * 10^4` — ratings can tie; the rule only applies to **strictly higher** ratings compared to a neighbor — equal ratings impose no ordering constraint between two adjacent children, which is a subtlety worth being precise about.
- The requirement is only about **immediate neighbors** (left and right), not a global ordering — this local-comparison-only nature is exactly what makes a two-pass left-to-right then right-to-left greedy scan sufficient, without needing global sorting or graph-based reasoning.

## 2. Recognition Signals

- "Compare each element only to its immediate neighbors, on both sides, with a monotonic 'give more if higher' rule" → the signature of the **two-pass greedy scan** (left-to-right pass handles the "higher than left neighbor" rule, right-to-left pass handles the "higher than right neighbor" rule).
- A single left-to-right pass alone can satisfy the "higher rating than the *left* neighbor gets more candy" rule, but cannot simultaneously guarantee the mirrored rule for the *right* neighbor — recognizing this asymmetry is the trigger to add a second, opposite-direction pass and combine results with `max`.
- Whenever a constraint must hold in both directions along a sequence (compare to left AND compare to right) — this is a strong signal for two directional passes rather than trying to handle both directions in a single scan.

**Pattern:** Two-Pass Greedy (left-to-right pass, then right-to-left pass, combining each child's requirement from both directions via `Math.max`).

## 3. Core Idea

Handle the two neighbor-comparison rules separately, since a single direction of scanning can only correctly enforce one of them at a time:

1. **Left-to-right pass:** Initialize every child to 1 candy. Scan left to right; whenever `ratings[i] > ratings[i-1]`, set `candies[i] = candies[i-1] + 1` (ensures child `i` has more than their *left* neighbor, whenever that's required). This pass alone correctly satisfies every "higher than left neighbor" requirement.

2. **Right-to-left pass:** Scan right to left; whenever `ratings[i] > ratings[i+1]`, child `i` needs more than their *right* neighbor. But child `i` might already have a value from the first pass (needed to be more than their *left* neighbor) — so take `candies[i] = Math.max(candies[i], candies[i+1] + 1)`, ensuring both constraints (from left and from right) are simultaneously satisfied without violating either.

The `max` combination is essential: each child's final candy count must be *at least* what's required to beat a higher-rated left neighbor, AND *at least* what's required to beat a higher-rated right neighbor — taking the max of both computed requirements guarantees both hold at once.

**Invariant:** After the left-to-right pass, `candies[i] > candies[i-1]` whenever `ratings[i] > ratings[i-1]`, for every `i`. After the right-to-left pass (combined via `max`), both that property and the mirrored property (`candies[i] > candies[i+1]` whenever `ratings[i] > ratings[i+1]`) hold simultaneously for every `i`.

## 4. Approach 1 — Brute Force (Iterative Local Fix-Up)

**Logic:** Start everyone at 1 candy. Repeatedly scan the array, and whenever a neighbor-rating rule is violated, bump the violating child's candy count up by 1. Repeat until no violations remain in a full pass.

```java
public int candy(int[] ratings) {
    int n = ratings.length;
    int[] candies = new int[n];
    Arrays.fill(candies, 1);

    boolean changed = true;
    while (changed) {
        changed = false;
        for (int i = 0; i < n; i++) {
            if (i > 0 && ratings[i] > ratings[i - 1] && candies[i] <= candies[i - 1]) {
                candies[i] = candies[i - 1] + 1;
                changed = true;
            }
            if (i < n - 1 && ratings[i] > ratings[i + 1] && candies[i] <= candies[i + 1]) {
                candies[i] = candies[i + 1] + 1;
                changed = true;
            }
        }
    }

    int total = 0;
    for (int c : candies) {
        total += c;
    }
    return total;
}
```

- **Time:** O(n²) in the worst case — each full pass is O(n), and in the worst case (e.g., a long strictly increasing or "mountain" rating sequence), violations can propagate one position per pass, requiring up to O(n) passes.
- **Space:** O(n) for the `candies` array.

**Why it is not optimal:** The repeated re-scanning until convergence can degrade to O(n²) for adversarial rating patterns, which risks a time limit exceeded verdict given `n` up to `5 * 10^4`. It's also harder to reason about termination and correctness compared to the clean two-pass approach.

## 5. Approach 2 — Optimized (Two-Pass Greedy)

**Algorithm:**
1. Create a `candies` array of size `n`, initialized entirely to 1 (every child starts with the minimum required).
2. **Left-to-right pass:** For `i` from `1` to `n-1`: if `ratings[i] > ratings[i-1]`, set `candies[i] = candies[i-1] + 1`.
3. **Right-to-left pass:** For `i` from `n-2` down to `0`: if `ratings[i] > ratings[i+1]`, set `candies[i] = Math.max(candies[i], candies[i+1] + 1)`.
4. Sum all values in `candies` and return the total.

```java
public int candy(int[] ratings) {
    int n = ratings.length;
    int[] candies = new int[n];
    Arrays.fill(candies, 1);

    for (int i = 1; i < n; i++) {
        if (ratings[i] > ratings[i - 1]) {
            candies[i] = candies[i - 1] + 1;
        }
    }

    for (int i = n - 2; i >= 0; i--) {
        if (ratings[i] > ratings[i + 1]) {
            candies[i] = Math.max(candies[i], candies[i + 1] + 1);
        }
    }

    int total = 0;
    for (int c : candies) {
        total += c;
    }
    return total;
}
```

- **Time:** O(n) — two linear passes plus one linear summation, all O(n).
- **Space:** O(n) auxiliary for the `candies` array (not counted as "extra" if the output requires this level of detail, but it is genuine working memory needed by the algorithm, distinct from just the input/output size, since a running total alone wouldn't be enough to compute the right-to-left pass without first knowing each position's left-pass value).

**Why this is optimal:** Every child's rating must be compared with both neighbors, so O(n) is a hard lower bound on time — and two fixed linear passes achieve this exactly, unlike the brute-force fix-up approach, which can degrade to O(n²). The two-pass structure is necessary (not just convenient) because a single directional scan cannot simultaneously guarantee correctness for both the "higher than left neighbor" and "higher than right neighbor" rules — the `max` combination step is what reconciles both constraints without violating either.

## 6. Dry Run

Example: `ratings = [1,0,2]`.
Chosen because it's the canonical example and clearly shows both passes contributing to the final result (a "valley" shape where both neighbors of the middle element are higher).

`n = 3`. Initial: `candies = [1,1,1]`.

**Left-to-right pass:**

| i | ratings[i] | ratings[i-1] | ratings[i] > ratings[i-1]? | candies[i] after |
|---|------------|-----------------|----------------------------------|----------------------|
| 1 | 0 | 1 | No | 1 (unchanged) |
| 2 | 2 | 0 | Yes | candies[1]+1 = 1+1 = 2 |

After left pass: `candies = [1,1,2]`.

**Right-to-left pass:**

| i | ratings[i] | ratings[i+1] | ratings[i] > ratings[i+1]? | candies[i] after |
|---|------------|-----------------|----------------------------------|----------------------|
| 1 | 0 | 2 | No | 1 (unchanged) |
| 0 | 1 | 0 | Yes | max(1, candies[1]+1) = max(1, 1+1) = 2 |

After right pass: `candies = [2,1,2]`.

Exit condition: both passes complete after processing all applicable indices.

**Final answer:** Sum of `candies = [2,1,2]` is `2+1+2 = 5`, matching the expected output exactly, and the specific distribution matches the example's own explanation.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Iterative local fix-up | O(n²) worst case | O(n) | Correct but can degrade badly on adversarial rating patterns |
| 2. Two-pass greedy | O(n) | O(n) | Optimal in time; the candies array itself is necessary working memory |

Input space for `ratings` is O(n) in both; the `candies` array is genuine auxiliary space required to track per-child results across both passes before summing.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single child (`n == 1`) | No neighbors exist; trivially needs exactly 1 candy | Both passes' loops (`i` from 1, and `i` from `n-2` down to 0) don't execute meaningfully for `n=1` (the right-to-left pass's starting index `n-2 = -1` means the loop condition `i >= 0` is false immediately), leaving `candies = [1]`, summing correctly to 1 |
| All ratings equal | No strict inequality is ever satisfied in either direction; everyone should get exactly 1 candy | Neither pass's `if` condition (`>`, strictly) is ever true, so `candies` remains all 1s throughout, summing to `n` |
| Strictly increasing ratings (e.g. `[1,2,3,4,5]`) | Left pass should build up increasing candy counts; right pass should find nothing further to adjust | Left pass correctly produces `[1,2,3,4,5]` since each rating exceeds the previous; right pass never triggers (no `ratings[i] > ratings[i+1]` anywhere in a strictly increasing sequence), leaving the result unchanged |
| Strictly decreasing ratings (e.g. `[5,4,3,2,1]`) | Right pass should build up increasing-from-the-right candy counts; left pass should find nothing to adjust | Left pass never triggers (no `ratings[i] > ratings[i-1]` in a strictly decreasing sequence), leaving all 1s initially; right pass then correctly builds `[5,4,3,2,1]` from the right end backward |
| A "mountain" or "valley" shape requiring both passes to contribute at the same index | Some children need their final value combined from both directions, not just one | The `Math.max` combination in the right-to-left pass ensures that whichever requirement (from left or right) demands more candy for a given child, that larger value wins, correctly satisfying both simultaneously — exactly as demonstrated in the dry run above |
| Minimum rating value 0 appearing anywhere | Should behave like any other rating value; no special-casing needed for the value 0 itself | Comparisons are purely relative (`>` between adjacent ratings), so the absolute value 0 has no special meaning to the algorithm beyond its relation to its neighbors |

## 9. Java Notes

- **`Arrays.fill(candies, 1)`:** idiomatic, efficient way to initialize every element of a primitive `int[]` to the same baseline value, rather than a manual loop.
- **`Math.max` in the right-to-left pass:** essential for correctly reconciling the two directional requirements without a manual `if`/`else` comparison — concise and clear.
- **No overflow risk in individual candy counts:** since candy counts grow by at most 1 per strictly-increasing step in a chain, and `n` is bounded at `5 * 10^4`, the maximum possible single child's candy count is bounded by `n` itself, and the total sum is bounded by roughly `O(n²)` in the absolute worst case (a full strictly-monotonic-then-reversing sequence), which for `n = 5*10^4` is on the order of `2.5 * 10^9` — this actually *can* exceed `int` range in the most extreme theoretical case, so using a `long` accumulator for the final `total` sum is a defensively sound choice worth considering, even though individual `candies[i]` values themselves stay within `int` range comfortably.
- **Two separate loops rather than attempting a single combined pass:** deliberately structured this way because the right-to-left requirement genuinely depends on information (the left-pass result) that isn't available until the first pass has fully completed — attempting to merge both directions into one pass would require look-ahead information not yet computed.

## 10. Common Mistakes

- **Trying to satisfy both neighbor-comparison rules in a single directional pass.** A single left-to-right scan can correctly enforce "higher rating than left neighbor gets more candy," but has no way to correctly enforce the mirrored right-neighbor rule without already knowing future values — attempting to hack this into one pass typically produces incorrect results for "valley" or "mountain" patterns. Fix: always use two separate passes, one in each direction, combined via `max`.
- **Overwriting instead of taking the max in the second pass.** Setting `candies[i] = candies[i+1] + 1` unconditionally (instead of `Math.max(candies[i], candies[i+1] + 1)`) in the right-to-left pass can silently violate the left-to-right pass's already-established requirement for that same child. Fix: always combine with `Math.max`, never overwrite, in the second pass.
- **Using `>=` instead of strict `>` in either pass's comparison.** The problem specifically requires *strictly higher* ratings to warrant more candy — equal ratings impose no ordering requirement between neighbors, so using `>=` would incorrectly force extra candy in tie situations. Fix: always use strict `>` when comparing adjacent ratings.
- **Assuming the iterative fix-up approach (Approach 1) is "good enough" without considering its worst-case time complexity.** While it's correct, its potential O(n²) blowup on certain rating patterns makes it risky for the given input size; interviewers specifically look for recognizing and implementing the more disciplined two-pass approach for this well-known "Hard" difficulty problem. Fix: default to the two-pass greedy method, which has a clean, provable O(n) bound.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Compare each element to both its left and right neighbor with a monotonic 'give more if higher' rule → two-pass greedy: left-to-right pass for the left-neighbor rule, right-to-left pass for the right-neighbor rule, combined with max."
- **90-second explanation:** "I start every child at the minimum of 1 candy. In a left-to-right pass, whenever a child's rating is higher than the child immediately to their left, I give them one more candy than that left neighbor — this alone correctly satisfies every 'higher than left neighbor' requirement. Then, in a right-to-left pass, whenever a child's rating is higher than the child immediately to their right, I need them to have more candy than that right neighbor too — but they might already have a value from the first pass, so I take the maximum of their current value and one more than their right neighbor's value, ensuring both directional requirements are satisfied simultaneously without violating either. Finally, I sum up everyone's candy count for the total. This runs in O(n) time using two linear passes, which is necessary because a single directional scan can't enforce both neighbor comparisons at once."
- **Related problems using this pattern:**
  - LeetCode 42 — Trapping Rain Water (also uses left-pass/right-pass combined via max/min)
  - LeetCode 134 — Gas Station (different greedy pattern, but similarly a single-direction greedy scan)
  - LeetCode 845 — Longest Mountain in Array (related "compare to both neighbors" structure)

## 12. Recall Questions

**Q:** Why can't a single left-to-right pass alone correctly satisfy both the "higher than left neighbor" and "higher than right neighbor" requirements?
**A:** A left-to-right pass only has information about each child's left neighbor at the time it processes that child; it has no way to know or enforce anything about a not-yet-processed right neighbor, so a separate pass in the opposite direction is needed to handle that mirrored requirement.

**Q:** Why must the right-to-left pass combine its result with `Math.max` rather than simply overwriting the left-pass value?
**A:** Overwriting would discard whatever candy count was already established to satisfy the left-neighbor requirement from the first pass, potentially violating it; taking the max ensures the final value satisfies whichever requirement (from either direction) demands more candy.

**Q:** Why does the problem's use of strict inequality (`>`, not `>=`) in the neighbor comparison matter for the algorithm's correctness?
**A:** The rule only requires more candy when a rating is strictly higher than a neighbor's; equal ratings impose no ordering constraint, so using a non-strict comparison would incorrectly force extra candy allocation in tie situations that the problem doesn't actually require.

**Q:** In what kind of rating pattern would the iterative local fix-up approach (Approach 1) degrade to its worst-case O(n²) behavior?
**A:** A long strictly monotonic run (increasing or decreasing) followed by a reversal can cause violations to propagate only one position per full scan of the array, requiring on the order of n repeated passes to fully converge.

**Q:** How does the two-pass approach's final candy value at each position reflect the combination of both directional constraints?
**A:** Each child's final value is the maximum of what the left-to-right pass determined was needed to beat a higher-rated left neighbor and what the right-to-left pass determined was needed to beat a higher-rated right neighbor, guaranteeing both constraints hold simultaneously.

## 13. Final Code

```java
public int candy(int[] ratings) {
    int n = ratings.length;
    int[] candies = new int[n];
    Arrays.fill(candies, 1);

    for (int i = 1; i < n; i++) {
        if (ratings[i] > ratings[i - 1]) {
            candies[i] = candies[i - 1] + 1;
        }
    }

    for (int i = n - 2; i >= 0; i--) {
        if (ratings[i] > ratings[i + 1]) {
            candies[i] = Math.max(candies[i], candies[i + 1] + 1);
        }
    }

    long total = 0;
    for (int c : candies) {
        total += c;
    }
    return (int) total;
}
```

## 14. Self-Test

You have `n` children in a line, each with a rating. Distribute candies so every child gets at least 1, and any child with a strictly higher rating than a neighbor gets more candy than that neighbor. Return the minimum total candies needed. Think about why a single directional scan can't satisfy both the left-neighbor and right-neighbor requirements at once, and how combining two opposite-direction passes with a max resolves that.
