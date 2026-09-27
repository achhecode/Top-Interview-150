---
problem: 73 - Set Matrix Zeroes
url: https://leetcode.com/problems/set-matrix-zeroes/
difficulty: Medium
section: Array / In-Place Manipulation
patterns: [marker-reuse, first-row-column-as-flags]
data_structures: [array, matrix]
time: O(m*n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An `m x n` integer matrix.

**Required:** For every cell that contains `0`, set its **entire row and entire column** to `0`. Must be done **in-place** (modifying the given matrix directly).

**Constraints that actually matter:**
- `1 <= m, n <= 200` — moderate size; the follow-up explicitly walks through a progression of space complexities (`O(mn)` → `O(m+n)` → `O(1)`), signaling that the *intended* lesson of this problem is specifically about progressively reducing auxiliary space, not about achieving faster time complexity (all three approaches share the same O(m×n) time).
- Values span the full 32-bit `int` range (`-2^31` to `2^31 - 1`) — this matters specifically because it means **you cannot simply reuse an existing non-zero sentinel value** (like marking a cell with some special marker value) as a way to "remember" which rows/columns need zeroing, since *any* value, including extreme ones, might already legitimately appear in the matrix.
- **The core difficulty is avoiding "cascading" false positives:** naively zeroing cells as soon as a `0` is found during the same pass that's still scanning for original zeros would cause newly-zeroed cells to be mistaken for original zeros, incorrectly triggering their own row/column to be zeroed too. The correct approach must first fully **identify** all rows/columns that need zeroing, and only *then* apply the zeroing, in a separate pass.

## 2. Recognition Signals

- "If a cell is 0, zero its entire row and column, in-place" → the signature of a **mark-then-apply** technique: first scan the entire matrix to record *which* rows and columns need zeroing (without modifying anything yet), then make a second pass to actually apply the zeroing based on those records — critically, these two phases must be kept separate to avoid the cascading-false-positive problem described above.
- The explicit follow-up's staged progression (`O(mn)` space → `O(m+n)` space → `O(1)` space) is itself the strongest signal in the entire problem: it's explicitly guiding the solver toward recognizing that the "which rows/columns need zeroing" information can be stored more and more compactly — first as two full boolean arrays (O(m+n)), and ultimately by **reusing the matrix's own first row and first column** as the storage for that same information, achieving O(1) extra space.
- Whenever a problem needs to "remember" a set of row/column indices without extra space, and the matrix itself has some naturally reusable "storage" (like its own first row/column, which can be sacrificed as marker space as long as their original zero-status is tracked separately) — this in-place marker-reuse technique is the general pattern to reach for.

**Pattern:** Mark-Then-Apply Using the Matrix's Own First Row and Column as O(1) Marker Storage (identify which rows/columns need zeroing by encoding that information into the matrix's own first row and first column, using two extra boolean flags to separately track whether the first row/column themselves originally contained a zero).

## 3. Core Idea

The problem can't be solved by zeroing cells as they're discovered during the same scan, since that would corrupt the very data still being scanned (a newly-zeroed cell looks identical to an original zero). So, split the work into two phases: **first, identify** every row and column that contains at least one original zero, without modifying the matrix yet; **second, apply** the zeroing based on that recorded information.

The space-optimized version of "recording which rows/columns need zeroing" reuses the matrix's own first row and first column as the storage for this record: use `matrix[i][0]` to mark whether row `i` needs zeroing, and `matrix[0][j]` to mark whether column `j` needs zeroing. This works because the first row and first column are themselves part of the matrix and would need to be checked and potentially zeroed anyway — the only wrinkle is that the *original* zero-status of the first row and first column themselves must be captured **before** they get overwritten with marker information, using two separate boolean variables (`firstRowHasZero`, `firstColHasZero`).

**Invariant:** After the marking pass, `matrix[i][0] == 0` if and only if row `i` (excluding cell `(i,0)` itself, which is part of the shared marker space) originally contained a zero somewhere in columns `1` through `n-1`, or row `i` is row 0 and column 0's own original value was zero (handled via `matrix[0][0]`, shared between row and column marking, requiring the two separate boolean flags to disambiguate row-0 and column-0's own original zero status).

## 4. Approach 1 — Brute Force (Full O(m×n) Auxiliary Copy)

**Logic:** Make a full copy of the original matrix, then scan the *copy* for zeros, applying the corresponding row/column zeroing to the *original* matrix — using a separate copy avoids the cascading problem entirely, at the cost of full O(m×n) auxiliary space.

```java
public void setZeroes(int[][] matrix) {
    int m = matrix.length;
    int n = matrix[0].length;
    int[][] original = new int[m][n];
    for (int i = 0; i < m; i++) {
        original[i] = matrix[i].clone();
    }

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            if (original[i][j] == 0) {
                for (int col = 0; col < n; col++) {
                    matrix[i][col] = 0;
                }
                for (int row = 0; row < m; row++) {
                    matrix[row][j] = 0;
                }
            }
        }
    }
}
```

- **Time:** O(m × n) for the initial copy, plus up to O(m × n × (m + n)) in the worst case for the zeroing itself (each original zero could trigger zeroing an entire row and column) — though this can be tightened to O(m×n) overall by instead just recording row/column *indices* that need zeroing rather than re-zeroing repeatedly, which Approach 2 does.
- **Space:** O(m × n) auxiliary for the full matrix copy.

**Why it is not optimal:** This directly matches the follow-up's explicitly-called-out "probably a bad idea" O(mn) space tier — while correct, it uses a full duplicate of the matrix just to preserve original zero information, which is far more space than necessary given the follow-up's guidance toward O(m+n) and ultimately O(1) space.

## 5. Approach 2 — Optimized (O(1) Space via First Row/Column as Markers)

**Algorithm:**
1. Determine `firstRowHasZero` and `firstColHasZero` by scanning row 0 and column 0 respectively for any original zero, **before** any marking begins.
2. **Marking pass:** for each `i` from `1` to `m-1` and each `j` from `1` to `n-1` (deliberately skipping row 0 and column 0, which are reserved for markers), if `matrix[i][j] == 0`, set `matrix[i][0] = 0` (mark row `i`) and `matrix[0][j] = 0` (mark column `j`).
3. **Zeroing pass:** for each `i` from `1` to `m-1` and each `j` from `1` to `n-1`, if `matrix[i][0] == 0` or `matrix[0][j] == 0`, set `matrix[i][j] = 0`.
4. **Handle row 0 and column 0 themselves last**, using the two flags captured in step 1: if `firstRowHasZero`, zero out all of row 0; if `firstColHasZero`, zero out all of column 0.

```java
public void setZeroes(int[][] matrix) {
    int m = matrix.length;
    int n = matrix[0].length;

    boolean firstRowHasZero = false;
    for (int j = 0; j < n; j++) {
        if (matrix[0][j] == 0) {
            firstRowHasZero = true;
            break;
        }
    }

    boolean firstColHasZero = false;
    for (int i = 0; i < m; i++) {
        if (matrix[i][0] == 0) {
            firstColHasZero = true;
            break;
        }
    }

    for (int i = 1; i < m; i++) {
        for (int j = 1; j < n; j++) {
            if (matrix[i][j] == 0) {
                matrix[i][0] = 0;
                matrix[0][j] = 0;
            }
        }
    }

    for (int i = 1; i < m; i++) {
        for (int j = 1; j < n; j++) {
            if (matrix[i][0] == 0 || matrix[0][j] == 0) {
                matrix[i][j] = 0;
            }
        }
    }

    if (firstRowHasZero) {
        for (int j = 0; j < n; j++) {
            matrix[0][j] = 0;
        }
    }

    if (firstColHasZero) {
        for (int i = 0; i < m; i++) {
            matrix[i][0] = 0;
        }
    }
}
```

- **Time:** O(m × n) — a small constant number of full passes over the matrix (capturing the two flags, marking, applying, and finally zeroing row 0/column 0 if needed), each O(m×n) or less.
- **Space:** O(1) auxiliary — only the two boolean flags, `firstRowHasZero` and `firstColHasZero`; no array or matrix of any size is allocated.

**Why this is optimal:** Every cell must be examined at least once to determine whether it's an original zero, so O(m×n) time is a hard lower bound, matching what this approach achieves alongside every other approach discussed. Space is O(1) because the matrix's own first row and first column are repurposed as the storage for "which rows/columns need zeroing," rather than allocating any separate structure — this directly answers the follow-up's explicit challenge to find a constant-space solution, improving on both the O(mn) and the intermediate O(m+n) (two separate boolean arrays, not shown but conceptually simpler) tiers.

## 6. Dry Run

Example: `matrix = [[1,1,1],[1,0,1],[1,1,1]]`.
Chosen because it's the canonical example with a single interior zero, clearly showing the marking and subsequent zeroing without extra complexity from row-0/column-0 interactions.

`m=3, n=3`.

**Step 1 — capture flags:** Row 0 = `[1,1,1]`, no zero → `firstRowHasZero = false`. Column 0 = `[1,1,1]` (matrix[0][0], matrix[1][0], matrix[2][0]), no zero → `firstColHasZero = false`.

**Step 2 — marking pass** (`i` from 1 to 2, `j` from 1 to 2):

| i | j | matrix[i][j] | Action |
|---|---|--------------------|--------|
| 1 | 1 | 0 | matrix[1][0]=0, matrix[0][1]=0 |
| 1 | 2 | 1 | no action |
| 2 | 1 | 1 | no action |
| 2 | 2 | 1 | no action |

After marking: `matrix = [[1,0,1],[0,0,1],[1,1,1]]` (matrix[1][0] and matrix[0][1] now marked as 0).

**Step 3 — zeroing pass** (`i` from 1 to 2, `j` from 1 to 2), checking `matrix[i][0]==0 || matrix[0][j]==0`:

| i | j | matrix[i][0] | matrix[0][j] | Condition | Action |
|---|---|--------------------|--------------------|-----------|--------|
| 1 | 1 | 0 | 0 | true | matrix[1][1]=0 (already 0) |
| 1 | 2 | 0 | 1 | true (matrix[1][0]=0) | matrix[1][2]=0 |
| 2 | 1 | 1 | 0 | true (matrix[0][1]=0) | matrix[2][1]=0 |
| 2 | 2 | 1 | 1 | false | no action |

After zeroing pass: `matrix = [[1,0,1],[0,0,0],[1,0,1]]`.

**Step 4 — row 0/column 0 handling:** `firstRowHasZero=false`, `firstColHasZero=false`, so no further changes.

Exit condition: all four steps complete.

**Final answer:** `matrix = [[1,0,1],[0,0,0],[1,0,1]]`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Full matrix copy | O(m×n) | O(m×n) | Explicitly called out by the follow-up as "probably a bad idea" |
| (Intermediate) Two boolean arrays | O(m×n) | O(m+n) | A "simple improvement," per the follow-up, but not the best |
| 2. First row/column as markers | O(m×n) | O(1) | Optimal; directly answers the follow-up's constant-space challenge |

Input/output space for `matrix` is O(m×n) in all approaches, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| A zero located in row 0 or column 0 itself | The marker-reuse technique overwrites row 0/column 0 with marking information, so their *original* zero status must be captured separately before it's potentially overwritten | The `firstRowHasZero` and `firstColHasZero` flags are computed in a dedicated first pass, strictly before any marking begins, correctly preserving this information regardless of what happens to row 0/column 0 afterward |
| A zero at `matrix[0][0]` specifically | This single cell is shared between the row-0 marker space and the column-0 marker space, so its original zero status affects both `firstRowHasZero` and `firstColHasZero` simultaneously | Since both flags are computed by independently scanning all of row 0 and all of column 0 respectively (both scans include index 0), a zero at `matrix[0][0]` correctly sets *both* flags to true, ensuring both row 0 and column 0 get zeroed in the final step |
| No zeros anywhere in the matrix | Should leave the matrix completely unchanged | Both flags remain `false`, the marking pass never sets any marker (since no `matrix[i][j] == 0` condition is ever true for `i,j >= 1`), the zeroing pass never triggers any change, and the final row-0/column-0 handling is skipped entirely — matrix is correctly left untouched |
| Every cell is already zero | Should correctly leave the matrix as all zeros (a trivial but valid case) | Both flags become `true` (since every cell, including all of row 0 and column 0, is zero), the marking pass marks every row and column (redundantly, but harmlessly, since they're already zero), the zeroing pass sets every interior cell to zero (already zero), and the final step zeroes row 0 and column 0 (already zero) — result is correctly unchanged |
| Single-row or single-column matrix (`m=1` or `n=1`) | The interior marking/zeroing loops (which start at index 1) may have a very limited or empty range to work with | If `m=1`, there are no rows beyond row 0, so the marking and zeroing passes' outer loop (`i` from 1 to `m-1`) simply doesn't execute, and the entire problem reduces correctly to just checking and applying `firstRowHasZero` (which, since `m=1`, covers the entire matrix) — the symmetric case applies for `n=1` |
| Values at the extremes of the 32-bit int range (`-2^31`, `2^31-1`) | Must confirm these extreme values are never confused with the marker value 0, and that no arithmetic on them risks overflow | The algorithm only ever checks for exact equality with 0 (`== 0`) and sets values directly to 0 — it never performs arithmetic on the actual matrix values, so extreme values pose no special risk; they're simply preserved as-is unless their row/column is being legitimately zeroed |

## 9. Java Notes

- **`int[].clone()` (used in Approach 1):** a convenient way to create a shallow copy of a 1D primitive array; used here to build a full independent copy of each row for the brute-force approach.
- **Reusing `matrix[i][0]` and `matrix[0][j]` as marker storage:** this is the crux Java-level (and general) technique of this optimized solution — recognizing that these specific cells are safe to overwrite with marker information as long as their *original* values are separately preserved (via the two boolean flags) before being overwritten.
- **Order of operations matters critically:** the two flag-capturing scans must happen strictly before the marking pass, the marking pass must complete strictly before the zeroing pass (since the zeroing pass reads the markers set by the marking pass), and the final row-0/column-0 zeroing must happen strictly last (after the interior zeroing pass, so it doesn't itself get read as if it were original marker data) — getting this sequencing wrong in any way corrupts the result.
- **No overflow or numeric risk:** the algorithm only ever compares values to 0 and assigns 0; it never performs arithmetic on the actual matrix values, so the full 32-bit int range poses no special risk.

## 10. Common Mistakes

- **Zeroing cells immediately upon finding a zero, during the very same pass that's still scanning for original zeros.** This is the classic cascading-false-positive bug: a cell that gets zeroed because of an earlier-found zero in its row/column can then be misinterpreted, later in that same scan, as if it were itself an *original* zero, incorrectly triggering its own row and column to be zeroed too, cascading further than intended. Fix: always separate the "identify which rows/columns need zeroing" phase from the "apply the zeroing" phase into two distinct passes.
- **Forgetting to capture `firstRowHasZero` and `firstColHasZero` before the marking pass begins.** Since the marking pass overwrites row 0 and column 0 with marker information, if their original zero status isn't captured first, that information is permanently lost, making it impossible to correctly determine at the end whether row 0/column 0 themselves need to be zeroed. Fix: always perform the two flag-capturing scans as the very first step, before any marking occurs.
- **Applying the row-0/column-0 zeroing (based on the two flags) before or during the interior marking/zeroing passes, rather than strictly last.** Zeroing row 0 or column 0 too early would destroy the marker information those cells are being used to store, corrupting the interior marking/zeroing logic that still needs to read them. Fix: always perform the final row-0/column-0 zeroing as the very last step, after the interior zeroing pass has already completed and no longer needs to read the markers.
- **Attempting to use some other sentinel value (instead of reusing the first row/column) to mark cells for later zeroing, given that values span the full int range.** Since any value, including extreme ones, might legitimately already appear in the matrix, there's no safe "unused" sentinel value available to use as a marker directly within arbitrary cells — this is exactly why the technique specifically repurposes the *first row and column* (tracked via separate boolean flags for their own original status) rather than trying to find an unused value to overwrite arbitrary cells with.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Zero out entire rows/columns based on found zeros, in-place, O(1) space → first identify which rows/columns need zeroing (without applying yet, to avoid cascading), storing that information in the matrix's own first row and column, with two extra flags to preserve their own original zero status."
- **90-second explanation:** "I can't just zero cells as I find them, because a newly-zeroed cell would then be mistaken for an original zero during the same scan, cascading incorrectly. So I split this into two phases: first, I scan the matrix to identify every row and column that contains an original zero, and second, I apply the zeroing based on that recorded information. To do this identification step with O(1) extra space, instead of using separate arrays, I reuse the matrix's own first row and first column as the storage for 'does this row/column need zeroing' — but since row 0 and column 0 are themselves part of the matrix and might have originally contained a zero, I first capture their own original zero status into two separate boolean flags before I start overwriting them as markers. After marking every row and column, I do a second pass to actually zero out the interior cells based on those markers, and only at the very end do I use the two flags to decide whether to zero out row 0 and column 0 themselves, since doing that any earlier would destroy the marker information I still needed. This achieves the follow-up's constant-space challenge while still visiting each cell a small constant number of times, for O(m times n) time overall."
- **Related problems using this pattern:**
  - LeetCode 289 — Game of Life (a different problem, but shares the same "don't corrupt data you're still reading" in-place mutation challenge)
  - LeetCode 48 — Rotate Image (different specific technique, but similarly an in-place matrix manipulation problem)
  - LeetCode 54 — Spiral Matrix (different technique, but same general "careful in-place matrix traversal" family)

## 12. Recall Questions

**Q:** Why can't zeros be applied to the matrix during the same pass that's still scanning for the original zeros?
**A:** A cell that gets zeroed as a result of an earlier-found zero would then look identical to a genuine original zero for the rest of that same scan, incorrectly triggering its own row and column to also be zeroed, cascading the effect far beyond what the actual original zeros warrant.

**Q:** Why must the two flags (`firstRowHasZero`, `firstColHasZero`) be captured before the marking pass begins, rather than at any other point?
**A:** The marking pass overwrites cells in row 0 and column 0 with marker information for other rows/columns, so if the original zero status of row 0 and column 0 themselves isn't captured before that overwriting happens, that original information is permanently lost and can never be recovered afterward.

**Q:** Why is it safe to reuse the matrix's own first row and first column as marker storage, given that matrix values can span the entire 32-bit int range?
**A:** Rather than trying to find some unused sentinel value to mark arbitrary cells with (which isn't safely possible given the full value range), this technique specifically designates the first row and column as the marker space, and separately preserves their own original zero status using two dedicated boolean flags before they get overwritten.

**Q:** Why must the final zeroing of row 0 and column 0 (based on the two flags) happen strictly after the interior marking and zeroing passes, not before or during them?
**A:** Row 0 and column 0 are still being used as active marker storage that the interior zeroing pass needs to read from, so zeroing them out prematurely would destroy that marker information before it's been fully used, corrupting the interior zeroing logic.

**Q:** How does this problem's O(1) space solution specifically build on and improve over an intermediate solution using two separate boolean arrays (one for rows, one for columns)?
**A:** The two-boolean-array approach already avoids a full O(mn) matrix copy by only tracking which specific rows and columns need zeroing (O(m+n) space), but this optimized solution goes further by recognizing that this same "which rows/columns need zeroing" information can be stored directly within the matrix's own first row and column, eliminating the need for any separate array at all.

## 13. Final Code

```java
public void setZeroes(int[][] matrix) {
    int m = matrix.length;
    int n = matrix[0].length;

    boolean firstRowHasZero = false;
    for (int j = 0; j < n; j++) {
        if (matrix[0][j] == 0) {
            firstRowHasZero = true;
            break;
        }
    }

    boolean firstColHasZero = false;
    for (int i = 0; i < m; i++) {
        if (matrix[i][0] == 0) {
            firstColHasZero = true;
            break;
        }
    }

    for (int i = 1; i < m; i++) {
        for (int j = 1; j < n; j++) {
            if (matrix[i][j] == 0) {
                matrix[i][0] = 0;
                matrix[0][j] = 0;
            }
        }
    }

    for (int i = 1; i < m; i++) {
        for (int j = 1; j < n; j++) {
            if (matrix[i][0] == 0 || matrix[0][j] == 0) {
                matrix[i][j] = 0;
            }
        }
    }

    if (firstRowHasZero) {
        for (int j = 0; j < n; j++) {
            matrix[0][j] = 0;
        }
    }

    if (firstColHasZero) {
        for (int i = 0; i < m; i++) {
            matrix[i][0] = 0;
        }
    }
}
```

## 14. Self-Test

You have an `m x n` matrix. If a cell is 0, set its entire row and column to 0, in-place, using O(1) extra space. Think about why you can't zero cells during the same scan that's still looking for original zeros, and how the matrix's own first row and column can be repurposed to record which rows/columns need zeroing, as long as their own original zero status is captured first.
