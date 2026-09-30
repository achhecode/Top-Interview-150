---
problem: 155 - Min Stack
url: https://leetcode.com/problems/min-stack/
difficulty: Medium
section: Design / Stack
patterns: [stack, auxiliary-min-tracking]
data_structures: [stack]
time: O(1)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** a requirement to design a stack data structure.

**Required:** support `push(value)`, `pop()`, `top()`, and `getMin()` — retrieving the current minimum element in the stack — with **every one of these operations running in O(1) time**, not just amortized or on average.

**Constraints that matter:**
- `-2^31 <= val <= 2^31 - 1` — values fit in `int`; no overflow concerns for storing or comparing them directly.
- `pop`, `top`, and `getMin` are **guaranteed to always be called on a non-empty stack** — this removes the need for any empty-stack error handling or edge-case guards in those three methods, simplifying the implementation.
- **"You must implement a solution with O(1) time complexity for each function"** — this is the crux of the problem: a naive `getMin()` that scans the whole stack is O(n), and the entire challenge is figuring out how to track the minimum incrementally so it's always available instantly, even as elements are pushed and popped.

## 2. Recognition Signals

- "Design a stack with an extra O(1) query" (here, minimum) → the general pattern of **augmenting a stack with parallel auxiliary state** that tracks some running property (min, max, sum) alongside the actual data.
- The requirement that `getMin()` must reflect the minimum of *only the currently present elements* (correctly changing after pops) → this rules out a single global variable tracking "the minimum ever seen," since values can be popped away, requiring something that can also "un-know" a minimum when it's popped off.
- The natural fix — track not just the current minimum, but the minimum **at each depth of the stack** — is exactly what a second, parallel stack achieves: it's popped in lockstep with the main stack, and so it never loses historical minimum information for whatever remains.
- Named pattern: **Auxiliary (Parallel) Min-Tracking Stack**.

## 3. Core Idea

Maintain two stacks in parallel: the main `stack` holding the actual values, and a `minStack` where `minStack`'s top always equals the minimum of everything currently in `stack` (not the minimum ever pushed — the minimum among only what's still present). Every `push` also pushes the new running minimum onto `minStack`; every `pop` also pops `minStack`, which correctly "forgets" that minimum if the popped element was responsible for it, revealing the correct prior minimum underneath.

**Why it's correct:** at any point, `minStack`'s top is defined to be `min(value being pushed, minStack's previous top)` at the moment of that push — this means `minStack`'s top after `k` pushes always reflects the true minimum of the first `k` elements' worth of "still present" state. Because `minStack` is popped in exact lockstep with `stack`, removing the top of `stack` also removes the minimum-tracking entry that was computed *including* that now-removed element, correctly exposing what the minimum was *before* that element was pushed — which is exactly the minimum of what remains.

**Invariant:** at every point in time, `minStack.peek()` equals the minimum value among all elements currently in `stack` (i.e., `minStack` and `stack` always have the same size, and `minStack[i]` is always `min(stack[0], stack[1], ..., stack[i])` for every valid index `i`).

## 4. Approach 1 — Brute Force (Single Stack, Linear Scan for Minimum)

1. Use a single `Deque<Integer> stack` for `push`, `pop`, and `top` — these are naturally O(1) with a standard stack.
2. For `getMin()`, iterate through the entire stack and return the smallest value found.

```java
class MinStackBruteForce {
    private final Deque<Integer> stack = new ArrayDeque<>();

    public void push(int value) {
        stack.push(value);
    }

    public void pop() {
        stack.pop();
    }

    public int top() {
        return stack.peek();
    }

    public int getMin() {
        int min = Integer.MAX_VALUE;
        for (int value : stack) {
            min = Math.min(min, value);
        }
        return min;
    }
}
```

**Time:** `push`, `pop`, `top` are O(1); `getMin()` is **O(n)**, scanning the entire stack every call.
**Space:** O(n) auxiliary for the stack itself (no extra structure beyond the data).

This is not optimal: the problem explicitly requires **every** operation, including `getMin()`, to run in O(1) time. A linear scan violates this requirement outright — with up to `3 * 10^4` calls, repeatedly scanning a potentially large stack for every `getMin()` call could be significantly slower than necessary, and more importantly, it simply doesn't satisfy the stated constraint.

## 5. Approach 2 — Optimized (Parallel Auxiliary Min-Stack)

1. Maintain two stacks: `stack` (the actual values) and `minStack` (parallel running minimums), both implemented as `Deque<Integer>`.
2. `push(value)`: push `value` onto `stack`. Compute the new minimum as `Math.min(value, minStack.isEmpty() ? value : minStack.peek())`, and push that onto `minStack`.
3. `pop()`: pop both `stack` and `minStack` together — always in lockstep, so they stay the same size and correctly aligned.
4. `top()`: return `stack.peek()`.
5. `getMin()`: return `minStack.peek()` — always O(1), since it's just reading the top of the auxiliary stack, never scanning.

```java
class MinStack {
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final Deque<Integer> minStack = new ArrayDeque<>();

    public MinStack() {
    }

    public void push(int value) {
        stack.push(value);
        int currentMin = minStack.isEmpty() ? value : Math.min(value, minStack.peek());
        minStack.push(currentMin);
    }

    public void pop() {
        stack.pop();
        minStack.pop();
    }

    public int top() {
        return stack.peek();
    }

    public int getMin() {
        return minStack.peek();
    }
}
```

**Time:** O(1) for every operation — `push`, `pop`, `top`, and `getMin()` all do a fixed, constant amount of work regardless of stack size, fully satisfying the problem's explicit requirement.
**Space:** O(n) — `minStack` grows in exact lockstep with `stack`, so it doubles the space usage of a plain stack (still O(n) overall, just with roughly a 2x constant factor), which is the trade-off made to achieve O(1) `getMin()`.

This is optimal: the problem demands O(1) for every operation, and this approach delivers exactly that. The trade-off is doubling the space (an auxiliary stack the same size as the main one) in exchange for eliminating the linear-time scan — a classic and necessary space-for-time exchange given the strict O(1) requirement.

## 6. Dry Run

Operations: `push(-2)`, `push(0)`, `push(-3)`, `getMin()`, `pop()`, `top()`, `getMin()`

| operation | stack after | minStack after | return value |
|---|---|---|---|
| push(-2) | [-2] | [-2] | — |
| push(0) | [-2, 0] | [-2, -2] | — |
| push(-3) | [-2, 0, -3] | [-2, -2, -3] | — |
| getMin() | [-2, 0, -3] | [-2, -2, -3] | `-3` (minStack.peek()) |
| pop() | [-2, 0] | [-2, -2] | — (removed -3 from both stacks) |
| top() | [-2, 0] | [-2, -2] | `0` (stack.peek()) |
| getMin() | [-2, 0] | [-2, -2] | `-2` (minStack.peek(), correctly reverted after -3 was popped) |

Exit: all operations processed. Final sequence of return values: `null, null, null, -3, null, 0, -2`, exactly matching the expected output.

## 7. Complexity Summary

| Approach | Time (per op) | Space | Notes |
|---|---|---|---|
| Single Stack, Linear `getMin` | push/pop/top: O(1); getMin: O(n) | O(n) | Violates the problem's explicit O(1)-for-all requirement |
| Parallel Auxiliary Min-Stack | O(1) for all four operations | O(n) (≈2x a plain stack) | Space traded for guaranteed constant-time min retrieval |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Pushing a new global minimum | `minStack`'s top must update to reflect it | `Math.min(value, minStack.peek())` at push time always captures a new lower value correctly |
| Pushing a value equal to the current minimum (duplicate minimum) | Must not lose track of the minimum when one copy is later popped | Pushing the duplicate value again onto `minStack` (since `Math.min(value, currentMin) == value == currentMin`) preserves a "layer" for each occurrence, so popping one copy still correctly leaves the minimum intact for the other |
| Popping the current minimum off the stack | The minimum must correctly revert to whatever was the minimum *before* that element was pushed | Popping `minStack` in lockstep with `stack` naturally exposes the previous minimum, since `minStack`'s entries were computed cumulatively at each push |
| First-ever push (stack starts empty) | `minStack.peek()` would fail on an empty stack | The `minStack.isEmpty() ? value : ...` check in `push` avoids calling `peek()` on an empty `minStack` for the very first push |
| Negative numbers and values at the `±2^31` boundary | Must compare correctly without overflow | `Math.min` on two `int` values performs a direct comparison, no arithmetic combination that could overflow; works identically for negative and boundary values |
| Sequence of pushes and pops that repeatedly changes the minimum | State must stay correctly synchronized | Because `stack` and `minStack` are always modified together (same push/pop calls), they never desynchronize in size or alignment, regardless of how complex the operation sequence is |

## 9. Java Notes

- `Deque<Integer>` used via `push`/`pop`/`peek` provides standard LIFO stack semantics in modern Java; `ArrayDeque` is preferred over the legacy `java.util.Stack` class for new code (the latter is synchronized and extends the older `Vector`, adding unnecessary overhead).
- Storing `Integer` in both deques means every value is autoboxed; for up to `3 * 10^4` operations this overhead is negligible, though a manual `int[]` array-backed stack implementation could avoid it entirely if performance were critical at a much larger scale.
- `minStack.peek()` returns an `Integer`, and `Math.min(value, minStack.peek())` auto-unboxes it for the primitive comparison — safe here since the `isEmpty()` guard ensures `peek()` is never called when there's nothing to return.
- Pushing a full redundant value onto `minStack` on every single `push` call (rather than trying to be "clever" and only push when a new minimum is set) is intentional and correct — it's what keeps `minStack` synchronized in size with `stack`, which is essential for the lockstep-pop correctness argument; trying to optimize away "duplicate" pushes onto `minStack` would break this invariant.

## 10. Common Mistakes

- Only pushing onto `minStack` when the new value is a **strictly new** minimum (skipping the push when `value >= currentMin`) — this desynchronizes `minStack`'s size from `stack`'s size, and popping later would remove the wrong number of entries, corrupting the minimum tracking. Fix: always push exactly one value onto `minStack` per `push` call, keeping both stacks the same size at all times.
- Using a single global variable to track "the minimum" instead of a full parallel stack — this variable can't be "rolled back" correctly when the element that set it gets popped, since there's no memory of what the minimum was *before* that element was pushed. Fix: use a full auxiliary stack, not a single variable, specifically to support correct reversion on pop.
- Forgetting to pop `minStack` when `pop()` is called on the main stack — this leaves `minStack` out of sync (too large relative to `stack`), and its top would reflect a minimum that includes elements no longer present. Fix: every `pop()` call must pop both stacks together, unconditionally.
- Calling `minStack.peek()` during the very first `push()` without guarding against `minStack` being empty at that point — throws an exception (or returns null, causing a `NullPointerException` on unboxing) since there's nothing to compare against yet. Fix: explicitly check `minStack.isEmpty()` before referencing its top, or initialize appropriately.

## 11. Interview Takeaway

- **Trigger sentence:** "Stack requiring O(1) retrieval of a running aggregate (min/max) that changes correctly on pop → maintain a second, parallel auxiliary stack tracking that aggregate at every depth, popped in lockstep with the main stack."
- **90-second explanation:** "The challenge is that `getMin()` needs to be O(1), but the minimum can change both when elements are pushed and, trickier, when the current minimum itself gets popped off. A single variable can't handle that second case, since it has no memory of what the minimum was before. So I keep a second stack, `minStack`, running in parallel — every time I push a value onto the main stack, I also push the minimum of that value and whatever was previously on top of `minStack` onto the min stack. That means `minStack`'s top is always exactly the minimum of everything currently in the main stack. When I pop, I pop both stacks together, which naturally reveals the correct prior minimum underneath, since that entry was computed before the just-removed element was ever pushed. This costs roughly double the space of a plain stack, but gives true O(1) time for every single operation, including `getMin()`."
- **Related problems:** 232 (Implement Queue using Stacks), 155's own max-stack variant, 84 (Largest Rectangle in Histogram, another stack-augmentation-style problem), 239 (Sliding Window Maximum, same "maintain a running aggregate efficiently" spirit with a deque instead).

## 12. Recall Questions

**Q:** Why can't a single variable tracking "the current minimum" work correctly for this problem?
**A:** A single variable has no way to know what the minimum was *before* the element currently responsible for it was pushed, so when that element gets popped, there's no way to correctly recover the previous minimum — you'd need to rescan the remaining stack, which is exactly the O(n) behavior the problem disallows.

**Q:** Why must `minStack` be popped every single time `stack` is popped, even when the popped value wasn't the current minimum?
**A:** Keeping both stacks the same size at all times is what guarantees they stay correctly aligned — `minStack`'s top always corresponds to the minimum of `stack`'s current contents specifically because they were built and torn down in exact lockstep; breaking that synchronization even once corrupts all future `getMin()` results.

**Q:** Why does pushing a duplicate value (equal to the current minimum) still push a new full entry onto `minStack`, rather than being skipped as "redundant"?
**A:** Even though the minimum value itself doesn't change, the *depth* at which that minimum is valid does — if the duplicate weren't pushed onto `minStack`, popping it off `stack` later would incorrectly also pop away the minimum-tracking entry for the *other* occurrence of that same minimum value, prematurely losing track of it.

**Q:** What specific problem constraint makes the brute-force linear-scan `getMin()` unacceptable, beyond it just being "slower"?
**A:** The problem explicitly states that a solution must achieve O(1) time complexity for *each* function individually, not just good average performance — an O(n) `getMin()` fails this stated requirement outright, regardless of how large `n` actually gets in practice.

**Q:** How does the space cost of this approach compare to a plain stack without min-tracking, and why is that cost justified?
**A:** It roughly doubles the space usage, since `minStack` grows to the same size as `stack` at all times; this is justified because it's the mechanism that converts an otherwise-necessary O(n) scan into a guaranteed O(1) lookup — a direct and standard time-for-space trade-off.

## 13. Final Code

```java
import java.util.*;

class MinStack {
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final Deque<Integer> minStack = new ArrayDeque<>();

    public MinStack() {
    }

    public void push(int value) {
        stack.push(value);
        int currentMin = minStack.isEmpty() ? value : Math.min(value, minStack.peek());
        minStack.push(currentMin);
    }

    public void pop() {
        stack.pop();
        minStack.pop();
    }

    public int top() {
        return stack.peek();
    }

    public int getMin() {
        return minStack.peek();
    }
}
```

## 14. Self-Test

Design a stack supporting `push`, `pop`, `top`, and `getMin`, where every operation — including retrieving the minimum — must run in O(1) time. Re-derive: maintain a second, parallel stack alongside the main one, where each push also pushes the minimum of the new value and the previous running minimum; pop both stacks together in lockstep so the auxiliary stack's top always correctly reflects the minimum of whatever remains after any sequence of pops.
