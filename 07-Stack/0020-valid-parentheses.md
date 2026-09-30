---
problem: 20 - Valid Parentheses
url: https://leetcode.com/problems/valid-parentheses/
difficulty: Easy
section: String / Stack
patterns: [stack, matching-pairs]
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

**Given:** a string `s` containing only the characters `(`, `)`, `{`, `}`, `[`, `]`.

**Required:** determine whether the string is valid, meaning every opening bracket is closed by a matching closing bracket of the same type, and brackets are closed in the correct nested order (no crossing pairs).

**Constraints that matter:**
- `1 <= s.length <= 10^4` — moderate size; while an O(n²) repeated-removal approach would technically still pass within limits, it's a clear signal that the cleaner O(n) approach is what the problem is testing for.
- `s` consists of bracket characters only — no letters, digits, or other symbols to filter out or ignore, simplifying the character-handling logic (every character is either an opener or a closer).
- Odd-length strings can never be valid (each pair contributes exactly 2 characters) — a cheap, immediate rejection that doesn't require examining any actual bracket content.

## 2. Recognition Signals

- "Open brackets must be closed by the same type" + "closed in the correct order" → the phrase "correct order" for nested pairs is the single strongest signal for a **stack** — the most recently opened bracket must be the next one closed, which is exactly Last-In-First-Out behavior.
- Matching pairs problems (brackets, tags, nested structures) almost always reduce to: push openers, and on a closer, check whether it matches what's currently on top of the stack.
- No need to look ahead or backtrack — a single left-to-right scan with a stack suffices, since validity only depends on the current character and whatever is most recently unmatched so far.
- Named pattern: **Stack-Based Matching Pairs**.

## 3. Core Idea

Scan the string left to right. Whenever an opening bracket is seen, push it onto a stack (it's now "waiting" to be closed). Whenever a closing bracket is seen, it must match whatever opening bracket is currently on top of the stack — if it does, pop that opener off (its pair is now satisfied); if it doesn't (wrong type, or nothing on the stack to match), the string is invalid immediately.

**Why it's correct:** a stack naturally enforces "most recently opened, first closed" ordering, which is precisely the definition of correctly nested brackets. Any closing bracket must correspond to the innermost still-open bracket — if it corresponded to any earlier (outer) one instead, that would mean brackets crossed rather than nested, which the problem explicitly disallows. At the end, the stack must be completely empty — any leftover opener means it was never closed.

**Invariant:** at any point during the scan, the stack (read from bottom to top) exactly represents the sequence of currently-unclosed opening brackets, in the order they were opened — the top of the stack is always the bracket that the very next closing bracket encountered must match.

## 4. Approach 1 — Brute Force (Repeated Removal)

1. Repeatedly scan the string for any adjacent matching pair (`"()"`, `"[]"`, or `"{}"`) and remove it, shrinking the string.
2. Keep repeating until no more adjacent matching pairs can be found.
3. The string is valid if and only if it has been reduced to empty.

```java
public boolean isValidBruteForce(String s) {
    StringBuilder sb = new StringBuilder(s);
    boolean changed = true;

    while (changed) {
        changed = false;
        int index = sb.indexOf("()");
        if (index == -1) index = sb.indexOf("[]");
        if (index == -1) index = sb.indexOf("{}");

        if (index != -1) {
            sb.delete(index, index + 2);
            changed = true;
        }
    }

    return sb.length() == 0;
}
```

**Time:** O(n²) in the worst case — each removal pass can cost O(n) to search and O(n) to shrink the `StringBuilder`, and up to O(n) removal passes may be needed (removing just one pair per pass in the worst arrangement).
**Space:** O(n) auxiliary for the mutable `StringBuilder` copy.

This is not optimal: it repeatedly re-scans large portions of the string that a single stack-based pass would handle in one go. It's also considerably more complex to reason about correctness for, despite arriving at the same answer.

## 5. Approach 2 — Optimized (Stack, Single Pass)

1. If `s.length()` is odd, return `false` immediately (optional fast-path optimization; the main logic would still correctly reject it otherwise).
2. Create an empty `Deque<Character> stack` (used as a stack via `push`/`pop`/`peek`).
3. For each character `c` in `s`:
   - If `c` is an opening bracket (`(`, `[`, `{`), push it onto the stack.
   - Otherwise (`c` is a closing bracket): if the stack is empty, or the top of the stack isn't the matching opener for `c`, return `false`. Otherwise, pop the stack.
4. After the scan, return `true` if and only if the stack is empty (every opener was matched and closed).

```java
public boolean isValid(String s) {
    if (s.length() % 2 != 0) {
        return false;
    }

    Deque<Character> stack = new ArrayDeque<>();

    for (char c : s.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') {
            stack.push(c);
        } else {
            if (stack.isEmpty()) {
                return false;
            }
            char top = stack.pop();
            if ((c == ')' && top != '(') ||
                (c == ']' && top != '[') ||
                (c == '}' && top != '{')) {
                return false;
            }
        }
    }

    return stack.isEmpty();
}
```

**Time:** O(n) — a single left-to-right pass through the string, with O(1) push/pop/peek operations per character.
**Space:** O(n) auxiliary in the worst case — a string of all opening brackets (e.g. `"((((("`) pushes every character onto the stack before any closing bracket is seen.

This is optimal: every character must be examined at least once, so O(n) is the best possible time, and the stack is the minimal structure needed to track nesting order — no simpler data structure (like a single counter) can distinguish between different bracket *types*, only balance a single type.

## 6. Dry Run

`s = "([)]"` (the classic crossing-order invalid case)

| i (char) | is opener? | action | stack after | early return? |
|---|---|---|---|---|
| 0 ('(') | yes | push '(' | ['('] | no |
| 1 ('[') | yes | push '[' | ['(', '['] | no |
| 2 (')') | no | pop top '[' ; expected '(' for ')' but top is '[' | ['('] (popped before check) | **mismatch → return false** |

Exit: the mismatch is detected at index 2 — the closing `)` expects the top of the stack to be `(`, but the top was `[` (pushed more recently). Final answer: `false`, matching the expected output.

Contrast with `s = "([])"` (valid nested case): `(` pushed, `[` pushed, `]` correctly pops `[`, `)` correctly pops `(`; stack ends empty → `true`.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Repeated Removal | O(n²) worst case | O(n) aux | Re-scans the shrinking string repeatedly |
| Stack, Single Pass | O(n) | O(n) aux (worst case: all openers) | Standard and minimal structure for nested matching |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Odd-length string | Can never have all brackets paired | Optional early check returns `false`; even without it, the stack would end non-empty or a mismatch would occur |
| String of only closing brackets (e.g. `"))"`)  | Stack is empty when a closer is encountered | `stack.isEmpty()` check catches this and returns `false` before attempting to pop |
| String of only opening brackets (e.g. `"((("`) | No closers ever appear to pop anything | Loop completes with a non-empty stack; final `stack.isEmpty()` check returns `false` |
| Correct types but wrong order (e.g. `"([)]"`) | Naive "does this type of bracket appear somewhere" logic would wrongly accept this | The top-of-stack check enforces strict nesting order, correctly rejecting crossed pairs |
| Deeply nested valid brackets (e.g. `"((([[[{{{}}}]]])))"`)  | Must correctly track many levels of nesting | Stack naturally handles arbitrary depth, bounded only by string length |
| Single bracket character (e.g. `"("` alone) | Minimal odd-length input | Caught by the odd-length check (or, without it, ends with a non-empty stack) |

## 9. Java Notes

- `Deque<Character>` used via `push`/`pop`/`peek` is the modern idiomatic Java stack — `java.util.Stack` still exists but is a legacy class (extends `Vector`, is synchronized, and is generally discouraged in new code); `ArrayDeque` is the recommended stack implementation.
- `stack.push(c)` on an `ArrayDeque` adds to the head (front), and `pop()` removes from the head — this matches standard LIFO stack semantics, distinct from `offer`/`poll` which treat the deque as a FIFO queue.
- Comparing `char` primitives directly with `==`/`!=` (as in `top != '('`) is always safe and correct in Java, unlike comparing boxed `Character` objects, which can hit the Integer/Character caching pitfall — here `top` is unboxed automatically from the `Character` popped off the stack, in the primitive `char` comparison context.
- `s.toCharArray()` allocates a new array copy of the string; for a single O(n) pass like this, the allocation cost is negligible and the resulting cleaner iteration syntax (`for (char c : ...)`) is generally preferred over manual `charAt(i)` indexing unless performance profiling suggests otherwise.

## 10. Common Mistakes

- Using a single integer counter (incrementing on any opener, decrementing on any closer) instead of a stack — this only works for a single bracket type; with multiple types, it fails to catch wrong-type mismatches or crossed-order cases like `"([)]"`, since it can't distinguish which type of bracket is unmatched. Fix: use a stack to track the specific sequence and type of unmatched openers.
- Forgetting to check `stack.isEmpty()` before popping when a closing bracket is encountered — throws a `NoSuchElementException` (or similar) on inputs like `")"` where a closer appears with nothing on the stack. Fix: always guard the pop with an emptiness check first.
- Forgetting the final `stack.isEmpty()` check after the loop — a string like `"((("` would otherwise incorrectly return `true` just because no mismatch was ever detected during the scan, ignoring the leftover unmatched openers. Fix: validity requires both no mismatches *and* a fully empty stack at the end.
- Pushing the closing bracket's *expected match* instead of the opener itself (e.g., pushing `')'` when seeing `'('`) and then comparing directly against the current closing character — this works too and is a valid alternative implementation, but mixing up the push/comparison direction inconsistently (e.g., pushing openers but comparing against openers when a closer is seen, without translating) is a common source of subtle bugs. Fix: be consistent — either push openers and translate for comparison, or push expected closers and compare directly.

## 11. Interview Takeaway

- **Trigger sentence:** "Matching pairs that must close in the correct nested order → stack: push openers, and on each closer, check and pop against the top of the stack."
- **90-second explanation:** "I scan the string once, pushing every opening bracket onto a stack as I encounter it, since it represents an unmatched bracket waiting to be closed. When I hit a closing bracket, it must match whatever's currently on top of the stack — that's the most recently opened, still-unclosed bracket, and correct nesting requires closing the innermost one first. If the stack is empty when I see a closer, or the types don't match, the string is immediately invalid. At the very end, I also need the stack to be completely empty — any bracket left on it means it was opened but never closed. This all happens in one linear pass, and the stack is essential because a simple counter couldn't distinguish between different bracket types or catch crossed, out-of-order pairs."
- **Related problems:** 22 (Generate Parentheses), 32 (Longest Valid Parentheses), 921 (Minimum Add to Make Parentheses Valid), 1249 (Minimum Remove to Make Valid Parentheses).

## 12. Recall Questions

**Q:** Why is a stack specifically necessary here, rather than a simple counter of open vs. closed brackets?
**A:** A counter can only track balance for a single bracket type and can't detect wrong-type mismatches or crossed ordering (like `"([)]"`), since it has no memory of *which* brackets are currently open or in what order — a stack preserves both the type and the nesting order of unmatched openers.

**Q:** Why must the final check include `stack.isEmpty()` rather than just checking that no mismatch occurred during the scan?
**A:** A string of only opening brackets never triggers a mismatch during the scan (there's nothing to compare against), but it clearly isn't valid since those openers were never closed — the empty-stack check at the end is what catches this leftover-opener case.

**Q:** Why does checking the top of the stack (rather than, say, checking if the matching opener exists *anywhere* in the stack) enforce correct nesting order?
**A:** Correct nesting means the most recently opened bracket must be the next one closed; checking only the top enforces exactly this LIFO requirement, and if the matching opener exists deeper in the stack instead of at the top, that means brackets have crossed rather than nested properly, which should be rejected.

**Q:** What would go wrong if the empty-stack check before popping were omitted?
**A:** On an input where a closing bracket appears with no corresponding opener on the stack (like `")"` alone, or more closers than openers anywhere), attempting to pop from an empty stack would throw a runtime exception instead of gracefully returning `false`.

**Q:** Why is the worst-case space complexity O(n) despite the algorithm otherwise seeming "simple"?
**A:** In the worst case — a string consisting entirely of opening brackets — every single character gets pushed onto the stack before the scan ends, so the stack's size can grow proportionally to the full length of the input string.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public boolean isValid(String s) {
        if (s.length() % 2 != 0) {
            return false;
        }

        Deque<Character> stack = new ArrayDeque<>();

        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else {
                if (stack.isEmpty()) {
                    return false;
                }
                char top = stack.pop();
                if ((c == ')' && top != '(') ||
                    (c == ']' && top != '[') ||
                    (c == '}' && top != '{')) {
                    return false;
                }
            }
        }

        return stack.isEmpty();
    }
}
```

## 14. Self-Test

Given a string of only bracket characters `()[]{}`, determine whether every opening bracket is closed by the correct matching type in the correct nested order. Re-derive: use a stack — push each opening bracket seen; on each closing bracket, fail immediately if the stack is empty or its top doesn't match the closer's type, otherwise pop; at the end, the string is valid only if the stack is completely empty.
