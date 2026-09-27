---
problem: 125 - Valid Palindrome
url: https://leetcode.com/problems/valid-palindrome/
difficulty: Easy
section: String / Two Pointers
patterns: [two-pointers, in-place-skip]
data_structures: [string]
time: O(n)
space: O(1)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** A string `s` of printable ASCII characters.

**Required:** Return whether `s`, after lowercasing all letters and removing all non-alphanumeric characters, reads the same forward and backward.

**Constraints that actually matter:**
- `1 <= s.length <= 2 * 10^5` — large enough that any approach re-scanning or re-allocating substrings excessively could add unnecessary overhead, though a single clean linear pass comfortably handles this size.
- `s` consists of only printable ASCII characters — a broad character set, meaning punctuation, spaces, and mixed case are all valid input and must be actively filtered out or normalized, not just letters and digits.
- An **empty string after filtering** (e.g., input `" "`) is explicitly defined as a valid palindrome — this is a deliberate hint that the "no characters left to compare" case should trivially succeed, not be treated as an error or edge case requiring special rejection.

## 2. Recognition Signals

- "Reads the same forward and backward, after normalizing case and filtering characters" → the classic **two-pointer palindrome check**, adapted with an extra filtering step: skip past invalid characters as you go, rather than filtering first and checking afterward.
- Whenever a palindrome check needs to ignore certain characters while comparing from both ends — converging inward two-pointer style, with each pointer independently skipping over "ignorable" characters before performing the actual comparison, avoids the overhead of building a separate cleaned-up string first.
- The mention of needing to strip characters *and* normalize case simultaneously, while checking symmetry, is itself a signal that doing this in a single combined pass (rather than multiple separate passes: one to clean, one to check) is both more efficient and a common expectation for this kind of problem.

**Pattern:** Two Pointers with In-Place Character Skipping (converge two pointers from both ends toward the middle, each independently advancing past any non-alphanumeric characters before comparing, and comparing case-insensitively).

## 3. Core Idea

Rather than first building a separate cleaned-up string (lowercase, alphanumeric-only) and then checking if *that* string is a palindrome with a standard two-pointer scan, do both steps simultaneously: maintain a `left` pointer starting at the beginning and a `right` pointer starting at the end of the original string. Before each comparison, advance `left` forward past any non-alphanumeric characters, and advance `right` backward past any non-alphanumeric characters. Once both pointers rest on alphanumeric characters (or the pointers cross, meaning all characters have been accounted for), compare the characters at `left` and `right` case-insensitively; if they differ, the string isn't a palindrome. Continue moving both pointers inward and repeating until they meet or cross.

**Invariant:** At the start of each iteration, everything strictly outside the `[left, right]` window has already been confirmed to correctly mirror its counterpart (or was skipped as non-alphanumeric on both sides), so the string is a valid palindrome if and only if every character comparison made as `left` and `right` converge succeeds, all the way until they meet or cross.

## 4. Approach 1 — Build a Cleaned String, Then Check (Two-Pass)

**Logic:** First, build an entirely new string containing only the lowercased alphanumeric characters from `s`. Then, check whether that cleaned string is a palindrome using a standard two-pointer scan (or by reversing it and comparing).

```java
public boolean isPalindrome(String s) {
    StringBuilder cleaned = new StringBuilder();
    for (char c : s.toCharArray()) {
        if (Character.isLetterOrDigit(c)) {
            cleaned.append(Character.toLowerCase(c));
        }
    }

    int left = 0;
    int right = cleaned.length() - 1;
    while (left < right) {
        if (cleaned.charAt(left) != cleaned.charAt(right)) {
            return false;
        }
        left++;
        right--;
    }
    return true;
}
```

- **Time:** O(n) — one pass to build the cleaned string, one pass to check it (each O(n)).
- **Space:** O(n) auxiliary for the `cleaned` `StringBuilder`, holding a filtered copy of the string.

**Why it is not optimal:** It's already O(n) time, matching the optimal, but it uses O(n) auxiliary space to store an entirely separate cleaned copy of the string, when the check can be performed directly on the original string without ever materializing that intermediate copy.

## 5. Approach 2 — Optimized (Two Pointers with In-Place Skipping, Single Pass)

**Algorithm:**
1. Initialize `left = 0` and `right = s.length() - 1`.
2. While `left < right`:
   - Advance `left` forward while `s.charAt(left)` is not alphanumeric (and `left < right`, to avoid overshooting).
   - Advance `right` backward while `s.charAt(right)` is not alphanumeric (and `left < right`).
   - If `left < right`, compare `Character.toLowerCase(s.charAt(left))` to `Character.toLowerCase(s.charAt(right))`; if they differ, return `false`.
   - Advance `left` forward and `right` backward by one each, continuing the loop.
3. If the loop completes without finding a mismatch, return `true`.

```java
public boolean isPalindrome(String s) {
    int left = 0;
    int right = s.length() - 1;

    while (left < right) {
        while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
            left++;
        }
        while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
            right--;
        }

        if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
            return false;
        }

        left++;
        right--;
    }

    return true;
}
```

- **Time:** O(n) — each pointer moves monotonically inward across the string, so the total number of character examinations across both pointers combined is bounded by `n`, regardless of how many characters are skipped versus compared.
- **Space:** O(1) auxiliary — only the two pointer variables, no intermediate string or buffer is ever created.

**Why this is optimal:** Every character must be examined at least once (either to skip it as non-alphanumeric or to compare it), so O(n) is a hard lower bound. Space is O(1) because the filtering and comparison happen simultaneously on the original string via pointer movement, with no need to ever materialize a separate cleaned-up copy — this is a direct improvement over Approach 1's O(n) auxiliary space, achieving the same O(n) time with no extra memory overhead.

## 6. Dry Run

Example: `s = "A man, a plan, a canal: Panama"`.
Chosen because it's the canonical example, and it exercises multiple non-alphanumeric characters being skipped on both sides throughout the scan, along with case normalization.

`s.length() = 31`. Initial: `left = 0`, `right = 30`.

| Iteration | left skip result | right skip result | s[left] (lower) | s[right] (lower) | Match? | left/right after |
|-----------|----------------------|------------------------|----------------------|------------------------|--------|--------------------------|
| 1 | left=0 ('A') | right=30 ('a') | 'a' | 'a' | Yes | left=1, right=29 |
| 2 | left=1 (' ')→2 ('m') | right=29 ('m') | 'm' | 'm' | Yes | left=3, right=28 |
| 3 | left=3 ('a') | right=28 ('a') | 'a' | 'a' | Yes | left=4, right=27 |
| 4 | left=4 ('n') | right=27 ('n') | 'n' | 'n' | Yes | left=5, right=26 |
| 5 | left=5 (',')→6 (' ')→7 ('a') | right=26 ('a') | 'a' | 'a' | Yes | left=8, right=25 |
| ... | (continues symmetrically, skipping spaces and the colon and comma as encountered) | | | | | |

(The scan continues symmetrically through "plan," / "canal:" on the left and "Panama" / "canal" on the right, with every corresponding letter matching after case normalization and punctuation/space skipping, all the way to the middle.)

Exit condition: `left` and `right` pointers meet or cross in the middle, having successfully matched every alphanumeric character pair along the way, with no mismatch ever found.

**Final answer:** `true`, matching the expected output exactly — the cleaned string "amanaplanacanalpanama" is indeed a palindrome.

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Build cleaned string, then check | O(n) | O(n) | Correct but materializes an unnecessary intermediate copy |
| 2. Two pointers with in-place skipping | O(n) | O(1) | Optimal; filters and compares simultaneously on the original string |

Input space for `s` is O(n) in both, as given by the problem.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| String reduces to empty after filtering (e.g. `s = " "`) | Explicitly defined as a valid palindrome by the problem, not an error case | If every character is non-alphanumeric, both skip-loops advance `left` and `right` toward each other until `left >= right`, at which point the outer `while (left < right)` loop condition becomes false immediately (or on the first iteration), so the function falls through to `return true;` without ever needing a special case |
| Single alphanumeric character surrounded by non-alphanumeric characters (e.g. `s = "!a!"`) | Should trivially be a palindrome (one character always reads the same forward and backward) | After skipping the surrounding punctuation, `left` and `right` both converge onto the single character (or `left == right`), and the loop condition `left < right` becomes false before or immediately upon comparison, correctly returning `true` without needing to compare a character to itself explicitly in a problematic way |
| Mixed case letters that should be treated as equal (e.g. `'A'` vs `'a'`) | Case sensitivity must be normalized before comparison | `Character.toLowerCase()` is applied to both characters immediately before comparing them, ensuring case differences never cause a false mismatch |
| Digits mixed with letters (e.g. `"0P"` type scenarios) | Digits count as alphanumeric and must be included in the comparison, not filtered out like punctuation | `Character.isLetterOrDigit(c)` correctly identifies both letters and digits as characters to keep, treating them uniformly in the skip logic |
| Non-alphanumeric characters occurring consecutively on one side (e.g. multiple punctuation marks in a row) | The skip loop must correctly advance past all of them, not just one | The `while (left < right && !Character.isLetterOrDigit(s.charAt(left))) left++;` loop continues advancing as long as the condition holds, correctly skipping any number of consecutive non-alphanumeric characters, not just a single one |
| A genuine non-palindrome with mismatched characters after filtering (e.g. `"race a car"`) | Must correctly detect the mismatch and return `false` promptly | As soon as `Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))` evaluates to true at any point during the scan, the function returns `false` immediately without needing to continue examining the rest of the string |

## 9. Java Notes

- **`Character.isLetterOrDigit(char)`:** a convenient built-in check that correctly identifies alphanumeric characters (both letters and digits) without needing to manually check separate ranges or use multiple conditions.
- **`Character.toLowerCase(char)`:** applied individually to each character just before comparison, rather than lowercasing the entire string upfront — this avoids any unnecessary allocation, since only the two characters actually being compared at each step need normalization, not the whole string.
- **Nested `while` loops for skipping non-alphanumeric characters on each side:** each inner skip loop includes the `left < right` bound check to prevent the pointers from crossing or going out of bounds while skipping, which is essential since a string might have all its remaining unskipped characters be non-alphanumeric on one side.
- **No `StringBuilder` or intermediate string needed:** unlike Approach 1, the optimized solution operates directly on `s` via `charAt`, never constructing any new string object, which is the key to achieving O(1) auxiliary space.

## 10. Common Mistakes

- **Forgetting the `left < right` bound check inside the inner skip-loops.** Without this guard, if a string like `"a,"` (or one where the remaining characters on one side are all non-alphanumeric) is processed, the skip loop could advance a pointer out of the valid array bounds, causing a `StringIndexOutOfBoundsException`. Fix: always include `left < right` as part of the skip-loop's condition, not just the alphanumeric check.
- **Comparing characters without normalizing case first.** Directly comparing `s.charAt(left) != s.charAt(right)` without applying `Character.toLowerCase()` to both would incorrectly treat `'A'` and `'a'` as different characters, producing false negatives for otherwise valid palindromes. Fix: always lowercase (or uppercase, consistently) both characters immediately before comparing them.
- **Building an entirely new filtered string first (Approach 1) without recognizing that the filtering and comparison can be combined into a single pass with two pointers.** This isn't incorrect, but it's a missed opportunity for the cleaner, more space-efficient solution that's typically expected for this well-known problem. Fix: prefer the combined single-pass, two-pointer approach with in-place skipping for O(1) auxiliary space.
- **Using `String.replaceAll` with a regex to strip non-alphanumeric characters, without considering the performance and space cost of regex-based filtering for very large inputs.** While functionally correct, this incurs both O(n) space for the filtered string and additional regex engine overhead, which is unnecessary given the simpler direct-scan alternative. Fix: prefer direct character-by-character checks (`Character.isLetterOrDigit`) over regex-based string transformation for this kind of straightforward filtering task.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Check if a string is a palindrome after ignoring case and non-alphanumeric characters → two pointers converging from both ends, each skipping non-alphanumeric characters in place before comparing case-insensitively."
- **90-second explanation:** "Instead of first building a separate cleaned-up version of the string and then checking if that's a palindrome, I do both steps at once. I use two pointers, one starting at the beginning and one at the end of the original string. Before comparing, I advance each pointer past any non-alphanumeric characters it's currently sitting on — the left pointer moves forward, the right pointer moves backward. Once both pointers rest on actual alphanumeric characters, I compare them after normalizing case, and if they don't match, I know immediately it's not a palindrome. I keep moving both pointers inward and repeating this process until they meet or cross in the middle, at which point, if no mismatch was ever found, the string is confirmed to be a palindrome. This approach avoids ever needing to allocate a separate filtered copy of the string, achieving O(n) time with O(1) extra space."
- **Related problems using this pattern:**
  - LeetCode 680 — Valid Palindrome II (allows deleting one character, extends this two-pointer approach)
  - LeetCode 234 — Palindrome Linked List
  - LeetCode 5 — Longest Palindromic Substring (different, more complex technique, but related palindrome-checking family)

## 12. Recall Questions

**Q:** Why is it possible to check the palindrome property directly on the original string, without first building a separate filtered/lowercased copy?
**A:** Because the two pointers can independently skip over non-alphanumeric characters as they converge, and case can be normalized on just the two characters being compared at each step, the filtering and comparison can happen simultaneously in a single pass rather than requiring two separate passes.

**Q:** Why must the `left < right` bound check be included inside the inner skip-loops, not just in the outer loop?
**A:** Without that guard, a skip-loop could advance its pointer past the other pointer's current position (or entirely out of the string's valid index range) if the remaining characters on one side happen to all be non-alphanumeric, risking either an incorrect comparison or an out-of-bounds array access.

**Q:** Why is an empty string (after filtering) correctly treated as a valid palindrome by this algorithm, without any special-case code?
**A:** If every character in the string is non-alphanumeric, both pointers naturally converge toward each other via the skip loops until they meet or cross, at which point the outer loop's `left < right` condition becomes false, and the function falls through to returning `true` — the same general logic that handles any other converging-pointers scenario also correctly covers this case.

**Q:** What would go wrong if character comparisons were made without first normalizing case using `Character.toLowerCase`?
**A:** Two characters that are the same letter but differ only in case, like 'A' and 'a', would be treated as unequal, causing the algorithm to incorrectly report a mismatch and return false for strings that should actually be considered valid palindromes once case is properly ignored.

**Q:** How does this algorithm's approach to filtering non-alphanumeric characters differ conceptually from using a regex-based string replacement to strip them out first?
**A:** Rather than transforming the entire string into a new, separate filtered string upfront (which requires additional space and potentially regex engine overhead), this algorithm skips over unwanted characters on the fly, directly within the same pass used to compare characters, avoiding any intermediate string allocation entirely.

## 13. Final Code

```java
public boolean isPalindrome(String s) {
    int left = 0;
    int right = s.length() - 1;

    while (left < right) {
        while (left < right && !Character.isLetterOrDigit(s.charAt(left))) {
            left++;
        }
        while (left < right && !Character.isLetterOrDigit(s.charAt(right))) {
            right--;
        }

        if (Character.toLowerCase(s.charAt(left)) != Character.toLowerCase(s.charAt(right))) {
            return false;
        }

        left++;
        right--;
    }

    return true;
}
```

## 14. Self-Test

You have a string `s`. Return whether it's a palindrome after lowercasing all letters and removing all non-alphanumeric characters. Think about why using two pointers that independently skip non-alphanumeric characters as they converge, comparing case-insensitively, lets you check this directly on the original string without ever building a separate cleaned copy.
