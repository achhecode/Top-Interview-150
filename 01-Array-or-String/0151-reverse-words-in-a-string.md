---
problem: 151 - Reverse Words in a String
url: https://leetcode.com/problems/reverse-words-in-a-string/
difficulty: Medium
section: String / Two Pointers
patterns: [reverse-scan, char-array-manipulation]
data_structures: [string, array]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** A string `s` containing words separated by one or more spaces, possibly with leading/trailing spaces too.

**Required:** Return a string with the words in **reverse order**, separated by exactly **one single space**, with **no leading or trailing spaces**.

**Constraints that actually matter:**
- `1 <= s.length <= 10^4` — moderate size; a couple of linear passes are comfortably fast enough.
- `s` may contain **leading spaces, trailing spaces, and multiple spaces between words** — this is the central complexity of the problem: the output must normalize all of that away, collapsing any run of spaces between words down to exactly one, and removing any leading/trailing spaces entirely.
- **There is guaranteed to be at least one word** — removes the need to handle a fully empty-of-words result.
- The explicit **follow-up** asks: "if the string data type is mutable in your language, can you solve it in-place with O(1) extra space?" — this is a strong hint about the *intended* technique (reverse the whole character sequence, then reverse each word back individually, all done via in-place character manipulation) even though Java's `String` immutability means true O(1) extra space isn't achievable in Java the way it would be in C++ — the *algorithmic idea* still transfers and is worth demonstrating.

## 2. Recognition Signals

- "Reverse the order of words, normalize whitespace" → a combination of **tokenization/whitespace-cleanup** plus a **reversal** — the most direct approach is to split into words (skipping empty tokens caused by multiple spaces), then reassemble in reverse order with single-space separators.
- The explicit **follow-up mentioning in-place, O(1) extra space, contingent on string mutability** → the signature of the **reverse-then-fix-up** technique: reverse the entire character sequence first, then reverse each individual word back to correct its internal order, since reversing everything once flips the word order correctly but scrambles each word's internal letters, requiring a second, localized fix.
- Whenever you need to reverse the order of "chunks" (words, blocks, tokens) within a sequence while independently preserving each chunk's own internal order — the "reverse the whole thing, then reverse each chunk back individually" trick is a general and reusable technique.

**Pattern:** Reverse-Then-Fix-Up (a two-phase in-place technique: reverse the entire character sequence, then reverse each individual word back to restore its internal letter order) — with a simpler, Java-idiomatic split-based alternative also presented, since Java's immutable strings make the fully in-place version more of an educational exercise than a genuine O(1) space win in this language.

## 3. Core Idea

**Simple approach:** Split the string on any run of one-or-more spaces (discarding empty tokens produced by leading/trailing/multiple spaces), then join the resulting words back together in reverse order with single spaces. This directly produces the correct, normalized output using Java's built-in string utilities.

**In-place-style approach (matching the follow-up's spirit):** Convert the string to a character array. First, reverse the *entire* array — this correctly reverses the overall word order, but as a side effect, it also reverses the letters *within* each word (e.g., "the sky" reversed entirely becomes "yks eht", where "the" has become "eht" and "sky" has become "yks", though now in the right word order). Second, walk through the reversed array and reverse each individual word's characters back to their correct internal order (turning "eht" back into "the", "yks" back into "sky"), while simultaneously collapsing multiple spaces and trimming leading/trailing spaces as you identify word boundaries. The combination of these two reversal passes correctly produces the final result.

**Invariant (for the reverse-then-fix-up approach):** After the first full-array reversal, the words appear in the correct final order, but each word's internal characters are backward; after the second pass (which finds each word's boundaries and reverses just that word's characters back), every word is simultaneously in the correct order (from pass 1) and has correct internal character order (restored in pass 2).

## 4. Approach 1 — Split and Reassemble (Java-Idiomatic)

**Logic:** Use `String.trim()` to remove leading/trailing spaces, split on `\\s+` (one or more whitespace characters) to tokenize into words while automatically collapsing multiple spaces, then iterate the resulting array from the end to the beginning, joining words with single spaces.

```java
public String reverseWords(String s) {
    String[] words = s.trim().split("\\s+");
    StringBuilder result = new StringBuilder();

    for (int i = words.length - 1; i >= 0; i--) {
        result.append(words[i]);
        if (i > 0) {
            result.append(' ');
        }
    }

    return result.toString();
}
```

- **Time:** O(n) — `trim()` and `split()` each scan the string once; building the result via `StringBuilder` is also linear.
- **Space:** O(n) auxiliary — the `words` array holds all the tokenized substrings, and the `StringBuilder` holds the full result.

**Why it is presented first:** This is the natural, idiomatic Java solution and is fully correct and efficient at O(n) time. It doesn't meet the follow-up's aspirational "O(1) extra space" bar (which, as discussed, isn't truly achievable in Java anyway due to string immutability), but it's simpler, more readable, and very unlikely to introduce subtle bugs — often the preferred real-world choice unless an interviewer specifically wants to see the in-place character-manipulation technique demonstrated.

## 5. Approach 2 — Reverse-Then-Fix-Up (In-Place Style, Matching the Follow-Up)

**Algorithm:**
1. Convert `s` to a `char[]` array.
2. **Phase 1:** Reverse the entire array in place (standard two-pointer swap from both ends toward the middle).
3. **Phase 2:** Walk through the reversed array with a pointer, identifying each word's start and end boundaries (skipping over space characters to find word starts), reversing each individual word's characters back to correct internal order in place, and simultaneously writing the cleaned-up result (single spaces between words, no leading/trailing spaces) into a new position within the array (or into a separate output buffer, since precisely collapsing spaces in place while iterating is intricate to get exactly right).
4. Return the final cleaned-up string, trimmed to the correct length.

```java
public String reverseWords(String s) {
    char[] chars = s.toCharArray();
    int n = chars.length;

    reverseRange(chars, 0, n - 1);

    int writeIndex = 0;
    int i = 0;
    while (i < n) {
        if (chars[i] == ' ') {
            i++;
            continue;
        }

        int wordStart = i;
        while (i < n && chars[i] != ' ') {
            i++;
        }
        int wordEnd = i - 1;

        reverseRange(chars, wordStart, wordEnd);

        if (writeIndex > 0) {
            chars[writeIndex++] = ' ';
        }
        for (int j = wordStart; j <= wordEnd; j++) {
            chars[writeIndex++] = chars[j];
        }
    }

    return new String(chars, 0, writeIndex);
}

private void reverseRange(char[] chars, int left, int right) {
    while (left < right) {
        char temp = chars[left];
        chars[left] = chars[right];
        chars[right] = temp;
        left++;
        right--;
    }
}
```

- **Time:** O(n) — the full-array reversal is O(n), and the word-boundary walk plus per-word reversal together touch each character a bounded constant number of times, remaining O(n) overall.
- **Space:** O(n) for the `char[]` array itself, since Java `String` objects are immutable and cannot be mutated in place — this is a fundamental language constraint, not a flaw in the algorithm's design; the *character manipulation itself* is performed in place within that array, matching the spirit of the follow-up even though Java can't achieve true O(1) *extra* space relative to needing a mutable copy of the string at all.

**Why this approach is shown despite Java's space limitation:** Demonstrating this technique shows familiarity with the classic in-place reversal trick (reverse everything, then reverse each piece back), which is directly transferable to languages with mutable strings (like C++, where this genuinely achieves O(1) *extra* space beyond the input itself) and is a generally reusable pattern for "reverse the order of chunks while preserving each chunk's internal order" problems.

## 6. Dry Run

Example: `s = "a good   example"`.
Chosen because it has multiple spaces between "good" and "example," directly exercising the whitespace-collapsing logic, using the reverse-then-fix-up approach (Approach 2).

`n = 17` (character array: `['a',' ','g','o','o','d',' ',' ',' ','e','x','a','m','p','l','e']` — wait, let me index precisely: "a good   example" has characters: a(0) (1)g(2)o(3)o(4)d(5) (6) (7) (8)e(9)x(10)a(11)m(12)p(13)l(14)e(15), so n=16.

**Phase 1 — reverse entire array:**
Original: `"a good   example"` → Reversed: `"elpmaxe   doog a"`

**Phase 2 — walk through reversed array, reverse each word back, write cleaned output:**

| i | Encounter | Action | writeIndex after | Output so far |
|---|-----------|--------|----------------------|--------------------|
| 0-5 | "elpmax" then 'e' at 6 → word "elpmaxe" (indices 0-6) | reverse word → "example"; write (writeIndex=0, no leading space needed) | 7 | "example" |
| 7-9 | spaces at 7,8,9 | skip | 7 | "example" |
| 10-13 | word "doog" (indices 10-13) | reverse word → "good"; write space + "good" | 12 | "example good" |
| 14 | space at 14 | skip | 12 | "example good" |
| 15 | word "a" (index 15) | reverse word (no-op, single char) → "a"; write space + "a" | 14 | "example good a" |

Exit condition: `i` reaches `n` (16), loop ends.

**Final answer:** `"example good a"`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Split and reassemble | O(n) | O(n) | Simple, idiomatic, and clear; the practical default choice in Java |
| 2. Reverse-then-fix-up (char array) | O(n) | O(n) (Java strings are immutable; true O(1) extra space isn't achievable in Java) | Demonstrates the classic in-place reversal technique; genuinely O(1) extra in a language with mutable strings (e.g. C++) |

Input space for `s` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Leading and/or trailing spaces (e.g. `"  hello world  "`) | Output must have no leading or trailing spaces at all | Approach 1's `trim()` explicitly removes them before splitting; Approach 2's phase 2 loop skips over space characters entirely when searching for the next word's start, and never writes a leading space before the very first word (`writeIndex > 0` guard) |
| Multiple consecutive spaces between words (e.g. `"a good   example"`, as in the dry run) | Must collapse down to exactly one space in the output | Approach 1's `split("\\s+")` treats any run of one-or-more spaces as a single delimiter, producing no empty tokens; Approach 2's phase 2 loop's `while (chars[i] == ' ') i++;`-style skipping naturally treats any length of space run identically |
| Single word with no spaces at all (e.g. `"word"`) | Should return unchanged (trivially "reversed" since there's only one word) | Approach 1's split produces a single-element array, and the reversal loop trivially outputs just that one word; Approach 2's phase 1 reverses the single word's characters, and phase 2 finds it as one word, reverses it back to original, and writes it with no additional space needed |
| Words containing digits (per the constraint: "letters and digits and spaces") | Must not treat digit characters any differently from letters during comparison/reversal | Both approaches operate purely on the position of space characters to determine word boundaries, treating every non-space character (letters or digits) identically, with no special-casing needed |
| A string that is otherwise entirely spaces except for one word in the middle | Edge case combining leading, trailing, and the single-word scenario simultaneously | Handled correctly by the same leading/trailing space skip logic and single-word handling already described, with no additional special-casing required beyond what's already in place |
| Mixed upper/lowercase letters | Should preserve case exactly, since the problem doesn't ask for case normalization, only word order and space normalization | Neither approach ever modifies character case — only positions/order are manipulated, so case is naturally preserved unchanged throughout |

## 9. Java Notes

- **Java `String` immutability and the follow-up's "if the string data type is mutable" caveat:** this is a deliberate, explicit acknowledgment in the problem statement itself that not all languages can achieve true O(1) extra space here — Java strings are immutable, so any manipulation requires either a `char[]` copy (as in Approach 2) or building up a new `String`/`StringBuilder` (as in Approach 1); neither is truly "in-place" relative to the original `String` object in the way a mutable `char*` buffer in C++ would allow.
- **`String.split("\\s+")` vs. manually skipping spaces:** `split` with a regex pattern is concise but involves regex engine overhead; the manual character-array approach (Approach 2) avoids this, though for the given constraints (`n <= 10^4`), the practical performance difference is negligible.
- **`StringBuilder` for building the reversed-word-order result (Approach 1):** avoids the O(n²) pitfall of repeated `String` concatenation in a loop, which would occur if using `+=` directly on a `String` instead.
- **`new String(char[], offset, count)` constructor (used in Approach 2's return statement):** an efficient way to construct the final `String` from only a portion of the working character array (specifically, only the first `writeIndex` characters, ignoring any leftover space at the end of the array from the original, now-unused length).

## 10. Common Mistakes

- **Using `split(" ")` (a single literal space) instead of `split("\\s+")` (one or more whitespace characters as a regex).** Splitting on a single space would produce empty string tokens wherever multiple consecutive spaces occurred, which would then need additional filtering — using the regex form avoids this problem entirely by treating any run of spaces as one delimiter. Fix: always use `split("\\s+")` for this kind of whitespace-tolerant tokenization, or explicitly filter out empty tokens if using a simpler split.
- **Forgetting to call `.trim()` before splitting, when leading or trailing spaces are present.** Without trimming first, splitting on `\\s+` could still produce a leading or trailing empty string token in certain edge cases (specifically, a leading space combined with the regex split behavior), leading to unwanted blank entries in the resulting word array. Fix: always trim the input string first, before splitting.
- **In the reverse-then-fix-up approach, forgetting to reverse each word's characters back after the full-array reversal.** Skipping this step would leave every word's letters backward (e.g., returning "eulb si yks eht" instead of "blue is sky the"), since the full-array reversal correctly fixes word *order* but scrambles word *content*. Fix: always perform the second, per-word reversal pass after the initial full-array reversal.
- **Writing a leading space before the very first word in the output during the reverse-then-fix-up approach's second phase.** This would produce an incorrect result with an unwanted leading space. Fix: guard the space-writing step with a check like `if (writeIndex > 0)`, ensuring a separator space is only written *between* words, never before the first one.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Reverse the order of words while normalizing whitespace → either split-trim-reverse-rejoin (simple, idiomatic), or reverse the entire character sequence then reverse each word back individually (classic in-place technique, true O(1) extra space in languages with mutable strings)."
- **90-second explanation:** "The straightforward approach is to trim the string, split it on any run of whitespace to get clean word tokens with no empty entries, and then rebuild the result by walking through those words from the last to the first, joining them with single spaces. That's simple and O(n) time, though it uses O(n) extra space for the intermediate word array. The more advanced technique, which is what the problem's follow-up is hinting at, is to reverse the entire character sequence first — this correctly reverses the overall word order but also scrambles the letters within each word — and then do a second pass to find each word's boundaries and reverse just that word's characters back to restore correct internal order, while simultaneously writing out the cleaned-up, single-spaced result. In a language with mutable strings, like C++, this achieves true O(1) extra space; in Java, string immutability means I still need a `char[]` copy to work with, so it's more of a demonstration of the classic reversal technique than a genuine space-complexity win in this specific language."
- **Related problems using this pattern:**
  - LeetCode 186 — Reverse Words in a String II (the true in-place, mutable-array version of this exact problem, often used in C++/array-based contexts)
  - LeetCode 344 — Reverse String (the basic building-block reversal operation used here)
  - LeetCode 541 — Reverse String II (a related "reverse chunks while preserving structure" variant)

## 12. Recall Questions

**Q:** Why does reversing the entire character sequence first correctly fix the word *order*, but require a second pass to fix each word's internal character order?
**A:** Reversing the whole sequence correctly flips which word ends up first, second, third, and so on, but it also reverses the character order *within* each individual word as a side effect, so a separate, localized reversal of each word's own characters is needed afterward to undo that unwanted internal scrambling.

**Q:** Why can't a true O(1) extra space solution be achieved in Java the way it can in a language like C++?
**A:** Java's `String` type is immutable, meaning its internal character data cannot be modified after creation, so any manipulation requires either creating a separate mutable `char[]` copy or building up an entirely new `String`/`StringBuilder`, both of which require space proportional to the input rather than truly manipulating the original string object in place.

**Q:** Why is `split("\\s+")` preferred over `split(" ")` for this problem, given the possibility of multiple consecutive spaces?
**A:** Splitting on a single literal space would produce empty string tokens at every point where multiple consecutive spaces occurred, requiring extra filtering logic, while the regex `\\s+` treats any run of one or more whitespace characters as a single delimiter, naturally avoiding empty tokens.

**Q:** In the reverse-then-fix-up approach, why is a guard needed to avoid writing a space before the very first word during the second phase?
**A:** Without such a guard, the algorithm would unconditionally insert a separator space before every word it writes, including the first one, producing an incorrect result with an unwanted leading space in the final output.

**Q:** How would this problem's difficulty change if the input were guaranteed to have exactly one space between every pair of words and no leading/trailing spaces at all?
**A:** The whitespace-normalization complexity would disappear entirely, reducing the problem to a much simpler task of just splitting on single spaces and reversing the resulting array of words, without needing to handle collapsing multiple spaces or trimming edges.

## 13. Final Code

```java
public String reverseWords(String s) {
    String[] words = s.trim().split("\\s+");
    StringBuilder result = new StringBuilder();

    for (int i = words.length - 1; i >= 0; i--) {
        result.append(words[i]);
        if (i > 0) {
            result.append(' ');
        }
    }

    return result.toString();
}
```

## 14. Self-Test

You have a string `s` with words separated by possibly multiple spaces, and possible leading/trailing spaces. Return the words in reverse order, separated by exactly one space, with no leading or trailing spaces. Think about both the simple split-and-rejoin approach, and the classic "reverse everything, then reverse each word back individually" in-place technique the follow-up is hinting at.
