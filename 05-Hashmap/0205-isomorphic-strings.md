---
problem: 205 - Isomorphic Strings
url: https://leetcode.com/problems/isomorphic-strings/
difficulty: Easy
section: String / Hash Table
patterns: [hashmap, bijective-mapping]
data_structures: [hashmap, array]
time: O(n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** two strings `s` and `t` of equal length.

**Required:** determine whether `s` can be transformed into `t` by consistently replacing every occurrence of each character in `s` with some character (order preserved), such that the mapping is one-to-one in both directions — no two distinct characters in `s` may map to the same character in `t`, though a character may map to itself.

**Constraints that matter:**
- `1 <= s.length <= 5 * 10^4` and `t.length == s.length` — lengths always match, so no need to handle mismatched lengths as a separate case; large enough that O(n²) approaches risk being slow but not egregiously so at this size, though O(n) is still the clean target.
- `s` and `t` consist of **any valid ASCII character** — not just lowercase letters, so a `HashMap<Character, Character>` (or an `int[128]`/`int[256]` array keyed by ASCII code) is the safe general structure, not a 26-letter array.
- The mapping must be a **bijection** (one-to-one in both directions) — this is the trap: checking only `s → t` consistency isn't enough; `t → s` must also be checked, or two different `s` characters could illegally map to the same `t` character.

## 2. Recognition Signals

- "Characters can be replaced to get another string, preserving order, each occurrence replaced consistently" → character-to-character **mapping/pattern-matching** problem.
- "No two characters may map to the same character" — this phrase is the explicit tell that a **single-direction map is insufficient**; you need to verify the mapping is a bijection, not just a function.
- Comparing two strings position-by-position with a consistency requirement (not a sum, not a window) → this isn't sliding window or two pointers; it's **two synchronized hash maps built in one linear scan**.
- Named pattern: **Bijective Character Mapping** (also appears as "Word Pattern," a near-identical problem at the word level instead of the character level).

## 3. Core Idea

Walk both strings simultaneously. Maintain two maps: one from `s`-characters to `t`-characters, and one from `t`-characters back to `s`-characters. At each position, if either mapping already exists, it must agree with what's being seen now; if it doesn't exist yet, record it in both directions.

**Why it's correct:** a valid isomorphism is exactly a bijection between the character sets of `s` and `t` that's consistent at every position. Checking only `mapS[s[i]] == t[i]` catches the case where the *same* `s`-character maps inconsistently to different `t`-characters, but misses the case where two *different* `s`-characters both try to map to the *same* `t`-character — that violates "no two characters may map to the same character" and can only be caught by also checking the reverse map `mapT[t[i]] == s[i]`.

**Invariant:** after processing index `i`, `mapS` and `mapT` are perfect inverses of each other restricted to the characters seen in `s[0..i]` and `t[0..i]` respectively — for every character `c` in `mapS`, `mapT[mapS[c]] == c`, and vice versa.

## 4. Approach 1 — Brute Force

1. For every pair of indices `(i, j)` with `i < j`, check that the "same character" relationship is preserved in both directions: `(s[i] == s[j])` must equal `(t[i] == t[j])`.
2. If this ever mismatches, the strings are not isomorphic.
3. If every pair agrees, they are isomorphic.

```java
public boolean isIsomorphicBruteForce(String s, String t) {
    int n = s.length();
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            boolean sameInS = s.charAt(i) == s.charAt(j);
            boolean sameInT = t.charAt(i) == t.charAt(j);
            if (sameInS != sameInT) {
                return false;
            }
        }
    }
    return true;
}
```

**Time:** O(n²) — every pair of indices is compared.
**Space:** O(1) auxiliary.

This is not optimal: with `n` up to `5 * 10^4`, O(n²) is ~2.5×10^9 comparisons, likely too slow. It also re-derives the same "does this character map consistently" fact repeatedly across overlapping pairs instead of recording it once.

## 5. Approach 2 — Optimized (Two Synchronized Hash Maps, Single Pass)

1. If `s.length() != t.length()`, return `false` (guard; the problem guarantees equal lengths, but this makes the function robust).
2. Create two maps: `mapS` (character in `s` → character in `t`) and `mapT` (character in `t` → character in `s`).
3. Iterate `i` from `0` to `n - 1`, letting `sc = s.charAt(i)` and `tc = t.charAt(i)`:
   - If `mapS` contains `sc`: it must map to `tc`; if not, return `false`.
   - Else if `mapT` contains `tc`: it means some other `s`-character already claimed `tc`; return `false`.
   - Else: record `mapS.put(sc, tc)` and `mapT.put(tc, sc)`.
4. If the loop completes without conflict, return `true`.

```java
public boolean isIsomorphic(String s, String t) {
    if (s.length() != t.length()) {
        return false;
    }

    Map<Character, Character> mapS = new HashMap<>();
    Map<Character, Character> mapT = new HashMap<>();

    for (int i = 0; i < s.length(); i++) {
        char sc = s.charAt(i);
        char tc = t.charAt(i);

        if (mapS.containsKey(sc)) {
            if (mapS.get(sc) != tc) {
                return false;
            }
        } else if (mapT.containsKey(tc)) {
            return false; // tc already claimed by a different s-character
        } else {
            mapS.put(sc, tc);
            mapT.put(tc, sc);
        }
    }

    return true;
}
```

**Time:** O(n) — a single pass through both strings, with O(1) amortized map operations per position.
**Space:** O(1) auxiliary in practice — since the ASCII character set is bounded (at most 128/256 distinct characters), both maps can hold at most a constant number of entries regardless of `n`; expressed in terms of alphabet size `k`, it's O(k).

This is optimal: every character of both strings must be examined at least once, so O(n) is the best possible time, and maintaining two maps (rather than one) is the necessary trade-off to correctly enforce the bijection requirement in a single pass.

## 6. Dry Run

`s = "badc"`, `t = "baba"` (constructed to show a bijection violation)

| i | sc, tc | mapS has sc? | mapT has tc? | action | mapS / mapT after |
|---|---|---|---|---|---|
| 0 | 'b','b' | no | no | record b→b, b→b | mapS={b:b}, mapT={b:b} |
| 1 | 'a','a' | no | no | record a→a, a→a | mapS={b:b,a:a}, mapT={b:b,a:a} |
| 2 | 'd','b' | no | yes ('b' already maps back to 'b', not 'd') | **conflict → return false** | — |

Exit: loop breaks early at `i = 2` because `mapT` already contains `'b'` mapped to `'b'`, but now `'d'` (a different `s`-character) is trying to also map to `'b'` — violates the "no two characters may map to the same character" rule. Final answer: `false`.

Contrast with the example `s = "egg"`, `t = "add"`: `mapS = {e:a, g:d}`, `mapT = {a:e, d:g}` build up with no conflicts, so the function returns `true`.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute Force (pairwise check) | O(n²) | O(1) aux | Re-derives relationships redundantly across overlapping pairs |
| Two Hash Maps, Single Pass | O(n) | O(k) aux (k = alphabet size, effectively O(1)) | Maps bounded by the ASCII character set regardless of n |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Single-character strings | Trivial mapping | One iteration, records the single character pair, returns `true` |
| A character maps to itself (e.g. `'b'` → `'b'`) | Must be allowed per the problem statement | No special-casing needed — `mapS.put('b','b')` and `mapT.put('b','b')` are consistent and valid like any other pair |
| Two different `s`-characters both need to map to the same `t`-character (e.g. `s="ab"`, `t="aa"`) | The classic bijection violation this problem is testing | `mapT.containsKey(tc)` check catches this: second character tries to claim an already-used `tc` |
| Same `s`-character maps to two different `t`-characters (e.g. `s="aa"`, `t="ab"`) | The simpler, single-direction violation | `mapS.containsKey(sc)` check catches this directly: `mapS.get(sc) != tc` |
| Strings with non-letter ASCII characters (digits, symbols) as in `s="f11", t="b23"` | Must not assume only letters | `HashMap<Character, Character>` handles any `char` value uniformly, no ASCII-offset assumptions needed |
| `s` and `t` are identical strings | Every character trivially maps to itself | Builds `mapS` and `mapT` as identity maps; no conflicts possible, returns `true` |

## 9. Java Notes

- `s` and `t` may contain **any valid ASCII character**, not just letters — this rules out a `c - 'a'` style array index and motivates either a `HashMap<Character, Character>` or an `int[128]` array (ASCII is 0–127; some solutions use 256 to be extra safe with extended ASCII).
- `mapS.get(sc) != tc` compares two `char` values here (both sides are effectively unboxed via the `!=` operator context on primitives after `.get()` returns a `Character` that gets auto-unboxed against the primitive `tc`) — this works correctly because one operand (`tc`) is a primitive `char`, which forces unboxing of the `Character` before comparison; comparing two boxed `Character` objects directly with `!=` would risk the Integer/Character cache pitfall.
- Using two separate `HashMap<Character, Character>` objects (rather than one bidirectional structure) is the simplest correct approach in Java, since the JDK has no built-in `BiMap` — `Map.Entry` reversal would need to be done manually otherwise.
- `s.length() != t.length()` is guarded even though the problem guarantees equal lengths, since defensive checks against future/other callers cost O(1) and prevent an `IndexOutOfBoundsException` if that guarantee were ever violated.

## 10. Common Mistakes

- Checking only `mapS` (the `s → t` direction) and skipping the `mapT` reverse check entirely — this misses the case where two different `s`-characters map to the same `t`-character, incorrectly returning `true` for inputs like `s="ab"`, `t="aa"`. Fix: always maintain and check both directions.
- Using a single `Map<Character, Character>` and trying to infer bijection violations by scanning `values()` on every insertion — technically possible but degrades to O(n) per insertion (O(n²) overall) instead of O(1) with a dedicated reverse map. Fix: maintain a second map explicitly for O(1) reverse lookups.
- Assuming the character set is limited to lowercase letters and using a `char[26]` or `int[26]` array — fails on inputs with digits or symbols like `"f11"`/`"b23"`. Fix: use a `HashMap` or an array sized for the full ASCII range (128 or 256).
- Forgetting that a character mapping to itself is valid and explicitly special-casing it as an error — unnecessary and incorrect; self-mapping is just a normal consistent mapping like any other and needs no special handling.

## 11. Interview Takeaway

- **Trigger sentence:** "Two strings, replace characters consistently and bijectively to transform one into the other → two synchronized hash maps built in a single pass, one for each direction."
- **90-second explanation:** "I walk both strings together, position by position, maintaining two maps: one from each `s`-character to its corresponding `t`-character, and one for the reverse direction. At each position, if the `s`-character has already been mapped, I check it still points to the same `t`-character as before. Then, even if it's a new `s`-character, I check whether the `t`-character has already been claimed by mapping back to some other `s`-character — if so, that's a bijection violation, since two different characters can't map to the same one. If neither map has an entry yet, I record both directions. This catches both kinds of inconsistency in a single linear pass."
- **Related problems:** 290 (Word Pattern), 890 (Find and Replace Pattern), 49 (Group Anagrams, related pattern-normalization idea).

## 12. Recall Questions

**Q:** Why is checking only the `s → t` direction insufficient to determine isomorphism?
**A:** It only catches cases where the same `s`-character is mapped inconsistently to different `t`-characters, but misses the case where two *different* `s`-characters both try to map to the *same* `t`-character, which also violates the bijection requirement.

**Q:** Why does a character being allowed to "map to itself" not require any special-case logic in the code?
**A:** Self-mapping is just a normal consistent pair like `'a' → 'a'` stored the same way as any other pair; the map-consistency checks apply identically whether the two characters happen to be equal or not.

**Q:** Why can't a fixed-size `int[26]` array safely replace the two hash maps here?
**A:** The problem states the strings can contain any valid ASCII character, not just lowercase letters, so a 26-slot array indexed by `c - 'a'` would either miscompute indices or go out of bounds for digits, symbols, or uppercase letters.

**Q:** What invariant do `mapS` and `mapT` maintain relative to each other throughout the scan?
**A:** At every point, they are exact inverses of each other over the characters seen so far — for any character `c` recorded in `mapS`, looking up `mapS[c]` in `mapT` always yields `c` back, and this two-way consistency is what's being verified on each new position.

**Q:** Why does the algorithm only need a single linear pass rather than needing to look ahead or backtrack?
**A:** Because the bijection constraint is purely local and cumulative — once two characters are mapped to each other, that mapping can never legally change for the rest of the string, so any future violation will be caught by comparing against the already-recorded mapping at the moment it's next referenced.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public boolean isIsomorphic(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }

        Map<Character, Character> mapS = new HashMap<>();
        Map<Character, Character> mapT = new HashMap<>();

        for (int i = 0; i < s.length(); i++) {
            char sc = s.charAt(i);
            char tc = t.charAt(i);

            if (mapS.containsKey(sc)) {
                if (mapS.get(sc) != tc) {
                    return false;
                }
            } else if (mapT.containsKey(tc)) {
                return false;
            } else {
                mapS.put(sc, tc);
                mapT.put(tc, sc);
            }
        }

        return true;
    }
}
```

## 14. Self-Test

Given two equal-length strings of any ASCII characters, determine whether one can be transformed into the other by a consistent, one-to-one character mapping (order preserved, self-mapping allowed). Re-derive: scan both strings together while maintaining two hash maps, one from each `s`-character to its `t`-character and one for the reverse; at each position, verify any existing mapping agrees, and reject if the `t`-character is already claimed by a different `s`-character, before recording a brand-new pair in both maps.
