import java.util.function.BiConsumer;

/**
 * Simple BST used to index City records by name. Equal keys go to the LEFT.
 * Deletion replaces a node with the maximum from the left subtree.
 * 
 * @author Parth Mehta (pmehta24)
 * @author Anurag Pokala (anuragp34)
 * @version 2025-10-06
 */
class BST<T extends Comparable<? super T>> {

    // ---- node ----
    private static final class Node<E> {
        E key;
        Node<E> left, right;
        Node(E k) { this.key = k; }
    }

    // ---- fields ----
    private Node<T> root;
    private int size;

    // ---- basic ops ----
    public void clear() { root = null; size = 0; }
    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }

    /** Insert one key (duplicates allowed; equals go LEFT). */
    public void insert(T x) {
        if (x == null) throw new IllegalArgumentException("null key");
        root = insertRec(root, x);
        size = size + 1;
    }

    private Node<T> insertRec(Node<T> n, T x) {
        if (n == null) return new Node<>(x);
        int cmp = x.compareTo(n.key);
        if (cmp <= 0) {
            n.left = insertRec(n.left, x);   // equals-left
        } else {
            n.right = insertRec(n.right, x);
        }
        return n;
    }

    /** Remove one occurrence of key; returns true iff something was removed. */
    public boolean remove(T key) {
        if (key == null) return false;
        RemoveRes<T> r = removeRec(root, key);
        root = r.newRoot;
        if (r.removed) { size = size - 1; }
        return r.removed;
    }

    private static final class RemoveRes<E> {
        final Node<E> newRoot;
        final boolean removed;
        RemoveRes(Node<E> r, boolean rem) { this.newRoot = r; this.removed = rem; }
    }

    private RemoveRes<T> removeRec(Node<T> n, T key) {
        if (n == null) return new RemoveRes<>(null, false);
        int cmp = key.compareTo(n.key);
        if (cmp < 0) {
            RemoveRes<T> rr = removeRec(n.left, key);
            n.left = rr.newRoot;
            return new RemoveRes<>(n, rr.removed);
        } else if (cmp > 0) {
            RemoveRes<T> rr = removeRec(n.right, key);
            n.right = rr.newRoot;
            return new RemoveRes<>(n, rr.removed);
        } else {
            if (n.left == null) return new RemoveRes<>(n.right, true);
            if (n.right == null) return new RemoveRes<>(n.left, true);
            Node<T> pred = getMaxNode(n.left);
            n.key = pred.key;
            n.left = deleteMax(n.left);
            return new RemoveRes<>(n, true);
        }
    }

    private Node<T> getMaxNode(Node<T> n) {
        Node<T> cur = n;
        while (cur.right != null) cur = cur.right;
        return cur;
    }

    private Node<T> deleteMax(Node<T> n) {
        if (n.right == null) return n.left;
        n.right = deleteMax(n.right);
        return n;
    }

    /** Membership by key equality. */
    public boolean contains(T key) {
        Node<T> cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp == 0) return true;
            cur = (cmp < 0) ? cur.left : cur.right;
        }
        return false;
    }

    /** Inorder traversal with level (root = 0). */
    public void inorderWithLevels(BiConsumer<Integer, T> visit) {
        inorderRec(root, 0, visit);
    }

    private void inorderRec(Node<T> n, int level, BiConsumer<Integer, T> visit) {
        if (n == null) return;
        inorderRec(n.left, level + 1, visit);
        visit.accept(level, n.key);
        inorderRec(n.right, level + 1, visit);
    }

    /**
     * Remove exactly one node whose value matches a predicate.
     * Traversal follows target.compareTo(..); ties (cmp==0) test the predicate;
     * if not a match, continue LEFT (equals-left invariant).
     */
    public boolean removeMatching(T target, java.util.function.Predicate<T> match) {
        RemoveMatchRes<T> r = removeMatchRec(root, target, match);
        root = r.newRoot;
        if (r.removed) { size = size - 1; }
        return r.removed;
    }

    private static final class RemoveMatchRes<E> {
        final Node<E> newRoot; final boolean removed;
        RemoveMatchRes(Node<E> r, boolean rem) { this.newRoot = r; this.removed = rem; }
    }

    private RemoveMatchRes<T> removeMatchRec(Node<T> n, T target,
                                             java.util.function.Predicate<T> match) {
        if (n == null) return new RemoveMatchRes<>(null, false);
        int cmp = target.compareTo(n.key);
        if (cmp < 0) {
            RemoveMatchRes<T> rr = removeMatchRec(n.left, target, match);
            n.left = rr.newRoot; return new RemoveMatchRes<>(n, rr.removed);
        } else if (cmp > 0) {
            RemoveMatchRes<T> rr = removeMatchRec(n.right, target, match);
            n.right = rr.newRoot; return new RemoveMatchRes<>(n, rr.removed);
        } else {
            if (match.test(n.key)) {
                if (n.left == null) return new RemoveMatchRes<>(n.right, true);
                if (n.right == null) return new RemoveMatchRes<>(n.left, true);
                Node<T> pred = getMaxNode(n.left);
                n.key = pred.key;
                n.left = deleteMax(n.left);
                return new RemoveMatchRes<>(n, true);
            } else {
                RemoveMatchRes<T> rr = removeMatchRec(n.left, target, match);
                n.left = rr.newRoot; return new RemoveMatchRes<>(n, rr.removed);
            }
        }
    }
}
