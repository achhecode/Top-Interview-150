---
problem: 380 - Insert Delete GetRandom O(1)
url: https://leetcode.com/problems/insert-delete-getrandom-o1/
difficulty: Medium
section: Array / Design
patterns: [swap-with-last-element, index-map]
data_structures: [array, hashmap]
time: O(1)
space: O(n)
solved_unaided: false
confidence: 0
last_reviewed: 
next_review: 
tags: [leetcode, top-interview-150, java]
---

## 1. Problem in Simple Words

**Given:** A design problem — implement a `RandomizedSet` class supporting `insert(val)`, `remove(val)`, and `getRandom()`.

**Required:**
- `insert(val)`: add `val` if not already present; return whether it was actually inserted.
- `remove(val)`: remove `val` if present; return whether it was actually removed.
- `getRandom()`: return a uniformly random element from the current set (guaranteed non-empty when called).
- **All three operations must run in average O(1) time.**

**Constraints that actually matter:**
- Values span the full 32-bit `int` range — rules out any array-indexed-by-value trick (like a direct-address table); a hash-based structure is needed to map arbitrary values to something O(1)-addressable.
- Up to `2 * 10^5` total operations — confirms O(1) *average* per operation is genuinely required for this to run efficiently at scale; anything O(n) per operation (like `ArrayList.remove(Object)`, which does a linear scan) would be too slow in aggregate.
- **`getRandom()` needs O(1) time with uniform probability** — this is the real crux of the problem. A `HashSet` alone gives O(1) insert/remove but *no* O(1) random access (you can't index into a hash set's internal buckets meaningfully or uniformly in O(1)). This mismatch is what forces a hybrid data structure.

## 2. Recognition Signals

- "O(1) insert, O(1) delete, O(1) **uniformly random access**" all on the *same* collection → this specific combination is the signature of the **array + hash map index combo**, since no single built-in Java collection satisfies all three simultaneously.
- Recognizing that a `HashSet`/`HashMap` alone solves insert/remove but not random access (no O(1) "get the k-th element" operation exists for hash-based structures), while an `ArrayList` alone solves random access via index but not O(1) removal by value (removal by value requires either a linear scan to find it, or, if removing from the middle, shifting all subsequent elements) — the pattern's job is to combine the strengths of both while eliminating both weaknesses.
- Whenever removal-by-value from a list needs to be O(1): the trick of **swapping the target with the last element, then removing the last element**, is the standard technique that avoids any shifting.

**Pattern:** Array + Hash Map Index Combo (a design pattern: maintain a dynamic array for O(1) random access, and a hash map from value to its array index for O(1) lookup, using swap-with-last for O(1) removal).

## 3. Core Idea

Maintain two structures in sync: a resizable array (`ArrayList<Integer>`) holding all current values (this gives O(1) random access via a random index for `getRandom`), and a `HashMap<Integer, Integer>` mapping each value to its current index in that array (this gives O(1) lookup for `insert`/`remove` to check presence and find position).

The key trick that makes `remove` O(1) instead of O(n): removing an element from the *middle* of an `ArrayList` normally requires shifting every subsequent element left by one, which is O(n). Instead, **swap the element to be removed with the last element in the array**, update the map entry for the swapped element to reflect its new index, then remove the last element (which is now the target) — removing from the *end* of an `ArrayList` is O(1), since nothing needs to shift.

**Invariant:** At all times, `map.get(val)` correctly equals the index of `val` within `list`, for every `val` currently in the set — this invariant must be carefully maintained across every insert, remove, and swap operation, or lookups and removals will silently corrupt.

## 4. Approach 1 — Naive (HashSet only, or ArrayList only)

**Logic (illustrating why neither alone works):** A `HashSet<Integer>` gives O(1) insert/remove/contains, but has no operation to fetch "a random element" in O(1) — you'd have to convert it to an array or iterate to an arbitrary position, which is O(n). An `ArrayList<Integer>` alone gives O(1) random access via `get(randomIndex)`, but `remove(Object)` on an `ArrayList` performs a linear scan to find the value, then shifts all subsequent elements — both O(n).

```java
// Illustrative only — does NOT meet the O(1) requirement for all operations.
class RandomizedSet {
    private final List<Integer> list = new ArrayList<>();
    private final Random random = new Random();

    public boolean insert(int val) {
        if (list.contains(val)) {
            return false;
        }
        list.add(val);
        return true;
    }

    public boolean remove(int val) {
        return list.remove(Integer.valueOf(val)); // O(n): linear scan + shift
    }

    public int getRandom() {
        return list.get(random.nextInt(list.size()));
    }
}
```

- **Time:** `insert`/`remove` are O(n) due to `contains`/linear-scan-and-shift; `getRandom` is O(1).
- **Space:** O(n) for the list itself.

**Why it is not optimal:** `insert` and `remove` both degrade to O(n) because `ArrayList` has no O(1) way to locate a value's position — exactly the gap the hash map fills in the optimized approach.

## 5. Approach 2 — Optimized (Array + Hash Map, Swap-With-Last Removal)

**Algorithm:**
- **`insert(val)`:** If `map` already contains `val`, return `false`. Otherwise, append `val` to `list`, record its index in `map` (`map.put(val, list.size() - 1)`), and return `true`.
- **`remove(val)`:** If `map` doesn't contain `val`, return `false`. Otherwise:
  1. Look up `val`'s index, `indexToRemove`, and the index of the last element, `lastIndex`.
  2. Get the value currently at the last position, `lastVal`.
  3. Overwrite `list.set(indexToRemove, lastVal)` (move the last element into the gap).
  4. Update `map.put(lastVal, indexToRemove)` (the map must reflect `lastVal`'s new position).
  5. Remove the now-duplicate last element: `list.remove(lastIndex)` (O(1) since it's the last position).
  6. Remove `val` from `map`.
  7. Return `true`.
- **`getRandom()`:** Return `list.get(random.nextInt(list.size()))`.

```java
class RandomizedSet {
    private final List<Integer> list = new ArrayList<>();
    private final Map<Integer, Integer> indexOf = new HashMap<>();
    private final Random random = new Random();

    public boolean insert(int val) {
        if (indexOf.containsKey(val)) {
            return false;
        }
        indexOf.put(val, list.size());
        list.add(val);
        return true;
    }

    public boolean remove(int val) {
        if (!indexOf.containsKey(val)) {
            return false;
        }

        int indexToRemove = indexOf.get(val);
        int lastIndex = list.size() - 1;
        int lastVal = list.get(lastIndex);

        list.set(indexToRemove, lastVal);
        indexOf.put(lastVal, indexToRemove);

        list.remove(lastIndex);
        indexOf.remove(val);
        return true;
    }

    public int getRandom() {
        return list.get(random.nextInt(list.size()));
    }
}
```

- **Time:** O(1) average for all three operations — `insert` and `remove` rely on O(1) average `HashMap` operations plus O(1) `ArrayList` end-append/end-removal; `getRandom` is a direct O(1) indexed access.
- **Space:** O(n) auxiliary — both `list` and `indexOf` store up to `n` elements each, proportional to the current set size.

**Why this is optimal:** All three operations are required to be O(1), and this design achieves exactly that: `HashMap` provides O(1) average lookup/insert/delete by value, `ArrayList` provides O(1) indexed random access and O(1) append/removal-from-end. The swap-with-last trick is what converts what would otherwise be an O(n) middle-removal into an O(1) end-removal, which is the one piece that a naive combination of the two structures wouldn't automatically provide. Space is O(n) because both structures must hold a full copy of the current set's membership — there's no way to support O(1) random access without keeping the elements in an indexable structure, so this space usage is inherent to the problem, not a wasteful trade-off.

## 6. Dry Run

Example: following the exact sequence from the problem's own example — `insert(1)`, `remove(2)`, `insert(2)`, `getRandom()`, `remove(1)`, `insert(2)`, `getRandom()`.
Chosen because it directly exercises a full remove-then-reinsert cycle and a removal that isn't from the very end, showing the swap mechanic clearly.

| Call | list before | indexOf before | Action | list after | indexOf after | Return |
|------|--------------|-------------------|--------|--------------|-------------------|--------|
| insert(1) | [] | {} | not present, append | [1] | {1:0} | true |
| remove(2) | [1] | {1:0} | 2 not in map | [1] | {1:0} | false |
| insert(2) | [1] | {1:0} | not present, append | [1,2] | {1:0, 2:1} | true |
| getRandom() | [1,2] | {1:0, 2:1} | random index 0 or 1 | [1,2] | {1:0, 2:1} | 1 or 2 |
| remove(1) | [1,2] | {1:0, 2:1} | indexToRemove=0, lastIndex=1, lastVal=2; list.set(0,2)→[2,2]; indexOf.put(2,0); list.remove(1)→[2]; indexOf.remove(1) | [2] | {2:0} | true |
| insert(2) | [2] | {2:0} | already present | [2] | {2:0} | false |
| getRandom() | [2] | {2:0} | only index 0 available | [2] | {2:0} | 2 |

Exit condition: sequence of calls completes as specified.

**Final answer:** Outputs `[true, false, true, (1 or 2), true, false, 2]`, matching the expected output pattern from the problem statement exactly (the `getRandom` calls are randomized but constrained correctly: the first can be either 1 or 2, the second must be 2 since it's the only element left).

## 7. Complexity Summary

| Approach | insert | remove | getRandom | Space | Notes |
|----------|--------|--------|-----------|-------|-------|
| 1. ArrayList/HashSet only | O(n) or O(1)* | O(n) | O(1) or O(n)* | O(n) | *Depends on which single structure is chosen; neither alone satisfies all three in O(1) |
| 2. Array + hash map index combo | O(1) avg | O(1) avg | O(1) | O(n) | Optimal; meets the O(1) average requirement for every operation |

Space is O(n) for the current set size in both the list and the map combined — this is inherent, not a wasteful choice, since O(1) random access requires holding all elements in an indexable structure.

## 8. Edge Cases

| Case | Why it is tricky | How the code handles it |
|------|-------------------|--------------------------|
| Removing the last element in the list | The "swap with last" step would be swapping an element with itself | `indexToRemove == lastIndex` in this case; `list.set(indexToRemove, lastVal)` sets the value to itself (a no-op value-wise), `indexOf.put(lastVal, indexToRemove)` re-confirms the same mapping, and `list.remove(lastIndex)` correctly removes it — no special-casing needed since the general logic handles this correctly as a degenerate case |
| Removing the only remaining element (set becomes empty) | `getRandom()` would then be called on an empty set, but the problem guarantees this never happens | The guarantee in the constraints ("there will be at least one element... when getRandom is called") means the implementation doesn't need to defensively handle an empty-set `getRandom()` call, though `remove` down to zero elements itself works correctly via the same logic as above |
| Inserting a value that was previously removed | Must correctly treat it as a fresh insert, not confuse it with stale state | Since `remove` fully deletes the entry from `indexOf` (not just marking it inactive), a subsequent `insert` of the same value is indistinguishable from inserting a brand-new value — it's correctly appended and re-indexed |
| Repeated insert of the same value without removing in between | Second insert attempt should return `false` and change nothing | `indexOf.containsKey(val)` correctly detects the existing membership and returns `false` immediately without touching `list` or `indexOf` |
| Values at the extremes of the `int` range (`-2^31`, `2^31 - 1`) | `HashMap<Integer, Integer>` must handle boxed extreme values correctly | Standard `Integer` boxing and hashing work correctly at any `int` value, including the extremes; no special handling is needed since `HashMap` doesn't have range restrictions |
| `getRandom()` called many times in a row without any insert/remove between calls | Must consistently return uniformly random elements each time, not cache or bias results | Each call independently generates a new random index via `random.nextInt(list.size())`, so repeated calls are correctly independent and uniform |

## 9. Java Notes

- **`HashMap.containsKey` / `get` / `put` / `remove`:** all O(1) average time; this problem is a direct, practical illustration of why `HashMap`'s average-case guarantees matter for meeting a strict O(1) requirement, as opposed to `ArrayList.contains`/`remove(Object)`, which are O(n) due to linear scanning.
- **`list.remove(int index)` vs `list.remove(Object)`:** critical distinction — `ArrayList.remove(int index)` removes by position (O(1) when the index is the last one, since no shifting is needed), while `ArrayList.remove(Object o)` removes by value via a linear scan (O(n)). The optimized solution deliberately calls `list.remove(lastIndex)`, an `int`, to invoke the O(1)-at-the-end position-based overload — if `val` itself were an `Integer` object with a value coinciding with a valid index, care must be taken not to accidentally call the wrong overload (this is exactly why the code removes by `lastIndex`, always an `int`, rather than by the value being removed).
- **`java.util.Random` for uniform selection:** `random.nextInt(bound)` returns a uniformly distributed value in `[0, bound)`, which combined with direct array indexing gives exactly the uniform random selection the problem requires.
- **Autoboxing in `HashMap<Integer, Integer>` and `List<Integer>`:** since values can span the full `int` range, boxed `Integer` objects are used throughout; this introduces minor boxing overhead but no correctness issues, since `Integer.equals()` (used internally by `HashMap`/`ArrayList` for comparisons) correctly compares by value, not by reference.

## 10. Common Mistakes

- **Removing from the middle of the `ArrayList` using `list.remove(indexToRemove)` directly, without first swapping in the last element.** This triggers the standard O(n) shift-left behavior of `ArrayList.remove(int index)` for any position other than the last, defeating the O(1) requirement. Fix: always swap the target with the last element first, then remove specifically from the last position.
- **Forgetting to update `indexOf` for the swapped-in last element.** After moving `lastVal` into `indexToRemove`'s old slot, if the map isn't updated to reflect `lastVal`'s new position, future lookups or removals of `lastVal` will use a stale, incorrect index. Fix: always call `indexOf.put(lastVal, indexToRemove)` immediately after the swap, before removing the old last position.
- **Not removing the target value from `indexOf` after removing it from `list`.** This leaves a stale entry in the map pointing to an index that either no longer exists or now holds a different value, corrupting future operations. Fix: always call `indexOf.remove(val)` as the final step of a successful removal.
- **Using `list.remove(Integer.valueOf(indexToRemove))` instead of `list.remove(indexToRemove)` (an `int`).** Passing a boxed `Integer` to `ArrayList.remove` invokes the by-value overload (linear scan for a matching *value*), not the by-index overload, which is exactly the bug this pattern is designed to avoid. Fix: always pass a primitive `int` index to trigger the correct overload.

## 11. Interview Takeaway

- **Trigger sentence to memorize:** "Need O(1) insert, remove, AND uniformly random access on the same collection → combine an ArrayList (for O(1) random indexing) with a HashMap from value to index (for O(1) lookup), and use swap-with-last to make removal O(1) too."
- **90-second explanation:** "Neither a HashSet nor an ArrayList alone satisfies all three requirements: a HashSet gives O(1) insert/remove but no O(1) way to fetch a uniformly random element, while an ArrayList gives O(1) random access but O(n) removal-by-value. So I combine them: an ArrayList holds the actual values for O(1) indexed access, and a HashMap maps each value to its current index in that list, giving O(1) lookup. The key trick for O(1) removal is that removing from the *end* of an ArrayList is O(1), but removing from the *middle* requires an O(n) shift — so instead of removing the target directly, I swap it with the last element in the list, update the map to reflect the swapped element's new position, and then remove what's now the last element. Insert is straightforward: append to the list and record its index in the map. GetRandom just picks a uniformly random valid index and returns that list position directly."
- **Related problems using this pattern:**
  - LeetCode 381 — Insert Delete GetRandom O(1) - Duplicates allowed
  - LeetCode 146 — LRU Cache (different combo pattern, but similarly pairs a hash map with another structure for O(1) operations)
  - LeetCode 895 — Maximum Frequency Stack (also combines a hash map with auxiliary structures for O(1) operations)

## 12. Recall Questions

**Q:** Why does neither a plain `HashSet` nor a plain `ArrayList` alone satisfy all three required operations in O(1)?
**A:** A `HashSet` has no O(1) way to retrieve an arbitrary/random element since its internal structure isn't indexable, while an `ArrayList` has no O(1) way to locate or remove an arbitrary value without either a linear scan or shifting subsequent elements.

**Q:** Why is removing an element from the end of an `ArrayList` O(1), while removing from the middle is O(n)?
**A:** Removing from the end requires no adjustment to any other element's position, but removing from the middle requires shifting every subsequent element one position to the left to close the gap, which touches up to n elements.

**Q:** What specifically would go wrong if you forgot to update the hash map's entry for the swapped-in last element during a removal?
**A:** The map would still point to that element's old index (now occupied by whatever was removed, or stale), so any future lookup, removal, or reliance on that mapping for that value would silently use the wrong array position.

**Q:** Why must the final `list.remove(...)` call in the removal method use an `int` index rather than passing the value itself?
**A:** `ArrayList` has two different `remove` overloads — one by index (O(1) at the end) and one by object/value (O(n) linear scan) — and passing an `int` index specifically ensures the O(1) by-position overload is invoked, rather than accidentally triggering an O(n) value-based scan.

**Q:** How does the swap-with-last technique preserve the correctness of `getRandom()`'s uniform distribution after a removal?
**A:** After the swap and removal, every remaining element still occupies exactly one valid index within the now-shorter list, and every index remains equally likely to be chosen by `random.nextInt(list.size())`, so the uniform distribution over the current set of elements is preserved.

## 13. Final Code

```java
class RandomizedSet {
    private final List<Integer> list = new ArrayList<>();
    private final Map<Integer, Integer> indexOf = new HashMap<>();
    private final Random random = new Random();

    public boolean insert(int val) {
        if (indexOf.containsKey(val)) {
            return false;
        }
        indexOf.put(val, list.size());
        list.add(val);
        return true;
    }

    public boolean remove(int val) {
        if (!indexOf.containsKey(val)) {
            return false;
        }

        int indexToRemove = indexOf.get(val);
        int lastIndex = list.size() - 1;
        int lastVal = list.get(lastIndex);

        list.set(indexToRemove, lastVal);
        indexOf.put(lastVal, indexToRemove);

        list.remove(lastIndex);
        indexOf.remove(val);
        return true;
    }

    public int getRandom() {
        return list.get(random.nextInt(list.size()));
    }
}
```

## 14. Self-Test

Design a `RandomizedSet` supporting `insert(val)`, `remove(val)`, and `getRandom()`, each running in average O(1) time, with `getRandom()` returning a uniformly random current element. Think about why you need two data structures working together, and specifically why removing an arbitrary element requires swapping it to the end of an array-like structure first rather than removing it directly from the middle.
