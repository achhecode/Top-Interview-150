---
problem: 238 - Product of Array Except Self
url: https://leetcode.com/problems/product-of-array-except-self/
difficulty: Medium
section: Array / Prefix-Suffix
patterns: [prefix-suffix-product, running-accumulator]
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

**Given:** An integer array `nums`.

**Required:** Return an array `answer` where `answer[i]` is the product of every element in `nums` except `nums[i]`.

**Constraints that actually matter:**
- `2 <= nums.length <= 10^5` — large enough that an O(n²) approach (recomputing the full product for every index) is too slow; O(n) is required.
- **Division is explicitly disallowed.** This is the single most important constraint — the "obvious" approach (compute the total product, then divide by `nums[i]` for each index) is forbidden, specifically because it would break on any zero in the array (division by zero) and the problem wants you to demonstrate a division-free technique.
- The product of any prefix or suffix is guaranteed to fit in a 32-bit `int` — this explicitly rules out needing `long` arithmetic, despite products potentially growing large.
- The explicit follow-up asks for **O(1) extra space** (excluding the output array itself) — this is the hint toward computing prefix and suffix products in place, reusing the output array, rather than allocating separate prefix/suffix arrays.

## 2. Recognition Signals

- "Product of all elements except self, in O(n), **without division**" → this exact phrasing is the signature of the **prefix product / suffix product** technique.
- Whenever you need, for every index `i`, some aggregate (product, sum, min, max) over "everything except position `i`" → split the problem into "everything to the left of `i`" and "everything to the right of `i`", computed via two passes.
- The explicit "no division" constraint is itself a strong signal: if division were allowed, this would be a trivial single-pass problem (`total / nums[i]`), so its prohibition is what elevates this into the prefix/suffix pattern.

**Pattern:** Prefix Product × Suffix Product (a two-pass accumulator technique, further space-optimized into a single output array plus one running variable).

## 3. Core Idea

`answer[i]` is exactly the product of everything to the left of `i` multiplied by the product of everything to the right of `i` — that is, `answer[i] = prefix[i] * suffix[i]`, where `prefix[i]` is the product of `nums[0..i-1]` and `suffix[i]` is the product of `nums[i+1..n-1]`. Compute `prefix` products left to right in one pass, then compute `suffix` products right to left in a second pass, multiplying into the same output array. Since each pass only depends on values seen so far in that direction, no division is ever needed to "undo" including `nums[i]` — it's simply never included in the first place.

**Invariant:** After the first (left-to-right) pass, `answer[i]` holds exactly `prefix[i]`, the product of all elements strictly before index `i`. During the second (right-to-left) pass, multiplying `answer[i]` by a running `suffix` accumulator (initialized to 1, updated after each index) correctly completes `answer[i]` to `prefix[i] * suffix[i]`, the full "everything except `nums[i]`" product.

## 4. Approach 1 — Brute Force

**Logic:** For each index `i`, compute the product of all elements except `nums[i]` by iterating through the entire array again, skipping index `i`.

```java
public int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] answer = new int[n];

    for (int i = 0; i < n; i++) {
        int product = 1;
        for (int j = 0; j < n; j++) {
            if (j != i) {
                product *= nums[j];
            }
        }
        answer[i] = product;
    }

    return answer;
}
```

- **Time:** O(n²) — for each of the `n` output positions, an O(n) inner scan is performed.
- **Space:** O(1) auxiliary (excluding the required output array).

**Why it is not optimal:** With `n` up to `10^5`, O(n²) is far too slow (up to ~10^10 operations in the worst case), even though the auxiliary space is already minimal. It repeats the same multiplication work across many different `i` values instead of reusing partial products computed for neighboring indices.

## 5. Approach 2 — Optimized (Prefix × Suffix Products, Single Output Array)

**Algorithm:**
1. Create the `answer` array of size `n`.
2. **First pass (left to right):** Set `answer[0] = 1` (there's nothing to the left of index 0). For `i` from `1` to `n-1`, set `answer[i] = answer[i-1] * nums[i-1]` — this builds up the prefix product incrementally.
3. **Second pass (right to left):** Initialize a running `suffix = 1`. For `i` from `n-1` down to `0`: multiply `answer[i] *= suffix` (completing the prefix product already stored there with the suffix product accumulated so far), then update `suffix *= nums[i]` (extending the running suffix product to include this index, for use by the next index to the left).
4. Return `answer`.

```java
public int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] answer = new int[n];

    answer[0] = 1;
    for (int i = 1; i < n; i++) {
        answer[i] = answer[i - 1] * nums[i - 1];
    }

    int suffix = 1;
    for (int i = n - 1; i >= 0; i--) {
        answer[i] *= suffix;
        suffix *= nums[i];
    }

    return answer;
}
```

- **Time:** O(n) — two linear passes over the array.
- **Space:** O(1) auxiliary (excluding the output array, which the problem explicitly says doesn't count) — only the single `suffix` running variable is needed beyond the required output.

**Why this is optimal:** Every element must be examined at least once to compute its contribution to every other position's product, so O(n) is a hard lower bound (matching what two linear passes achieve). Space is O(1) auxiliary because the prefix products are stored directly in the output array as they're computed, and the suffix products are folded in using a single running accumulator rather than a separate suffix array — exactly meeting the explicit follow-up requirement. This avoids division entirely by construction: each pass only ever multiplies in values it has already "seen," never needing to divide out a value that was mistakenly included.

## 6. Dry Run

Example: `nums = [1,2,3,4]`.
Chosen because it's the canonical example, and it clearly demonstrates both passes contributing meaningfully to the final result.

`n = 4`.

**First pass (prefix products, left to right):**

| i | answer[i-1] | nums[i-1] | answer[i] = answer[i-1] * nums[i-1] |
|---|---------------|-------------|------------------------------------------|
| 0 | — | — | answer[0] = 1 (base case) |
| 1 | 1 | nums[0]=1 | 1*1=1 |
| 2 | 1 | nums[1]=2 | 1*2=2 |
| 3 | 2 | nums[2]=3 | 2*3=6 |

After first pass: `answer = [1,1,2,6]` (these are the prefix products: product of everything strictly before each index).

**Second pass (suffix products, right to left):** Initial `suffix = 1`.

| i | answer[i] before | answer[i] *= suffix | suffix *= nums[i] (new suffix) |
|---|----------------------|--------------------------|-------------------------------------|
| 3 | 6 | 6*1=6 | suffix = 1*nums[3]=1*4=4 |
| 2 | 2 | 2*4=8 | suffix = 4*nums[2]=4*3=12 |
| 1 | 1 | 1*12=12 | suffix = 12*nums[1]=12*2=24 |
| 0 | 1 | 1*24=24 | suffix = 24*nums[0]=24*1=24 |

Exit condition: second pass completes after processing `i = 0`.

**Final answer:** `[24,12,8,6]`, matching the expected output exactly.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Brute force, recompute per index | O(n²) | O(1) (excl. output) | Correct but far too slow for n up to 10^5 |
| 2. Prefix × suffix, single output array | O(n) | O(1) (excl. output) | Optimal; meets both the O(n) time and O(1) extra space follow-up |

Input/output space for the `answer` array is O(n) in both, as given by the problem, and explicitly excluded from the space-complexity analysis per the follow-up's own framing.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| A single zero in the array (e.g. `[-1,1,0,-3,3]`) | Every position except the zero's own index should get a nonzero product (product of everything else), while the zero's own index should get the product of all the *other*, nonzero elements | The prefix/suffix technique naturally handles this: `answer[i]` for the zero's index correctly multiplies together everything else (nonzero), while every other index's product correctly includes the zero, making it 0 — no special-casing needed since multiplication naturally propagates the zero everywhere except its own position |
| Two or more zeros in the array | Every position's answer should be 0, since every product except at most one zero's own position still includes at least one other zero | Since prefix or suffix products naturally include any zero encountered during that pass, every `answer[i]` ends up multiplied by at least one zero from either the prefix or suffix side (except possibly the zero's own two index positions, which still each see the *other* zero), correctly yielding 0 everywhere |
| Negative numbers | Sign of the product must be correctly tracked through repeated multiplication | Standard `int` multiplication correctly handles sign propagation with no special logic needed; the sign naturally falls out of how many negative factors are multiplied together |
| Minimum length array (`n = 2`) | Prefix pass has almost nothing to build (`answer[0] = 1` immediately), and suffix pass must still correctly complete both positions | With `n=2`, `answer[0]=1` and `answer[1] = answer[0]*nums[0] = nums[0]` after the prefix pass; the suffix pass then correctly multiplies `answer[1]` by `suffix=1` (unchanged) and `answer[0]` by `suffix=nums[1]` (updated after processing index 1), yielding `answer = [nums[1], nums[0]]`, the correct result for a 2-element array |
| Maximum negative values repeated many times (extreme products) | The problem guarantees any prefix/suffix product fits in 32-bit `int`, but it's worth confirming no intermediate overflow occurs mid-computation | Since the guarantee explicitly covers *any* prefix or suffix product (not just the final combined `answer[i]` values), every intermediate multiplication performed during either pass is itself guaranteed to fit within `int` range, so no overflow occurs at any step |
| All elements equal to 1 | Trivial case; every `answer[i]` should just be 1 | Every multiplication involves multiplying by 1, so the products remain unchanged throughout, correctly yielding an all-1s result |

## 9. Java Notes

- **No `long` arithmetic needed:** the problem explicitly guarantees that any prefix or suffix product fits within a 32-bit `int`, so plain `int` multiplication throughout is both correct and idiomatic — reaching for `long` here would be unnecessary defensive coding given the explicit guarantee.
- **Reusing the output array for the prefix pass, then updating in place during the suffix pass:** this is the specific Java-level realization of the O(1) extra space follow-up — rather than allocating a separate `prefix[]` and `suffix[]` array (which would be O(n) extra space each), the prefix values are written directly into `answer`, and the suffix contribution is folded in via a single scalar `suffix` variable during the second pass.
- **Order of operations in the suffix pass (`answer[i] *= suffix;` before `suffix *= nums[i];`):** this ordering is essential — the suffix accumulator must reflect the product of elements *strictly to the right* of `i` when completing `answer[i]`, so `nums[i]` itself must not be folded into `suffix` until after `answer[i]` has already been finalized for this index.

## 10. Common Mistakes

- **Trying to use division (`totalProduct / nums[i]`) as a shortcut.** This is explicitly disallowed by the problem, and even if it weren't, it would break entirely whenever any `nums[i]` is 0, since division by zero is undefined and there's no clean way to recover the correct per-index products from a single total product plus division in that case. Fix: always use the prefix/suffix multiplication technique, which sidesteps division entirely.
- **Updating the `suffix` accumulator before multiplying it into `answer[i]` during the second pass.** This would incorrectly include `nums[i]` itself in its own "except self" product for that index. Fix: always multiply `answer[i]` by the current `suffix` value first, and only update `suffix` to include `nums[i]` afterward.
- **Allocating separate `prefix[]` and `suffix[]` arrays instead of reusing the output array and a single running variable.** This works correctly but uses O(n) extra space for each auxiliary array, failing to meet the explicit O(1) extra space follow-up. Fix: compute prefix products directly into the output array, then fold in suffix products using one scalar accumulator during a second pass over the same array.
- **Forgetting to initialize `answer[0] = 1` (or the equivalent base case for whichever end the prefix pass starts from).** Without this explicit base case, the first meaningful prefix product wouldn't have a correct starting point to build from. Fix: always explicitly set the boundary value (empty prefix or empty suffix product is 1, the multiplicative identity) before starting the accumulation loop.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Product of everything except self, O(n), no division → prefix products left to right, then fold in suffix products right to left, reusing the output array."
- **90-second explanation:** "I recognize that `answer[i]` is just the product of everything to the left of `i` times everything to the right of `i`. So I do two passes: first, left to right, I build up prefix products directly into the output array, where `answer[i]` ends up holding the product of everything strictly before index `i`. Then, right to left, I keep a single running `suffix` product, and at each index I multiply it into `answer[i]` to complete the calculation, before extending `suffix` to include the current element for the next index to the left. This avoids division entirely, which matters especially because the array might contain zeros where division would be undefined. It runs in O(n) time with two linear passes, and O(1) extra space since I reuse the output array for prefix products and only need one extra scalar variable for the suffix accumulation."
- **Related problems using this pattern:**
  - LeetCode 42 — Trapping Rain Water (also uses left-max/right-max prefix-suffix accumulation)
  - LeetCode 152 — Maximum Product Subarray
  - LeetCode 724 — Find Pivot Index (prefix sum variant of a similar "everything except here" idea)

## 12. Recall Questions

**Q:** Why does this problem's "no division" constraint push toward a fundamentally different technique than the seemingly obvious "total product divided by `nums[i]`" approach?
**A:** Division by `nums[i]` would be undefined whenever any element is zero, and the problem explicitly disallows division anyway, so the prefix/suffix multiplication technique is needed instead, which never requires "undoing" a multiplication.

**Q:** Why must `answer[i] *= suffix` happen *before* `suffix *= nums[i]` in the second pass, rather than after?
**A:** The suffix accumulator is meant to represent the product of elements strictly to the right of index `i` when completing that index's answer; updating it to include `nums[i]` before using it would incorrectly include the element in its own "except self" product.

**Q:** How does this algorithm correctly handle an array containing exactly one zero, without any explicit special-casing for zero?
**A:** Every index other than the zero's own position will have either its prefix or suffix product include that zero (making its answer 0), while the zero's own index's answer is built purely from the products of all the surrounding nonzero elements — multiplication naturally propagates this without needing extra logic.

**Q:** Why doesn't this solution need `long` arithmetic despite computing potentially large products?
**A:** The problem explicitly guarantees that any prefix or suffix product of the input is guaranteed to fit within a 32-bit `int`, so plain `int` multiplication is both sufficient and correct throughout every step of the computation.

**Q:** How does storing prefix products directly in the output array, rather than in a separate array, help meet the O(1) extra space follow-up?
**A:** Since the output array is required regardless and explicitly excluded from the space-complexity count, reusing it to hold intermediate prefix products (rather than allocating a second array) means the only truly "extra" memory needed is the single scalar `suffix` variable used in the second pass.

## 13. Final Code

```java
public int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] answer = new int[n];

    answer[0] = 1;
    for (int i = 1; i < n; i++) {
        answer[i] = answer[i - 1] * nums[i - 1];
    }

    int suffix = 1;
    for (int i = n - 1; i >= 0; i--) {
        answer[i] *= suffix;
        suffix *= nums[i];
    }

    return answer;
}
```

## 14. Self-Test

You have an integer array `nums`. Return an array `answer` where `answer[i]` is the product of all elements except `nums[i]`, in O(n) time, without using division, and ideally in O(1) extra space (excluding the output). Think about how splitting each position's answer into "everything to the left" times "everything to the right," computed in two passes, avoids ever needing to divide.
