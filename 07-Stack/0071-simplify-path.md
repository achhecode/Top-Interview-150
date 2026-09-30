---
problem: 71 - Simplify Path
url: https://leetcode.com/problems/simplify-path/
difficulty: Medium
section: String / Stack
patterns: [stack, tokenization]
data_structures: [stack, array]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** an absolute Unix-style file path string.

**Required:** transform it into its simplified canonical form: collapse repeated slashes into one, resolve `.` (current directory, meaning "do nothing") and `..` (parent directory, meaning "pop the last directory"), treat any other period sequence (`...`, `....`) as a literal valid name, and produce a result starting with `/`, using single slashes between directories, with no trailing slash unless the result is just the root.

**Constraints that matter:**
- `1 <= path.length <= 3000` — small; performance is not the challenge here, correctness of the many parsing rules is.
- `path` consists of English letters, digits, `.`, `/`, or `_` — a known, limited character set, so no need to handle spaces, other punctuation, or arbitrary Unicode directory names.
- `path` is guaranteed to be a valid absolute Unix path — always starts with `/`, so there's no need to validate or reject malformed input; only the simplification logic matters.
- Going `..` past the root must be a no-op (per Example 4: `/../` → `/`), not an error — this is a specific rule that must be explicitly handled, not left to "whatever falls out" of naive stack logic.

## 2. Recognition Signals

- "Simplify a path by resolving `.` and `..` tokens" → this is the canonical **stack-based tokenization** problem: split into components, then process each component with push/pop semantics.
- The parent-directory (`..`) rule — "go back to what came before" — is the direct signal for a **stack**, since it needs to undo the most recently added directory, which is exactly a pop operation.
- Multiple slashes need collapsing and components need to be re-joined with a fixed separator → splitting on `/` and filtering out empty tokens handles this naturally, rather than trying to manually detect and skip repeated slash characters.
- Named pattern: **Stack-Based Path/Token Resolution** (structurally similar to Valid Parentheses, but the "matching" here is against a rule set rather than bracket types).

## 3. Core Idea

Split the path into components separated by `/`. Process each non-empty component: a plain name gets pushed onto a stack (it's now part of the current directory path); `.` is ignored (it doesn't change the path); `..` pops the stack if non-empty (go up one level), or does nothing if the stack is already empty (can't go above root). After processing every component, join what remains on the stack with `/` and prepend a leading `/`.

**Why it's correct:** a stack directly models a directory path's structure — each pushed name represents one level of depth, and popping models moving up exactly one level, matching how `..` behaves in a real filesystem. Because the stack only ever contains genuine directory names (never `.` or `..` themselves, and never empty strings from collapsed slashes), reconstructing the final path from the stack's contents joined by single slashes automatically satisfies all four output formatting rules at once.

**Invariant:** after processing the first `k` components of the split path, the stack holds exactly the sequence of directory names that make up the correct canonical path for that prefix — no more, no less — so processing is fully composable: the final stack state after all components is the correct canonical path's directory sequence, regardless of the specific mix of names, `.`, `..`, and empty tokens encountered along the way.

## 4. Approach 1 — Brute Force (Manual Char-by-Char Parsing and String Rebuilding)

1. Manually scan the path character by character, extracting tokens between slashes by tracking start/end indices (rather than using `split`).
2. For each extracted token, decide its type (empty, `.`, `..`, or a real name) and rebuild the result as a `String` by either appending a new segment or manually truncating back to the last `/` found via `lastIndexOf` when a `..` is encountered.
3. Continue until the whole input has been scanned; return the rebuilt string (with edge handling for an empty result meaning root).

```java
public String simplifyPathBruteForce(String path) {
    String result = "";
    int i = 0;
    int n = path.length();

    while (i < n) {
        while (i < n && path.charAt(i) == '/') {
            i++;
        }
        int start = i;
        while (i < n && path.charAt(i) != '/') {
            i++;
        }
        String token = path.substring(start, i);

        if (token.isEmpty() || token.equals(".")) {
            continue;
        } else if (token.equals("..")) {
            int lastSlash = result.lastIndexOf('/');
            if (lastSlash >= 0) {
                result = result.substring(0, lastSlash); // costly re-scan and rebuild
            }
        } else {
            result = result + "/" + token; // repeated string concatenation
        }
    }

    return result.isEmpty() ? "/" : result;
}
```

**Time:** O(n²) in the worst case — `String` concatenation (`result + "/" + token`) creates a new string each time, costing O(result length) per append, and `lastIndexOf` similarly scans the accumulated result; across many components, this totals O(n²).
**Space:** O(n) for the accumulated result string (plus the repeated intermediate allocations from concatenation, which add further overhead not reflected in the final space bound).

This is not optimal: repeated `String` concatenation and `lastIndexOf` scanning on a growing string is quadratic behavior for what should be a linear tokenization task; it also entangles the parsing and rebuilding logic in a way that's harder to verify against all the edge cases (trailing slash, root-level `..`, consecutive slashes) compared to a clean stack-based split.

## 5. Approach 2 — Optimized (Split + Stack)

1. Split `path` on `/` using `path.split("/")` — this naturally produces empty strings for consecutive slashes and for a leading slash, which will simply be filtered out.
2. Create a `Deque<String> stack` (used as a stack via `push`/`pop`/`peek`... here more naturally iterated in order, so an `ArrayDeque` used with `addLast`/`removeLast`, or equivalently processed with an `ArrayList` — either works; a `Deque` is used for idiomatic stack semantics).
3. For each token from the split:
   - If it's empty or equals `"."`, skip it.
   - If it equals `".."`, pop the stack if it's not empty (ignore if empty — can't go above root).
   - Otherwise, it's a real directory/file name — push it onto the stack.
4. Join all remaining stack elements (in their original order) with `/`, prepend a leading `/`, and return the result — if the stack is empty, the result is just `/`.

```java
public String simplifyPath(String path) {
    String[] tokens = path.split("/");
    Deque<String> stack = new ArrayDeque<>();

    for (String token : tokens) {
        if (token.isEmpty() || token.equals(".")) {
            continue;
        } else if (token.equals("..")) {
            if (!stack.isEmpty()) {
                stack.pollLast();
            }
        } else {
            stack.addLast(token);
        }
    }

    if (stack.isEmpty()) {
        return "/";
    }

    StringBuilder result = new StringBuilder();
    for (String dir : stack) {
        result.append('/').append(dir);
    }

    return result.toString();
}
```

**Time:** O(n) — `split` is O(n), the token-processing loop is O(number of tokens) with O(1) amortized stack operations per token, and the final `StringBuilder` join is O(total output length), all linear in `n`.
**Space:** O(n) — the `tokens` array, the stack, and the `StringBuilder` result each scale with the input size in the worst case.

This is optimal: every character of the input must be examined at least once to correctly tokenize it, so O(n) is the best possible time, and `StringBuilder` for the final join avoids the repeated-concatenation cost that made the brute force quadratic.

## 6. Dry Run

`path = "/home/user/Documents/../Pictures"` → `tokens = ["", "home", "user", "Documents", "..", "Pictures"]`

| token | type | action | stack after |
|---|---|---|---|
| "" | empty | skip | [] |
| "home" | name | push | [home] |
| "user" | name | push | [home, user] |
| "Documents" | name | push | [home, user, Documents] |
| ".." | parent | pop | [home, user] |
| "Pictures" | name | push | [home, user, Pictures] |

Exit: all tokens processed. Join stack with `/`: `/home/user/Pictures`, matching the expected output.

Contrast with `path = "/../"` → `tokens = ["", "..", ""]`: `""` skipped, `".."` attempted on an empty stack → no-op (can't go above root), final `""` skipped. Stack remains empty → result is `"/"`, matching Example 4.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Manual Parse + String Rebuild | O(n²) worst case | O(n) (plus concatenation overhead) | Repeated `String` concatenation and `lastIndexOf` scans |
| Split + Stack | O(n) | O(n) | `split`, stack, and `StringBuilder` join all linear |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Trailing slash (`"/home/"`) | Must not appear in output unless root | `split` produces a trailing empty token, which is skipped; the final join never adds a trailing slash since it's always prepended before each directory, not appended after |
| Multiple consecutive slashes (`"/home//foo/"`) | Must collapse to a single slash | `split("/")` produces empty tokens between consecutive slashes, all skipped |
| `".."` at the root (`"/../"`) | Must be a no-op, not an error or negative depth | `if (!stack.isEmpty())` guard prevents popping an already-empty stack |
| `"."` (current directory) anywhere in the path | Must be ignored entirely, contributing nothing | Explicitly skipped alongside empty tokens |
| Names that look like periods but aren't exactly `.` or `..` (e.g. `"..."`, `"...."`)  | Must be treated as literal valid directory/file names, not resolved | Only exact string equality checks against `"."` and `".."` trigger special handling; `"..."` falls through to the "real name" branch and gets pushed normally |
| Path is just root (`"/"`) | Simplest possible valid input | `split` produces only empty tokens, all skipped; stack stays empty, result is `"/"` |

## 9. Java Notes

- `String.split("/")` on a path starting with `/` always produces a leading empty string as the first array element (since there's "nothing" before the first slash) — this is expected and is simply skipped along with any other empty tokens from consecutive slashes, rather than treated as a special case.
- `String.split("/")` also does **not** produce a trailing empty string for a trailing slash by default (Java's `split` drops trailing empty strings unless a negative limit is passed) — but since we're only counting on all empty tokens being skipped anyway, this behavior doesn't affect correctness either way.
- `Deque<String>` with `addLast`/`pollLast` is used here instead of `push`/`pop` (which operate on the *head* for `ArrayDeque`) specifically so that iterating the deque in its natural order (`for (String dir : stack)`) visits elements from the bottom (oldest, i.e., first pushed) to the top (most recent) — matching the order they should appear in the final path, left to right.
- `StringBuilder` is used for the final join specifically to avoid the O(n²) repeated-concatenation trap that the brute force demonstrates — each `append` call is O(1) amortized, unlike `String + String` which allocates a new string every time.

## 10. Common Mistakes

- Using `stack.push()`/`stack.pop()` (head-based operations on `ArrayDeque`) but then iterating the deque directly for the final join — this would visit elements in reverse (most-recently-pushed first) order, producing a backwards path. Fix: either iterate in reverse, or use tail-based operations (`addLast`/`pollLast`) consistently so natural iteration order matches path order.
- Treating any token containing only periods as `".."`-like (e.g., checking `token.matches("\\.+")` instead of exact equality) — this incorrectly resolves valid literal names like `"..."` as if they were parent-directory references. Fix: only exact string equality against `"."` and `".."` should trigger special handling; anything else, periods included, is a literal name.
- Popping from the stack unconditionally on `".."` without checking `isEmpty()` first — this throws an exception (or, with certain deque methods, silently returns `null`, which could then corrupt later logic) when `..` appears at the root with nothing to pop. Fix: always guard the pop with an emptiness check, treating root-level `..` as a no-op per the problem's explicit rule.
- Manually trying to detect and collapse consecutive slashes with a character-by-character scan (as in the brute force) instead of using `split("/")`, which handles this naturally by producing empty tokens that get filtered out — not incorrect, but more error-prone and harder to get exactly right across all the slash-related edge cases.

## 11. Interview Takeaway

- **Trigger sentence:** "Simplify a path with `.`/`..` navigation and redundant slashes → split into tokens, use a stack to push real names and pop on `..`, then join what remains."
- **90-second explanation:** "I split the path on slashes, which conveniently turns consecutive or leading slashes into empty tokens I can just skip. Then I process each token: a `.` means stay put, so I skip it too; a `..` means go up one level, so I pop the stack if it's not already empty — if it is empty, that means we're already at the root, so I just ignore it rather than erroring. Anything else is a genuine directory or file name, so I push it. A stack is exactly the right structure here because going up a directory is inherently an 'undo the last push' operation. At the end, I join whatever's left on the stack with single slashes and a leading slash, which automatically satisfies all the output formatting rules — no trailing slash, single separators, and no leftover `.`/`..` tokens, since those never made it onto the stack in the first place."
- **Related problems:** 20 (Valid Parentheses, similar stack-based token resolution), 388 (Longest Absolute File Path), 224 (Basic Calculator, another stack-based expression/token resolution problem).

## 12. Recall Questions

**Q:** Why does a stack naturally model the effect of `..` in a file path?
**A:** Moving up one directory level is inherently an "undo the most recent push" operation — the last directory entered is exactly the one that should be removed, which is precisely what a stack's pop operation does.

**Q:** Why is `".."` at the root treated as a no-op instead of an error or causing some kind of negative-depth state?
**A:** The problem explicitly states that going above the root isn't possible, so the correct behavior is to simply ignore the `..` token when the stack is already empty, leaving the path unchanged rather than crashing or corrupting the stack state.

**Q:** Why can't `"..."` or `"...."` be resolved the same way as `".."`, even though they're also made entirely of periods?
**A:** The problem explicitly defines only exact `"."` and `".."` as having special navigational meaning; any other sequence of periods, no matter how many, is defined as a literal, valid directory or file name and must be treated like any ordinary name, i.e., pushed onto the stack.

**Q:** Why does splitting on `/` conveniently handle both leading slashes and consecutive slashes without extra logic?
**A:** Both situations produce empty-string tokens in the split result (nothing between two adjacent slash characters, or nothing before the very first slash), and since all empty tokens are already skipped as part of the normal token-processing loop, no separate detection logic for "collapse consecutive slashes" or "handle the leading slash" is needed.

**Q:** Why is `StringBuilder` used for the final result construction instead of repeated `String` concatenation?
**A:** Repeated `String` concatenation allocates a brand-new string object on every single append, which costs time proportional to the string's current length each time and leads to quadratic total cost across many appends; `StringBuilder` avoids this by growing an internal mutable buffer, making each append O(1) amortized.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public String simplifyPath(String path) {
        String[] tokens = path.split("/");
        Deque<String> stack = new ArrayDeque<>();

        for (String token : tokens) {
            if (token.isEmpty() || token.equals(".")) {
                continue;
            } else if (token.equals("..")) {
                if (!stack.isEmpty()) {
                    stack.pollLast();
                }
            } else {
                stack.addLast(token);
            }
        }

        if (stack.isEmpty()) {
            return "/";
        }

        StringBuilder result = new StringBuilder();
        for (String dir : stack) {
            result.append('/').append(dir);
        }

        return result.toString();
    }
}
```

## 14. Self-Test

Given an absolute Unix-style path, produce its simplified canonical form, resolving `.` and `..`, collapsing repeated slashes, and treating any other period sequence as a literal name. Re-derive: split the path on `/`, then process each token with a stack — skip empty tokens and `.`, pop the stack (if non-empty) on `..`, and push any other token as a real directory name; finally join the stack's contents with single slashes and a leading slash, returning `/` if the stack ends empty.
