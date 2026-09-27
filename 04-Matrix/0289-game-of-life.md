---
problem: 289 - Game of Life
url: https://leetcode.com/problems/game-of-life/
difficulty: Medium
section: Array / In-Place Manipulation
patterns: [state-encoding, simultaneous-update-simulation]
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

**Given:** An `m x n` board where each cell is live (`1`) or dead (`0`), following Conway's Game of Life rules based on each cell's 8 neighbors (horizontal, vertical, diagonal).

**Required:** Update the board to its next state, applying all four rules **simultaneously** to every cell — meaning every cell's next state must be computed using the *current* (pre-update) states of its neighbors, never a neighbor's already-updated new state.

**Constraints that actually matter:**
- `1 <= m, n <= 25` — tiny; performance is a non-issue — the entire challenge is correctly handling the **simultaneous update** requirement, not algorithmic efficiency.
- **"Births and deaths occur simultaneously"** — this is the central difficulty: if cells were updated one at a time, in-place, in a simple left-to-right, top-to-bottom scan, then by the time a later cell computes its live-neighbor count, some of its neighbors (the ones already processed) would already reflect their *next* state rather than their *current* state, corrupting the count and producing a wrong result. The whole problem is about correctly simulating "everyone updates at once" using a strictly sequential scan.
- **Cell values are only ever 0 or 1** — this narrow value range is exactly what enables the classic space-optimized trick: encoding *both* the current and the next state into the same integer cell using extra bits, since only 2 bits of information are ever needed per cell, well within a single `int`.
- The explicit **follow-up** about in-place, O(1) space, and about handling an "infinite" board are both hints toward the intended deeper technique(s): first, that in-place simultaneous update is achievable via state-encoding (not by using an auxiliary board copy); second, an awareness that a truly infinite board would require a different, sparse-representation approach (only tracking live cells) rather than a fixed-size 2D array.

## 2. Recognition Signals

- "Update every cell simultaneously, in-place, based on current neighbor states" → the specific signature of the **state-encoding trick**: since the problem's own constraints restrict cell values to a tiny, fixed set (0 or 1), you can temporarily overload extra bits within each cell to record *both* its current state and its computed next state at once, decoding back to the true next state in a final cleanup pass.
- Recognizing the **core obstacle** — that naively overwriting a cell with its new value during the same scan would corrupt later cells' neighbor-counting (since they'd read the new value instead of the old one) — is itself the signal that a direct one-pass in-place update won't work without some additional trick to preserve the "old" information alongside the "new."
- Whenever a problem needs to compute a new state for every element based on the *current* state of its neighbors, and updating in-place naively would let already-updated neighbors bleed into later computations — either use an auxiliary copy (simple but O(n) extra space) or, when the value range is small enough, encode both old and new states into extra bits of the same storage (achieving O(1) extra space).

**Pattern:** In-Place State Encoding via Extra Bits (temporarily store both the current and next state of each cell using additional encoding values beyond the original 0/1 range, then decode back to just the next state in a final pass) — with a Simple Auxiliary Board Copy as the more straightforward, higher-space alternative.

## 3. Core Idea

Since each cell's value is only ever 0 or 1, and Java's `int` has plenty of unused bit capacity, temporarily use a small set of encoded values during the update process to represent **both** the cell's original state and its newly computed next state simultaneously:
- `0` → was dead, stays dead
- `1` → was live, stays live
- `2` → was live, becomes dead
- `3` → was dead, becomes live

During the main scan, for each cell, count live neighbors by checking if a neighbor's *encoded* value is `1` or `2` (both of these mean the neighbor was **originally** live, regardless of what it's transitioning to) — this is the key trick that lets the algorithm always recover each neighbor's *original* state even after that neighbor has already been "updated" (re-encoded) earlier in the same scan. Based on the live-neighbor count and the current cell's original state (`encodedValue % 2`, since `0` and `2` were originally dead/live... actually originally-live values are `1` and `2`, originally-dead are `0` and `3` — need to define encoding precisely and consistently), apply the four rules to determine the encoded next state. After the full scan completes, do one final pass converting every encoded value to just its "new state" bit (e.g., `board[i][j] >>= 1` if the encoding is chosen so the new state occupies a bit that a right-shift extracts cleanly, or simpler: `board[i][j] %= 2` extracts the original bit, so instead choose the encoding so that dividing by 2, `board[i][j] > 1`, or similar cleanly yields the *new* state).

**Invariant:** At any point during the main scan, for any cell not yet visited, its encoded value still directly reflects its true original state (0 or 1) unchanged; for any cell already visited, its encoded value simultaneously preserves enough information (via the specific 0/1/2/3 encoding) to still determine its *original* state (via `value == 1 || value == 2` for "was live") while also recording its *new* state for the final decoding pass — this dual-purpose encoding is what allows a single sequential in-place scan to correctly implement a simultaneous update.

## 4. Approach 1 — Auxiliary Board Copy (Simple, O(m×n) Space)

**Logic:** Create a full copy of the original board. Compute each cell's next state by counting live neighbors from the *copy* (which never changes during the scan), and write the result into the *original* board.

```java
public void gameOfLife(int[][] board) {
    int m = board.length;
    int n = board[0].length;
    int[][] original = new int[m][n];
    for (int i = 0; i < m; i++) {
        original[i] = board[i].clone();
    }

    int[] dRow = {-1, -1, -1, 0, 0, 1, 1, 1};
    int[] dCol = {-1, 0, 1, -1, 1, -1, 0, 1};

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            int liveNeighbors = 0;
            for (int d = 0; d < 8; d++) {
                int ni = i + dRow[d];
                int nj = j + dCol[d];
                if (ni >= 0 && ni < m && nj >= 0 && nj < n && original[ni][nj] == 1) {
                    liveNeighbors++;
                }
            }

            if (original[i][j] == 1) {
                board[i][j] = (liveNeighbors == 2 || liveNeighbors == 3) ? 1 : 0;
            } else {
                board[i][j] = (liveNeighbors == 3) ? 1 : 0;
            }
        }
    }
}
```

- **Time:** O(m × n) — each cell is visited once, with a bounded (at most 8) neighbor check per cell.
- **Space:** O(m × n) auxiliary for the `original` board copy.

**Why it is not optimal:** This is simple and directly satisfies the "simultaneous update" requirement by construction (since `original` never changes during the scan), but it uses O(m×n) extra space, which the problem's explicit follow-up specifically challenges the solver to eliminate via an in-place technique instead.

## 5. Approach 2 — Optimized (In-Place State Encoding, O(1) Space)

**Algorithm:**
1. Define an encoding: `0` = was dead, stays dead; `1` = was live, stays live; `2` = was live, becomes dead; `3` = was dead, becomes live. Note that "was live" corresponds to encoded values `1` or `2` (i.e., `value == 1 || value == 2`), and "becomes live" (the true next state) corresponds to encoded values `1` or `3`.
2. For each cell `(i, j)`, count live neighbors by checking, for each of the 8 neighbor offsets, whether that neighbor's *current* (possibly-already-encoded) value equals `1` or `2` (both mean "was originally live").
3. Apply the four rules using the cell's own *original* state (`board[i][j] == 1` before any encoding is applied to this cell) and the counted live neighbors, setting `board[i][j]` to the appropriate encoded value (`2` if it was live but should die; `3` if it was dead but should become live; otherwise leave it as its original `0` or `1`, since those already correctly represent "stays the same").
4. After the full scan, do a final pass: for each cell, extract just the new-state bit — since encoded values `1` and `3` represent "new state is live," this can be done via `board[i][j] %= 2`... but wait, `3 % 2 = 1` (correct, live) and `2 % 2 = 0` (correct, dead) and `1 % 2 = 1` (correct, live) and `0 % 2 = 0` (correct, dead) — so `board[i][j] %= 2` correctly extracts the new state for all four encoded values.

```java
public void gameOfLife(int[][] board) {
    int m = board.length;
    int n = board[0].length;

    int[] dRow = {-1, -1, -1, 0, 0, 1, 1, 1};
    int[] dCol = {-1, 0, 1, -1, 1, -1, 0, 1};

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            int liveNeighbors = 0;
            for (int d = 0; d < 8; d++) {
                int ni = i + dRow[d];
                int nj = j + dCol[d];
                if (ni >= 0 && ni < m && nj >= 0 && nj < n
                        && (board[ni][nj] == 1 || board[ni][nj] == 2)) {
                    liveNeighbors++;
                }
            }

            boolean wasLive = (board[i][j] == 1);
            if (wasLive && (liveNeighbors < 2 || liveNeighbors > 3)) {
                board[i][j] = 2; // was live, becomes dead
            } else if (!wasLive && liveNeighbors == 3) {
                board[i][j] = 3; // was dead, becomes live
            }
            // otherwise leave as-is: 0 (stays dead) or 1 (stays live)
        }
    }

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            board[i][j] %= 2;
        }
    }
}
```

- **Time:** O(m × n) — the same two full passes over the board (main update pass, final decoding pass), each visiting every cell a bounded constant number of times.
- **Space:** O(1) auxiliary — only the small fixed-size neighbor-offset arrays and a few loop variables; no auxiliary board of any size is allocated.

**Why this is optimal:** Every cell must be examined (and every one of its up to 8 neighbors checked) at least once to determine its next state, so O(m×n) time is a hard lower bound, matching what both approaches achieve. Space is O(1) because the narrow 0/1 value range leaves enough "room" within a standard `int` to encode both the original and new state simultaneously using just four distinct values, entirely eliminating the need for any auxiliary board copy — this directly answers the follow-up's explicit in-place, constant-space challenge.

## 6. Dry Run

Example: `board = [[1,1],[1,0]]`.
Chosen because it's the second canonical example, small enough to trace every cell's neighbor count directly, and it results in a cell transitioning from dead to live (birth), exercising the `3` encoding.

`m=2, n=2`.

**Main pass — process each cell in row-major order:**

**Cell (0,0), original value 1 (live):** Neighbors within bounds: (0,1)=1, (1,0)=1, (1,1)=0. Live count (checking for value 1 or 2): (0,1)=1→live, (1,0)=1→live, (1,1)=0→not live. `liveNeighbors=2`. `wasLive=true`, `2` is within `[2,3]` → stays live (condition `liveNeighbors<2 || >3` is false) → no change, stays `1`.

**Cell (0,1), original value 1 (live):** Neighbors: (0,0)=1(still original, not yet re-encoded this pass since we just left it as 1), (1,0)=1, (1,1)=0. `liveNeighbors=2`. `wasLive=true`, stays live → stays `1`.

**Cell (1,0), original value 1 (live):** Neighbors: (0,0)=1, (0,1)=1, (1,1)=0. `liveNeighbors=2`. `wasLive=true`, stays live → stays `1`.

**Cell (1,1), original value 0 (dead):** Neighbors: (0,0)=1, (0,1)=1, (1,0)=1. `liveNeighbors=3`. `wasLive=false`, `liveNeighbors==3` → becomes live → encoded as `3`.

After main pass: `board = [[1,1],[1,3]]`.

**Final decoding pass** (`%= 2` on every cell): `1%2=1`, `1%2=1`, `1%2=1`, `3%2=1`.

Exit condition: both passes complete.

**Final answer:** `board = [[1,1],[1,1]]`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Auxiliary board copy | O(m×n) | O(m×n) | Correct and simple, but doesn't meet the in-place follow-up |
| 2. In-place state encoding | O(m×n) | O(1) | Optimal; directly answers the follow-up's constant-space in-place challenge |

Input/output space for `board` is O(m×n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| A cell on the edge or corner of the board (fewer than 8 actual neighbors) | Must not attempt to read out-of-bounds neighbor positions | The bounds check `ni >= 0 && ni < m && nj >= 0 && nj < n` before checking a neighbor's value correctly skips any neighbor offset that would fall outside the board, naturally handling edge and corner cells with fewer than 8 neighbors without any special-casing beyond this check |
| A neighbor cell that has already been updated (re-encoded) earlier in the same scan | Must still correctly determine that neighbor's *original* state, not be confused by its new encoded value | Checking for `board[ni][nj] == 1 || board[ni][nj] == 2` specifically captures "was originally live" regardless of whether that neighbor has already been re-encoded to `2` (live→dead) in this same pass — this is precisely the mechanism that makes the simultaneous-update illusion work correctly |
| Single-cell board (`m=1, n=1`) | No neighbors exist at all; the cell's next state depends only on its own current state and zero live neighbors | The neighbor-checking loop finds no valid in-bounds neighbors (since the only possible neighbor offsets would fall outside the 1x1 board), so `liveNeighbors` remains 0, correctly triggering "dies from under-population" if the cell was live, or "stays dead" if it was already dead (0 is not 3, so no birth) |
| All cells live, or all cells dead | Should correctly compute the next state uniformly across such a homogeneous board | The algorithm's logic doesn't depend on any variation between cells — it processes each cell independently based purely on its own original state and its neighbors' original states, so a fully uniform board is handled correctly by the same general logic, with no special-casing needed |
| A board where the final decoding pass (`%= 2`) must correctly extract the new state from all four possible encoded values | Must verify the chosen encoding scheme decodes correctly and consistently for every case | As verified in the algorithm description, `0%2=0`, `1%2=1`, `2%2=0`, `3%2=1` — each of the four encoded values correctly reduces to its intended new-state bit (0 or 1) under this specific modulo operation, confirming the encoding scheme's internal consistency |
| Follow-up: an "infinite" board where live cells could reach the current array's border | Conceptually, if live cells are near the edge of the fixed-size array, the "next generation" could theoretically need to expand beyond the current array's bounds, which a fixed-size 2D array can't represent | Not directly handled by the given fixed-size-array solution — this follow-up point is explicitly a discussion/design question rather than something the core algorithm needs to solve; a full answer would involve a different data representation entirely (e.g., a sparse set storing only the coordinates of live cells, allowing the "board" to conceptually extend infinitely in any direction) — see the Interview Takeaway section for how to discuss this |

## 9. Java Notes

- **Bit/value encoding via extra integer states (0, 1, 2, 3) instead of a full auxiliary array:** this is the crux Java-agnostic (but here Java-implemented) technique of this problem — recognizing that a narrow value range (just 0/1) leaves ample room within a standard `int` to temporarily encode extra information without needing separate storage.
- **`board[i][j] %= 2` for final decoding:** a compact way to extract just the "new state" bit from the four possible encoded values, relying on the specific encoding scheme chosen (values 1 and 3 both being odd, correctly representing "new state is live") — an alternative encoding scheme might instead use a right-shift (`>>= 1`) if the bits were arranged differently; the specific decoding operation must match whatever encoding scheme was chosen.
- **Neighbor offset arrays (`dRow`, `dCol`):** a concise, reusable way to enumerate all 8 neighbor directions without writing out 8 separate manual bounds-checked lookups — a common idiom for grid/matrix neighbor-traversal problems in general.
- **No overflow or numeric risk:** all values involved (0, 1, 2, 3, and small neighbor counts up to 8) are tiny and well within any numeric range concerns; no arithmetic here risks overflow.

## 10. Common Mistakes

- **Directly overwriting a cell with its final new value (0 or 1) during the main scan, rather than using an intermediate encoded value.** This would corrupt the "original state" information needed by not-yet-processed cells when they check this cell as one of their neighbors, since a cell that "died" would look identical (as a plain `0`) to a cell that started dead and never had any bearing on the count — the whole point of the encoding is to distinguish "originally live, now dead" (`2`) from "originally dead, stays dead" (`0`). Fix: always use the four-value encoding during the main pass, decoding to the true final value only in a separate, final pass.
- **Checking for `board[ni][nj] == 1` alone (forgetting to also check for `2`) when counting live neighbors.** Since a neighbor that was originally live but has already transitioned to "dies" during this same scan is encoded as `2`, not `1`, forgetting to include `2` in the "was live" check would undercount live neighbors for any cell processed after such a neighbor. Fix: always check for *both* `1` and `2` when determining whether a neighbor was originally live.
- **Performing the final decoding pass (`%= 2`) interleaved with the main update pass, rather than as a strictly separate pass afterward.** Decoding a cell to its final value too early would destroy the "was originally live" information (specifically, distinguishing encoded `2` from plain `0`) that later cells in the same main pass still need to correctly count that cell as a neighbor. Fix: always complete the entire main encoding pass over the whole board first, and only then perform a separate, final decoding pass.
- **Getting the encoding scheme's four values or their meanings mixed up**, e.g., accidentally swapping which value means "was live, becomes dead" versus "was dead, becomes live," or choosing an encoding where the final decoding operation doesn't cleanly and correctly extract the new state. Fix: carefully define and double-check the encoding scheme (which values mean what) before implementing, and verify the final decoding operation correctly reduces every encoded value to its intended new-state bit.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Update every cell simultaneously based on current neighbor states, in-place, O(1) space → encode both the original and new state into extra values (since cell values are only 0 or 1), then decode to just the new state in a final pass."
- **90-second explanation:** "The core challenge here is that I need every cell's next state to be based on everyone's *current* state, but if I update cells one at a time in a normal scan, later cells would end up reading already-updated neighbors instead of their original values, corrupting the simulation. Since cell values are only ever 0 or 1, I have room to temporarily use two extra encoded values — I use 2 to mean 'this cell was originally live but is now dying,' and 3 to mean 'this cell was originally dead but is now being born.' When counting a neighbor's live status during the main scan, I check for either the original live value (1) or the now-dying encoded value (2), since both of those mean the neighbor was originally live, regardless of whether it's already been processed and re-encoded. After I've gone through and encoded every cell's transition this way, I do one final, separate pass over the whole board to convert each encoded value down to just its true new state — which conveniently, with this specific encoding, is just each value modulo 2. This lets me solve the whole problem in-place with O(1) extra space, fully satisfying the simultaneous-update requirement without ever needing a full auxiliary copy of the board."
- **Discussing the infinite-board follow-up:** "If the board were truly infinite and live cells could reach the edges of whatever fixed-size array I'm using, I'd need a fundamentally different representation — instead of a dense 2D array, I'd store only the *coordinates* of live cells in a set. To compute the next generation, I'd only need to examine cells that are either currently live or are neighbors of a currently live cell (since a cell with zero live neighbors and that's currently dead can never be born), which keeps the computation bounded by the number of live cells rather than the size of a fixed grid, and naturally allows the 'active area' to grow in any direction without ever running into a hard array boundary."
- **Related problems using this pattern:**
  - LeetCode 73 — Set Matrix Zeroes (different specific technique, but shares the same "don't corrupt data you're still reading" in-place mutation challenge)
  - LeetCode 130 — Surrounded Regions (different technique, but similarly a grid-based simulation with careful in-place state tracking)

## 12. Recall Questions

**Q:** Why can't cells simply be updated to their final new value (0 or 1) directly during the main scan, one at a time?
**A:** Later cells in the scan need to know the *original* state of their neighbors to correctly count live neighbors, but if an earlier-processed neighbor has already been overwritten with its final new value, that original information is lost, and the count would incorrectly reflect a mix of old and new states rather than a true snapshot of everyone's original state.

**Q:** Why must a neighbor's "was originally live" status be checked using *two* encoded values (1 and 2), not just the original live value (1) alone?
**A:** A neighbor that was originally live but has already been processed and determined to be dying in this same scan is encoded as 2, not 1, so checking for only 1 would incorrectly fail to count that neighbor as having been live, undercounting live neighbors for any cell that gets processed after it.

**Q:** Why does the specific encoding scheme (0, 1, 2, 3) allow such a simple final decoding step (`value % 2`)?
**A:** The encoding was deliberately chosen so that the two values representing "new state is live" (1 and 3) are both odd, and the two values representing "new state is dead" (0 and 2) are both even, so taking each value modulo 2 directly and correctly extracts just the new-state bit in every case.

**Q:** Why is it essential that the final decoding pass happens strictly after the entire main encoding pass completes, rather than being interleaved with it?
**A:** Decoding a cell to its plain final value (0 or 1) too early would erase the distinction between "originally dead, stays dead" and "originally live, now dying," both of which would otherwise become indistinguishable as plain 0s, corrupting the live-neighbor count for any not-yet-processed cells that still need to check that cell's original state.

**Q:** How would the fixed-size 2D array approach need to change to properly handle a truly infinite board, as the problem's follow-up asks about?
**A:** Rather than a dense array representing a fixed-size grid, the board would need to be represented sparsely — typically as a set of coordinates for only the currently live cells — with the next generation computed by only examining live cells and their immediate neighbors, allowing the conceptually infinite board's active region to grow in any direction without being constrained by a predetermined array size.

## 13. Final Code

```java
public void gameOfLife(int[][] board) {
    int m = board.length;
    int n = board[0].length;

    int[] dRow = {-1, -1, -1, 0, 0, 1, 1, 1};
    int[] dCol = {-1, 0, 1, -1, 1, -1, 0, 1};

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            int liveNeighbors = 0;
            for (int d = 0; d < 8; d++) {
                int ni = i + dRow[d];
                int nj = j + dCol[d];
                if (ni >= 0 && ni < m && nj >= 0 && nj < n
                        && (board[ni][nj] == 1 || board[ni][nj] == 2)) {
                    liveNeighbors++;
                }
            }

            boolean wasLive = (board[i][j] == 1);
            if (wasLive && (liveNeighbors < 2 || liveNeighbors > 3)) {
                board[i][j] = 2; // was live, becomes dead
            } else if (!wasLive && liveNeighbors == 3) {
                board[i][j] = 3; // was dead, becomes live
            }
        }
    }

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            board[i][j] %= 2;
        }
    }
}
```

## 14. Self-Test

You have an `m x n` board of live/dead cells following Conway's Game of Life rules. Update the board to its next state in-place, with every cell's update based on everyone's *current* state simultaneously, using O(1) extra space. Think about why encoding both a cell's original and new state into extra values (since only 0/1 are ever "real" values) lets a single sequential scan correctly simulate a simultaneous update.
