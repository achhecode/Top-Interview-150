---
problem: 202 - Happy Number
url: https://leetcode.com/problems/happy-number/
difficulty: Easy
section: Math / Hash Table
patterns: [cycle-detection, floyd-tortoise-hare]
data_structures: [hashset]
time: O(log n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** a positive integer `n`.

**Required:** repeatedly replace `n` with the sum of the squares of its digits; determine whether this process eventually reaches `1` (happy) or falls into an infinite loop that never includes `1` (not happy).

**Constraints that matter:**
- `1 <= n <= 2^31 - 1` — `n` fits in a 32-bit signed int, and critically, the sum of squares of digits of *any* number in this range quickly shrinks to a small bounded value (for example, the worst case, a 10-digit number of all 9s, has digit-square-sum at most `10 * 81 = 810`), so after the very first transformation, all subsequent values stay small and bounded — this is what guarantees the process either reaches 1 or enters a cycle in a small, bounded number of steps, never growing unboundedly.
- The problem statement itself tells you the two possible outcomes explicitly: reaching `1`, or looping endlessly in a cycle that excludes `1` — this is a direct, explicit statement that **cycle detection** is the mechanism needed, since without it there'd be no way to distinguish "will eventually reach 1" from "loops forever" without some way to recognize repetition.
- No input can cause the digit-sum-of-squares transformation itself to overflow `int`, since even the worst case (`810`) is tiny relative to `int`'s range.

## 2. Recognition Signals

- "Repeat a transformation until you reach a target value or detect an infinite loop" → this is the textbook signature of a **cycle detection problem**, structurally identical to detecting a cycle in a linked list, just with the "next node" being defined by a mathematical function instead of a pointer.
- The transformation (sum of squares of digits) is **deterministic** — the same input always produces the same output — which is exactly the property needed for Floyd's cycle detection (tortoise and hare) to apply, since it relies on the sequence of values behaving like a linked list where each value has exactly one "next" value.
- No explicit data structure (like an actual linked list) is given, but the sequence of transformed values forms an **implicit linked list** where "next" is a function call rather than a `.next` pointer.
- Named pattern: **Cycle Detection via Floyd's Tortoise and Hare** (or, more simply, a `HashSet` of previously seen values).

## 3. Core Idea

Treat the sequence `n, f(n), f(f(n)), ...` (where `f` computes the sum of squares of digits) as an implicit linked list. Either track every value seen so far in a `HashSet` and stop as soon as a repeat appears (meaning a cycle was entered without ever hitting 1), or use two pointers moving at different speeds (a "slow" pointer taking one step, a "fast" pointer taking two steps) — if there's a cycle, the fast pointer will eventually lap the slow pointer and they'll meet at the same value.

**Why it's correct:** since `f` is a deterministic function on a finite set of possible values below some bound, the sequence of values must either reach `1` (and then stay there forever, since `f(1) = 1`) or eventually revisit some earlier value, creating a cycle — by the pigeonhole principle, an infinite sequence of values drawn from a bounded range must repeat. Floyd's algorithm correctly detects this: if the sequence is cyclic, a pointer moving twice as fast as another will eventually be at the same position within the cycle as the slower pointer, because each step the fast pointer closes the distance-within-the-cycle gap by exactly one.

**Invariant:** at every step of the two-pointer version, if a cycle exists, the fast pointer's distance ahead of the slow pointer (measured within the cycle) decreases by exactly one per shared iteration, guaranteeing they meet within at most one full cycle length of iterations, and the meeting value is `1` if and only if `1` is part of that "cycle" (with `1` treated as its own trivial fixed-point cycle).

## 4. Approach 1 — Brute Force (HashSet of Seen Values)

1. Create a `Set<Integer> seen`.
2. Repeatedly: if `n == 1`, return `true`. If `n` is already in `seen`, a cycle has been detected without reaching `1` — return `false`. Otherwise, add `n` to `seen` and replace `n` with the sum of the squares of its digits.
3. This loop is guaranteed to terminate because the value space is bounded (as explained above), so a repeat must eventually occur if `1` is never reached.

```java
public boolean isHappyBruteForce(int n) {
    Set<Integer> seen = new HashSet<>();

    while (n != 1 && !seen.contains(n)) {
        seen.add(n);
        n = sumOfSquaredDigits(n);
    }

    return n == 1;
}

private int sumOfSquaredDigits(int n) {
    int sum = 0;
    while (n > 0) {
        int digit = n % 10;
        sum += digit * digit;
        n /= 10;
    }
    return sum;
}
```

**Time:** O(log n) — the number of distinct values the sequence can take before repeating is bounded by a small constant (since digit-square-sums stay under ~243 for 3-digit inputs and similar small bounds generally), and each digit-sum computation costs O(log n) for the number of digits in the current value; in practice this converges extremely quickly regardless of how large the initial `n` is.
**Space:** O(log n) — bounded by the number of distinct values that can appear before a repeat, which is a small constant in practice (not related to `n`'s magnitude directly, but conventionally expressed this way since the very first transformation already caps subsequent values at a small range).

This is not the fully optimal solution the follow-up spirit of this problem often asks for (the problem's classic Floyd's-cycle framing suggests an O(1)-space alternative exists), but it is entirely valid, simple, and commonly accepted — it's included here as the natural first approach before recognizing that the extra space can be eliminated entirely.

## 5. Approach 2 — Optimized (Floyd's Cycle Detection, Tortoise and Hare)

1. Initialize `slow = n` and `fast = sumOfSquaredDigits(n)` (fast starts one step ahead).
2. While `fast != 1` and `slow != fast`:
   - Advance `slow` by one transformation: `slow = sumOfSquaredDigits(slow)`.
   - Advance `fast` by two transformations: `fast = sumOfSquaredDigits(sumOfSquaredDigits(fast))`.
3. Return `fast == 1` — if the loop exited because `fast` reached `1`, `n` is happy; if it exited because `slow == fast` (a cycle was detected that doesn't include 1), it's not.

```java
public boolean isHappy(int n) {
    int slow = n;
    int fast = sumOfSquaredDigits(n);

    while (fast != 1 && slow != fast) {
        slow = sumOfSquaredDigits(slow);
        fast = sumOfSquaredDigits(sumOfSquaredDigits(fast));
    }

    return fast == 1;
}

private int sumOfSquaredDigits(int n) {
    int sum = 0;
    while (n > 0) {
        int digit = n % 10;
        sum += digit * digit;
        n /= 10;
    }
    return sum;
}
```

**Time:** O(log n) — same reasoning as the brute force: the sequence converges to a small bounded range almost immediately, and Floyd's algorithm detects the cycle (or reaches 1) within a bounded number of steps proportional to the cycle length, which is small and constant here; each step costs O(log n) for the digit computation on the current (typically small) value.
**Space:** O(1) auxiliary — only two integer variables (`slow`, `fast`) are needed, no matter how large `n` starts.

This is optimal: it achieves the same time behavior as the `HashSet` approach while eliminating all auxiliary space beyond a constant number of variables, which is the standard reason this technique is preferred whenever a deterministic "next value" function and a bounded/cyclic state space are present — the same pattern used for cycle detection in linked lists.

## 6. Dry Run

`n = 19`

Sequence of `f` applications: `19 → 82 → 68 → 100 → 1`

| step | slow | fast | fast==1? | slow==fast? |
|---|---|---|---|---|
| init | 19 | 82 (=f(19)) | no | no |
| 1 | 82 (=f(19)) | f(f(82))=f(68)=100 | no | no |
| 2 | 68 (=f(82)) | f(f(100))=f(1)=1 | **yes** | — |

Exit: loop stops because `fast == 1`. Final answer: `true`, matching the expected output.

Contrast with `n = 2`: the sequence is `2 → 4 → 16 → 37 → 58 → 89 → 145 → 42 → 20 → 4 → ...` (cycles back to 4 without ever hitting 1). Floyd's slow/fast pointers will eventually land on the same value somewhere within that cycle (not equal to 1), causing the loop to exit via `slow == fast`, and the function correctly returns `false`.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| HashSet of Seen Values | O(log n) | O(log n) aux | Space bounded by number of distinct values before a repeat (small constant in practice) |
| Floyd's Tortoise and Hare | O(log n) | O(1) aux | Same time behavior, no extra data structure needed |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| `n = 1` | Already happy, trivially | `fast = sumOfSquaredDigits(1) = 1`; loop condition `fast != 1` is immediately false, returns `true` |
| `n` is a known unhappy number that cycles (e.g. `n = 2`, `n = 4`) | Must correctly detect the cycle rather than looping forever | Floyd's pointers are guaranteed to meet within the bounded cycle, at which point `slow == fast` triggers loop exit with `fast != 1`, correctly returning `false` |
| Large `n` near `2^31 - 1` | Must not take excessively long or overflow during digit-sum computation | The very first `sumOfSquaredDigits` call collapses any large `n` down to at most a few hundred, after which all further computation is on small values; no overflow risk since max possible sum for any realistic digit count stays well within `int` |
| `n` with digit `0`s (e.g. `n = 100`) | `0^2 = 0` contributes nothing to the sum | `sumOfSquaredDigits` naturally handles this — `digit * digit` for `digit = 0` just adds `0`, no special-casing needed |
| Single-digit `n` (e.g. `n = 7`) | Small input, sequence still needs to be followed correctly | Works identically to larger inputs — `sumOfSquaredDigits` handles any positive integer uniformly via the `while (n > 0)` digit-extraction loop |

## 9. Java Notes

- `n % 10` extracts the last digit and `n /= 10` removes it — this is the standard idiomatic digit-extraction loop in Java for any base-10 digit processing task, terminating when `n` reaches `0`.
- Floyd's cycle detection (tortoise and hare) is the same technique used for linked list cycle detection (LeetCode 141); recognizing that this problem's transformation function `f` plays the role of `.next` is the key insight transferring that pattern here.
- No `long` is needed anywhere in this solution — every intermediate digit-square-sum stays comfortably within `int` range given the constraint `n <= 2^31 - 1`, since even a 10-digit number's digit-square-sum (`810` max) is tiny.
- A private helper method (`sumOfSquaredDigits`) is used to avoid duplicating the digit-extraction loop in both the slow and fast pointer advancement logic — a small but meaningful readability choice given `fast` needs the transformation applied twice per iteration.

## 10. Common Mistakes

- Forgetting to detect cycles at all and just running the transformation some fixed number of iterations (e.g., a loop bound of 1000) hoping it converges — this is a heuristic, not a proof of correctness, and could theoretically (though not in practice for realistic constraints) miss a case or waste computation on inputs that converge quickly. Fix: use proper cycle detection (HashSet or Floyd's) rather than an arbitrary iteration cap.
- Advancing `fast` by only one step instead of two in Floyd's algorithm — this degrades the technique to two pointers moving in lockstep, which will never detect a cycle since they'd always be at the same position. Fix: `fast` must always take exactly two transformation steps per iteration to two-times the speed of `slow`.
- Checking `slow == fast` before both pointers have been initialized to different starting points (e.g., initializing both to `n` and immediately comparing) — this would trivially and incorrectly report a "cycle" on the very first check. Fix: initialize `fast` one step ahead of `slow` (as `f(n)`, not `n`) before the loop begins comparing them.
- Off-by-one in the digit-extraction loop, such as using `n >= 0` instead of `n > 0` as the while condition — this would either infinite-loop (since after the last digit is removed, `n` becomes `0`, and `0 >= 0` is still true, repeatedly adding `0*0=0` forever) or otherwise misbehave. Fix: use `n > 0` so the loop terminates exactly when all digits have been consumed.

## 11. Interview Takeaway

- **Trigger sentence:** "Repeated deterministic transformation that either reaches a target or loops forever → cycle detection, either a HashSet of seen values or Floyd's tortoise-and-hare for O(1) space."
- **90-second explanation:** "I treat the sequence of transformed values as an implicit linked list, where each value's 'next' is the sum of the squares of its digits. Since that transformation always shrinks large numbers down into a small bounded range almost immediately, the sequence must either reach 1 or start repeating — there's no way for it to grow forever. To detect which outcome happens without needing extra memory, I use two pointers: a slow one that takes one step per iteration, and a fast one that takes two. If the sequence is happy, the fast pointer reaches 1 first. If it's not happy, the sequence is cyclic, and the fast pointer will eventually lap around and land on the exact same value as the slow pointer, which I can detect just by comparing them — no need to store every value I've seen."
- **Related problems:** 141 (Linked List Cycle), 142 (Linked List Cycle II), 287 (Find the Duplicate Number, another application of Floyd's on an implicit linked list).

## 12. Recall Questions

**Q:** Why must the sequence of digit-square-sums eventually either reach 1 or enter a cycle, rather than growing forever?
**A:** After the very first transformation, the digit-square-sum of any number is bounded by a small constant (since even a 10-digit number can't produce a sum larger than a few hundred), so from that point on the sequence is confined to a small finite range of possible values, and by the pigeonhole principle a value must eventually repeat.

**Q:** Why does Floyd's algorithm correctly detect a cycle using only two pointers instead of storing every visited value?
**A:** If the sequence is cyclic, the fast pointer (moving twice as fast) closes the gap to the slow pointer by exactly one position every iteration once both are inside the cycle, guaranteeing they'll land on the exact same value within at most one full cycle length of iterations — no need to remember prior values to detect this convergence.

**Q:** Why must `fast` be initialized one step ahead of `slow` before the comparison loop begins, rather than both starting at `n`?
**A:** If both pointers started at the identical value `n`, the `slow == fast` check would trivially be true from the very first comparison, incorrectly signaling a cycle before either pointer has actually moved through the sequence.

**Q:** What is the key structural similarity between this problem and detecting a cycle in a linked list?
**A:** In both cases, there's a deterministic "next" operation applied repeatedly to a current state — a `.next` pointer for a linked list, or the digit-square-sum function here — and the question of whether that sequence of states is finite (terminates) or cyclic (repeats forever) is answered the same way in both cases: Floyd's tortoise and hare.

**Q:** Why doesn't the algorithm need to worry about integer overflow despite `n` being allowed up to `2^31 - 1`?
**A:** The digit-square-sum transformation is only ever applied to the current value, and even the very first application collapses any input (regardless of how large) down to a small bounded sum (at most a few hundred for realistic digit counts), so every subsequent computation operates on small values well within `int` range.

## 13. Final Code

```java
class Solution {
    public boolean isHappy(int n) {
        int slow = n;
        int fast = sumOfSquaredDigits(n);

        while (fast != 1 && slow != fast) {
            slow = sumOfSquaredDigits(slow);
            fast = sumOfSquaredDigits(sumOfSquaredDigits(fast));
        }

        return fast == 1;
    }

    private int sumOfSquaredDigits(int n) {
        int sum = 0;
        while (n > 0) {
            int digit = n % 10;
            sum += digit * digit;
            n /= 10;
        }
        return sum;
    }
}
```

## 14. Self-Test

Given a positive integer, determine whether repeatedly replacing it with the sum of the squares of its digits eventually reaches `1`, or instead falls into an infinite non-1 cycle. Re-derive: treat the sequence of transformed values as an implicit linked list and apply Floyd's tortoise-and-hare — a slow pointer taking one transformation step, a fast pointer taking two — stopping when either the fast pointer reaches `1` (happy) or the two pointers meet at the same non-1 value (a cycle, not happy).
