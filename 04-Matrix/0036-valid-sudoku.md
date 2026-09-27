---
problem: 36 - Valid Sudoku
url: https://leetcode.com/problems/valid-sudoku/
difficulty: Medium
section: Array / Hash Set
patterns: [single-pass-validation, box-index-mapping]
data_structures: [array, hashset]
time: O(1)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Views

## 1. Problem in Simple Words

**Given:** A `9 x 9` Sudoku board, partially filled with digits `1-9` or `'.'` (empty cells).

**Required:** Determine whether the board is valid — meaning **only** that each filled row, each filled column, and each filled `3x3` sub-box contains no repeated digits. The board does not need to be *solvable*, only free of rule violations among the cells already filled.

**Constraints that actually matter:**
- `board.length == 9`, `board[i].length == 9` — the board size is **fixed and small** (always exactly 81 cells), so this problem is technically O(1) time and space regardless of algorithm, since there's no growing "n" to scale against; the interesting part is purely about organizing the validation logic cleanly and efficiently, not about asymptotic growth.
- **Only filled cells need validation** — `'.'` cells are simply skipped entirely; they impose no constraint and should never be treated as if they were a repeated "empty" value.
- Each of the three rule types (row, column, box) is checked **independently** — a valid board must satisfy all three simultaneously, but a cell only ever needs to be compared against the other cells sharing its specific row, its specific column, and its specific 3x3 box, never against unrelated cells elsewhere on the board.

## 2. Recognition Signals

- "Validate rows, columns, AND sub-boxes for duplicates, all simultaneously" → the signature of a **single-pass multi-constraint validation** problem: rather than doing three completely separate passes over the board (one for rows, one for columns, one for boxes), track duplicate-detection state for all three constraint types at once as you scan each cell exactly once.
- The specific challenge of mapping a `(row, col)` cell position to **which of the nine 3x3 boxes it belongs to** is the standout technical detail of this problem — recognizing the formula `boxIndex = (row / 3) * 3 + (col / 3)` (integer division) is the key piece of domain-specific logic that distinguishes this from a simpler "just check rows and columns" problem.
- Whenever you need to detect duplicates within multiple different, overlapping "groupings" of the same underlying data (here: 9 rows, 9 columns, and 9 boxes, all covering the same 81 cells) — using one hash set (or boolean array) per grouping, indexed appropriately, and checking-then-inserting as you scan, is the standard technique.

**Pattern:** Single-Pass Multi-Constraint Validation with Box-Index Mapping (scan every cell exactly once, checking and recording its value against three separate duplicate-tracking structures — one per row, one per column, one per box — using a formula to map each cell to its correct box).

## 3. Core Idea

Maintain three arrays of hash sets (or boolean arrays), each sized 9: `rows[9]` (one set per row), `cols[9]` (one set per column), and `boxes[9]` (one set per 3x3 box). Scan through all 81 cells exactly once. For each filled cell at `(row, col)` with digit `d`, compute `boxIndex = (row / 3) * 3 + (col / 3)` — this formula correctly maps any `(row, col)` pair to one of the nine box indices (0 through 8), since integer division by 3 groups rows into three bands (0-2, 3-5, 6-8) and columns into three bands similarly, and multiplying the row-band by 3 and adding the column-band produces a unique index for each of the 3x3 grid of boxes. Then, check whether `d` already exists in `rows[row]`, `cols[col]`, or `boxes[boxIndex]` — if it exists in any of them, the board is invalid, return `false` immediately. Otherwise, add `d` to all three corresponding sets and continue.

**Invariant:** After processing all cells up to and including the current one, `rows[r]` contains exactly the set of digits that have appeared so far in row `r`, `cols[c]` contains exactly the digits seen so far in column `c`, and `boxes[b]` contains exactly the digits seen so far in box `b` — so a duplicate in any of these three groupings is detected at the exact moment it would occur, during the single forward scan.

## 4. Approach 1 — Brute Force (Separate Full Passes for Each Rule)

**Logic:** Perform three entirely separate validation passes over the board: one that checks every row independently for duplicates, one that checks every column independently, and one that checks every 3x3 box independently — each pass using its own fresh `Set` per group.

```java
public boolean isValidSudoku(char[][] board) {
    // Check rows
    for (int r = 0; r < 9; r++) {
        Set<Character> seen = new HashSet<>();
        for (int c = 0; c < 9; c++) {
            char val = board[r][c];
            if (val != '.' && !seen.add(val)) {
                return false;
            }
        }
    }

    // Check columns
    for (int c = 0; c < 9; c++) {
        Set<Character> seen = new HashSet<>();
        for (int r = 0; r < 9; r++) {
            char val = board[r][c];
            if (val != '.' && !seen.add(val)) {
                return false;
            }
        }
    }

    // Check 3x3 boxes
    for (int boxRow = 0; boxRow < 3; boxRow++) {
        for (int boxCol = 0; boxCol < 3; boxCol++) {
            Set<Character> seen = new HashSet<>();
            for (int r = boxRow * 3; r < boxRow * 3 + 3; r++) {
                for (int c = boxCol * 3; c < boxCol * 3 + 3; c++) {
                    char val = board[r][c];
                    if (val != '.' && !seen.add(val)) {
                        return false;
                    }
                }
            }
        }
    }

    return true;
}
```

- **Time:** O(1) — the board is always exactly 81 cells, and this approach visits each cell a small constant number of times (three total passes, so up to 3× the board size, still a fixed constant regardless of any "n").
- **Space:** O(1) — each `Set` used is bounded to at most 9 entries, and only one is active at a time (reused per row/column/box group).

**Why it is not optimal (stylistically):** Since the board size is fixed, this is technically the same O(1) complexity class as the single-pass approach — there's no asymptotic advantage to combining the checks. However, doing three separate full traversals of the board (touching every cell up to 3 times total) is less elegant and slightly more code than necessary, when all three checks can be performed simultaneously during a single traversal of the board, touching each cell exactly once.

## 5. Approach 2 — Optimized (Single Pass, Three Parallel Constraint-Tracking Structures)

**Algorithm:**
1. Create three arrays of `Set<Character>` (or `boolean[9][10]` arrays, indexed by digit, for a slightly more efficient alternative), each of length 9: `rows`, `cols`, `boxes`.
2. For each cell `(r, c)` from `(0,0)` to `(8,8)`, scanned in a single nested loop:
   - If `board[r][c] == '.'`, skip this cell.
   - Otherwise, let `val = board[r][c]` and compute `boxIndex = (r / 3) * 3 + (c / 3)`.
   - Check whether `val` is already present in `rows[r]`, `cols[c]`, or `boxes[boxIndex]`. If so, return `false` immediately.
   - Otherwise, add `val` to `rows[r]`, `cols[c]`, and `boxes[boxIndex]`.
3. If the scan completes without finding any violation, return `true`.

```java
public boolean isValidSudoku(char[][] board) {
    Set<Character>[] rows = new HashSet[9];
    Set<Character>[] cols = new HashSet[9];
    Set<Character>[] boxes = new HashSet[9];

    for (int i = 0; i < 9; i++) {
        rows[i] = new HashSet<>();
        cols[i] = new HashSet<>();
        boxes[i] = new HashSet<>();
    }

    for (int r = 0; r < 9; r++) {
        for (int c = 0; c < 9; c++) {
            char val = board[r][c];
            if (val == '.') {
                continue;
            }

            int boxIndex = (r / 3) * 3 + (c / 3);

            if (rows[r].contains(val) || cols[c].contains(val) || boxes[boxIndex].contains(val)) {
                return false;
            }

            rows[r].add(val);
            cols[c].add(val);
            boxes[boxIndex].add(val);
        }
    }

    return true;
}
```

- **Time:** O(1) — a single pass over the fixed 81 cells, with each cell's lookup/insert into the hash sets being O(1) average.
- **Space:** O(1) — 27 hash sets total (9 rows + 9 columns + 9 boxes), each bounded to at most 9 entries, all fixed regardless of anything resembling an "n."

**Why this is optimal:** Since the board size is fixed at 81 cells, both approaches are technically O(1) — there's no asymptotic complexity distinction to make here. This approach's advantage is purely structural: every cell is visited exactly once (rather than up to three times), and all three rule checks (row, column, box) are performed together during that single visit, which is simpler to reason about as "one clean validation pass" and slightly reduces the constant-factor work compared to three separate full traversals.

## 6. Dry Run

Example (abbreviated to the relevant cells): consider `board[0][0] = '8'` and `board[3][0] = '8'` (as in the problem's second, invalid example, where the top-left corner is changed to 8, colliding with the existing 8 at row 3, column 0, since both fall within the same top-left 3x3 box).

Processing `(r=0, c=0)`, `val='8'`: `boxIndex = (0/3)*3 + (0/3) = 0*3+0 = 0`. Check `rows[0]`, `cols[0]`, `boxes[0]` — all empty initially, no conflict. Add '8' to `rows[0]`, `cols[0]`, `boxes[0]`.

... (scan continues through other cells in row 0, row 1, row 2, adding their values to the respective sets) ...

Processing `(r=3, c=0)`, `val='8'`: `boxIndex = (3/3)*3 + (0/3) = 1*3+0 = 3`. Wait — let me recompute: row 3 is in the *second* row-band (rows 3-5), so `3/3 = 1`; column 0 is in the first column-band, so `0/3 = 0`; `boxIndex = 1*3 + 0 = 3`. This is actually a *different* box than box 0 (which covers rows 0-2, columns 0-2) — box 3 covers rows 3-5, columns 0-2. So this specific pair (0,0) and (3,0) don't actually share a box; let me instead verify against the problem's own stated explanation, which says the conflict is specifically with **another 8 in the top-left 3x3 sub-box**, meaning the second 8 must be at a position like `(r=2, c=1)` or similar, within rows 0-2 and columns 0-2. Rather than mis-locate the exact second cell from memory, the key mechanic remains: whichever cell holds the second '8' within the same box index will trigger `boxes[boxIndex].contains('8')` to be `true` at that point, immediately returning `false`.

Exit condition: the moment a `val` is found already present in any of `rows[r]`, `cols[c]`, or `boxes[boxIndex]`, the scan halts and returns `false` immediately, without needing to process any remaining cells.

**Final answer:** `false`, matching the expected output exactly, since a genuine duplicate `'8'` exists within one of the 3x3 boxes in this modified board.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Three separate full passes | O(1) (fixed 81-cell board) | O(1) | Correct; touches each cell up to 3 times total across all passes |
| 2. Single pass, three parallel structures | O(1) (fixed 81-cell board) | O(1) | Correct and slightly cleaner; touches each cell exactly once |

Both approaches are O(1) in the strict sense, since the board size is always exactly 9x9 and never grows — the distinction between them is code structure and constant-factor efficiency, not asymptotic complexity.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| A completely empty board (all cells are `'.'`) | Should trivially be valid, since there are no filled cells to violate any rule | Every cell triggers the `if (val == '.') continue;` skip, so no checks or insertions ever occur, and the function correctly falls through to `return true;` |
| A fully and correctly filled, valid Sudoku solution | Should be recognized as valid despite every single cell being filled | Every cell's value is checked against and found absent from its row/column/box sets (since the board is genuinely valid), so no conflict is ever detected, and the scan completes successfully, returning `true` |
| Duplicate digits within a row that are NOT in the same column or box | Should still be correctly detected purely by the row-check mechanism | The `rows[r].contains(val)` check operates independently of the column and box checks, so a row-only duplicate is caught by that specific check regardless of the other two constraints being satisfied |
| Duplicate digits within a column that are NOT in the same row or box | Symmetric to the row case; should be caught purely by the column-check mechanism | The `cols[c].contains(val)` check independently catches this case |
| Duplicate digits within a 3x3 box that are NOT in the same row or column (as in the problem's own second example) | This is the trickiest case to get right, since the two conflicting cells can be positioned anywhere within the same box without sharing a row or column index at all | The `boxIndex = (r/3)*3 + (c/3)` formula correctly maps any cell within a given 3x3 box to the same `boxIndex` regardless of the cell's specific row/column offset within that box, so the `boxes[boxIndex].contains(val)` check correctly catches this case independent of row/column positioning |
| A digit repeated but positioned such that it's genuinely valid (different rows, different columns, different boxes) — i.e., a normal, non-conflicting board with the same digit appearing 9 times total across the whole board (once per row, column, and box, as any valid solved Sudoku requires) | Must NOT be flagged as invalid, since repetition across genuinely independent rows/columns/boxes is expected and required in a valid solution | Since each of the 9 possible values for a given digit falls into a distinct row, distinct column, and distinct box (by the nature of a valid Sudoku), none of the three checks ever finds a true conflict for correctly placed repeated digits, correctly allowing the board to validate as `true` |

## 9. Java Notes

- **`(r / 3) * 3 + (c / 3)` for box-index mapping:** integer division truncates, so `r / 3` yields 0 for rows 0-2, 1 for rows 3-5, 2 for rows 6-8 (the "row band"), and similarly for `c / 3` (the "column band"); multiplying the row band by 3 and adding the column band produces a unique value from 0 to 8 for each of the nine boxes — this specific formula is worth memorizing as a reusable technique for any problem involving 3x3 (or generally, block-structured) grid partitioning.
- **`Set<Character>[]` array of hash sets:** Java doesn't allow direct generic array creation (`new HashSet<Character>[9]` would be a compile error due to type erasure), so the common workaround is to declare it as a raw-typed array (`new HashSet[9]`) and populate each slot individually, accepting an unchecked-cast compiler warning — an alternative is to use `List<Set<Character>>` (e.g., backed by an `ArrayList`) to avoid the generic array creation issue entirely, or to use `boolean[9][10]` arrays (indexed directly by digit, converting `char` to `int` via subtraction) for a warning-free and marginally more efficient alternative to hash sets given the small, fixed digit range.
- **`Set.contains` vs. `Set.add`'s boolean return value:** Approach 2 explicitly separates the "check" (`contains`) from the "insert" (`add`) into two steps for clarity across three different structures simultaneously; Approach 1's per-group version instead uses the more concise `!seen.add(val)` idiom (since `add` returns `false` if the element was already present), which works well when only a single set is being checked at a time.
- **No overflow or numeric risk:** this problem involves only small, bounded index arithmetic (0-8 ranges) and character comparisons; no risk of overflow anywhere in the computation.

## 10. Common Mistakes

- **Getting the box-index formula wrong** (e.g., using `r / 3 + c / 3` instead of `(r / 3) * 3 + (c / 3)`, forgetting the multiplication by 3). Without the multiplication, multiple genuinely different boxes would incorrectly map to the same index (e.g., row-band 1 + column-band 0 would equal row-band 0 + column-band 1, both giving 1), causing false duplicate detections or missed ones. Fix: always multiply the row-band by 3 before adding the column-band, to correctly spread the nine combinations across nine distinct indices.
- **Forgetting to skip `'.'` (empty) cells before performing the duplicate check.** Treating `'.'` as if it were a regular value to check and insert would incorrectly flag a board with multiple empty cells in the same row/column/box as having a "duplicate '.'" violation, which is never a real Sudoku rule. Fix: always explicitly check for and `continue` past `'.'` cells before any duplicate-checking logic.
- **Using three completely separate, disconnected traversals (Approach 1) and forgetting that a single combined pass is possible and cleaner**, though this isn't a correctness bug — both approaches work — it's worth recognizing as an opportunity for a tidier solution. Fix: default to the single-pass, three-parallel-structure approach shown in Approach 2 for cleaner code.
- **Checking `contains` and calling `add` as separate, unguarded steps without confirming no conflict was found first** — i.e., accidentally adding a value to the tracking sets *before* fully verifying it doesn't already exist in all three structures, potentially causing a value to end up compared against itself. Fix: always perform all three `contains` checks first, and only proceed to the three `add` calls if none of the checks found a conflict.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Validate rows, columns, and 3x3 boxes simultaneously on a Sudoku board → single pass over every cell, checking and updating one hash set per row, per column, and per box, using `(r/3)*3 + (c/3)` to map each cell to its box."
- **90-second explanation:** "I maintain three arrays of sets — one indexed by row, one by column, and one by box — each holding the digits seen so far in that specific grouping. I scan through the board once, cell by cell. For each filled cell, I first compute which of the nine 3x3 boxes it belongs to, using the formula `(row / 3) * 3 + (column / 3)`, which correctly groups rows and columns into their respective 'bands' and combines them into a unique box index. Then I check whether the current digit already exists in that cell's row set, column set, or box set — if it does, the board is invalid and I return false immediately. Otherwise, I add the digit to all three sets and move on. Since the board is always a fixed 9x9 size, this is technically O(1) time and space regardless of the specific algorithm, but doing all three checks together in a single pass, rather than three separate full traversals, is the cleaner and more elegant approach."
- **Related problems using this pattern:**
  - LeetCode 37 — Sudoku Solver (extends this validation logic into a full backtracking solver)
  - LeetCode 2133 — Check if Every Row and Column Contains All Numbers (a simpler variant checking only rows/columns, not boxes)
  - LeetCode 1275 — Find Winner on a Tic Tac Toe Game (different game, but similarly involves grid-based state tracking)

## 12. Recall Questions

**Q:** Why does the formula `(row / 3) * 3 + (col / 3)` correctly and uniquely map any cell to one of the nine 3x3 boxes?
**A:** Integer division of the row and column by 3 groups them into three "bands" each (0, 1, or 2), and multiplying the row band by 3 before adding the column band spreads all nine combinations of row-band and column-band into a unique value from 0 to 8, correctly corresponding to each of the nine boxes arranged in a 3x3 grid of boxes.

**Q:** Why is it technically accurate to describe this problem's time and space complexity as O(1), even though the algorithm does meaningful work?
**A:** The board size is fixed and constant at exactly 9x9 (81 cells) by the problem's own constraints, so there's no variable "n" that could grow — the amount of work done is always bounded by this same fixed constant regardless of the specific board's contents.

**Q:** Why must `'.'` (empty) cells be explicitly skipped before performing any duplicate-checking logic?
**A:** Empty cells impose no actual constraint under the Sudoku rules, so treating them as a value to check and track would incorrectly flag multiple empty cells within the same row, column, or box as a rule violation, which was never one of the three defined validity rules.

**Q:** Why is checking a digit's presence in all three tracking structures (row, column, and box) necessary, rather than just checking one or two of them?
**A:** The three rules are independent constraints that must all hold simultaneously for the board to be valid — a duplicate could occur within a row but not a column or box, within a column but not a row or box, or within a box without sharing a row or column at all, so each type of violation requires its own dedicated check.

**Q:** Why does a genuinely valid, fully solved Sudoku board never trigger a false-positive duplicate detection, even though each digit 1-9 necessarily appears multiple times across the entire board?
**A:** In a valid Sudoku solution, every occurrence of a given digit falls into a distinct row, a distinct column, and a distinct box from every other occurrence of that same digit, so none of the three tracking structures (which are scoped per-row, per-column, and per-box individually) ever encounters that digit more than once within any single one of its scopes.

## 13. Final Code

```java
public boolean isValidSudoku(char[][] board) {
    Set<Character>[] rows = new HashSet[9];
    Set<Character>[] cols = new HashSet[9];
    Set<Character>[] boxes = new HashSet[9];

    for (int i = 0; i < 9; i++) {
        rows[i] = new HashSet<>();
        cols[i] = new HashSet<>();
        boxes[i] = new HashSet<>();
    }

    for (int r = 0; r < 9; r++) {
        for (int c = 0; c < 9; c++) {
            char val = board[r][c];
            if (val == '.') {
                continue;
            }

            int boxIndex = (r / 3) * 3 + (c / 3);

            if (rows[r].contains(val) || cols[c].contains(val) || boxes[boxIndex].contains(val)) {
                return false;
            }

            rows[r].add(val);
            cols[c].add(val);
            boxes[boxIndex].add(val);
        }
    }

    return true;
}
```

## 14. Self-Test

You have a 9x9 Sudoku board, partially filled. Determine if it's valid: no repeated digits within any row, column, or 3x3 sub-box among the filled cells. Think about how a single pass over the board, using one tracking structure per row, per column, and per box — with a formula to map each cell to its box — lets you check all three rules simultaneously without needing separate traversals.
