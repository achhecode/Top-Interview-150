---
problem: 228 - Summary Ranges
url: https://leetcode.com/problems/summary-ranges/
difficulty: Easy
section: Array / Single Pass
patterns: [single-pass, range-grouping]
data_structures: [array, string]
time: O(n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** A sorted array `nums` of unique integers.

**Required:** Return the smallest list of ranges that exactly cover every number in `nums` — each range formatted as `"a->b"` if it spans more than one value, or just `"a"` if it's a single value.

**Constraints that actually matter:**
- `0 <= nums.length <= 20` — tiny; performance is a complete non-issue, this is purely about correct range-grouping and formatting logic.
- **`nums` is sorted and contains only unique values** — this is essential: sortedness plus uniqueness means consecutive elements in the array are either exactly 1 apart (continuing the current range) or have a gap (ending the current range and starting a new one) — there's no need to sort or deduplicate, and no ambiguity about whether two adjacent array elements belong in the same range.
- **The empty array is a valid input** (`nums.length` can be 0) — must correctly return an empty list, not error.
- `nums[i]` can be as extreme as `-2^31` or `2^31 - 1` (the full `int` range) — this is a subtle but important detail: checking "is the next number exactly one more than the current number" via `nums[i] + 1 == nums[i+1]` risks **integer overflow** if `nums[i]` happens to be `Integer.MAX_VALUE`, since `Integer.MAX_VALUE + 1` wraps around to `Integer.MIN_VALUE` in Java's `int` arithmetic — this must be handled carefully (e.g., by comparing using `long` arithmetic, or by rewriting the comparison to avoid the addition entirely).

## 2. Recognition Signals

- "Sorted array, group consecutive runs into ranges" → the classic **single-pass range-grouping** technique: track the start of the current run, scan forward extending the run as long as consecutive values continue, and finalize (format and emit) the range the moment a gap or the end of the array is reached.
- Since the array is both sorted and contains unique values, "consecutive" has an exact, simple test: `nums[i+1] == nums[i] + 1`. Recognizing that sortedness plus uniqueness collapses "is this part of the same run" into this single, precise arithmetic check (rather than needing a more general grouping or comparison mechanism) is the main insight of this problem.
- The specific mention of extreme `int` values in the constraints (`-2^31` to `2^31-1`) is itself a signal to be wary of potential overflow when performing the "next value" check — recognizing this risk before it causes a bug is a mark of careful, experienced problem-solving.

**Pattern:** Single-Pass Range Grouping (scan the sorted array once, tracking the start of the current consecutive run, and extend or close out that run based on a simple "is the next value exactly one more" check).

## 3. Core Idea

Since `nums` is sorted and every value is unique, walk through the array once, keeping track of `rangeStart`, the first value of the current consecutive run. For each position, check whether the *next* value in the array continues the run (i.e., equals the current value plus 1). If it does, the run continues, so just move forward. If it doesn't (either because there's a gap to the next value, or because this is the last element of the array), the current run has ended: format it as `"a->b"` (if `rangeStart` differs from the current end value) or just `"a"` (if the run was only a single element), add it to the result, and start a new run beginning at the next element.

**Invariant:** At any point during the scan, `rangeStart` correctly holds the first value of whatever consecutive run of values is currently "in progress," and every previously completed run (up to but not including the current one) has already been correctly formatted and added to the result list — so when the scan finishes, every element of `nums` has been assigned to exactly one correctly-formatted range in the output.

## 4. Approach 1 — Two-Pointer Range Boundaries (Explicit Inner Scan)

**Logic:** Use an outer index `i` marking the start of each new range, and an inner pointer `j` that scans forward from `i` as long as the sequence remains consecutive; once `j` can't extend further, format the range from `i` to `j`, add it, and restart the outer index at `j + 1`.

```java
public List<String> summaryRanges(int[] nums) {
    List<String> result = new ArrayList<>();
    int n = nums.length;
    int i = 0;

    while (i < n) {
        int j = i;
        while (j + 1 < n && (long) nums[j + 1] == (long) nums[j] + 1) {
            j++;
        }

        if (i == j) {
            result.add(String.valueOf(nums[i]));
        } else {
            result.add(nums[i] + "->" + nums[j]);
        }

        i = j + 1;
    }

    return result;
}
```

- **Time:** O(n) — the inner `j` pointer, across the entire run of the algorithm, advances at most `n` times total (it never resets backward), so combined with the outer loop, the total work remains linear.
- **Space:** O(1) auxiliary beyond the output list, which is required regardless of approach.

**Why it is presented as a valid but slightly more verbose alternative:** This two-pointer formulation is fully correct and O(n), same as the more streamlined single-pass version below — the distinction is purely stylistic: using an explicit inner `while` loop to find each range's end is a very natural, easy-to-verify-correct way to think about the problem, though it can be expressed slightly more concisely with a single loop and an explicit `rangeStart` tracker instead of two separate index variables.

## 5. Approach 2 — Optimized/Cleaner (Single Pass with `rangeStart` Tracking)

**Algorithm:**
1. If `nums` is empty, return an empty list immediately.
2. Initialize `rangeStart = nums[0]`.
3. For each index `i` from `0` to `n - 1`:
   - If `i` is the last index, or the next element does **not** continue the run (`nums[i+1] != nums[i] + 1`, checked carefully to avoid overflow), the current run ends at `nums[i]`: format the range from `rangeStart` to `nums[i]` (using `"a"` if they're equal, `"a->b"` otherwise), add it to the result, and if there's a next element, set `rangeStart = nums[i+1]` for the next run.
4. Return the result list.

```java
public List<String> summaryRanges(int[] nums) {
    List<String> result = new ArrayList<>();
    int n = nums.length;
    if (n == 0) {
        return result;
    }

    int rangeStart = nums[0];

    for (int i = 0; i < n; i++) {
        boolean isRangeEnd = (i == n - 1) || ((long) nums[i + 1] != (long) nums[i] + 1);

        if (isRangeEnd) {
            if (rangeStart == nums[i]) {
                result.add(String.valueOf(rangeStart));
            } else {
                result.add(rangeStart + "->" + nums[i]);
            }

            if (i < n - 1) {
                rangeStart = nums[i + 1];
            }
        }
    }

    return result;
}
```

- **Time:** O(n) — a single pass through the array, with O(1) work per element (a comparison, and occasionally a string-formatting operation).
- **Space:** O(1) auxiliary beyond the output list.

**Why this is optimal:** Every element must be examined at least once to determine which range it belongs to, so O(n) is a hard lower bound, and both this approach and Approach 1 achieve it identically — there's no asymptotic distinction between them. This version is presented as the "cleaner" one mainly because it uses a single loop and a single tracked variable (`rangeStart`) rather than two separate index pointers, which some find easier to reason about, though both are equally valid, correct, O(n) solutions to internalize.

## 6. Dry Run

Example: `nums = [0,1,2,4,5,7]`.
Chosen because it's the canonical example, and it clearly shows two multi-element ranges and one single-element range, exercising both output formats.

`n=6`. Initial: `rangeStart = 0`.

| i | nums[i] | Last index or gap to next? | isRangeEnd | Action | rangeStart after |
|---|---------|-----------------------------------|-----------------|--------|------------------------|
| 0 | 0 | nums[1]=1=nums[0]+1, no gap | false | — | 0 |
| 1 | 1 | nums[2]=2=nums[1]+1, no gap | false | — | 0 |
| 2 | 2 | nums[3]=4≠nums[2]+1=3, gap! | true | rangeStart(0)≠nums[2](2) → add "0->2"; rangeStart=nums[3]=4 | 4 |
| 3 | 4 | nums[4]=5=nums[3]+1, no gap | false | — | 4 |
| 4 | 5 | nums[5]=7≠nums[4]+1=6, gap! | true | rangeStart(4)≠nums[4](5) → add "4->5"; rangeStart=nums[5]=7 | 7 |
| 5 | 7 | last index (i=n-1=5) | true | rangeStart(7)==nums[5](7) → add "7" | (no next) |

Exit condition: loop completes after `i=5`.

**Final answer:** `["0->2", "4->5", "7"]`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Two-pointer explicit inner scan | O(n) | O(1) (excl. output) | Correct; two index variables, very explicit about finding each range's end |
| 2. Single pass, rangeStart tracking | O(n) | O(1) (excl. output) | Correct and slightly more streamlined; one tracked variable instead of two indices |

Output space (the result list) is required by both approaches and isn't counted as "extra" auxiliary space.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Empty array (`nums.length == 0`) | Must return an empty list, not error on `nums[0]` access | Explicit `if (n == 0) return result;` guard handles this before any array access is attempted |
| Single-element array | Should produce exactly one single-value range (format "a", not "a->a") | With `n=1`, `rangeStart = nums[0]`, and the loop's single iteration (`i=0`) immediately finds `isRangeEnd = true` (since `i == n-1`), and `rangeStart == nums[0]`, correctly producing just `"a"` |
| All elements consecutive (one single range covering the whole array) | Should produce exactly one range spanning the entire array | The gap-check condition never triggers `isRangeEnd = true` until the very last index (`i == n-1`), at which point the single accumulated range from `rangeStart` to `nums[n-1]` is correctly emitted as one entry |
| No elements consecutive (every element is its own range) | Should produce `n` separate single-value ranges | Every single index triggers `isRangeEnd = true` (since every `nums[i+1] != nums[i]+1`), so each element is immediately closed out as its own range, correctly producing `n` entries |
| `nums[i] = Integer.MAX_VALUE` present, potentially followed by another value (though given uniqueness and the array being sorted ascending, nothing could actually follow `Integer.MAX_VALUE` in a valid sorted array, but the check must still not silently misbehave if this value appears as the last element or is being compared) | `nums[i] + 1` on `Integer.MAX_VALUE` overflows to `Integer.MIN_VALUE` in plain `int` arithmetic, which could cause the gap-check to incorrectly believe a very different next value (like `Integer.MIN_VALUE` itself, if it somehow appeared, though sortedness/uniqueness would prevent that specific scenario) is actually consecutive | Casting to `(long)` before performing the `+1` addition and comparison (`(long) nums[i] + 1`) avoids the overflow entirely, since `long` has enough range to represent `Integer.MAX_VALUE + 1` correctly without wraparound |
| Negative numbers, including very negative values near `Integer.MIN_VALUE` | Must correctly identify consecutive runs even when values are negative | The `long`-based comparison works identically regardless of sign, correctly identifying runs like `[-3,-2,-1,0,1]` as one consecutive range |

## 9. Java Notes

- **Casting to `long` before the `+1` comparison (`(long) nums[i] + 1 == (long) nums[i+1]`, or equivalently comparing `(long) nums[i+1] != (long) nums[i] + 1`):** this is the essential fix for the overflow risk discussed above — performing the addition in `long` arithmetic instead of `int` ensures `Integer.MAX_VALUE + 1` is correctly represented as `2147483648L` rather than wrapping around to `Integer.MIN_VALUE`.
- **String concatenation for range formatting (`rangeStart + "->" + nums[i]`):** since this only happens a bounded number of times (at most `n` times, once per emitted range) rather than in a tight inner loop, plain `String` concatenation here is perfectly acceptable and doesn't warrant the `StringBuilder` optimization that would matter in a hot loop.
- **`String.valueOf(rangeStart)` for the single-value case:** explicitly converts the `int` to its `String` representation for the "a" (no arrow) format, distinct from the two-value "a->b" format.
- **No general overflow risk beyond the specific `+1` comparison:** aside from that one comparison, no other arithmetic in this problem risks overflow, since values are only ever compared or directly formatted into strings, never summed or otherwise combined numerically.

## 10. Common Mistakes

- **Performing the "is this consecutive" check as plain `int` arithmetic (`nums[i] + 1 == nums[i+1]`) without considering the overflow risk when `nums[i]` is `Integer.MAX_VALUE`.** While in a strictly sorted, unique array `Integer.MAX_VALUE` could only ever be the very last element (nothing could validly follow it), the check itself, if written carelessly, could still technically compute an overflowed value during the comparison depending on exact code structure, or the habit of not considering this could carry over to less constrained variations of similar problems. Fix: always perform this kind of "next value" arithmetic using `long` (or an equivalent overflow-safe technique) when the input range includes the extremes of `int`.
- **Forgetting to handle the empty array case explicitly, causing an `ArrayIndexOutOfBoundsException` on `nums[0]`.** Fix: always check for and handle `nums.length == 0` as an explicit early return before accessing any array elements.
- **Using `"a-a"` or some other incorrect format for single-value ranges, instead of just `"a"`.** The problem explicitly specifies two distinct output formats depending on whether the range spans more than one value. Fix: always check whether the range's start and end values are equal, and format accordingly using the two distinct required formats.
- **Off-by-one errors in determining exactly when a run has ended**, e.g., checking `nums[i] != nums[i-1] + 1` (looking backward) inconsistently with the rest of the loop's indexing, rather than consistently looking forward (`nums[i+1] != nums[i] + 1`) or using a clearly defined two-pointer boundary. Fix: pick one consistent direction (typically looking forward to decide if the *current* run continues) and apply it uniformly throughout the loop.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Sorted, unique array, group into consecutive ranges → single pass tracking the start of the current run, closing it out (and formatting) whenever the next value isn't exactly one more, watching for potential overflow in the 'plus one' check."
- **90-second explanation:** "Since the array is sorted and every value is unique, I know two adjacent elements belong to the same consecutive range exactly when the second one equals the first plus one — there's no ambiguity to resolve. I scan through the array once, remembering the start of whatever range is currently in progress. Whenever I reach a point where the next element doesn't continue that pattern, either because there's a genuine gap or because I've reached the end of the array, I know the current range has just ended, so I format it — using just the single number if the range started and ended at the same value, or the 'a to b' arrow format otherwise — and add it to my results, then start tracking a new range from the next element. One subtlety worth calling out: since the values can reach the extremes of the 32-bit integer range, checking 'is this number one more than the previous' by literally adding 1 risks silent integer overflow if the previous number happens to be the maximum possible int value, so I do that specific comparison using long arithmetic to sidestep that risk entirely."
- **Related problems using this pattern:**
  - LeetCode 163 — Missing Ranges (a close variant, finding the *gaps* rather than the covered ranges)
  - LeetCode 56 — Merge Intervals (a related but distinct interval-grouping problem, working with given ranges rather than individual sorted values)
  - LeetCode 57 — Insert Interval (also interval-based, different specific mechanics)

## 12. Recall Questions

**Q:** Why does the combination of sortedness and uniqueness in `nums` make it possible to determine "is this part of the same consecutive range" using such a simple check?
**A:** Since the array is sorted and every value is distinct, two adjacent array elements are guaranteed to either differ by exactly one (continuing the same range) or differ by more than one (indicating a genuine gap), with no possibility of ties, duplicates, or out-of-order values complicating that determination.

**Q:** Why does checking `nums[i] + 1 == nums[i+1]` using plain `int` arithmetic risk a subtle bug, and how is this avoided?
**A:** If `nums[i]` happens to equal `Integer.MAX_VALUE`, adding 1 to it in plain `int` arithmetic overflows and wraps around to `Integer.MIN_VALUE`, potentially producing an incorrect comparison result; this is avoided by performing the addition and comparison using `long` arithmetic instead, which has enough range to represent the result correctly without wrapping.

**Q:** Why must the empty array case be handled as an explicit, separate check before the main loop begins?
**A:** The algorithm's initialization step needs to read `nums[0]` to set the initial `rangeStart`, which would throw an out-of-bounds exception on a genuinely empty array, so this case must be detected and handled (by returning an empty result immediately) before any such access is attempted.

**Q:** Why does a range get formatted differently depending on whether it spans a single value versus multiple values?
**A:** The problem explicitly defines two distinct required output formats — just the number itself when a range covers only one value, since there's no meaningful 'start to end' span to express, versus the 'a arrow b' format when the range genuinely spans from a distinct start value to a distinct end value.

**Q:** How would this algorithm's logic need to change, if at all, if the input array were sorted but *not* guaranteed to contain only unique values?
**A:** The simple check of "next value equals current value plus one" would no longer correctly capture all valid groupings on its own, since duplicate values would need to be explicitly handled (likely by first deduplicating, or by adjusting the continuation check to also treat exact duplicates as still being part of the same value point rather than a new range), since the problem's given guarantee of uniqueness is precisely what makes the straightforward "plus one" check sufficient.

## 13. Final Code

```java
public List<String> summaryRanges(int[] nums) {
    List<String> result = new ArrayList<>();
    int n = nums.length;
    if (n == 0) {
        return result;
    }

    int rangeStart = nums[0];

    for (int i = 0; i < n; i++) {
        boolean isRangeEnd = (i == n - 1) || ((long) nums[i + 1] != (long) nums[i] + 1);

        if (isRangeEnd) {
            if (rangeStart == nums[i]) {
                result.add(String.valueOf(rangeStart));
            } else {
                result.add(rangeStart + "->" + nums[i]);
            }

            if (i < n - 1) {
                rangeStart = nums[i + 1];
            }
        }
    }

    return result;
}
```

## 14. Self-Test

You have a sorted, unique integer array `nums`. Return the smallest list of ranges that exactly cover every value, formatted as "a->b" for multi-value ranges or "a" for single values. Think about why sortedness plus uniqueness makes "is this the same range" a simple plus-one check, and why that specific check needs to be overflow-safe given the full int value range allowed.
