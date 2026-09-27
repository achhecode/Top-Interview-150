---
problem: 48 - Rotate Image
url: https://leetcode.com/problems/rotate-image/
difficulty: Medium
section: Array / In-Place Manipulation
patterns: [transpose-then-reverse, layer-rotation]
data_structures: [array, matrix]
time: O(n^2)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An `n x n` 2D matrix representing an image.

**Required:** Rotate the image 90 degrees clockwise, **in-place** — modifying the input matrix directly, without allocating a separate matrix to build the rotated result and copy it back.

**Constraints that actually matter:**
- `1 <= n <= 20` — tiny; performance is not the challenge here at all — the entire difficulty is correctly implementing the in-place rotation logic without an auxiliary matrix, which the problem explicitly forbids.
- **The matrix is always square** (`n x n`) — this is essential, since a non-square matrix's dimensions would literally change after a 90-degree rotation (an `m x n` matrix becomes `n x m`), which wouldn't even be possible to represent "in-place" within the original array structure; squareness is what makes true in-place rotation feasible at all.
- **The problem explicitly forbids allocating another 2D matrix** — this is a deliberate, stated constraint (not just a space-optimization suggestion), meaning any solution that builds a separate rotated copy and returns or copies it back, while technically producing the correct values, is considered *not* to have solved the problem as intended. The intended solution must mutate the given matrix directly.

## 2. Recognition Signals

- "Rotate a matrix 90 degrees, in-place, no auxiliary matrix allowed" → this exact combination is the signature of the **transpose-then-reverse** technique, or its close relative, **layer-by-layer four-way cyclic rotation** — both achieve true in-place rotation using only O(1) extra space (a handful of temp variables), as opposed to any approach that builds a second matrix.
- The key mathematical insight enabling transpose-then-reverse: a 90-degree clockwise rotation of a matrix is mathematically equivalent to **first transposing the matrix (swap rows and columns, i.e., reflect across the main diagonal), then reversing each row**. Recognizing this decomposition of a single complex transformation into two simpler, well-understood in-place operations (transpose, then row-reversal) is the main "aha" for this problem.
- Alternatively, recognizing that a 90-degree rotation can be performed by processing the matrix in concentric square "layers" (like peeling an onion), and within each layer, cyclically rotating groups of four corresponding elements (top, right, bottom, left) using a single temp variable — this is a more manual, first-principles approach to the same in-place goal.

**Pattern:** Transpose Then Reverse Rows (decompose a 90-degree clockwise rotation into two simpler in-place operations: transpose across the main diagonal, then reverse each row) — with Layer-by-Layer Four-Way Cyclic Rotation as an alternative, more manual technique achieving the same in-place result.

## 3. Core Idea

A 90-degree clockwise rotation can be decomposed into two separate, simpler in-place transformations applied in sequence:

1. **Transpose the matrix:** swap `matrix[i][j]` with `matrix[j][i]` for all `i < j` (reflecting the matrix across its main diagonal, from top-left to bottom-right). This alone doesn't produce the rotated result, but it's a well-understood, easy-to-implement in-place operation.
2. **Reverse each row:** for each row, reverse its elements left-to-right (using the standard two-pointer swap-from-both-ends technique).

The combination of these two operations is mathematically equivalent to a 90-degree clockwise rotation: transposing moves what was in column `j` of row `i` to row `j`, column `i`; then reversing each row effectively "flips" the transposed result horizontally, which together produces exactly the clockwise rotation. Both sub-operations are individually simple, well-known in-place techniques, and their composition solves the full problem without ever needing a second matrix.

**Invariant:** After the transpose step, `matrix[i][j]` holds the value that was originally at `matrix[j][i]`; after the subsequent row-reversal step, `matrix[i][j]` holds the value that was originally at `matrix[n-1-j][i]` — which is exactly the value that belongs at position `(i, j)` after a genuine 90-degree clockwise rotation of the original matrix.

## 4. Approach 1 — Using an Auxiliary Matrix (Explicitly Disallowed, Shown for Contrast)

**Logic:** Allocate a new `n x n` matrix, compute each rotated position directly (`rotated[j][n-1-i] = matrix[i][j]`), then copy the rotated matrix's values back into the original matrix's cells.

```java
// NOTE: This approach is explicitly disallowed by the problem statement
// ("DO NOT allocate another 2D matrix"). Shown here only to illustrate
// the direct rotation formula before covering the required in-place technique.
public void rotate(int[][] matrix) {
    int n = matrix.length;
    int[][] rotated = new int[n][n];

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            rotated[j][n - 1 - i] = matrix[i][j];
        }
    }

    for (int i = 0; i < n; i++) {
        System.arraycopy(rotated[i], 0, matrix[i], 0, n);
    }
}
```

- **Time:** O(n²) — every cell is visited once to compute its rotated position, plus a copy-back pass.
- **Space:** O(n²) auxiliary for the `rotated` matrix.

**Why it is not acceptable here:** This approach is explicitly forbidden by the problem's own stated constraint ("DO NOT allocate another 2D matrix and do the rotation"), regardless of its correctness or complexity — it's shown here purely to illustrate the underlying rotation formula (`rotated[j][n-1-i] = matrix[i][j]`) that motivates understanding *why* the transpose-then-reverse decomposition works, before presenting the actual required in-place solution.

## 5. Approach 2 — Optimized (Transpose, Then Reverse Each Row, Fully In-Place)

**Algorithm:**
1. **Transpose:** for each `i` from `0` to `n-1`, and each `j` from `i+1` to `n-1` (only the upper triangle, to avoid swapping each pair twice), swap `matrix[i][j]` with `matrix[j][i]`.
2. **Reverse each row:** for each row `i`, reverse its `n` elements in place using a standard two-pointer swap from both ends toward the middle.

```java
public void rotate(int[][] matrix) {
    int n = matrix.length;

    // Step 1: transpose in place.
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[j][i];
            matrix[j][i] = temp;
        }
    }

    // Step 2: reverse each row in place.
    for (int i = 0; i < n; i++) {
        int left = 0;
        int right = n - 1;
        while (left < right) {
            int temp = matrix[i][left];
            matrix[i][left] = matrix[i][right];
            matrix[i][right] = temp;
            left++;
            right--;
        }
    }
}
```

- **Time:** O(n²) — the transpose step touches roughly half of the `n²` cells (the upper triangle, each swap handling two cells), and the row-reversal step touches all `n²` cells once more; both are O(n²), so the total remains O(n²).
- **Space:** O(1) auxiliary — only a single `temp` variable is ever needed at a time, for either a transpose swap or a row-reversal swap; no second matrix or array is allocated anywhere.

**Why this is optimal:** Every cell's value must be moved to its correct rotated position, so O(n²) is a hard lower bound on time, and this approach achieves it directly, matching (not exceeding) the auxiliary-matrix approach's time complexity while satisfying the problem's explicit in-place requirement. Space is O(1) because both the transpose and the row-reversal are classic, well-known techniques that only ever need a single temporary variable to perform an in-place swap — no additional data structure of any size is required, directly satisfying the problem's explicit prohibition on allocating another 2D matrix.

## 6. Dry Run

Example: `matrix = [[1,2,3],[4,5,6],[7,8,9]]`.
Chosen because it's the canonical example, and it clearly shows both the transpose step and the row-reversal step's individual contributions.

`n = 3`.

**Step 1 — Transpose (swap `matrix[i][j]` with `matrix[j][i]` for `i < j`):**

| i | j | matrix[i][j] before | matrix[j][i] before | After swap |
|---|---|---------------------------|---------------------------|----------------|
| 0 | 1 | 2 | 4 | matrix[0][1]=4, matrix[1][0]=2 |
| 0 | 2 | 3 | 7 | matrix[0][2]=7, matrix[2][0]=3 |
| 1 | 2 | 6 | 8 | matrix[1][2]=8, matrix[2][1]=6 |

After transpose: `matrix = [[1,4,7],[2,5,8],[3,6,9]]`.

**Step 2 — Reverse each row:**

- Row 0: `[1,4,7]` → reverse → `[7,4,1]`.
- Row 1: `[2,5,8]` → reverse → `[8,5,2]`.
- Row 2: `[3,6,9]` → reverse → `[9,6,3]`.

Exit condition: both steps complete after processing all rows.

**Final answer:** `matrix = [[7,4,1],[8,5,2],[9,6,3]]`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Auxiliary matrix | O(n²) | O(n²) | Explicitly disallowed by the problem's stated constraint |
| 2. Transpose then reverse rows | O(n²) | O(1) | Optimal and required; matches the problem's explicit in-place, no-second-matrix constraint |

Input/output space for `matrix` is O(n²) in both, as given by the problem — only the *extra* auxiliary space differs.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single-element matrix (`n = 1`) | Rotating a 1x1 matrix should trivially leave it unchanged | The transpose loop's inner condition `j` from `i+1` to `n-1` never executes for `n=1` (no valid `j` exists since `i+1=1` already exceeds `n-1=0`), and the row-reversal step on a single-element row is a no-op (`left < right` is false immediately since `left=0, right=0`), correctly leaving the single element untouched |
| Matrix with negative values (constraint allows down to -1000) | Comparisons and swaps must work correctly regardless of sign | Plain integer swaps via a temp variable work identically regardless of the values' signs, with no special-casing needed |
| Matrix with all identical values | Should still "rotate" correctly, though the result would look unchanged since every value is the same | Both the transpose and row-reversal steps perform their swaps mechanically based on position, not value, so the algorithm's correctness doesn't depend on the values being distinct — the result is correctly computed even though it happens to look identical to the input in this specific case |
| Even `n` (e.g. `n=4`, as in the second example) vs. odd `n` (e.g. `n=3`) | Both must be handled by the same uniform logic, without needing separate code paths for a "middle" row/column that only exists when `n` is odd | The transpose loop's bounds (`i` from 0 to n-1, `j` from i+1 to n-1) and the row-reversal's two-pointer convergence (`left < right`) both work correctly and uniformly regardless of whether `n` is even or odd — an odd `n`'s middle row/column is naturally handled without any special case, since the two-pointer reversal simply does nothing extra when `left` and `right` meet at the same middle index |
| Maximum constraint size (`n = 20`) | Should still function correctly and efficiently at the upper bound, though given the tiny size, this is a non-issue for performance | The algorithm's correctness and O(n²) time bound apply uniformly regardless of the specific (small) size within the given constraints |

## 9. Java Notes

- **In-place transpose using only the upper triangle (`j` starting from `i+1`, not `0`):** iterating `j` from `i+1` rather than `0` ensures each pair `(i,j)` and `(j,i)` is swapped exactly once, rather than being swapped twice (which would cancel out and leave the matrix unchanged) — this is a subtle but essential detail of implementing an in-place transpose correctly.
- **Two-pointer row reversal, identical to the standard `reverse` helper pattern seen in other problems (like LC 189 Rotate Array):** reusing this well-known in-place reversal technique for each row is a direct, idiomatic application of a broadly useful pattern.
- **`void` return type:** since the problem requires modifying the input matrix directly rather than returning a new one, the method signature appropriately returns `void`, with all mutation happening directly on the `matrix` parameter (which, as a 2D array, is passed by reference in Java, so in-place modifications are visible to the caller).
- **No overflow risk:** given `-1000 <= matrix[i][j] <= 1000`, all values and swaps involve simple assignment, never arithmetic that could overflow.

## 10. Common Mistakes

- **Iterating the transpose loop's inner `j` from `0` instead of `i+1`.** This would cause every off-diagonal pair to be swapped twice (once when processing `(i,j)` and again when processing `(j,i)` later), which cancels out and leaves the matrix completely untransposed. Fix: always start the inner transpose loop at `j = i + 1`, covering only the upper triangle, so each pair is swapped exactly once.
- **Reversing columns instead of rows (or reversing in the wrong direction) after the transpose step.** The correct decomposition for a *clockwise* rotation is transpose-then-reverse-rows; reversing columns instead, or reversing rows for a counter-clockwise rotation, would produce an incorrect or differently-oriented result. Fix: carefully confirm the transpose-then-reverse-*rows* combination specifically for clockwise rotation (transpose-then-reverse-*columns*, or equivalently reverse-rows-then-transpose, produces a counter-clockwise rotation instead).
- **Allocating an auxiliary matrix despite the problem's explicit prohibition**, perhaps because it's the more intuitive first approach to think of. While this produces a correct rotated result, it doesn't satisfy the problem's explicit "DO NOT allocate another 2D matrix" requirement, and would typically not be accepted as a correct solution to this specific problem's constraints. Fix: always implement the in-place transpose-then-reverse (or layer-rotation) technique when this constraint is explicitly stated.
- **Forgetting that the matrix is guaranteed square, and attempting to apply this exact in-place technique to a non-square matrix.** A non-square matrix's rotated result has swapped dimensions (`m x n` becomes `n x m`), which fundamentally cannot be represented by mutating the original array structure in place — this in-place technique specifically relies on and requires the matrix being square. Fix: recognize that true in-place rotation is only meaningful and possible for square matrices; a non-square rotation would require returning a newly-shaped array.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Rotate a square matrix 90 degrees clockwise, in-place, no auxiliary matrix → transpose the matrix (swap across the main diagonal), then reverse each row."
- **90-second explanation:** "I break this down into two simpler, well-known in-place operations instead of trying to directly compute the rotation in one step. First, I transpose the matrix — swapping `matrix[i][j]` with `matrix[j][i]` for every pair above the main diagonal, which reflects the matrix across that diagonal. This alone doesn't produce the rotated result, but it's a standard, easy-to-get-right operation. Second, I reverse each row of the now-transposed matrix, using the classic two-pointer swap-from-both-ends technique. The combination of these two steps is mathematically equivalent to a full 90-degree clockwise rotation. Both steps only ever need a single temporary variable to perform their swaps, so the entire process runs in O(n squared) time — since every cell needs to move to its correct position — with O(1) extra space, which is exactly what the problem requires by explicitly forbidding the allocation of a second matrix."
- **Related problems using this pattern:**
  - LeetCode 54 — Spiral Matrix (different traversal technique, but similarly a structured, boundary-aware matrix manipulation)
  - LeetCode 59 — Spiral Matrix II (related matrix-filling variant)
  - LeetCode 189 — Rotate Array (uses the same underlying reversal technique, applied to a 1D array instead)

## 12. Recall Questions

**Q:** Why is transposing the matrix and then reversing each row mathematically equivalent to a 90-degree clockwise rotation?
**A:** Transposing reflects the matrix across its main diagonal, moving each element to the position with its row and column swapped; reversing each row of that transposed result then effectively flips it horizontally, and the combination of these two specific transformations produces exactly the same result as rotating the original matrix 90 degrees clockwise.

**Q:** Why must the transpose step's inner loop start at `j = i + 1` rather than `j = 0`?
**A:** Starting from `j = 0` would cause every off-diagonal pair of positions to be swapped twice — once while processing the earlier index as `i` and once again while processing the later index as `i` — and two swaps of the same pair cancel each other out, leaving the matrix completely unchanged instead of transposed.

**Q:** Why is it fundamentally impossible to perform this kind of in-place rotation on a non-square (`m x n` with `m != n`) matrix using this same technique?
**A:** Rotating a non-square matrix 90 degrees changes its dimensions from `m x n` to `n x m`, and since the original array structure has a fixed number of rows and columns that can't be reshaped by simply mutating values in place, there's no way to represent the differently-shaped result within the same original array structure.

**Q:** What would happen if the row-reversal step were replaced with a column-reversal step instead, after the same transpose operation?
**A:** That combination would produce a counter-clockwise rotation instead of the required clockwise rotation, since reversing columns after a transpose effectively flips the result vertically rather than horizontally, which corresponds to the opposite rotational direction.

**Q:** Why does the problem's explicit prohibition on allocating a second matrix specifically rule out the seemingly natural approach of computing each element's rotated position directly into a new matrix?
**A:** That approach, while mathematically correct and no worse in time complexity, requires an entire second `n x n` matrix as a staging area before copying values back, which directly violates the problem's explicit requirement to modify the given matrix directly rather than building and copying from a separate one.

## 13. Final Code

```java
public void rotate(int[][] matrix) {
    int n = matrix.length;

    // Step 1: transpose in place.
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[j][i];
            matrix[j][i] = temp;
        }
    }

    // Step 2: reverse each row in place.
    for (int i = 0; i < n; i++) {
        int left = 0;
        int right = n - 1;
        while (left < right) {
            int temp = matrix[i][left];
            matrix[i][left] = matrix[i][right];
            matrix[i][right] = temp;
            left++;
            right--;
        }
    }
}
```

## 14. Self-Test

You have an `n x n` matrix. Rotate it 90 degrees clockwise, in-place, without allocating another matrix. Think about why transposing the matrix across its main diagonal, then reversing each row, together produces exactly the same result as a direct clockwise rotation — and why the transpose step's inner loop must start just past the diagonal, not from the very beginning of each row.
