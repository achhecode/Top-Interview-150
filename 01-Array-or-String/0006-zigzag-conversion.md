---
problem: 6 - Zigzag Conversion
url: https://leetcode.com/problems/zigzag-conversion/
difficulty: Medium
section: String / Simulation
patterns: [simulation, direction-flip]
data_structures: [string, array]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** A string `s` and an integer `numRows`.

**Required:** Simulate writing `s` in a zigzag pattern across `numRows` rows (down one column, then diagonally up-right, then down again, repeating), then return the string formed by reading the result row by row, left to right, top to bottom.

**Constraints that actually matter:**
- `1 <= s.length <= 1000` and `1 <= numRows <= 1000` — both small; performance is not a concern, so the focus is entirely on correctly simulating the zigzag placement pattern.
- **`numRows == 1` is a valid, important edge case** — with only one row, there's no zigzag at all; the "pattern" degenerates to just the original string unchanged (as shown in example 3).
- The zigzag movement is a **bounce**: move down through rows `0, 1, 2, ..., numRows-1`, then immediately reverse direction and move back up through `numRows-2, numRows-3, ..., 1`, then bounce down again — this back-and-forth "bounce off the top and bottom row" behavior is the core mechanic to simulate correctly.

## 2. Recognition Signals

- "Zigzag pattern across N rows, then read row by row" → the specific signature of a **simulation problem**: rather than deriving a closed-form mathematical index formula upfront, directly simulate the character-by-character placement process, since the movement pattern (down, then diagonally up, repeat) is most naturally expressed as a simple direction-tracking loop.
- Whenever a sequence needs to be distributed across multiple "buckets" (here, rows) following a back-and-forth or bouncing traversal order — track a "current bucket index" and a "direction" flag, flipping the direction whenever a boundary (top row or bottom row) is reached.
- The problem's own visual diagram (characters arranged in a zigzag grid, read back row by row) is itself the clearest hint toward "just simulate this row-assignment process directly, then concatenate the rows."

**Pattern:** Simulation with Direction Flip (track a current row index and a direction, appending each character to its assigned row's buffer, flipping direction upon hitting the top or bottom row boundary).

## 3. Core Idea

Rather than trying to derive a mathematical formula for exactly which row each character belongs to in one step, directly simulate the process: maintain one growable string buffer per row (`numRows` buffers total), a `currentRow` pointer starting at 0, and a `direction` flag (initially "moving down"). For each character in `s`, append it to `currentRow`'s buffer, then advance `currentRow` by `+1` if moving down or `-1` if moving up. Whenever `currentRow` reaches either boundary (row 0 or row `numRows - 1`), flip the direction for the next step. After processing every character, concatenate all `numRows` row buffers together, top to bottom, to form the final answer.

**Invariant:** After processing the first `k` characters of `s`, each row buffer contains exactly the characters (in correct left-to-right order) that would appear in that row of the zigzag pattern when only the first `k` characters have been written — so after all characters are processed, concatenating the row buffers in order produces the complete, correct row-by-row reading of the full zigzag pattern.

## 4. Approach 1 — Brute Force (Full 2D Grid Simulation)

**Logic:** Allocate an actual 2D character grid sized to fit the entire zigzag pattern (`numRows` rows by however many columns the zigzag would span), simulate placing each character at its exact `(row, column)` position by tracking both a row and column pointer with the same down/up bounce logic, then read the grid back row by row, skipping any empty/unused cells.

```java
public String convert(String s, int numRows) {
    if (numRows == 1) {
        return s;
    }

    int n = s.length();
    int cycleLen = 2 * numRows - 2;
    int numCols = (n / cycleLen + 1) * (numRows - 1);
    char[][] grid = new char[numRows][numCols];
    for (char[] row : grid) {
        Arrays.fill(row, '\0');
    }

    int row = 0;
    int col = 0;
    boolean goingDown = true;

    for (int i = 0; i < n; i++) {
        grid[row][col] = s.charAt(i);
        if (goingDown) {
            row++;
            if (row == numRows) {
                row -= 2;
                col++;
                goingDown = false;
            }
        } else {
            row--;
            col++;
            if (row < 0) {
                row += 2;
                col -= 1;
                goingDown = true;
            }
        }
    }

    StringBuilder result = new StringBuilder();
    for (char[] gridRow : grid) {
        for (char c : gridRow) {
            if (c != '\0') {
                result.append(c);
            }
        }
    }
    return result.toString();
}
```

- **Time:** O(n) for placing characters, plus O(numRows × numCols) for reading the grid back, which in the worst case is proportional to O(n) as well (the grid is sized just large enough to hold the pattern, not wastefully oversized).
- **Space:** O(n) for the 2D grid (sized proportionally to fit the full zigzag layout, including unused cells).

**Why it is not optimal:** While technically O(n) overall, this approach is considerably more complex to get right (tracking both row *and* column pointers with fiddly boundary-bounce arithmetic, plus needing to size the grid correctly and skip unused cells when reading back) compared to the simpler "one buffer per row" approach — the extra column-tracking complexity buys no real advantage here, since the final read-out only ever needs the *row* groupings, not the exact 2D positions.

## 5. Approach 2 — Optimized (One StringBuilder Per Row, Direction Flip)

**Algorithm:**
1. If `numRows == 1` (or `numRows >= s.length()`, though the `numRows == 1` check alone suffices given the direction-flip logic naturally handles larger row counts correctly), return `s` unchanged immediately (no zigzag occurs with only one row).
2. Create an array of `numRows` `StringBuilder` objects, one per row.
3. Initialize `currentRow = 0` and `goingDown = false` (or `true`; the exact starting direction convention just needs to be consistent with the boundary-flip logic below).
4. For each character `c` in `s`:
   - Append `c` to `rows[currentRow]`.
   - If `currentRow == 0` or `currentRow == numRows - 1`, flip `goingDown`.
   - Update `currentRow += goingDown ? 1 : -1`.
5. Concatenate all row buffers together, in order, and return the result.

```java
public String convert(String s, int numRows) {
    if (numRows == 1) {
        return s;
    }

    StringBuilder[] rows = new StringBuilder[numRows];
    for (int i = 0; i < numRows; i++) {
        rows[i] = new StringBuilder();
    }

    int currentRow = 0;
    boolean goingDown = false;

    for (char c : s.toCharArray()) {
        rows[currentRow].append(c);
        if (currentRow == 0 || currentRow == numRows - 1) {
            goingDown = !goingDown;
        }
        currentRow += goingDown ? 1 : -1;
    }

    StringBuilder result = new StringBuilder();
    for (StringBuilder row : rows) {
        result.append(row);
    }
    return result.toString();
}
```

- **Time:** O(n) — a single pass through `s`, appending each character to exactly one row buffer, plus a final O(n) concatenation of all row buffers.
- **Space:** O(n) auxiliary — the `numRows` `StringBuilder` objects together hold exactly `n` characters total (every character from `s` goes into exactly one row buffer), so total auxiliary space is proportional to `n`, not to any wastefully oversized grid.

**Why this is optimal:** Every character must be placed into its correct row at least once, so O(n) time is a hard lower bound, matching what this single pass achieves. Space is O(n) because the output itself must eventually hold all `n` characters — there's no way to represent the answer in less space than the input requires, and this approach doesn't waste any additional space on unused grid cells (unlike Approach 1's full 2D grid, which allocates space for columns that are only partially filled). This is simpler to reason about and implement correctly, since it only ever needs to track a single "current row" index and a direction flag, not a full two-dimensional position with more intricate boundary arithmetic.

## 6. Dry Run

Example: `s = "PAYPALISHIRING"`, `numRows = 3`.
Chosen because it's the canonical example, and with 3 rows it clearly shows the direction flipping at both the top (row 0) and bottom (row 2) boundaries multiple times.

`n = 14`. Initial: `currentRow = 0`, `goingDown = false`, `rows = ["", "", ""]`.

| i | c | rows[currentRow] before | currentRow == 0 or numRows-1? | goingDown after | currentRow after append |
|---|---|-------------------------------|-------------------------------------|-----------------------|--------------------------------|
| 0 | P | rows[0]="" → "P" | Yes (0==0) | flip: true | currentRow=0+1=1 |
| 1 | A | rows[1]="" → "A" | No (1≠0, 1≠2) | true (unchanged) | currentRow=1+1=2 |
| 2 | Y | rows[2]="" → "Y" | Yes (2==numRows-1=2) | flip: false | currentRow=2-1=1 |
| 3 | P | rows[1]="A" → "AP" | No | false | currentRow=1-1=0 |
| 4 | A | rows[0]="P" → "PA" | Yes (0==0) | flip: true | currentRow=0+1=1 |
| 5 | L | rows[1]="AP" → "APL" | No | true | currentRow=1+1=2 |
| 6 | I | rows[2]="Y" → "YI" | Yes | flip: false | currentRow=2-1=1 |
| 7 | S | rows[1]="APL" → "APLS" | No | false | currentRow=1-1=0 |
| 8 | H | rows[0]="PA" → "PAH" | Yes | flip: true | currentRow=0+1=1 |
| 9 | I | rows[1]="APLS" → "APLSI" | No | true | currentRow=1+1=2 |
| 10 | R | rows[2]="YI" → "YIR" | Yes | flip: false | currentRow=2-1=1 |
| 11 | I | rows[1]="APLSI" → "APLSII" | No | false | currentRow=1-1=0 |
| 12 | N | rows[0]="PAH" → "PAHN" | Yes | flip: true | currentRow=0+1=1 |
| 13 | G | rows[1]="APLSII" → "APLSIIG" | No | true | currentRow=1+1=2 (unused, loop ends) |

Exit condition: all 14 characters processed.

Final rows: `rows[0] = "PAHN"`, `rows[1] = "APLSIIG"`, `rows[2] = "YIR"`.

**Final answer:** Concatenating: `"PAHN" + "APLSIIG" + "YIR" = "PAHNAPLSIIGYIR"`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Full 2D grid simulation | O(n) | O(n) (grid sized to fit, but with some unused cells) | Correct but needlessly complex two-pointer (row+column) boundary logic |
| 2. One StringBuilder per row, direction flip | O(n) | O(n) (exactly n characters total across all row buffers, no waste) | Optimal and simpler; single "current row" index tracked, not a 2D position |

Input/output space for `s` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| `numRows == 1` | No zigzag occurs at all; the "pattern" is just the original string read straight through | Explicit early return `if (numRows == 1) return s;` handles this directly, also conveniently avoiding a potential issue where the direction-flip logic's boundary check (`currentRow == 0 || currentRow == numRows - 1`) would be ambiguous or degenerate when both boundaries are the same row (row 0) |
| `numRows >= s.length()` (more rows than characters) | The zigzag never has a chance to "bounce" back up before the string ends; effectively degenerates to one character per row read in original order | The direction-flip logic still works correctly even in this case: characters are simply placed into consecutive rows top-to-bottom without ever reaching a bounce point, so concatenating the (mostly single-character) row buffers reproduces the original string unchanged, which is the correct behavior |
| Single-character string (`s.length() == 1`) | Trivial case; should return the single character unchanged regardless of `numRows` | Whether `numRows == 1` (early return) or `numRows > 1` (the single character goes into `rows[0]`, and the loop ends immediately after), the result is correctly just that one character |
| `numRows == 2` (minimum case where zigzag logic is non-trivial) | With only 2 rows, the "boundary" for both directions is adjacent (row 0 and row 1 are both boundaries), so direction flips every single character | The boundary check `currentRow == 0 || currentRow == numRows - 1` correctly triggers on every character when `numRows == 2` (since every row is either row 0 or row `numRows-1 = 1`), resulting in characters alternating strictly between the two rows, which is the correct zigzag behavior for exactly 2 rows |
| String containing the allowed non-letter characters (commas, periods, per the constraints) | Should be treated identically to letters; no special-casing needed | The algorithm operates purely on character positions and row assignment, never inspecting or caring about what the character actually is, so commas and periods are handled identically to letters |
| `numRows` and `s.length()` both at their maximum constraint values (1000) | Should still run efficiently and correctly at the upper bound of input size | Since the algorithm is O(n) time and space regardless of the specific relationship between `numRows` and `s.length()`, the maximum constraint values pose no special difficulty beyond what's already handled by the general-case logic |

## 9. Java Notes

- **Array of `StringBuilder` objects (`StringBuilder[] rows`):** each row's buffer grows independently as characters are appended to it; using `StringBuilder` avoids the O(n²)-style inefficiency of repeated `String` concatenation within each row's buildup.
- **`s.toCharArray()` for iteration:** a minor efficiency convenience for iterating over each character of the string via an enhanced `for` loop, avoiding repeated `charAt(i)` calls (though the difference is negligible given the small constraints here).
- **Boolean `goingDown` flag with an XOR-like flip (`goingDown = !goingDown`) at boundaries:** a concise, idiomatic way to implement the bounce logic without needing separate explicit `if (movingDown) {...} else {...}` branches for every single step — the flip only happens at the two boundary rows, and the actual row-index update (`currentRow += goingDown ? 1 : -1`) is a single unified line regardless of direction.
- **No overflow or numeric risk:** this problem involves only string/character manipulation and small bounded integer row indices; no arithmetic operations here risk overflow.

## 10. Common Mistakes

- **Forgetting the `numRows == 1` special case.** Without it, depending on exactly how the boundary-flip condition is written, a single-row scenario could behave unpredictably (e.g., if the boundary check is `currentRow == 0 || currentRow == numRows - 1`, with `numRows = 1` this becomes `currentRow == 0 || currentRow == 0`, which is technically fine on its own, but combined with how `currentRow` updates, could still produce subtly wrong behavior depending on implementation details) — explicitly handling this case upfront avoids any ambiguity. Fix: always check for `numRows == 1` (or `numRows >= s.length()`, as an equivalent or more general safeguard) and return the string unchanged immediately.
- **Getting the direction-flip boundary condition wrong (e.g., using `<` or `>` instead of `==`, or checking the wrong pair of boundary values).** This can cause the simulated row index to go out of bounds (negative or `>= numRows`) or fail to bounce at the correct point, corrupting the zigzag pattern. Fix: the flip should trigger precisely when `currentRow` equals either `0` or `numRows - 1` (the two boundary rows), each time the row index reaches that boundary.
- **Updating `currentRow` before checking the boundary condition, instead of after.** This ordering matters: the check for "have I just reached a boundary" must happen based on the row the character was *just placed into*, before advancing to the next row for the *next* character. Fix: append the character, then check the boundary condition using the row it was just appended to, then update `currentRow` for the next iteration.
- **Attempting to derive a closed-form index formula for "which position in the output does character `i` map to" without first understanding the simpler simulation approach.** While a mathematical formula does exist for this problem (based on the cycle length `2 * numRows - 2`), it's more error-prone to derive correctly from scratch under interview pressure compared to the straightforward simulation, and the simulation is fully sufficient given the small constraints. Fix: default to the simulation approach unless specifically asked for a formula-based solution.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Zigzag pattern across N rows, read back row by row → simulate directly: one buffer per row, track a current row index and a direction, flipping direction at the top and bottom row boundaries."
- **90-second explanation:** "Rather than trying to compute a mathematical formula for exactly where each character ends up, I directly simulate the zigzag writing process. I create one string buffer per row, and as I scan through the input string character by character, I append each one to whichever row I'm currently 'at.' I track my current row and a direction flag — moving down normally, but the moment I hit either the very top row or the very bottom row, I flip my direction, so I start bouncing back the other way. After processing every character this way, I just concatenate all the row buffers together, top to bottom, to get the final row-by-row reading of the zigzag pattern. I also handle the special case where there's only one row, since with just one row there's no zigzag at all — the answer is simply the original string. This runs in O(n) time and space, since every character is placed into exactly one row buffer, and the final concatenation just reads through those buffers once more."
- **Related problems using this pattern:**
  - LeetCode 12 — Integer to Roman (different domain, but similarly a direct simulation over a small, bounded structure)
  - LeetCode 54 — Spiral Matrix (a different, but conceptually related, direction-flipping traversal simulation)
  - LeetCode 59 — Spiral Matrix II (also a direction-flip simulation)

## 12. Recall Questions

**Q:** Why does the "current row" tracking approach avoid needing to compute exact 2D grid coordinates (row and column) for each character?
**A:** The final output only cares about which *row* each character belongs to, and the order within that row (which is naturally preserved by appending in left-to-right scan order), so tracking a single row index is sufficient — the column position within the zigzag grid is never actually needed for producing the correct row-by-row output.

**Q:** Why must the direction flip check happen based on the row the character was just placed into, rather than being checked before placing the character?
**A:** The boundary condition needs to reflect "have I just reached the top or bottom row with this character," which is only known after placing the character in its row — checking before placement could use a stale or not-yet-updated row value, leading to an incorrect flip timing.

**Q:** Why is the `numRows == 1` case handled as an explicit early return rather than relying on the general direction-flip logic to naturally produce the correct result?
**A:** With only one row, both "boundaries" collapse to the same single row, which can create ambiguity or subtly incorrect behavior in the general bounce logic depending on implementation details, so explicitly short-circuiting this case guarantees correct, unambiguous behav23ior.

**Q:** How does the algorithm's behavior change, if at all, when `numRows` is greater than or equal to the length of the input string?
**A:** The zigzag never gets the chance to bounce back upward before the string runs out, so characters simply get placed into consecutive rows from top to bottom without any reversal, and the row-by-row concatenation ends up reproducing the original string's character order unchanged.

**Q:** Why does using an array of `StringBuilder` objects (one per row) avoid a performance pitfall that naive `String` concatenation would introduce?
**A:** Repeatedly concatenating onto an immutable `String` using `+=` inside a loop would require creating a new string object on every single append, leading to O(n²) behavior in the worst case, whereas `StringBuilder` maintains a mutable, resizable internal buffer that allows efficient O(1) amortized appends.

## 13. Final Code

```java
public String convert(String s, int numRows) {
    if (numRows == 1) {
        return s;
    }

    StringBuilder[] rows = new StringBuilder[numRows];
    for (int i = 0; i < numRows; i++) {
        rows[i] = new StringBuilder();
    }

    int currentRow = 0;
    boolean goingDown = false;

    for (char c : s.toCharArray()) {
        rows[currentRow].append(c);
        if (currentRow == 0 || currentRow == numRows - 1) {
            goingDown = !goingDown;
        }
        currentRow += goingDown ? 1 : -1;
    }

    StringBuilder result = new StringBuilder();
    for (StringBuilder row : rows) {
        result.append(row);
    }
    return result.toString();
}
```

## 14. Self-Test

You have a string `s` and an integer `numRows`. Simulate writing `s` in a zigzag pattern across `numRows` rows (down, then diagonally up, repeating), and return the result read row by row. Think about why tracking just a single "current row" index and a direction flag, flipping at the top and bottom row boundaries, is enough to correctly simulate the pattern without needing to compute exact 2D grid positions.
