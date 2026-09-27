---
problem: 13 - Roman to Integer
url: https://leetcode.com/problems/roman-to-integer/
difficulty: Easy
section: String / Hash Map
patterns: [single-pass, lookahead-comparison]
data_structures: [string, hashmap]
time: O(n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** A string `s` representing a valid Roman numeral (guaranteed to represent a value in `[1, 3999]`).

**Required:** Convert it to its integer value. Roman numerals are normally read largest-to-smallest left to right (additive), except for six specific subtractive pairs (IV, IX, XL, XC, CD, CM) where a smaller-value symbol placed before a larger one means "subtract the smaller from the larger."

**Constraints that actually matter:**
- `1 <= s.length <= 15` — tiny input; performance is a non-issue here, so the focus is entirely on correctness of the parsing logic, especially the subtractive cases.
- `s` contains only the seven fixed symbols (`I, V, X, L, C, D, M`) — a small, fixed alphabet, which makes a direct symbol-to-value lookup (array or hash map) both simple and efficient, no need for general parsing.
- **`s` is guaranteed to be a valid Roman numeral** — this removes the need for any validation logic (e.g., you never need to detect or reject malformed input like "IIII" or "VV"); the algorithm can assume the six subtractive patterns are the only "smaller before larger" cases that will ever appear.

## 2. Recognition Signals

- "Convert Roman numeral to integer" + "smaller symbol before larger means subtract" → the specific **lookahead comparison** technique: at each symbol, compare its value to the *next* symbol's value to decide whether to add or subtract.
- A small, fixed set of symbol-to-value mappings → strongly suggests a `Map<Character, Integer>` (or equivalent lookup array) rather than a chain of `if`/`else` on characters.
- Whenever a sequence's interpretation at each position depends on comparing it to its immediate neighbor (here, the *next* character) to decide between two operations (add vs. subtract) — this single-pass lookahead pattern is the general technique, seen in various symbol-parsing problems.

**Pattern:** Single-Pass Lookahead Comparison (scan left to right, and at each symbol, peek at the next symbol to decide whether the current symbol's value should be added or subtracted).

## 3. Core Idea

Scan the string left to right. At each position `i`, compare `value(s[i])` to `value(s[i+1])` (when a next character exists). If the current symbol's value is **less than** the next symbol's value, this is one of the six subtractive pairs (e.g., "IV"), so subtract the current symbol's value from the running total instead of adding it. Otherwise, add it normally. Since the input is guaranteed valid, this simple local comparison is always sufficient — there's no need to explicitly enumerate or special-case the six specific pairs (IV, IX, XL, XC, CD, CM); the general "smaller before larger means subtract" rule captures all of them uniformly.

**Invariant:** After processing position `i`, the running total correctly equals the integer value of the prefix `s[0..i]`, since each symbol's contribution (add or subtract) is fully and correctly determined by its relationship to the single symbol immediately following it.

## 4. Approach 1 — Brute Force (Explicit Pair Matching)

**Logic:** Explicitly check for each of the six two-character subtractive substrings at every position first, consuming two characters and adding the combined value when found; otherwise consume one character and add its single value.

```java
public int romanToInt(String s) {
    Map<String, Integer> pairValues = new HashMap<>();
    pairValues.put("IV", 4);
    pairValues.put("IX", 9);
    pairValues.put("XL", 40);
    pairValues.put("XC", 90);
    pairValues.put("CD", 400);
    pairValues.put("CM", 900);

    Map<Character, Integer> singleValues = new HashMap<>();
    singleValues.put('I', 1);
    singleValues.put('V', 5);
    singleValues.put('X', 10);
    singleValues.put('L', 50);
    singleValues.put('C', 100);
    singleValues.put('D', 500);
    singleValues.put('M', 1000);

    int total = 0;
    int i = 0;
    while (i < s.length()) {
        if (i + 1 < s.length() && pairValues.containsKey(s.substring(i, i + 2))) {
            total += pairValues.get(s.substring(i, i + 2));
            i += 2;
        } else {
            total += singleValues.get(s.charAt(i));
            i += 1;
        }
    }
    return total;
}
```

- **Time:** O(n) — single pass, though with some constant-factor overhead from substring creation and two hash map lookups per iteration.
- **Space:** O(1) auxiliary beyond the two fixed-size lookup maps (which are themselves O(1), since they always contain the same small, bounded number of entries regardless of input size).

**Why it is not optimal:** It's already O(n) time and technically correct, but it's needlessly verbose — maintaining two separate maps and explicit substring matching duplicates logic that a single, more elegant lookahead comparison rule captures automatically. It's a reasonable first-pass solution to state, but the optimized version is simpler, cleaner, and avoids substring allocation overhead entirely.

## 5. Approach 2 — Optimized (Single-Pass Lookahead Comparison)

**Algorithm:**
1. Build a single `Map<Character, Integer>` (or a lookup array) mapping each of the seven symbols to its value.
2. Initialize `total = 0`.
3. For each index `i` from `0` to `s.length() - 1`:
   - Look up `current = value(s.charAt(i))`.
   - If `i + 1 < s.length()` and `current < value(s.charAt(i + 1))`, subtract `current` from `total` (this position is the smaller half of a subtractive pair).
   - Otherwise, add `current` to `total`.
4. Return `total`.

```java
public int romanToInt(String s) {
    Map<Character, Integer> values = new HashMap<>();
    values.put('I', 1);
    values.put('V', 5);
    values.put('X', 10);
    values.put('L', 50);
    values.put('C', 100);
    values.put('D', 500);
    values.put('M', 1000);

    int total = 0;
    int n = s.length();

    for (int i = 0; i < n; i++) {
        int current = values.get(s.charAt(i));
        if (i + 1 < n && current < values.get(s.charAt(i + 1))) {
            total -= current;
        } else {
            total += current;
        }
    }

    return total;
}
```

- **Time:** O(n) — single pass, O(1) work per character (constant-time map lookups, no substring allocation).
- **Space:** O(1) auxiliary — the symbol-value map has a fixed size of 7 entries regardless of input length, and no other data structures are used.

**Why this is optimal:** Every character must be examined at least once to determine its contribution, so O(n) is a hard lower bound — matching what this single pass achieves. Space is O(1) because the lookup table's size is fixed and independent of the input string's length, and no substrings or intermediate collections are created. This is simpler and slightly more efficient in practice than Approach 1, since it avoids substring allocation and redundant map lookups, while expressing the same underlying logic (smaller-before-larger means subtract) through a single unified rule rather than explicit pair enumeration.

## 6. Dry Run

Example: `s = "MCMXCIV"`.
Chosen because it's the canonical example, and it exercises three separate subtractive pairs (CM, XC, IV) interspersed with a plain additive symbol (M), covering the full range of behavior.

`n = 7`. Initial: `total = 0`.

| i | s[i] | current | s[i+1] (if exists) | current < next? | Action | total after |
|---|------|---------|--------------------------|----------------------|--------|----------------|
| 0 | M | 1000 | C (100) | No (1000 < 100 false) | total += 1000 | 1000 |
| 1 | C | 100 | M (1000) | Yes (100 < 1000) | total -= 100 | 900 |
| 2 | M | 1000 | X (10) | No | total += 1000 | 1900 |
| 3 | X | 10 | C (100) | Yes (10 < 100) | total -= 10 | 1890 |
| 4 | C | 100 | I (1) | No | total += 100 | 1990 |
| 5 | I | 1 | V (5) | Yes (1 < 5) | total -= 1 | 1989 |
| 6 | V | 5 | (none, last char) | N/A (no next char) | total += 5 | 1994 |

Exit condition: `i` reaches `n` (7), loop ends.

**Final answer:** `total = 1994`, matching the expected output exactly, and consistent with the problem's own breakdown: M=1000, CM=900, XC=90, IV=4 (1000+900+90+4=1994).

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Explicit pair matching | O(n) | O(1) (fixed-size maps) | Correct but more verbose; substring allocation adds constant-factor overhead |
| 2. Single-pass lookahead comparison | O(n) | O(1) (fixed-size map) | Optimal and simpler; the general subtract-if-smaller-than-next rule replaces explicit pair enumeration |

Input/output space for `s` is O(n) in both, as given by the problem (and trivially small given `n <= 15`).

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single-character input (e.g. `"I"`, `"M"`) | No next character exists; must not attempt to look ahead out of bounds | The condition `i + 1 < n` correctly guards against accessing `s.charAt(i + 1)` when `i` is the last index, safely falling through to the "add" branch |
| The last character of the string being part of a subtractive pair's second half (e.g. `"IV"`, where V is the last char) | The second character of a pair should never itself trigger a subtraction (it's always the larger value in a valid pair, and it's evaluated on its own merit against whatever comes after *it*, if anything) | Since the comparison is always "current vs. the *next* character," the second character of a pair is only ever evaluated by looking at what follows it (or nothing, if it's the last character) — its own status as "part of a pair" is implicitly and correctly handled without needing explicit pair tracking |
| All-additive numerals with no subtractive pairs (e.g. `"III"`, `"LVIII"`) | Should behave as simple summation with no subtraction ever triggered | Every comparison `current < next` (or the no-next-character case) evaluates such that the "add" branch is always taken, since no symbol in these examples is smaller than the one following it |
| Repeated identical symbols (e.g. `"III"`, `"XXX"`) | Each occurrence should simply add its own value, since a symbol is never smaller than itself | `current < value(s.charAt(i+1))` is false when both characters are identical (equal values aren't "less than"), so each repeated symbol correctly falls into the "add" branch |
| Numerals near the guaranteed boundary values (1 and 3999, e.g. `"I"` or `"MMMCMXCIX"`) | Must correctly handle both the smallest and largest valid inputs within the guaranteed range | The algorithm's logic is uniform regardless of the numeral's overall magnitude — it only ever reasons about pairs of adjacent characters, so extreme values within the guaranteed valid range are handled identically to any other valid input |
| Multiple consecutive subtractive pairs immediately adjacent (as in the dry run's CM, XC, IV sequence) | Must correctly handle transitions between pairs without any interference or leftover state | Since each character's add/subtract decision depends only on itself and the single character immediately following it (no persistent state carried beyond the running `total`), consecutive pairs are handled correctly and independently, exactly as shown in the dry run |

## 9. Java Notes

- **`Map<Character, Integer>` for symbol lookup:** a clean, idiomatic choice for a small, fixed set of character-to-value mappings; a `switch` statement or a pre-sized array indexed by character offset would also work and might be marginally faster, but the map is clear and simple given the tiny, fixed alphabet involved.
- **Avoiding `s.substring(i, i+2)` in the optimized approach:** unlike Approach 1, the optimized version never allocates substrings, comparing individual `char` values via map lookups instead — a minor but real efficiency and cleanliness improvement, especially relevant if this pattern were applied to a much larger input in a different context.
- **No overflow risk:** since the guaranteed value range is `[1, 3999]`, the running `total` never approaches anywhere near `int` overflow territory, so no special numeric handling is needed.
- **Guard `i + 1 < n` before accessing `s.charAt(i + 1)`:** essential to avoid a `StringIndexOutOfBoundsException` when processing the last character of the string; short-circuit evaluation of `&&` ensures the length check happens before the potentially out-of-bounds access is attempted.

## 10. Common Mistakes

- **Forgetting the bounds check before looking ahead to the next character.** Attempting `s.charAt(i + 1)` on the last character of the string without first confirming `i + 1 < n` throws a runtime exception. Fix: always guard the lookahead with an explicit length check, relying on short-circuit `&&` evaluation.
- **Explicitly hardcoding the six subtractive pairs instead of using the general "subtract if smaller than next" comparison.** This works (as shown in Approach 1) but is more verbose and error-prone to get exactly right (all six pairs must be enumerated correctly), when a single unified comparison rule handles all of them automatically. Fix: prefer the lookahead comparison approach for its simplicity, unless there's a specific reason to enumerate pairs explicitly.
- **Processing the string right to left with a "previous max value seen" tracking approach without carefully getting the comparison direction right.** This is a valid alternative technique (common in some accepted solutions), but mixing up whether to add or subtract based on comparing to the previous maximum, rather than to the immediate right neighbor, can introduce subtle bugs if not implemented carefully. Fix: if using a right-to-left approach, be precise about comparing each symbol to the running maximum of everything processed so far (from the right), not to an arbitrary neighbor.
- **Assuming validation of the input is necessary (e.g., checking for four consecutive identical symbols or invalid pair combinations).** The problem explicitly guarantees the input is always a valid Roman numeral, so adding defensive validation logic is unnecessary extra complexity for this specific problem. Fix: trust the guarantee and keep the parsing logic focused purely on correct conversion.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Roman numeral to integer, smaller symbol before larger means subtract → single pass, compare each symbol's value to the next symbol's value to decide add vs. subtract."
- **90-second explanation:** "I map each of the seven Roman symbols to its integer value using a small lookup table. Then I scan the string once, and at each position, I compare the current symbol's value to the value of the symbol immediately after it. If the current symbol's value is smaller than the next one's, that's exactly the subtractive pattern — like the I in IV — so I subtract it from my running total instead of adding it. Otherwise, I just add it normally. Since the problem guarantees the input is always a valid Roman numeral, I don't need to explicitly enumerate or validate the six specific subtractive pairs — this single local comparison rule naturally captures all of them. This runs in O(n) time with a single pass and O(1) space, since the lookup table's size is fixed regardless of the input string's length."
- **Related problems using this pattern:**
  - LeetCode 12 — Integer to Roman (the inverse conversion, typically solved with a greedy symbol-subtraction approach)
  - LeetCode 273 — Integer to English Words (different domain, but similarly a fixed-vocabulary conversion problem)
  - LeetCode 8 — String to Integer (atoi) (different parsing logic, but similarly a single-pass character-by-character conversion)

## 12. Recall Questions

**Q:** Why does comparing each symbol's value only to the single symbol immediately following it correctly capture all six subtractive pairs without needing to enumerate them explicitly?
**A:** All six subtractive pairs share the same underlying structural property — a smaller-value symbol immediately followed by a larger-value symbol — so a single general comparison rule (subtract if the current value is less than the next) uniformly detects every instance of this pattern regardless of which specific pair it is.

**Q:** Why is it safe to assume the current symbol is never larger than the next symbol when they're part of a legitimate additive sequence (like "III" or "XX")?
**A:** In additive sequences, symbols of equal or decreasing value are written left to right, so the current symbol's value is always greater than or equal to the next symbol's value in those cases, correctly triggering the "add" branch rather than "subtract."

**Q:** What would happen if you forgot the `i + 1 < n` bounds check before comparing to the next character?
**A:** Attempting to access a character beyond the end of the string when processing the last character would throw a `StringIndexOutOfBoundsException`, crashing the program instead of gracefully falling through to the "add" branch for that final symbol.

**Q:** Why doesn't this algorithm need any explicit validation logic to detect malformed Roman numerals?
**A:** The problem statement explicitly guarantees that the input string is always a valid Roman numeral representing a value in the range [1, 3999], so the algorithm can safely assume every "smaller before larger" pattern it encounters is a legitimate subtractive pair, without needing to check for or reject invalid numeral sequences.

**Q:** How would the comparison logic need to change if you processed the Roman numeral from right to left instead of left to right?
**A:** You'd need to track the maximum value seen so far (from the right), and add the current symbol's value if it's greater than or equal to that running maximum, or subtract it if it's smaller — comparing to a running maximum rather than to a single immediate neighbor, since you no longer have direct access to "the next character" in that direction.

## 13. Final Code

```java
public int romanToInt(String s) {
    Map<Character, Integer> values = new HashMap<>();
    values.put('I', 1);
    values.put('V', 5);
    values.put('X', 10);
    values.put('L', 50);
    values.put('C', 100);
    values.put('D', 500);
    values.put('M', 1000);

    int total = 0;
    int n = s.length();

    for (int i = 0; i < n; i++) {
        int current = values.get(s.charAt(i));
        if (i + 1 < n && current < values.get(s.charAt(i + 1))) {
            total -= current;
        } else {
            total += current;
        }
    }

    return total;
}
```

## 14. Self-Test

You have a valid Roman numeral string `s`. Convert it to an integer, where a smaller-value symbol immediately before a larger-value one means subtract instead of add. Think about why comparing each symbol only to the single symbol immediately following it is enough to correctly handle every subtractive case, without needing to explicitly enumerate the six special pairs.
