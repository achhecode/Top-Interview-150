---
problem: 224 - Basic Calculator
url: https://leetcode.com/problems/basic-calculator/
difficulty: Hard
section: Stack / Math
patterns: [stack, sign-tracking, expression-evaluation]
data_structures: [stack]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** a string `s` representing a valid arithmetic expression containing non-negative integers, `+`, `-`, parentheses `(` `)`, and spaces.

**Required:** evaluate the expression and return the result, without using any built-in expression-evaluating function like `eval()`.

**Constraints that matter:**
- `1 <= s.length <= 3 * 10^5` — large enough that anything beyond O(n) risks being too slow; a single-pass approach is the expected target.
- Only `+`, `-`, `(`, `)`, digits, and spaces appear — no `*` or `/`, which significantly simplifies the problem: **without multiplication/division, operator precedence is a non-issue** (`+` and `-` have equal precedence and are simply applied left to right), so the only real complexity comes from handling parentheses and unary minus correctly.
- `'-'` can be unary (e.g., `"-1"`, `"-(2 + 3)"` are valid), but `'+'` **cannot** be unary — this asymmetry must be handled correctly: a `-` immediately before a number or `(` (with no operand before it, or right after another `(`) means negate what follows, not subtract from something.
- "Every number and running calculation will fit in a signed 32-bit integer" — removes the need for `long` arithmetic or overflow guards anywhere in the solution.

## 2. Recognition Signals

- "Evaluate an expression with parentheses, no built-in eval" → this is the classic **stack-based expression evaluator with parenthesis handling**, a step up in complexity from flat postfix evaluation (LeetCode 150) because here the input is still in its natural infix form, parentheses included.
- Parentheses that can be **nested arbitrarily deep** → whenever you need to "pause" the current computation, remember it, dive into a sub-computation, and then resume exactly where you left off — that "remember and resume" behavior is the hallmark of needing a **stack** to save state across nesting levels.
- No operator precedence to worry about (only `+`/`-`) → the entire expression, ignoring parentheses, could be evaluated with a simple running total and a "current sign" that flips on `-`; parentheses just mean that sign can be temporarily inherited/multiplied at deeper nesting.
- Named pattern: **Stack-Based Sign and Result Tracking for Parenthesized Infix Expressions**.

## 3. Core Idea

Maintain a running `result`, the `sign` that applies to the *next* number encountered, and a stack that saves `(result, sign)` pairs whenever a `(` is opened — allowing that state to be restored (and combined) once the matching `)` closes. When a digit sequence is read, apply the current sign and add it to `result`. When `(` is seen, push the current `result` and `sign` onto the stack, then reset them to start fresh for the sub-expression inside the parentheses. When `)` is seen, the sub-expression just finished is fully evaluated in `result`; combine it back into the outer expression's `result` and `sign` by popping the stack.

**Why it's correct:** since only `+` and `-` exist, an expression's value is exactly the sum of a series of signed terms; the "sign in effect" for a given term is the product of all the `+`/`-` signs (and any enclosing implicit signs from unary minus before parentheses) that apply to it going into that point. Pushing `(result, sign)` before entering a `(` freezes the outer expression's progress and the sign that should apply to the *entire* parenthesized sub-expression once it's evaluated; popping on `)` correctly resumes the outer computation, multiplying the just-finished sub-expression's value by the sign that was in effect right before the `(` was opened (this is what correctly propagates a `-` in front of a parenthesized group to every term inside it).

**Invariant:** at any point outside a set of parentheses, `result` holds the correctly-signed sum of everything evaluated so far at the current nesting level, and the stack holds, from bottom to top, the `(result, sign)` snapshots of every enclosing level still waiting to be resumed — so popping the stack always correctly resumes exactly one level up.

## 4. Approach 1 — Brute Force (Recursive Evaluation via Substring Extraction)

1. Scan the string for a `(`. For each one found, find its **matching** `)` by counting nested-parenthesis depth as you scan forward.
2. Extract the substring strictly between the matched `(` and `)`, recursively evaluate it, and replace the entire `(...)` substring (including the parentheses) with the string form of that evaluated result.
3. Repeat until no parentheses remain, then evaluate the flat `+`/`-` expression left over with a simple left-to-right scan.

```java
public int calculateBruteForce(String s) {
    while (s.contains("(")) {
        int openIndex = s.indexOf('(');
        int depth = 0;
        int closeIndex = -1;

        for (int i = openIndex; i < s.length(); i++) {
            if (s.charAt(i) == '(') depth++;
            if (s.charAt(i) == ')') depth--;
            if (depth == 0) {
                closeIndex = i;
                break;
            }
        }

        String inner = s.substring(openIndex + 1, closeIndex);
        int innerValue = calculateBruteForce(inner); // recursive re-evaluation
        s = s.substring(0, openIndex) + innerValue + s.substring(closeIndex + 1);
    }

    return evaluateFlatExpression(s);
}

private int evaluateFlatExpression(String s) {
    int result = 0;
    int sign = 1;
    int num = 0;
    boolean hasNum = false;

    for (int i = 0; i <= s.length(); i++) {
        char c = i < s.length() ? s.charAt(i) : '\0';
        if (Character.isDigit(c)) {
            num = num * 10 + (c - '0');
            hasNum = true;
        } else if (c == '+' || c == '-' || c == '\0') {
            if (hasNum) {
                result += sign * num;
            }
            sign = (c == '-') ? -1 : 1;
            num = 0;
            hasNum = false;
        }
        // spaces are simply skipped (no branch needed, they fall through)
    }

    return result;
}
```

**Time:** O(n²) in the worst case — repeatedly searching for `(`, scanning to find its matching `)`, and rebuilding the string via concatenation (`s.substring(...) + ... + s.substring(...)`) each cost up to O(n), and this can repeat up to O(n) times for deeply nested or numerous parenthesized groups.
**Space:** O(n) for the string rebuilding at each step, plus O(depth) recursion stack space.

This is not optimal: with `n` up to `3 * 10^5`, O(n²) string rebuilding is far too slow. It also makes the logic harder to reason about, since it interleaves string mutation with re-evaluation rather than processing the expression in one clean pass.

## 5. Approach 2 — Optimized (Single-Pass Stack of Sign/Result Snapshots)

1. Initialize `result = 0`, `sign = 1`, and an empty `Deque<Integer> stack`.
2. Scan `s` character by character:
   - If it's a digit, accumulate it into the current number (`num = num * 10 + (c - '0')`) — numbers can be multiple digits, so this must continue across consecutive digit characters before being applied.
   - If it's `+` or `-` (or the end of the string, or a `)` about to be processed): apply the pending number (if any) to `result` using the current `sign`, then update `sign` to `1` or `-1` accordingly (only for `+`/`-`), and reset `num` to `0`.
   - If it's `(`: push the current `result` and `sign` onto the stack (in that order, or packed together), then reset `result = 0` and `sign = 1` to start evaluating the sub-expression fresh.
   - If it's `)`: first apply any pending number to `result` (same as above), then pop the saved outer `sign` and `result` from the stack, and update `result = poppedResult + poppedSign * result` (the just-finished sub-expression's value, scaled by the sign that applied to it, added into the resumed outer result).
   - Spaces are skipped entirely.
3. After the scan, apply any final pending number, and return `result`.

```java
public int calculate(String s) {
    Deque<Integer> stack = new ArrayDeque<>();
    int result = 0;
    int sign = 1;
    int num = 0;

    for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);

        if (Character.isDigit(c)) {
            num = num * 10 + (c - '0');
        } else if (c == '+' || c == '-') {
            result += sign * num;
            num = 0;
            sign = (c == '-') ? -1 : 1;
        } else if (c == '(') {
            stack.push(result);
            stack.push(sign);
            result = 0;
            sign = 1;
        } else if (c == ')') {
            result += sign * num;
            num = 0;
            int savedSign = stack.pop();
            int savedResult = stack.pop();
            result = savedResult + savedSign * result;
        }
        // spaces: no matching branch, simply skipped
    }

    result += sign * num; // apply any trailing number
    return result;
}
```

**Time:** O(n) — a single pass through the string, with O(1) work per character (digit accumulation, sign updates, and O(1) amortized stack push/pop).
**Space:** O(n) auxiliary in the worst case — the stack grows by two entries per `(`, so a deeply nested expression (up to O(n) opening parentheses) could push up to O(n) stack entries.

This is optimal: every character of the input must be examined at least once, so O(n) is the best possible time, and the stack is the minimal structure needed to correctly save and resume state across arbitrarily deep nesting — no simpler structure (like a single saved value) could handle more than one level of nesting correctly.

## 6. Dry Run

`s = "(1+(4+5+2)-3)+(6+8)"`

| i (char) | action | result | sign | num | stack (top last) |
|---|---|---|---|---|---|
| '(' | push (0,1); reset | 0 | 1 | 0 | [0,1] |
| '1' | accumulate | 0 | 1 | 1 | [0,1] |
| '+' | result+=1*1=1; sign=1 | 1 | 1 | 0 | [0,1] |
| '(' | push (1,1); reset | 0 | 1 | 0 | [0,1,1,1] |
| '4' | accumulate | 0 | 1 | 4 | [0,1,1,1] |
| '+' | result+=4 | 4 | 1 | 0 | [0,1,1,1] |
| '5' | accumulate | 4 | 1 | 5 | [0,1,1,1] |
| '+' | result+=5=9 | 9 | 1 | 0 | [0,1,1,1] |
| '2' | accumulate | 9 | 1 | 2 | [0,1,1,1] |
| ')' | result+=2=11; pop sign=1,result=1; result=1+1*11=12 | 12 | (restored) | 0 | [0,1] |
| '-' | (no pending num) sign=-1 | 12 | -1 | 0 | [0,1] |
| '3' | accumulate | 12 | -1 | 3 | [0,1] |
| ')' | result+=(-1*3)=9; pop sign=1,result=0; result=0+1*9=9 | 9 | (restored) | 0 | [] |
| '+' | sign=1 | 9 | 1 | 0 | [] |
| '(' | push (9,1); reset | 0 | 1 | 0 | [9,1] |
| '6' | accumulate | 0 | 1 | 6 | [9,1] |
| '+' | result+=6 | 6 | 1 | 0 | [9,1] |
| '8' | accumulate | 6 | 1 | 8 | [9,1] |
| ')' | result+=8=14; pop sign=1,result=9; result=9+1*14=23 | 23 | (restored) | 0 | [] |

Exit: end of string reached; trailing `result += sign * num` adds `1*0=0` (no-op). Final answer: `23`, matching the expected output.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Recursive Substring Extraction | O(n²) worst case | O(n) per rebuild + O(depth) recursion | Repeated string search, matching, and rebuilding |
| Single-Pass Stack (Sign/Result Snapshots) | O(n) | O(n) worst case (deep nesting) | Each character processed exactly once |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Leading unary minus (e.g. `"-1+2"` or `"-(2+3)"`) | `-` with no preceding operand must be treated as negating what follows, not subtracting from something | `sign` starts at `1` and `result` at `0`; encountering `-` before any number just sets `sign = -1` for the upcoming term, which correctly negates it since `result` is still `0` |
| Multi-digit numbers (e.g. `"100"`) | Must accumulate digits across multiple characters before applying | `num = num * 10 + (c - '0')` correctly builds up multi-digit values as consecutive digit characters are scanned |
| Spaces anywhere in the expression | Must be completely ignored, not treated as separators requiring special handling | The character-type checks (digit, `+`/`-`, `(`, `)`) simply don't match a space, so it falls through with no branch executing — effectively a no-op |
| Nested parentheses with a `-` immediately before, negating the whole group (e.g. `"-(2+3)"`) | The sign must apply to the entire sub-expression's result, not just its first term | Pushing `(result, sign)` before entering `(`, then combining as `savedResult + savedSign * result` on `)`, correctly multiplies the *entire* evaluated sub-expression by whatever sign preceded the parenthesis |
| Deeply nested parentheses (many levels) | Must correctly resume each outer level in the right order | The stack's LIFO nature ensures each `)` resumes exactly the most recently paused (innermost still-open) outer context, matching the correct nesting structure |
| Expression ending right after a number with no trailing operator (e.g. `"1+1"`) | The last number's contribution must still be added to `result` | The `result += sign * num;` line after the loop ends applies any number that was accumulated but never triggered by a following `+`/`-`/`)` |

## 9. Java Notes

- `Character.isDigit(c)` is used rather than a manual range check (`c >= '0' && c <= '9'`) — functionally equivalent for ASCII digits, and either is acceptable; `isDigit` is slightly more expressive of intent.
- Pushing `result` then `sign` (in that order) onto the `Deque` means they must be popped in the reverse order (`sign` first, then `result`) — getting this order backwards silently swaps which value is treated as which, corrupting every subsequent calculation involving that nesting level.
- No `long` is needed anywhere, per the problem's explicit guarantee that all intermediate and final results fit within a signed 32-bit integer — this simplifies the arithmetic throughout to plain `int`.
- The trailing `result += sign * num;` after the loop is easy to forget since most of the "apply the pending number" logic lives inside the loop triggered by `+`/`-`/`)` — but the very last number in the expression has no such trailing character to trigger it, so this final flush is a required, deliberate step, not an optional cleanup.

## 10. Common Mistakes

- Forgetting the final `result += sign * num;` after the loop ends — this silently drops the very last number in the expression if it isn't followed by any operator or closing parenthesis (e.g., `"1+1"` would incorrectly evaluate to `1` instead of `2`, missing the trailing `1`). Fix: always flush the pending number once after the scan completes.
- Popping `stack.pop()` in the same order they were pushed (result first, then sign) instead of the reverse — since a stack is LIFO, the last-pushed value (`sign`) must be popped first. Fix: always pop in exactly reverse order of pushing, or pack both values into a single object/array to avoid order confusion entirely.
- Treating `+` as capable of being unary (e.g., trying to handle `"+1"` as a valid negation-free prefix) — the problem explicitly states `+` is never used as a unary operator, so no special-casing for a leading or post-`(` `+` is needed; only `-` requires this care. Fix: don't add unnecessary handling for a case the problem guarantees won't occur.
- On encountering `(`, forgetting to reset `result` and `sign` to their starting values (`0` and `1`) before evaluating the sub-expression — without this reset, the sub-expression's evaluation would incorrectly start from the outer expression's partial result instead of from zero. Fix: always reset both `result` and `sign` immediately after pushing the outer state onto the stack.

## 11. Interview Takeaway

- **Trigger sentence:** "Evaluate an infix `+`/`-` expression with parentheses, no operator precedence to worry about → single-pass scan with a running result and sign, pushing (result, sign) snapshots onto a stack on `(` and combining them back in on `)`."
- **90-second explanation:** "Since there's no multiplication or division, I don't need to worry about operator precedence — I can just track a running result and the sign that applies to the next number, updating them as I scan left to right. The only complexity is parentheses: whenever I hit an open paren, I need to pause my current progress and start fresh for the sub-expression inside, then resume afterward. So I push my current result and sign onto a stack before resetting them to zero and one. When I hit the closing paren, the sub-expression is fully evaluated in `result`, so I pop the saved outer sign and result, and combine them as `savedResult + savedSign * result` — multiplying the entire sub-expression's value by whatever sign was in front of the parenthesis, which correctly handles cases like a minus sign negating an entire parenthesized group. I do this in one linear pass with no need to search for matching parentheses or rebuild the string."
- **Related problems:** 227 (Basic Calculator II, adds `*`/`/` and precedence), 772 (Basic Calculator III, combines both), 150 (Evaluate Reverse Polish Notation, simpler postfix version), 71 (Simplify Path, related stack-based token processing).

## 12. Recall Questions

**Q:** Why doesn't this problem need to handle operator precedence the way a full calculator would?
**A:** The expression only contains `+` and `-`, which have equal precedence and are naturally evaluated left to right as a running sum of signed terms, so there's no need to determine which operator "binds tighter" — parentheses are the only structural complexity that needs explicit handling.

**Q:** Why must both `result` and `sign` be pushed onto the stack when encountering `(`, rather than just one of them?
**A:** Resuming the outer expression correctly after the parenthesized sub-expression finishes requires knowing both what the outer result was *before* the parenthesis (to add the sub-expression's value into) and what sign was in effect right before the parenthesis (to correctly scale the entire sub-expression's contribution, which matters especially for a `-` preceding the group).

**Q:** Why is `savedResult + savedSign * result` the correct way to combine an inner sub-expression's value back into the outer expression on `)`?
**A:** The sub-expression's fully evaluated value (`result`) needs to be scaled by whatever sign applied to the entire parenthesized group (`savedSign`) before being added onto the outer expression's progress up to that point (`savedResult`) — this correctly propagates a leading `-` in front of a parenthesis to the group's total contribution, not just its first term.

**Q:** Why is the trailing `result += sign * num;` after the main loop necessary, even though similar logic already exists inside the loop?
**A:** The in-loop logic only applies a pending number when it encounters a triggering character (`+`, `-`, or `)`), but the very last number in the expression has no such character following it before the string simply ends, so without an explicit final flush, that last number would never get added to the result.

**Q:** Why is a stack specifically necessary here instead of just a single pair of saved variables?
**A:** Parentheses can nest arbitrarily deeply, and each level of nesting needs its own independently saved `(result, sign)` snapshot to resume correctly — a single pair of variables could only support one level of nesting and would be overwritten (losing information) as soon as a second `(` was encountered before the first was closed.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public int calculate(String s) {
        Deque<Integer> stack = new ArrayDeque<>();
        int result = 0;
        int sign = 1;
        int num = 0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (Character.isDigit(c)) {
                num = num * 10 + (c - '0');
            } else if (c == '+' || c == '-') {
                result += sign * num;
                num = 0;
                sign = (c == '-') ? -1 : 1;
            } else if (c == '(') {
                stack.push(result);
                stack.push(sign);
                result = 0;
                sign = 1;
            } else if (c == ')') {
                result += sign * num;
                num = 0;
                int savedSign = stack.pop();
                int savedResult = stack.pop();
                result = savedResult + savedSign * result;
            }
        }

        result += sign * num;
        return result;
    }
}
```

## 14. Self-Test

Given a string representing a valid `+`/`-`/parentheses arithmetic expression, evaluate it without using `eval()`. Re-derive: scan left to right tracking a running result, a current sign, and the accumulating current number; on `(`, push the current result and sign onto a stack and reset both to start the sub-expression fresh; on `)`, apply any pending number, then pop the saved sign and result and combine as `savedResult + savedSign * result` to resume the outer expression correctly scaled.
