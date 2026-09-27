---
problem: 76 - Minimum Window Substring
url: https://leetcode.com/problems/minimum-window-substring/
difficulty: Hard
section: String / Sliding Window
patterns: [sliding-window, hashmap]
data_structures: [hashmap, string]
time: O(m + n)
space: O(m + n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** two strings `s` (length `m`) and `t` (length `n`).

**Required:** find the shortest contiguous substring of `s` that contains every character of `t`, including duplicates (i.e., if `t` has two `'a'`s, the window must contain at least two `'a'`s). Return `""` if no such window exists. The answer is guaranteed unique.

**Constraints that matter:**
- `1 <= m, n <= 10^5` — an O(m² ) or worse approach (checking every substring against `t`'s requirements from scratch) is too slow; the follow-up explicitly asks for O(m + n), confirming a single linear pass is the target.
- `s` and `t` consist of uppercase **and** lowercase English letters — 52 possible characters, not just 26, so any fixed-size array solution needs to account for both cases (or just use a `HashMap`/128-size array to stay safe).
- "Including duplicates" is the crux of the problem — this isn't "does the window contain each distinct character," it's "does the window contain at least as many of each character as `t` requires," which is a multiset-containment check.

## 2. Recognition Signals

- "Minimum/shortest window" + "contains all characters of another string (with counts)" → the canonical **variable-size sliding window with frequency matching** problem.
- The phrase "including duplicates" is a strong signal to use a **frequency map**, not a `Set`, since membership alone can't express "at least two of this character."
- The task alternates between two goals — grow the window until it's valid, then shrink it while it stays valid — which is the classic "expand to satisfy, contract to minimize" two-pointer shape.
- Named pattern: **Minimum Window / Shrinkable Sliding Window with Character-Count Matching**.

## 3. Core Idea

Track how many of each required character (from `t`) the current window `[left, right]` holds, plus a single counter `formed` for how many distinct required characters currently meet or exceed their required count. Expand `right` until `formed` equals the total number of distinct required characters (the window is now valid), then greedily shrink `left` as far as possible while the window stays valid, recording the shortest valid window seen.

**Why it's correct:** a window is valid exactly when, for every character in `t`, the window's count of that character is `>=` its required count. Instead of re-scanning the whole window to check this condition every time, maintaining a single `formed` counter that increments/decrements only when a specific character's count crosses its required threshold turns the validity check into an O(1) comparison at every step.

**Invariant:** `formed` always equals the number of distinct characters `c` in `t` for which `windowCount[c] >= need[c]` holds for the current window `[left, right]`; the window is valid exactly when `formed == need.size()`.

## 4. Approach 1 — Brute Force

1. For every pair of start and end indices `(i, j)` with `i <= j`, extract the substring `s[i..j]`.
2. Check whether that substring contains every character of `t` with sufficient count (build a frequency map of the substring and compare against `t`'s frequency map).
3. Track the shortest substring that passes the check.

```java
public String minWindowBruteForce(String s, String t) {
    if (s.isEmpty() || t.isEmpty()) {
        return "";
    }

    Map<Character, Integer> need = new HashMap<>();
    for (char c : t.toCharArray()) {
        need.merge(c, 1, Integer::sum);
    }

    int bestStart = -1;
    int bestLen = Integer.MAX_VALUE;
    int m = s.length();

    for (int i = 0; i < m; i++) {
        Map<Character, Integer> windowCount = new HashMap<>();
        for (int j = i; j < m; j++) {
            windowCount.merge(s.charAt(j), 1, Integer::sum);
            if (containsAll(windowCount, need) && j - i + 1 < bestLen) {
                bestLen = j - i + 1;
                bestStart = i;
                break; // shortest valid window starting at i found; move to next i
            }
        }
    }

    return bestStart == -1 ? "" : s.substring(bestStart, bestStart + bestLen);
}

private boolean containsAll(Map<Character, Integer> windowCount, Map<Character, Integer> need) {
    for (Map.Entry<Character, Integer> entry : need.entrySet()) {
        if (windowCount.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
            return false;
        }
    }
    return true;
}
```

**Time:** O(m² * k) in the worst case, where `k` is the number of distinct characters in `t` (each `containsAll` check costs O(k), and it's called up to O(m) times per starting index across O(m) starting indices).
**Space:** O(m + k) auxiliary for the window map and the need map.

This is not optimal: with `m = 10^5`, even O(m²) alone is ~10^10, far too slow. It also recomputes window validity from scratch at every extension instead of maintaining it incrementally.

## 5. Approach 2 — Optimized (Sliding Window with a Single `formed` Counter)

1. If `t` is empty or longer than `s`, return `""` immediately.
2. Build `need`: a frequency map of each character in `t`. Let `required = need.size()` (number of distinct characters to satisfy).
3. Initialize `left = 0`, `formed = 0`, an empty `windowCount` map, and `bestLen = Integer.MAX_VALUE`, `bestStart = 0`.
4. Iterate `right` from `0` to `m - 1`:
   - Add `s.charAt(right)` to `windowCount`.
   - If that character is in `need` and `windowCount.get(c)` now exactly equals `need.get(c)`, increment `formed`.
   - While `formed == required` (window is currently valid):
     - If `right - left + 1 < bestLen`, update `bestLen` and `bestStart`.
     - Remove `s.charAt(left)` from `windowCount`; if that character is in `need` and its count now drops below `need.get(c)`, decrement `formed`.
     - Increment `left`.
5. Return `s.substring(bestStart, bestStart + bestLen)` if `bestLen` was updated, else `""`.

```java
public String minWindow(String s, String t) {
    if (s.isEmpty() || t.isEmpty() || s.length() < t.length()) {
        return "";
    }

    Map<Character, Integer> need = new HashMap<>();
    for (char c : t.toCharArray()) {
        need.merge(c, 1, Integer::sum);
    }
    int required = need.size();

    Map<Character, Integer> windowCount = new HashMap<>();
    int left = 0;
    int formed = 0;
    int bestLen = Integer.MAX_VALUE;
    int bestStart = 0;

    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        windowCount.merge(c, 1, Integer::sum);

        if (need.containsKey(c) && windowCount.get(c).intValue() == need.get(c).intValue()) {
            formed++;
        }

        while (formed == required) {
            if (right - left + 1 < bestLen) {
                bestLen = right - left + 1;
                bestStart = left;
            }

            char leftChar = s.charAt(left);
            windowCount.put(leftChar, windowCount.get(leftChar) - 1);
            if (need.containsKey(leftChar) && windowCount.get(leftChar).intValue() < need.get(leftChar).intValue()) {
                formed--;
            }
            left++;
        }
    }

    return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
}
```

**Time:** O(m + n) — building `need` costs O(n); `right` advances m times and `left` advances at most m times total (never resets), with O(1) amortized map work per step, giving O(m) for the scan.
**Space:** O(m + n) — `need` holds at most `n` distinct characters (bounded by 52 possible letters in practice), `windowCount` holds at most as many distinct characters as appear in `s`.

This is optimal: the follow-up explicitly asks for O(m + n), and this achieves it — every character of both strings is processed a constant number of times, and the `formed` counter avoids ever re-scanning the window to check validity.

## 6. Dry Run

`s = "ADOBECODEBANC"`, `t = "ABC"` → `need = {A:1, B:1, C:1}`, `required = 3`

| right (char) | windowCount update | formed | window valid? (shrink) | bestLen / bestStart |
|---|---|---|---|---|
| 0 ('A') | A:1 | 1 | no | ∞ |
| 1 ('D') | D:1 | 1 | no | ∞ |
| 2 ('O') | O:1 | 1 | no | ∞ |
| 3 ('B') | B:1 | 2 | no | ∞ |
| 4 ('E') | E:1 | 2 | no | ∞ |
| 5 ('C') | C:1 | 3 | **yes** → shrink: len=6 (0..5) saved; remove 'A'@0, formed→2, left=1 | 6 / 0 |
| 6 ('O') | O:2 | 2 | no | 6 / 0 |
| 7 ('D') | D:2 | 2 | no | 6 / 0 |
| 8 ('E') | E:2 | 2 | no | 6 / 0 |
| 9 ('B') | B:2 | 2 | no (B already satisfied, formed unchanged since it wasn't newly reaching threshold — B count 2 still >=1, formed stays 2 here because only first time reaching `need` triggers increment) | 6 / 0 |
| 10 ('A') | A:1 | 3 | **yes** → shrink: window is s[1..10]="DOBECODEBA", len=10, not better than 6; remove 'D'@1, formed stays 3 (D not in need); left=2; still formed==3 → remove 'O'@2, left=3; still 3 → remove 'B'@3, B count→1, still >=1 formed stays 3, left=4; still 3 → remove 'E'@4, left=5; still 3 → remove 'C'@5, C count→0 <1, formed→2, left=6 | 6 / 0 (unchanged, no shorter window found here) |
| 11 ('N') | N:1 | 2 | no | 6 / 0 |
| 12 ('C') | C:1 | 3 | **yes** → shrink: window s[6..12]="ODEBANC" wait recompute len=7, not shorter; continue shrinking: remove 'O'@6, left=7; formed stays 3; remove 'D'@7, left=8; remove 'E'@8, left=9; remove 'B'@9, B count→0<1, formed→2, left=10, loop exits | window s[9..12]="BANC" checked at left=9 before removal: len=4, shorter than 6 → **bestLen=4, bestStart=9** |

Exit: `right` reaches `m`. Final answer: `s.substring(9, 13) = "BANC"`, matching the expected output.

*(Note: the table above compresses several shrink iterations per row for readability; each shrink step is a single loop iteration in the actual code.)*

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute Force | O(m² · k) | O(m + k) aux | k = distinct chars in t; recomputes validity from scratch repeatedly |
| Sliding Window + `formed` counter | O(m + n) | O(m + n) aux | Meets the problem's explicit O(m+n) follow-up target |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| `t` longer than `s` | No window can ever satisfy all of `t` | Early check `s.length() < t.length()` returns `""` immediately |
| No valid window exists (e.g. `s="a", t="aa"`) | `bestLen` never updates | `bestLen` stays `Integer.MAX_VALUE`; final check converts this to `""` |
| `t` has duplicate characters (e.g. `t="aa"`) | Window must contain **at least** that many, not just one | `need.get(c)` reflects the full required count; `formed` only increments when the count reaches that exact required threshold |
| Entire `s` is the minimum window (e.g. `s="a", t="a"`) | Shrinking must not remove the only valid window incorrectly | While-loop shrinks only while `formed == required`; once shrinking would break validity, it stops, leaving the correct minimal window recorded |
| Characters in `s` not present in `t` at all | These should be "transparent" — window still valid despite containing them | `windowCount` tracks all characters, but `formed` only reacts to characters present in `need`; extra characters don't block validity |
| Mixed uppercase and lowercase (e.g. `'A'` vs `'a'`) | Must be treated as distinct characters, not case-insensitively merged | `HashMap<Character, Integer>` keys are exact `char` values, so `'A'` and `'a'` are naturally distinct entries |

## 9. Java Notes

- `windowCount.get(c).intValue() == need.get(c).intValue()` explicitly unboxes before comparing — using `==` directly on two `Integer` objects would rely on Java's Integer cache (only guaranteed correct for values `-128` to `127`), which is a subtle bug risk worth naming even though window counts here would often stay small; `.intValue()` (or `.equals()`) sidesteps it entirely.
- `Map.merge(c, 1, Integer::sum)` is the concise idiomatic way to build a frequency map in one line rather than a manual `containsKey`/`put` pair.
- `s` and `t` use both uppercase and lowercase English letters (52 possible characters), so a `HashMap<Character, Integer>` is the safe general-purpose choice; a fixed `int[128]` array (covering standard ASCII) would also work and avoid boxing overhead, trading a small amount of clarity for speed.
- `left` never resets across the outer loop — it's critical for the O(m) bound that both pointers are monotonically non-decreasing across the entire scan, not per outer iteration.
- `s.substring(bestStart, bestStart + bestLen)` allocates a new string only once, at the very end, rather than materializing candidate substrings during the scan — an important efficiency detail since `s.length()` can be up to `10^5`.

## 10. Common Mistakes

- Incrementing `formed` every time a required character is seen in the window, rather than only when its count first reaches the required threshold — this overcounts `formed` and can make the window appear valid before it actually satisfies all duplicate requirements. Fix: only increment when `windowCount.get(c) == need.get(c)` exactly (the crossing point), not on every occurrence.
- Decrementing `formed` every time a required character is removed from the window, rather than only when its count drops **below** the required threshold — this can decrement `formed` even while the window is still technically valid for that character. Fix: only decrement when the post-removal count is strictly less than `need.get(c)`.
- Using `windowCount.get(c) < need.get(c)` when `c` isn't a key in `need` at all — throws a `NullPointerException` from unboxing `null`. Fix: always guard with `need.containsKey(c)` before comparing counts.
- Forgetting that shrinking should happen in a `while` loop (not `if`) — an `if` only shrinks by one character per `right` step, potentially leaving the window larger than necessary and missing the true minimum. Fix: shrink with `while (formed == required)` so the window contracts fully before moving on.

## 11. Interview Takeaway

- **Trigger sentence:** "Shortest substring of `s` containing all characters of `t` (with duplicate counts) → variable-size sliding window with a frequency map and a `formed` counter tracking how many distinct required characters are currently satisfied."
- **90-second explanation:** "I build a frequency map of what `t` requires, then expand a window over `s` with a right pointer, tracking a running frequency map of the window's own contents. I keep a single counter, `formed`, that increments only when a required character's count in the window first reaches its required amount — that way I can check window validity in O(1) instead of rescanning. Once `formed` equals the number of distinct required characters, the window is valid, so I try to shrink it from the left as much as possible, updating the best answer each time, and decrementing `formed` only when a character's count drops below what's required. This way both pointers only move forward across the whole string, giving linear time."
- **Related problems:** 3 (Longest Substring Without Repeating Characters), 209 (Minimum Size Subarray Sum), 438 (Find All Anagrams in a String), 567 (Permutation in String), 30 (Substring with Concatenation of All Words).

## 12. Recall Questions

**Q:** Why is `formed` incremented only when a character's window count exactly reaches its required count, rather than every time that character appears?
**A:** Incrementing on every occurrence would overcount how many distinct requirements are actually satisfied — `formed` needs to represent the number of distinct characters whose count requirement has been met, which only changes at the moment the count crosses the threshold, not on subsequent occurrences.

**Q:** Why must the shrink step use `windowCount.get(leftChar) < need.get(leftChar)` (strictly less than) rather than checking equality to decide when to decrement `formed`?
**A:** The window remains valid for that character as long as its count is still `>=` required; only once removal pushes the count strictly below the required amount does that character's requirement stop being satisfied, which is the exact moment `formed` should drop.

**Q:** Why is a `HashMap` used instead of a fixed-size array here, given the character set is bounded?
**A:** The problem allows both uppercase and lowercase English letters (52 possible characters), so while an array of size 128 (covering ASCII) would also work, a `HashMap` keyed by `Character` is the more general, self-documenting choice without needing to reason about ASCII offsets.

**Q:** Why does the algorithm still run in O(m) despite having a `while` loop nested inside a `for` loop?
**A:** The `left` pointer never resets between outer iterations — across the entire scan it advances at most m times total, just like `right`, so the combined work is linear rather than quadratic.

**Q:** What would go wrong if extra (non-`t`) characters in `s` were also tracked toward `formed`?
**A:** `formed` is deliberately scoped to only the distinct characters present in `need`; characters outside `t` are irrelevant to validity, and including them would make the validity check incorrect since the window's actual requirement is unaffected by unrelated characters.

**Q:** Why is `s.substring(...)` called only once at the very end instead of during the scan?
**A:** Materializing substrings during the scan would cost O(window length) work at every improvement, which for a string up to 10^5 characters could add significant overhead; tracking just the best start index and length as integers keeps every intermediate step O(1) and defers the one necessary allocation to the final result.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public String minWindow(String s, String t) {
        if (s.isEmpty() || t.isEmpty() || s.length() < t.length()) {
            return "";
        }

        Map<Character, Integer> need = new HashMap<>();
        for (char c : t.toCharArray()) {
            need.merge(c, 1, Integer::sum);
        }
        int required = need.size();

        Map<Character, Integer> windowCount = new HashMap<>();
        int left = 0;
        int formed = 0;
        int bestLen = Integer.MAX_VALUE;
        int bestStart = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            windowCount.merge(c, 1, Integer::sum);

            if (need.containsKey(c) && windowCount.get(c).intValue() == need.get(c).intValue()) {
                formed++;
            }

            while (formed == required) {
                if (right - left + 1 < bestLen) {
                    bestLen = right - left + 1;
                    bestStart = left;
                }

                char leftChar = s.charAt(left);
                windowCount.put(leftChar, windowCount.get(leftChar) - 1);
                if (need.containsKey(leftChar) && windowCount.get(leftChar).intValue() < need.get(leftChar).intValue()) {
                    formed--;
                }
                left++;
            }
        }

        return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLen);
    }
}
```

## 14. Self-Test

Given strings `s` and `t`, find the shortest substring of `s` that contains every character of `t`, including duplicate counts, or `""` if none exists. Re-derive: build a required-count map from `t`, slide a window over `s` tracking its own character counts and a `formed` counter that increments only when a required character's window count first reaches its required amount; once `formed` equals the number of distinct required characters, shrink from the left while still valid, recording the shortest window and decrementing `formed` only when a count drops below what's required.
