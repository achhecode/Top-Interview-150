---
problem: 209 - Minimum Size Subarray Sum
url: https://leetcode.com/problems/minimum-size-subarray-sum/
difficulty: Medium
section: Array / Sliding Window
patterns: [sliding-window, prefix-sum-binary-search]
data_structures: [array]
time: O(n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** an array `nums` of positive integers and a positive integer `target`.

**Required:** return the length of the shortest contiguous subarray whose sum is `>= target`. If none exists, return `0`.

**Constraints that matter:**
- `1 <= nums.length <= 10^5` — an O(n²) brute force (≈10^10 in the worst case) is too slow; O(n) or O(n log n) is expected.
- `1 <= nums[i] <= 10^4` — **all values are strictly positive**. This is the single most important constraint: it guarantees that growing a window always increases its sum and shrinking it always decreases it, which is exactly what makes the sliding window technique valid.
- `1 <= target <= 10^9` — the sum of the whole array can reach `10^5 * 10^4 = 10^9`, which combined with `target` up to `10^9` still fits safely inside `int` (max ~2.1×10^9), but it's close enough to be worth a deliberate check rather than an assumption.

## 2. Recognition Signals

- "Contiguous subarray" + "minimal/maximal length" + "sum meets a threshold" → sliding window territory.
- **All elements positive** is the green light: if negative numbers were allowed, shrinking the window wouldn't reliably decrease the sum, and sliding window would break.
- Looking for the shortest window satisfying a condition (as opposed to counting all windows) — the classic "expand until valid, then shrink while still valid" shape.
- Named pattern: **Variable-Size Sliding Window** (also called the two-pointer/window technique).

## 3. Core Idea

Maintain a window `[left, right]` and its running sum. Expand `right` to grow the sum; once the window sum meets `target`, record its length and then shrink from `left` as much as possible while the sum still meets `target`, since we're hunting for the shortest such window, not just any window.

**Why it's correct:** because every element is positive, the window sum is a monotonic function of both endpoints — moving `right` forward strictly increases the sum, and moving `left` forward strictly decreases it. This monotonicity means once a window `[left, right]` is valid (`sum >= target`), we can safely try shrinking it from the left without needing to re-expand `right` or re-check windows we've already ruled out; every window boundary is visited at most twice (once by `right`, once by `left`).

**Invariant:** at the top of each outer iteration, `sum` is exactly the sum of `nums[left..right]`, and every valid window ending before the current `right` has already been considered for its minimal length.

## 4. Approach 1 — Brute Force

1. For every starting index `i`, extend `j` from `i` to the end, accumulating the sum.
2. As soon as the running sum `>= target`, record the window length `j - i + 1` and stop extending `j` for this `i` (no need to go further, since we already want the shortest from `i`).
3. Track the global minimum length across all `i`.

```java
public int minSubArrayLenBruteForce(int target, int[] nums) {
    int n = nums.length;
    int minLength = Integer.MAX_VALUE;

    for (int i = 0; i < n; i++) {
        int sum = 0;
        for (int j = i; j < n; j++) {
            sum += nums[j];
            if (sum >= target) {
                minLength = Math.min(minLength, j - i + 1);
                break;
            }
        }
    }

    return minLength == Integer.MAX_VALUE ? 0 : minLength;
}
```

**Time:** O(n²) — for each of n starting points, the inner loop can scan up to n elements.
**Space:** O(1) auxiliary.

This is not optimal: with `n = 10^5`, O(n²) is ~10^10 operations, far beyond what runs in time. It also recomputes overlapping sums from scratch for every `i` instead of reusing the work already done by previous windows.

## 5. Approach 2 — Optimized (Sliding Window)

1. Initialize `left = 0`, `sum = 0`, `minLength = Integer.MAX_VALUE`.
2. Iterate `right` from `0` to `n - 1`, adding `nums[right]` to `sum`.
3. While `sum >= target`:
   - Update `minLength = min(minLength, right - left + 1)`.
   - Subtract `nums[left]` from `sum` and increment `left` (shrink from the left to look for a shorter valid window).
4. After the loop, return `minLength` if it was updated, else `0`.

```java
public int minSubArrayLen(int target, int[] nums) {
    int n = nums.length;
    int left = 0;
    int sum = 0;
    int minLength = Integer.MAX_VALUE;

    for (int right = 0; right < n; right++) {
        sum += nums[right];

        while (sum >= target) {
            minLength = Math.min(minLength, right - left + 1);
            sum -= nums[left];
            left++;
        }
    }

    return minLength == Integer.MAX_VALUE ? 0 : minLength;
}
```

**Time:** O(n) — `right` advances n times total; `left` also advances at most n times total across the whole run (it never resets), so the total work is linear, not quadratic, despite the nested loop shape.
**Space:** O(1) auxiliary.

This is optimal: since we must examine every element at least once to know its value, O(n) is the best possible time complexity, and no extra space is needed beyond a few counters. (The problem's own follow-up mentions an O(n log n) alternative using prefix sums plus binary search — useful if elements could be negative and a true sliding window weren't valid, but strictly worse here since positivity already gives us O(n).)

## 6. Dry Run

`target = 7, nums = [2, 3, 1, 2, 4, 3]`

| right (nums[right]) | sum after add | while sum>=target? | action | minLength |
|---|---|---|---|---|
| 0 (2) | 2 | no | — | ∞ |
| 1 (3) | 5 | no | — | ∞ |
| 2 (1) | 6 | no | — | ∞ |
| 3 (2) | 8 | yes | len=4 (0..3), sum-=2→6, left=1; sum<target, exit while | 4 |
| 4 (4) | 10 | yes | len=4 (1..4), sum-=3→7, left=2; still sum>=target: len=3 (2..4), sum-=1→6, left=3; sum<target, exit | 3 |
| 5 (3) | 9 | yes | len=3 (3..5), sum-=2→7, left=4; still sum>=target: len=2 (4..5), sum-=4→3, left=5; sum<target, exit | 2 |

Exit condition: `right` reaches `n`. Final answer: `minLength = 2`, matching the expected output (`[4,3]`).

## 7. Complexity Summary

| Approach | Time | Space | Notes |
|---|---|---|---|
| Brute Force | O(n²) | O(1) aux | Too slow for n=10^5 |
| Sliding Window | O(n) | O(1) aux | `left` and `right` each traverse the array at most once in total |

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|---|---|---|
| No subarray meets target | Must return 0, not a garbage length | `minLength` stays `Integer.MAX_VALUE` if the while-loop body never runs; final check converts that to `0` |
| Single element `>= target` | Shortest window has length 1 | While-loop still fires on that single-element window and records length 1 correctly |
| Entire array needed | Window must grow to full array length before becoming valid | `right` reaches `n - 1` before `sum >= target`; loop still records it correctly on the last iteration |
| `target` equal to total array sum | Boundary case where only the full array qualifies | Sliding window still shrinks correctly since the check is `>=`, not `>` |
| Every element equals target | Every single element is individually valid | Each window immediately shrinks to length 1 as soon as `right` touches that element |
| Large `nums.length` (10^5) with max values (10^4) | Sum can reach ~10^9; must not overflow | `int sum` still fits since max possible sum (~10^9) is well under `Integer.MAX_VALUE` (~2.1×10^9) |

## 9. Java Notes

- `int sum` is safe here because the maximum possible running sum (`10^5 * 10^4 = 10^9`) stays under `Integer.MAX_VALUE` (2,147,483,647) — but this is a deliberate check, not an assumption; in a variant with looser constraints, `long` would be the safer default.
- Using `Integer.MAX_VALUE` as a sentinel for "no answer yet" is idiomatic here since the problem guarantees `nums.length <= 10^5`, so no valid length could ever equal or exceed that sentinel by accident.
- The `while` loop nested inside the `for` loop looks like O(n²) at first glance — worth explicitly noting in an interview that `left` is monotonically non-decreasing across the *entire* run (never reset per outer iteration), which is what gives amortized O(n), not O(n²).

## 10. Common Mistakes

- Using `if (sum >= target)` instead of `while (sum >= target)` — this only shrinks the window once per `right` step instead of shrinking it as much as possible, missing shorter valid windows. Fix: use `while` so the window shrinks fully before moving on.
- Resetting `left` back to `0` (or to `right`) for each new `right` instead of letting it persist across iterations — this silently turns the algorithm back into O(n²). Fix: `left` should only ever move forward, never reset.
- Forgetting to convert the `Integer.MAX_VALUE` sentinel back to `0` when no valid window was found — returns a nonsensical huge number instead of `0`. Fix: check `minLength == Integer.MAX_VALUE` before returning.
- Updating `minLength` after shrinking instead of before — this measures the wrong (already-shrunk, possibly invalid) window. Fix: record the length while `sum >= target` still holds, before subtracting `nums[left]`.

## 11. Interview Takeaway

- **Trigger sentence:** "Shortest/longest contiguous subarray meeting a sum condition, with all-positive elements → variable-size sliding window, expand right, shrink left while still valid."
- **90-second explanation:** "I keep a window with two pointers and a running sum. I grow the window by moving `right` forward and adding to the sum. Whenever the sum meets or exceeds the target, I know this window is a candidate, so I record its length, then try to shrink it from the left as much as possible while it's still valid, updating the minimum length each time. Because all numbers are positive, shrinking always decreases the sum and growing always increases it, so I never have to re-check a window I've already ruled out — both pointers only move forward, giving linear time overall."
- **Related problems:** 3 (Longest Substring Without Repeating Characters), 76 (Minimum Window Substring), 424 (Longest Repeating Character Replacement), 1004 (Max Consecutive Ones III), 713 (Subarray Product Less Than K).

## 12. Recall Questions

**Q:** Why does the sliding window technique require all elements to be positive?
**A:** Because only with strictly positive elements is the window sum guaranteed to increase when the window grows and decrease when it shrinks — that monotonicity is what lets you safely decide to expand or shrink without re-examining discarded windows.

**Q:** Why is the overall time complexity O(n) even though there's a `while` loop nested inside a `for` loop?
**A:** The `left` pointer never resets between outer iterations — across the entire run it moves forward at most n times total, and `right` also moves forward at most n times total, so the combined work across all iterations is linear, not quadratic.

**Q:** Why update `minLength` while the window is still valid, rather than after shrinking it?
**A:** The length only reflects a window that actually satisfies `sum >= target`; if you shrink first and measure after, you might be measuring a window that no longer meets the target.

**Q:** What does returning `Integer.MAX_VALUE` (before conversion) signal, and why is it a safe sentinel here?
**A:** It signals that the inner while-loop body never executed, meaning no valid window was ever found; it's safe because no real subarray length can reach `Integer.MAX_VALUE` given the constraint `nums.length <= 10^5`.

**Q:** Why would this sliding window approach fail if negative numbers were allowed?
**A:** Shrinking the window could then either increase or decrease the sum depending on the sign of the removed element, breaking the monotonic relationship the algorithm relies on to know which pointer to move next.

**Q:** When would the O(n log n) prefix-sum-plus-binary-search alternative actually be preferable to this O(n) sliding window?
**A:** It generally isn't preferable here since O(n) is strictly better, but the technique is useful to know because it generalizes to variants of the problem where the straightforward monotonic sliding-window assumption doesn't hold.

## 13. Final Code

```java
class Solution {
    public int minSubArrayLen(int target, int[] nums) {
        int n = nums.length;
        int left = 0;
        int sum = 0;
        int minLength = Integer.MAX_VALUE;

        for (int right = 0; right < n; right++) {
            sum += nums[right];

            while (sum >= target) {
                minLength = Math.min(minLength, right - left + 1);
                sum -= nums[left];
                left++;
            }
        }

        return minLength == Integer.MAX_VALUE ? 0 : minLength;
    }
}
```

## 14. Self-Test

Given a positive-integer array and a positive target, find the length of the shortest contiguous subarray whose sum is at least the target, or `0` if none exists. Re-derive: use two pointers over one pass — grow the window by moving the right pointer and adding to a running sum, and whenever the sum meets the target, record the window length and shrink from the left as far as possible while it's still valid, since all values are positive and shrinking only decreases the sum.
