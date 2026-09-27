---
problem: 134 - Gas Station
url: https://leetcode.com/problems/gas-station/
difficulty: Medium
section: Array / Greedy
patterns: [greedy, total-and-running-deficit]
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

**Given:** `n` gas stations arranged in a circle. `gas[i]` is the fuel available at station `i`; `cost[i]` is the fuel needed to travel from station `i` to station `i+1`. You start with an empty tank at some station.

**Required:** Return the index of the starting station that lets you complete the full circular route, or `-1` if no such station exists. **The answer is guaranteed unique if one exists.**

**Constraints that actually matter:**
- `1 <= n <= 10^5` — large enough that an O(n²) brute force (trying every start and simulating the full loop) is too slow in the worst case; O(n) is expected.
- `0 <= gas[i], cost[i] <= 10^4` — non-negative values; no need to worry about negative fuel or negative cost.
- **The route is circular** (`(i + 1)th` wraps around) — this is central to the problem's structure and is what makes a simple "scan once, find the max" approach insufficient; you have to reason about the whole loop.
- **A solution, if it exists, is guaranteed unique** — this is a strong hint that once you find *a* candidate starting point that never goes negative under a specific greedy construction, you don't need to also verify it's the "best" among multiple valid candidates — there's only ever at most one.

## 2. Recognition Signals

- "Circular route" + "return the starting point that lets you complete the loop, or -1 if impossible" → this exact framing (circular tank/resource feasibility) is the signature of the **total-surplus-plus-running-deficit greedy** technique.
- A **necessary condition** jumps out immediately: if `sum(gas) < sum(cost)` overall, it's impossible from *any* starting point, since the total fuel available can never cover the total cost of the full loop — this "check the total first" instinct generalizes to many circular feasibility problems.
- Whenever a starting point search over a circular array can be reduced to "track a running balance, and restart your candidate starting point the moment the balance goes negative" — this is the tell for the linear greedy scan, instead of trying every start explicitly.

**Pattern:** Greedy — Total Surplus Check + Running Deficit Reset (a specialized technique for circular feasibility/starting-point problems).

## 3. Core Idea

Two key facts combine to make this solvable in one pass:

1. **Global feasibility check:** If `sum(gas) < sum(cost)` across the entire circuit, it's impossible to complete the loop starting anywhere, since the total fuel can never cover the total cost regardless of starting point. If `sum(gas) >= sum(cost)`, a valid starting point is guaranteed to exist (this is the nontrivial mathematical fact underlying the greedy correctness).

2. **Greedy starting-point selection:** Scan the stations left to right, maintaining a running "tank" balance (`gas[i] - cost[i]` accumulated). Whenever this running balance goes negative at some station `i`, it means **no station from the current candidate start up through `i` could have been a valid starting point** — if you couldn't make it *to* `i` with a nonnegative tank, starting even later within that failed stretch only means less accumulated fuel, making it strictly worse, not better. So the next candidate start is reset to `i + 1`, and the deficit is folded into the global total (tracked separately) rather than carried forward locally.

**Invariant:** At any point during the scan, if the running local balance since the current candidate start has never gone negative, that candidate start remains a viable prefix; the moment it goes negative, every station from the previous candidate start through the current index is provably invalid as a starting point, justifying the reset.

## 4. Approach 1 — Brute Force

**Logic:** Try every possible starting station; for each one, simulate the full circular trip, checking if the tank ever goes negative.

```java
public int canCompleteCircuit(int[] gas, int[] cost) {
    int n = gas.length;

    for (int start = 0; start < n; start++) {
        int tank = 0;
        boolean success = true;

        for (int step = 0; step < n; step++) {
            int station = (start + step) % n;
            tank += gas[station] - cost[station];
            if (tank < 0) {
                success = false;
                break;
            }
        }

        if (success) {
            return start;
        }
    }

    return -1;
}
```

- **Time:** O(n²) — for each of `n` possible starting points, an O(n) simulation of the full loop is performed in the worst case.
- **Space:** O(1) auxiliary — just a running `tank` variable per simulation.

**Why it is not optimal:** With `n` up to `10^5`, O(n²) risks up to ~10^10 operations in the worst case, far too slow. It also doesn't exploit the mathematical insight that failed starting points within a "deficit stretch" can be skipped entirely rather than individually retried.

## 5. Approach 2 — Optimized (Greedy, Total + Running Deficit)

**Algorithm:**
1. Initialize `totalTank = 0` (tracks whether the whole circuit is feasible at all), `currentTank = 0` (tracks the running balance since the current candidate start), and `start = 0` (the current candidate starting station).
2. For each station `i` from `0` to `n - 1`:
   - Compute `diff = gas[i] - cost[i]`.
   - Add `diff` to both `totalTank` and `currentTank`.
   - If `currentTank < 0`, the current candidate `start` (and every station up through `i`) is invalid: reset `start = i + 1` and `currentTank = 0`.
3. After the loop, if `totalTank < 0`, the entire circuit is infeasible from any starting point — return `-1`. Otherwise, return `start` (guaranteed to be the valid, unique starting index).

```java
public int canCompleteCircuit(int[] gas, int[] cost) {
    int totalTank = 0;
    int currentTank = 0;
    int start = 0;

    for (int i = 0; i < gas.length; i++) {
        int diff = gas[i] - cost[i];
        totalTank += diff;
        currentTank += diff;

        if (currentTank < 0) {
            start = i + 1;
            currentTank = 0;
        }
    }

    return totalTank >= 0 ? start : -1;
}
```

- **Time:** O(n) — single pass, O(1) work per station.
- **Space:** O(1) auxiliary — just three running variables.

**Why this is optimal:** Every station must be examined at least once to determine its contribution to feasibility, so O(n) is a hard lower bound. Space is O(1) because the algorithm never needs to remember more than the current running deficit and the global total — no need to simulate the entire loop from multiple starting points explicitly. This is a case where the greedy reset rule is provably correct (not just a heuristic): any station within a failed stretch is guaranteed invalid as a start, so skipping straight to `i + 1` after a failure never risks missing a valid answer, and combined with the global feasibility check, this reaches the true unique answer (or correctly reports `-1`) in one pass — no better time or space complexity is achievable.

## 6. Dry Run

Example: `gas = [1,2,3,4,5]`, `cost = [3,4,5,1,2]`.
Chosen because it's the canonical "success" example, and it shows multiple deficit resets before landing on the true valid starting point.

Initial: `totalTank = 0`, `currentTank = 0`, `start = 0`.

| i | gas[i] | cost[i] | diff | totalTank after | currentTank after | currentTank < 0? | Action |
|---|--------|---------|------|--------------------|------------------------|------------------------|--------|
| 0 | 1 | 3 | -2 | -2 | -2 | Yes | start=1, currentTank=0 |
| 1 | 2 | 4 | -2 | -4 | -2 | Yes | start=2, currentTank=0 |
| 2 | 3 | 5 | -2 | -6 | -2 | Yes | start=3, currentTank=0 |
| 3 | 4 | 1 | 3 | -3 | 3 | No | — |
| 4 | 5 | 2 | 3 | 0 | 6 | No | — |

Exit condition: loop completes after processing all 5 stations.

**Final answer:** `totalTank = 0 >= 0`, so the circuit is feasible; return `start = 3`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Brute force, try every start | O(n²) | O(1) | Correct but far too slow for n up to 10^5 |
| 2. Greedy, total + running deficit reset | O(n) | O(1) | Optimal; single pass exploits the guaranteed uniqueness and the deficit-skip insight |

Input/output space for `gas`/`cost` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| No valid starting point exists (`sum(gas) < sum(cost)`) | Must correctly report `-1`, not a stale or incorrect `start` value | The final check `totalTank >= 0 ? start : -1` correctly overrides whatever `start` value the scan settled on if the global total is negative, since local resets alone can't detect a globally infeasible circuit |
| Single station (`n == 1`) | Trivial case: feasible if and only if `gas[0] >= cost[0]` | With one station, `diff = gas[0] - cost[0]`; if non-negative, `currentTank` never goes negative and `start` stays 0, correctly returned since `totalTank` also equals `diff >= 0`; if negative, both `currentTank < 0` triggers a reset to `start = 1` (out of bounds conceptually, but irrelevant) and `totalTank < 0` correctly forces a `-1` return regardless |
| The valid starting point is the very last station (`n - 1`) | The reset mechanic must correctly carry the candidate start all the way to the end without prematurely finalizing on an earlier wrong index | Since resets only happen when `currentTank` actually goes negative, and the problem guarantees a unique valid answer exists somewhere, the scan naturally arrives at the correct final `start` value regardless of where in the array it lies |
| Every station has `gas[i] == cost[i]` | Every diff is 0; the tank never goes negative, but also never grows | `currentTank` stays exactly 0 throughout (never triggering a reset), so `start` remains 0, and `totalTank` also stays exactly 0, satisfying `totalTank >= 0`, correctly returning station 0 as a valid (barely) starting point |
| Values at the boundary (`gas[i] = 0`, `cost[i] = 10^4`, or vice versa) | Could tempt overflow concerns with large sums accumulated over many stations | With `n` up to `10^5` and per-station values up to `10^4`, the maximum possible magnitude of `totalTank` is around `10^5 * 10^4 = 10^9`, which fits within `int` range (`~2.1 * 10^9`) with some margin, so no overflow occurs, though it's worth explicitly noting this bound was checked rather than assumed |
| Multiple stretches of negative running balance before the final valid start | Must correctly reset the candidate start multiple times without getting confused about which reset is "final" | Each reset is independent and local — the algorithm doesn't need to remember previous failed candidates, since each reset to `i + 1` is justified purely by the fact that the immediately preceding stretch failed, regardless of how many such stretches occurred earlier in the scan |

## 9. Java Notes

- **No overflow risk (with the given constraints):** as analyzed above, `totalTank`'s maximum possible magnitude (~10^9) comfortably fits within `int` range, so no `long` arithmetic is needed here, though it's a detail worth explicitly verifying rather than assuming, especially since sums accumulated over many iterations are a classic overflow risk area.
- **Combining the global feasibility check and the local greedy scan into a single pass:** rather than computing `sum(gas)` and `sum(cost)` in one loop and then running a separate greedy scan in another, both `totalTank` and `currentTank` are updated together in the same loop, avoiding a second full pass over the array.
- **The final ternary `totalTank >= 0 ? start : -1`:** a concise way to apply the global feasibility gate only at the very end, after the local greedy scan has already determined its best candidate `start` — this ordering matters, since the local scan alone (without the global check) could produce a `start` value even when the circuit is genuinely infeasible.

## 10. Common Mistakes

- **Skipping the global feasibility check (`totalTank >= 0`) and just returning whatever `start` the local greedy scan lands on.** The local scan's reset logic alone doesn't detect a case where the entire circuit is infeasible (total cost exceeds total gas) — it would still produce some `start` value even when no valid answer exists. Fix: always check `totalTank >= 0` at the end and return `-1` if it fails, regardless of what `start` ended up being.
- **Resetting `start` to `i` instead of `i + 1` upon a negative `currentTank`.** Station `i` itself was part of the failed stretch (its inclusion is exactly what caused the balance to go negative), so it can't be the next candidate start either — the next viable candidate must be the station immediately after the failure point. Fix: always reset to `i + 1`, not `i`.
- **Trying to verify the found `start` by re-simulating the full loop afterward.** This is unnecessary extra work (and would reintroduce O(n) or worse overhead) given that the problem guarantees uniqueness and the greedy algorithm's correctness is mathematically guaranteed once `totalTank >= 0` — no verification pass is needed. Fix: trust the greedy result once the global feasibility check passes.
- **Confusing this problem with a simple "find the station with the most gas" or "find the station with the smallest deficit" heuristic.** Neither of those simpler heuristics is provably correct for this problem; the specific running-deficit-reset logic is what's mathematically justified, not just picking an extremal single station. Fix: rely on the running balance and reset rule, not a single-station heuristic.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Circular route feasibility, find the unique valid starting point (or -1) → check total gas vs total cost first, then scan once tracking a running local balance, resetting the candidate start whenever that balance goes negative."
- **90-second explanation:** "First, I check whether the total gas across all stations is at least the total cost — if not, it's impossible to complete the loop from anywhere, so I can return -1 immediately (checked at the end, but conceptually first). Otherwise, I scan through the stations once, maintaining a running local balance since my current candidate starting station. Whenever that local balance dips negative, it proves that no station from my current candidate through the station that caused the dip could have worked as a starting point — including any of them would only accumulate that same deficit — so I reset my candidate to the very next station and reset the local balance to zero. By the guarantee that a solution, if one exists, is unique, the candidate that survives to the end of the scan, combined with confirming the global total is non-negative, is guaranteed to be the correct answer. This runs in O(n) time and O(1) space, since I never need to explicitly simulate multiple full loop attempts."
- **Related problems using this pattern:**
  - LeetCode 135 — Candy (different greedy pattern, but similarly a single-pass greedy with local resets/adjustments)
  - LeetCode 55 — Jump Game (different greedy pattern, but similarly a single-pass feasibility check)
  - LeetCode 45 — Jump Game II (related greedy family)

## 12. Recall Questions

**Q:** Why does a negative running local balance at station `i` prove that every station from the current candidate start through `i` is an invalid starting point, not just station `i` itself?
**A:** If starting from the current candidate already leads to a negative balance by station `i`, then starting from any later station within that same stretch would have even less accumulated fuel by the time it reached station `i` (since it skips whatever surplus, if any, came before it), making it strictly no better, so none of those intermediate stations could work either.

**Q:** Why is checking `sum(gas) >= sum(cost)` a necessary first condition, and why isn't it sufficient on its own to identify the correct starting index?
**A:** It's necessary because the total fuel must cover the total cost for completing the loop to be possible from any starting point at all, but it's not sufficient by itself to identify *which* specific station is the valid start — that requires the local running-balance scan with resets to pinpoint the actual index.

**Q:** What would go wrong if, upon detecting a negative running balance at station `i`, you reset the candidate start to `i` instead of `i + 1`?
**A:** Station `i` itself was part of the failed stretch and directly contributed to causing the deficit, so it cannot be a valid starting point either — resetting to `i` instead of `i + 1` would immediately reintroduce the same failure on the very next iteration or produce an incorrect final answer.

**Q:** Why doesn't this algorithm need to separately verify the final candidate `start` by simulating the entire loop from that station?
**A:** The problem guarantees that a valid solution, if it exists, is unique, and the combination of the global total-feasibility check with the mathematically justified local reset logic is provably sufficient to identify that unique correct starting point without needing an additional verification pass.

**Q:** How does this problem's circular structure specifically complicate a naive "just scan once and find the best start" approach, and how does the algorithm work around that?
**A:** A naive single scan without the reset mechanic and global check would only capture information about a linear left-to-right traversal, not account for the wraparound nature of the route; the algorithm works around this by relying on the mathematical property that if a valid start exists, the greedy reset process will correctly converge on it without needing to explicitly simulate the wraparound.

## 13. Final Code

```java
public int canCompleteCircuit(int[] gas, int[] cost) {
    int totalTank = 0;
    int currentTank = 0;
    int start = 0;

    for (int i = 0; i < gas.length; i++) {
        int diff = gas[i] - cost[i];
        totalTank += diff;
        currentTank += diff;

        if (currentTank < 0) {
            start = i + 1;
            currentTank = 0;
        }
    }

    return totalTank >= 0 ? start : -1;
}
```

## 14. Self-Test

You have `n` gas stations arranged in a circle, with `gas[i]` fuel available and `cost[i]` fuel needed to reach the next station. Starting with an empty tank, return the unique starting station index that lets you complete the full loop, or -1 if none exists. Think about why a running local balance going negative proves every station in that failed stretch is an invalid start, and why checking the global total gas versus total cost is a separate, necessary condition.
