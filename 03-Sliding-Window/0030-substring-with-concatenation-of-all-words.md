---
problem: 30 - Substring with Concatenation of All Words
url: https://leetcode.com/problems/substring-with-concatenation-of-all-words/
difficulty: Hard
section: String / Sliding Window
patterns: [sliding-window, hashmap]
data_structures: [hashmap, string]
time: O(n * wordLen)
space: O(m * wordLen)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** a string `s` and an array `words` of strings, all the same length.

**Required:** return every starting index in `s` where a contiguous substring is formed by concatenating **all** the words in `words`, in **any order** (each word used exactly as many times as it appears in `words`, including duplicates).

**Constraints that matter:**
- `1 <= s.length <= 10^4` and `1 <= words.length <= 5000`, `1 <= words[i].length <= 30` — the total concatenated length (`words.length * wordLen`) can be up to `s.length`, but the individual pieces are small; this shape (many small fixed-length pieces glued together) is the signal that a naive substring-comparison approach will redo huge amounts of overlapping work.
- `s` and `words[i]` consist of lowercase English letters only — no need to worry about mixed case or non-letter characters, but duplicates within `words` are allowed and must be matched by **count**, not by set membership.
- All words share the same length — this is what makes it possible to chop `s` into fixed-size blocks and compare block-by-block instead of trying arbitrary substring lengths.

## 2. Recognition Signals

- "Find substrings formed by concatenating **all** elements of a word list, in **any order**" → this is an anagram/permutation-matching problem lifted from single characters (like Find All Anagrams in a String) up to whole fixed-length words.
- Fixed-length building blocks + need to match exact multiset counts → **HashMap of word → frequency**, compared against a **sliding window's own frequency map**.
- The window size is fixed (`numWords * wordLen`), which is the tell for a **fixed-size sliding window**, but because the window is measured in whole words rather than characters, the window must advance in **word-length jumps**, and multiple interleaved starting offsets (`0` through `wordLen - 1`) must each be scanned independently to catch every alignment.
- Named pattern: **Sliding Window over Fixed-Length Blocks with Frequency Matching** (generalized anagram-substring pattern).

## 3. Core Idea

Since every word has the same length `wordLen`, any valid concatenated substring must be composed of consecutive `wordLen`-sized chunks starting at some index `left` where `left` and every chunk boundary are all multiples of `wordLen` apart from a shared starting offset. So for each of the `wordLen` possible starting offsets (`0, 1, ..., wordLen - 1`), slide a window in units of whole words: extend `right` by one word at a time, track how many words in the window match the required frequency, and shrink `left` (also by whole words) whenever a word's count in the window exceeds what's needed or an entirely unrecognized word appears.

**Why it's correct:** a match at position `left` must contain exactly `numWords` words, each drawn from `s` at successive `wordLen`-aligned positions starting at `left`. By fixing the offset (`left mod wordLen`) per outer pass, every window boundary considered is a valid candidate starting point, so no valid answer is skipped, and no invalid (misaligned) position is wasted time on.

**Invariant:** within one offset's scan, the window `[left, right)` (in word units) always contains a sub-multiset of the required word counts — never more of any word than `words` actually contains — so a window becomes an answer exactly when the number of words in the window equals `numWords`.

## 4. Approach 1 — Brute Force

1. Build a frequency map `wordCount` from `words`.
2. For every starting index `i` in `s` where a full concatenation could fit (`i` from `0` to `s.length() - totalLen`), copy `wordCount` into a fresh working map.
3. Walk `numWords` consecutive `wordLen`-sized chunks starting at `i`; for each chunk, decrement its count in the working map. If a chunk isn't present with a positive count, this starting index fails — stop early.
4. If all `numWords` chunks were successfully consumed, record `i` as a valid start.

```java
public List<Integer> findSubstringBruteForce(String s, String[] words) {
    List<Integer> result = new ArrayList<>();
    if (words.length == 0 || words[0].isEmpty()) {
        return result;
    }

    int wordLen = words[0].length();
    int numWords = words.length;
    int totalLen = wordLen * numWords;
    int n = s.length();

    Map<String, Integer> wordCount = new HashMap<>();
    for (String word : words) {
        wordCount.merge(word, 1, Integer::sum);
    }

    for (int i = 0; i + totalLen <= n; i++) {
        Map<String, Integer> seen = new HashMap<>(wordCount.size());
        int j = 0;
        for (; j < numWords; j++) {
            int start = i + j * wordLen;
            String chunk = s.substring(start, start + wordLen);
            Integer required = wordCount.get(chunk);
            if (required == null) {
                break;
            }
            int used = seen.merge(chunk, 1, Integer::sum);
            if (used > required) {
                break;
            }
        }
        if (j == numWords) {
            result.add(i);
        }
    }

    return result;
}
```

**Time:** O(n * numWords * wordLen) in the worst case — for each of ~n starting indices, up to `numWords` chunk extractions and comparisons, each costing O(wordLen). Since `numWords * wordLen = totalLen <= n`, this is roughly O(n * totalLen), which can approach O(n²) when `totalLen` is large.
**Space:** O(m * wordLen) auxiliary for `wordCount` and the per-index `seen` map, where `m` is the number of distinct words.

This is not optimal: it re-examines overlapping chunks from scratch at every starting index, discarding all the matching work done for the previous index even though the two windows overlap almost entirely.

## 5. Approach 2 — Optimized (Sliding Window per Word-Length Offset)

1. Build `wordCount`: a frequency map of each word in `words`.
2. Compute `wordLen`, `numWords`, `totalLen = wordLen * numWords`, and `n = s.length()`. If `n < totalLen`, return an empty list immediately.
3. For each `offset` from `0` to `wordLen - 1` (these are the only distinct word-alignments possible):
   - Initialize `left = offset`, `count = 0` (words currently matched in the window), and an empty `windowCount` map.
   - Slide `right` from `offset` to `n - wordLen` in steps of `wordLen`:
     - Extract `word = s.substring(right, right + wordLen)`.
     - If `word` is not a required word at all, the window can't include it: reset — clear `windowCount`, set `count = 0`, and move `left` to `right + wordLen`.
     - Otherwise, add `word` to `windowCount` and increment `count`. If `word`'s count in the window now exceeds its required count, shrink from `left` (removing words one at a time, decrementing `count`) until it no longer exceeds.
     - If `count == numWords`, the window `[left, right + wordLen)` is a full match: record `left`, then shrink by exactly one word from the left (removing `s.substring(left, left+wordLen)` and advancing `left`) to continue scanning for overlapping matches.
4. Return all recorded starting indices.

```java
public List<Integer> findSubstring(String s, String[] words) {
    List<Integer> result = new ArrayList<>();
    if (words.length == 0 || words[0].isEmpty()) {
        return result;
    }

    int wordLen = words[0].length();
    int numWords = words.length;
    int totalLen = wordLen * numWords;
    int n = s.length();
    if (n < totalLen) {
        return result;
    }

    Map<String, Integer> wordCount = new HashMap<>();
    for (String word : words) {
        wordCount.merge(word, 1, Integer::sum);
    }

    for (int offset = 0; offset < wordLen; offset++) {
        int left = offset;
        int count = 0;
        Map<String, Integer> windowCount = new HashMap<>();

        for (int right = offset; right + wordLen <= n; right += wordLen) {
            String word = s.substring(right, right + wordLen);

            if (!wordCount.containsKey(word)) {
                windowCount.clear();
                count = 0;
                left = right + wordLen;
                continue;
            }

            windowCount.merge(word, 1, Integer::sum);
            count++;

            while (windowCount.get(word) > wordCount.get(word)) {
                String leftWord = s.substring(left, left + wordLen);
                windowCount.merge(leftWord, -1, Integer::sum);
                count--;
                left += wordLen;
            }

            if (count == numWords) {
                result.add(left);
                String leftWord = s.substring(left, left + wordLen);
                windowCount.merge(leftWord, -1, Integer::sum);
                count--;
                left += wordLen;
            }
        }
    }

    return result;
}
```

**Time:** O(n * wordLen) — for each of `wordLen` offsets, the inner window scan visits O(n / wordLen) positions, each doing O(wordLen) work for substring extraction and O(1) amortized map work; `wordLen` offsets × (n / wordLen) positions × O(wordLen) per position = O(n * wordLen) total.
**Space:** O(m * wordLen) auxiliary, where `m` is the number of distinct words in `words` — for `wordCount` and the per-offset `windowCount` map (each string key costs O(wordLen)).

This is optimal for the standard approach to this problem: each offset's scan reuses overlapping window information via the frequency-count shrink/grow logic instead of recomputing from scratch, and every character position is visited a bounded number of times across all offsets combined, avoiding the brute force's repeated re-scanning of the same substring.

## 6. Dry Run

`s = "barfoothefoobarman"`, `words = ["foo","bar"]` → `wordLen = 3`, `numWords = 2`, `totalLen = 6`, `wordCount = {foo:1, bar:1}`

Offset `0`:

| right (word) | in wordCount? | windowCount after | count | shrink? | match? | left after |
|---|---|---|---|---|---|---|
| 0 ("bar") | yes | {bar:1} | 1 | no | no | 0 |
| 3 ("foo") | yes | {bar:1,foo:1} | 2 | no | **yes** → record left=0 | remove "bar" at 0 → left=3, count=1 |
| 6 ("the") | no | cleared | 0 | reset left=9 | no | 9 |
| 9 ("foo") | yes | {foo:1} | 1 | no | no | 9 |
| 12 ("bar") | yes | {foo:1,bar:1} | 2 | no | **yes** → record left=9 | remove "foo" at 9 → left=12, count=1 |
| 15 ("man") | no | cleared | 0 | reset left=18 | no | 18 (loop ends, 18+3 > 18) |

Offset `1` and `2` scan the remaining misaligned windows and find no matches for this example (all valid concatenations happen to be word-aligned at offset 0 here).

Exit condition: all offsets `0` to `wordLen - 1` have been fully scanned. Final answer: `[0, 9]`, matching the expected output.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute Force | O(n * totalLen) worst case | O(m * wordLen) aux | Re-scans overlapping chunks from scratch at every index |
| Sliding Window per Offset | O(n * wordLen) | O(m * wordLen) aux | `wordLen` independent linear scans, each reusing window state |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| `s.length() < totalLen` | No concatenation can possibly fit | Early return of an empty list right after computing `totalLen` |
| Duplicate words in `words` (e.g. `["word","good","best","word"]`) | Must match by **count**, not just presence | `wordCount` and `windowCount` both use frequency maps, so duplicates are required exactly as many times as they appear |
| A chunk that matches a required word but the window already has enough of it | Window would otherwise "over-collect" that word and miss the true start | The `while (windowCount.get(word) > wordCount.get(word))` shrink loop removes from the left until the count is back in bounds |
| A chunk that isn't in `words` at all | Window must be invalidated entirely, not just shrunk | `windowCount.clear()`, `count = 0`, and `left` jumps past the offending chunk |
| Overlapping valid matches (e.g. `"barfoofoobar"` with `words=["bar","foo"]` type patterns) | Sliding window must find matches that start close together, not just the first one | After recording a match, only one word is shrunk off the left (not the whole window reset), so the scan continues correctly from the very next possible start |
| `words` containing a single word (`numWords == 1`) | Degenerates to "find all occurrences of one word" | Works identically: `count` reaches `numWords == 1` after matching a single chunk, so it immediately records and continues |

## 9. Java Notes

- `s.substring(start, end)` in modern Java (post Java 7 update) copies the underlying char array rather than sharing it, so each extraction is a genuine O(wordLen) operation — this is why the complexity analysis explicitly multiplies by `wordLen` rather than treating substring extraction as O(1).
- `Map.merge(key, 1, Integer::sum)` is the idiomatic way to increment-or-initialize a counter in one call, avoiding a separate `containsKey`/`put` pair; `merge` with a negative delta (`-1`) works the same way for decrementing.
- `HashMap.containsKey` followed by `HashMap.get` is technically two lookups; for a hot loop, `getOrDefault(key, 0)` collapses this to one lookup where a sentinel default is acceptable.
- Building a fresh `HashMap` per offset (`windowCount`) rather than reusing and clearing one map across offsets is a deliberate simplicity choice here; either is correct, but reuse-and-clear can reduce garbage collection pressure in a tight loop over many offsets.
- Recursion is not used in this solution, so there's no stack-depth concern; the algorithm is purely iterative.

## 10. Common Mistakes

- Scanning `s` with a plain single-pass sliding window over **characters** instead of one pass per word-length offset — this silently misses valid concatenations that don't happen to start at an index divisible by `wordLen` relative to index 0. Fix: run the scan independently for every offset `0` through `wordLen - 1`.
- Using a `Set<String>` instead of a frequency map to track required words — this loses the ability to detect when `words` has duplicates, causing both false positives (matching without enough copies) and false negatives (rejecting valid windows that legitimately reuse a word). Fix: always use count-based maps for this problem.
- After a full match is found, resetting the whole window (`left = right + wordLen`, clear `windowCount`) instead of shrinking by just one word — this skips overlapping matches that start one word later. Fix: only remove the single leftmost word and advance `left` by one word length, then keep scanning.
- Forgetting the `while` shrink loop when a word's window count exceeds its required count (only handling the "word not in wordCount at all" case) — this fails on inputs where a word is required once but the window has collected it twice due to natural repetition in `s`. Fix: always shrink from the left until every word's window count is `<=` its required count.

## 11. Interview Takeaway

- **Trigger sentence:** "Find substrings that are exact concatenations of all words in a list (any order, duplicates allowed, all same length) → sliding window over fixed-length word blocks, one scan per starting alignment offset, with frequency-map matching."
- **90-second explanation:** "Since every word is the same length, a valid match is just a sequence of word-sized blocks starting at some aligned position. I run a separate sliding-window scan for each of the `wordLen` possible starting offsets, because a match could begin at any character position modulo the word length. Within each scan, I extend the window one word at a time, tracking how many words currently in the window match what's required, using a frequency map so duplicates are counted correctly. If a block isn't a required word at all, I reset the window past it. If a block's count in the window exceeds what's needed, I shrink from the left until it's back in range. Whenever the window contains exactly the right total count of words, I record the start and shrink by one word to keep looking for overlapping matches."
- **Related problems:** 76 (Minimum Window Substring), 438 (Find All Anagrams in a String), 567 (Permutation in String), 3 (Longest Substring Without Repeating Characters).

## 12. Recall Questions

**Q:** Why does the algorithm need to run a separate scan for each of the `wordLen` starting offsets instead of one single scan?
**A:** A valid concatenation could begin at any character index in `s`, but within one scan the window only advances in whole `wordLen` steps, so a single scan can only ever discover matches whose starting index shares the same remainder modulo `wordLen`; covering all remainders requires one scan per offset.

**Q:** Why use a frequency map (`Map<String,Integer>`) instead of a `Set<String>` for the required words?
**A:** `words` can contain duplicate entries that must each be matched by an equal count in the substring; a `Set` would collapse duplicates and either over-accept or under-reject matches involving repeated words.

**Q:** Why does the shrink-by-one-word step after a full match (rather than a full window reset) matter for correctness?
**A:** Overlapping valid matches can start one word apart from each other; resetting the entire window after a match would skip past a starting position that is itself a valid concatenation.

**Q:** What does the `while (windowCount.get(word) > wordCount.get(word))` loop protect against?
**A:** It protects against the window "over-collecting" a word — having more copies of that word in the current window than `words` actually requires — which would otherwise prevent the window from ever reaching a valid exact match.

**Q:** Why is resetting the window (clearing `windowCount`, zeroing `count`, jumping `left`) necessary when an encountered block isn't a required word at all?
**A:** A block that isn't in `wordCount` can never be part of any valid concatenation, so any window that would have to include it is automatically invalid; the window must start fresh immediately after that block rather than trying to shrink around it.

**Q:** Why is the total time complexity expressed as O(n * wordLen) rather than simply O(n)?
**A:** Each of the `wordLen` offset scans does O(n / wordLen) window steps, but each step involves an O(wordLen) substring extraction (and map operations proportional to word length via hashing/equality), so the offset count and the per-step cost both scale with `wordLen`, multiplying out to O(n * wordLen) overall.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public List<Integer> findSubstring(String s, String[] words) {
        List<Integer> result = new ArrayList<>();
        if (words.length == 0 || words[0].isEmpty()) {
            return result;
        }

        int wordLen = words[0].length();
        int numWords = words.length;
        int totalLen = wordLen * numWords;
        int n = s.length();
        if (n < totalLen) {
            return result;
        }

        Map<String, Integer> wordCount = new HashMap<>();
        for (String word : words) {
            wordCount.merge(word, 1, Integer::sum);
        }

        for (int offset = 0; offset < wordLen; offset++) {
            int left = offset;
            int count = 0;
            Map<String, Integer> windowCount = new HashMap<>();

            for (int right = offset; right + wordLen <= n; right += wordLen) {
                String word = s.substring(right, right + wordLen);

                if (!wordCount.containsKey(word)) {
                    windowCount.clear();
                    count = 0;
                    left = right + wordLen;
                    continue;
                }

                windowCount.merge(word, 1, Integer::sum);
                count++;

                while (windowCount.get(word) > wordCount.get(word)) {
                    String leftWord = s.substring(left, left + wordLen);
                    windowCount.merge(leftWord, -1, Integer::sum);
                    count--;
                    left += wordLen;
                }

                if (count == numWords) {
                    result.add(left);
                    String leftWord = s.substring(left, left + wordLen);
                    windowCount.merge(leftWord, -1, Integer::sum);
                    count--;
                    left += wordLen;
                }
            }
        }

        return result;
    }
}
```

## 14. Self-Test

Given a string `s` and a list of equal-length words, find every starting index where a substring is an exact concatenation of all the words (any order, duplicates matched by count). Re-derive: since blocks are fixed-length, run one sliding-window scan per starting offset (`0` to `wordLen-1`); within each scan, grow the window one word at a time using a frequency map, reset the window entirely on an unrecognized word, shrink from the left when a word is over-represented, and record (then shrink by one word) whenever the window's total word count hits the required count.
