---
problem: 14 - Longest Common Prefix
url: https://leetcode.com/problems/longest-common-prefix/
difficulty: Easy
section: String / Horizontal Scanning
patterns: [horizontal-scanning, vertical-scanning]
data_structures: [string, array]
time: O(S)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An array of strings `strs`.

**Required:** Return the longest string that is a prefix of **every** string in `strs`. If no non-empty common prefix exists, return `""`.

**Constraints that actually matter:**
- `1 <= strs.length <= 200` and `0 <= strs[i].length <= 200` — both the number of strings and their individual lengths are small and bounded, so `S` (the total number of characters across all strings, up to `200 * 200 = 40,000`) is small; even an approach that compares characters across all strings multiple times is fast enough here.
- **A string can be empty (`strs[i].length == 0`)** — if any string in the array is empty, the longest common prefix must immediately be `""`, since an empty string has no characters to share with anything.
- `strs[i]` consists of only lowercase English letters (when non-empty) — a simple, fixed character set; no need to worry about case sensitivity, digits, or special characters.

## 2. Recognition Signals

- "Find the longest common prefix among multiple strings" → the signature **horizontal scanning** (or its dual, **vertical scanning**) technique: repeatedly narrow down a candidate prefix by comparing it against each string, or compare character-by-character across all strings at each position.
- Whenever a shared property must hold across an entire collection simultaneously, and that property can only shrink (never grow) as you incorporate more elements — start with an initial candidate (e.g., the first string entirely) and progressively trim it down based on each subsequent element, stopping early the moment it's reduced to nothing.
- The presence of potentially empty strings, or strings of very different lengths, in the input is itself a signal to be careful about **bounds checking** at every character comparison — the common prefix can never be longer than the *shortest* string in the array.

**Pattern:** Horizontal Scanning (treat the first string as the initial candidate prefix, then repeatedly shrink it by comparing against each subsequent string in turn) — an alternative dual approach, Vertical Scanning, compares character-by-character across all strings at each position instead.

## 3. Core Idea

The longest common prefix of the entire array can never be longer than the common prefix of just the first two strings, which can never be longer than the common prefix of the first three, and so on — each additional string can only shrink (or preserve) the current candidate prefix, never extend it. So: start with the entire first string as the candidate answer, then for each subsequent string in the array, trim the candidate down to the longest prefix it shares with *that* string specifically (by comparing character-by-character until a mismatch or the end of either string is reached). If the candidate ever shrinks to an empty string, it can never grow back, so you can stop immediately and return `""`.

**Invariant:** After processing the first `k` strings in the array, the current candidate prefix is exactly the longest common prefix shared among those `k` strings — so once all `n` strings have been processed, the candidate is exactly the answer for the full array.

## 4. Approach 1 — Vertical Scanning (Column by Column)

**Logic:** Compare characters at the same index position across *all* strings simultaneously, moving left to right. For each character position `i`, check whether every string has the same character at position `i` (and is even long enough to have a character there). Stop at the first mismatch or the first string that's too short.

```java
public String longestCommonPrefix(String[] strs) {
    if (strs.length == 0) {
        return "";
    }

    for (int i = 0; i < strs[0].length(); i++) {
        char c = strs[0].charAt(i);
        for (int j = 1; j < strs.length; j++) {
            if (i == strs[j].length() || strs[j].charAt(i) != c) {
                return strs[0].substring(0, i);
            }
        }
    }

    return strs[0];
}
```

- **Time:** O(S) where `S` is the total number of characters across all strings in the worst case (each character is compared at most once against the first string's corresponding character) — technically this can terminate early, but worst case still bounds to O(S).
- **Space:** O(1) auxiliary (excluding the returned substring itself).

**Why it is presented as an alternative, not strictly "worse":** This approach is actually asymptotically comparable to the horizontal scanning approach below — both are O(S) in the worst case. The key practical difference is that vertical scanning can terminate **earlier** in cases where a mismatch occurs early in the strings (e.g., if the very first character already differs across some strings, vertical scanning detects this in O(n) rather than needing to fully process one string against another first), while horizontal scanning's early termination depends on processing one full string pair at a time. Both are valid, reasonable approaches; horizontal scanning is presented as the primary approach below for its simplicity and intuitive "shrink a candidate" framing.

## 5. Approach 2 — Horizontal Scanning (Shrink the Candidate)

**Algorithm:**
1. If `strs` is empty, return `""` (defensive; the constraint guarantees at least one string, but this guards against a technically empty array regardless).
2. Initialize `prefix = strs[0]` (start with the entire first string as the candidate).
3. For each subsequent string `strs[i]` from index `1` to `strs.length - 1`:
   - While `strs[i]` does **not** start with `prefix` (i.e., `!strs[i].startsWith(prefix)`), shorten `prefix` by removing its last character (`prefix = prefix.substring(0, prefix.length() - 1)`).
   - If `prefix` becomes empty during this shrinking, return `""` immediately (no common prefix can exist).
4. Return `prefix` after all strings have been processed.

```java
public String longestCommonPrefix(String[] strs) {
    String prefix = strs[0];

    for (int i = 1; i < strs.length; i++) {
        while (!strs[i].startsWith(prefix)) {
            prefix = prefix.substring(0, prefix.length() - 1);
            if (prefix.isEmpty()) {
                return "";
            }
        }
    }

    return prefix;
}
```

- **Time:** O(S) in the worst case, where `S` is the total number of characters across all strings — each character of `prefix` may be compared against each string via `startsWith`, and the shrinking process across all strings combined is bounded by the total prefix-length work done.
- **Space:** O(1) auxiliary beyond the `prefix` string itself (and Java's `substring` in modern versions creates a new character array copy, so repeated shrinking does allocate intermediate strings, though this is still bounded by O(S) total work given the small constraints here).

**Why this is a reasonable optimal choice:** Every character of the eventual answer (and potentially some characters beyond it, before a mismatch is found) must be examined at least once across the strings to confirm it's shared, so O(S) is a natural lower bound given the problem's nature — and this approach achieves that bound with a simple, easy-to-reason-about "progressively shrink the candidate" strategy. Given the small constraints (`S` up to 40,000), the practical performance difference between this and vertical scanning is negligible; horizontal scanning is chosen here for its conceptual clarity as the primary technique to internalize, since "start with a full candidate and trim it down" generalizes well to other "find the common X across many things" problems.

## 6. Dry Run

Example: `strs = ["flower", "flow", "flight"]`.
Chosen because it's the canonical example, and it clearly shows the candidate prefix shrinking across two different comparisons before settling on the final answer.

Initial: `prefix = "flower"` (the entire first string).

**Processing `strs[1] = "flow"`:**

| Check | strs[1].startsWith(prefix)? | Action | prefix after |
|-------|-----------------------------------|--------|-------------------|
| "flow".startsWith("flower") | No (flow is shorter) | shrink: prefix="flowe" | "flowe" |
| "flow".startsWith("flowe") | No | shrink: prefix="flow" | "flow" |
| "flow".startsWith("flow") | Yes | stop shrinking | "flow" |

**Processing `strs[2] = "flight"`:**

| Check | strs[2].startsWith(prefix)? | Action | prefix after |
|-------|-----------------------------------|--------|-------------------|
| "flight".startsWith("flow") | No | shrink: prefix="flo" | "flo" |
| "flight".startsWith("flo") | No | shrink: prefix="fl" | "fl" |
| "flight".startsWith("fl") | Yes | stop shrinking | "fl" |

Exit condition: all strings in `strs` (indices 1 and 2) have been processed.

**Final answer:** `"fl"`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Vertical scanning (column by column) | O(S) worst case | O(1) (excl. output) | Can terminate very early on an early mismatch; compares one character position across all strings at a time |
| 2. Horizontal scanning (shrink candidate) | O(S) worst case | O(1) (excl. intermediate substrings) | Simple, intuitive "start full, trim down" strategy; comparable practical performance given small constraints |

`S` denotes the total number of characters across all strings in `strs` (bounded by `200 * 200 = 40,000` given the constraints), and both approaches share the same worst-case asymptotic bound.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| An empty string present anywhere in `strs` (e.g. `["", "abc"]`) | The common prefix must immediately be `""`, since an empty string shares no characters with anything | If `strs[0]` itself is empty, `prefix` starts as `""`, and the `while` loop's `startsWith` check trivially passes for any string (every string starts with the empty string), so `prefix` remains `""` throughout, correctly returned; if a *later* string is empty, `strs[i].startsWith(prefix)` is false for any non-empty `prefix` (since an empty string can't start with something longer than itself), triggering the shrink loop until `prefix` itself becomes empty, correctly returning `""` |
| Single string in the array (`strs.length == 1`) | With nothing to compare against, the entire first string should be the answer | The `for` loop (starting at `i = 1`) never executes since there's no second element, so `prefix` remains the entire first string, correctly returned unchanged |
| No common prefix at all (e.g. `["dog","racecar","car"]`) | Should correctly shrink all the way down to `""`, not get stuck at some non-empty but still-incorrect candidate | The shrink loop continues removing characters from the end of `prefix` until either a match is found or `prefix` becomes fully empty, at which point the explicit `if (prefix.isEmpty()) return "";` check catches this and returns immediately |
| All strings identical (e.g. `["test","test","test"]`) | The common prefix should be the entire string itself | Every `startsWith` check succeeds immediately without any shrinking needed, so `prefix` remains the full first string throughout, correctly returned |
| The common prefix equals the *shortest* string in the array exactly (e.g. `["flow","flower","flowchart"]`) | The algorithm must correctly recognize when the current candidate exactly matches a shorter string, without over-shrinking | Once `prefix` shrinks down to match the length and content of the shortest relevant string, `startsWith` succeeds and shrinking stops at exactly that point, not one character short or one character too many |
| Very short array with maximum-length strings, or vice versa (boundary values of the constraints) | Should behave correctly regardless of whether the "large" dimension is array length or individual string length | The algorithm's logic doesn't depend on any particular balance between array length and string length — it processes whichever combination is given uniformly, correctly bounded by the total work `S` regardless of how that total is distributed |

## 9. Java Notes

- **`String.startsWith(prefix)`:** a built-in, idiomatic, and efficient way to check whether one string begins with another, without manually looping through characters — used here to avoid writing a manual character-comparison loop for this specific check.
- **`String.substring(0, length)`:** used to shrink the candidate prefix by one character at a time; note that in modern Java (7u6+), `substring` creates a new backing character array copy (rather than sharing the original array with an offset, as in older Java versions), so repeated shrinking does incur some allocation overhead — acceptable here given the small constraints, but worth knowing as a general Java performance characteristic if applying this pattern to much larger strings.
- **Comparing approaches — `startsWith` (Approach 2) vs. manual `charAt` comparison (Approach 1):** both are valid; `startsWith` is more concise and leverages a well-tested library method, while manual character comparison (as in vertical scanning) offers more fine-grained control and can be marginally more efficient by avoiding repeated full-prefix comparisons on each shrink iteration — a good discussion point for demonstrating awareness of trade-offs in an interview.
- **No overflow or numeric concerns:** this is a pure string-comparison problem; there's no arithmetic involved that could risk overflow.

## 10. Common Mistakes

- **Forgetting to check for an empty `prefix` during the shrinking loop, or checking it in the wrong place.** Without this check, if no common prefix exists at all, the shrink loop would eventually call `substring(0, -1)` on an already-empty string once it tries to shrink further, throwing a `StringIndexOutOfBoundsException`. Fix: always explicitly check `if (prefix.isEmpty())` and return `""` immediately after each shrink, before attempting further comparisons.
- **Not handling the case where a string in the array is empty from the start.** If not handled carefully, comparing an empty string with `startsWith` or attempting to access its characters by index could produce unexpected results if the logic assumes every string has at least one character. Fix: rely on `String.startsWith` and `String.substring`, which correctly and safely handle empty strings as edge cases within their own well-defined semantics, rather than writing manual index-based comparisons that might not account for zero-length strings.
- **Assuming the longest common prefix must be as long as the shortest string, without actually verifying character-by-character equality.** Two strings can be the same length as each other but still have no common prefix at all (e.g. `"abc"` and `"xyz"`), so length alone is never sufficient — actual character comparison (via `startsWith`, or manual character checks) is always required. Fix: always perform genuine content comparison, not just length-based reasoning.
- **Using vertical scanning but forgetting to check `i == strs[j].length()` (i.e., that string `j` even has a character at position `i`) before comparing characters.** This would throw a `StringIndexOutOfBoundsException` when comparing against a shorter string. Fix: always check the current string's length before attempting `charAt(i)` on it during vertical scanning.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Longest common prefix across multiple strings → start with the first string as a full candidate, then shrink it down against each subsequent string until it's confirmed to be a genuine shared prefix everywhere (or shrinks to empty)."
- **90-second explanation:** "I take the first string in the array as my initial candidate for the answer. Then, for each remaining string, I check whether it starts with my current candidate prefix; if it doesn't, I shorten the candidate by one character from the end and check again, repeating until either the candidate does match as a genuine prefix of that string, or the candidate shrinks all the way down to an empty string, in which case I can return immediately since no common prefix exists. I repeat this process across every string in the array, and whatever candidate remains after processing all of them is the longest common prefix, since each string can only ever shrink the candidate, never grow it back. This runs in O(S) time, where S is the total number of characters across all the strings, since in the worst case every character might be examined once during the shrinking comparisons."
- **Related problems using this pattern:**
  - LeetCode 58 — Length of Last Word (different specific technique, but similarly a direct character-scan approach on strings)
  - LeetCode 720 — Longest Word in Dictionary (related "build up shared structure across many strings" flavor, though typically solved with a Trie)
  - LeetCode 1044 — Longest Duplicate Substring (much harder variant of a related "shared substring" family, typically requiring suffix arrays or binary search + hashing)

## 12. Recall Questions

**Q:** Why is it correct to say the longest common prefix of the full array can never be *longer* than the longest common prefix of just the first two strings?
**A:** Any prefix shared by all strings in the array must, in particular, be shared by the first two strings specifically, so the common prefix of the full set is always a prefix of (or equal to) the common prefix of any subset, including just the first two — it can only shrink as more strings are considered, never grow.

**Q:** Why must the algorithm explicitly check whether the candidate prefix has become empty during the shrinking process, rather than letting the loop continue naturally?
**A:** If no common prefix exists at all, the shrinking process would otherwise try to remove a character from an already-empty string, which is an invalid operation (there's no character left to remove), so an explicit empty check is needed to safely terminate and return the correct empty-string result.

**Q:** How does the presence of an empty string anywhere in the input array affect the final answer, and why?
**A:** If any string in the array is empty, the longest common prefix must also be empty, since an empty string has no characters at all to share as a common prefix with any other string, regardless of what the other strings look like.

**Q:** What is the key practical difference in early-termination behavior between horizontal scanning and vertical scanning, even though both share the same worst-case time complexity?
**A:** Vertical scanning compares one character position across every string simultaneously, so it can detect a mismatch as early as the very first character position across all strings; horizontal scanning instead fully reconciles the candidate against one string at a time before moving to the next, so its early termination depends on processing complete string-pair comparisons rather than a single shared character position.

**Q:** Why doesn't the algorithm need to know the lengths of all the strings in advance, or sort them by length, before beginning the comparison process?
**A:** The shrinking process naturally and correctly converges to the true longest common prefix through repeated pairwise trimming regardless of the order or relative lengths of the strings encountered, since each comparison only ever trims the candidate down to what's actually shared, without requiring any prior knowledge of which string is shortest.

## 13. Final Code

```java
public String longestCommonPrefix(String[] strs) {
    String prefix = strs[0];

    for (int i = 1; i < strs.length; i++) {
        while (!strs[i].startsWith(prefix)) {
            prefix = prefix.substring(0, prefix.length() - 1);
            if (prefix.isEmpty()) {
                return "";
            }
        }
    }

    return prefix;
}
```

## 14. Self-Test

You have an array of strings `strs`. Find the longest string that is a prefix of every string in the array, or return `""` if none exists. Think about why starting with the first string as a full candidate answer and progressively shrinking it against each subsequent string, until it's confirmed as a genuine shared prefix everywhere, correctly converges to the true answer.
