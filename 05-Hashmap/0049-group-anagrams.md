---
problem: 49 - Group Anagrams
url: https://leetcode.com/problems/group-anagrams/
difficulty: Medium
section: String / Hash Map
patterns: [canonical-key-grouping, character-count-signature]
data_structures: [hashmap, array, string]
time: O(n*k)
space: O(n*k)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** An array of strings `strs`.

**Required:** Group the strings such that all anagrams (strings that are rearrangements of the same letters) end up in the same group. Return the groups in any order (and elements within each group in any order).

**Constraints that actually matter:**
- `1 <= strs.length <= 10^4` and `0 <= strs[i].length <= 100` — up to 10,000 strings, each up to 100 characters — an O(n²) pairwise-comparison approach (comparing every string to every other string) would risk up to `~5*10^7` comparisons, each potentially O(k) itself, likely too slow; a hash-map-based grouping in roughly O(n·k) total is expected.
- **`strs[i]` consists of only lowercase English letters** — this fixed, small (26-character) alphabet is the key detail that enables an even more efficient "canonical key" than sorting each string: a fixed-size character-frequency count array can serve as a unique signature for any anagram group, computable in O(k) per string rather than O(k log k) for sorting.
- **Anagrams share exactly the same multiset of characters** — this is the defining property being exploited: any two strings are anagrams of each other if and only if they have identical character counts (or, equivalently, identical sorted forms) — so grouping by some canonical representation of "the multiset of characters" directly solves the problem.

## 2. Recognition Signals

- "Group strings that are anagrams of each other" → the classic **canonical-key grouping** technique: compute some transformation of each string that is *identical* for all anagrams of each other and *different* for non-anagrams, then use a hash map keyed by that transformation to bucket strings into their groups.
- The two standard choices for the canonical key are: **(a) the sorted version of the string** (simple, works for any alphabet, O(k log k) per string), or **(b) a character-frequency count signature** (more efficient when the alphabet is small and fixed, like the 26 lowercase letters here, O(k) per string) — recognizing that the fixed small alphabet specifically enables option (b) as a genuine efficiency upgrade over the more generally-applicable option (a) is the key insight distinguishing a "good" solution from an "optimal" one for this specific problem.
- Whenever a problem asks to "group" or "bucket" items by some equivalence relation (anagram-ness, in this case) — using a hash map from a canonical representative of each equivalence class to a list of members is the standard, broadly reusable technique.

**Pattern:** Canonical-Key Grouping via Hash Map (map each string to a canonical representation that's identical for all its anagrams, then group strings sharing that same canonical key) — using a character-frequency-count signature as the specific canonical key, exploiting the fixed small alphabet for better-than-sorting efficiency.

## 3. Core Idea

Two strings are anagrams of each other if and only if they contain exactly the same multiset of characters — the same letters, in the same quantities, regardless of order. So, any transformation that maps every string to a value uniquely determined by (and only by) its multiset of characters can serve as a "canonical key": all anagrams will map to the identical key, and no two non-anagram strings will ever collide on the same key. Using a `Map<String, List<String>>` (or similar), compute this canonical key for each string in `strs`, and append the string to the list associated with that key. After processing all strings, the map's values are exactly the desired anagram groups.

The most straightforward canonical key is the **sorted version of the string** (e.g., "eat" and "tea" and "ate" all sort to "aet"). A more efficient alternative, given the fixed 26-letter lowercase alphabet, is a **character-count signature**: build a length-26 array counting occurrences of each letter, then convert that fixed-size array into a single string (or other hashable form) to use as the map key — this avoids the O(k log k) sorting cost per string, achieving O(k) per string instead.

**Invariant:** Two strings produce the same canonical key if and only if they are anagrams of each other, so after processing every string in `strs`, each list of values grouped under a single key in the map is exactly one complete, correct anagram group, and every string belongs to exactly one such group.

## 4. Approach 1 — Sorted-String Key (General, Works for Any Alphabet)

**Logic:** For each string, sort its characters to produce a canonical form (e.g., "eat" → "aet"), and use that sorted string as the hash map key, grouping original strings under their shared sorted-key bucket.

```java
public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> groups = new HashMap<>();

    for (String s : strs) {
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        String key = new String(chars);

        groups.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
    }

    return new ArrayList<>(groups.values());
}
```

- **Time:** O(n × k log k), where `n = strs.length` and `k` is the maximum string length — sorting each string's characters costs O(k log k), repeated across all `n` strings.
- **Space:** O(n × k) — the map stores all `n` strings (or their sorted-key copies) across all groups, proportional to the total input size.

**Why it is not optimal:** This is correct and works for any alphabet, not just lowercase English letters, but the O(k log k) sorting cost per string is unnecessary overhead given that the problem's fixed 26-letter alphabet enables a more efficient O(k) canonical key via direct character counting instead of sorting.

## 5. Approach 2 — Optimized (Character-Count Signature Key)

**Algorithm:**
1. For each string `s` in `strs`:
   - Build a length-26 `int[]` array, counting the occurrences of each lowercase letter in `s` (incrementing `counts[c - 'a']` for each character `c`).
   - Convert this fixed-size count array into a canonical string key (e.g., by joining the counts with a delimiter, such as `"1#0#0#...#2"` for 26 comma/hash-separated numbers, ensuring no ambiguity between different count arrays).
   - Append `s` to the list associated with this key in the map (using `computeIfAbsent` for concise bucket creation).
2. Return the map's values as the final list of groups.

```java
public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> groups = new HashMap<>();

    for (String s : strs) {
        int[] counts = new int[26];
        for (char c : s.toCharArray()) {
            counts[c - 'a']++;
        }

        StringBuilder keyBuilder = new StringBuilder();
        for (int count : counts) {
            keyBuilder.append(count).append('#');
        }
        String key = keyBuilder.toString();

        groups.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
    }

    return new ArrayList<>(groups.values());
}
```

- **Time:** O(n × k) — building the character-count array and the resulting key string is O(k) per string (26 fixed iterations for the array, plus O(k) to scan the string itself), repeated across all `n` strings, with no sorting overhead.
- **Space:** O(n × k) — same overall space as Approach 1 for storing the grouped strings themselves, plus O(26) = O(1) additional per-string working space for the count array, which doesn't change the asymptotic total.

**Why this is optimal:** Every character of every string must be examined at least once to determine its contribution to the anagram signature, so O(n × k) is a natural lower bound on time, and this approach achieves it directly — a genuine asymptotic improvement over Approach 1's O(n × k log k), specifically enabled by the problem's fixed, small alphabet allowing a counting-based signature instead of a sorting-based one. Space remains O(n × k) in both approaches for storing the actual grouped output strings, which is unavoidable regardless of the specific keying strategy used.

## 6. Dry Run

Example: `strs = ["eat","tea","tan","ate","nat","bat"]`.
Chosen because it's the canonical example, and it clearly shows multiple strings correctly converging to the same character-count key despite differing surface order.

Processing each string (showing the count array in `a,b,...,z` order, only non-zero entries listed for brevity, plus the resulting delimited key):

| s | Non-zero counts (letter:count) | Key (abbreviated) | Bucket after insertion |
|---|-------------------------------------|---------------------------|-------------------------------|
| "eat" | a:1, e:1, t:1 | "1#0#...#1#...#1#..." | {K1: ["eat"]} |
| "tea" | a:1, e:1, t:1 | same as "eat"'s key (K1) | {K1: ["eat","tea"]} |
| "tan" | a:1, n:1, t:1 | different key (K2) | {K1:[...], K2:["tan"]} |
| "ate" | a:1, e:1, t:1 | same as K1 | {K1:["eat","tea","ate"], K2:["tan"]} |
| "nat" | a:1, n:1, t:1 | same as K2 | {K1:[...], K2:["tan","nat"]} |
| "bat" | a:1, b:1, t:1 | different key (K3) | {K1:[...], K2:[...], K3:["bat"]} |

Exit condition: all six strings processed.

**Final answer:** `[["eat","tea","ate"], ["tan","nat"], ["bat"]]` (order of groups and order within groups may vary, but the grouping itself matches the expected output exactly — "bat" alone, "nat"/"tan" together, "ate"/"eat"/"tea" together).

## 7. Complexity Summary

| Approach | Time | Space (Auxiliary) | Notes |
|----------|------|--------------------|-------|
| 1. Sorted-string key | O(n·k log k) | O(n·k) | Correct and general (works for any alphabet), but sorting each string is unnecessary overhead here |
| 2. Character-count signature key | O(n·k) | O(n·k) | Optimal; exploits the fixed 26-letter lowercase alphabet to avoid sorting entirely |

`n = strs.length`, `k` = maximum string length. Output space (the grouped strings themselves) is O(n·k) in both, as it must hold every input string exactly once across all groups.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| A single empty string in the array (e.g. `strs = [""]`, example 2) | An empty string has an all-zero character-count signature; must still be grouped correctly (as its own singleton group) | The count array for `""` is simply all zeros (the `for (char c : s.toCharArray())` loop never executes), producing a valid, consistent key that correctly maps to a bucket containing just that one empty string |
| A single-character string (e.g. `strs = ["a"]`, example 3) | Trivial case; should form its own singleton group unless another identical single-character string exists | The count array correctly reflects a count of 1 for that one letter and 0 for all others, producing a key that groups it correctly (alone, if no other anagram exists in the input) |
| Multiple empty strings in the input | All empty strings are trivially anagrams of each other (and only of each other) | Every empty string produces the identical all-zero count key, so they all correctly land in the same single group together |
| Strings that are permutations of each other but of different original order (the core case the problem is built around) | Must correctly recognize these as anagrams despite differing character sequences | Since the character-count signature only depends on *how many* of each letter appear, not their order, any permutation of the same multiset of letters produces an identical key, correctly grouping them together |
| Strings with no anagram partners at all in the input (e.g. "bat" in the main example) | Should correctly form their own singleton group, not be incorrectly merged with unrelated strings | Since "bat" contains a genuinely different multiset of letters than any other string in the example, its count-array key is unique among the input, so `computeIfAbsent` correctly creates a new singleton bucket for it |
| Maximum input size (`n=10^4` strings, each up to `k=100` characters) | Should still perform efficiently at the upper bound of the constraints | The O(n·k) time complexity of the optimized approach comfortably handles the worst-case input size (`10^4 * 100 = 10^6` total character operations), well within typical time limits |

## 9. Java Notes

- **`Map.computeIfAbsent(key, k -> new ArrayList<>())`:** a concise, idiomatic way to either retrieve the existing list for a key or atomically create and insert a new empty list if the key isn't present yet, avoiding a more verbose manual `containsKey`/`get`/`put` sequence.
- **Building the character-count key as a delimited string (e.g., using `'#'` as a separator between counts):** the delimiter is important to avoid ambiguity — without it, count sequences like `[1, 23]` and `[12, 3]` could both naively stringify to `"123"`, incorrectly colliding; using a clear separator between each count value avoids this ambiguity entirely.
- **`char - 'a'` for mapping a lowercase letter to a 0-25 array index:** a standard, efficient idiom for indexing into a fixed-size array by lowercase letter, relying on the contiguous ASCII ordering of `'a'` through `'z'`.
- **`Arrays.sort(char[])` (used in Approach 1):** sorts a primitive `char` array in place; used to produce the canonical sorted-string key in the more general (but less efficient, for this specific problem) sorting-based approach.

## 10. Common Mistakes

- **Using a naive, unseparated concatenation of counts as the key (e.g., simply appending each count directly without any delimiter).** As noted in the Java Notes, this can cause different count arrays to collide into the same ambiguous string key, incorrectly merging non-anagram groups together. Fix: always include an unambiguous delimiter (like `'#'`) between each count value when building the key string.
- **Forgetting that the sorted-string key approach (Approach 1), while correct, incurs unnecessary O(k log k) sorting overhead per string given the problem's small, fixed alphabet.** This isn't a correctness bug, but it's a missed opportunity for the more efficient character-count-based solution that's typically expected as the optimal answer for this well-known problem. Fix: recognize the fixed 26-letter lowercase alphabet as the specific signal to use a character-count signature instead of sorting.
- **Attempting to use the raw `int[26]` count array directly as a `HashMap` key without converting it to a proper hashable/equatable representation (like a `String` or a wrapped object with correct `equals`/`hashCode`).** Java arrays use reference-based equality and hashing by default, so two different `int[]` instances with identical contents would *not* be treated as equal keys, silently breaking the grouping logic. Fix: always convert the count array into a `String` (or use `Arrays.hashCode`/a wrapper class with proper `equals`/`hashCode`) before using it as a map key.
- **Not handling the empty string case correctly**, though as shown in the edge cases, the given algorithm handles it naturally without needing special code — but it's worth explicitly verifying this behavior rather than assuming it, since an implementation that manually pre-checks string length before processing could accidentally skip or mishandle empty strings if not careful. Fix: ensure the character-counting loop naturally and correctly produces an all-zero signature for an empty string, with no special-casing required.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Group strings that are anagrams of each other → hash map keyed by a canonical representation of each string's character multiset (sorted string, or, better, a character-count signature given a small fixed alphabet)."
- **90-second explanation:** "Two strings are anagrams if and only if they contain exactly the same letters in the same quantities, just possibly in a different order. So I need some transformation of each string that captures 'what letters, how many of each' while ignoring order — that transformation becomes my grouping key in a hash map. The simplest version sorts each string's characters, since any two anagrams sort to the identical string, but that costs O(k log k) per string. Since this problem's strings only ever contain lowercase English letters, a fixed 26-letter alphabet, I can do better: for each string, I build a fixed-size array counting how many of each letter it contains, and convert that count array into a string key, using a separator between counts to avoid any ambiguity. This only costs O(k) per string, since it's just one pass through the string's characters plus a fixed 26 iterations to build the key, avoiding the sorting overhead entirely. I use `computeIfAbsent` to cleanly bucket each original string under its computed key, and at the end, the map's collection of values is exactly the grouped anagram lists I need to return."
- **Related problems using this pattern:**
  - LeetCode 242 — Valid Anagram (the simpler, single-pair version of the same core character-counting idea)
  - LeetCode 438 — Find All Anagrams in a String (a sliding-window variant using a similar character-count comparison)
  - LeetCode 76 — Minimum Window Substring (different objective, but shares the character-frequency-matching technique)

## 12. Recall Questions

**Q:** Why does a character-count signature work as a valid canonical key for grouping anagrams, in the same way a sorted string does?
**A:** Two strings are anagrams of each other precisely when they contain the same letters in the same quantities, and both the sorted-string form and the character-count array are transformations that depend only on that multiset of letters, not on their original order, so any two anagrams will always produce identical values under either transformation.

**Q:** Why is a character-count signature more efficient than a sorted-string key specifically for this problem, but not necessarily for anagram-grouping problems over an arbitrary, unbounded alphabet?
**A:** The character-count approach relies on iterating over a small, fixed alphabet (the 26 lowercase letters) to build a fixed-size count array in O(k) time, whereas sorting takes O(k log k) regardless of alphabet size — but if the alphabet were very large or unbounded, building a full fixed-size count array might become impractical or lose its efficiency advantage, making the more general sorting-based approach preferable in that scenario.

**Q:** Why is it necessary to include a delimiter between count values when converting the count array into a string key, rather than just concatenating the numbers directly?
**A:** Without a delimiter, different count sequences could produce identical concatenated strings due to ambiguity in where one number ends and the next begins (for example, count sequences of [1,23] and [12,3] could both become the string "123"), incorrectly causing non-anagram groups to collide under the same key.

**Q:** Why can't a raw `int[]` array be used directly as a `HashMap` key without first converting it to some other representation?
**A:** Java's default array equality and hashing are based on object reference identity, not the actual contents of the array, so two different array instances containing identical count values would incorrectly be treated as different, distinct keys unless explicitly converted into a content-based representation like a `String`.

**Q:** How would you expect this algorithm's performance to change if the input strings could contain uppercase letters, digits, or other characters in addition to lowercase letters?
**A:** The character-count array would need to be sized to accommodate the full range of possible characters rather than just 26 lowercase letters, and if that range became very large, the per-string cost of building and comparing the count-based key could become less advantageous relative to the more universally applicable sorted-string key approach.

## 13. Final Code

```java
public List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> groups = new HashMap<>();

    for (String s : strs) {
        int[] counts = new int[26];
        for (char c : s.toCharArray()) {
            counts[c - 'a']++;
        }

        StringBuilder keyBuilder = new StringBuilder();
        for (int count : counts) {
            keyBuilder.append(count).append('#');
        }
        String key = keyBuilder.toString();

        groups.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
    }

    return new ArrayList<>(groups.values());
}
```

## 14. Self-Test

You have an array of strings `strs`. Group all strings that are anagrams of each other. Think about why a fixed-size character-count array, converted into a string key, serves as a canonical signature for the anagram relationship — and why this is more efficient than sorting each string, specifically because the alphabet here is small and fixed.
