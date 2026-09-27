---
problem: 242 - Valid Anagram
url: https://leetcode.com/problems/valid-anagram/
difficulty: Easy
section: String / Hash Table
patterns: [frequency-counting]
data_structures: [array, hashmap]
time: O(n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** two strings `s` and `t`.

**Required:** return `true` if `t` is an anagram of `s` — meaning `t` uses exactly the same letters as `s`, with exactly the same counts, just possibly in a different order.

**Constraints that matter:**
- `1 <= s.length, t.length <= 5 * 10^4` — large enough that an O(n log n) sort-based approach is reasonably fast but not optimal; the problem's own follow-up (adapting to Unicode) signals the intended solution should generalize cleanly, which a fixed-size counting approach does more awkwardly than others.
- Both strings consist of **lowercase English letters only** — exactly 26 possible characters, directly enabling a fixed-size `int[26]` array as the fastest practical structure.
- If `s.length() != t.length()`, they can never be anagrams — an immediate, cheap rejection that avoids any further work.

## 2. Recognition Signals

- "Same letters, same counts, different order allowed" → this is the defining phrase for an **anagram check**, distinct from isomorphism (which requires a positional structural match) or subsequence problems (which require order).
- Small, fixed alphabet (lowercase English letters) stated explicitly → strong hint toward a **fixed-size frequency array** rather than a general hash map, for both speed and simplicity.
- No positional constraint at all (order is explicitly irrelevant) → rules out any pointer-matching or index-based technique; this is purely a **multiset equality** check.
- Named pattern: **Frequency Counting / Multiset Comparison**.

## 3. Core Idea

Two strings are anagrams of each other exactly when they contain the same characters with the same counts. Count the characters of one string as "credits" and the other as "debits" into a single shared array — if every count nets to exactly zero at the end, the multisets are identical.

**Why it's correct:** an anagram relationship is entirely defined by character multiset equality; order is irrelevant by definition. Incrementing counts for `s` and decrementing for `t` in the same array is equivalent to computing `count(c in s) - count(c in t)` for every character `c` simultaneously — if and only if every one of these differences is zero, the two strings are anagrams.

**Invariant:** after processing index `i` of both strings (assuming equal length), `counts[k]` holds `count(k-th letter in s[0..i]) - count(k-th letter in t[0..i])`; at the very end, all 26 entries being zero is both necessary and sufficient for `t` to be an anagram of `s`.

## 4. Approach 1 — Brute Force (Sort and Compare)

1. If `s.length() != t.length()`, return `false`.
2. Convert both strings to `char[]` arrays and sort each.
3. Compare the sorted arrays for equality — if they're identical, the original strings were anagrams.

```java
public boolean isAnagramBruteForce(String s, String t) {
    if (s.length() != t.length()) {
        return false;
    }

    char[] sChars = s.toCharArray();
    char[] tChars = t.toCharArray();
    Arrays.sort(sChars);
    Arrays.sort(tChars);

    return Arrays.equals(sChars, tChars);
}
```

**Time:** O(n log n) — dominated by sorting both character arrays.
**Space:** O(n) auxiliary for the two sorted character array copies (`toCharArray()` allocates new arrays).

This is not optimal: sorting does more work than necessary — the problem doesn't need the characters in any particular order, only their counts compared, so paying O(n log n) instead of O(n) is unnecessary overhead. Still, this approach generalizes trivially to any character set (Unicode included) with no code changes, which is a real trade-off worth naming, since the optimized approach below sacrifices that generality for the lowercase-only case.

## 5. Approach 2 — Optimized (Fixed-Size Frequency Array)

1. If `s.length() != t.length()`, return `false` immediately.
2. Create an `int[26]` array `counts`, initialized to zero.
3. Iterate through both strings simultaneously (single pass, since lengths are equal): increment `counts[s.charAt(i) - 'a']` and decrement `counts[t.charAt(i) - 'a']`.
4. After the loop, check that every entry in `counts` is exactly zero; if any is nonzero, `s` and `t` have differing character counts and are not anagrams.

```java
public boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) {
        return false;
    }

    int[] counts = new int[26];
    for (int i = 0; i < s.length(); i++) {
        counts[s.charAt(i) - 'a']++;
        counts[t.charAt(i) - 'a']--;
    }

    for (int count : counts) {
        if (count != 0) {
            return false;
        }
    }

    return true;
}
```

**Time:** O(n) — one pass to build the combined count differences, one constant-size (26-element) pass to verify all zeros.
**Space:** O(1) auxiliary — the `int[26]` array is a fixed size regardless of input length.

This is optimal for the stated constraints (lowercase English letters only): every character must be inspected at least once, so O(n) is the best possible time, and a fixed 26-element array is the minimal structure needed, beating the O(n log n) sort-based approach and avoiding the extra `char[]` allocations it requires.

## 6. Dry Run

`s = "anagram"`, `t = "nagaram"`

| i | s char / t char | counts update | counts state (only touched indices shown) |
|---|---|---|---|
| 0 | 'a' / 'n' | a++, n-- | a:1, n:-1 |
| 1 | 'n' / 'a' | n++, a-- | a:0, n:0 |
| 2 | 'a' / 'g' | a++, g-- | a:1, g:-1 |
| 3 | 'g' / 'a' | g++, a-- | a:0, g:0 |
| 4 | 'r' / 'r' | r++, r-- | r:0 |
| 5 | 'a' / 'a' | a++, a-- | a:0 |
| 6 | 'm' / 'm' | m++, m-- | m:0 |

Exit: loop finishes after processing all 7 character pairs; final scan confirms every one of the 26 entries is `0`. Final answer: `true`, matching the expected output.

Contrast with `s = "rat"`, `t = "car"`: after processing, `counts['t'-'a']` ends at `+1` (t has a 't' that car doesn't) and `counts['c'-'a']` ends at `-1` (car has a 'c' that rat doesn't) — the final verification scan finds a nonzero entry and returns `false`.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Sort and Compare | O(n log n) | O(n) aux | Generalizes trivially to any character set (e.g. Unicode) |
| Fixed-Size Frequency Array | O(n) | O(1) aux | Fastest for the stated lowercase-only constraint |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Different lengths | Can never be anagrams regardless of content | Early `s.length() != t.length()` check returns `false` immediately |
| `s` equals `t` exactly | Every character trivially matches its own count | Each increment is immediately offset by an equal decrement; all counts end at zero |
| Single-character strings | Minimal input size | One increment, one decrement (possibly same or different letters); works identically to longer inputs |
| All characters identical in both (e.g. `s="aaa"`, `t="aaa"`) | Must correctly confirm equal counts, not just equal length | `counts['a'-'a']` increments and decrements exactly 3 times each, netting to zero |
| Same letters, different counts (e.g. `s="aab"`, `t="abb"`) | Classic near-miss that must still fail | `counts['a'-'a']` nets to `+1`, `counts['b'-'a']` nets to `-1` — final scan catches the mismatch |
| Unicode or non-lowercase input (per the problem's own follow-up) | `c - 'a'` indexing breaks outside lowercase ASCII letters | Not handled by this array-based solution as written; the follow-up explicitly calls for a `HashMap<Character, Integer>` (or similar) replacement in that case |

## 9. Java Notes

- `c - 'a'` only produces a valid, in-bounds index (`0`–`25`) when `c` is guaranteed to be a lowercase English letter, which the constraints explicitly promise here; this indexing trick would break (produce out-of-bounds or negative indices) for uppercase letters, digits, or Unicode characters.
- `int[26]` avoids all autoboxing overhead versus a `HashMap<Character, Integer>`, which matters when scanning up to `5 * 10^4` characters twice (increment and decrement) — this is the textbook case for preferring a small fixed array over a general map.
- `Arrays.sort(char[])` uses a dual-pivot quicksort variant for primitive arrays, O(n log n) average with O(log n) auxiliary stack space — worth knowing as the baseline this problem's O(n) solution beats.
- `s.toCharArray()` and `t.toCharArray()` each allocate a full copy of the string's characters, which is part of why the sort-based approach costs O(n) extra space beyond just the sorting itself.

## 10. Common Mistakes

- Forgetting the `s.length() != t.length()` early check and instead relying solely on the final "all counts zero" verification — this actually still works correctly for the *given* problem since counts alone fully determine anagram status, but it's a slower and less explicit approach, and in variants without guaranteed equal lengths could allow subtle bugs elsewhere. Fix: always check lengths first as a fast, explicit short-circuit.
- Using `counts[s.charAt(i) - 'a']++` on strings containing uppercase letters, digits, or symbols without realizing the array indexing assumption — silently produces wrong results or throws `ArrayIndexOutOfBoundsException`. Fix: verify the constraint (lowercase only) before using this indexing trick, or switch to a `HashMap` for general Unicode-safe support as the problem's follow-up suggests.
- Checking only that all 26 counts are `<= 0` or `>= 0` instead of strictly `== 0` — this would incorrectly accept cases where one string has extra characters of some letter and a deficit of another, as long as the sign pattern happens to look consistent (though in practice this specific rule would rarely produce false positives, it's a conceptually wrong check). Fix: require exact equality to zero for every entry.
- Iterating the two strings in separate loops (first fully processing `s`, then fully processing `t`) into two separate arrays and comparing those two arrays at the end — correct, but doubles the array traversals and requires a full 26-element comparison instead of a single-array all-zero check. Fix: use one shared array with increment/decrement to fold both counts into a single comparison.

## 11. Interview Takeaway

- **Trigger sentence:** "Same characters, same counts, order irrelevant → frequency counting; for a small fixed alphabet, a fixed-size array beats sorting."
- **90-second explanation:** "Since an anagram just means the same multiset of characters, I use one 26-element array to track the net difference in counts between the two strings — incrementing for each character in `s` and decrementing for the corresponding character in `t`. If `t` is truly an anagram of `s`, every letter's increments and decrements must cancel out exactly, so at the end every entry in the array should be zero. I check the lengths match up front as a cheap early rejection, since equal length is necessary (though not sufficient) for an anagram. This beats sorting both strings and comparing, which works but costs O(n log n) instead of O(n) for no extra benefit here."
- **Related problems:** 383 (Ransom Note), 49 (Group Anagrams), 438 (Find All Anagrams in a String), 567 (Permutation in String).

## 12. Recall Questions

**Q:** Why is checking `s.length() != t.length()` up front a necessary optimization rather than just a nice-to-have?
**A:** Different lengths make an anagram impossible regardless of content, and while the counting logic would still technically produce a nonzero result eventually, checking lengths first avoids any unnecessary work and makes the impossibility explicit and immediate.

**Q:** Why does incrementing for `s` and decrementing for `t` into the *same* array work, instead of needing two separate arrays?
**A:** The net effect of incrementing by one string's counts and decrementing by the other's is mathematically the same as computing count(s) − count(t) for every character in one pass; if the two strings have identical character multisets, every such difference is exactly zero, so one shared array fully captures the comparison.

**Q:** How would this solution need to change to handle Unicode characters, as the problem's follow-up asks?
**A:** The fixed `int[26]` array relies on the lowercase-English-letters guarantee for its `c - 'a'` indexing; with Unicode input, that assumption breaks, so the array would need to be replaced with a `HashMap<Character, Integer>` (or similar) that can key on any character value without a bounded, contiguous index range.

**Q:** Why is the sort-based brute force approach considered less optimal despite also being correct?
**A:** Sorting imposes an unnecessary O(n log n) cost and extra O(n) space for the character array copies, when the problem only requires comparing character *counts*, not producing any ordering — a linear counting pass achieves the same correctness in less time.

**Q:** What would go wrong if the final verification only checked whether the counts array's sum was zero, rather than checking each entry individually?
**A:** Positive and negative differences across different letters could cancel each other out in a simple sum (e.g., one letter over by 2 and another under by 2 would sum to zero) while the strings are still clearly not anagrams, so each of the 26 entries must be individually verified as exactly zero.

## 13. Final Code

```java
class Solution {
    public boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }

        int[] counts = new int[26];
        for (int i = 0; i < s.length(); i++) {
            counts[s.charAt(i) - 'a']++;
            counts[t.charAt(i) - 'a']--;
        }

        for (int count : counts) {
            if (count != 0) {
                return false;
            }
        }

        return true;
    }
}
```

## 14. Self-Test

Given two lowercase-letter strings `s` and `t`, determine whether `t` is an anagram of `s` (same characters, same counts, any order). Re-derive: check lengths match first, then use a single fixed-size 26-element array, incrementing per character seen in `s` and decrementing per character seen in `t` in one combined pass, and confirm every entry nets to exactly zero at the end.
