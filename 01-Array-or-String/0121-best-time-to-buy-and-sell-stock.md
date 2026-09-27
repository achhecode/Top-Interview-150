---
problem: 121 - Best Time to Buy and Sell Stock
url: https://leetcode.com/problems/best-time-to-buy-and-sell-stock/
difficulty: Easy
section: Array / Dynamic Programming
patterns: [single-pass, running-minimum]
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

**Given:** An array `prices` where `prices[i]` is a stock's price on day `i`.

**Required:** Pick one day to buy and a strictly later day to sell, maximizing profit (`sell price - buy price`). If no profitable pair exists, return 0.

**Constraints that actually matter:**
- `1 <= prices.length <= 10^5` — large enough that the naive O(n²) "try every buy/sell pair" approach would be too slow (up to ~10^10 pair comparisons); this pushes toward an O(n) single-pass solution.
- `0 <= prices[i] <= 10^4` — non-negative prices, small range, no negative-price edge cases to worry about; doesn't otherwise change the algorithm.
- Exactly **one transaction** (one buy, one sell, buy strictly before sell) — this is what makes a simple running-minimum single pass sufficient; problems with multiple transactions (LC 122) need a different approach.

## 2. Recognition Signals

- "Choose a day to buy, a later day to sell, maximize profit, single transaction" → classic **running minimum + max profit tracking** in one pass.
- "Maximize `prices[j] - prices[i]` for `i < j`" → whenever you need the max difference between a later and an earlier element under an ordering constraint, track the best "earlier" value seen so far as you scan forward.
- The naive approach is an obvious O(n²) double loop over all buy/sell pairs — recognizing this and asking "can I avoid recomputing the best buy price for every sell day?" is the trigger to switch to tracking a running minimum instead.

**Pattern:** Single-Pass Running Minimum (a specialized greedy/DP pattern: track the cheapest "buy" seen so far, and at each day compute what selling today would yield against that minimum).

## 3. Core Idea

For any potential sell day `j`, the best possible profit is `prices[j] - (minimum price among all days before j)`. Rather than recomputing that minimum for every `j` (which would be O(n²)), scan left to right while maintaining a running minimum of prices seen so far. At each day, first check what profit selling today would yield against the running minimum, then update the running minimum to include today's price (for future days). This way each day is processed in O(1), using only the best-so-far information, never needing to look back explicitly.

**Invariant:** At the moment day `j` is processed, `minPrice` equals the minimum of `prices[0..j-1]` (strictly before `j`, matching the "sell after buy" requirement), so `prices[j] - minPrice` is exactly the best achievable profit if day `j` is the sell day.

## 4. Approach 1 — Brute Force

**Logic:** Try every valid buy day `i` and every sell day `j > i`, tracking the maximum profit found.

```java
public int maxProfit(int[] prices) {
    int maxProfit = 0;
    for (int i = 0; i < prices.length; i++) {
        for (int j = i + 1; j < prices.length; j++) {
            maxProfit = Math.max(maxProfit, prices[j] - prices[i]);
        }
    }
    return maxProfit;
}
```

- **Time:** O(n²) — nested loop over all pairs.
- **Space:** O(1) auxiliary — just the running `maxProfit`.

**Why it is not optimal:** With `n` up to `10^5`, O(n²) is far too slow (roughly 10^10 operations in the worst case), even though the space usage is already minimal. It recomputes information redundantly — for each `j`, the best `i` is always the minimum price before `j`, which doesn't need to be re-derived from scratch every time.

## 5. Approach 2 — Optimized (Running Minimum, Single Pass)

**Algorithm:**
1. Initialize `minPrice` to `prices[0]` (the cheapest price seen so far, initially just the first day) and `maxProfit` to 0.
2. For each day `i` from `1` to `prices.length - 1`:
   - Compute the profit if selling today: `prices[i] - minPrice`. Update `maxProfit` if this is larger.
   - Update `minPrice` to `Math.min(minPrice, prices[i])`, so future days compare against the best buy price up to and including today.
3. Return `maxProfit`.

```java
public int maxProfit(int[] prices) {
    int minPrice = prices[0];
    int maxProfit = 0;

    for (int i = 1; i < prices.length; i++) {
        maxProfit = Math.max(maxProfit, prices[i] - minPrice);
        minPrice = Math.min(minPrice, prices[i]);
    }

    return maxProfit;
}
```

- **Time:** O(n) — single pass, O(1) work per day.
- **Space:** O(1) auxiliary — just `minPrice` and `maxProfit`.

**Why this is optimal:** Every price must be examined at least once to know whether it's a better buy or sell opportunity, so O(n) is a hard lower bound. Space is O(1) because the only information needed to evaluate any future sell day is the single best (minimum) buy price seen so far — no need to remember the full history of prices or which specific day had the minimum. There's no time/space trade-off left to make; this is optimal in both dimensions simultaneously.

## 6. Dry Run

Example: `prices = [7,1,5,3,6,4]`.
Chosen because the minimum price occurs early (day 1) but the best profit comes from a later peak (day 4), and the price dips again afterward — exercising both the running-minimum update and the profit-tracking logic without them coinciding on the last day.

Initial: `minPrice = 7` (prices[0]), `maxProfit = 0`.

| i | prices[i] | prices[i] - minPrice | maxProfit after | minPrice after |
|---|-----------|------------------------|-------------------|------------------|
| 1 | 1 | 1 - 7 = -6 | 0 (unchanged, negative) | min(7,1) = 1 |
| 2 | 5 | 5 - 1 = 4 | 4 | min(1,5) = 1 |
| 3 | 3 | 3 - 1 = 2 | 4 (unchanged, 2 < 4) | min(1,3) = 1 |
| 4 | 6 | 6 - 1 = 5 | 5 | min(1,6) = 1 |
| 5 | 4 | 4 - 1 = 3 | 5 (unchanged, 3 < 5) | min(1,4) = 1 |

Exit condition: `i` reaches `prices.length` (6), loop ends.

**Final answer:** `maxProfit = 5`, matching the expected output (buy at price 1 on day 2, sell at price 6 on day 5, profit `6 - 1 = 5`).

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Brute force, all pairs | O(n²) | O(1) | Correct but far too slow for `n` up to 10^5 |
| 2. Running minimum, single pass | O(n) | O(1) | Optimal in both time and space |

Input/output space for `prices` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single-day array (`prices.length == 1`) | No valid sell day exists (must sell on a *different, later* day); should return 0 | The `for` loop starts at `i = 1`, and with length 1 the condition `1 < 1` is false, so the loop never executes and `maxProfit` stays 0, correctly returned |
| Strictly decreasing prices (e.g. `[7,6,4,3,1]`) | Every possible sell would be at a loss; must recognize no profit is achievable | Every `prices[i] - minPrice` computed is negative (since `minPrice` keeps tracking the lowest price, which is always the most recent one in a decreasing sequence), so `maxProfit` never updates from its initial 0 |
| Strictly increasing prices | Best strategy is buy on day 0, sell on the last day | `minPrice` stays fixed at `prices[0]` throughout (since no later price is ever lower), so each day's profit calculation correctly measures against the true best buy price, and the maximum naturally occurs on the last day |
| All prices identical | No profit possible; should return 0 | Every `prices[i] - minPrice` computation yields exactly 0, so `maxProfit` remains 0 throughout |
| Global minimum occurs on the very last day | Buying on the last day leaves no day left to sell, so it must never be used as a buy candidate that produces a "profit" | Since `minPrice` is only updated *after* the profit check for each day, the last day's own price is never compared against itself as a same-day buy-sell (would trivially be a 0 profit, but more importantly the algorithm never needs a same-day case since `minPrice` before the last iteration still reflects an earlier day) |
| `prices[i] = 0` (minimum allowed value) | Should behave like any other value; no special-casing needed | Plain integer subtraction and comparison work identically at 0, no divide-by-zero or sign issues since prices are non-negative per constraints |

## 9. Java Notes

- **`Math.max` / `Math.min` for primitive `int`:** these are simple, allocation-free comparisons for primitives, more idiomatic than manual `if`/`else` blocks for this kind of running-best tracking.
- **No overflow risk:** since `0 <= prices[i] <= 10^4`, the difference `prices[i] - minPrice` is bounded well within `int` range (at most `10^4`), so there's no need for `long` arithmetic here.
- **Update order matters within the loop body:** compute the profit for selling *today* using the minimum price from *before* today, and only update `minPrice` to include today's price afterward — reversing this order would incorrectly allow buying and selling on the same day (which the problem disallows, since it requires a different, later day).

## 10. Common Mistakes

- **Updating `minPrice` before computing the day's profit.** This would allow "buying" on the same day you're evaluating for selling, always yielding a profit of 0 for that day instead of correctly using the minimum from strictly earlier days. Fix: always compute the profit comparison first, then update `minPrice` afterward in the same iteration.
- **Using the brute-force O(n²) nested loop without considering the input size.** With `n` up to `10^5`, this risks a time limit exceeded verdict. Fix: recognize the "maximize `prices[j] - prices[i]` for `i < j`" shape as a running-minimum single-pass problem.
- **Forgetting to initialize `maxProfit` to 0 (instead of some other sentinel), or forgetting to allow it to stay 0 when no profitable transaction exists.** The problem explicitly requires returning 0 when no profit is achievable, not a negative number or an exception. Fix: initialize `maxProfit = 0` and only ever update it with `Math.max`, so it naturally never goes below 0.
- **Confusing this single-transaction problem with the "as many transactions as you like" variant (LC 122).** Applying a multi-transaction greedy approach (summing all positive day-over-day differences) would give a wrong, inflated answer here, since only one buy and one sell are allowed. Fix: always double-check whether the problem allows one transaction or multiple before choosing an approach.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Maximize `prices[j] - prices[i]` for `i < j` in one pass → track a running minimum of prices seen so far, and compare each day's price against it before updating it."
- **90-second explanation:** "I scan the prices once, keeping track of the lowest price seen so far as a potential buy day. At each day, before updating that running minimum, I first check what profit I'd get if I sold today against the best buy price found so far, and update my overall best profit if it's higher. Then I update the running minimum to include today's price, so it's ready for future days. Because I always check the profit *before* updating the minimum, I never accidentally allow buying and selling on the same day. This runs in O(n) time and O(1) space, since I only ever need the single best buy price seen so far, not the full price history."
- **Related problems using this pattern:**
  - LeetCode 122 — Best Time to Buy and Sell Stock II
  - LeetCode 123 — Best Time to Buy and Sell Stock III
  - LeetCode 188 — Best Time to Buy and Sell Stock IV
  - LeetCode 309 — Best Time to Buy and Sell Stock with Cooldown

## 12. Recall Questions

**Q:** Why is it correct to compute each day's potential profit using only the single running minimum, rather than checking against every earlier day individually?
**A:** For any fixed sell day, the best possible profit only depends on the lowest price among all earlier days, so tracking just that one minimum value carries all the information needed — the specific day it occurred on doesn't matter.

**Q:** Why must the profit calculation happen before updating `minPrice` in each iteration, rather than after?
**A:** Updating first would let the current day's own price be used as its own "buy" price, incorrectly permitting a same-day buy-and-sell, which the problem explicitly disallows since the sell day must be strictly later than the buy day.

**Q:** If the price array is strictly decreasing from start to end, what does this algorithm return, and why?
**A:** It returns 0, because every day's price is lower than or equal to all previous prices, so `minPrice` always equals the current day's price, making every profit calculation zero or negative.

**Q:** Why doesn't this algorithm work correctly if the problem allowed unlimited buy/sell transactions instead of just one?
**A:** Tracking a single running minimum only captures the best possible one-time buy-low-sell-high opportunity; with multiple transactions allowed, profit can be captured from every local upward price movement, which requires summing consecutive gains rather than a single running minimum comparison.

**Q:** Why is there no risk of integer overflow when computing `prices[i] - minPrice` given the problem's constraints?
**A:** Prices are bounded between 0 and `10^4`, so the maximum possible difference is only `10^4`, well within the range of a 32-bit `int` with enormous headroom to spare.

## 13. Final Code

```java
public int maxProfit(int[] prices) {
    int minPrice = prices[0];
    int maxProfit = 0;

    for (int i = 1; i < prices.length; i++) {
        maxProfit = Math.max(maxProfit, prices[i] - minPrice);
        minPrice = Math.min(minPrice, prices[i]);
    }

    return maxProfit;
}
```

## 14. Self-Test

You have an array `prices` where `prices[i]` is a stock's price on day `i`. Choose one day to buy and a later day to sell to maximize profit, or return 0 if no profit is possible. Think about how tracking just the lowest price seen so far, and checking each day's profit against it before updating it, lets you solve this in a single O(n) pass.
