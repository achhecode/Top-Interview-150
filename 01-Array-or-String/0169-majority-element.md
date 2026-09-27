---
problem: 169 - Majority Element
url: https://leetcode.com/problems/majority-element/
difficulty: Easy
section: Array / Voting
patterns: [boyer-moore-voting, counting]
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

**Given:** An array `nums` of size `n`.

**Required:** Return the majority element — the value that appears **more than `⌊n/2⌋` times**. It's guaranteed one exists (no need to handle "no majority" case).

**Constraints that actually matter:**
- `1 <= n <= 5 * 10^4` — moderate size; O(n log n) sorting would technically pass, but the explicit follow-up ("linear time and O(1) space") signals the intended solution avoids both sorting and extra data structures.
- **A majority element is guaranteed to exist** — this removes the need for any validation step and is precisely what makes the Boyer–Moore voting trick provably correct without extra bookkeeping.
- Appearing "more than `⌊n/2⌋` times" means strictly more than half — this strict majority (not just "most frequent") is what guarantees exactly one candidate can survive a cancellation-based approach.

## 2. Recognition Signals

- "Return the element that appears **more than half the time**" (a guaranteed strict majority) → this exact phrasing is the signature of **Boyer–Moore Voting**.
- Follow-up explicitly asks for **O(n) time and O(1) space** → rules out sorting (O(n log n)) and hash-map counting (O(n) space), pointing at a counting/cancellation trick instead.
- Whenever one element is guaranteed to outnumber the combined count of all others — pairing it against any other element and "canceling them out" can never eliminate the true majority element down to zero net count.

**Pattern:** Boyer–Moore Voting Algorithm (a specialized counting/cancellation technique, distinct from generic frequency counting).

## 3. Core Idea

Think of each element as casting a vote either for or against a running "candidate." Maintain a `candidate` and a `count`. When you see a value equal to `candidate`, increment `count`; otherwise decrement it. If `count` ever hits 0, discard the current candidate and adopt the new value as the candidate (reset `count` to 1). Because the majority element appears more than `n/2` times, it can never be fully "canceled out" by all other elements combined — no matter how the non-majority elements are distributed, they can knock the majority element's net count down but never to a permanent zero for the rest of the array, so whatever candidate survives to the end must be the true majority element.

**Invariant:** At any point in the scan, if the current `candidate` is *not* the true majority element, then the number of majority-element votes seen so far exceeds the number of votes cast for the current candidate — guaranteeing the candidate will eventually be replaced (by the true majority element or another value) before the scan ends, and specifically that the true majority element can never be permanently eliminated.

## 4. Approach 1 — Brute Force / Hash Map Counting

**Logic:** Count occurrences of every value using a hash map, then return the value whose count exceeds `n/2`.

```java
public int majorityElement(int[] nums) {
    Map<Integer, Integer> counts = new HashMap<>();
    int n = nums.length;
    for (int num : nums) {
        int updated = counts.merge(num, 1, Integer::sum);
        if (updated > n / 2) {
            return num;
        }
    }
    throw new IllegalStateException("No majority element found");
}
```

- **Time:** O(n) — one pass, each `merge` operation is O(1) amortized.
- **Space:** O(n) auxiliary in the worst case (up to n/2 distinct values could be stored before the majority element's count crosses the threshold).

**Why it is not optimal:** It matches the optimal O(n) time, but uses O(n) auxiliary space for the hash map, failing the explicit O(1) space follow-up. It's a perfectly reasonable first correct solution to state before optimizing, though.

## 5. Approach 2 — Optimized (Boyer–Moore Voting)

**Algorithm:**
1. Initialize `candidate` to any placeholder value and `count = 0`.
2. For each `num` in `nums`:
   - If `count == 0`, set `candidate = num` (adopt a new candidate).
   - If `num == candidate`, increment `count`; otherwise decrement `count`.
3. After the scan, `candidate` holds the majority element — return it directly (no verification pass needed, since the problem guarantees a majority element exists).

```java
public int majorityElement(int[] nums) {
    int candidate = nums[0];
    int count = 0;

    for (int num : nums) {
        if (count == 0) {
            candidate = num;
        }
        count += (num == candidate) ? 1 : -1;
    }

    return candidate;
}
```

- **Time:** O(n) — single pass.
- **Space:** O(1) auxiliary — just `candidate` and `count`.

**Why this is optimal:** Every element must be examined at least once (you can't know the majority without looking at all the data — imagine an adversary hiding the majority element's exact count until the last element), so O(n) is a hard lower bound. Space is O(1) because the algorithm never needs to remember more than one running candidate and a signed tally — no per-value frequency table is required. This meets the follow-up's O(n) time / O(1) space target exactly, with no trade-off left to make.

## 6. Dry Run

Example: `nums = [2,2,1,1,1,2,2]`.
Chosen because the candidate flips at least once mid-scan (from 2 to 1) before recovering back to the true majority, exercising both the cancellation and the candidate-replacement logic.

Initial: `candidate = 2` (nums[0]), `count = 0`.

| num | count == 0? | New candidate | num == candidate? | count after |
|-----|-------------|----------------|--------------------|-------------|
| 2 | Yes | 2 | Yes | 1 |
| 2 | No | — | Yes | 2 |
| 1 | No | — | No | 1 |
| 1 | No | — | No | 0 |
| 1 | Yes | 1 | Yes | 1 |
| 2 | No | — | No | 0 |
| 2 | Yes | 2 | Yes | 1 |

Exit condition: loop reaches the end of `nums` (all 7 elements processed).

**Final answer:** `candidate = 2`, which matches the expected majority element (2 appears 4 times out of 7, more than `⌊7/2⌋ = 3`).

Note how the candidate temporarily became `1` in the middle of the scan (when count hit 0 at index 3) but got displaced back to `2` by index 6 — this is exactly the cancellation dynamic that guarantees the true majority survives.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Hash map counting | O(n) | O(n) | Correct but fails the explicit O(1) space follow-up |
| 2. Boyer–Moore voting | O(n) | O(1) | Optimal; meets both the time and space follow-up targets |

Input/output space for `nums` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single element array (`n = 1`) | The lone element is trivially the majority (`1 > ⌊1/2⌋ = 0`) | `candidate` initializes to `nums[0]`, the loop processes it once (`count` goes from 0 to 1), and it's returned correctly without any special-casing |
| Majority element is the very first element | Candidate must not get prematurely replaced before it accumulates enough count | The voting mechanics guarantee that even if `count` dips to 0 partway through and a different candidate is temporarily adopted, the true majority element's sheer frequency forces it back as the final surviving candidate |
| Exactly `⌊n/2⌋ + 1` occurrences (the tightest possible majority) | Cancellation dynamics must still correctly preserve the majority even at the minimum guaranteed margin | The algorithm's correctness proof relies only on "more than half," not on any specific margin beyond that — works identically whether the margin is razor-thin or overwhelming |
| Negative numbers (constraint allows down to `-10^9`) | Comparison logic must not assume non-negative values | Plain `==`/`!=` comparison on `int` works identically regardless of sign |
| Values at `Integer.MIN_VALUE` / near `-10^9`/`10^9` | Could tempt overflow if using subtraction-based comparison tricks | The algorithm never subtracts values from each other — it only compares for equality (`num == candidate`) and increments/decrements a separate `count` variable, so no overflow risk exists |
| All elements identical | Trivial majority; algorithm should never flip candidates | `count` only ever increments (never hits 0 after the first element), so `candidate` never changes from `nums[0]` |

## 9. Java Notes

- **No overflow risk:** since equality is checked directly (`num == candidate`) rather than via subtraction, and `count` only ever increments or decrements by 1, there's no risk of integer overflow even with values near `Integer.MIN_VALUE`/`MAX_VALUE`.
- **Ternary for the vote:** `count += (num == candidate) ? 1 : -1;` is a concise, idiomatic way to express the increment/decrement without an `if`/`else` block — keeps the voting logic compact and readable.
- **No `HashMap` needed in the optimized solution:** avoids boxing (`Integer` keys/values) and hashing overhead entirely, which the brute-force approach incurs.
- **Initializing `candidate = nums[0]` up front:** avoids needing a sentinel value or nullable `Integer` — since `nums.length >= 1` is guaranteed by the constraints, this is always safe.

## 10. Common Mistakes

- **Adding a verification pass after the voting loop to double-check the candidate's count exceeds `n/2`.** This is unnecessary extra work here specifically because the problem *guarantees* a majority element exists; the verification pass is only needed for the related "Majority Element II" variant (where up to two candidates might survive but neither is guaranteed to actually be a majority). Fix: skip verification when existence is guaranteed, but remember to add it back if adapting this pattern to a problem without that guarantee.
- **Resetting `count` to 0 instead of 1 when adopting a new candidate.** This effectively discards the very element that triggered the candidate change, undercounting its first vote. Fix: the `count == 0` check and the vote increment/decrement happen in the same iteration for the same `num`, so make sure the new candidate's first vote is actually counted (as the code above does, since the `count +=` line runs unconditionally after the candidate check).
- **Assuming the algorithm works without the "guaranteed majority exists" precondition.** Without that guarantee, Boyer–Moore can return a non-majority value as the "candidate" if no true majority exists; it only detects a majority *candidate*, not that one truly exists. Fix: always confirm the problem statement guarantees existence before skipping a verification step.
- **Trying to reason about the algorithm by tracking exact per-value counts instead of net cancellation.** This overcomplicates understanding the correctness argument and can lead to bugs when generalizing. Fix: think in terms of "votes canceling out" rather than trying to track individual value frequencies during the scan.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Return the element guaranteed to appear more than half the time, in O(n) time and O(1) space → Boyer–Moore voting: maintain one candidate and a signed vote count."
- **90-second explanation:** "I track a single candidate and a vote count, both starting fresh. As I scan the array, if the current count is zero, I adopt whatever element I'm looking at as the new candidate. Then, if the current element matches the candidate, I increment the count; otherwise I decrement it — essentially, matching elements vote for the candidate and non-matching elements vote against it. The key insight is that because the true majority element appears more than half the time, it can never be permanently canceled out by all the other elements combined, no matter how they're arranged — so whichever candidate survives to the end of the scan must be the true majority element. This runs in O(n) time with O(1) space, and since the problem guarantees a majority exists, I don't need a separate verification pass."
- **Related problems using this pattern:**
  - LeetCode 229 — Majority Element II
  - LeetCode 274 — H-Index (different pattern, but similarly exploits a counting threshold)
  - LeetCode 1046 — Last Stone Weight (different pattern, but conceptually related "cancellation" flavor)

## 12. Recall Questions

**Q:** Why can the true majority element never be permanently eliminated by the Boyer–Moore voting process?
**A:** Because it appears more than half the time, the combined votes from all other elements can never outnumber its votes enough to keep its net presence at zero for the remainder of the array — it always resurfaces as the final surviving candidate.

**Q:** Why doesn't this algorithm need a verification step to confirm the final candidate is actually the majority element?
**A:** The problem explicitly guarantees a majority element always exists, so the candidate that survives the voting process is guaranteed to be it; without that guarantee, a verification pass would be necessary.

**Q:** What would happen if you reset `count` to 0 instead of 1 when switching to a new candidate?
**A:** You'd fail to count that triggering element's own vote for the new candidate, effectively undercounting it by one and potentially causing an incorrect result in edge cases.

**Q:** Why is there no risk of integer overflow in this algorithm despite the wide value range in the constraints?
**A:** The algorithm never subtracts array values from each other; it only checks equality between `num` and `candidate` and adjusts a separate small counter by ±1, so the actual array values never participate in arithmetic that could overflow.

**Q:** How does the voting mechanic behave differently for a value that appears in a long consecutive run versus one that's scattered throughout the array?
**A:** It behaves identically either way — the algorithm only cares about net votes for versus against the current candidate at each step, not about the positions or clustering of occurrences.

## 13. Final Code

```java
public int majorityElement(int[] nums) {
    int candidate = nums[0];
    int count = 0;

    for (int num : nums) {
        if (count == 0) {
            candidate = num;
        }
        count += (num == candidate) ? 1 : -1;
    }

    return candidate;
}
```

## 14. Self-Test

You have an array `nums` where one element is guaranteed to appear more than `⌊n/2⌋` times. Return that majority element in O(n) time and O(1) space. Think about how pairing up "votes" for and against a running candidate, and letting the count hit zero trigger a candidate swap, guarantees the true majority element survives to the end.
