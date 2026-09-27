---
problem: 12 - Integer to Roman
url: https://leetcode.com/problems/integer-to-roman/
difficulty: Medium
section: String / Greedy
patterns: [greedy, descending-value-table]
data_structures: [array, string]
time: O(1)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An integer `num`.

**Required:** Convert it to its Roman numeral string representation, following the standard rules: largest symbols first, with six specific subtractive shorthand forms (IV=4, IX=9, XL=40, XC=90, CD=400, CM=900) used instead of writing a symbol four times in a row.

**Constraints that actually matter:**
- `1 <= num <= 3999` — a small, fixed, bounded range. This is the single biggest hint: since the input is always within `[1, 3999]`, the entire problem can be solved with a small, fixed lookup table of value/symbol pairs (covering thousands, hundreds, tens, ones, including the six subtractive shorthand values) — no dynamic place-value computation or string-building logic beyond simple greedy subtraction is needed.
- Symbols like V, L, D **cannot be repeated** — only I, X, C, M (powers of 10) can appear up to 3 times consecutively; this is exactly why the subtractive shorthand forms exist (to avoid needing to repeat V, L, or D, or to avoid repeating I, X, C, M a fourth time).
- Since `num <= 3999`, the input never requires more than 3 consecutive M's, and no place value ever needs a "thousands subtractive form" (there's no symbol above M), which is consistent with the guaranteed range.

## 2. Recognition Signals

- "Convert integer to Roman numeral" + fixed, small numeric range (`[1, 3999]`) → the specific signature of the **greedy descending-value table** technique: build a table of every value/symbol pair (including the six subtractive shorthand values) in descending order, then greedily subtract the largest fitting value repeatedly.
- Whenever a "make change" or "represent this quantity using the fewest/largest denominations first" framing appears, and larger denominations are tried before smaller ones — this greedy digit-based representation approach is standard, and is provably correct specifically because the Roman numeral symbol set (including the subtractive shorthand entries) is a "canonical" denomination system for this purpose (unlike, say, arbitrary coin denominations, where greedy isn't always optimal).
- Recognizing that treating IV, IX, XL, XC, CD, CM as **first-class entries in the same table as I, V, X, L, C, D, M** (rather than as a special case handled separately) unifies the entire algorithm into a single greedy loop.

**Pattern:** Greedy Table-Driven Conversion (a specialized greedy technique: iterate through value/symbol pairs from largest to smallest, repeatedly subtracting and appending while the value still fits).

## 3. Core Idea

Roman numerals, including their six subtractive shorthand forms, can be treated as a single unified list of (value, symbol) pairs sorted from largest to smallest: `[(1000,"M"), (900,"CM"), (500,"D"), (400,"CD"), (100,"C"), (90,"XC"), (50,"L"), (40,"XL"), (10,"X"), (9,"IX"), (5,"V"), (4,"IV"), (1,"I")]`. Walking through this list in order and greedily appending the symbol (and subtracting its value from `num`) as many times as it still fits, before moving to the next smaller value, always produces the correct, minimal-length Roman numeral. This works because each subtractive shorthand entry (like 900 for "CM") is deliberately placed in the table at exactly the position its value warrants, so the greedy process naturally reaches for it exactly when appropriate, without needing separate logic to detect "does this place value start with a 4 or 9."

**Invariant:** After processing all table entries with value `>= v`, the portion of `num` reducible using symbols of value `>= v` has been fully extracted and appended to the result in the correct order, and the remaining `num` is strictly less than `v`, ready to be handled by smaller-value entries.

## 4. Approach 1 — Brute Force (Explicit Place-Value Decomposition)

**Logic:** Explicitly break `num` into its thousands, hundreds, tens, and ones digits, and use four separate small lookup tables (one per place value) to convert each digit (0-9) into its correct Roman numeral fragment, then concatenate.

```java
public String intToRoman(int num) {
    String[] thousands = {"", "M", "MM", "MMM"};
    String[] hundreds = {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    String[] tens = {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    String[] ones = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    return thousands[num / 1000]
         + hundreds[(num % 1000) / 100]
         + tens[(num % 100) / 10]
         + ones[num % 10];
}
```

- **Time:** O(1) — a fixed number of array lookups and string concatenations, independent of the specific value of `num` (bounded by the fixed `[1, 3999]` range).
- **Space:** O(1) auxiliary — four small, fixed-size lookup tables (sizes 4, 10, 10, 10), none of which scale with input.

**Why it is not optimal (stylistically):** This is actually a perfectly valid and genuinely O(1) solution — it's arguably not "worse" in complexity than Approach 2 given the fixed input range, but it requires **four separate precomputed tables**, one meticulously hand-built per decimal place, which is more error-prone to construct correctly (40 total string entries to get right) and less generalizable/elegant compared to the single unified table-driven greedy approach. It's presented here as an alternative valid strategy, not as an asymptotically inferior one, since both approaches are technically O(1) given the bounded input.

## 5. Approach 2 — Optimized (Greedy Single Table, Descending Order)

**Algorithm:**
1. Build two parallel arrays (or a single ordered list of pairs): `values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1]` and `symbols = ["M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"]`, in strictly descending order of value.
2. Initialize an empty `StringBuilder result` and iterate through the table by index `i` from `0` to `values.length - 1`.
3. While `num >= values[i]`: append `symbols[i]` to `result`, and subtract `values[i]` from `num`.
4. Once `num` drops below `values[i]`, move to the next (smaller) table entry.
5. Continue until `num` reaches 0, then return `result.toString()`.

```java
public String intToRoman(int num) {
    int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    StringBuilder result = new StringBuilder();

    for (int i = 0; i < values.length && num > 0; i++) {
        while (num >= values[i]) {
            result.append(symbols[i]);
            num -= values[i];
        }
    }

    return result.toString();
}
```

- **Time:** O(1) — the total number of symbols appended across the entire conversion is bounded by a small constant (at most 15 symbols for any value up to 3999, e.g. "MMMDCCCLXXXVIII" for 3888), independent of any growing input size, since `num` is bounded by the fixed constraint.
- **Space:** O(1) auxiliary — the two lookup arrays are fixed-size (13 entries each), and the `StringBuilder`'s final size is bounded by a small constant as well.

**Why this is optimal:** Since `num` is bounded to `[1, 3999]`, there's no asymptotic "n" to optimize against — both this approach and Approach 1 are O(1). This approach's advantage is structural simplicity and correctness confidence: a single unified table (13 entries) replacing four separately hand-constructed tables (40 total entries) reduces the surface area for transcription errors, and the greedy logic is self-evidently correct once the table is verified to be right, rather than requiring four independently-correct lookup tables.

## 6. Dry Run

Example: `num = 1994`.
Chosen because it's the canonical example from the problem statement, and it exercises four different table entries (M, CM, XC, IV), including two subtractive shorthand forms in a row.

Initial: `num = 1994`, `result = ""`.

| i | values[i] | symbols[i] | num >= values[i]? | Action | num after | result after |
|---|-----------|--------------|-------------------------|--------|--------------|------------------|
| 0 | 1000 | M | Yes (1994>=1000) | append "M", num-=1000 | 994 | "M" |
| 0 | 1000 | M | No (994>=1000 false) | move to next i | 994 | "M" |
| 1 | 900 | CM | Yes (994>=900) | append "CM", num-=900 | 94 | "MCM" |
| 1 | 900 | CM | No | move to next i | 94 | "MCM" |
| 2 | 500 | D | No | move to next i | 94 | "MCM" |
| 3 | 400 | CD | No | move to next i | 94 | "MCM" |
| 4 | 100 | C | No | move to next i | 94 | "MCM" |
| 5 | 90 | XC | Yes (94>=90) | append "XC", num-=90 | 4 | "MCMXC" |
| 5 | 90 | XC | No | move to next i | 4 | "MCMXC" |
| 6 | 50 | L | No | move to next i | 4 | "MCMXC" |
| 7 | 40 | XL | No | move to next i | 4 | "MCMXC" |
| 8 | 10 | X | No | move to next i | 4 | "MCMXC" |
| 9 | 9 | IX | No | move to next i | 4 | "MCMXC" |
| 10 | 5 | V | No | move to next i | 4 | "MCMXC" |
| 11 | 4 | IV | Yes (4>=4) | append "IV", num-=4 | 0 | "MCMXCIV" |

Exit condition: `num` reaches 0 after processing `i = 11`; outer loop condition `num > 0` becomes false, terminating early before reaching `i = 12`.

**Final answer:** `"MCMXCIV"`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Explicit place-value decomposition | O(1) | O(1) | Correct; requires four separately hand-built tables (40 entries total) |
| 2. Greedy single descending table | O(1) | O(1) | Correct and simpler; one unified table (13 entries), self-evidently correct greedy logic |

Both approaches are O(1) in time and space given the fixed, bounded input range `[1, 3999]`; the distinction between them is code clarity and maintainability, not asymptotic complexity.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Minimum value (`num = 1`) | Should produce simply `"I"` | Every table entry from 1000 down to 4 is skipped (num never `>=` any of them), until reaching `values[12] = 1`, where the single `while` iteration appends "I" and reduces num to 0 |
| Maximum value (`num = 3999`) | Should produce `"MMMCMXCIX"`, exercising the maximum 3 repetitions of M plus multiple subtractive forms | The `while` loop at `i=0` (value 1000) executes exactly 3 times (appending "M" three times, since `3999, 2999, 1999 >= 1000` but `999 < 1000`), correctly capping at 3 repetitions without any special-casing, since the loop naturally stops once `num` drops below 1000 |
| A value that is purely a round thousand/hundred/ten (e.g. `num = 2000`, `num = 300`) | Should produce a clean repeated-symbol string with no subtractive forms needed at all | The greedy `while` loop naturally handles repeated symbols correctly (e.g., "MM" for 2000) without needing any special logic to detect "no subtraction needed here" |
| A value requiring back-to-back subtractive forms (e.g. `num = 1994`, as in the dry run, using both CM and XC and IV) | Must correctly chain multiple subtractive shorthand entries without interference between them | Since each table entry is processed fully independently (the outer loop simply moves to the next smaller entry once the current one no longer fits), consecutive subtractive forms are naturally handled correctly with no special transition logic needed |
| A value like 49 (which is NOT "IL", since Roman numerals don't subtract across two symbol tiers) | The problem explicitly notes that 49 is XLIX (40+9), not some invalid "L minus I" shorthand, because conversion is based on decimal place values, not raw greedy subtraction across arbitrary symbol gaps | The table itself is structured with all valid subtractive forms explicitly listed (XL=40, IX=9) and no invalid ones (there is no "IL" entry), so the greedy process can only ever produce valid combinations, naturally respecting the decimal-place-based construction rule without needing extra validation logic |

## 9. Java Notes

- **`StringBuilder` for result construction:** since the result can require appending the same symbol multiple times in a loop (up to 3 times for M, X, C, I), using `StringBuilder.append` avoids the O(n²)-style inefficiency of repeated `String` concatenation (`+=`) inside a loop — though given the tiny bounded output size here (at most 15 characters), this is more a matter of good practice than a genuine performance concern for this specific problem.
- **Parallel arrays (`values[]` and `symbols[]`) vs. a `LinkedHashMap<Integer, String>`:** parallel arrays are used here for simplicity and to guarantee iteration order without relying on a map's iteration-order guarantees; a `LinkedHashMap` would also work (preserving insertion order) but is arguably less idiomatic for this fixed, small, indexed lookup use case compared to simple parallel arrays.
- **No overflow risk:** given the guaranteed range `1 <= num <= 3999`, all arithmetic (subtraction) stays well within `int` range with enormous headroom.
- **The `while` loop inside the `for` loop, rather than an `if`:** essential for correctly handling values that need a symbol repeated multiple times consecutively (like "MMM" for 3000) — using `if` instead of `while` would only append each symbol once per table entry, failing to handle repetition.

## 10. Common Mistakes

- **Using `if` instead of `while` when checking `num >= values[i]`.** This would only ever append each symbol at most once, failing on any input requiring a repeated symbol (like 3000 = "MMM", requiring three M's, or 30 = "XXX"). Fix: always use `while`, allowing the same table entry to be applied multiple times before moving to the next smaller value.
- **Forgetting to include the six subtractive shorthand entries (CM, CD, XC, XL, IX, IV) in the table, or placing them at the wrong position in the descending order.** Omitting them would cause the algorithm to fall back to writing four repeated symbols (e.g., "VIIII" instead of "IX"), which violates the "no more than 3 consecutive powers of 10, and no repeating V/L/D at all" rule. Placing them out of descending order would cause the greedy algorithm to select the wrong symbol at the wrong time. Fix: ensure all 13 entries are present and correctly sorted from largest to smallest value.
- **Building four separate per-place-value tables (as in Approach 1) but making a transcription error in one of the 40 total string entries.** This is a real risk given how much manual, repetitive table construction is required; a single off-by-one or typo in any of the four tables produces silently wrong output for specific input ranges. Fix: prefer the unified 13-entry greedy table approach, which has less total data to get right and is easier to visually verify against the standard Roman numeral value list.
- **Assuming a naive "digit by digit, symbol by symbol" approach (without an explicit table) would work by just outputting a symbol for each 1 in the value or similar simplistic logic.** Roman numerals require a fair amount of special-casing (the subtractive forms) that pure "convert like a normal place-value system" thinking misses. Fix: rely on the explicit value/symbol table (either the 13-entry unified version or the four-table decomposition), not on trying to derive symbols algorithmically from digit values alone.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Convert an integer (bounded, small range) to Roman numeral → build one table of value/symbol pairs, including the six subtractive shorthand forms, sorted largest to smallest, and greedily subtract/append."
- **90-second explanation:** "I build a single table of value-symbol pairs sorted from largest to smallest, and critically, I include the six subtractive shorthand values — 900 for CM, 400 for CD, 90 for XC, 40 for XL, 9 for IX, 4 for IV — as first-class entries in that same table, at the position their value naturally falls. Then I greedily walk through the table: for each entry, I keep appending its symbol and subtracting its value from the number as many times as it still fits, before moving on to the next smaller entry. Because the subtractive shorthand values are baked directly into the table at the right position, the greedy process naturally reaches for them exactly when appropriate, without needing any separate logic to detect 'does this look like a 4 or a 9 case.' Since the input is bounded to at most 3999, this whole process runs in O(1) time and space — there's no input size to scale against."
- **Related problems using this pattern:**
  - LeetCode 13 — Roman to Integer (the inverse conversion)
  - LeetCode 273 — Integer to English Words (different domain, but similarly a fixed-vocabulary, place-value-based conversion problem)
  - LeetCode 899 — Orderly Queue (unrelated pattern, but LC 12 is often grouped with other "greedy denomination" style problems like coin change variants)

## 12. Recall Questions

**Q:** Why is it important to use a `while` loop (not an `if`) when checking whether the current table value still fits into the remaining number?
**A:** Some Roman numeral values require the same symbol to be repeated multiple times consecutively (like three M's for 3000), and only a `while` loop allows the same table entry to be applied repeatedly until it no longer fits, before moving to the next smaller entry.

**Q:** Why does including the subtractive shorthand values (like 900 for "CM") directly in the same descending table as the primary symbols eliminate the need for separate "does this start with a 4 or 9" logic?
**A:** Because the table is processed strictly in descending order, the greedy algorithm will naturally reach and select 900 (CM) before 500 (D) whenever the remaining number is at least 900, correctly producing the subtractive form exactly when it's the right choice, without any additional conditional checks.

**Q:** Why is this problem considered O(1) in time complexity despite involving a loop that processes the input?
**A:** The input `num` is constrained to a fixed, bounded range (1 to 3999), and the maximum number of symbols that could ever be appended is a small constant (at most 15), so the total work done is bounded by a constant regardless of the specific input value, not growing with any unbounded "n."

**Q:** Why can't a naive digit-by-digit conversion (treating each decimal digit independently without a proper subtractive-aware table) correctly produce Roman numerals for digits like 4 or 9?
**A:** Roman numerals use a special subtractive shorthand for exactly these digit patterns (avoiding four repeated symbols or repeating non-power-of-10 symbols like V, L, D), which isn't something that falls out naturally from simple digit-value reasoning — it requires an explicit lookup table capturing these special forms.

**Q:** What specifically would go wrong if the six subtractive shorthand entries were omitted from the value/symbol table entirely?
**A:** The greedy algorithm would fall back to repeating the next-smaller primary symbol as many times as needed (e.g., producing "VIIII" instead of "IX" for 9, or being completely unable to correctly represent values like 40 without repeating X four times), violating the rule that non-power-of-10 symbols like V, L, D can never repeat and that powers of 10 can repeat at most 3 times.

## 13. Final Code

```java
public String intToRoman(int num) {
    int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    StringBuilder result = new StringBuilder();

    for (int i = 0; i < values.length && num > 0; i++) {
        while (num >= values[i]) {
            result.append(symbols[i]);
            num -= values[i];
        }
    }

    return result.toString();
}
```

## 14. Self-Test

You have an integer `num` between 1 and 3999. Convert it to its Roman numeral string representation, using the standard subtractive shorthand forms where required. Think about why building a single table of value-symbol pairs — including the six subtractive shorthand values placed at their correct position in descending order — lets a simple greedy subtract-and-append loop produce the correct result without any special-case logic.
