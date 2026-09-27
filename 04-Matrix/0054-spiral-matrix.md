---
problem: 54 - Spiral Matrix
url: https://leetcode.com/problems/spiral-matrix/
difficulty: Medium
section: Array / Simulation
patterns: [boundary-shrinking, simulation]
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

**Given:** An `m x n` matrix.

**Required:** Return all elements of the matrix, visited in spiral order — starting at the top-left, moving right across the top row, then down the right column, then left across the bottom row, then up the left column, and repeating this pattern inward until every element has been visited exactly once.

**Constraints that actually matter:**
- `1 <= m, n <= 10` — very small; performance is not a concern at all here — the entire challenge is correctly implementing the boundary-tracking logic for the spiral traversal, not algorithmic efficiency.
- The matrix need not be square (`m` and `n` can differ) — the traversal logic must correctly handle rectangular matrices, not just square ones, including the specific edge cases that arise when the matrix degenerates to a single row or a single column partway through the spiral.
- Each element is visited **exactly once** — the traversal must terminate precisely when all `m * n` elements have been collected, neither stopping early nor attempting to revisit already-collected cells (which would happen if boundary shrinking isn't handled carefully).

## 2. Recognition Signals

- "Traverse a matrix in spiral order" → the specific signature of the **boundary-shrinking simulation** technique: maintain four boundaries (top, bottom, left, right) representing the current "unvisited ring" of the matrix, traverse each of the four sides of that ring in order, then shrink the corresponding boundary inward after each side is completed.
- Whenever a traversal needs to move in a repeating cycle of directions (right, down, left, up) around a shrinking rectangular region — tracking four boundary variables that each get adjusted inward after their corresponding side is fully traversed is the standard, reliable technique, more robust than trying to track a single "current direction" with complex turn-detection logic.
- The need to carefully re-check boundary validity **between** each of the four directional passes (not just once per full loop iteration) is itself a signal — since a rectangular (non-square) matrix can have its rows fully exhausted before its columns, or vice versa, requiring a check after every single side, not just after all four.

**Pattern:** Boundary-Shrinking Simulation (maintain four boundary variables — top, bottom, left, right — traverse the current ring's four sides in order, shrinking the corresponding boundary after each side, and re-validating boundaries between every single side to correctly handle non-square matrices).

## 3. Core Idea

Think of the matrix as a series of nested rectangular "rings," from the outermost ring inward. Track four boundary variables: `top`, `bottom`, `left`, `right`, initially set to the matrix's actual edges. Repeatedly traverse the current ring's four sides in order — left-to-right along the top row, top-to-bottom along the right column, right-to-left along the bottom row, bottom-to-top along the left column — and after completing each side, shrink the corresponding boundary inward (`top++`, `right--`, `bottom--`, `left++` respectively). Critically, re-check whether the boundaries are still valid (`top <= bottom` and `left <= right`) **before starting each of the last two sides** (the right-to-left bottom pass and the bottom-to-top left pass), since a non-square matrix can exhaust its rows or columns partway through a ring, making one or both of those final two sides degenerate (already fully covered by an earlier side in this same ring) and thus needing to be skipped to avoid revisiting cells.

**Invariant:** At the start of processing each ring, every cell strictively outside the current `[top, bottom] x [left, right]` boundary rectangle has already been visited exactly once and added to the result, and every cell strictly inside it has not yet been visited — so traversing the current ring's valid sides and then shrinking the boundaries correctly extends this invariant to the next, smaller ring.

## 4. Approach 1 — Brute Force / Alternative (Direction-Vector with Visited-Tracking)

**Logic:** Simulate the spiral using a single moving pointer and a repeating cycle of direction vectors (right, down, left, up), turning to the next direction whenever the next cell would be out of bounds or already visited — this requires an auxiliary `visited` matrix to detect the "already visited" turning condition.

```java
public List<Integer> spiralOrder(int[][] matrix) {
    int m = matrix.length;
    int n = matrix[0].length;
    List<Integer> result = new ArrayList<>();
    boolean[][] visited = new boolean[m][n];

    int[] dRow = {0, 1, 0, -1};
    int[] dCol = {1, 0, -1, 0};
    int dir = 0;
    int row = 0, col = 0;

    for (int i = 0; i < m * n; i++) {
        result.add(matrix[row][col]);
        visited[row][col] = true;

        int nextRow = row + dRow[dir];
        int nextCol = col + dCol[dir];

        if (nextRow < 0 || nextRow >= m || nextCol < 0 || nextCol >= n || visited[nextRow][nextCol]) {
            dir = (dir + 1) % 4;
            nextRow = row + dRow[dir];
            nextCol = col + dCol[dir];
        }

        row = nextRow;
        col = nextCol;
    }

    return result;
}
```

- **Time:** O(m × n) — each of the `m × n` cells is visited exactly once.
- **Space:** O(m × n) auxiliary for the `visited` matrix, in addition to the required output list.

**Why it is not optimal:** While correct and conceptually intuitive (literally simulating "walk forward, turn right when blocked"), this approach requires an entire auxiliary `visited` matrix of the same size as the input just to detect when a turn is needed — this is genuinely unnecessary extra space, since the boundary-shrinking approach achieves the exact same traversal using only four integer variables instead of a full matrix.

## 5. Approach 2 — Optimized (Boundary Shrinking, No Auxiliary Matrix)

**Algorithm:**
1. Initialize `top = 0`, `bottom = m - 1`, `left = 0`, `right = n - 1`.
2. While `top <= bottom` and `left <= right`:
   - **Traverse top row, left to right:** for `col` from `left` to `right`, add `matrix[top][col]`. Then `top++`.
   - **Traverse right column, top to bottom:** for `row` from `top` to `bottom`, add `matrix[row][right]`. Then `right--`.
   - **Check boundaries again** (`top <= bottom`): if still valid, traverse bottom row, right to left: for `col` from `right` down to `left`, add `matrix[bottom][col]`. Then `bottom--`.
   - **Check boundaries again** (`left <= right`): if still valid, traverse left column, bottom to top: for `row` from `bottom` down to `top`, add `matrix[row][left]`. Then `left++`.
3. Return the collected result list.

```java
public List<Integer> spiralOrder(int[][] matrix) {
    List<Integer> result = new ArrayList<>();
    int top = 0, bottom = matrix.length - 1;
    int left = 0, right = matrix[0].length - 1;

    while (top <= bottom && left <= right) {
        for (int col = left; col <= right; col++) {
            result.add(matrix[top][col]);
        }
        top++;

        for (int row = top; row <= bottom; row++) {
            result.add(matrix[row][right]);
        }
        right--;

        if (top <= bottom) {
            for (int col = right; col >= left; col--) {
                result.add(matrix[bottom][col]);
            }
            bottom--;
        }

        if (left <= right) {
            for (int row = bottom; row >= top; row--) {
                result.add(matrix[row][left]);
            }
            left++;
        }
    }

    return result;
}
```

- **Time:** O(m × n) — every cell is visited and added to the result exactly once across the entire traversal.
- **Space:** O(1) auxiliary (excluding the required output list) — only four boundary integer variables are needed, no auxiliary matrix.

**Why this is optimal:** Every cell must be visited exactly once to be included in the output, so O(m × n) is a hard lower bound on time, matching what this approach achieves. Space is O(1) auxiliary because the four boundary variables alone are sufficient to correctly track which cells remain unvisited, without needing an explicit `visited` matrix — this is a genuine improvement over Approach 1's O(m × n) auxiliary space, since the boundary-shrinking invariant inherently guarantees no cell is ever revisited, making explicit tracking unnecessary.

## 6. Dry Run

Example: `matrix = [[1,2,3,4],[5,6,7,8],[9,10,11,12]]` (`m=3, n=4`).
Chosen because it's a non-square matrix (the second canonical example), directly exercising the boundary re-checks needed to correctly terminate the last two sides of the single ring this matrix contains.

Initial: `top=0, bottom=2, left=0, right=3`.

**Iteration 1 (`top<=bottom` and `left<=right`, i.e. `0<=2` and `0<=3`, both true):**

- Top row, left to right (`col` 0 to 3): add `matrix[0][0..3]` = 1,2,3,4. `top++` → `top=1`.
- Right column, top to bottom (`row` 1 to 2): add `matrix[1][3]`, `matrix[2][3]` = 8,12. `right--` → `right=2`.
- Check `top<=bottom`: `1<=2`, true. Bottom row, right to left (`col` 2 to 0): add `matrix[2][2]`, `matrix[2][1]`, `matrix[2][0]` = 11,10,9. `bottom--` → `bottom=1`.
- Check `left<=right`: `0<=2`, true. Left column, bottom to top (`row` 1 to 1): add `matrix[1][0]` = 5. `left++` → `left=1`.

**Iteration 2 check:** `top<=bottom` (`1<=1`, true) and `left<=right` (`1<=2`, true) — loop continues.

- Top row, left to right (`col` 1 to 2): add `matrix[1][1]`, `matrix[1][2]` = 6,7. `top++` → `top=2`.
- Right column, top to bottom (`row` 2 to 1): loop condition `row<=bottom` is `2<=1`, false — no elements added. `right--` → `right=1`.
- Check `top<=bottom`: `2<=1`, false — skip bottom row entirely.
- Check `left<=right`: `1<=1`, true, but the left column loop `row` from `bottom(1)` down to `top(2)` — condition `row>=top` is `1>=2`, false — no elements added. `left++` → `left=2`.

**Iteration 3 check:** `top<=bottom` (`2<=1`), false — outer loop ends.

Exit condition: `top > bottom`, loop terminates.

**Final answer:** `[1,2,3,4,8,12,11,10,9,5,6,7]`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Direction-vector with visited matrix | O(m×n) | O(m×n) | Correct but uses an unnecessary auxiliary matrix to detect turns |
| 2. Boundary shrinking | O(m×n) | O(1) | Optimal; four boundary variables suffice, no auxiliary matrix needed |

Output space (the result list, size `m*n`) is required by both approaches and isn't counted as "extra" auxiliary space.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single row (`m == 1`) | After traversing the top row, the "right column" traversal (top to bottom) has no rows left to cover, and the bottom/left passes must be correctly skipped to avoid re-adding the same row's elements | After the top-row pass, `top` becomes 1, making `top > bottom` (`1 > 0`), so the right-column loop's condition `row <= bottom` is immediately false (no elements added), and the subsequent `top <= bottom` check before the bottom-row pass correctly evaluates to false, skipping it; the `left <= right` check for the left-column pass may still pass, but that loop's own bounds (`row` from `bottom` down to `top`, i.e., `0` down to `1`) also correctly finds no valid range |
| Single column (`n == 1`) | Symmetric to the single-row case; the top-row traversal alone would need to correctly avoid triggering redundant passes afterward | The top-row pass (a single element, since `left == right`) executes, then `top++` shrinks past all rows after the right-column pass, and both boundary re-checks correctly prevent any further redundant traversal |
| Single cell (`m == 1, n == 1`) | The simplest possible case; must return a list containing just that one element | The top-row pass adds the single element; the right-column pass finds no additional rows (`top` already incremented past `bottom`); the boundary checks correctly skip the remaining two passes, and the outer `while` loop condition fails on the next check, ending with exactly one element in the result |
| Square matrix, odd dimension (e.g. 3x3), converging to a single center cell | The innermost "ring" is just one cell, and the traversal must correctly identify and add it exactly once, not zero or multiple times | As the boundaries shrink inward with each full ring, they eventually converge such that `top == bottom` and `left == right` simultaneously, at which point the top-row pass (a loop of exactly one iteration) correctly adds that single center cell, and the subsequent boundary re-checks prevent any of the remaining passes from redundantly re-adding it |
| Square matrix, even dimension (e.g. 4x4), converging to a final 2x2 (or similar) inner ring with no true "center cell" | The final ring must be traversed completely and correctly without any degenerate skipped sides, since a 2x2 (or larger) final ring genuinely needs all four sides properly handled | The boundary-shrinking logic and re-checks work identically at any ring size — there's no special-casing needed for the final ring versus any other ring, since the same general logic correctly handles rings of any remaining size down to a single cell |
| Maximum constraint size (`m, n` both up to 10) | Should still function correctly and efficiently at the upper bound, though given the tiny size, this is a non-issue for performance | The algorithm's correctness and O(m×n) time bound apply uniformly regardless of the specific (small) size within the given constraints |

## 9. Java Notes

- **Four `int` boundary variables (`top`, `bottom`, `left`, `right`) instead of a `boolean[][] visited` matrix:** this is the key space-saving realization — the shrinking-boundary invariant alone is sufficient to guarantee no cell is revisited, without needing explicit auxiliary tracking, since the geometry of the "remaining unvisited rectangle" is always well-defined at every point in the algorithm.
- **The two intermediate boundary re-checks (`if (top <= bottom)` and `if (left <= right)`) between the third and fourth sides:** these are not optional or merely defensive — they are essential correctness logic for non-square (and single-row/column) matrices, since without them, a matrix like `1 x n` or `m x 1` would have its already-fully-covered top row incorrectly re-traversed (in reverse) as a "bottom row," double-counting elements.
- **`List<Integer>` (via `ArrayList`) for the result:** appropriate here since the required return type is `List<Integer>`, and the total number of elements (`m * n`) is known in advance, though Java's `ArrayList` doesn't require pre-sizing to function correctly (though pre-sizing via `new ArrayList<>(m * n)` could be a minor, optional performance tweak to avoid internal resizing).
- **No overflow or numeric risk:** all values and index arithmetic here are small and bounded by the tiny constraint sizes (`m, n <= 10`); no risk of overflow anywhere.

## 10. Common Mistakes

- **Forgetting the intermediate boundary re-checks before the third and fourth side traversals.** Without `if (top <= bottom)` before the bottom-row pass and `if (left <= right)` before the left-column pass, a single-row or single-column matrix (or the final ring of certain matrix shapes) would have already-visited cells incorrectly re-added to the result, producing a wrong, over-long output. Fix: always re-validate boundary validity between the third and fourth sides specifically, not just once per full outer loop iteration.
- **Shrinking a boundary variable before finishing its corresponding side's traversal**, e.g., incrementing `top` in the middle of the top-row loop instead of after it completes. This would cause the loop to read from an incorrect (already-shrunk) row partway through, corrupting the traversal. Fix: always complete the full inner `for` loop for a given side before applying that side's boundary adjustment.
- **Using the direction-vector-with-visited-matrix approach (Approach 1) without recognizing the unnecessary O(m×n) space cost it introduces**, when the boundary-shrinking approach achieves the identical result with only O(1) auxiliary space. Fix: prefer the boundary-shrinking technique as the default, more space-efficient solution for spiral traversal problems.
- **Getting the loop bounds or direction backward for any of the four sides** (e.g., iterating `col` from `right` to `left` instead of `left` to `right` for the top row, or using the wrong comparison operator for a decreasing loop). Fix: carefully verify each of the four inner loops' start point, end point, and increment/decrement direction against the intended spiral direction (right, down, left, up) before trusting the implementation.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Traverse a matrix in spiral order → maintain four shrinking boundaries (top, bottom, left, right), traverse each side of the current ring in order, and re-check boundary validity between the third and fourth sides to correctly handle non-square matrices."
- **90-second explanation:** "I think of the matrix as a series of nested rectangular rings, and I track four boundary variables marking the edges of the current unvisited ring. I traverse each of the four sides of that ring in spiral order — left to right across the top, top to bottom down the right side, right to left across the bottom, and bottom to top up the left side — shrinking the corresponding boundary inward after finishing each side. The one subtlety is that after the first two sides, I need to re-check whether the boundaries are still valid before attempting the third and fourth sides, because a non-square matrix — like a single row or a very wide, short matrix — can have its rows or columns fully exhausted partway through a ring, and without that check, I'd end up re-traversing and duplicating cells that the earlier sides already covered. This approach only needs four integer variables to track everything, so it runs in O(m times n) time, visiting each cell exactly once, with O(1) auxiliary space — no need for a separate visited-tracking matrix like a more naive direction-vector simulation would require."
- **Related problems using this pattern:**
  - LeetCode 59 — Spiral Matrix II (generates a matrix by filling it in spiral order, using the same boundary-shrinking technique)
  - LeetCode 885 — Spiral Matrix III (a variant starting from an arbitrary cell, extending the spiral concept)
  - LeetCode 498 — Diagonal Traverse (a different, but conceptually related, structured matrix-traversal simulation)

## 12. Recall Questions

**Q:** Why is it necessary to re-check boundary validity specifically between the second and third sides, and again between the third and fourth sides, rather than just once per full outer loop iteration?
**A:** A non-square matrix (such as a single row or single column) can have its remaining rows or columns fully exhausted partway through processing a single ring, so without re-checking after each side, the algorithm would incorrectly attempt to traverse a side that has already been fully covered by an earlier side in that same ring, resulting in duplicated elements.

**Q:** Why does the boundary-shrinking approach not need an explicit `visited` matrix to avoid revisiting cells, unlike the direction-vector simulation approach?
**A:** The four boundary variables, combined with the careful ordering and re-checking of the four sides, inherently guarantee that the region strictly inside the current boundaries has never been visited and the region strictly outside has already been fully visited, so this invariant alone is sufficient to prevent any cell from being processed more than once, without needing separate tracking.

**Q:** What specifically would go wrong if a boundary variable (like `top`) were incremented before its corresponding side's traversal loop had fully completed?
**A:** The loop would end up reading from an already-shrunk boundary partway through its own execution, potentially skipping cells it should have covered or causing inconsistent, incorrect behavior in the remaining iterations of that same side's traversal.

**Q:** Why does a single-row matrix specifically require the algorithm's boundary re-checks to prevent incorrect output, and what would the output look like without them?
**A:** After the top-row pass covers the entire single row, the boundaries indicate no rows remain (`top` exceeds `bottom`), but without the explicit re-checks, the algorithm might still attempt the bottom-row and left-column passes, which would incorrectly re-add some or all of the same row's elements a second time, producing a result longer than the actual number of matrix elements.

**Q:** How does the boundary-shrinking algorithm's behavior generalize uniformly across matrices of very different shapes (square, wide, tall, single row, single column) without needing shape-specific special-casing?
**A:** The same core logic — traverse each valid side of the current ring, shrink the corresponding boundary, and re-check validity before the third and fourth sides — correctly handles every possible matrix shape simply as a consequence of how the boundary variables naturally converge, without requiring the algorithm to explicitly detect or branch on the matrix's specific dimensions.

## 13. Final Code

```java
public List<Integer> spiralOrder(int[][] matrix) {
    List<Integer> result = new ArrayList<>();
    int top = 0, bottom = matrix.length - 1;
    int left = 0, right = matrix[0].length - 1;

    while (top <= bottom && left <= right) {
        for (int col = left; col <= right; col++) {
            result.add(matrix[top][col]);
        }
        top++;

        for (int row = top; row <= bottom; row++) {
            result.add(matrix[row][right]);
        }
        right--;

        if (top <= bottom) {
            for (int col = right; col >= left; col--) {
                result.add(matrix[bottom][col]);
            }
            bottom--;
        }

        if (left <= right) {
            for (int row = bottom; row >= top; row--) {
                result.add(matrix[row][left]);
            }
            left++;
        }
    }

    return result;
}
```

## 14. Self-Test

You have an `m x n` matrix. Return all its elements in spiral order (right across the top, down the right side, left across the bottom, up the left side, repeating inward). Think about why tracking four shrinking boundaries, and re-validating them between the third and fourth sides of each ring, correctly handles matrices of any shape without ever revisiting a cell.
