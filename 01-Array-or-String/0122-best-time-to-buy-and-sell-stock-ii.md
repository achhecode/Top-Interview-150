---
problem: 122 - Best Time to Buy and Sell Stock II
url: https://leetcode.com/problems/best-time-to-buy-and-sell-stock-ii/
difficulty: Medium
section: Array / Greedy
patterns: [greedy, sum-of-positive-differences]
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

**Required:** Unlike LC 121, you may buy and sell **as many times as you want**, but can hold at most one share at a time (you must sell before buying again — though same-day buy-then-sell-then-rebuy is allowed and has no effect on the math). Return the maximum total profit achievable.

**Constraints that actually matter:**
- `1 <= prices.length <= 3 * 10^4` — moderate size, but more importantly, the "unlimited transactions" rule is what fundamentally changes the approach from LC 121's single running minimum to a greedy summation strategy.
- `0 <= prices[i] <= 10^4` — non-negative, small range; no special edge-case handling needed for negative or extreme values.
- "You can sell and buy... multiple times on the same day" — this clarifies that same-day round trips are allowed but irrelevant to the optimal strategy (they can never help, since buying and selling at the same price nets zero), so it doesn't change the algorithm at all.

## 2. Recognition Signals

- "Maximize profit" + "**as many transactions as you like**" (versus a single transaction) → signals a **greedy** approach: capture every profitable up-move, since transactions are free and unlimited.
- Whenever a problem lets you take unlimited non-overlapping opportunities from a sequence with no cost or limit per opportunity → sum up every positive local gain.
- Distinguishing this from LC 121 (single transaction, "running minimum") — the phrase "as many transactions as you'd like" is the specific tell that a running-minimum tracking a single buy/sell pair is *not* the right model anymore.

**Pattern:** Greedy — Sum of Positive Consecutive Differences.

## 3. Core Idea

Since there's no limit on the number of transactions and no transaction cost, the maximum total profit is achieved by capturing every single upward price movement, no matter how small. Any price sequence that goes up over a stretch of days can be decomposed into buying at the start of that upward stretch and selling at the end — but since transactions are free, it's equivalent (and easier to reason about) to instead buy and sell on every single consecutive day where the price increases, summing `prices[i] - prices[i-1]` whenever that difference is positive. Any zigzag pattern of ups and downs is optimally handled by capturing only the "up" segments and ignoring the "down" segments entirely (never buy before a price drop).

**Invariant:** Summing `max(0, prices[i] - prices[i-1])` for every `i` from 1 to `n-1` exactly equals the maximum achievable profit, because any longer upward run's total gain equals the sum of its constituent single-day gains (telescoping), and it's never beneficial to hold through a price decrease.

## 4. Approach 1 — Brute Force (Exhaustive Search / DP over transaction states)

**Logic:** A more "obvious" but heavier approach: use recursion (or DP) to explicitly try, at each day, whether to hold, buy, or sell, exploring all valid sequences of transactions and taking the maximum.

```java
public int maxProfit(int[] prices) {
    return solve(prices, 0, false);
}

private int solve(int[] prices, int day, boolean holding) {
    if (day == prices.length) {
        return 0;
    }

    // Option 1: do nothing today.
    int best = solve(prices, day + 1, holding);

    if (holding) {
        // Option 2: sell today.
        best = Math.max(best, prices[day] + solve(prices, day + 1, false));
    } else {
        // Option 2: buy today.
        best = Math.max(best, -prices[day] + solve(prices, day + 1, true));
    }

    return best;
}
```

- **Time:** O(2^n) without memoization — each day branches into two choices; with memoization on `(day, holding)` it reduces to O(n), but still requires extra bookkeeping.
- **Space:** O(n) recursion stack depth in the worst case, plus O(n) memoization table if added.

**Why it is not optimal:** The unmemoized version is exponential and impractical for `n` up to `3 * 10^4`. Even the memoized version, while O(n) time, needs O(n) auxiliary space for memoization and recursion — more complex and heavier than necessary when a direct greedy insight is available and provably correct.

## 5. Approach 2 — Optimized (Greedy, Sum of Positive Differences)

**Algorithm:**
1. Initialize `totalProfit = 0`.
2. For each day `i` from `1` to `prices.length - 1`:
   - If `prices[i] > prices[i-1]`, add the difference `prices[i] - prices[i-1]` to `totalProfit` (capturing this day's gain, conceptually "buy yesterday, sell today").
   - Otherwise, do nothing (no profit to capture from a flat or falling day).
3. Return `totalProfit`.

```java
public int maxProfit(int[] prices) {
    int totalProfit = 0;

    for (int i = 1; i < prices.length; i++) {
        if (prices[i] > prices[i - 1]) {
            totalProfit += prices[i] - prices[i - 1];
        }
    }

    return totalProfit;
}
```

- **Time:** O(n) — single pass, O(1) work per day.
- **Space:** O(1) auxiliary — just the running `totalProfit`.

**Why this is optimal:** Every day's price must be examined at least once to determine whether it represents a profitable opportunity, so O(n) is a hard lower bound. Space is O(1) because no history beyond the immediately previous day's price is ever needed — the greedy decision at each step only depends on the local day-over-day comparison. This is a case where the greedy choice is provably optimal (not just a heuristic): summing every positive daily delta always equals the true maximum achievable profit with unlimited transactions, so there's no better algorithm to find, and this simultaneously beats the DP/recursive approach in both time and space.

## 6. Dry Run

Example: `prices = [7,1,5,3,6,4]`.
Chosen because it has both an upward run (1→5) and a downward dip (5→3) followed by another upward run (3→6), directly matching the example's own two-transaction explanation and exercising both the "capture" and "skip" branches.

Initial: `totalProfit = 0`.

| i | prices[i] | prices[i-1] | prices[i] > prices[i-1]? | Contribution | totalProfit after |
|---|-----------|--------------|-----------------------------|----------------|----------------------|
| 1 | 1 | 7 | No | 0 | 0 |
| 2 | 5 | 1 | Yes | 5 - 1 = 4 | 4 |
| 3 | 3 | 5 | No | 0 | 4 |
| 4 | 6 | 3 | Yes | 6 - 3 = 3 | 7 |
| 5 | 4 | 6 | No | 0 | 7 |

Exit condition: `i` reaches `prices.length` (6), loop ends.

**Final answer:** `totalProfit = 7`, matching the expected output exactly — and note the greedy sum `(5-1) + (6-3) = 4 + 3 = 7` exactly matches the two-transaction breakdown given in the problem's own explanation, confirming the greedy decomposition captures the same optimal trades.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Recursive/DP over buy-sell states | O(2^n) unmemoized, O(n) memoized | O(n) (recursion stack / memo table) | Correct but unnecessarily heavy; the greedy insight avoids all of this |
| 2. Greedy, sum of positive daily deltas | O(n) | O(1) | Optimal; provably matches the true maximum profit |

Input/output space for `prices` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single-day array (`prices.length == 1`) | No day-over-day comparison possible; should return 0 | The loop starts at `i = 1`, and with length 1 the condition `1 < 1` is false, so it never executes, leaving `totalProfit = 0`, correctly returned |
| Strictly decreasing prices | No profitable day-over-day move exists anywhere | Every comparison `prices[i] > prices[i-1]` is false, so `totalProfit` never increases from 0 |
| Strictly increasing prices | Every single day-over-day step is profitable; total should equal `prices[last] - prices[first]` | Every comparison is true, and the sum of consecutive positive differences telescopes exactly to `prices[n-1] - prices[0]`, matching what a single buy-at-start/sell-at-end strategy would yield |
| All prices identical | No profit possible; every day-over-day difference is zero | Every comparison `prices[i] > prices[i-1]` is false (strictly greater, not greater-or-equal), so nothing is added, and `totalProfit` stays 0 |
| Zigzag pattern (alternating up/down every day) | Must correctly capture every up-move independently without conflating with the down-moves | Since each day is evaluated independently based only on the immediately previous day, alternating patterns are handled correctly regardless of how frequently the direction changes |
| `prices[i] = 0` (minimum allowed value) | Should behave like any other value; no special-casing needed | Plain integer subtraction and comparison work identically at 0, since prices are non-negative per constraints and no division or sign-based logic is used |

## 9. Java Notes

- **No overflow risk:** since `0 <= prices[i] <= 10^4` and `prices.length <= 3 * 10^4`, the theoretical maximum total profit is bounded well within `int` range (at most on the order of `3 * 10^4 * 10^4 = 3 * 10^8`, still comfortably under `Integer.MAX_VALUE`), so no `long` arithmetic is needed.
- **Simple `+=` accumulation:** the greedy sum is just a running total updated conditionally — no library calls, comparators, or auxiliary structures needed, keeping the solution extremely lightweight.
- **Avoiding recursion for this variant:** unlike more constrained versions of this problem family (LC 123, LC 188, LC 309, which do require DP due to transaction-count or cooldown limits), the unlimited-transaction variant here has a closed-form greedy solution, so recursion/memoization is unnecessary overhead.

## 10. Common Mistakes

- **Applying the LC 121 "running minimum" single-transaction approach here without adjusting for unlimited transactions.** That approach would only find the single best buy-sell pair, undercounting the achievable profit when multiple profitable up-moves exist (as in the dry run above, where the correct answer requires summing two separate gains, not just the largest one). Fix: recognize "as many transactions as you'd like" as the specific signal to switch to the greedy day-over-day summation instead.
- **Using `>=` instead of `>` in the comparison.** While this happens to not change the result mathematically (a zero difference contributes nothing either way), it's a sign of not having reasoned precisely about why only strictly positive differences matter — worth being deliberate about it in an interview explanation. Fix: use strict `>` and be able to explain that equal-or-decreasing days contribute nothing.
- **Overcomplicating the solution with explicit buy/sell state tracking (recursion or DP) when the greedy approach suffices.** This adds unnecessary time and space overhead for a problem that has a direct, provably optimal greedy solution. Fix: recognize that unlimited, cost-free transactions is the specific condition that unlocks the simpler greedy approach — more constrained variants (limited transaction count, cooldown, transaction fee) genuinely do need DP.
- **Forgetting that "buy and sell multiple times on the same day" is explicitly allowed but has zero effect on the optimal answer, and trying to special-case same-day transactions.** This is a red herring in the problem statement meant to clarify allowed behavior, not something the algorithm needs to explicitly model. Fix: trust that the greedy day-over-day delta sum already captures the true optimum without any same-day special casing.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Unlimited buy/sell transactions, maximize total profit → greedily sum every positive day-over-day price increase."
- **90-second explanation:** "Since I can make as many transactions as I want with no cost, the optimal strategy is to capture every single profitable up-move in the price sequence, no matter how small. I scan through the prices once, and whenever today's price is higher than yesterday's, I add that difference to my running total profit — conceptually, that's like buying yesterday and selling today. Any longer upward run of days decomposes into the sum of its individual day-over-day gains anyway, so this greedy, day-by-day approach captures exactly the same total profit as any more complex strategy of holding across a whole upward run. Downward or flat days simply contribute nothing. This runs in O(n) time and O(1) space, with no need for DP or recursion, since the unlimited-transaction condition makes the greedy approach provably optimal."
- **Related problems using this pattern:**
  - LeetCode 121 — Best Time to Buy and Sell Stock
  - LeetCode 123 — Best Time to Buy and Sell Stock III
  - LeetCode 188 — Best Time to Buy and Sell Stock IV
  - LeetCode 309 — Best Time to Buy and Sell Stock with Cooldown
  - LeetCode 714 — Best Time to Buy and Sell Stock with Transaction Fee

## 12. Recall Questions

**Q:** Why does summing every positive day-over-day price difference give the same total profit as buying at the start and selling at the end of each upward run?
**A:** The sum telescopes — for a run of consecutive increasing days, the sum of consecutive single-day gains equals the difference between the run's final and starting prices, so both approaches yield an identical total.

**Q:** Why is the greedy approach provably optimal here, when greedy strategies aren't always optimal for other problems?
**A:** Because transactions are unlimited and free, there's no cost or restriction that would ever make it better to skip a profitable single-day move in favor of waiting for a larger one — capturing every available gain independently can only help or be neutral, never hurt.

**Q:** How would you need to change this approach if the problem limited you to at most two transactions instead of unlimited?
**A:** The simple greedy sum no longer applies, since capturing every small up-move might use up transaction "budget" that a smarter, more global strategy would spend differently; that constrained version (LC 123) requires dynamic programming over transaction counts instead.

**Q:** If the price array is strictly decreasing throughout, what does the greedy algorithm return, and why?
**A:** It returns 0, because every day-over-day comparison is false (no day is ever higher than the previous day), so no positive differences are ever added to the running total.

**Q:** Why doesn't the "you can buy and sell multiple times on the same day" clarification in the problem statement require any special handling in the algorithm?
**A:** Buying and selling at the same price on the same day nets zero profit and has no effect on the total, so the greedy day-over-day delta sum already produces the correct optimal answer without needing to explicitly model same-day round trips.

## 13. Final Code

```java
public int maxProfit(int[] prices) {
    int totalProfit = 0;

    for (int i = 1; i < prices.length; i++) {
        if (prices[i] > prices[i - 1]) {
            totalProfit += prices[i] - prices[i - 1];
        }
    }

    return totalProfit;
}
```

## 14. Self-Test

You have an array `prices` where `prices[i]` is a stock's price on day `i`. You may buy and sell as many times as you like (holding at most one share at a time). Return the maximum total profit achievable. Think about why capturing every single day-over-day price increase, independently, gives exactly the same result as any smarter-looking multi-day holding strategy.
