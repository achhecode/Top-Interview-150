---
problem: 68 - Text Justification
url: https://leetcode.com/problems/text-justification/
difficulty: Hard
section: String / Simulation
patterns: [greedy-line-packing, simulation]
data_structures: [string, array, list]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An array of strings `words` and an integer `maxWidth`.

**Required:** Format the text into lines, each exactly `maxWidth` characters long, fully justified (both left and right aligned) by distributing extra spaces evenly between words — except the **last line**, which is left-justified with single spaces and padded with trailing spaces, and any line with **only one word**, which is also left-justified (padded on the right) rather than needing space distribution between words that don't exist.

**Constraints that actually matter:**
- `1 <= words.length <= 300`, `1 <= words[i].length <= 20`, `1 <= maxWidth <= 100` — all small and bounded; performance is not the challenge here at all — the entire difficulty of this "Hard"-rated problem is in correctly handling the many formatting edge cases (single-word lines, the last line, uneven space distribution), not in algorithmic complexity.
- **Each word's length is guaranteed to not exceed `maxWidth`** — removes the need to handle a pathological case where a single word alone is too long to fit on any line.
- "Extra spaces... distributed as evenly as possible... left slots get more spaces than right slots when it doesn't divide evenly" — this is a precise mathematical rule (essentially spreading a remainder across the leftmost gaps first) that must be implemented exactly as specified, not approximated.
- **A word is defined as a non-space character sequence** — words themselves never contain internal spaces, simplifying line-packing to just "does this word plus a separating space fit within the remaining width."

## 2. Recognition Signals

- "Pack words into lines up to a fixed width, then justify" → the specific signature of the **greedy line-packing + space-distribution simulation** technique: greedily fit as many words as possible onto each line, then compute exactly how to distribute the leftover width as spaces according to the specified rules.
- Distinguishing **three separate formatting cases** within the same problem — a normal fully-justified line (multiple words, not the last line), a single-word line (left-justified regardless of position), and the last line (always left-justified) — is itself the main recognition signal that this problem requires careful, deliberate branching logic rather than one uniform formula applied everywhere.
- Whenever "distribute N extra spaces evenly across K gaps, with any remainder going to the leftmost gaps first" appears — this specific quotient/remainder distribution pattern (`extraSpaces = totalSpaces / gaps`, `remainder = totalSpaces % gaps`, first `remainder` gaps get one additional space) is a reusable sub-technique worth recognizing on its own.

**Pattern:** Greedy Line-Packing with Space-Distribution Simulation (a two-phase technique: first greedily determine which words belong on each line based on available width, then compute and apply the correct space distribution for that specific line according to its type).

## 3. Core Idea

**Phase 1 — greedily determine line groupings:** Walk through `words` left to right, accumulating words onto the current line as long as they (plus at least one separating space per word already on the line) still fit within `maxWidth`. The moment adding the next word would exceed `maxWidth`, finalize the current line's word group and start a new line with that word.

**Phase 2 — justify each line according to its type:** For each finalized group of words on a line:
- **If it's the last line, or the line contains only a single word:** left-justify — join the words with single spaces, then pad the end with spaces until the total length is exactly `maxWidth`.
- **Otherwise (a normal, non-last line with 2+ words):** compute `totalSpaces = maxWidth - (sum of word lengths)` and `gaps = (number of words on this line) - 1` (the number of "slots" between words where extra spaces can go). Distribute `totalSpaces` as evenly as possible across the `gaps` slots: each gap gets `totalSpaces / gaps` spaces at minimum, and the first `totalSpaces % gaps` gaps (the leftmost ones) get one additional space each.

**Invariant:** For every fully-justified (non-last, multi-word) line, the total character count — sum of word lengths plus the sum of all space-slot widths — always equals exactly `maxWidth`, by construction of the quotient/remainder distribution; for left-justified lines (last line or single-word lines), the trailing padding is computed to make up exactly the remaining width after single-spacing the words.

## 4. Approach 1 — Straightforward but Less Careful (Building Lines with Manual Space Counting in a Loop)

**Logic:** Similar overall greedy grouping, but construct each justified line by looping and appending one space at a time in a round-robin fashion across the gaps, rather than directly computing the exact space count per gap upfront.

```java
public List<String> fullJustify(String[] words, int maxWidth) {
    List<String> result = new ArrayList<>();
    int i = 0;
    int n = words.length;

    while (i < n) {
        int lineLength = words[i].length();
        int j = i + 1;
        while (j < n && lineLength + 1 + words[j].length() <= maxWidth) {
            lineLength += 1 + words[j].length();
            j++;
        }

        int numWords = j - i;
        StringBuilder line = new StringBuilder();

        if (j == n || numWords == 1) {
            for (int k = i; k < j; k++) {
                line.append(words[k]);
                if (k < j - 1) {
                    line.append(' ');
                }
            }
            while (line.length() < maxWidth) {
                line.append(' ');
            }
        } else {
            int totalWordLength = 0;
            for (int k = i; k < j; k++) {
                totalWordLength += words[k].length();
            }
            int totalSpaces = maxWidth - totalWordLength;
            int gaps = numWords - 1;

            for (int k = i; k < j; k++) {
                line.append(words[k]);
                if (k < j - 1) {
                    int spacesThisGap = totalSpaces / gaps + (((k - i) < totalSpaces % gaps) ? 1 : 0);
                    for (int s = 0; s < spacesThisGap; s++) {
                        line.append(' ');
                    }
                }
            }
        }

        result.add(line.toString());
        i = j;
    }

    return result;
}
```

- **Time:** O(n) where `n` is the total number of characters across all words plus the output (each character is touched a small constant number of times).
- **Space:** O(n) for the output lines (unavoidable, since the output itself must contain every input character plus padding).

**Why it is presented as the less-preferred variant:** This version's inner space-appending loop (`for (int s = 0; s < spacesThisGap; s++) line.append(' ');`) recomputes `spacesThisGap` per gap in a slightly more verbose, character-by-character-append style rather than directly computing and appending whole space runs at once. It's fully correct and the same asymptotic complexity, but Approach 2's use of a dedicated space-building helper (e.g., `" ".repeat(count)`) is cleaner and less error-prone to get right on the first try, since it separates "how many spaces go here" from "append that many spaces" more explicitly.

## 5. Approach 2 — Optimized/Cleaner (Explicit Space-Count Computation, Direct Append)

**Algorithm:**
1. Use pointer `i` to track the start of the current line's word group. While `i < words.length`:
   - Greedily extend a pointer `j` forward from `i`, accumulating word lengths plus one mandatory space per word already added, stopping as soon as adding the next word would exceed `maxWidth`.
   - Let `numWords = j - i` be the count of words on this line.
   - **If this is the last line (`j == words.length`) or `numWords == 1`:** join the words with single spaces, then pad the end with spaces to reach exactly `maxWidth`.
   - **Otherwise:** compute `totalSpaces = maxWidth - (sum of word lengths in this group)` and `gaps = numWords - 1`; compute `spacesPerGap = totalSpaces / gaps` and `extraSpaces = totalSpaces % gaps`; build the line by appending each word followed by `spacesPerGap` spaces (plus one more if this gap index is among the first `extraSpaces` gaps).
   - Append the finished line to the result list, and advance `i = j`.
2. Return the result list.

```java
public List<String> fullJustify(String[] words, int maxWidth) {
    List<String> result = new ArrayList<>();
    int i = 0;
    int n = words.length;

    while (i < n) {
        int lineLength = words[i].length();
        int j = i + 1;
        while (j < n && lineLength + 1 + words[j].length() <= maxWidth) {
            lineLength += 1 + words[j].length();
            j++;
        }

        int numWords = j - i;
        boolean isLastLine = (j == n);

        StringBuilder line = new StringBuilder();

        if (isLastLine || numWords == 1) {
            for (int k = i; k < j; k++) {
                line.append(words[k]);
                if (k < j - 1) {
                    line.append(' ');
                }
            }
            appendSpaces(line, maxWidth - line.length());
        } else {
            int totalWordLength = 0;
            for (int k = i; k < j; k++) {
                totalWordLength += words[k].length();
            }
            int gaps = numWords - 1;
            int totalSpaces = maxWidth - totalWordLength;
            int spacesPerGap = totalSpaces / gaps;
            int extraSpaces = totalSpaces % gaps;

            for (int k = i; k < j; k++) {
                line.append(words[k]);
                if (k < j - 1) {
                    int gapIndex = k - i;
                    int spacesHere = spacesPerGap + (gapIndex < extraSpaces ? 1 : 0);
                    appendSpaces(line, spacesHere);
                }
            }
        }

        result.add(line.toString());
        i = j;
    }

    return result;
}

private void appendSpaces(StringBuilder sb, int count) {
    for (int k = 0; k < count; k++) {
        sb.append(' ');
    }
}
```

- **Time:** O(n) — every character (word content plus all inserted spaces) is touched a bounded constant number of times across the entire process.
- **Space:** O(n) auxiliary for building the output lines, which is unavoidable since the full justified output must be produced and returned.

**Why this is optimal:** Every character of the input and output must be examined/written at least once, so O(n) is a hard lower bound given the total input/output size — both approaches achieve this. The distinction between Approach 1 and 2 is code clarity, not asymptotic performance: separating "compute how many spaces belong in this gap" from "append that many spaces" via a small helper function makes the space-distribution logic easier to verify correct at a glance, and reduces the chance of an off-by-one error creeping into a combined loop-and-append expression.

## 6. Dry Run

Example: `words = ["This", "is", "an", "example", "of", "text", "justification."]`, `maxWidth = 16`.
Chosen because it's the canonical example, and it exercises all three line-formatting cases: a normal fully-justified multi-word line, another fully-justified line with uneven space distribution, and the final left-justified line.

**Line 1 grouping:** Start `i=0` ("This", length 4). Try adding "is" (2): `4+1+2=7 <= 16`, include. Try adding "an" (2): `7+1+2=10 <= 16`, include. Try adding "example" (7): `10+1+7=18 > 16`, stop. Group: `["This","is","an"]`, `j=3`, not last line, `numWords=3`.

`totalWordLength = 4+2+2 = 8`. `gaps = 2`. `totalSpaces = 16-8 = 8`. `spacesPerGap = 8/2 = 4`. `extraSpaces = 8%2 = 0`.

| Gap index (k-i) | spacesHere = 4 + (index<0?1:0) |
|-----------------|-------------------------------------|
| 0 (after "This") | 4 |
| 1 (after "is") | 4 |

Line 1: `"This" + "    " + "is" + "    " + "an"` = `"This    is    an"` (16 chars: This=4, 4 spaces=8, is=2, wait let me recount: "This"(4) + 4 spaces(4) = 8, + "is"(2)=10, + 4 spaces(4)=14, + "an"(2)=16). Matches expected `"This    is    an"`.

**Line 2 grouping:** Start `i=3` ("example", length 7). Try "of" (2): `7+1+2=10<=16`, include. Try "text" (4): `10+1+4=15<=16`, include. Try "justification." (14): `15+1+14=30>16`, stop. Group: `["example","of","text"]`, `j=6`, not last, `numWords=3`.

`totalWordLength=7+2+4=13`. `gaps=2`. `totalSpaces=16-13=3`. `spacesPerGap=3/2=1`. `extraSpaces=3%2=1`.

| Gap index | spacesHere = 1 + (index<1?1:0) |
|-----------|--------------------------------------|
| 0 (after "example") | 1+1=2 |
| 1 (after "of") | 1+0=1 |

Line 2: `"example" + "  " + "of" + " " + "text"` = `"example  of text"` (7+2+2+1+4=16). Matches expected.

**Line 3 grouping:** `i=6` ("justification.", length 14), `j=7=n`, last line, `numWords=1`.

Left-justify: `"justification."` (14 chars) + pad to 16 → append 2 trailing spaces → `"justification.  "`.

**Final answer:** `["This    is    an", "example  of text", "justification.  "]`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Manual per-space append loop | O(n) | O(n) | Correct; slightly more verbose space-appending logic |
| 2. Explicit space-count + helper append | O(n) | O(n) | Same complexity; cleaner separation of "compute" vs. "apply" for space distribution |

`n` denotes the total number of characters across all words plus the total output size (both proportional to the same overall problem size).

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| A line with only one word (not necessarily the last line) | Must be left-justified (padded on the right), not "fully justified" with spaces distributed across zero gaps (which would be a division-by-zero risk) | The explicit `numWords == 1` check routes this case to the left-justify branch, avoiding any attempt to compute `totalSpaces / gaps` with `gaps = 0` |
| The very last line, regardless of how many words it contains | Must always be left-justified with single spaces and trailing padding, never fully justified, even if it has multiple words | The explicit `isLastLine` (or `j == n`) check ensures this case always routes to the left-justify branch, overriding what would otherwise be the normal multi-word full-justification logic |
| A single word that exactly fills `maxWidth` on its own | Should occupy the entire line with no padding needed at all | The left-justify branch's padding loop (`appendSpaces(line, maxWidth - line.length())`) correctly appends zero spaces when the word already exactly fills the width, since `maxWidth - line.length()` evaluates to 0 in that case |
| Uneven space distribution where `totalSpaces` doesn't divide evenly by `gaps` (as in Line 2 of the dry run) | The leftmost gaps must receive the extra spaces, not the rightmost ones | The `gapIndex < extraSpaces` condition specifically checks gaps starting from index 0 (the leftmost), ensuring exactly the first `extraSpaces` gaps (out of the total `gaps`) receive one additional space each, matching the problem's explicit requirement |
| A line that greedily fits exactly one word before the next word would overflow, even though multiple words could theoretically fit if arranged differently | Greedy packing must correctly determine the line boundary based on cumulative length including mandatory single spaces between words | The inner `while (j < n && lineLength + 1 + words[j].length() <= maxWidth)` loop correctly accounts for the mandatory separating space when checking whether the next word fits, ensuring the greedy grouping never overflows `maxWidth` |
| `words.length == 1` (a single word total) | This word is simultaneously both the only word on its line AND the last line | Both the `isLastLine` and `numWords == 1` conditions are true simultaneously in this case, but since they're combined with `||` (either condition routes to the same left-justify branch), there's no conflict — the single word is correctly left-justified and padded to `maxWidth` |

## 9. Java Notes

- **`StringBuilder` for line construction:** essential for efficiently building each line character by character (words plus computed space runs) without the O(n²)-style cost of repeated `String` concatenation.
- **A small `appendSpaces` helper method:** separates the concern of "how many spaces to add" (computed via the quotient/remainder arithmetic) from "actually adding them," improving readability and reducing the chance of an off-by-one error compared to inlining the space-appending loop directly into the main line-building logic.
- **Integer division and modulo (`totalSpaces / gaps`, `totalSpaces % gaps`) for even distribution:** a standard, idiomatic way to implement "distribute N items across K buckets as evenly as possible, with the remainder going to the first buckets" — worth recognizing as a reusable technique beyond just this problem.
- **`List<String>` as the return type, built via `ArrayList`:** appropriate here since the number of output lines isn't known in advance and grows dynamically as the greedy packing process determines line boundaries.

## 10. Common Mistakes

- **Attempting to fully justify a single-word line (or the last line) using the `totalSpaces / gaps` formula without first checking for these special cases.** With `numWords == 1`, `gaps` would be 0, causing a division-by-zero error; and even if that were guarded against separately, the last line must never be fully justified regardless of word count, per the problem's explicit rule. Fix: always check `isLastLine || numWords == 1` first and route to the simpler left-justify-with-padding logic before ever computing gap-based space distribution.
- **Distributing the remainder spaces to the rightmost gaps instead of the leftmost ones.** The problem explicitly states extra (uneven) spaces go to the left slots first, not the right. Fix: use `gapIndex < extraSpaces` (checking against the leftmost `extraSpaces` gap indices, starting from 0) rather than checking from the right end.
- **Miscounting the mandatory space when determining whether the next word fits during greedy line-packing.** Forgetting to account for the separating space between the current line's accumulated length and the next candidate word can cause a line to either overflow `maxWidth` or under-pack when it shouldn't. Fix: always check `lineLength + 1 + words[j].length() <= maxWidth` (the `+1` accounts for the mandatory space before the next word), not just `lineLength + words[j].length() <= maxWidth`.
- **Forgetting that the last line still needs trailing padding to reach exactly `maxWidth`, not just single spaces between its words.** Simply joining the last line's words with single spaces (without additional padding) would produce a line shorter than `maxWidth`, violating the requirement that every line be exactly `maxWidth` characters. Fix: after single-spacing the last line's words, explicitly pad the end with additional spaces until the total length equals `maxWidth`.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Pack words into fixed-width lines with justified spacing, special-casing the last line and single-word lines → greedily group words per line based on available width, then either left-justify-and-pad (last line or single word) or distribute spaces evenly with leftmost gaps getting any remainder (normal multi-word lines)."
- **90-second explanation:** "I process the words greedily, building up each line by adding words as long as they (plus a mandatory single space before each) still fit within maxWidth — the moment the next word wouldn't fit, that line's word group is finalized. Then, for each line, I check two special cases first: if it's the very last line, or if it only contains a single word, I left-justify it — joining with single spaces and padding the end with extra spaces to reach exactly maxWidth. Otherwise, for a normal line with multiple words that isn't the last one, I compute the total leftover space after accounting for all the word lengths, divide that evenly among the gaps between words, and — since it might not divide evenly — I give any remainder spaces to the leftmost gaps first, exactly as the problem specifies. This is fundamentally a simulation problem rather than one requiring a clever algorithmic insight — the challenge is correctly implementing all the specific formatting rules, especially the two exceptions to full justification."
- **Related problems using this pattern:**
  - LeetCode 6 — Zigzag Conversion (different specific technique, but similarly a direct simulation problem)
  - LeetCode 5 — (unrelated) — no strong direct relative; this problem is fairly unique in the "text formatting simulation" category
  - Related conceptually to general "word wrap" / "greedy line-breaking" algorithms used in text editors and typesetting systems

## 12. Recall Questions

**Q:** Why must both "the last line" and "a line with only one word" be special-cased to left-justification, rather than using the general full-justification formula?
**A:** The last line is explicitly required by the problem to always be left-justified regardless of word count, and a single-word line has zero gaps between words, which would make the general formula's division by the number of gaps undefined (division by zero).

**Q:** Why does the greedy line-packing check need to include a `+1` for the mandatory space when determining whether the next word still fits on the current line?
**A:** Every word on a line (except the very first) requires at least one separating space before it, so failing to account for that space in the fits-on-the-line check could either allow a line to exceed maxWidth or incorrectly reject a word that would have actually fit once the space is properly accounted for.

**Q:** Why do the leftmost gaps receive the extra (remainder) spaces rather than the rightmost gaps when the total space doesn't divide evenly among gaps?
**A:** This is simply the specific rule the problem defines for resolving the ambiguity of uneven distribution — there's no other correct answer for a given line's justification besides following this explicitly stated convention.

**Q:** What would go wrong if the last line were joined with single spaces between words but no additional trailing padding was applied?
**A:** The resulting line would be shorter than the required maxWidth (since single-spacing alone doesn't guarantee reaching the target width unless the words happen to exactly fill it), violating the requirement that every output line be exactly maxWidth characters long.

**Q:** How does the algorithm ensure that a fully-justified (non-last, multi-word) line's total character count always exactly equals maxWidth?
**A:** By construction: the total spaces to distribute is calculated as maxWidth minus the sum of the word lengths on that line, and that exact total is then fully distributed across the gaps (via the quotient plus the remainder allocation), so summing the word lengths and all the distributed space counts together always reconstructs exactly maxWidth.

## 13. Final Code

```java
public List<String> fullJustify(String[] words, int maxWidth) {
    List<String> result = new ArrayList<>();
    int i = 0;
    int n = words.length;

    while (i < n) {
        int lineLength = words[i].length();
        int j = i + 1;
        while (j < n && lineLength + 1 + words[j].length() <= maxWidth) {
            lineLength += 1 + words[j].length();
            j++;
        }

        int numWords = j - i;
        boolean isLastLine = (j == n);

        StringBuilder line = new StringBuilder();

        if (isLastLine || numWords == 1) {
            for (int k = i; k < j; k++) {
                line.append(words[k]);
                if (k < j - 1) {
                    line.append(' ');
                }
            }
            appendSpaces(line, maxWidth - line.length());
        } else {
            int totalWordLength = 0;
            for (int k = i; k < j; k++) {
                totalWordLength += words[k].length();
            }
            int gaps = numWords - 1;
            int totalSpaces = maxWidth - totalWordLength;
            int spacesPerGap = totalSpaces / gaps;
            int extraSpaces = totalSpaces % gaps;

            for (int k = i; k < j; k++) {
                line.append(words[k]);
                if (k < j - 1) {
                    int gapIndex = k - i;
                    int spacesHere = spacesPerGap + (gapIndex < extraSpaces ? 1 : 0);
                    appendSpaces(line, spacesHere);
                }
            }
        }

        result.add(line.toString());
        i = j;
    }

    return result;
}

private void appendSpaces(StringBuilder sb, int count) {
    for (int k = 0; k < count; k++) {
        sb.append(' ');
    }
}
```

## 14. Self-Test

You have an array of words and a target width `maxWidth`. Pack words greedily into lines of exactly `maxWidth` characters, fully justifying each line by distributing spaces as evenly as possible (leftmost gaps get any remainder), except the last line and any single-word line, which are left-justified with trailing padding instead. Think through the three distinct formatting cases and why each needs its own handling.
