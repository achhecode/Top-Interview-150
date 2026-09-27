---
problem: 3 - Longest Substring Without Repeating Characters
url: https://leetcode.com/problems/longest-substring-without-repeating-characters/
difficulty: Medium
section: String / Sliding Window
patterns: [sliding-window, hashmap]
data_structures: [hashmap, string]
time: O(n)
space: O(min(n,m))
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** a string `s`.

**Required:** find the length of the longest contiguous substring (not subsequence) that contains no repeated characters.

**Constraints that matter:**
- `0 <= s.length <= 10^5` — an O(n²) or O(n³) brute force (checking every substring for uniqueness) is too slow; O(n) is expected.
- `s` can contain English letters, digits, symbols, and spaces — the character set is not restricted to lowercase letters, so a fixed-size array indexed by `c - 'a'` is unsafe; a `HashMap` (or a full 256/128-size array for extended ASCII) is the safe choice.
- Empty string is explicitly allowed (`s.length >= 0`), so the answer must correctly be `0` for `""`.

## 2. Recognition Signals

- "Longest/shortest substring" + "no repeating characters" (a uniqueness constraint on the window's contents, not just its sum) → sliding window over a hash-based "seen" structure.
- The phrase "substring" (contiguous) as opposed to "subsequence" (not necessarily contiguous) rules out DP-over-subsequences approaches and confirms a window-based scan.
- The condition being checked ("no duplicates in window") is naturally violated by adding one specific character, so we don't need to shrink char-by-char blindly — we can jump `left` directly past the offending duplicate once we know where it last occurred.
- Named pattern: **Variable-Size Sliding Window with a Hash Map for last-seen positions**.

## 3. Core Idea

Slide a window `[left, right]` across the string. Track the most recent index at which each character was seen. When extending `right` lands on a character already inside the current window, jump `left` forward to one position past that character's last occurrence — this is the minimal shrink needed to restore uniqueness, so we never need to shrink one character at a time.

**Why it's correct:** the window `[left, right]` is unique-character by construction as an invariant maintained at every step. When `s.charAt(right)` was last seen at index `p`, the window can only stay valid if `left` moves past `p`; jumping directly to `p + 1` (never backward, since `left` only ever increases) preserves correctness while doing this in O(1) per step instead of O(window size).

**Invariant:** at the top of each iteration, `s[left..right-1]` contains no duplicate characters, and `lastSeen` accurately reflects the most recent index of every character encountered so far (including ones that have since left the window — stale entries are harmless because the `lastSeen.get(c) >= left` check filters them out).

## 4. Approach 1 — Brute Force

1. For every starting index `i`, extend `j` from `i` forward, tracking seen characters in a `Set`.
2. As soon as a duplicate is found, stop extending for this `i` and record the length seen so far.
3. Track the global maximum length across all `i`.

```java
public int lengthOfLongestSubstringBruteForce(String s) {
    int n = s.length();
    int maxLength = 0;

    for (int i = 0; i < n; i++) {
        Set<Character> seen = new HashSet<>();
        int j = i;
        while (j < n && !seen.contains(s.charAt(j))) {
            seen.add(s.charAt(j));
            j++;
        }
        maxLength = Math.max(maxLength, j - i);
    }

    return maxLength;
}
```

**Time:** O(n²) — for each of n starting points, the inner loop can scan up to n characters in the worst case (e.g., all-unique string).
**Space:** O(min(n, m)) auxiliary for the `Set`, where `m` is the size of the character set.

This is not optimal: with `n = 10^5`, O(n²) is ~10^10 operations, too slow. It also discards all information about seen characters between outer iterations, redoing work that the optimized approach reuses.

## 5. Approach 2 — Optimized (Sliding Window + HashMap of Last-Seen Index)

1. Initialize `left = 0`, `maxLength = 0`, and an empty `Map<Character, Integer> lastSeen`.
2. Iterate `right` from `0` to `n - 1`:
   - Let `c = s.charAt(right)`.
   - If `lastSeen` contains `c` **and** `lastSeen.get(c) >= left` (meaning the duplicate is inside the current window, not a stale entry from before the window started), move `left` to `lastSeen.get(c) + 1`.
   - Update `lastSeen.put(c, right)`.
   - Update `maxLength = max(maxLength, right - left + 1)`.
3. Return `maxLength`.

```java
public int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> lastSeen = new HashMap<>();
    int left = 0;
    int maxLength = 0;

    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);

        if (lastSeen.containsKey(c) && lastSeen.get(c) >= left) {
            left = lastSeen.get(c) + 1;
        }

        lastSeen.put(c, right);
        maxLength = Math.max(maxLength, right - left + 1);
    }

    return maxLength;
}
```

**Time:** O(n) — `right` visits each character once; `left` only ever moves forward, and each map operation is O(1) average.
**Space:** O(min(n, m)) auxiliary, where `m` is the size of the possible character set — the map holds at most one entry per distinct character seen.

This is optimal: every character must be inspected at least once, so O(n) is the best possible time complexity, and the trade-off is O(min(n, m)) space to avoid re-scanning the window on every duplicate — a small, bounded cost for a large speed gain over brute force.

## 6. Dry Run

`s = "pwwkew"`

| right (char) | lastSeen has char in window? | left before | left after | maxLength |
|---|---|---|---|---|
| 0 ('p') | no | 0 | 0 | 1 |
| 1 ('w') | no | 0 | 0 | 2 |
| 2 ('w') | yes, at index 1 (>= left=0) | 0 | 2 | 2 |
| 3 ('k') | no | 2 | 2 | 2 |
| 4 ('e') | no | 2 | 2 | 3 |
| 5 ('w') | yes, at index 2, but 2 < left=2? → 2 >= 2 is true | 2 | 3 | 3 |

Exit: loop ends when `right` reaches `n`. Final answer: `maxLength = 3`, matching `"wke"`.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute Force | O(n²) | O(min(n,m)) aux | `Set` rebuilt per outer iteration |
| Sliding Window + HashMap | O(n) | O(min(n,m)) aux | Map holds at most one entry per distinct character |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Empty string `""` | Loop body never runs | `maxLength` initialized to `0` and returned unchanged |
| Single character | Trivial window of length 1 | Loop runs once, `maxLength` becomes 1 |
| All identical characters (`"bbbbb"`) | Window must repeatedly shrink to size 1 | Each repeat triggers `left = lastSeen.get(c) + 1`, keeping window at length 1 |
| All unique characters | Window should never shrink | `lastSeen.containsKey(c)` never true for the first-time-seen path, or the stored index is always `< left` after the first pass — window grows to full string length |
| Duplicate character *outside* the current window | Stale map entry could wrongly shrink the window | The `lastSeen.get(c) >= left` check ignores duplicates that occurred before the window started |
| Non-letter characters (digits, symbols, spaces) | Fixed-size `int[26]` array (letters-only) would be unsafe or insufficient | `HashMap<Character, Integer>` handles any `char` value, not just lowercase letters |

## 9. Java Notes

- `s consists of English letters, digits, symbols and spaces` rules out a simple `int[26]` array keyed on `c - 'a'`; a `HashMap<Character, Integer>` (or a full 128/256-size array for ASCII) is required instead.
- `lastSeen.get(c) >= left` is essential, not optional — without the `>= left` check, a character seen before the current window started would incorrectly force `left` backward-looking logic to jump forward based on stale data, though since `left` only moves forward this specific bug would manifest as jumping `left` to a position *behind* its current value, which needs an explicit `Math.max` guard if the `>= left` check is omitted.
- `Map.containsKey` + `Map.get` is two lookups; using `getOrDefault(c, -1)` collapses this into one lookup and naturally handles "never seen" as `-1`, which is always `< left` (since `left >= 0`), removing the need for a separate `containsKey` check.
- `HashMap<Character, Integer>` autoboxes both keys and values — for a hot loop over up to 10^5 characters this is a minor but real overhead; an `int[128]` (or `int[256]`) array initialized to `-1` avoids boxing entirely and is the faster idiomatic choice when the character set is known to be bounded ASCII.

## 10. Common Mistakes

- Forgetting the `>= left` (or equivalent `getOrDefault(c, -1) >= left`) check and unconditionally jumping `left` on any previously-seen character, even one outside the current window — silently shrinks the window too aggressively and undercounts the true answer. Fix: only jump `left` if the last occurrence is still within `[left, right]`.
- Moving `left` forward by one character at a time in a `while` loop (mimicking the "shrink one at a time" pattern from sum-based sliding window problems) instead of jumping directly via the map — still correct, but degrades to O(n·m) in pathological cases and misses the point of using last-seen indices. Fix: jump `left` directly to `lastSeen.get(c) + 1`.
- Computing length as `right - left` instead of `right - left + 1` — off-by-one that undercounts every window by 1. Fix: window `[left, right]` inclusive has length `right - left + 1`.
- Using `Character` equality with `==` when keys came from autoboxing outside the cached `-128..127` range — not an issue for `char` primitives used directly as `HashMap` keys (autoboxing to `Character` happens correctly via `.equals()` in map lookups), but worth knowing this pitfall exists for `Integer`-keyed maps in general.

## 11. Interview Takeaway

- **Trigger sentence:** "Longest/shortest contiguous substring with a uniqueness (no-duplicate) constraint → sliding window with a hash map tracking each character's last-seen index."
- **90-second explanation:** "I scan the string with two pointers marking a window that never contains a duplicate character. I keep a hash map of the last index each character was seen at. As I extend the right pointer, if the current character was already seen at an index inside the current window, I jump the left pointer directly to one past that occurrence instead of shrinking one character at a time — that keeps the whole scan linear. After updating the map with the character's new position, I update the answer with the current window's length. Because the character set includes more than just lowercase letters, I use a hash map rather than a fixed 26-size array."
- **Related problems:** 76 (Minimum Window Substring), 209 (Minimum Size Subarray Sum), 340 (Longest Substring with At Most K Distinct Characters), 424 (Longest Repeating Character Replacement), 1004 (Max Consecutive Ones III).

## 12. Recall Questions

**Q:** Why is it safe to jump `left` directly to `lastSeen.get(c) + 1` instead of shrinking the window one character at a time?
**A:** Because we already know exactly where the duplicate character last occurred, jumping there in one step is the minimal shrink needed to restore uniqueness — there's no shorter valid window between the old `left` and that point, so checking each intermediate position would be wasted work.

**Q:** Why do we need the `lastSeen.get(c) >= left` check instead of just checking `containsKey(c)`?
**A:** A character could have been seen earlier in the string but already fallen outside the current window (its last occurrence is before `left`); without the `>= left` guard, that stale occurrence would incorrectly force the window to shrink even though there's no duplicate in the current window.

**Q:** Why can't a fixed-size `int[26]` array safely replace the `HashMap` for this specific problem?
**A:** The problem states the string can contain digits, symbols, and spaces in addition to English letters, so the character set isn't limited to 26 lowercase letters, and a `c - 'a'` index could go out of bounds or collide.

**Q:** Why does the window length formula use `right - left + 1` rather than `right - left`?
**A:** Both `left` and `right` are inclusive indices of the current window, so the count of characters between them is one more than their difference.

**Q:** What invariant does the algorithm maintain at the start of every iteration of the loop?
**A:** The substring `s[left..right-1]` (everything added so far, before the current character) contains no duplicate characters, and the map accurately reflects the most recent index of every character encountered.

**Q:** How would the "last-seen index jump" version outperform a naive "shrink one character per step" sliding window in the worst case?
**A:** The naive shrink-by-one version can still take O(n) total shrinks amortized in many cases, but the jump version guarantees each map lookup directly resolves the correct new `left` in O(1), avoiding any scenario where repeated identical duplicates force many small shrink steps in sequence within a single outer iteration.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> lastSeen = new HashMap<>();
        int left = 0;
        int maxLength = 0;

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            int previousIndex = lastSeen.getOrDefault(c, -1);

            if (previousIndex >= left) {
                left = previousIndex + 1;
            }

            lastSeen.put(c, right);
            maxLength = Math.max(maxLength, right - left + 1);
        }

        return maxLength;
    }
}
```

## 14. Self-Test

Given a string, find the length of the longest contiguous substring with no repeated characters (empty string allowed, character set is not limited to lowercase letters). Re-derive: slide a window with two pointers, keep a hash map of each character's last-seen index, and whenever the current character's last occurrence falls inside the current window, jump the left pointer directly past it; update the map and the running maximum length after every step.
