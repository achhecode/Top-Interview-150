---
problem: 128 - Longest Consecutive Sequence
url: https://leetcode.com/problems/longest-consecutive-sequence/
difficulty: Medium
section: Array / Hash Table
patterns: [hashset, sequence-start-detection]
data_structures: [hashset]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** an unsorted array of integers `nums`.

**Required:** return the length of the longest run of consecutive integers (e.g., `[1,2,3,4]`) that appear somewhere in `nums`, regardless of their original order or position.

**Constraints that matter:**
- `0 <= nums.length <= 10^5` — the empty-array case is explicitly allowed and must return `0`; the upper bound plus the problem's own explicit demand for O(n) time rules out any sort-based O(n log n) solution as the "intended" answer, even though it would still pass within limits.
- `-10^9 <= nums[i] <= 10^9` — values fit safely in `int`; consecutive-value comparisons (`num + 1`, `num - 1`) stay well within `int` range except at the very extremes, which is worth a deliberate note even though this problem's bounds don't actually trigger overflow.
- **"You must write an algorithm that runs in O(n) time"** — this is stated directly in the problem, making it one of the few LeetCode problems where the brute force (even a fairly good O(n log n) sort) is explicitly disqualified as the target solution, not just suboptimal.

## 2. Recognition Signals

- "Longest run of consecutive integers" + "unsorted array" + "O(n) required" → this combination almost always points to a **HashSet-based sequence-start detection** technique rather than sorting.
- The key insight-enabling structure: membership testing (`does x+1 exist?`) is all that's needed, not order or position — a strong signal that a **HashSet** (O(1) average lookups) is the right tool, not a sorted structure.
- Avoiding redundant work is central: naively expanding outward from *every* number would revisit the same sequence many times; the trick of only starting a scan from numbers that are **sequence starts** (`num - 1` not present) is what makes the linear bound achievable.
- Named pattern: **HashSet Sequence-Start Expansion**.

## 3. Core Idea

Put all numbers into a `HashSet` for O(1) membership checks. For each number, only begin counting a sequence from it if it's a **true starting point** — meaning `num - 1` is not in the set. From each such start, count forward (`num + 1`, `num + 2`, ...) as long as consecutive values exist, tracking the longest run found.

**Why it's correct:** every consecutive sequence has exactly one starting number (the smallest value in that run), and checking `num - 1 not in set` is precisely the condition that identifies it. By only expanding from true starts, each number in the array is visited by the inner `while` loop **at most once** across the entire algorithm's execution — once as part of counting its sequence's length from that sequence's start, and it's never revisited from a different start (since the check that gates the outer scan prevents redundant re-scans of any given run).

**Invariant:** across the whole algorithm's execution, each distinct number in the set contributes to the inner counting loop's work at most one time total (as part of exactly one sequence's expansion), which is what bounds the total work across all outer iterations combined to O(n) rather than O(n²).

## 4. Approach 1 — Brute Force (Sort and Scan)

1. If `nums` is empty, return `0`.
2. Sort `nums`.
3. Scan through the sorted array, tracking the current run's length: increment when the next element is exactly one more than the previous (skip duplicates without breaking the run), reset to `1` when there's a gap.
4. Track the maximum run length seen.

```java
public int longestConsecutiveBruteForce(int[] nums) {
    if (nums.length == 0) {
        return 0;
    }

    Arrays.sort(nums);
    int longest = 1;
    int currentRun = 1;

    for (int i = 1; i < nums.length; i++) {
        if (nums[i] == nums[i - 1]) {
            continue; // duplicate, doesn't break or extend the run
        }
        if (nums[i] == nums[i - 1] + 1) {
            currentRun++;
        } else {
            currentRun = 1;
        }
        longest = Math.max(longest, currentRun);
    }

    return longest;
}
```

**Time:** O(n log n) — dominated by sorting; the scan itself is O(n).
**Space:** O(log n) to O(n) depending on the sort algorithm's internal stack/auxiliary usage (Java's `Arrays.sort(int[])` uses a dual-pivot quicksort, O(log n) auxiliary in typical cases).

This is not optimal per the problem's own explicit requirement: it's O(n log n), not O(n), even though at `n = 10^5` it would likely run fast enough in practice. The problem is deliberately testing whether the candidate can find the O(n) HashSet-based technique instead of defaulting to "just sort it."

## 5. Approach 2 — Optimized (HashSet, Sequence-Start Expansion)

1. If `nums` is empty, return `0`.
2. Insert every element of `nums` into a `HashSet<Integer> numSet` (this also naturally deduplicates).
3. Initialize `longest = 0`.
4. For each `num` in `numSet`:
   - If `numSet.contains(num - 1)`, skip this number — it's not a sequence start, so some earlier number will already account for (or will account for) this run.
   - Otherwise, this is a sequence start: count forward — `currentNum = num`, `currentLength = 1`; while `numSet.contains(currentNum + 1)`, increment `currentNum` and `currentLength`.
   - Update `longest = Math.max(longest, currentLength)`.
5. Return `longest`.

```java
public int longestConsecutive(int[] nums) {
    if (nums.length == 0) {
        return 0;
    }

    Set<Integer> numSet = new HashSet<>();
    for (int num : nums) {
        numSet.add(num);
    }

    int longest = 0;

    for (int num : numSet) {
        if (numSet.contains(num - 1)) {
            continue; // not a sequence start
        }

        int currentNum = num;
        int currentLength = 1;

        while (numSet.contains(currentNum + 1)) {
            currentNum++;
            currentLength++;
        }

        longest = Math.max(longest, currentLength);
    }

    return longest;
}
```

**Time:** O(n) — building the set is O(n); the outer loop iterates over at most n distinct elements, and although there's a nested `while` loop, each element is only ever consumed by the innermost expansion of its own sequence's single start, so the total work across all iterations of the inner loop, summed over the whole algorithm, is bounded by O(n).
**Space:** O(n) auxiliary for the `HashSet`.

This is optimal: the problem explicitly demands O(n), and this achieves it by ensuring no number is ever redundantly re-examined as part of more than one sequence expansion — the `num - 1` check is what guarantees this bound.

## 6. Dry Run

`nums = [100, 4, 200, 1, 3, 2]` → `numSet = {100, 4, 200, 1, 3, 2}`

| num (iteration order may vary) | numSet.contains(num-1)? | sequence start? | expansion | currentLength | longest after |
|---|---|---|---|---|---|
| 100 | no (99 absent) | yes | 100→101? no | 1 | 1 |
| 4 | yes (3 present) | no, skip | — | — | 1 |
| 200 | no (199 absent) | yes | 200→201? no | 1 | 1 |
| 1 | no (0 absent) | yes | 1→2→3→4→5? (5 absent) | 4 | 4 |
| 3 | yes (2 present) | no, skip | — | — | 4 |
| 2 | yes (1 present) | no, skip | — | — | 4 |

Exit: all 6 distinct numbers in the set have been visited by the outer loop (three were skipped as non-starts, three were expanded as starts). Final answer: `4`, matching the expected output — the sequence `[1,2,3,4]`.

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Sort and Scan | O(n log n) | O(log n) to O(n) for sort internals | Explicitly disqualified by the problem's own O(n) requirement |
| HashSet Sequence-Start Expansion | O(n) | O(n) aux | Each number contributes to at most one sequence's expansion, bounding total work |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| Empty array (`nums.length == 0`) | No elements, no sequence | Early check returns `0` immediately, avoiding any set operations |
| Single element | Trivial sequence of length 1 | The single number is always a sequence start (nothing to check against); `currentLength` stays 1 |
| All duplicate values (e.g. `[5,5,5]`) | Duplicates shouldn't inflate the sequence length | `HashSet` naturally deduplicates on insertion, so the set has just `{5}`, correctly giving a longest length of 1 |
| Values scattered with no two consecutive (e.g. `[10, 50, 90]`) | Every number is its own sequence of length 1 | Every number is a start (no `num-1` present) and no expansion succeeds; longest stays 1 |
| Negative numbers and values spanning zero (e.g. `[-1, 0, 1]`) | Consecutive-ness must work correctly across the zero boundary | Plain integer arithmetic (`num - 1`, `num + 1`) handles negative values with no special-casing |
| A run containing duplicates interspersed elsewhere (e.g. `nums = [1,0,1,2]`) | Duplicate `1` shouldn't be double-counted or break the true run `[0,1,2]` | Deduplication via `HashSet` means `numSet = {0,1,2}`; `0` is correctly identified as the sole start, expanding to length 3 |

## 9. Java Notes

- `HashSet<Integer>` autoboxes every element — for `n` up to `10^5` this is an acceptable, standard trade-off in Java, since no built-in primitive-int hash set exists without a third-party library.
- `numSet.contains(num - 1)` and `numSet.contains(currentNum + 1)` are both O(1) average-case operations, which is what makes the amortized linear-time bound achievable — this relies on `HashSet`'s hash table implementation, not on any ordering.
- Converting `nums` (an array, which can contain duplicates and preserves original order) into a `HashSet` (unique elements, no order) is a deliberate and necessary transformation — the problem's "sequence" concept only cares about which distinct values exist, not how many times or where each appears in the original array.
- The nested `while` loop inside the `for` loop might look like O(n²) at first glance, but the `numSet.contains(num - 1)` gate at the top of the outer loop is what prevents this — without it, every number would attempt its own full expansion, and the same run would be re-walked once per member, producing genuine O(n²) behavior in the worst case (e.g., one giant consecutive run).

## 10. Common Mistakes

- Omitting the `numSet.contains(num - 1)` sequence-start check and expanding forward from *every* number unconditionally — this still produces the correct final answer, but degrades the time complexity to O(n²) in the worst case (e.g., a single run `[1,2,3,...,n]` would have every element trigger a full O(n) expansion). Fix: always gate the expansion with the sequence-start check.
- Iterating over the original `nums` array instead of the deduplicated `numSet` for the outer loop — not incorrect on its own, but processes duplicate values redundantly (though the sequence-start check still limits wasted work per duplicate to O(1), so this is a minor inefficiency rather than a correctness bug). Fix: iterate over the set to avoid redundant outer-loop entries entirely.
- Trying to sort `nums` in place and then modifying the sorting-based scan to also deduplicate carefully — functionally works (as shown in Approach 1) but is explicitly not the O(n) solution the problem demands; candidates sometimes present this as if it satisfies the requirement. Fix: recognize that any comparison-based sort has an inherent O(n log n) lower bound, so sorting can never satisfy a strict O(n) requirement.
- Off-by-one in the inner while loop, e.g. checking `numSet.contains(currentNum)` instead of `numSet.contains(currentNum + 1)` — either fails to advance or double-counts the current element. Fix: always check for the *next* value in sequence before incrementing.

## 11. Interview Takeaway

- **Trigger sentence:** "Longest run of consecutive integers in an unsorted array, O(n) required → HashSet for membership, only expand forward from true sequence starts (where `num - 1` is absent)."
- **90-second explanation:** "I put all the numbers into a hash set for O(1) membership checks. For each number, I only start counting a sequence from it if it's a true starting point — meaning the number just below it isn't in the set. That check is what keeps this linear: without it, I'd redundantly re-walk the same run from every one of its members. From each genuine start, I count forward as far as consecutive numbers exist in the set, tracking the longest run found. Because every number can only ever be part of exactly one sequence's forward expansion — the one starting from its own run's minimum — the total work across all expansions, summed over the whole array, stays O(n), even though there's a while loop nested inside a for loop."
- **Related problems:** 300 (Longest Increasing Subsequence, different technique but related "longest run" framing), 128's own sort-based variant, 56 (Merge Intervals, similar reasoning about gaps and runs).

## 12. Recall Questions

**Q:** Why does checking `numSet.contains(num - 1)` before expanding matter for the algorithm's time complexity?
**A:** Without that check, every number would attempt to expand its own sequence, and for a single long consecutive run, this would cause the same run to be walked redundantly once per member, producing O(n²) total work; the check ensures only one expansion happens per distinct sequence, from its true minimum.

**Q:** Why is it correct to say each number contributes to at most one sequence's expansion across the whole algorithm?
**A:** A number that is not a sequence start is skipped entirely by the outer loop's gate; a number that is a sequence start triggers exactly one expansion, and any number visited during that expansion belongs only to that one run (since consecutive integers can't belong to two different runs simultaneously) and will never be re-expanded from elsewhere.

**Q:** Why does converting `nums` into a `HashSet` first matter, beyond just enabling O(1) lookups?
**A:** It also naturally deduplicates the input, which is important because duplicate values in the original array shouldn't be treated as extending a sequence more than once or be double-counted in the final length.

**Q:** Why is the sort-based approach explicitly disqualified here, even though it would pass within the given constraints?
**A:** Any comparison-based sorting algorithm has an inherent O(n log n) lower bound, and the problem explicitly states the algorithm must run in O(n) time, so sorting can never satisfy that stated requirement regardless of how fast it runs in practice.

**Q:** What would happen to correctness (not just performance) if the sequence-start check were removed entirely?
**A:** Correctness would actually be preserved — every number would still eventually find and report the length of its own sequence — but performance would degrade to O(n²) in the worst case, since the same consecutive run could be fully re-walked from many different starting points within it.

## 13. Final Code

```java
import java.util.*;

class Solution {
    public int longestConsecutive(int[] nums) {
        if (nums.length == 0) {
            return 0;
        }

        Set<Integer> numSet = new HashSet<>();
        for (int num : nums) {
            numSet.add(num);
        }

        int longest = 0;

        for (int num : numSet) {
            if (numSet.contains(num - 1)) {
                continue;
            }

            int currentNum = num;
            int currentLength = 1;

            while (numSet.contains(currentNum + 1)) {
                currentNum++;
                currentLength++;
            }

            longest = Math.max(longest, currentLength);
        }

        return longest;
    }
}
```

## 14. Self-Test

Given an unsorted integer array, find the length of the longest run of consecutive integers present in it, in O(n) time. Re-derive: put all values into a hash set; for each value that has no predecessor (`value - 1`) in the set, treat it as a sequence start and count forward while successors exist, tracking the maximum length found — the predecessor check is what guarantees each number is only ever expanded from as part of one sequence.
