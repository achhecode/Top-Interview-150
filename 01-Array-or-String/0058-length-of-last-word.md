---
problem: 58 - Length of Last Word
url: https://leetcode.com/problems/length-of-last-word/
difficulty: Easy
section: String / Two Pointers
patterns: [reverse-scan, trailing-whitespace-skip]
data_structures: [string]
time: O(n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** A string `s` consisting of English letters and spaces.

**Required:** Return the length of the **last** word in the string, where a word is a maximal run of non-space characters.

**Constraints that actually matter:**
- `1 <= s.length <= 10^4` — moderate size, but this problem is inherently a single linear scan regardless, so the size mainly confirms that any O(n) approach (even a couple of passes) will comfortably fit within limits.
- `s` consists of only English letters and spaces — no other whitespace characters (tabs, newlines) to worry about; only `' '` (a single space character) counts as a separator.
- **There may be trailing spaces after the last word** (as shown in example 2: `"   fly me   to   the moon  "` has two trailing spaces) — this is the key subtlety: you can't just look at the very last character of the string and assume it's part of a word; you must first skip any trailing spaces to find where the last word actually ends.
- **There is guaranteed to be at least one word** — removes the need to handle a fully empty-of-words edge case (like an all-space string), simplifying the loop termination logic.

## 2. Recognition Signals

- "Return the length of the last word" + "string may have leading/trailing/multiple spaces" → the signature of a **reverse scan with whitespace-skipping** — process the string from the end backward, since that's the most direct route to "the last word" without needing to parse the entire string into a list of words first.
- Whenever you only need information about the **end** of a sequence (not the whole thing), scanning backward from the end is often more direct and can avoid unnecessary work compared to a forward scan or a full split/tokenize operation.
- The explicit mention of trailing spaces in the examples is itself a signal: it's specifically warning you not to naively assume `s.length() - 1` is part of the last word — always signals "skip trailing whitespace first" as a required first step.

**Pattern:** Reverse Scan with Two Phases (skip trailing spaces first, then count backward through the last word's characters) — a specialized two-pointer-style technique applied to a single string, scanning right to left.

## 3. Core Idea

Since only the *last* word matters, there's no need to parse the entire string into a list of words (which would require extra space and do unnecessary work on words that aren't needed). Instead, scan from the end of the string backward in two phases: first, skip over any trailing space characters to find the actual last character of the last word; second, continue scanning backward, counting characters, until either a space is encountered or the beginning of the string is reached — that count is the length of the last word.

**Invariant:** After phase one (skipping trailing spaces), the pointer sits on the last non-space character of the string (guaranteed to exist, since the problem guarantees at least one word exists). During phase two, the count of characters scanned before hitting either a space or the start of the string exactly equals the length of that maximal non-space run, which is by construction the last word.

## 4. Approach 1 — Brute Force (Split on Spaces)

**Logic:** Split the string on one-or-more spaces (or split and filter out empty strings), then return the length of the last element in the resulting array.

```java
public int lengthOfLastWord(String s) {
    String[] words = s.trim().split("\\s+");
    return words[words.length - 1].length();
}
```

- **Time:** O(n) — `split` internally scans the entire string once (using a regex engine), and `trim()` is also O(n).
- **Space:** O(n) auxiliary — `split` allocates an array of substrings for *every* word in the string, even though only the last one is actually needed.

**Why it is not optimal:** While technically O(n) time, this approach does unnecessary work: it tokenizes the *entire* string into an array of all words, using O(n) auxiliary space, when only the very last word's length is actually required. It's also subject to the overhead of regex compilation/matching for `split("\\s+")`, which is less efficient in practice than a direct character scan.

## 5. Approach 2 — Optimized (Reverse Scan, Two Phases)

**Algorithm:**
1. Initialize a pointer `i = s.length() - 1` (starting from the very end of the string).
2. **Phase 1 — skip trailing spaces:** While `i >= 0` and `s.charAt(i) == ' '`, decrement `i`.
3. **Phase 2 — count the last word's length:** Initialize `length = 0`. While `i >= 0` and `s.charAt(i) != ' '`, increment `length` and decrement `i`.
4. Return `length`.

```java
public int lengthOfLastWord(String s) {
    int i = s.length() - 1;

    while (i >= 0 && s.charAt(i) == ' ') {
        i--;
    }

    int length = 0;
    while (i >= 0 && s.charAt(i) != ' ') {
        length++;
        i--;
    }

    return length;
}
```

- **Time:** O(n) in the worst case (e.g., a string that's entirely one giant word with no trailing spaces would require scanning back through the whole thing), but in practice often much faster than scanning the *entire* string forward, since it stops as soon as the last word's boundary is found — no need to process anything before that point.
- **Space:** O(1) auxiliary — only a couple of integer variables (`i`, `length`), no arrays or substrings created.

**Why this is optimal:** In the worst case, every character might need to be examined (if the entire string is a single word), so O(n) is the correct worst-case bound — but unlike Approach 1, this approach never does more work than strictly necessary, since it stops scanning the moment it has found the complete last word (i.e., it never looks at characters before the last word's start, whereas splitting tokenizes the *entire* string regardless of where the last word is). Space is O(1) because no intermediate word list or substring is ever created — just a running length counter and a scanning index.

## 6. Dry Run

Example: `s = "   fly me   to   the moon  "`.
Chosen because it's the trickiest of the three canonical examples, with both leading spaces (irrelevant here) and, critically, trailing spaces after the actual last word ("moon"), directly exercising the two-phase logic.

`s.length() = 28`. Initial: `i = 27` (last index).

**Phase 1 — skip trailing spaces:**

| i | s.charAt(i) | Is space? | Action |
|---|--------------|-----------|--------|
| 27 | ' ' | Yes | i-- → 26 |
| 26 | ' ' | Yes | i-- → 25 |
| 25 | 'n' | No | stop phase 1 |

**Phase 2 — count backward through "moon":**

| i | s.charAt(i) | Is space? | Action | length after |
|---|--------------|-----------|--------|------------------|
| 25 | 'n' | No | length++, i-- | 1 |
| 24 | 'o' | No | length++, i-- | 2 |
| 23 | 'o' | No | length++, i-- | 3 |
| 22 | 'm' | No | length++, i-- | 4 |
| 21 | ' ' | Yes | stop phase 2 | 4 |

Exit condition: phase 2 stops upon encountering the space at index 21, immediately before "moon" begins.

**Final answer:** `length = 4`, matching the expected output exactly — the last word is "moon" with length 4.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Split on whitespace | O(n) | O(n) | Correct but tokenizes the entire string unnecessarily; only the last word is needed |
| 2. Reverse scan, two phases | O(n) worst case | O(1) | Optimal in space; often does less real work in practice by stopping early |

Input space for `s` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Trailing spaces after the last word (e.g. example 2) | Naively checking `s.charAt(s.length()-1)` would find a space, not a letter, if not handled | Phase 1 explicitly skips over any trailing spaces before phase 2 begins counting, correctly locating the true end of the last word regardless of how many trailing spaces exist |
| No trailing spaces at all (e.g. `"Hello World"`) | Phase 1 should be a no-op in this case | The `while` condition in phase 1 immediately fails (`s.charAt(i) != ' '` for the last character), so phase 1 does nothing and phase 2 begins counting right away |
| Single word with no spaces at all (e.g. `"a"`) | Both phases must correctly handle a string with no space characters whatsoever | Phase 1 finds no trailing space to skip (loop condition fails immediately), and phase 2 counts the entire string as one word, correctly stopping only when `i < 0` (reaching the start of the string), not when finding a space that doesn't exist |
| Leading spaces before the first (and possibly only) word | Since scanning happens from the end backward, leading spaces are simply never reached if the last word is found before the scan gets that far | The reverse-scan approach never needs to examine leading spaces at all unless the entire rest of the string past them is also just spaces, in which case phase 2 would naturally terminate at `i < 0` after fully counting back through the single word, without ever needing to distinguish "leading" spaces from any other spaces |
| Multiple consecutive spaces between words (e.g. `"fly me   to"`) | Should not affect the boundary detection for the *last* word specifically | Since the algorithm only cares about the boundary immediately before the last word, multiple consecutive spaces anywhere earlier in the string are irrelevant and never examined, as the scan stops as soon as phase 2 encounters the first space it hits, going backward from the end |
| Minimum-length input consisting of just one letter (`s = "a"`, length 1) | Smallest possible valid input per constraints | Phase 1's `i >= 0` check starts at `i = 0`, finds a non-space character immediately (skips phase 1), and phase 2 counts exactly 1 character before `i` becomes -1, correctly returning 1 |

## 9. Java Notes

- **`s.charAt(i)` for direct character access:** avoids the overhead of creating substrings or arrays; a simple, efficient way to inspect individual characters by index in a reverse scan.
- **Avoiding `String.split` and regex overhead:** `split("\\s+")` involves compiling and running a regular expression against the entire string, which carries more overhead than simple character-by-character comparison, especially for a problem where only the trailing portion of the string actually needs to be examined.
- **No `trim()` needed:** since the reverse scan naturally skips trailing spaces as its first phase, there's no need to preprocess the string with `trim()` (which would itself do an O(n) pass and allocate a new trimmed string) — the algorithm handles this inline.
- **Two separate `while` loops rather than one combined loop with a state flag:** keeps the two phases (skip trailing spaces, then count the word) conceptually and syntactically distinct, making the logic easier to read and verify correct, compared to trying to merge both phases into a single loop with an internal boolean state toggle.

## 10. Common Mistakes

- **Forgetting to skip trailing spaces before starting to count the last word's length.** Without this step, if the string ends in one or more spaces, the algorithm would either miscount (treating leading spaces as part of "no word found") or need awkward special-casing. Fix: always perform the trailing-space-skip phase first, unconditionally, before beginning to count characters.
- **Using `String.trim()` plus checking only the very last character without a full reverse scan.** `trim()` only removes leading and trailing whitespace, but doesn't help you find where the last *word specifically* starts if there are multiple spaces between the second-to-last and last words — the reverse scan through the last word's actual characters is still necessary after trimming (or, better, skip the trim and directly reverse-scan, as shown in Approach 2). Fix: after locating the end of the last word, continue scanning backward specifically until hitting a space or the string's start, not just checking a single character.
- **Not handling the case where the entire string could be a single word with no spaces at all.** Some implementations might inadvertently assume a space will always be found to terminate the counting phase, leading to an off-by-one or index-out-of-bounds error if that assumption fails. Fix: always include the `i >= 0` bounds check in both phases' loop conditions, not just the space-character check.
- **Using `String.split` (Approach 1) without realizing it constructs the entire word array unnecessarily, when only the last word is needed.** While not incorrect, this is a missed opportunity for a cleaner, more space-efficient solution, and it's worth being able to explain why the reverse-scan approach is preferable in an interview setting. Fix: default to the reverse-scan approach for cleaner O(1) auxiliary space.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Only need info about the *end* of a string (like the last word) → scan backward from the end, skipping trailing separators first, then counting until the next separator or the start of the string."
- **90-second explanation:** "Since I only care about the last word, I scan from the end of the string backward rather than parsing the whole thing into a list of words. First, I skip over any trailing spaces to find where the actual last word ends — the problem's examples specifically show there can be trailing spaces after the last word, so this step is essential. Then, I continue scanning backward, counting characters, until I either hit a space (marking the start of the word) or reach the beginning of the string entirely (if the last word happens to be the very first word too). The count I accumulate during that second phase is exactly the length of the last word. This approach avoids allocating any extra arrays or substrings, unlike splitting the string on whitespace, which would tokenize the entire string just to look at its last piece."
- **Related problems using this pattern:**
  - LeetCode 151 — Reverse Words in a String
  - LeetCode 14 — Longest Common Prefix (different direction of scan, but similarly a direct character-comparison technique avoiding unnecessary tokenization)
  - LeetCode 344 — Reverse String (related reverse-scan/two-pointer family)

## 12. Recall Questions

**Q:** Why is it necessary to explicitly skip trailing spaces before beginning to count the last word's characters?
**A:** The problem's own examples show the input can end with one or more spaces after the actual last word, so without first skipping them, the algorithm would either miscount or need to special-case an unexpected trailing space as if it were meaningful content.

**Q:** Why is scanning backward from the end of the string generally more efficient here than parsing the entire string into a list of words?
**A:** Only the last word's length is needed, so scanning backward can stop as soon as that word's boundary is found, without ever needing to examine or store any of the earlier words in the string, unlike a full split/tokenize approach.

**Q:** What would happen if the algorithm only checked `s.charAt(s.length() - 1)` directly, without a full reverse scan, to try to determine the last word?
**A:** If the string ends with trailing spaces, that single character check would find a space rather than a letter, incorrectly suggesting there's no word at the end, or requiring extra ad hoc logic to handle that case — a full reverse scan is needed to correctly locate and measure the last word regardless of trailing spaces.

**Q:** Why does the algorithm need a bounds check (`i >= 0`) in addition to the space-character check in both of its scanning phases?
**A:** If the entire string (or the remaining unscanned portion) consists of a single word with no leading spaces before it, the scan could reach the very beginning of the string without ever encountering a space character, so the loop must also terminate correctly when the index runs out of bounds, not only when a space is found.

**Q:** How does this algorithm's behavior differ, if at all, when there are multiple consecutive spaces between two words earlier in the string, versus just a single space?
**A:** It behaves identically either way, since the algorithm only examines the boundary immediately before the last word during its scan and never needs to look at or process anything about the spacing between other, earlier words in the string.

## 13. Final Code

```java
public int lengthOfLastWord(String s) {
    int i = s.length() - 1;

    while (i >= 0 && s.charAt(i) == ' ') {
        i--;
    }

    int length = 0;
    while (i >= 0 && s.charAt(i) != ' ') {
        length++;
        i--;
    }

    return length;
}
```

## 14. Self-Test

You have a string `s` of English letters and spaces, with at least one word present. Return the length of the last word (a maximal run of non-space characters), being careful about possible trailing spaces after it. Think about why scanning backward from the end of the string, in two distinct phases — skip trailing spaces, then count the word — is more direct than tokenizing the whole string.
