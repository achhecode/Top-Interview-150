---
problem: 274 - H-Index
url: https://leetcode.com/problems/h-index/
difficulty: Medium
section: Array / Sorting
patterns: [sorting, counting-buckets]
data_structures: [array]
time: O(n)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An array `citations` where `citations[i]` is the number of citations the researcher's `i`th paper received.

**Required:** Return the h-index: the maximum value `h` such that the researcher has **at least `h` papers** each cited **at least `h` times**.

**Constraints that actually matter:**
- `1 <= n <= 5000` and `0 <= citations[i] <= 1000` — both bounds are small, which is the key hint: since `h` can never exceed `n` (you can't have more than `n` qualifying papers out of `n` total), the valid range of `h` is exactly `[0, n]`, a small, bounded space perfectly suited to a counting/bucket approach instead of sorting.
- The h-index is defined relative to **counts of papers meeting a threshold**, not the citation values directly — this is the hint toward thinking in terms of "how many papers have at least `h` citations," which naturally leads to a monotonic search or bucket-counting structure.
- A citation count can exceed `n` (up to 1000, while `n` can be as small as 1) — any paper with citations `>= n` behaves identically to one with exactly `n` citations for the purposes of this problem, since `h` can never exceed `n` anyway.

## 2. Recognition Signals

- "Maximum `h` such that at least `h` items each satisfy a threshold of `h`" → this specific self-referential threshold definition is the signature of the **h-index problem type**, distinct from ordinary counting or frequency problems.
- Small, bounded range for the answer (`h` is always between 0 and `n`) → strongly suggests a **counting sort / bucket** approach can replace a full O(n log n) sort with an O(n) alternative.
- Whenever you need "how many elements are `>= h`, for the best possible `h`" → sorting the array (descending or ascending) and scanning for the crossover point where `citations[i] >= (required count)` is a direct, reliable technique; counting buckets is the linear-time refinement of the same idea.

**Pattern:** Sorting-based Threshold Search, optimized via Counting Buckets (since the answer range is bounded by `n`, not by the citation values themselves).

## 3. Core Idea

Sort the citations in descending order. Then, walk through the sorted array with index `i` (0-based): at each position, `i+1` papers so far have citation counts `>= citations[i]` (since the array is sorted descending, everything before and including index `i` has at least `citations[i]` citations). The h-index is the largest `i+1` such that `citations[i] >= i+1` still holds — once `citations[i] < i+1`, going further can't help, since citation counts only decrease from here. This works because the h-index is fundamentally about finding the balance point between "how many papers" and "how many citations each," and sorting exposes that balance point directly.

For the O(n) optimization: since `h` can never exceed `n`, bucket every citation count into `min(citations[i], n)` "at least this many" buckets, then scan from `h = n` down to `0`, accumulating how many papers have at least `h` citations — the first `h` (scanning from the top down) where the accumulated count is `>= h` is the answer.

**Invariant:** After processing bucket `h` during the downward scan, `accumulated` holds the exact number of papers with citation count `>= h`; the moment `accumulated >= h`, that `h` is the largest valid h-index, since larger `h` values were already checked and failed (scanning from `n` down to `0` guarantees the first success is the maximum).

## 4. Approach 1 — Sorting-Based

**Logic:** Sort `citations` in descending order. Scan through; the h-index is the largest count `i+1` for which `citations[i] >= i+1` still holds.

```java
public int hIndex(int[] citations) {
    Integer[] boxed = new Integer[citations.length];
    for (int i = 0; i < citations.length; i++) {
        boxed[i] = citations[i];
    }
    Arrays.sort(boxed, Collections.reverseOrder());

    int h = 0;
    for (int i = 0; i < boxed.length; i++) {
        if (boxed[i] >= i + 1) {
            h = i + 1;
        } else {
            break;
        }
    }
    return h;
}
```

- **Time:** O(n log n) — dominated by the sort.
- **Space:** O(n) auxiliary for the boxed `Integer[]` array needed to use a custom comparator (primitive `int[]` can't use `Collections.reverseOrder()` directly).

**Why it is not optimal:** Correct and simple to reason about, but the sort's O(n log n) time isn't necessary given how small and bounded the citation-count range effectively is for this problem's purposes (since only values up to `n` matter) — a counting-based approach can do this in O(n) instead.

## 5. Approach 2 — Optimized (Counting Buckets)

**Algorithm:**
1. Let `n = citations.length`. Create a `buckets` array of size `n + 1`, where `buckets[h]` will count papers with **exactly** `min(citations[i], n)` citations (capping at `n`, since `h` can never exceed `n`).
2. For each citation count, increment the appropriate bucket: `buckets[Math.min(citations[i], n)]++`.
3. Scan `h` from `n` down to `0`, maintaining a running total `papersWithAtLeastH`. Add `buckets[h]` to this total at each step. The moment `papersWithAtLeastH >= h`, return `h` immediately — this is the largest valid h-index.

```java
public int hIndex(int[] citations) {
    int n = citations.length;
    int[] buckets = new int[n + 1];

    for (int c : citations) {
        buckets[Math.min(c, n)]++;
    }

    int papersWithAtLeastH = 0;
    for (int h = n; h >= 0; h--) {
        papersWithAtLeastH += buckets[h];
        if (papersWithAtLeastH >= h) {
            return h;
        }
    }
    return 0;
}
```

- **Time:** O(n) — one pass to fill buckets, one pass (of fixed size `n+1`) to scan them.
- **Space:** O(n) auxiliary for the `buckets` array.

**Why this is optimal:** Every citation count must be examined at least once to know its contribution, so O(n) time is a hard lower bound — and this approach achieves it, beating the O(n log n) sort. The O(n) auxiliary space for buckets is a deliberate trade-off: since the answer's valid range is capped at `n` (not at the potentially much larger max citation value of 1000), bucket size `n+1` is the natural fit, and this space is a reasonable price for shaving the log factor off the time complexity. (A stricter O(1) auxiliary space solution based on in-place partitioning exists but is significantly more intricate to implement correctly and isn't necessary here, since O(n) already meets the practical goal.)

## 6. Dry Run

Example: `citations = [3,0,6,1,5]`.
Chosen because it's the canonical example, and it exercises a citation count (6) that exceeds `n = 5`, requiring the capping logic.

`n = 5`. Buckets array size 6 (indices 0 to 5), all initialized to 0.

**Bucket-filling pass:**

| citations[i] | min(c, n) | bucket incremented |
|--------------|-----------|----------------------|
| 3 | 3 | buckets[3]++ → 1 |
| 0 | 0 | buckets[0]++ → 1 |
| 6 | 5 (capped) | buckets[5]++ → 1 |
| 1 | 1 | buckets[1]++ → 1 |
| 5 | 5 | buckets[5]++ → 2 |

Resulting `buckets = [1,1,0,1,0,2]` (indices 0 through 5).

**Downward scan pass:** Initial `papersWithAtLeastH = 0`.

| h | buckets[h] | papersWithAtLeastH after | papersWithAtLeastH >= h? |
|---|------------|------------------------------|-------------------------------|
| 5 | 2 | 0+2=2 | No (2 >= 5 false) |
| 4 | 0 | 2+0=2 | No (2 >= 4 false) |
| 3 | 1 | 2+1=3 | **Yes (3 >= 3 true)** → return 3 |

Exit condition: at `h = 3`, `papersWithAtLeastH (3) >= h (3)` triggers the return.

**Final answer:** `3`, matching the expected output exactly — the researcher has 3 papers (with 3, 5, and 6 citations) each cited at least 3 times.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Sort descending, scan | O(n log n) | O(n) (boxed array for custom comparator) | Correct and simple, but not optimal in time |
| 2. Counting buckets | O(n) | O(n) | Optimal in time; trades bounded auxiliary space for beating the log factor |

Input/output space for `citations` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Single paper (`n == 1`) | h-index should be 1 if that paper has at least 1 citation, else 0 | With `n=1`, buckets has size 2; a citation count of 0 lands in `buckets[0]`, and the downward scan starting at `h=1` finds `papersWithAtLeastH = 0 < 1`, falling through to `h=0` where `0 >= 0` returns 0 correctly; a citation count `>= 1` lands in `buckets[1]` (or capped there), giving `papersWithAtLeastH = 1 >= 1` at `h=1`, correctly returning 1 |
| All citations are 0 | h-index should be 0 (no paper has even 1 citation) | Every value lands in `buckets[0]`; the downward scan never finds `papersWithAtLeastH >= h` for any `h > 0` (since `buckets[h] = 0` for all `h >= 1`), only succeeding at `h = 0` where any non-negative count trivially satisfies `>= 0` |
| Citation counts far exceeding `n` (e.g., a single paper with 1000 citations while `n` is small) | Must not let a huge citation value push `h` beyond `n`, which is mathematically impossible (can't have more qualifying papers than total papers) | The capping `Math.min(citations[i], n)` ensures any citation count is only ever credited up to bucket index `n`, correctly bounding the maximum possible h-index at `n` |
| All papers have the same high citation count `c >= n` | h-index should be exactly `n` (every paper qualifies for the maximum possible threshold) | All values cap into `buckets[n]`; at `h = n`, `papersWithAtLeastH` equals the full count `n`, satisfying `n >= n` immediately, correctly returning `n` |
| Citation array already sorted, or in any particular order | The bucket approach is order-independent | Since bucketing only counts occurrences regardless of position, and the downward scan only depends on aggregate bucket counts, the original order of `citations` never affects the result |
| `citations[i] = 0` (minimum allowed value) | Should behave correctly as "this paper doesn't count toward any `h >= 1`" | Lands in `buckets[0]`, contributing only to the `h=0` fallback case, exactly as expected |

## 9. Java Notes

- **`Math.min(c, n)` for capping:** a simple, direct way to bound citation counts into the valid bucket range without any conditional branching beyond the min itself.
- **Boxed `Integer[]` needed for `Collections.reverseOrder()` in Approach 1:** Java's `Arrays.sort` on primitive `int[]` doesn't support custom comparators (including simple descending order), so sorting descending requires either boxing to `Integer[]` (as shown) or sorting ascending and reading the array from the end, or negating values before sorting ascending — worth knowing this Java-specific quirk when comparing primitive vs. object array sorting.
- **No overflow risk:** citation counts and `n` are both small (`n <= 5000`, citations `<= 1000`), so all sums and comparisons stay comfortably within `int` range with enormous headroom.
- **Downward scan avoids needing a separate "find the max valid h" step:** because the scan proceeds from the largest possible `h` down to 0, the very first success encountered is guaranteed to be the maximum, allowing an immediate return rather than needing to track and compare multiple candidates.

## 10. Common Mistakes

- **Forgetting to cap citation counts at `n` when bucketing.** Without capping, a citation count larger than `n` would require a bucket array larger than `n+1`, wasting space and complicating the scan range — and it's unnecessary anyway, since `h` can never exceed `n`. Fix: always bucket using `Math.min(citations[i], n)`.
- **Scanning the buckets from `h = 0` upward instead of from `h = n` downward.** Scanning upward would require tracking the *maximum* qualifying `h` seen so far rather than returning immediately on the first hit, since smaller `h` values are almost always trivially satisfiable, making the "first success" trick only work in the downward direction. Fix: always scan from the largest possible `h` (`n`) down to 0 so the first qualifying `h` found is definitively the answer.
- **Confusing the h-index definition with "the paper with the most citations" or "the average citation count."** The h-index is a specific balance between paper count and citation count, not simply the maximum or mean of the citation values — a common early misunderstanding when first encountering this problem. Fix: revisit the precise Wikipedia-style definition (at least `h` papers, each with at least `h` citations) before choosing an approach.
- **Using the sorting approach with a primitive `int[]` and trying to pass a comparator directly.** `Arrays.sort(int[], Comparator)` doesn't exist in the Java standard library — only object array overloads support comparators. Fix: either box to `Integer[]`, or sort ascending and iterate from the end, or negate values before an ascending sort.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Maximum h such that at least h papers each have at least h citations → sort descending and find the crossover point, or bucket-count since h is always bounded by n."
- **90-second explanation:** "I recognize that the h-index can never exceed the total number of papers, `n`, since you can't have more qualifying papers than papers that exist. That bounded range is the key to an O(n) solution: I bucket each paper's citation count, capping it at `n` if it's larger (since anything `>= n` behaves identically for this problem). Then I scan the bucket counts from `h = n` down to `0`, keeping a running total of how many papers have at least `h` citations. The very first `h` where that running total is at least `h` itself is the answer, and because I'm scanning from the top down, that first success is guaranteed to be the maximum possible h-index. This runs in O(n) time, trading a bounded O(n) auxiliary space for beating the O(n log n) time a straightforward sort-based approach would need."
- **Related problems using this pattern:**
  - LeetCode 275 — H-Index II (sorted input variant, solvable with binary search)
  - LeetCode 349 — Intersection of Two Arrays (different pattern, but similarly benefits from bucket/counting techniques)
  - LeetCode 1748 — Sum of Unique Elements (counting-based)

## 12. Recall Questions

**Q:** Why can the h-index never exceed the total number of papers, `n`, and why does that fact matter for this algorithm's efficiency?
**A:** You can't have more papers satisfying a citation threshold than the total number of papers that exist, so the valid range for `h` is bounded by `n` regardless of how large individual citation counts are — this bounded range is exactly what makes a counting/bucket approach with size `n+1` sufficient and efficient.

**Q:** Why does scanning the buckets from `h = n` down to `0` (rather than from `0` up to `n`) allow an immediate return on the first success?
**A:** Scanning from the largest possible value downward means the first `h` where the running total of qualifying papers meets or exceeds `h` is guaranteed to be the maximum such `h`, since every larger value was already checked and failed.

**Q:** Why is it correct to cap any citation count greater than `n` down to exactly `n` when bucketing?
**A:** Since `h` can never be validated beyond `n` papers total, a citation count of `n` or more contributes identically to the calculation as a citation count of exactly `n` — there's no additional benefit to distinguishing higher values beyond that cap.

**Q:** What would go wrong if you tried to sort a primitive `int[]` directly using `Collections.reverseOrder()`?
**A:** That comparator-based sort method only works on object arrays (like `Integer[]`), not primitive `int[]`, so you'd need to box the array into `Integer[]` first, or use an alternative technique like sorting ascending and reading from the end.

**Q:** If every paper in the dataset has exactly 0 citations, what h-index does this algorithm return, and why?
**A:** It returns 0, because no paper has even a single citation, so the downward scan never finds a running total meeting or exceeding any `h > 0`, only succeeding trivially once it reaches `h = 0`.

## 13. Final Code

```java
public int hIndex(int[] citations) {
    int n = citations.length;
    int[] buckets = new int[n + 1];

    for (int c : citations) {
        buckets[Math.min(c, n)]++;
    }

    int papersWithAtLeastH = 0;
    for (int h = n; h >= 0; h--) {
        papersWithAtLeastH += buckets[h];
        if (papersWithAtLeastH >= h) {
            return h;
        }
    }
    return 0;
}
```

## 14. Self-Test

You have an array `citations` where `citations[i]` is the number of citations the researcher's `i`th paper received. Return the h-index: the maximum `h` such that at least `h` papers each have at least `h` citations. Think about why the h-index can never exceed the total number of papers, and how that bounded range enables an O(n) counting-bucket approach instead of sorting.
