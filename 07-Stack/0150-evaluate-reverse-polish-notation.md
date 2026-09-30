---
problem: 150 - Evaluate Reverse Polish Notation
url: https://leetcode.com/problems/evaluate-reverse-polish-notation/
difficulty: Medium
section: Stack / Math
patterns: [stack, expression-evaluation]
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

**Given:** an array of strings `tokens` representing an arithmetic expression written in Reverse Polish Notation (RPN, also called postfix notation), where operators come *after* their operands.

**Required:** evaluate the expression and return the resulting integer.

**Constraints that matter:**
- `1 <= tokens.length <= 10^4` — moderate size; performance isn't the challenge, correctly handling the postfix evaluation order and truncating division is.
- Operands are integers in `[-200, 200]`, and the problem guarantees **the answer and all intermediate calculations fit in a 32-bit integer** — this explicitly removes the need to worry about overflow at any point, even mid-computation, so plain `int` arithmetic throughout is safe.
- **"Division between two integers always truncates toward zero"** — this matters because Java's native `int` division (`/`) already truncates toward zero by default (unlike some languages' floor division for negative numbers), so no special rounding logic is needed; this is a case where the language's default behavior happens to exactly match the problem's requirement.
- The input is guaranteed to be a **valid** RPN expression — no malformed input, mismatched operators, or missing operands to defend against.

## 2. Recognition Signals

- "Reverse Polish Notation" / "postfix expression" is itself the direct textual signal for a **stack-based evaluator** — this is close to the canonical textbook use case for a stack in computer science curricula.
- Operators always apply to the two **most recently seen** operands (or sub-results) — this "most recent first" access pattern is a LIFO relationship, which a stack directly provides.
- Each token, processed left to right, is either "data to remember for later" (an operand) or "combine the two most recent things I'm remembering" (an operator) — this push/combine rhythm is the structural signature of stack-based expression evaluation.
- Named pattern: **Stack-Based Postfix Expression Evaluation**.

## 3. Core Idea

Scan the tokens left to right. Whenever a number is seen, push it onto a stack. Whenever an operator is seen, pop the two most recently pushed values (the second-to-top is the left operand, the top is the right operand, since it was pushed later), apply the operator, and push the result back onto the stack. After processing all tokens, exactly one value remains on the stack — the final answer.

**Why it's correct:** postfix notation is specifically designed so that by the time an operator token is reached, both of its operands (which may themselves be the results of earlier sub-expressions) have already been fully evaluated and are sitting at the top of the stack, in the correct left-to-right order (second-from-top = left operand, top = right operand, since the right operand was necessarily produced or pushed more recently). This ordering is guaranteed by the definition of valid RPN, so no lookahead, parentheses tracking, or operator-precedence logic is ever needed — the notation itself has already encoded the correct evaluation order.

**Invariant:** immediately before processing any token, the stack contains exactly the fully-evaluated results of every complete sub-expression seen so far, in the order those sub-expressions appear in the original notation — so an operator token always finds its two correct operands, already evaluated, at the top of the stack.

## 4. Approach 1 — Brute Force (Repeated In-Place List Reduction)

1. Copy `tokens` into a mutable `List<String>`.
2. Repeatedly scan the list to find the first operator token.
3. When found, apply it to the two elements immediately before it in the list, replace all three elements (two operands + operator) with a single element holding the computed result, and shrink the list accordingly.
4. Repeat until only one element remains; return it as an integer.

```java
public int evalRPNBruteForce(String[] tokens) {
    List<String> list = new ArrayList<>(Arrays.asList(tokens));

    while (list.size() > 1) {
        for (int i = 0; i < list.size(); i++) {
            String token = list.get(i);
            if (isOperator(token)) {
                int b = Integer.parseInt(list.get(i - 1));
                int a = Integer.parseInt(list.get(i - 2));
                int result = applyOperator(a, b, token);

                list.set(i - 2, String.valueOf(result));
                list.remove(i - 1);
                list.remove(i - 1); // indices shift after first removal
                break;
            }
        }
    }

    return Integer.parseInt(list.get(0));
}

private boolean isOperator(String token) {
    return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
}

private int applyOperator(int a, int b, String op) {
    return switch (op) {
        case "+" -> a + b;
        case "-" -> a - b;
        case "*" -> a * b;
        case "/" -> a / b;
        default -> throw new IllegalArgumentException("Invalid operator: " + op);
    };
}
```

**Time:** O(n²) — each reduction step requires an O(n) scan to find the next operator, plus O(n) list-shifting cost from `remove()` on an `ArrayList`, and there are O(n) reduction steps overall (one per operator).
**Space:** O(n) for the mutable list copy.

This is not optimal: it repeatedly re-scans the shrinking list from the start to find the next operator, and each removal shifts subsequent elements — both are wasted work that a single left-to-right pass with a stack completely avoids, since a stack processes each token exactly once and never needs to search for "what to do next."

## 5. Approach 2 — Optimized (Stack, Single Pass)

1. Create a `Deque<Integer> stack`.
2. For each token in `tokens`, in order:
   - If it's an operator (`+`, `-`, `*`, `/`), pop the top two values — `b` (top, right operand) then `a` (next, left operand) — compute `a op b`, and push the result.
   - Otherwise, it's a number: parse it and push it directly.
3. After the loop, the single remaining value on the stack is the answer — pop and return it.

```java
public int evalRPN(String[] tokens) {
    Deque<Integer> stack = new ArrayDeque<>();

    for (String token : tokens) {
        switch (token) {
            case "+" -> {
                int b = stack.pop();
                int a = stack.pop();
                stack.push(a + b);
            }
            case "-" -> {
                int b = stack.pop();
                int a = stack.pop();
                stack.push(a - b);
            }
            case "*" -> {
                int b = stack.pop();
                int a = stack.pop();
                stack.push(a * b);
            }
            case "/" -> {
                int b = stack.pop();
                int a = stack.pop();
                stack.push(a / b);
            }
            default -> stack.push(Integer.parseInt(token));
        }
    }

    return stack.pop();
}
```

**Time:** O(n) — a single pass through `tokens`, with O(1) push/pop per token and O(digits) for parsing each number (bounded by a small constant, since operands are within `[-200, 200]`).
**Space:** O(n) auxiliary in the worst case — for an expression that's mostly operands before any operators consolidate them (though in a valid RPN expression, the stack size is bounded by roughly half the token count at most, since operators reduce two values to one).

This is optimal: every token must be examined at least once to evaluate the expression, so O(n) is the best possible time, and the stack directly mirrors the structure that postfix notation already encodes — no searching, no re-scanning, no shifting of prior results.

## 6. Dry Run

`tokens = ["4","13","5","/","+"]`

| token | action | stack after |
|---|---|---|
| "4" | push 4 | [4] |
| "13" | push 13 | [4, 13] |
| "5" | push 5 | [4, 13, 5] |
| "/" | pop b=5, pop a=13, push 13/5=2 (truncated toward zero) | [4, 2] |
| "+" | pop b=2, pop a=4, push 4+2=6 | [6] |

Exit: all tokens processed; exactly one value, `6`, remains on the stack. Final answer: `6`, matching the expected output (`4 + (13 / 5) = 4 + 2 = 6`).

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Repeated In-Place List Reduction | O(n²) | O(n) aux | Re-scans and shifts the shrinking list on every reduction step |
| Stack, Single Pass | O(n) | O(n) aux | Each token processed exactly once, no re-scanning |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Single-token expression (e.g. `tokens = ["5"]`) | No operators at all; trivial evaluation | The loop pushes the single number, and the final `stack.pop()` returns it directly |
| Negative operands (e.g. `"-11"`) | Must not be confused with the `"-"` subtraction operator | `Integer.parseInt("-11")` correctly parses the full negative number; the operator-check switch only matches the exact single-character strings `"+"`, `"-"`, `"*"`, `"/"`, so a multi-character token like `"-11"` falls through to the `default` (number-parsing) branch |
| Division that truncates toward zero with negative results (e.g. `-132 / 12` truncating to `-11`, not floor-dividing to `-12`) | Different languages handle negative integer division differently | Java's native `/` operator on `int` already truncates toward zero by default, exactly matching the problem's required behavior with no extra logic needed |
| Deeply nested expressions (e.g. Example 3's expression) | Many operators consuming previously-computed sub-results | The stack naturally handles arbitrary nesting depth, since each operator's result is just pushed back for potential use by a later operator, with no limit on how many times a value can be "reused" as an intermediate |
| Operand order for non-commutative operators (subtraction, division) | Popping order must correctly preserve which operand was "left" and which was "right" in the original expression | The code always pops `b` (top, most recently pushed = right operand) before `a` (left operand), and computes `a op b`, not `b op a` — this ordering is essential for correctness |
| Operands at the boundary of the stated `[-200, 200]` range, or results approaching 32-bit limits | Problem guarantees safety, but worth confirming | The problem explicitly guarantees all intermediate and final values fit within a 32-bit integer, so plain `int` arithmetic throughout requires no additional overflow handling |

## 9. Java Notes

- Java's `int` division (`/`) truncates toward zero natively — this is a specific, deliberate design choice in the Java Language Specification (distinct from, e.g., Python's `//` which floors toward negative infinity), and it happens to exactly match what this problem requires, so no `Math.signum`-based correction or similar adjustment is needed.
- A Java 14+ `switch` expression with arrow syntax (`case "+" -> { ... }`) is used here for readability, grouping each operator's pop/compute/push logic cleanly; a traditional `switch` statement with `break` or a chain of `if`/`else if` would work identically.
- Popping `b` before `a` (right operand first, since it's on top) and then computing `a op b` — not `b op a` — is the detail most likely to cause a subtle correctness bug for non-commutative operators; it's worth deliberately verifying this order when implementing from scratch.
- `Integer.parseInt(token)` correctly handles a leading `-` character as part of a negative number's string representation, distinguishing it from the standalone `"-"` operator token via `String.equals` comparison, not by checking for the presence of a `-` character alone.

## 10. Common Mistakes

- Popping the two operands into variables named in *push* order rather than *pop* order (i.e., assuming the first `pop()` call gives the left operand) — this silently reverses the operands for subtraction and division, producing wrong answers for any non-commutative operation. Fix: always explicitly assign `b = stack.pop()` (right, popped first) then `a = stack.pop()` (left, popped second), and compute `a op b`.
- Checking for a `"-"` character anywhere in the token to decide "is this a negative number" versus "is this the subtraction operator" — this is unnecessary and risks bugs; the cleanest approach is checking for exact equality against the four single-character operator strings first, and treating anything else (including `"-11"`) as a number to parse. Fix: use exact string equality checks for the four operators, not substring/character-presence checks.
- Assuming Java's integer division needs manual truncation-toward-zero correction (as would be needed in some other languages) — this is unnecessary extra code, since Java's `/` operator on `int` operands already truncates toward zero by default. Fix: trust the language's native behavior here; no correction needed.
- Forgetting that a valid RPN expression always leaves exactly one value on the stack at the end, and instead trying to sum or otherwise combine multiple leftover stack values — if implemented correctly, there should only ever be one value left, so any logic handling "what if there's more than one" indicates an earlier bug in the push/pop logic, not a case that needs separate handling.

## 11. Interview Takeaway

- **Trigger sentence:** "Reverse Polish / postfix notation → stack-based evaluation: push operands, and on each operator pop the top two (right first, then left), compute, and push the result back."
- **90-second explanation:** "Postfix notation is specifically structured so that by the time you reach an operator, both of its operands have already appeared and been fully resolved — possibly as the results of earlier sub-expressions. So I scan left to right with a stack: numbers get pushed directly, and whenever I see an operator, I pop the top two values off the stack. The one popped first is the right operand, since it was pushed most recently, and the one popped second is the left operand. I apply the operator in that order — left op right, not the reverse — and push the result back onto the stack, since it might itself become an operand for a later operator. Java's native integer division already truncates toward zero, which conveniently matches what the problem asks for. After processing every token, exactly one value remains on the stack, and that's the final answer."
- **Related problems:** 71 (Simplify Path, similar stack-based token processing), 224 (Basic Calculator), 227 (Basic Calculator II), 772 (Basic Calculator III, infix-to-postfix-style evaluation).

## 12. Recall Questions

**Q:** Why does postfix (RPN) notation avoid the need for parentheses or operator-precedence rules during evaluation?
**A:** The order in which operators and operands appear in valid postfix notation already fully encodes the intended evaluation order — by the time an operator is reached, its operands (or their already-computed sub-results) are guaranteed to be exactly the two most recent values, so no additional structural information is needed to know what to combine or in what order.

**Q:** Why must the first popped value be treated as the *right* operand and the second popped value as the *left* operand?
**A:** The stack is LIFO, so the operand pushed most recently — which corresponds to the operand that appears later, i.e., on the right, in the original left-to-right token order — comes off first; getting this backwards silently breaks subtraction and division, which are not commutative.

**Q:** Why doesn't this solution need any special-case logic for Java's integer division truncation behavior?
**A:** Java's `/` operator on two `int` operands already truncates toward zero by default as part of the language specification, which happens to be exactly the truncation rule the problem requires, so the language's built-in behavior can simply be relied upon directly.

**Q:** How does the code correctly distinguish a negative number token like `"-11"` from the subtraction operator token `"-"`?
**A:** The operator check uses exact string equality against the single-character strings `"+"`, `"-"`, `"*"`, `"/"`; since `"-11"` is not exactly equal to `"-"`, it falls through to the default branch, where `Integer.parseInt` correctly interprets the leading minus sign as part of the numeric value.

**Q:** Why is exactly one value guaranteed to remain on the stack after processing all tokens, given a valid RPN expression?
**A:** Every operand contributes one push, and every operator consumes exactly two stack values and produces exactly one, so across a valid, fully-formed expression, the net effect of all pushes and pops is guaranteed to leave precisely one value — the fully reduced result — regardless of how deeply nested the expression is.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public int evalRPN(String[] tokens) {
        Deque<Integer> stack = new ArrayDeque<>();

        for (String token : tokens) {
            switch (token) {
                case "+" -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a + b);
                }
                case "-" -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a - b);
                }
                case "*" -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a * b);
                }
                case "/" -> {
                    int b = stack.pop();
                    int a = stack.pop();
                    stack.push(a / b);
                }
                default -> stack.push(Integer.parseInt(token));
            }
        }

        return stack.pop();
    }
}
```

## 14. Self-Test

Given an array of tokens representing a valid Reverse Polish Notation expression, evaluate it and return the result. Re-derive: scan tokens left to right with a stack, pushing numbers directly; on each operator, pop the top two values (first popped is the right operand, second is the left), apply the operator in that order, and push the result back — the single value left on the stack at the end is the answer.
