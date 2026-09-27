---
problem: 55 - Jump Game
url: https://leetcode.com/problems/jump-game/
difficulty: Medium
section: Array / Greedy
patterns: [greedy, farthest-reachable-index]
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

**Given:** An integer array `nums`, where `nums[i]` is the maximum jump length from index `i`. You start at index 0.

**Required:** Return `true` if you can reach the last index by some sequence of jumps, `false` otherwise.

**Constraints that actually matter:**
- `1 <= nums.length <= 10^4` — moderate size; rules out anything worse than roughly O(n log n), but a linear greedy scan or O(n) DP is expected and sufficient.
- `0 <= nums[i] <= 10^5` — a jump length of 0 means you're stuck at that index unless you never need to land on it; this is exactly what makes example 2 fail (landing on the 0 at index 3 traps you).
- A single-element array (`nums.length == 1`) is a valid input where you're trivially already at the last index — worth confirming this returns `true` without needing any jump at all.

## 2. Recognition Signals

- "Maximum jump length from each position" + "can you reach the end" → classic **greedy reachability** problem: track the farthest index reachable so far as you scan left to right.
- You don't need to know the *exact path* of jumps, only *whether reaching the end is possible* → this "existence" framing (not "shortest path" or "count paths") is what allows a greedy linear scan instead of DP or BFS.
- Whenever each position grants a "reach" that can be combined additively with previous reaches — track a running maximum reachable index instead of exploring every possible jump combination.

**Pattern:** Greedy — Farthest Reachable Index (a specialized single-pass technique for reachability problems where each position extends a frontier).

## 3. Core Idea

Track `farthest`, the furthest index reachable using any combination of jumps made from indices processed so far. As you scan each index `i` from left to right, first check if `i` is even reachable (`i <= farthest`) — if not, you're stuck and can never proceed further, so return `false` immediately. If it is reachable, update `farthest` to `max(farthest, i + nums[i])`, since standing at `i` (which you can indeed reach) lets you extend the frontier by up to `nums[i]` more steps. If you ever reach a point where `farthest >= nums.length - 1`, you've confirmed the last index is reachable.

**Invariant:** At the start of processing index `i`, `farthest` correctly reflects the maximum index reachable using only information from indices `0` through `i-1` — and if `i > farthest`, no sequence of jumps from earlier indices can possibly reach index `i`, meaning the goal is unreachable regardless of what any index beyond `i` might offer (since you can never even physically stand on any index beyond an unreachable gap).

## 4. Approach 1 — Brute Force (Recursion / Backtracking)

**Logic:** From the current index, recursively try every possible jump length (from 1 up to the max allowed), checking if any leads to successfully reaching the end.

```java
public boolean canJump(int[] nums) {
    return canJumpFrom(nums, 0);
}

private boolean canJumpFrom(int[] nums, int position) {
    if (position >= nums.length - 1) {
        return true;
    }

    int maxJump = Math.min(nums[position], nums.length - 1 - position);
    for (int step = 1; step <= maxJump; step++) {
        if (canJumpFrom(nums, position + step)) {
            return true;
        }
    }
    return false;
}
```

- **Time:** O(2^n) in the worst case without memoization — each position can branch into many possible next positions, and the same positions get re-explored repeatedly.
- **Space:** O(n) recursion stack depth in the worst case.

**Why it is not optimal:** Exponential blowup makes this impractical for `n` up to `10^4` — many positions get revisited redundantly across different jump paths. Memoizing on position would reduce this to O(n²) (each of n positions tries up to n jump lengths), still far worse than the linear greedy approach, and adds O(n) memoization space overhead.

## 5. Approach 2 — Optimized (Greedy, Farthest Reachable Index)

**Algorithm:**
1. Initialize `farthest = 0` (initially, you can "reach" index 0 trivially, since that's your starting position).
2. For each index `i` from `0` to `nums.length - 1`:
   - If `i > farthest`, index `i` is unreachable — return `false` immediately.
   - Update `farthest = Math.max(farthest, i + nums[i])`.
   - (Optional early exit) If `farthest >= nums.length - 1`, you can already return `true`.
3. If the loop completes without returning `false`, return `true` (every index was reachable, so implicitly the last index was too).

```java
public boolean canJump(int[] nums) {
    int farthest = 0;
    int lastIndex = nums.length - 1;

    for (int i = 0; i <= lastIndex; i++) {
        if (i > farthest) {
            return false;
        }
        farthest = Math.max(farthest, i + nums[i]);
        if (farthest >= lastIndex) {
            return true;
        }
    }
    return true;
}
```

- **Time:** O(n) — single pass, O(1) work per index.
- **Space:** O(1) auxiliary — just `farthest` and `lastIndex`.

**Why this is optimal:** Every index up to the point of failure or success must be examined at least once to know how far it can extend the reachable frontier, so O(n) is a hard lower bound. Space is O(1) because the only information needed at each step is the single running maximum reach — no need to remember the specific path taken or explore alternate jump lengths, since maximizing the reach at each step is always at least as good as any smaller jump (a larger `farthest` can never hurt future reachability).

## 6. Dry Run

Example: `nums = [3,2,1,0,4]`.
Chosen because it's the "false" example from the problem statement, directly exercising the early-return-false branch when index 3's zero jump length combined with the frontier not extending past it traps the scan.

`lastIndex = 4`. Initial: `farthest = 0`.

| i | i > farthest? | nums[i] | i + nums[i] | farthest after | farthest >= lastIndex? |
|---|-----------------|---------|---------------|-------------------|---------------------------|
| 0 | No (0 > 0 false) | 3 | 0+3=3 | max(0,3)=3 | No (3 >= 4 false) |
| 1 | No (1 > 3 false) | 2 | 1+2=3 | max(3,3)=3 | No |
| 2 | No (2 > 3 false) | 1 | 2+1=3 | max(3,3)=3 | No |
| 3 | No (3 > 3 false) | 0 | 3+0=3 | max(3,3)=3 | No |
| 4 | **Yes (4 > 3 true)** | — | — | — | return false |

Exit condition: at `i = 4`, `i > farthest` (4 > 3) triggers the early `return false`.

**Final answer:** `false`, matching the expected output exactly — the frontier gets permanently stuck at index 3 because every index from 0 to 3 all cap the reach at exactly index 3, and index 3's own jump length of 0 contributes nothing further.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Recursive backtracking | O(2^n) unmemoized, O(n²) memoized | O(n) (recursion stack / memo) | Correct but far heavier than necessary; the greedy insight avoids this entirely |
| 2. Greedy, farthest reachable index | O(n) | O(1) | Optimal; single pass with no extra structure |

Input/output space for `nums` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single-element array (`nums.length == 1`) | Already at the last index with no jump needed; should trivially return `true` | `lastIndex = 0`, and the loop's first check at `i = 0` immediately finds `farthest (0) >= lastIndex (0)`, returning `true` right away |
| A `0` at the very last index | The last index itself never needs to "jump" from, so its own jump length is irrelevant | The loop can return `true` as soon as `farthest >= lastIndex` is satisfied by an earlier index's reach, without ever needing to evaluate `nums[lastIndex]` itself |
| A `0` at some index strictly before the last, but the frontier already extends past it | A zero-jump index doesn't have to be fatal if earlier indices already jumped far enough to skip over it entirely | Since `farthest` is a running maximum built from all earlier indices, landing "on" index `i` conceptually and finding `nums[i] = 0` only stalls further extension from that specific index — if `farthest` was already pushed beyond it by an earlier index, the zero has no negative effect |
| A `0` at some index that traps all reach exactly at that index (as in the dry run above) | This is the actual failure case: reach is capped and can never extend past the trap | The `i > farthest` check at the very next index correctly detects that no reachable index exists past the trap, returning `false` |
| All jump lengths are large enough to reach the end from index 0 directly | Should short-circuit almost immediately | The early-exit check `farthest >= lastIndex` triggers right after processing index 0, avoiding unnecessary further iterations |
| `nums[i] = 0` for every index except possibly enabling trivial single-element success | Every index other than a single-element array would be stuck immediately | For `n > 1`, `farthest` never grows past 0, so the loop's `i > farthest` check fails as soon as `i = 1`, correctly returning `false` |

## 9. Java Notes

- **`Math.max` for the running frontier:** a simple, allocation-free way to track the best-so-far reach without manual `if` comparisons.
- **Early-exit optimization (`farthest >= lastIndex`):** not strictly necessary for correctness (the loop would still terminate correctly and return `true` if it ran to completion), but avoids unnecessary iterations once success is already guaranteed — a nice practical touch, though optional to mention as an enhancement rather than a requirement.
- **No overflow risk:** `i + nums[i]` in the worst case is at most `(10^4 - 1) + 10^5`, comfortably within `int` range, so no `long` arithmetic is needed.
- **Placement of the `i > farthest` check before updating `farthest`:** this order matters — you must confirm `i` is actually reachable *before* trusting `nums[i]` to extend the frontier, since if `i` itself is unreachable, its jump length is meaningless (you'd never actually be standing there to use it).

## 10. Common Mistakes

- **Updating `farthest` before checking whether `i` is reachable.** This would incorrectly use the jump length of an index you can never actually stand on, potentially producing a false "reachable" result. Fix: always check `i > farthest` first, and only then use `nums[i]` to extend `farthest`.
- **Using recursion/backtracking without memoization for this problem given its input size.** This leads to exponential time and risks a time limit exceeded verdict for larger inputs. Fix: recognize the "can you reach the end" (existence, not path enumeration) framing as a strong hint toward the greedy farthest-reach technique.
- **Confusing this problem with "Jump Game II" (LC 45), which asks for the *minimum number* of jumps rather than just reachability.** Applying a minimum-jumps counting strategy here (or vice versa) solves a subtly different question and can produce wrong or overcomplicated logic. Fix: note explicitly whether the problem asks "can you reach the end" (this one, `true`/`false`) or "what's the fewest jumps" (LC 45, an integer count) before choosing an approach.
- **Forgetting that reaching an index doesn't require using the exact jump length available there — any jump *up to* the maximum is allowed.** Some solutions mistakenly assume you must always jump the maximum distance, which isn't required; only the *reach* (i.e., `i + nums[i]` as an upper bound) matters, not a forced maximal jump. Fix: think of `nums[i]` as defining an upper bound on how far you can extend the frontier from `i`, not a mandatory jump length.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Can you reach the last index given a max-jump-length at each position → track the farthest reachable index greedily in one pass; if you ever encounter an unreachable index, return false."
- **90-second explanation:** "I track a single value, `farthest`, representing the furthest index reachable using information from all indices processed so far. As I scan left to right, before trusting any index's jump length, I first check if that index itself is even reachable — if the current index exceeds `farthest`, there's no way to have ever stood there, so I return false immediately. Otherwise, I extend `farthest` using that index's jump length, taking the maximum with what I already had. If at any point `farthest` reaches or exceeds the last index, I can return true immediately, since the goal is now guaranteed reachable. This runs in O(n) time and O(1) space because I never need to explore or remember specific jump paths — only the single best possible reach matters at each step, since a larger frontier can never be worse than a smaller one."
- **Related problems using this pattern:**
  - LeetCode 45 — Jump Game II
  - LeetCode 1306 — Jump Game III
  - LeetCode 1345 — Jump Game IV
  - LeetCode 134 — Gas Station

## 12. Recall Questions

**Q:** Why is it correct to check `i > farthest` *before* using `nums[i]` to update the frontier, rather than after?
**A:** If `i` itself is not yet reachable (i.e., exceeds the current frontier), you could never actually be standing at index `i` to make use of its jump length, so trusting it first would be logically invalid.

**Q:** Why does greedily maximizing the reachable frontier at each step never lead to a worse outcome than considering smaller jumps?
**A:** A larger `farthest` value always includes every index that a smaller jump could have reached, plus potentially more, so it can never close off any future possibility that a smaller jump would have kept open.

**Q:** If index `i` has `nums[i] = 0`, does that always mean the array is unreachable beyond that point?
**A:** No — only if the running frontier `farthest` was exactly equal to `i` at that moment with no earlier index having already pushed the reach further; if an earlier index's jump already extended past `i`, the zero at `i` has no effect on overall reachability.

**Q:** How does this problem differ from "Jump Game II," and why does that difference change the required approach?
**A:** This problem only asks whether reaching the end is *possible* (`true`/`false`), which a single greedy frontier-tracking pass answers directly; "Jump Game II" asks for the *minimum number of jumps*, which requires additionally tracking jump-count boundaries as the frontier expands, not just whether the end is reachable at all.

**Q:** Why doesn't this algorithm need to track or reconstruct the actual sequence of jumps taken?
**A:** The problem only asks for a yes/no answer about reachability, not the path itself, so tracking only the furthest reachable index at each step provides all the information needed to answer correctly, without needing to remember how that reach was achieved.

## 13. Final Code

```java
public boolean canJump(int[] nums) {
    int farthest = 0;
    int lastIndex = nums.length - 1;

    for (int i = 0; i <= lastIndex; i++) {
        if (i > farthest) {
            return false;
        }
        farthest = Math.max(farthest, i + nums[i]);
        if (farthest >= lastIndex) {
            return true;
        }
    }
    return true;
}
```

## 14. Self-Test

You have an array `nums` where `nums[i]` is the max jump length from index `i`, starting at index 0. Return whether you can reach the last index. Think about why tracking only the single farthest index reachable so far, and checking whether each new index is still within that reach before trusting its jump length, is enough to answer correctly in one linear pass.
