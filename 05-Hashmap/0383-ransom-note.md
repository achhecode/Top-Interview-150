---
problem: 383 - Ransom Note
url: https://leetcode.com/problems/ransom-note/
difficulty: Easy
section: String / Hash Table
patterns: [frequency-counting]
data_structures: [array, hashmap]
time: O(m + n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** two strings, `ransomNote` and `magazine`.

**Required:** return `true` if `ransomNote` can be built entirely out of letters taken from `magazine`, where each letter in `magazine` can be used **at most once** (you can't reuse the same physical letter twice).

**Constraints that matter:**
- `1 <= ransomNote.length, magazine.length <= 10^5` — large enough that any approach re-scanning `magazine` per character of `ransomNote` (naive removal-based search) risks O(m·n), so a single frequency-counting pass is the expected shape.
- Both strings consist of **lowercase English letters only** — exactly 26 possible characters, which is the direct signal that a fixed-size `int[26]` array is not just possible but the idiomatically optimal structure, faster than a general-purpose `HashMap`.
- "Each letter... used once" is what makes this a **count** comparison (how many of each letter magazine has vs. needs), not a simple set-membership check.

## 2. Recognition Signals

- "Can string A be built from the letters of string B, each letter usable once" → classic **frequency count comparison** between two strings.
- Fixed, small alphabet (lowercase English letters only) → prefer a fixed-size array over a `HashMap` for speed and simplicity.
- The problem only asks for a yes/no answer, not the actual construction or positions — this rules out any need for indices, order, or backtracking; it's purely a multiset-containment check.
- Named pattern: **Character Frequency Counting / Anagram-style comparison**.

## 3. Core Idea

Count how many of each letter `magazine` provides. Then, for `ransomNote` to be constructible, every letter it needs must not exceed the count `magazine` has available for that letter.

**Why it's correct:** since letters are interchangeable (position doesn't matter, only availability), the entire problem reduces to a per-letter supply-vs-demand check: `ransomNote` is constructible if and only if, for every letter `c`, `count(c in ransomNote) <= count(c in magazine)`. No other property of the strings (order, adjacency, etc.) affects the answer.

**Invariant:** after processing all of `magazine`, `available[c]` holds the exact number of unused copies of letter `c` remaining; as `ransomNote` is scanned, decrementing `available[c]` and checking for a negative result at each step is equivalent to checking the full inequality up front, just interleaved with an early-exit opportunity.

## 4. Approach 1 — Brute Force

1. Convert `magazine` into a mutable list of characters.
2. For each character in `ransomNote`, search the list for a matching character.
3. If found, remove it from the list (so it can't be reused); if not found, return `false`.
4. If every character of `ransomNote` was matched and removed, return `true`.

```java
public boolean canConstructBruteForce(String ransomNote, String magazine) {
    List<Character> availableLetters = new ArrayList<>();
    for (char c : magazine.toCharArray()) {
        availableLetters.add(c);
    }

    for (char c : ransomNote.toCharArray()) {
        int index = availableLetters.indexOf(c);
        if (index == -1) {
            return false;
        }
        availableLetters.remove(index);
    }

    return true;
}
```

**Time:** O(n · m) in the worst case — for each of the `n` characters in `ransomNote`, `indexOf` and `remove` on an `ArrayList` can each cost O(m).
**Space:** O(m) auxiliary for the mutable letter list.

This is not optimal: with both strings up to `10^5` characters, O(n·m) can reach ~10^10 operations. It also does unnecessary linear scans and list shifting for what is fundamentally a counting problem, not a search problem.

## 5. Approach 2 — Optimized (Fixed-Size Frequency Array)

1. Create an `int[26]` array `available`, initialized to zero.
2. Scan `magazine` once, incrementing `available[c - 'a']` for each character `c`.
3. Scan `ransomNote` once, decrementing `available[c - 'a']` for each character `c`; if any count goes negative, `ransomNote` needs more of that letter than `magazine` has — return `false` immediately.
4. If the scan completes without going negative, return `true`.

```java
public boolean canConstruct(String ransomNote, String magazine) {
    if (ransomNote.length() > magazine.length()) {
        return false; // can't build a longer note from a shorter magazine
    }

    int[] available = new int[26];
    for (char c : magazine.toCharArray()) {
        available[c - 'a']++;
    }

    for (char c : ransomNote.toCharArray()) {
        available[c - 'a']--;
        if (available[c - 'a'] < 0) {
            return false;
        }
    }

    return true;
}
```

**Time:** O(m + n) — one pass over `magazine` (length `m`) to build counts, one pass over `ransomNote` (length `n`) to consume them.
**Space:** O(1) auxiliary — the `int[26]` array is a fixed constant size regardless of input length.

This is optimal: every character of both strings must be inspected at least once to know the answer, so O(m + n) is the best possible time complexity, and the fixed alphabet size means the array never grows with input, making the space cost truly constant.

## 6. Dry Run

`ransomNote = "aa"`, `magazine = "aab"`

Build `available` from `magazine`: `a`→1, `a`→2, `b`→1. So `available['a'-'a']=2`, `available['b'-'a']=1`, rest 0.

| ransomNote char | available['a'-'a'] before | after decrement | negative? | action |
|---|---|---|---|---|
| 'a' | 2 | 1 | no | continue |
| 'a' | 1 | 0 | no | continue |

Exit condition: loop finishes scanning all of `ransomNote` without any count going negative. Final answer: `true`, matching the expected output.

Contrast with `ransomNote = "aa"`, `magazine = "ab"`: `available['a'-'a']` starts at 1; after the first `'a'` it becomes `0` (fine), after the second `'a'` it becomes `-1` → return `false` immediately, matching the expected output.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute Force (list search + remove) | O(n · m) | O(m) aux | `indexOf`/`remove` on `ArrayList` are each O(m) |
| Frequency Array | O(m + n) | O(1) aux | Fixed-size 26-element array regardless of input size |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| `ransomNote` longer than `magazine` | Impossible regardless of letter distribution | Early length check returns `false` immediately without scanning |
| `ransomNote` requires more of a letter than `magazine` has | Core failure case the problem is testing | Decrementing `available[c]` below zero triggers immediate `false` |
| `ransomNote` equals `magazine` exactly | Every letter used exactly once, none left over | Counts decrement to exactly zero for every letter; no negative ever occurs |
| `magazine` has extra unused letters | Leftover supply shouldn't affect the answer | Only `ransomNote`'s letters are checked against supply; unused letters in `magazine` are simply never decremented past zero |
| Single-character strings | Minimal input size | Works identically — one increment, one decrement, one comparison |
| All characters identical (e.g. `ransomNote="aaa"`, `magazine="aa"`) | Must detect insufficient supply for a single repeated letter | Third decrement of `available[0]` goes negative, correctly returning `false` |

## 9. Java Notes

- `c - 'a'` maps a lowercase letter directly to an index `0`–`25`; this only works safely because the constraints guarantee both strings consist of lowercase English letters only — no uppercase, digits, or symbols to worry about.
- `int[26]` avoids all autoboxing overhead that a `HashMap<Character, Integer>` would incur, which matters when scanning up to `10^5` characters — this is the preferred idiom whenever the alphabet is small and fixed.
- The early `ransomNote.length() > magazine.length()` check is a cheap O(1) short-circuit that avoids doing any per-character work when the answer is trivially `false`; it's an optimization, not a correctness requirement (the main loop would still return `false` correctly without it).
- `String.toCharArray()` allocates a new `char[]` copy of the string; for very hot loops, iterating with `charAt(i)` in an indexed `for` loop avoids this allocation, though for a single O(n) pass the difference is negligible here.

## 10. Common Mistakes

- Using a `HashMap<Character, Integer>` and forgetting to handle missing keys (calling `.get(c) - 1` when `c` was never put) — throws a `NullPointerException` from unboxing `null`. Fix: either use `int[26]` (which defaults to `0`), or use `getOrDefault(c, 0)` consistently with a `HashMap`.
- Checking `available[c - 'a'] == 0` instead of `< 0` after decrementing — this incorrectly rejects the case where a letter's count reaches exactly zero as the last valid use. Fix: only reject when the count goes negative, not when it merely reaches zero.
- Building the frequency count from `ransomNote` and decrementing using `magazine`, i.e. the counts reversed — this checks the wrong direction (whether magazine's letters fit within ransomNote's needs) and produces incorrect results whenever `magazine` has more total letters than `ransomNote`. Fix: always count supply from `magazine`, then consume it while scanning `ransomNote`.
- Forgetting that letters aren't reusable and instead just checking that every distinct letter in `ransomNote` merely *appears* in `magazine` (set membership) — this passes cases it shouldn't, like `ransomNote="aa"`, `magazine="a"`. Fix: use full counts, not just presence/absence.

## 11. Interview Takeaway

- **Trigger sentence:** "Can string A be built from string B's letters, each used once, small fixed alphabet → frequency array, count supply from B, consume while scanning A."
- **90-second explanation:** "I count how many of each letter `magazine` provides using a fixed 26-element array, since the alphabet is just lowercase English letters. Then I scan `ransomNote`, decrementing the count for each letter it needs. If any letter's count ever goes negative, that means `ransomNote` needs more of that letter than `magazine` has left, so I return false immediately. If I get through the whole note without going negative, every letter had enough supply, so I return true. As a quick short-circuit, if `ransomNote` is longer than `magazine` outright, it can never be constructed, so I check that up front."
- **Related problems:** 242 (Valid Anagram), 49 (Group Anagrams), 438 (Find All Anagrams in a String), 387 (First Unique Character in a String).

## 12. Recall Questions

**Q:** Why does the algorithm only need to check for a negative count rather than comparing full frequency maps at the end?
**A:** Decrementing while scanning and checking for a negative result at each step is mathematically equivalent to comparing final counts, but lets the algorithm exit as soon as it's certain the answer is `false`, rather than always processing the entire string.

**Q:** Why is `int[26]` preferred over a `HashMap<Character, Integer>` for this problem specifically?
**A:** The constraints guarantee only lowercase English letters, a fixed alphabet of exactly 26 characters, so a small fixed-size array gives O(1) direct indexing without any hashing or autoboxing overhead that a general-purpose map would incur.

**Q:** Why must the frequency counts come from `magazine` first, and then be consumed by scanning `ransomNote`, rather than the other way around?
**A:** The direction of the check matters: the question is whether `ransomNote`'s letter demands fit within `magazine`'s letter supply, so `magazine` must be the source of available counts and `ransomNote` the consumer of them; reversing this checks an unrelated condition.

**Q:** Why is a plain set-membership check (does this letter appear anywhere in `magazine`?) insufficient here?
**A:** The problem requires each letter in `magazine` to be usable only once, so `ransomNote` could legitimately fail even if every one of its distinct letters appears somewhere in `magazine`, simply because it needs more copies of a letter than `magazine` actually has.

**Q:** What does the early `ransomNote.length() > magazine.length()` check optimize, and is it required for correctness?
**A:** It's a constant-time short-circuit for an unwinnable case — the main frequency-counting logic would still correctly return `false` without it, since some letter's count would eventually go negative, but the early check avoids unnecessary work.

## 13. Final Code

```java
class Solution {
    public boolean canConstruct(String ransomNote, String magazine) {
        if (ransomNote.length() > magazine.length()) {
            return false;
        }

        int[] available = new int[26];
        for (char c : magazine.toCharArray()) {
            available[c - 'a']++;
        }

        for (char c : ransomNote.toCharArray()) {
            available[c - 'a']--;
            if (available[c - 'a'] < 0) {
                return false;
            }
        }

        return true;
    }
}
```

## 14. Self-Test

Given two lowercase-letter strings, `ransomNote` and `magazine`, determine whether `ransomNote` can be built using `magazine`'s letters, each letter usable only once. Re-derive: count each letter's supply from `magazine` in a fixed 26-element array, then scan `ransomNote` decrementing that supply per letter, returning `false` immediately if any count goes negative, else `true`.
