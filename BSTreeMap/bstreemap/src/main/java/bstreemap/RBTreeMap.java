package bstreemap;

/**
 * Class that implements a red-black tree which implements the MyMap interface.
 * @author Brian S. Borowski
 * @version 1.2.1 March 5, 2024
 */
public class RBTreeMap<K extends Comparable<K>, V> extends BSTreeMap<K, V>
        implements MyMap<K, V> {

    /**
     * Creates an empty red-black tree map.
     */
    public RBTreeMap() { }

    /**
     * Creates a red-black tree map from the array of key-value pairs.
     * @param elements an array of key-value pairs
     */
    public RBTreeMap(Pair<K, V>[] elements) {
        insertElements(elements);
    }

    /**
     * Creates a red-black tree map of the given key-value pairs. If
     * sorted is true, a balanced tree will be created via a divide-and-conquer
     * approach. If sorted is false, the pairs will be inserted in the order
     * they are received, and the tree will be rotated to maintain the red-black
     * tree properties.
     * @param elements an array of key-value pairs
     */
    public RBTreeMap(Pair<K, V>[] elements, boolean sorted) {
        if (!sorted) {
            insertElements(elements);
        } else {
            root = createBST(elements, 0, elements.length - 1);
        }
    }

    /**
     * Recursively constructs a balanced binary search tree by inserting the
     * elements via a divide-snd-conquer approach. The middle element in the
     * array becomes the root. The middle of the left half becomes the root's
     * left child. The middle element of the right half becomes the root's right
     * child. This process continues until low > high, at which point the
     * method returns a null Node.
     * All nodes in the tree are black down to and including the deepest full
     * level. Nodes below that deepest full level are red. This scheme ensures
     * that all paths from the root to the nulls contain the same number of
     * black nodes.
     * @param pairs an array of <K, V> pairs sorted by key
     * @param low   the low index of the array of elements
     * @param high  the high index of the array of elements
     * @return      the root of the balanced tree of pairs
     */
    protected Node<K, V> createBST(Pair<K, V>[] pairs, int low, int high) {
        int max = (int)(Math.log(pairs.length) / Math.log(2));
        return createBSTHelper(pairs, low, high, max, 0);
    }

    private Node<K, V> createBSTHelper(Pair<K, V>[] pairs, int low, int high, int maxLevel, int curLevel)
    {
        if (low > high)
            return null;
        int mid = low + (high - low) / 2;
        RBNode<K,V> node = new RBNode<>(pairs[mid].key, pairs[mid].value);
        size++;
        if (curLevel == 0)
            root = node;
        if (curLevel < maxLevel)
            node.color = RBNode.BLACK;
        else
            node.color = RBNode.RED;
        node.setLeft(createBSTHelper(pairs, low, mid - 1, maxLevel, curLevel + 1));
        node.setRight(createBSTHelper(pairs, mid + 1, high, maxLevel, curLevel + 1));
        if (node.getLeft() != null)
            node.getLeft().setParent(node);
        if (node.getRight() != null)
            node.getRight().setParent(node);
        return node;
    }

    /**
     * Associates the specified value with the specified key in this map. If the
     * map previously contained a mapping for the key, the old value is replaced
     * by the specified value.
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no
     *         mapping for key
     */
    @Override
    public V put(K key, V value) {
        RBNode<K,V> current = (RBNode<K, V>)root;
        RBNode<K,V> prev = (RBNode<K, V>)current;
        boolean left = true;
        while (current != null)
        {
            if (current.key.compareTo(key) == 0)
            {
                V oldValue = (V)current.value;
                current.value = value;
                return oldValue;
            }
            else if (current.key.compareTo(key) > 0)
            {
                prev = current;
                current = current.getLeft();
                left = true;
            }
            else
            {
                prev = current;
                current = current.getRight();
                left = false;
            }
        }
        RBNode<K,V> node = new RBNode<>(key, value);
        if (prev == null)
        {
            root = node;
            node.color = RBNode.BLACK;
            size++;
            return null;
        }
        if (left)
        {
            prev.setLeft(node);
            node.setParent(prev);
        }
        else
        {
            prev.setRight(node);
            node.setParent(prev);
        }
        insertFixup(node);
        size++;
        return null;
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null if there was no
     *         mapping for key
     */
    public V remove(K key)
    {
        Node<K,V> r = root;
        while (r != null)
        {
            if (r.key.compareTo(key) == 0)
            {
                break;
            }
            else if (r.key.compareTo(key) > 0)
            {
                r = r.getLeft();
            }
            else
            {
                r = r.getRight();
            }
        }
        if (r == null)
        {
            return null;
        }
        V oldValue = (V)r.value;
        size--;
        if (r.getLeft() != null && r.getRight() != null)
        {
            Node<K,V> s = (Node<K,V>)treeMinimum(r.getRight());
            r.key = s.key;
            r.value = s.value;
            r = s;
        }
        Node<K,V> repl = (r.getLeft() != null) ? r.getLeft() : r.getRight();
        if (repl != null)
        {
            repl.setParent(r.getParent());
            if (r.getParent() == null)
            {
                root = repl;
            }
            else if (r == r.getParent().getLeft())
            {
                r.getParent().setLeft(repl);
            }
            else
            {
                r.getParent().setRight(repl);
            }
            r.setLeft(null);
            r.setRight(null);
            r.setParent(null);
            if (((RBNode<K,V>) r).color == RBNode.BLACK)
            {
                deleteFixup((RBNode<K,V>) repl);
            }
        }
        else if (r.getParent() == null)
        {
            root = null;
        }
        else
        {
            if (((RBNode<K,V>) r).color == RBNode.BLACK)
            {
                deleteFixup((RBNode<K,V>) r);
            }

            if (r.getParent() != null)
            {
                if (r == r.getParent().getLeft())
                {
                    r.getParent().setLeft(null);
                }
                else if (r == r.getParent().getRight())
                {
                    r.getParent().setRight(null);
                }
                r.setParent(null);
            }
        }
        return oldValue;
    }

    private void deleteFixup(RBNode<K,V> x)
    {
        while (x != root && x.color == RBNode.BLACK)
        {
            if (x == x.getParent().getLeft())
            {
                RBNode<K,V> w = (RBNode<K,V>) x.getParent().getRight();
                if (w != null && w.color == RBNode.RED)
                {
                    w.color = RBNode.BLACK;
                    x.getParent().color = RBNode.RED;
                    leftRotate(x.getParent());
                    w = (RBNode<K,V>) x.getParent().getRight();
                }
                if (w == null)
                {
                    x = x.getParent();
                    continue;
                }
                if ((w.getLeft() == null || w.getLeft().color == RBNode.BLACK)
                && (w.getRight() == null || w.getRight().color == RBNode.BLACK))
                {
                    w.color = RBNode.RED;
                    x = x.getParent();
                }
                else
                {
                    if (w.getRight() == null || w.getRight().color == RBNode.BLACK)
                    {
                        if (w.getLeft() != null)
                        {
                            w.getLeft().color = RBNode.BLACK;
                        }
                        w.color = RBNode.RED;
                        rightRotate(w);
                        w = (RBNode<K,V>) x.getParent().getRight();
                    }
                    w.color = x.getParent().color;
                    x.getParent().color = RBNode.BLACK;
                    if (w.getRight() != null)
                    {
                        w.getRight().color = RBNode.BLACK;
                    }
                    leftRotate(x.getParent());
                    x = (RBNode<K,V>) root;
                }
            }
            else
            {
                RBNode<K,V> w = (RBNode<K,V>) x.getParent().getLeft();
                if (w != null && w.color == RBNode.RED)
                {
                    w.color = RBNode.BLACK;
                    x.getParent().color = RBNode.RED;
                    rightRotate(x.getParent());
                    w = (RBNode<K,V>) x.getParent().getLeft();
                }
                if (w == null)
                {
                    x = x.getParent();
                    continue;
                }
                if ((w.getLeft() == null || w.getLeft().color == RBNode.BLACK)
                && (w.getRight() == null || w.getRight().color == RBNode.BLACK))
                {
                    w.color = RBNode.RED;
                    x = x.getParent();
                }
                else
                {
                    if (w.getLeft() == null || w.getLeft().color == RBNode.BLACK)
                    {
                        if (w.getRight() != null)
                        {
                            w.getRight().color = RBNode.BLACK;
                        }
                        w.color = RBNode.RED;
                        leftRotate(w);
                        w = (RBNode<K,V>) x.getParent().getLeft();
                    }
                    w.color = x.getParent().color;
                    x.getParent().color = RBNode.BLACK;
                    if (w.getLeft() != null)
                    {
                        w.getLeft().color = RBNode.BLACK;
                    }
                    rightRotate(x.getParent());
                    x = (RBNode<K,V>) root;
                }
            }
        }
        x.color = RBNode.BLACK;
    }


    /**
     * Fixup method described on p. 339 of CLRS, 4e.
     */
    private void insertFixup(RBNode<K, V> z) {
        if (z == null)
            return;
        while (z.getParent() != null && z.getParent().color == RBNode.RED)
        {
            if (z.getParent() == z.getParent().getParent().getLeft())
            {
                RBNode<K, V> y = z.getParent().getParent().getRight();
                if (y != null && y.color == RBNode.RED)
                {
                    z.getParent().color = RBNode.BLACK;
                    y.color = RBNode.BLACK;
                    z.getParent().getParent().color = RBNode.RED;
                    z = z.getParent().getParent();
                }
                else
                {
                    if (z == z.getParent().getRight())
                    {
                        z = z.getParent();
                        leftRotate(z);
                    }
                    z.getParent().color = RBNode.BLACK;
                    z.getParent().getParent().color = RBNode.RED;
                    rightRotate(z.getParent().getParent());
                }
            }
            else
            {
                RBNode<K, V> y = z.getParent().getParent().getLeft();
                if (y != null && y.color == RBNode.RED)
                {
                    z.getParent().color = RBNode.BLACK;
                    y.color = RBNode.BLACK;
                    z.getParent().getParent().color = RBNode.RED;
                    z = z.getParent().getParent();
                }
                else
                {
                    if (z == z.getParent().getLeft())
                    {
                        z = z.getParent();
                        rightRotate(z);
                    }
                    z.getParent().color = RBNode.BLACK;
                    z.getParent().getParent().color = RBNode.RED;
                    leftRotate(z.getParent().getParent());
                }
            }
        }
        ((RBNode<K, V>)root).color = RBNode.BLACK;
    }

    /**
     * Fixup method described on p. 351 of CLRS, 4e.
     */
    // private void deleteFixup(RBNode<K, V> x) {
    //     while (x != root && x.color == RBNode.BLACK)
    //     {
    //         if (x == x.getParent().getLeft())
    //         {
    //             RBNode<K, V> w = x.getParent().getRight();
    //             if (w != null && w.color == RBNode.RED)
    //             {
    //                 w.color = RBNode.BLACK;
    //                 x.getParent().color = RBNode.RED;
    //                 leftRotate(x.getParent());
    //                 w = x.getParent().getRight();
    //             }
    //             if (w == null)
    //             {
    //                 x = x.getParent();
    //                 continue;
    //             }
    //             if ((w.getLeft() == null || w.getLeft().color == RBNode.BLACK) && (w.getRight() == null || w.getRight().color == RBNode.BLACK))
    //             {
    //                 w.color = RBNode.RED;
    //                 x = x.getParent();
    //             }
    //             else
    //             {
    //                 if (w.getRight().color == RBNode.BLACK)
    //                 {
    //                     w.getLeft().color = RBNode.BLACK;
    //                     w.color = RBNode.RED;
    //                     rightRotate(w);
    //                     w = x.getParent().getRight();
    //                 }
    //                 w.color = x.getParent().color;
    //                 x.getParent().color = RBNode.BLACK;
    //                 w.getRight().color = RBNode.BLACK;
    //                 leftRotate(x.getParent());
    //                 x = (RBNode<K, V>)root;
    //             }
    //         }
    //         else
    //         {
    //             RBNode<K, V> w = x.getParent().getLeft();
    //             if (w != null && w.color == RBNode.RED)
    //             {
    //                 w.color = RBNode.BLACK;
    //                 x.getParent().color = RBNode.RED;
    //                 rightRotate(x.getParent());
    //                 w = x.getParent().getLeft();
    //             }
    //             if (w == null)
    //             {
    //                 x = x.getParent();
    //                 continue;
    //             }
    //             if ((w.getLeft() == null || w.getLeft().color == RBNode.BLACK) && (w.getRight() == null || w.getRight().color == RBNode.BLACK))
    //             {
    //                 w.color = RBNode.RED;
    //                 x = x.getParent();
    //             }
    //             else
    //             {
    //                 if (w.getLeft().color == RBNode.BLACK)
    //                 {
    //                     w.getRight().color = RBNode.BLACK;
    //                     w.color = RBNode.RED;
    //                     leftRotate(w);
    //                     w = x.getParent().getLeft();
    //                 }
    //                 w.color = x.getParent().color;
    //                 x.getParent().color = RBNode.BLACK;
    //                 w.getLeft().color = RBNode.BLACK;
    //                 rightRotate(x.getParent());
    //                 x = (RBNode<K, V>)root;
    //             }
    //         }
    //     }
    //     x.color = RBNode.BLACK;
    // }

    /**
     * Left-rotate method described on p. 336 of CLRS, 4e.
     */
    private void leftRotate(Node<K, V> x) {
        RBNode<K, V> y = ((RBNode<K, V>)x).getRight();
        x.setRight(y.getLeft());
        if (y.getLeft() != null)
            y.getLeft().setParent(x);
        y.setParent(x.getParent());
        if (x.getParent() == null)
            root = y;
        else if (x == x.getParent().getLeft())
            x.getParent().setLeft(y);
        else
            x.getParent().setRight(y);
        y.setLeft(x);
        x.setParent(y);
    }

    /**
     * Right-rotate method described on p. 336 of CLRS, 4e.
     */
    private void rightRotate(Node<K, V> x) {
        RBNode<K, V> y = ((RBNode<K, V>)x).getLeft();
        x.setLeft(y.getRight());
        if (y.getRight() != null)
            y.getRight().setParent(x);
        y.setParent(x.getParent());
        if (x.getParent() == null)
            root = y;
        else if (x.getParent().getRight() == x)
            x.getParent().setRight(y);
        else
            x.getParent().setLeft(y);
        y.setRight(x);
        x.setParent(y);
    }
}
