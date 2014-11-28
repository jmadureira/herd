package io.herd.base;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class WaitFreeHashMap<Key, Value> implements Map<Key, Value> {

    private static class ArrayNode<K, V> extends Node<K, V> {

        final AtomicReferenceArray<Node<K, V>> array;

        public ArrayNode(int arrayLength) {
            super(0);
            this.array = new AtomicReferenceArray<>(arrayLength);
        }

        @Override
        public K getKey() {
            throw new UnsupportedOperationException();
        }

        @Override
        public V getValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            int length = array.length();
            for (int i = 0; i < length; i++) {
                Map.Entry<K, V> entry = array.get(i);
                if (entry != null) {
                    sb.append(entry).append(',').append(' ');
                }
            }
            return sb.toString();
        }

    }

    private abstract static class Node<K, V> implements Map.Entry<K, V> {

        final int hash;

        protected Node(int hash) {
            this.hash = hash;
        }
    }

    private static class DataNode<K, V> extends Node<K, V> {

        private final K key;
        private final V value;

        public DataNode(int hash, K key, V value) {
            super(hash);
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

    }

    /**
     * Taken from JDK's map implementations.
     * <p/>
     * Spreads (XORs) higher bits of hash to lower and also forces top bit to 0. Because the table uses power-of-two
     * masking, sets of hashes that vary only in bits above the current mask will always collide. (Among known examples
     * are sets of Float keys holding consecutive whole numbers in small tables.) So we apply a transform that spreads
     * the impact of higher bits downward. There is a tradeoff between speed, utility, and quality of bit-spreading.
     * Because many common sets of hashes are already reasonably distributed (so don't benefit from spreading), and
     * because we use trees to handle large sets of collisions in bins, we just XOR some shifted bits in the cheapest
     * possible way to reduce systematic lossage, as well as to incorporate impact of the highest bits that would
     * otherwise never be used in index calculations because of table bounds.
     * <p/>
     * Static methods are automatically inlined by the compiler ... I think.
     */
    private static final int hash(int key) {
        return (key ^ (key >>> 16)) & HASH_BITS;
    }

    private static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash
    private static final int DEFAULT_ARRAY_LENGTH = 4;

    private static final int KEY_SIZE = Integer.SIZE;
    private final int arrayLength;
    private final int bytesToShift;

    private final ArrayNode<Key, Value> head;

    public WaitFreeHashMap() {
        this(DEFAULT_ARRAY_LENGTH);
    }

    public WaitFreeHashMap(int arrayLength) {
        this.arrayLength = arrayLength;
        this.bytesToShift = (int) Maths.log(2, arrayLength);
        this.head = new ArrayNode<>(this.arrayLength);
    }

    /**
     * Removes all of the mappings from this map. There is however no guarantee that the map will be empty if there are
     * other threads inserting items.
     */
    @Override
    public void clear() {
        int length = head.array.length();
        for (int i = 0; i < length; i++) {
            head.array.set(i, null);
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<Map.Entry<Key, Value>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    private ArrayNode<Key, Value> expandTable(ArrayNode<Key, Value> local, int pos, Node<Key, Value> node, int right) {
        ArrayNode<Key, Value> newArrayNode = new ArrayNode<>(this.arrayLength);
        int hash = hash(node.getKey().hashCode()) >> (right + this.bytesToShift);
        newArrayNode.array.set(hash & (arrayLength - 1), node);
        if (local.array.compareAndSet(pos, node, newArrayNode)) {
            return newArrayNode;
        } else {
            return (ArrayNode<Key, Value>) local.array.get(pos);
        }
    }

    @Override
    public Value get(Object key) {
        int hash = hash(key.hashCode());
        ArrayNode<Key, Value> local = head;
        for (int right = 0; right < KEY_SIZE; right += this.bytesToShift) {
            int pos = hash & (arrayLength - 1);
            hash = hash >> this.bytesToShift;
            Node<Key, Value> node = unmark(getNode(local, pos));
            if (isArrayNode(node)) {
                local = (ArrayNode<Key, Value>) node;
            } else if (node == null) {
                return null;
            } else {
                if (node.hash == hash(key.hashCode())) {
                    return node.getValue();
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private Node<Key, Value> getNode(ArrayNode<Key, Value> local, int pos) {
        return local.array.get(pos);
    }

    private boolean isArrayNode(Map.Entry<Key, Value> node) {
        return node instanceof ArrayNode;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean isMarked(java.util.Map.Entry<Key, Value> node) {
        return node != null;
    }

    @Override
    public Set<Key> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Value put(Key key, Value value) {
        int hash = hash(key.hashCode());
        Node<Key, Value> newNode = new DataNode<>(hash, key, value);
        ArrayNode<Key, Value> local = head;
        for (int right = 0; right < KEY_SIZE; right += this.bytesToShift) {
            int pos = hash & (arrayLength - 1);
            hash = hash >> this.bytesToShift;
            while (true) {
                Node<Key, Value> node = getNode(local, pos);
                if (isArrayNode(node)) {
                    local = (ArrayNode<Key, Value>) node;
                    break;
                } else if (isMarked(node)) {
                    local = expandTable(local, pos, node, right);
                    break;
                } else if (node == null) {
                    if (((ArrayNode<Key, Value>) local).array.compareAndSet(pos, null, newNode)) {
                        return null;
                    } else {
                        node = getNode(local, pos);
                        if (isArrayNode(node)) {
                            local = (ArrayNode<Key, Value>) node;
                            break;
                        } else if (node.hash == hash(key.hashCode())) {
                            return node.getValue();
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends Key, ? extends Value> m) {
        for (Map.Entry<? extends Key, ? extends Value> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Value remove(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Returns a string representation of this map. The string representation consists of a list of key-value mappings
     * (in no particular order) enclosed in braces ("<code>{}</code>"). Adjacent mappings are separated by the
     * characters "<code>, </code>" (comma and space). Each key-value mapping is rendered as the key followed by an
     * equals sign ("<code>=</code>") followed by the associated value.
     *
     * @return a string representation of this map
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append(head.toString());
        return sb.append('}').toString();
    }

    private Node<Key, Value> unmark(Node<Key, Value> node) {
        return node;
    }

    @Override
    public Collection<Value> values() {
        // TODO Auto-generated method stub
        return null;
    }

}
