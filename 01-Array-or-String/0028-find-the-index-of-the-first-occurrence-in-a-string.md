---
problem: 28 - Find the Index of the First Occurrence in a String
url: https://leetcode.com/problems/find-the-index-of-the-first-occurrence-in-a-string/
difficulty: Easy
section: String / Pattern Matching
patterns: [kmp, prefix-function]
data_structures: [string, array]
time: O(n + m)
space: O(m)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** Two strings, `haystack` and `needle`.

**Required:** Return the index of the first occurrence of `needle` as a substring within `haystack`, or `-1` if it doesn't occur at all.

**Constraints that actually matter:**
- `1 <= haystack.length, needle.length <= 10^4` — both strings can be individually up to 10,000 characters, so a naive nested-loop brute force (O(n × m)) could reach up to `10^8` character comparisons in the worst case (e.g., `haystack` = "aaaa...a" and `needle` = "aaa...ab") — slow but often still within typical time limits for this specific Easy-rated problem, though it's the textbook motivating example for the KMP algorithm as the "proper" solution.
- Both strings consist of only lowercase English letters — a simple, fixed alphabet; no special character-handling needed.
- This is, essentially, Java's built-in `String.indexOf(String)` functionality — the problem is explicitly testing whether you can implement substring search from scratch, most notably via the classic **Knuth-Morris-Pratt (KMP)** algorithm, which guarantees O(n + m) time regardless of how adversarial the input is.

## 2. Recognition Signals

- "Find the first occurrence of one string within another" → the classic **substring search / pattern matching** problem, whose well-known optimal solution is the **KMP algorithm**.
- Recognizing that the brute-force nested-loop approach can degrade to O(n × m) specifically on inputs with **long repeated character runs** (like `haystack` full of the same character and `needle` almost matching but failing near the very end) — this adversarial-case awareness is exactly what motivates needing a smarter algorithm that avoids redundant re-comparisons.
- The core KMP insight to look for: when a mismatch occurs partway through a comparison, the characters already matched so far tell you something about where the *next* possible match could start — specifically, if part of what you just matched is *also* a prefix of the pattern, you can skip directly to aligning that reusable prefix, rather than restarting the comparison one position over from scratch.

**Pattern:** Knuth-Morris-Pratt (KMP) String Matching (precompute a "failure function"/"longest proper prefix that is also a suffix" array for the pattern, then use it to avoid re-scanning `haystack` characters upon a mismatch).

## 3. Core Idea

The brute-force approach, upon a mismatch at some position, simply restarts the comparison one character further along in `haystack` — this can redundantly re-examine characters that were already confirmed to match in the previous failed attempt. KMP avoids this by precomputing, for the `needle` pattern itself, an array `lps` (**L**ongest **P**roper **P**refix which is also a **S**uffix) where `lps[i]` tells you the length of the longest proper prefix of `needle[0..i]` that is also a suffix of `needle[0..i]`. When a mismatch occurs while comparing `needle[j]` against `haystack[i]`, instead of resetting `j` to 0 and starting over, you reset `j` to `lps[j-1]` — this correctly "reuses" the fact that the characters already matched contain a smaller prefix of the pattern that's guaranteed to already align correctly, letting you skip ahead without ever needing to re-examine already-confirmed `haystack` characters.

**Invariant:** At every point during the main matching scan, the `haystack` pointer `i` never moves backward — it only ever advances forward or stays in place while `j` (the pattern pointer) is adjusted using the precomputed `lps` array — ensuring each character of `haystack` is examined only a bounded (amortized constant) number of times across the entire scan, guaranteeing overall O(n) work for the `haystack` traversal, on top of O(m) work to build the `lps` array itself.

## 4. Approach 1 — Brute Force (Nested Loop)

**Logic:** For every possible starting position `i` in `haystack`, check whether `needle` matches starting at that position by comparing character by character; return the first `i` where a full match is found.

```java
public int strStr(String haystack, String needle) {
    int n = haystack.length();
    int m = needle.length();

    for (int i = 0; i <= n - m; i++) {
        int j = 0;
        while (j < m && haystack.charAt(i + j) == needle.charAt(j)) {
            j++;
        }
        if (j == m) {
            return i;
        }
    }

    return -1;
}
```

- **Time:** O(n × m) in the worst case — for each of up to `n` starting positions, up to `m` characters may be compared before a mismatch (or full match) is found.
- **Space:** O(1) auxiliary — just a couple of index variables.

**Why it is not optimal:** On adversarial inputs (e.g., `haystack` = a long run of the same character, `needle` = a slightly different long run of that character), this degrades to O(n × m), which with `n, m` both up to `10^4` could reach `10^8` character comparisons — likely to pass on LeetCode's judges for this specific Easy problem given typical time limits, but not truly optimal, and it's the standard motivating example for why KMP exists.

## 5. Approach 2 — Optimized (KMP: Precompute LPS Array, Then Linear Scan)

**Algorithm:**
1. **Build the `lps` array** for `needle` (length `m`): `lps[i]` = length of the longest proper prefix of `needle[0..i]` that's also a suffix of `needle[0..i]`. Computed via a self-referential two-pointer scan over `needle` itself.
2. **Main matching scan:** Use two pointers, `i` over `haystack` and `j` over `needle`. While scanning:
   - If `haystack[i] == needle[j]`, advance both `i` and `j`.
   - If `j == m` (a full match of `needle` has been found), return `i - j` (the starting index of the match).
   - If there's a mismatch (`haystack[i] != needle[j]`) and `j > 0`, don't reset `j` to 0 — instead set `j = lps[j-1]` (reuse the longest matching prefix/suffix overlap) and retry the comparison at the same `i` without advancing it.
   - If there's a mismatch and `j == 0`, simply advance `i` (nothing to reuse).
3. If the scan completes without finding a full match, return `-1`.

```java
public int strStr(String haystack, String needle) {
    int n = haystack.length();
    int m = needle.length();
    if (m == 0) {
        return 0;
    }

    int[] lps = buildLPS(needle);

    int i = 0;
    int j = 0;
    while (i < n) {
        if (haystack.charAt(i) == needle.charAt(j)) {
            i++;
            j++;
            if (j == m) {
                return i - j;
            }
        } else if (j > 0) {
            j = lps[j - 1];
        } else {
            i++;
        }
    }

    return -1;
}

private int[] buildLPS(String needle) {
    int m = needle.length();
    int[] lps = new int[m];
    int len = 0;
    int i = 1;

    while (i < m) {
        if (needle.charAt(i) == needle.charAt(len)) {
            len++;
            lps[i] = len;
            i++;
        } else if (len > 0) {
            len = lps[len - 1];
        } else {
            lps[i] = 0;
            i++;
        }
    }

    return lps;
}
```

- **Time:** O(n + m) — building the `lps` array is O(m) (each character of `needle` is processed via a similar amortized-constant two-pointer technique), and the main scan is O(n) (the `haystack` pointer `i` never moves backward, so it advances at most `n` times total across the entire scan, regardless of how many times `j` gets reset via `lps`).
- **Space:** O(m) auxiliary for the `lps` array, proportional to the length of `needle` only, independent of `haystack`'s length.

**Why this is optimal:** Every character of both `haystack` and `needle` must be examined at least once in the worst case (you can't determine a match or non-match without looking at the relevant characters), so O(n + m) is essentially the best possible bound for a comparison-based approach, and KMP achieves exactly that — a genuine asymptotic improvement over the brute force's O(n × m) worst case. The O(m) auxiliary space for the `lps` array is a necessary and modest cost for enabling this speedup, since it's what allows the algorithm to "remember" the pattern's own internal self-overlap structure and avoid re-scanning `haystack` upon a mismatch.

## 6. Dry Run

Example: `haystack = "sadbutsad"`, `needle = "sad"`.
Chosen because it's the canonical example, and while `needle` here is simple enough that the `lps` array is mostly trivial, it still demonstrates the full KMP mechanics end to end.

**Building `lps` for `needle = "sad"`:** `m = 3`. Initial: `lps = [0,0,0]`, `len = 0`, `i = 1`.

| i | needle[i] | needle[len] | Equal? | Action | lps after | len after | i after |
|---|-----------|-----------------|--------|--------|---------------|---------------|-------------|
| 1 | 'a' | needle[0]='s' | No | len=0, so lps[1]=0, i++ | [0,0,0] | 0 | 2 |
| 2 | 'd' | needle[0]='s' | No | len=0, so lps[2]=0, i++ | [0,0,0] | 0 | 3 |

`lps = [0,0,0]` (no self-overlap exists anywhere in "sad").

**Main scan:** `n=9`, `m=3`. Initial: `i=0, j=0`.

| i | j | haystack[i] | needle[j] | Equal? | Action | i after | j after |
|---|---|--------------|-----------|--------|--------|-------------|-------------|
| 0 | 0 | 's' | 's' | Yes | i++, j++ | 1 | 1 |
| 1 | 1 | 'a' | 'a' | Yes | i++, j++ | 2 | 2 |
| 2 | 2 | 'd' | 'd' | Yes | i++, j++; j==m(3)! return i-j=3-3=0 | — | — |

Exit condition: `j == m` is reached at `i=3, j=3`, triggering an immediate return.

**Final answer:** `0`, matching the expected output exactly — "sad" is found starting at index 0 of "sadbutsad".

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Brute force, nested loop | O(n × m) worst case | O(1) | Correct and often fast enough in practice, but not asymptotically optimal |
| 2. KMP (precomputed LPS array) | O(n + m) | O(m) | Optimal; guarantees linear time regardless of adversarial input structure |

`n` = `haystack.length()`, `m` = `needle.length()`.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| `needle` longer than `haystack` | No match is possible; must correctly return -1 without error | The main scan's `i < n` loop condition, combined with the fact that `j` can never reach `m` if there simply aren't enough characters left in `haystack` to complete a match, naturally results in the loop ending with no match found, correctly returning -1 |
| `needle` occurs at the very start of `haystack` (index 0) | Should be detected immediately without unnecessary scanning | As shown in the dry run above, the match is found and returned as soon as `j` reaches `m`, which can happen as early as index 0 if the match starts there |
| `needle` occurs at the very end of `haystack`, with no earlier occurrence | Must correctly scan through the entire non-matching prefix of `haystack` before finding the match | The KMP scan naturally continues advancing `i` and adjusting `j` via `lps` through any non-matching region, correctly finding the match whenever it actually occurs, regardless of position |
| `needle` does not occur anywhere in `haystack` at all | Must correctly return -1 after fully scanning | The main `while (i < n)` loop completes without `j` ever reaching `m`, falling through to the final `return -1;` |
| `needle` has significant internal self-overlap (e.g. `needle = "aabaa"`) | The `lps` array itself must correctly capture this overlap structure for KMP's mismatch-recovery to work correctly | The `buildLPS` helper correctly computes overlapping prefix-suffix relationships through its own internal two-pointer logic, which is exactly the mechanism that makes KMP's main scan able to skip redundant comparisons when such self-overlapping patterns are involved |
| `haystack` consists of a long run of a single repeated character, with `needle` almost but not quite matching (classic KMP adversarial case for brute force) | This is exactly the case where brute force degrades to O(n×m); KMP must remain efficient here | Because KMP's `haystack` pointer `i` never moves backward, and mismatches are resolved purely by adjusting `j` via the precomputed `lps` array, this case is handled in genuine O(n+m) time, without any of the redundant re-scanning that would slow down the brute-force approach |

## 9. Java Notes

- **`String.charAt(index)` for character access:** used throughout for direct character comparisons; straightforward and efficient for this purpose.
- **Two-pointer self-referential construction of the `lps` array:** the `buildLPS` helper itself uses a very similar mismatch-recovery technique (via `len = lps[len - 1]`) as the main scan does — recognizing this structural symmetry (the pattern is essentially matched against a shifted version of itself to build the array) is a good way to internalize why KMP works as elegantly as it does.
- **No overflow or numeric risk:** this problem involves purely character comparisons and bounded array indices; no arithmetic operations here risk overflow.
- **Note on Java's built-in `String.indexOf(String)`:** Java's standard library already provides this exact functionality; this problem exists specifically to have you implement the underlying algorithm from scratch, most notably as the standard teaching vehicle for KMP, since the brute-force nested loop, while simpler, doesn't demonstrate the more sophisticated technique this problem is designed to test.

## 10. Common Mistakes

- **Resetting `j` to 0 upon every mismatch (i.e., accidentally implementing brute force with extra bookkeeping) instead of using `lps[j-1]`.** This defeats the entire purpose of KMP, since it reintroduces the redundant re-scanning that KMP is specifically designed to avoid, degrading back to O(n × m) time in the worst case despite the extra complexity of maintaining an `lps` array. Fix: always use `j = lps[j - 1]` (when `j > 0`) upon a mismatch, never reset to 0 directly unless `j` is already 0.
- **Off-by-one errors in the `lps` array construction, particularly confusing "proper prefix" (which excludes the whole string itself) with just "any prefix."** Getting this wrong subtly corrupts the entire mismatch-recovery mechanism. Fix: carefully verify the `lps` array against small hand-worked examples (like "aabaa" or "aaaa") to build confidence in the construction logic before relying on it.
- **Forgetting to check `j == m` (a full match found) inside the main scan loop, or checking it in the wrong place relative to the pointer increments.** This could cause the algorithm to either miss returning the correct match index or to access `needle.charAt(j)` out of bounds if `j` is allowed to reach `m` without being caught. Fix: always check `j == m` immediately after incrementing both pointers upon a successful character match, before the next loop iteration begins.
- **Not handling the edge case where `needle` is empty (though the problem's constraints here guarantee `needle.length() >= 1`, so this is more of a general defensive-programming habit than a strict requirement for this specific problem).** Fix: while not strictly required by this problem's constraints, it's good practice to explicitly handle or at least consider an empty `needle` (conventionally, an empty pattern matches at index 0) when implementing substring search in a more general context.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Find the first occurrence of a substring in a string, and avoid the O(n×m) worst case → KMP: precompute the pattern's longest-prefix-that-is-also-a-suffix (LPS) array, then use it to skip redundant re-comparisons upon a mismatch."
- **90-second explanation:** "The brute-force approach compares the pattern against every possible starting position in the text, restarting from scratch after every mismatch — which can redundantly re-examine characters that were already confirmed to match, leading to O(n×m) worst-case time on adversarial inputs. KMP avoids this by first precomputing an array for the pattern itself, called the LPS array, where each entry tells you the length of the longest proper prefix of the pattern up to that point which is also a suffix of it. Then, during the main scan through the text, whenever a mismatch occurs partway through a comparison, instead of resetting my pattern pointer all the way back to the start, I use the LPS array to jump it forward to the next position that's guaranteed to still be consistent with everything already matched — without ever needing to move my text pointer backward. This means every character in the text is examined only a bounded number of times overall, giving a genuine O(n+m) time guarantee regardless of how adversarial the input is, at the cost of O(m) extra space to store the LPS array."
- **Related problems using this pattern:**
  - LeetCode 459 — Repeated Substring Pattern (also solvable using the KMP LPS array's properties)
  - LeetCode 214 — Shortest Palindrome (uses KMP's LPS construction technique)
  - LeetCode 1392 — Longest Happy Prefix (directly asks for the LPS array's final value)

## 12. Recall Questions

**Q:** Why does resetting the pattern pointer to `lps[j-1]` upon a mismatch, instead of resetting it to 0, avoid redundant re-scanning of the text?
**A:** The characters already matched before the mismatch contain within them a smaller prefix of the pattern that's guaranteed to already be correctly aligned (as captured by the LPS array), so jumping directly to that known-good position skips having to re-verify characters that are already confirmed consistent, without ever needing to move the text pointer backward.

**Q:** Why is the text pointer (`i`, scanning through `haystack`) guaranteed to never move backward during KMP's main scan?
**A:** All the mismatch-recovery work is handled entirely by adjusting the pattern pointer `j` via the precomputed LPS array; the text pointer only ever advances forward (or stays in place for one retry) upon a mismatch, which is precisely what guarantees the overall linear O(n) bound on the text-scanning portion of the algorithm.

**Q:** What does `lps[i]` represent, and why is the qualifier "proper" (excluding the whole substring itself) important in its definition?
**A:** `lps[i]` represents the length of the longest prefix of `needle[0..i]` that is also a suffix of that same substring, excluding the trivial case of the entire substring matching itself — without this exclusion, the value would always trivially equal the substring's own full length, providing no useful information for the mismatch-recovery mechanism.

**Q:** Why does the brute-force nested-loop approach specifically struggle with inputs like a long run of a repeated character in `haystack` combined with a similarly repetitive but slightly different `needle`?
**A:** In such cases, many different starting positions in the text can partially match a large portion of the pattern before finally failing near the end, and without any mechanism to remember and reuse that partial-match information, brute force redundantly re-examines much of the same data at each new starting position, compounding into O(n×m) total work.

**Q:** Why does the LPS array only need to be as large as the pattern (`needle`), rather than needing any information about the text (`haystack`)?
**A:** The LPS array captures a purely structural, self-referential property of the pattern itself — specifically, its own internal prefix-suffix overlaps — which is entirely independent of whatever text it will later be searched against, so it only needs to be computed once per pattern and reused throughout the entire scan of the text.

## 13. Final Code

```java
public int strStr(String haystack, String needle) {
    int n = haystack.length();
    int m = needle.length();
    if (m == 0) {
        return 0;
    }

    int[] lps = buildLPS(needle);

    int i = 0;
    int j = 0;
    while (i < n) {
        if (haystack.charAt(i) == needle.charAt(j)) {
            i++;
            j++;
            if (j == m) {
                return i - j;
            }
        } else if (j > 0) {
            j = lps[j - 1];
        } else {
            i++;
        }
    }

    return -1;
}

private int[] buildLPS(String needle) {
    int m = needle.length();
    int[] lps = new int[m];
    int len = 0;
    int i = 1;

    while (i < m) {
        if (needle.charAt(i) == needle.charAt(len)) {
            len++;
            lps[i] = len;
            i++;
        } else if (len > 0) {
            len = lps[len - 1];
        } else {
            lps[i] = 0;
            i++;
        }
    }

    return lps;
}
```

## 14. Self-Test

You have two strings, `haystack` and `needle`. Return the index of the first occurrence of `needle` in `haystack`, or -1 if it doesn't occur. Think about why precomputing a "longest prefix that is also a suffix" array for the pattern lets you skip redundant re-comparisons in the text upon a mismatch, guaranteeing linear time overall.
