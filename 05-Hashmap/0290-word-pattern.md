---
problem: 290 - Word Pattern
url: https://leetcode.com/problems/word-pattern/
difficulty: Easy
section: String / Hash Table
patterns: [hashmap, bijective-mapping]
data_structures: [hashmap, array]
time: O(n + m)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** a `pattern` string of letters, and a string `s` of space-separated words.

**Required:** determine whether `s` follows `pattern` — there must be a **bijection** between each letter of `pattern` and each unique word of `s`: every letter always maps to the same word, every word always maps to the same letter, and the number of words in `s` must exactly match the number of characters in `pattern`.

**Constraints that matter:**
- `1 <= pattern.length <= 300` and `1 <= s.length <= 3000` — small enough that performance is not the bottleneck; the challenge here is correctness of the bijection logic, not efficiency.
- `pattern` contains only lowercase letters; `s` contains only lowercase letters and single spaces with no leading/trailing spaces and single-space word separation — this guarantees `s.split(" ")` cleanly tokenizes into words with no empty-string artifacts to filter out.
- The word count in `s` must match `pattern.length()` **exactly** — if they differ, no bijection is even possible regardless of content, so this is a fast early rejection.

## 2. Recognition Signals

- "Bijection between a letter and a word" + "no two letters map to the same word, no two words map to the same letter" → this is the **exact same bijective-mapping shape as Isomorphic Strings (LeetCode 205)**, just applied to characters-to-words instead of characters-to-characters.
- Explicit mention of "each maps to exactly one" in both directions is the direct textual cue for needing **two synchronized maps**, not one.
- A string that needs tokenizing by whitespace before comparison → split `s` into words first, then treat the problem as a parallel-array comparison between `pattern.charAt(i)` and `words[i]`.
- Named pattern: **Bijective Mapping** (character ↔ word variant).

## 3. Core Idea

Split `s` into words. Walk `pattern` and `words` together, position by position, maintaining two maps: letter → word, and word → letter. At each position, if either mapping already exists, it must agree with what's seen now; otherwise, record both directions.

**Why it's correct:** identical reasoning to isomorphic strings — a valid "follows the pattern" relationship is a bijection between the pattern's letters and the string's words. Checking only `letterToWord` consistency catches a letter mapping to two different words, but misses two different letters mapping to the same word; the reverse map `wordToLetter` is required to catch that second violation.

**Invariant:** after processing position `i`, `letterToWord` and `wordToLetter` are exact inverses of each other restricted to the letters/words seen in `pattern[0..i]` and `words[0..i]`.

## 4. Approach 1 — Brute Force

1. Split `s` into `words`. If `words.length != pattern.length()`, return `false`.
2. For every pair of indices `(i, j)` with `i < j`, check that the "same letter" relationship in `pattern` matches the "same word" relationship in `words`: `(pattern.charAt(i) == pattern.charAt(j))` must equal `words[i].equals(words[j])`.
3. If this ever mismatches, return `false`; otherwise return `true`.

```java
public boolean wordPatternBruteForce(String pattern, String s) {
    String[] words = s.split(" ");
    if (words.length != pattern.length()) {
        return false;
    }

    int n = pattern.length();
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            boolean samePatternChar = pattern.charAt(i) == pattern.charAt(j);
            boolean sameWord = words[i].equals(words[j]);
            if (samePatternChar != sameWord) {
                return false;
            }
        }
    }
    return true;
}
```

**Time:** O(n²) for the pairwise comparison (where `n = pattern.length()`), plus O(m) for splitting `s` (where `m = s.length()`) and O(n) `String.equals` calls costing up to O(word length) each, so effectively O(n² · L) where `L` is average word length in the worst case.
**Space:** O(m) for the `words` array from splitting.

This is not optimal: it redundantly re-derives the same relationship across overlapping pairs, and although `n <= 300` keeps this technically fast enough to pass here, it doesn't scale and misses the cleaner single-pass structure the problem is really testing.

## 5. Approach 2 — Optimized (Two Synchronized Hash Maps, Single Pass)

1. Split `s` into `words` using `s.split(" ")`. If `words.length != pattern.length()`, return `false` immediately (bijection is impossible if the counts differ).
2. Create `letterToWord: Map<Character, String>` and `wordToLetter: Map<String, Character>`.
3. Iterate `i` from `0` to `pattern.length() - 1`, letting `c = pattern.charAt(i)` and `word = words[i]`:
   - If `letterToWord` contains `c`: it must map to `word`; if not, return `false`.
   - Else if `wordToLetter` contains `word`: it means some other letter already claimed this word; return `false`.
   - Else: record `letterToWord.put(c, word)` and `wordToLetter.put(word, c)`.
4. If the loop completes without conflict, return `true`.

```java
public boolean wordPattern(String pattern, String s) {
    String[] words = s.split(" ");
    if (words.length != pattern.length()) {
        return false;
    }

    Map<Character, String> letterToWord = new HashMap<>();
    Map<String, Character> wordToLetter = new HashMap<>();

    for (int i = 0; i < pattern.length(); i++) {
        char c = pattern.charAt(i);
        String word = words[i];

        if (letterToWord.containsKey(c)) {
            if (!letterToWord.get(c).equals(word)) {
                return false;
            }
        } else if (wordToLetter.containsKey(word)) {
            return false; // word already claimed by a different letter
        } else {
            letterToWord.put(c, word);
            wordToLetter.put(word, c);
        }
    }

    return true;
}
```

**Time:** O(n + m) — splitting `s` costs O(m) where `m = s.length()`; the main loop runs `n = pattern.length()` times, with O(1) amortized map operations (word comparisons cost O(word length), but total word-character work across the whole loop is bounded by O(m)).
**Space:** O(n) — at most `n` distinct letters (bounded by 26, but expressed relative to pattern length) and at most `n` distinct words stored across both maps, plus O(m) for the `words` array itself.

This is optimal: every character of `pattern` and every word of `s` must be examined at least once, so O(n + m) is the best possible time, and two maps are the minimal structure needed to enforce a two-way bijection in a single pass.

## 6. Dry Run

`pattern = "abba"`, `s = "dog cat cat dog"` → `words = ["dog","cat","cat","dog"]`

| i | c, word | letterToWord has c? | wordToLetter has word? | action | maps after |
|---|---|---|---|---|---|
| 0 | 'a',"dog" | no | no | record a→dog, dog→a | L2W={a:dog}, W2L={dog:a} |
| 1 | 'b',"cat" | no | no | record b→cat, cat→b | L2W={a:dog,b:cat}, W2L={dog:a,cat:b} |
| 2 | 'b',"cat" | yes, maps to "cat" | — | matches, no change | unchanged |
| 3 | 'a',"dog" | yes, maps to "dog" | — | matches, no change | unchanged |

Exit: loop completes all 4 positions without conflict. Final answer: `true`, matching the expected output.

Contrast with `pattern = "abba"`, `s = "dog cat cat fish"`: at `i = 3`, `c = 'a'` already maps to `"dog"` in `letterToWord`, but the word at this position is `"fish"` — mismatch → return `false`, matching the expected output.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute Force (pairwise check) | O(n² · L) | O(m) for split array | L = average word length; redundant re-derivation across pairs |
| Two Hash Maps, Single Pass | O(n + m) | O(n) for maps + O(m) for split array | n = pattern length, m = s length |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Word count doesn't match pattern length (e.g. `pattern="abba"`, `s="dog cat cat dog too"`) | No bijection possible regardless of content | Early `words.length != pattern.length()` check returns `false` before any mapping logic runs |
| Two different letters map to the same word (e.g. `pattern="ab"`, `s="dog dog"`) | Classic bijection violation testing the reverse direction | `wordToLetter.containsKey(word)` check catches this when a second letter tries to claim an already-used word |
| Same letter maps to two different words (e.g. `pattern="aa"`, `s="dog cat"`) | Simpler, single-direction violation | `letterToWord.containsKey(c)` check catches this directly |
| All letters distinct, all words distinct (e.g. `pattern="abc"`, `s="dog cat fish"`) | Trivial bijection, easy to satisfy but easy to over-complicate | Straightforward: each position records a brand-new pair with no conflicts |
| Repeated pattern with matching repeated words (e.g. `pattern="aaaa"`, `s="dog dog dog dog"`) | Must confirm single letter maps consistently to single repeated word | Every position after the first just re-validates the existing `a → dog` mapping |
| Single-character pattern, single-word `s` | Minimal valid input | One iteration, records one pair, returns `true` |

## 9. Java Notes

- `s.split(" ")` is safe here specifically because the constraints guarantee `s` has no leading/trailing spaces and words separated by exactly a single space — with looser input (multiple spaces, leading/trailing whitespace), `split(" ")` could produce empty-string elements and would need `split("\\s+")` with a trim, or `String.trim().split(" +")`.
- `letterToWord.get(c).equals(word)` (not `==`) is essential since `word` and the stored value are `String` objects — using `==` would compare references rather than content and could produce false negatives for logically equal but distinct `String` instances (e.g., ones not interned the same way).
- `Map<Character, String>` and `Map<String, Character>` are two distinct generic map types; Java has no built-in bidirectional map (`BiMap`), so maintaining both directions manually is the standard idiom.
- This problem is structurally identical to Isomorphic Strings (205) except the "characters" on one side are whole `String` words rather than single `char` values — recognizing that similarity is often faster than re-deriving the logic from scratch.

## 10. Common Mistakes

- Forgetting the `words.length != pattern.length()` check and instead only looping up to `Math.min(words.length, pattern.length())` — this can produce a false `true` when `s` has extra unmatched words or `pattern` has extra unmatched letters that the truncated loop never inspects. Fix: check lengths match exactly before starting the main loop, since a length mismatch alone always means "not following the pattern."
- Comparing words with `==` instead of `.equals()` — since words come from `String.split()`, they're likely to be distinct `String` objects even when equal in content, so `==` often fails even for genuinely matching words. Fix: always use `.equals()` for `String` content comparison.
- Checking only `letterToWord` and omitting the `wordToLetter` reverse check — misses the case where two different pattern letters map to the same word, incorrectly returning `true`. Fix: always maintain and check both directions, exactly as in Isomorphic Strings.
- Using `pattern.length()` as the loop bound without having verified `words.length` matches it first — risks an `ArrayIndexOutOfBoundsException` if `words` is shorter than `pattern`. Fix: the length-equality guard at the top prevents this entirely.

## 11. Interview Takeaway

- **Trigger sentence:** "Bijection between characters of one sequence and elements (words) of another → two synchronized hash maps built in one pass — same shape as Isomorphic Strings, applied at the word level."
- **90-second explanation:** "First I split `s` into words and immediately check the word count matches the pattern length, since a bijection is impossible otherwise. Then I walk the pattern and the words together, maintaining two maps: one from each letter to its word, and one from each word back to its letter. At each position, if the letter's already mapped, I confirm it still points to the same word; if the word wasn't already claimed by some other letter, I record both directions together. Checking both directions is essential — it's exactly the same bijection-verification structure as Isomorphic Strings, just with whole words standing in for characters on one side."
- **Related problems:** 205 (Isomorphic Strings), 890 (Find and Replace Pattern), 49 (Group Anagrams).

## 12. Recall Questions

**Q:** Why is this problem structurally identical to Isomorphic Strings, and what's the one meaningful difference?
**A:** Both require verifying a bijection maintained via two synchronized maps built in a single pass; the only real difference is that one side of the mapping here is a whole `String` word instead of a single `char`, which just changes the map's value/key type and requires `.equals()` instead of `==` for comparisons.

**Q:** Why must `words.length` be checked against `pattern.length()` before the main loop runs, rather than relying on the loop itself to catch a mismatch?
**A:** If the counts differ, a valid bijection is impossible regardless of content, and looping only up to the shorter length (or risking an index-out-of-bounds by looping too far) would either miss real mismatches at the end or crash — an explicit upfront length check is the correct, safe way to reject immediately.

**Q:** Why is `.equals()` required instead of `==` when comparing words?
**A:** Words produced by `String.split()` are typically separate `String` object instances even when their content matches, so `==` compares object identity rather than the character content the problem actually cares about.

**Q:** What specific violation does the `wordToLetter.containsKey(word)` check catch that `letterToWord` alone would miss?
**A:** It catches the case where two different pattern letters both try to map to the same word — `letterToWord` alone only verifies that a given letter consistently maps to the same word, but says nothing about whether that word has already been claimed by a different letter.

**Q:** If `pattern = "aaa"` and `s = "dog dog dog"`, why does the algorithm return `true` without any special-casing for the repeated letter?
**A:** The first occurrence records `a → dog` and `dog → a` in the maps; every subsequent occurrence of `'a'` and `"dog"` simply re-validates against those already-recorded, consistent mappings, so no additional logic is needed for repetition — consistency checking naturally handles it.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public boolean wordPattern(String pattern, String s) {
        String[] words = s.split(" ");
        if (words.length != pattern.length()) {
            return false;
        }

        Map<Character, String> letterToWord = new HashMap<>();
        Map<String, Character> wordToLetter = new HashMap<>();

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            String word = words[i];

            if (letterToWord.containsKey(c)) {
                if (!letterToWord.get(c).equals(word)) {
                    return false;
                }
            } else if (wordToLetter.containsKey(word)) {
                return false;
            } else {
                letterToWord.put(c, word);
                wordToLetter.put(word, c);
            }
        }

        return true;
    }
}
```

## 14. Self-Test

Given a `pattern` of letters and a space-separated string `s`, determine whether there's a bijection between each pattern letter and each unique word (same letter always same word, same word always same letter, counts must match). Re-derive: split `s` into words and check the count matches `pattern.length()`; then scan both together maintaining a letter-to-word map and a word-to-letter map, verifying any existing mapping agrees and rejecting if a word is already claimed by a different letter, before recording new pairs.
