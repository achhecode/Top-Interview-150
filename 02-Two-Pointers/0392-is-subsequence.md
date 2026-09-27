---
problem: 392 - Is Subsequence
url: https://leetcode.com/problems/is-subsequence/
difficulty: Easy
section: String / Two Pointers
patterns: [two-pointers, greedy-matching]
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

**Given:** Two strings, `s` and `t`.

**Required:** Return whether `s` is a subsequence of `t` — that is, whether `s` can be formed by deleting some (possibly zero) characters from `t` without changing the relative order of the remaining characters.

**Constraints that actually matter:**
- `0 <= s.length <= 100` and `0 <= t.length <= 10^4` — `s` is always much smaller than or equal to `t` in the base problem; a single linear scan through `t` while advancing through `s` is more than fast enough.
- `s` or `t` can be **empty**. An empty `s` is trivially a subsequence of any `t` (delete everything, or delete nothing if `t` is also empty) — this is a natural consequence of the matching logic rather than something requiring special-case code.
- **The explicit follow-up** — checking many different `s` strings (`s1, s2, ..., sk` with `k >= 10^9`) against the same fixed `t` — is a strong hint that the "intended deeper lesson" of this problem is recognizing when to **preprocess `t` once** (e.g., recording each character's occurrence positions) so that each subsequent query can be answered faster than a fresh O(n) scan through all of `t` every single time.

## 2. Recognition Signals

- "Is one string a subsequence of another" (not substring — order matters, but characters need not be contiguous) → the classic **two-pointer greedy matching** technique: walk through both strings simultaneously, advancing the "target" pointer only when a match is found, and the "source" pointer unconditionally.
- The specific greedy insight: to check if `s` is a subsequence of `t`, it's always optimal to match each character of `s` against the **earliest possible** unused position in `t` — there's never a reason to prefer a later matching position, since doing so can only make it harder (or no easier) to match the remaining characters of `s` afterward.
- The explicit follow-up mentioning a **huge number of repeated queries against the same `t`** → a strong, deliberate signal pointing toward **binary search on precomputed per-character position lists** as the more advanced, amortized-efficient technique, once `t` is fixed and queried repeatedly.

**Pattern:** Two-Pointer Greedy Matching (for a single query) — with **Binary Search on Precomputed Character Positions** as the follow-up-motivated optimization for repeated queries against a fixed `t`.

## 3. Core Idea

To check if `s` is a subsequence of `t`, use two pointers: `i` for `s`, `j` for `t`. Walk `j` through `t` from the start; whenever `t.charAt(j)` matches the character `s` currently needs (`s.charAt(i)`), advance `i` as well (this character of `s` has now been "found" in `t`); regardless of whether it matched, always advance `j`. If `i` reaches the end of `s`, every character was successfully matched in order, so `s` is a subsequence. This greedy strategy — always taking the *earliest* available match in `t` for the current character of `s` — is provably optimal: matching a character of `s` at an earlier position in `t` can never make it harder to match the remaining characters of `s`, and might make it easier (more of `t` remains available afterward).

**Invariant:** After processing `t.charAt(j)`, the pointer `i` correctly equals the number of characters of `s` that have been successfully matched, in order, using only the earliest possible positions within `t.charAt(0..j)` — so once `j` reaches the end of `t`, `i == s.length()` if and only if `s` is a subsequence of `t`.

## 4. Approach 1 — Brute Force (Repeated Search from Current Position)

**Logic:** For each character of `s` in order, search forward in `t` (starting from just after where the previous character was found) for the next occurrence of that character, using `String.indexOf(char, fromIndex)`. If any character can't be found, `s` is not a subsequence.

```java
public boolean isSubsequence(String s, String t) {
    int searchFrom = 0;

    for (int i = 0; i < s.length(); i++) {
        int foundAt = t.indexOf(s.charAt(i), searchFrom);
        if (foundAt == -1) {
            return false;
        }
        searchFrom = foundAt + 1;
    }

    return true;
}
```

- **Time:** O(n × m) in the worst case, where `n = t.length()` and `m = s.length()` — each call to `indexOf` can itself scan up to the remainder of `t`, and this is repeated once per character of `s`.
- **Space:** O(1) auxiliary — just the `searchFrom` index.

**Why it is not optimal:** Although `String.indexOf` is a convenient built-in, calling it once per character of `s` means the algorithm can, in the worst case, re-scan overlapping portions of `t` multiple times, leading to O(n × m) total work rather than a single coordinated O(n + m) pass. For a single query this is usually still fast enough given the small constraints, but it doesn't scale as cleanly and doesn't build toward the follow-up's repeated-query scenario as naturally as the two-pointer approach does.

## 5. Approach 2 — Optimized (Two Pointers, Single Coordinated Pass)

**Algorithm:**
1. Initialize `i = 0` (pointer into `s`) and `j = 0` (pointer into `t`).
2. While `i < s.length()` and `j < t.length()`:
   - If `s.charAt(i) == t.charAt(j)`, increment `i` (this character of `s` has been matched).
   - Always increment `j` (move forward through `t` regardless of whether this position matched).
3. After the loop, return `i == s.length()` (every character of `s` was successfully matched in order).

```java
public boolean isSubsequence(String s, String t) {
    int i = 0;
    int j = 0;

    while (i < s.length() && j < t.length()) {
        if (s.charAt(i) == t.charAt(j)) {
            i++;
        }
        j++;
    }

    return i == s.length();
}
```

- **Time:** O(n) where `n = t.length()` — a single coordinated pass through `t`, with `i` advancing at most `m = s.length()` times total, and `j` advancing at most `n` times total; overall O(n + m), with `t` scanned only once regardless.
- **Space:** O(1) auxiliary — just the two pointer variables.

**Why this is optimal:** Every character of `t` may need to be examined at least once to determine whether it's useful for matching some character of `s`, so O(n) is a natural lower bound for a single query, and this approach achieves it with a single unified pass — no repeated or overlapping re-scanning of any portion of `t`, unlike Approach 1. Space is O(1) since only two index pointers are needed, with no auxiliary data structures. For a *single* query, this is the standard, expected optimal solution; the follow-up's repeated-query scenario calls for an additional layer of precomputation on top of this core idea (discussed in the Java Notes and Interview Takeaway sections below).

## 6. Dry Run

Example: `s = "abc"`, `t = "ahbgdc"`.
Chosen because it's the canonical "true" example, and it clearly shows `j` advancing past non-matching characters in `t` while `i` only advances on actual matches.

`s.length()=3`, `t.length()=6`. Initial: `i=0, j=0`.

| j | t[j] | s[i] (current) | Match? | Action | i after | j after |
|---|------|---------------------|--------|--------|-------------|-------------|
| 0 | 'a' | 'a' (i=0) | Yes | i++, j++ | 1 | 1 |
| 1 | 'h' | 'b' (i=1) | No | j++ | 1 | 2 |
| 2 | 'b' | 'b' (i=1) | Yes | i++, j++ | 2 | 3 |
| 3 | 'g' | 'c' (i=2) | No | j++ | 2 | 4 |
| 4 | 'd' | 'c' (i=2) | No | j++ | 2 | 5 |
| 5 | 'c' | 'c' (i=2) | Yes | i++, j++ | 3 | 6 |

Exit condition: `i == s.length()` (3) is reached, so the outer `while` loop's `i < s.length()` condition becomes false, ending the loop (even though `j` also reached 6 simultaneously here).

**Final answer:** `i == s.length()` is `3 == 3`, which is `true`, matching the expected output exactly — "abc" is indeed a subsequence of "ahbgdc".

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Repeated `indexOf` search | O(n × m) worst case | O(1) | Correct but can redundantly re-scan portions of `t` |
| 2. Two pointers, single pass | O(n) | O(1) | Optimal for a single query; one coordinated pass through `t` |

`n = t.length()`, `m = s.length()`, for a single query. (The follow-up scenario of many repeated queries against a fixed `t` calls for additional preprocessing, discussed below.)

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| `s` is empty (`s.length() == 0`) | An empty string is trivially a subsequence of any string (including another empty string) | The `while` loop's condition `i < s.length()` is `0 < 0`, false immediately, so the loop never executes, and `i == s.length()` (`0 == 0`) is `true`, correctly returned without any special-casing |
| `t` is empty but `s` is not | A non-empty `s` cannot be a subsequence of an empty `t` | The `while` loop's condition `j < t.length()` is `0 < 0`, false immediately, so the loop never executes, and `i` remains 0, while `s.length() > 0`, so `i == s.length()` is `false`, correctly returned |
| `s` and `t` are identical | `s` should trivially be a subsequence of itself | Every character comparison succeeds in lockstep, with both `i` and `j` advancing together every iteration, until both reach the end simultaneously, correctly returning `true` |
| `s` is longer than `t` | Cannot possibly be a subsequence, since there aren't enough characters in `t` to match every character of `s` | The `while` loop will exhaust `j` (reaching `t.length()`) before `i` can reach `s.length()`, since `i` can advance at most as many times as `j` does, so the loop ends with `i < s.length()`, correctly returning `false` |
| `s` requires characters from `t` in a different relative order than they appear (e.g. `s="axc"`, matching example 2's logic) | Must correctly reject a character sequence that exists in `t` but in the wrong order | Since `i` only ever advances forward (never resets or searches backward), and `j` only ever advances forward through `t`, the algorithm inherently enforces that matched characters must appear in the same relative order in both strings — a mismatch anywhere breaks the ability to complete matching all of `s` |
| `t` contains many repeated characters, some of which could match `s`'s characters in multiple possible ways | Greedy "earliest match" choice must still be provably correct, not just "a" valid choice | Matching each character of `s` at the earliest possible position in `t` is always at least as good as any later match, since it never uses up more of `t` than necessary, leaving maximum room for matching the remaining characters of `s` — this greedy choice is provably optimal, not just a heuristic |

## 9. Java Notes

- **`String.charAt(index)` for direct character comparison:** the two-pointer approach relies on simple, efficient character-by-character access rather than substring creation or regex.
- **`String.indexOf(char, fromIndex)` (used in Approach 1):** a convenient built-in for finding the next occurrence of a character starting from a given position, but its repeated use across multiple characters of `s` is what causes the O(n×m) worst-case behavior — each call can independently scan a large portion of `t`.
- **No overflow or numeric risk:** this problem involves only character comparisons and small bounded index variables; no arithmetic operations here risk overflow.
- **Addressing the follow-up in Java:** for the scenario of checking many different `s` strings against the same fixed `t`, the standard technique is to preprocess `t` once into a `Map<Character, List<Integer>>`, recording every index at which each character occurs in `t`. Then, for each query string `s`, instead of a linear scan through `t`, use binary search (`Collections.binarySearch`, or a manual binary search) on the sorted position list for each character of `s` in turn, finding the smallest recorded position that's still greater than the previously matched position — this reduces each query to O(m log n) instead of O(n), which is a significant improvement when `n` is large and there are many (`k >= 10^9`) queries to answer against the same `t`.

## 10. Common Mistakes

- **Advancing `i` (the `s` pointer) unconditionally alongside `j`, rather than only advancing it upon an actual character match.** This would incorrectly assume every character of `t` corresponds to a character of `s`, rather than correctly allowing `t` to contain extra, non-matching characters in between the ones that do match. Fix: only advance `i` inside the `if` block when a match is found; always advance `j` regardless, every iteration.
- **Trying to match characters of `s` starting from the most recent position without considering that this must be the *earliest* available match, not just *any* available match.** While the given two-pointer approach naturally achieves earliest-match greedily by construction, an alternative flawed implementation might search for matches in a way that doesn't guarantee this property, potentially leading to incorrect results on inputs with repeated characters. Fix: ensure the matching logic always considers `t` in strict left-to-right order, taking the very first available match for each character of `s`.
- **For the follow-up scenario, re-running the O(n) two-pointer scan freshly for every single query string, without any preprocessing of `t`.** With `k >= 10^9` queries, this would result in an enormous amount of redundant work, since `t` itself never changes between queries. Fix: preprocess `t` once (e.g., building per-character position lists), then answer each query using a faster method (like binary search) that leverages that preprocessing.
- **Off-by-one errors when implementing the binary-search-based follow-up solution, particularly around finding the smallest position strictly greater than the previously used position.** This requires careful use of binary search variants (like "find first position greater than X") rather than a standard exact-match binary search. Fix: carefully verify the binary search's boundary conditions against small hand-worked examples before trusting it on the full problem.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Is one string a subsequence of another (order matters, but not contiguity) → two pointers, greedily matching each character of the shorter string at the earliest possible position in the longer string."
- **90-second explanation:** "I use two pointers, one for each string. I walk through the longer string, `t`, one character at a time, and whenever the current character of `t` matches the character I currently need from `s`, I advance my pointer into `s` as well — otherwise, I just keep moving forward through `t`. If I successfully advance all the way through `s`, that means every character was found in the correct order somewhere in `t`, so `s` is a subsequence. This greedy strategy of always taking the earliest possible match is provably optimal, since matching earlier in `t` can never make it harder to match the rest of `s` afterward, and leaves more of `t` available for whatever comes next. For a single query, this runs in O(n) time and O(1) space. If I needed to answer this question repeatedly for a huge number of different `s` strings against the same fixed `t` — which is exactly what this problem's follow-up asks about — I'd preprocess `t` once by recording every position where each character occurs, and then use binary search on those position lists to answer each subsequent query much faster than rescanning all of `t` from scratch every time."
- **Related problems using this pattern:**
  - LeetCode 1143 — Longest Common Subsequence (related but requires full DP, not just a yes/no greedy check)
  - LeetCode 524 — Longest Word in Dictionary through Deleting (directly builds on this exact subsequence-checking technique)
  - LeetCode 792 — Number of Matching Subsequences (the exact scenario the follow-up describes, benefiting from the precomputed-positions/binary-search optimization)

## 12. Recall Questions

**Q:** Why is it always optimal to match each character of `s` at the *earliest* possible position in `t`, rather than some later position?
**A:** Using an earlier position in `t` leaves strictly more of `t` remaining (or at least no less) to match the rest of `s` afterward, so it can never make completing the match harder, and might make it easier — there's never a benefit to deliberately choosing a later match.

**Q:** Why does the two-pointer approach only advance the `s` pointer conditionally (on a match), but advance the `t` pointer unconditionally every iteration?
**A:** `t` is allowed to contain extra characters that don't need to be part of the subsequence, so the algorithm must always move forward through `t` regardless of whether the current character is useful, while only "consuming" a character of `s` when an actual match has just been confirmed.

**Q:** Why does the brute-force repeated-`indexOf` approach risk O(n×m) time, while the two-pointer approach achieves O(n)?
**A:** Each call to `indexOf` independently scans forward through `t` from its starting point, and across multiple characters of `s`, these scans can overlap and redundantly re-examine portions of `t` already passed over, whereas the two-pointer approach makes exactly one coordinated pass through `t` from start to finish, never revisiting any position.

**Q:** How does preprocessing `t` into per-character position lists help address the follow-up's repeated-query scenario?
**A:** Once those position lists are built (a one-time O(n) cost), each individual query can use binary search to quickly find the next valid matching position for each character of the query string, taking O(m log n) time per query rather than a full O(n) rescan of `t` every single time, which becomes a substantial savings when there are an enormous number of queries against the same fixed `t`.

**Q:** Why can't a simple exact-match binary search be used directly for the follow-up's optimization, and what kind of binary search variant is needed instead?
**A:** For each character of the query string, the algorithm needs to find the smallest recorded position in `t` for that character that is still strictly greater than the position used for the previous character, which requires a "find the first element greater than X" style binary search rather than a search for an exact matching value.

## 13. Final Code

```java
public boolean isSubsequence(String s, String t) {
    int i = 0;
    int j = 0;

    while (i < s.length() && j < t.length()) {
        if (s.charAt(i) == t.charAt(j)) {
            i++;
        }
        j++;
    }

    return i == s.length();
}
```

## 14. Self-Test

You have two strings, `s` and `t`. Return whether `s` is a subsequence of `t` — formed by deleting some characters from `t` without changing the relative order of what remains. Think about why greedily matching each character of `s` at the earliest possible position in `t`, using two pointers advancing through both strings, correctly and efficiently determines the answer — and how you'd adapt this if you needed to check many different `s` strings against the same fixed `t` repeatedly.
