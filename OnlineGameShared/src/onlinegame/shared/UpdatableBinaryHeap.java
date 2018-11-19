package onlinegame.shared;

import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.AbstractQueue;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Alfred
 * @param <E>
 */
public final class UpdatableBinaryHeap<E> extends AbstractQueue<E>
{
    private E[] _heap;
    private final Comparator<? super E> _comparator;
    private final TObjectIntHashMap<E> _indexMap;
    
    private int _size = 0;
    
    public UpdatableBinaryHeap()
    {
        this(16, null);
    }
    public UpdatableBinaryHeap(Comparator<? super E> comparator)
    {
        this(16, comparator);
    }
    public UpdatableBinaryHeap(int initialCapacity)
    {
        this(initialCapacity, null);
    }
    @SuppressWarnings("unchecked")
    public UpdatableBinaryHeap(int initialCapacity, Comparator<? super E> comparator)
    {
        initialCapacity = Math.max(initialCapacity, 1);
        _heap = (E[])new Object[initialCapacity];
        _comparator = comparator;
        _indexMap = new TObjectIntHashMap<>(initialCapacity * 2 + 1, .5f, -1);
    }
    
    public UpdatableBinaryHeap(Collection<? extends E> c)
    {
        this(c, null);
    }
    @SuppressWarnings("unchecked")
    public UpdatableBinaryHeap(Collection<? extends E> c, Comparator<? super E> comparator)
    {
        _heap = (E[])c.toArray();
        if (_heap.length == 0)
        {
            _heap = (E[])new Object[16];
            _size = 0;
        }
        else
        {
            _size = _heap.length;
        }
        _comparator = comparator;
        _indexMap = new TObjectIntHashMap<>(Math.max(_size, 32), .5f, -1);
        heapify();
    }
    
    public void heapify()
    {
        TObjectIntHashMap<E> indexMap = _indexMap;
        indexMap.clear();
        
        E[] heap = _heap;
        int size = _size;
        for (int i = (size >>> 1) - 1; i >= 0; i--)
        {
            siftDown(i, heap[i]);
        }
        
        for (int i = 0; i < size; i++)
        {
            indexMap.put(heap[i], i);
        }
    }
    
    public void ensureCapacity(int capacity)
    {
        if (_heap.length < capacity)
        {
            grow(capacity);
        }
    }
    
    public void ensureAdditionalCapacity(int elements)
    {
        ensureCapacity(size() + elements);
    }
    
    @SuppressWarnings("unchecked")
    private void grow(int capacity)
    {
        int len = _heap.length;
        int newlen = Math.min(len + (len >>> 1), capacity);
        if (newlen < 0 || newlen >= Integer.MAX_VALUE - 8)
        {
            if (capacity <= Integer.MAX_VALUE - 8)
            {
                newlen = Integer.MAX_VALUE - 8;
            }
            else
            {
                newlen = capacity;
            }
        }
        E[] newheap = (E[])Arrays.copyOf((Object[])_heap, newlen);
        _heap = newheap;
    }
    
    @Override
    public boolean add(E e)
    {
        return offer(e);
    }
    
    @Override
    public boolean offer(E e)
    {
        if (e == null)
        {
            throw new NullPointerException();
        }
        
        ensureAdditionalCapacity(1);
        int pos = _size++;
        
        //_heap[pos] = e;
        siftUp(pos, e);
        
        return true;
    }
    
    private void siftUp(int pos, E e)
    {
        if (_comparator == null)
        {
            siftUpComparable(pos, e);
        }
        else
        {
            siftUpWithComparator(pos, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void siftUpComparable(int pos, E e)
    {
        Comparable<? super E> key = (Comparable<? super E>)e;
        E[] heap = _heap;
        TObjectIntHashMap<E> indexMap = _indexMap;
        
        while (pos > 0)
        {
            int parent = (pos - 1) >>> 1;
            E p = heap[parent];
            if (key.compareTo(p) >= 0)
            {
                break;
            }
            heap[pos] = p;
            indexMap.put(p, pos);
            pos = parent;
        }
        heap[pos] = e;
        indexMap.put(e, pos);
    }
    
    private void siftUpWithComparator(int pos, E e)
    {
        Comparator<? super E> comparator = _comparator;
        E[] heap = _heap;
        TObjectIntHashMap<E> indexMap = _indexMap;
        
        while (pos > 0)
        {
            int parent = (pos - 1) >>> 1;
            E p = heap[parent];
            if (comparator.compare(e, p) >= 0)
            {
                break;
            }
            heap[pos] = p;
            indexMap.put(p, pos);
            pos = parent;
        }
        heap[pos] = e;
        indexMap.put(e, pos);
    }
    
    @Override
    public E poll()
    {
        if (_size == 0)
        {
            return null;
        }
        E[] heap = _heap;
        int pos = --_size;
        
        E root = heap[0];
        E last = heap[pos];
        heap[pos] = null;
        
        if (pos != 0)
        {
            siftDown(0, last);
        }
        
        _indexMap.remove(root);
        return root;
    }
    
    private void siftDown(int pos, E e)
    {
        if (_comparator == null)
        {
            siftDownComparable(pos, e);
        }
        else
        {
            siftDownWithComparator(pos, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void siftDownComparable(int pos, E e)
    {
        Comparable<? super E> key = (Comparable<? super E>)e;
        int size = _size;
        int half = size >>> 1;
        E[] heap = _heap;
        TObjectIntHashMap<E> indexMap = _indexMap;
        
        while (pos < half)
        {
            int child = (pos << 1) + 1;
            int right = child + 1;
            E c = heap[child];
            E r = heap[right];
            if (right < size && ((Comparable<? super E>)c).compareTo(r) > 0)
            {
                child = right;
                c = r;
            }
            if (key.compareTo(c) <= 0)
            {
                break;
            }
            heap[pos] = c;
            indexMap.put(c, pos);
            pos = child;
        }
        heap[pos] = e;
        indexMap.put(e, pos);
    }
    
    private void siftDownWithComparator(int pos, E e)
    {
        Comparator<? super E> comparator = _comparator;
        int size = _size;
        int half = size >>> 1;
        E[] heap = _heap;
        TObjectIntHashMap<E> indexMap = _indexMap;
        
        while (pos < half)
        {
            int child = (pos << 1) + 1;
            int right = child + 1;
            E c = heap[child];
            E r = heap[right];
            if (right < size && comparator.compare(c, r) > 0)
            {
                child = right;
                c = r;
            }
            if (comparator.compare(e, c) <= 0)
            {
                break;
            }
            heap[pos] = c;
            indexMap.put(c, pos);
            pos = child;
        }
        heap[pos] = e;
        indexMap.put(e, pos);
    }
    
    private void siftDownIgnoringMap(int pos, E e)
    {
        if (_comparator == null)
        {
            siftDownComparableIgnoringMap(pos, e);
        }
        else
        {
            siftDownWithComparatorIgnoringMap(pos, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void siftDownComparableIgnoringMap(int pos, E e)
    {
        Comparable<? super E> key = (Comparable<? super E>)e;
        int size = _size;
        int half = size >>> 1;
        E[] heap = _heap;
        
        while (pos < half)
        {
            int child = (pos << 1) + 1;
            int right = child + 1;
            E c = heap[child];
            E r = heap[right];
            if (right < size && ((Comparable<? super E>)c).compareTo(r) > 0)
            {
                child = right;
                c = r;
            }
            if (key.compareTo(c) <= 0)
            {
                break;
            }
            heap[pos] = c;
            pos = child;
        }
        heap[pos] = e;
    }
    
    private void siftDownWithComparatorIgnoringMap(int pos, E e)
    {
        Comparator<? super E> comparator = _comparator;
        int size = _size;
        int half = size >>> 1;
        E[] heap = _heap;
        
        while (pos < half)
        {
            int child = (pos << 1) + 1;
            int right = child + 1;
            E c = heap[child];
            E r = heap[right];
            if (right < size && comparator.compare(c, r) > 0)
            {
                child = right;
                c = r;
            }
            if (comparator.compare(e, c) <= 0)
            {
                break;
            }
            heap[pos] = c;
            pos = child;
        }
        heap[pos] = e;
    }
    
    @Override
    public E peek()
    {
        return _heap[0];
    }
    
    @Override
    public boolean contains(Object o)
    {
        return _indexMap.contains(o);
    }
    
    public int indexOf(Object o)
    {
        return _indexMap.get(o);
    }
    
    public E getDirect(int i)
    {
        if (i < 0 || i >= _size)
        {
            throw new IndexOutOfBoundsException();
        }
        return _heap[i];
    }
    
    @Override
    public boolean remove(Object o)
    {
        int i = indexOf(o);
        if (i == -1)
        {
            return false;
        }
        else
        {
            removeAt(i);
            return true;
        }
    }
    
    private boolean removeEquals(Object o)
    {
        E[] heap = _heap;
        int size = _size;
        for (int i = 0; i < size; i++)
        {
            if (o == heap[i])
            {
                removeAt(i);
                return true;
            }
        }
        return false;
    }
    
    public void addOrSiftUp(E e)
    {
        int i = indexOf(e);
        if (i == -1)
        {
            add(e);
        }
        else
        {
            siftUp(i, _heap[i]);
        }
    }
    
    public void addOrSiftDown(E e)
    {
        int i = indexOf(e);
        if (i == -1)
        {
            add(e);
        }
        else
        {
            siftDown(i, _heap[i]);
        }
    }
    
    public void addOrSiftUpOrDown(E e)
    {
        int i = indexOf(e);
        if (i == -1)
        {
            add(e);
        }
        else
        {
            siftUpOrDown(i);
        }
    }
    
    public void siftUp(E e)
    {
        int i = indexOf(e);
        if (i != -1)
        {
            siftUp(i, _heap[i]);
        }
    }
    
    public void siftDown(E e)
    {
        int i = indexOf(e);
        if (i != -1)
        {
            siftDown(i, _heap[i]);
        }
    }
    
    public void siftUpOrDown(E e)
    {
        int i = indexOf(e);
        if (i != -1)
        {
            siftUpOrDown(i);
        }
    }
    
    public void siftUp(int i)
    {
        if (i < 0 || i >= _size)
        {
            throw new IndexOutOfBoundsException();
        }
        siftUp(i, _heap[i]);
    }
    
    public void siftDown(int i)
    {
        if (i < 0 || i >= _size)
        {
            throw new IndexOutOfBoundsException();
        }
        siftDown(i, _heap[i]);
    }
    
    public void siftUpOrDown(int i)
    {
        if (i < 0 || i >= _size)
        {
            throw new IndexOutOfBoundsException();
        }
        E[] heap = _heap;
        E moved = heap[i];
        siftUp(i, moved);
        if (heap[i] == moved)
        {
            siftDown(i, moved);
        }
    }
    
    public void removeAt(int i)
    {
        if (i < 0 || i >= _size)
        {
            throw new IndexOutOfBoundsException();
        }
        int pos = --_size;
        E[] heap = _heap;
        E toRemove = heap[i];
        if (pos == i)
        {
            heap[pos] = null;
        }
        else
        {
            E moved = heap[pos];
            heap[pos] = null;
            siftDown(i, moved);
            if (heap[i] == moved)
            {
                siftUp(i, moved);
            }
        }
        _indexMap.remove(toRemove);
    }
    private E removeAtForItr(int i)
    {
        int pos = --_size;
        E[] heap = _heap;
        E toRemove = heap[i];
        if (pos == i)
        {
            heap[pos] = null;
        }
        else
        {
            E moved = heap[pos];
            heap[pos] = null;
            siftDown(i, moved);
            if (heap[i] == moved)
            {
                siftUp(i, moved);
                if (heap[i] != moved)
                {
                    return moved;
                }
            }
        }
        _indexMap.remove(toRemove);
        return null;
    }
    
    @Override
    public void clear()
    {
        E[] heap = _heap;
        int size = _size;
        for (int i = 0; i < size; i++)
        {
            heap[i] = null;
        }
        _size = 0;
        _indexMap.clear();
    }
    
    @Override
    public int size()
    {
        return _size;
    }
    
    public int capacity()
    {
        return _heap.length;
    }
    
    @Override
    public Object[] toArray()
    {
        int size = _size;
        Object[] a = new Object[size];
        System.arraycopy(_heap, 0, a, 0, size);
        return a;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a)
    {
        int size = _size;
        
        if (a.length < size)
        {
            return (T[])Arrays.copyOf(_heap, size, a.getClass());
        }
        System.arraycopy(_heap, 0, a, 0, size);
        return a;
    }
    
    @Override
    public Iterator<E> iterator()
    {
        return new HeapItr();
    }
    
    private class HeapItr implements Iterator<E>
    {
        private int cursor = 0;

        private int lastRet = -1;
        private ArrayDeque<E> forgetMeNot = null;
        private E lastRetElt = null;

        @Override
        public boolean hasNext()
        {
            return cursor < _size || (forgetMeNot != null && !forgetMeNot.isEmpty());
        }

        @Override
        public E next()
        {
            if (cursor < _size)
                return _heap[lastRet = cursor++];
            if (forgetMeNot != null)
            {
                lastRet = -1;
                lastRetElt = forgetMeNot.poll();
                if (lastRetElt != null)
                    return lastRetElt;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove()
        {
            if (lastRet != -1)
            {
                E moved = removeAtForItr(lastRet);
                lastRet = -1;
                if (moved == null)
                {
                    cursor--;
                }
                else
                {
                    if (forgetMeNot == null)
                    {
                        forgetMeNot = new ArrayDeque<>();
                    }
                    forgetMeNot.add(moved);
                }
            }
            else if (lastRetElt != null)
            {
                removeEquals(lastRetElt);
                lastRetElt = null;
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }
}
