---
problem: 45 - Jump Game II
url: https://leetcode.com/problems/jump-game-ii/
difficulty: Medium
section: Array / Greedy
patterns: [greedy, bfs-style-level-expansion]
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

**Given:** A 0-indexed array `nums`, where `nums[i]` is the maximum forward jump length from index `i`. You start at index 0.

**Required:** Return the **minimum number of jumps** needed to reach index `n - 1`. It's guaranteed to be reachable, so no need to handle an "impossible" case (unlike LC 55, which asks a yes/no reachability question).

**Constraints that actually matter:**
- `1 <= nums.length <= 10^4` — moderate size; rules out anything worse than roughly O(n log n), and a linear greedy scan is expected.
- `0 <= nums[i] <= 1000` — jump lengths are non-negative and bounded; a value of 0 simply means no forward progress from that index, but since reachability is guaranteed, this never creates an unreachable trap in valid test cases.
- **Reachability is guaranteed** — this removes the need for any "can we even get there" check (that's LC 55's job); this problem is purely about minimizing jump count once success is already assured.
- `n = 1` (single element) means you're already at the last index with **zero jumps needed** — an important boundary case to get right explicitly.

## 2. Recognition Signals

- "Minimum number of jumps/steps to reach the end" + "each position has a maximum reach" → classic **greedy level-by-level expansion**, conceptually identical to BFS on an implicit graph where each "level" is one jump.
- Distinguishing from LC 55 (Jump Game): that problem asks "can you reach the end" (`true`/`false`); this one asks "what's the fewest jumps" (an integer count) — the *counting* framing is what requires tracking jump boundaries, not just a single running frontier.
- Whenever you need the minimum number of "hops" to cover a range where each position grants a variable extension — track the current jump's boundary and the farthest reach discovered within it, incrementing the jump count each time you must move to the next boundary.

**Pattern:** Greedy BFS-style Level Expansion (each "jump" is treated like one BFS level: explore everything reachable within the current jump before committing to the next one).

## 3. Core Idea

Think of this as BFS where each "level" corresponds to one jump. Within the current jump's range (`[currentJumpEnd's previous boundary + 1, currentJumpEnd]`), scan every index and track the farthest index reachable using one more jump from any position in this range. Once you've scanned the entire current range, if you haven't yet reached the last index, you must take another jump — increment the jump count and set the new range boundary to the farthest reach discovered. This greedily ensures each jump extends as far as possible, which is provably optimal for minimizing jump count (never take a smaller jump than necessary, since a farther reach can only help, never hurt, future jumps).

**Invariant:** After fully processing all indices within jump number `k`'s range, `farthest` holds the maximum index reachable using exactly `k` jumps from the start — so the moment `farthest >= n - 1`, exactly `k` jumps (or fewer, if it happened mid-range and we return early) suffice to reach the end.

## 4. Approach 1 — Brute Force (Recursion / Backtracking)

**Logic:** From the current index, recursively try every possible jump length, taking the minimum count over all paths that reach the end.

```java
public int jump(int[] nums) {
    return jumpFrom(nums, 0);
}

private int jumpFrom(int[] nums, int position) {
    if (position >= nums.length - 1) {
        return 0;
    }

    int minJumps = Integer.MAX_VALUE;
    int maxReach = Math.min(position + nums[position], nums.length - 1);
    for (int next = position + 1; next <= maxReach; next++) {
        minJumps = Math.min(minJumps, 1 + jumpFrom(nums, next));
    }
    return minJumps;
}
```

- **Time:** O(2^n) in the worst case without memoization — many overlapping subpaths get re-explored.
- **Space:** O(n) recursion stack depth in the worst case.

**Why it is not optimal:** Exponential blowup makes this impractical for `n` up to `10^4`. Even memoizing on position reduces it to roughly O(n²) (each of n positions tries up to n next-positions), still far worse than the O(n) greedy approach, and requires O(n) auxiliary memoization space.

## 5. Approach 2 — Optimized (Greedy BFS-style Level Expansion)

**Algorithm:**
1. If `nums.length <= 1`, return 0 immediately (already at the last index, no jump needed).
2. Initialize `jumps = 0`, `currentEnd = 0` (the boundary of the range reachable with the current jump count), and `farthest = 0` (the farthest reach discovered while scanning the current range).
3. For each index `i` from `0` to `nums.length - 2` (no need to jump *from* the last index):
   - Update `farthest = Math.max(farthest, i + nums[i])`.
   - If `i == currentEnd` (you've finished scanning the entire current jump's range), increment `jumps`, and set `currentEnd = farthest` (committing to the best boundary found, starting the next jump's range).
4. Return `jumps`.

```java
public int jump(int[] nums) {
    int n = nums.length;
    if (n <= 1) {
        return 0;
    }

    int jumps = 0;
    int currentEnd = 0;
    int farthest = 0;

    for (int i = 0; i < n - 1; i++) {
        farthest = Math.max(farthest, i + nums[i]);
        if (i == currentEnd) {
            jumps++;
            currentEnd = farthest;
        }
    }

    return jumps;
}
```

- **Time:** O(n) — single pass, O(1) work per index.
- **Space:** O(1) auxiliary — just `jumps`, `currentEnd`, and `farthest`.

**Why this is optimal:** Every index must be examined at least once to know how far it can extend the reachable range, so O(n) is a hard lower bound. Space is O(1) because only the current range's farthest extension needs to be tracked — no need to remember specific paths or explore alternate jump choices, since the BFS-level framing guarantees that always extending to the farthest possible boundary within each "level" is never worse than a more conservative choice. This is a provably optimal greedy strategy, matching the O(n) time / O(1) space profile with no trade-off left to make.

## 6. Dry Run

Example: `nums = [2,3,1,1,4]`.
Chosen because it's the canonical example, and it clearly shows a jump-boundary transition (finishing the first jump's range and committing to the second) partway through the scan.

`n = 5`. Initial: `jumps = 0`, `currentEnd = 0`, `farthest = 0`.

| i | nums[i] | i + nums[i] | farthest after | i == currentEnd? | Action | jumps after | currentEnd after |
|---|---------|---------------|-------------------|----------------------|--------|----------------|----------------------|
| 0 | 2 | 0+2=2 | max(0,2)=2 | Yes (0==0) | jumps++, currentEnd=farthest | 1 | 2 |
| 1 | 3 | 1+3=4 | max(2,4)=4 | No (1==2 false) | — | 1 | 2 |
| 2 | 1 | 2+1=3 | max(4,3)=4 | Yes (2==2) | jumps++, currentEnd=farthest | 2 | 4 |
| 3 | 1 | 3+1=4 | max(4,4)=4 | No (3==4 false) | — | 2 | 4 |

Exit condition: loop runs `i` from `0` to `n-2 = 3`, so it stops after `i = 3` (index 4, the last index, is never processed as a jump-from position, which is correct since you don't need to jump further once there).

**Final answer:** `jumps = 2`, matching the expected output exactly — jump 1 step from index 0 to 1 (using the range extended by index 0), then 3 steps from index 1 to index 4 (using the range extended by index 1), reaching the last index in exactly 2 jumps.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Recursive backtracking | O(2^n) unmemoized, O(n²) memoized | O(n) (recursion stack / memo) | Correct but unnecessarily heavy; the greedy BFS-level insight avoids all of this |
| 2. Greedy BFS-style level expansion | O(n) | O(1) | Optimal; single pass, no extra structure |

Input/output space for `nums` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single-element array (`n == 1`) | Already at the last index; zero jumps needed, and the main loop (`i < n - 1`) would never execute meaningfully anyway | Explicit early return `if (n <= 1) return 0;` handles this directly and unambiguously |
| Two-element array (`n == 2`), e.g. `[1,0]` | Exactly one jump is always needed (guaranteed reachable per constraints) | The loop runs only for `i = 0`; since `i == currentEnd` (0==0) is true, `jumps` increments to 1 and `currentEnd` updates — correctly returning 1 |
| A `0` at some index within the interior, but reachability is still guaranteed by the problem | A zero-jump index contributes nothing to extending `farthest`, but as long as the overall path is guaranteed reachable, `farthest` will already have been pushed past it by an earlier index in the same or an earlier jump range | Since `farthest` is a running maximum and the problem guarantees reachability, a zero at any single index simply fails to add to `farthest` on its own turn, but doesn't break the algorithm as long as *some* earlier index provided enough reach — which the guarantee ensures |
| The last index is reached exactly at the boundary of a jump range (i.e., `farthest` first covers `n-1` right as `i == currentEnd`) | Must correctly count this as using that jump, not require an extra unnecessary one | Since the loop only runs up to `i < n - 1`, and `currentEnd` is updated to `farthest` as soon as the current range is exhausted, the jump count is incremented at the correct moment without any extra iterations needed beyond that boundary |
| All jump lengths large enough to reach the end from index 0 in a single jump | Should return exactly 1 | At `i = 0`, `i == currentEnd` (0==0) triggers `jumps++` (becomes 1) and `currentEnd` becomes `farthest`, which already covers `n-1`; the loop then simply finishes without further increments since no other `i` will equal the new, larger `currentEnd` before the loop ends |
| Jump lengths exactly enough to require moving one index at a time (e.g., `[1,1,1,1,1]`) | Should return `n - 1` (one jump per step) | Each index's own `nums[i] = 1` only extends `farthest` by exactly one beyond `i`, causing `i == currentEnd` to trigger on every single iteration, incrementing `jumps` once per index — correctly totaling `n - 1` jumps |

## 9. Java Notes

- **`Math.max` for the running frontier:** simple, allocation-free tracking of the best-so-far reach within the current jump's range.
- **No overflow risk:** `i + nums[i]` in the worst case is at most `(10^4 - 1) + 1000`, comfortably within `int` range, so no `long` arithmetic is needed.
- **Loop bound `i < n - 1` (not `i < n`):** deliberately excludes the last index from being a "jump-from" position, since once you've reached it, no further jump is needed — including it would be harmless (its own `nums[n-1]` value wouldn't matter) but is logically unnecessary and slightly wasteful.
- **Order of operations inside the loop:** update `farthest` first, *then* check `i == currentEnd` — this ensures the current index's own contribution is folded into `farthest` before potentially committing to a new jump boundary based on it.

## 10. Common Mistakes

- **Checking `i == currentEnd` before updating `farthest`.** This would fail to include the current index's own jump length when deciding the new `currentEnd`, potentially producing a suboptimal (too small) new boundary. Fix: always update `farthest` first, then check the boundary condition.
- **Using `nums.length` instead of `nums.length - 1` as the loop's upper bound.** Processing the last index unnecessarily as a "jump-from" position is at best harmless overhead and at worst introduces confusing off-by-one reasoning about whether an extra jump gets erroneously counted. Fix: loop only while `i < n - 1`, since reaching the last index means you're done.
- **Confusing this problem with "Jump Game" (LC 55), which only asks for reachability (`true`/`false`), not a minimum count.** Applying LC 55's simpler single-frontier tracking here would tell you *whether* you can reach the end, but not *how many jumps* it takes — a fundamentally different question requiring the jump-boundary/level-counting mechanism shown here. Fix: note whether the problem wants a boolean or a count before choosing an approach.
- **Attempting a full BFS with an explicit queue of indices.** While conceptually related (this greedy approach *is* essentially BFS), explicitly enqueuing and dequeuing every index adds unnecessary O(n) space and overhead compared to the O(1)-space greedy boundary-tracking version, which achieves the same level-by-level guarantee implicitly. Fix: recognize that the greedy "farthest within current range, increment on boundary" trick captures BFS-level semantics without any queue.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Minimum number of jumps to reach the end, given a max-jump-length at each position → greedy BFS-style level expansion: track the current jump's range boundary and the farthest reach found within it, incrementing the jump count each time you exhaust the current range."
- **90-second explanation:** "I treat each jump like a level in a BFS. I keep track of `currentEnd`, the farthest index reachable using the jumps taken so far, and `farthest`, the best reach discovered while scanning within that current range. As I scan left to right, I keep updating `farthest` using each index's jump length. The moment I reach the boundary of my current jump's range (`i == currentEnd`), that means I've explored everything possible with the current jump count, so I commit to taking one more jump, incrementing my jump counter and extending my range to `farthest`. This greedy approach is optimal because always extending to the farthest possible boundary within each jump can never be worse than a smaller, more conservative jump — a farther reach only opens up more options for the next jump, never fewer. This runs in O(n) time and O(1) space, since reachability is guaranteed and I never need to explore alternate jump choices explicitly."
- **Related problems using this pattern:**
  - LeetCode 55 — Jump Game
  - LeetCode 1306 — Jump Game III
  - LeetCode 1345 — Jump Game IV
  - LeetCode 134 — Gas Station

## 12. Recall Questions

**Q:** Why is this problem's greedy solution structured like BFS "levels," with each jump treated as one level?
**A:** Each jump can reach a whole range of indices at once, and minimizing the number of jumps is equivalent to minimizing the number of BFS levels needed to reach the target — exploring everything reachable within the current level before committing to the next mirrors exactly how BFS guarantees shortest-path-in-unweighted-steps behavior.

**Q:** Why must `farthest` be updated *before* checking whether `i == currentEnd`, rather than after?
**A:** The current index's own jump length needs to be folded into the farthest-reach calculation before deciding the new jump boundary, since otherwise you'd commit to a boundary that doesn't yet account for what the current index itself contributes.

**Q:** How does this problem differ from "Jump Game" (LC 55), and why does that difference require a different algorithm?
**A:** LC 55 only asks whether reaching the end is possible at all, answerable with a single running frontier and an early-failure check; this problem asks for the minimum jump *count*, which requires additionally tracking when one jump's range ends and the next begins, incrementing a counter at each such boundary.

**Q:** Why does the main loop stop at `i < n - 1` instead of running through the very last index?
**A:** Once you've reached the last index, no further jump is needed, so there's no reason to evaluate a "jump from" the last index — doing so would be logically unnecessary since the goal has already been achieved by that point.

**Q:** If every element in `nums` (except possibly the last) has a jump length of exactly 1, how many total jumps does the algorithm return, and why?
**A:** It returns `n - 1`, because each index can only extend the reach by exactly one position, so `i == currentEnd` triggers on every single iteration, incrementing the jump count once per index processed.

## 13. Final Code

```java
public int jump(int[] nums) {
    int n = nums.length;
    if (n <= 1) {
        return 0;
    }

    int jumps = 0;
    int currentEnd = 0;
    int farthest = 0;

    for (int i = 0; i < n - 1; i++) {
        farthest = Math.max(farthest, i + nums[i]);
        if (i == currentEnd) {
            jumps++;
            currentEnd = farthest;
        }
    }

    return jumps;
}
```

## 14. Self-Test

You have an array `nums` where `nums[i]` is the max forward jump length from index `i`, starting at index 0, and reaching the last index is guaranteed. Return the minimum number of jumps needed. Think about why treating each jump as a BFS "level" — tracking the current level's boundary and the farthest reach found within it — lets you count the minimum jumps in a single linear pass.
