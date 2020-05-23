package com.haizhi.iap.tag.trie;

import java.io.Serializable;
import java.util.*;


public class DoubleArrayTrie implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 5586394930559218801L;
    private static final int leafBit = 1 << 30;
    private static final int ROOT_INDEX = 1;
    private static final int ROOT_BASE = 1;
    private static final int[] EMPTY_WALK_STATE = new int[]{-1, -1};

    CharacterMapping charMap;
    //下标0作为链表入口，负数表示链表，正数表示double array trie的base
    DAList check;
    DAList base;
    private char unuseChar = (char) 0;
    private int unuseCharValue = 0;
    //单词个数
    private int number;

    public DoubleArrayTrie() throws DictionaryException {
        this(new ByteCharacterMapping());
    }

    public DoubleArrayTrie(CharacterMapping charMap) throws DictionaryException {
        this(charMap, charMap.getInitSize());
    }

    public DoubleArrayTrie(CharacterMapping charMap, int initArraySize) throws DictionaryException {
        this.charMap = charMap;
        base = new DAList(initArraySize);
        check = new DAList(initArraySize);
        //base array form a next array link
        base.add(0);
        //check array form a previous array link
        check.add(0);
        //root
        base.add(ROOT_BASE);
        check.add(0);
        expandArray(charMap.getInitSize());
        unuseCharValue = charMap.toId(unuseChar);
    }

    private boolean isLeaf(int value) {
        return value > 0 && ((value & leafBit) != 0);
    }

    private int setLeafValue(int value) {
        return (value | leafBit);
    }

    private int getLeafValue(int value) {
        return (value ^ leafBit);
    }

    public int getSize() {
        return this.base.getSize();
    }

    private int getBase(int position) {
        return this.base.get(position);
    }

    private int getCheck(int position) {
        return this.check.get(position);
    }

    private void setBase(int position, int value) {
        this.base.set(position, value);
    }

    private void setCheck(int position, int value) {
        this.check.set(position, value);
    }

    protected boolean isEmpty(int position) {
        return getCheck(position) <= 0;
    }

    private int getNextFreeBase(int nextChar) throws DictionaryException {
        int pos = -getCheck(0);
        while (pos != 0) {
            if (pos > nextChar + 1) {
                return pos - nextChar;
            }
            pos = -getCheck(pos);
        }
        //all unavailable
        int oldSize = getSize();
        expandArray(oldSize + base.getExpandFactor());
        return oldSize;
    }

    private void addFreeLink(int position) {
        check.set(position, check.get(-base.get(0)));
        check.set(-base.get(0), -position);
        base.set(position, base.get(0));
        base.set(0, -position);
    }

    private void delFreeLink(int position) {
        base.set(-check.get(position), base.get(position));
        check.set(-base.get(position), check.get(position));
    }

    private void expandArray(int maxSize) throws DictionaryException {
        int curSize = this.base.getSize();
        if (curSize > maxSize) return;
        if (maxSize >= leafBit) {
            throw new DictionaryException("Double Array Trie的数组过大", null);
        }
        for (int i = curSize; i <= maxSize; i++) {
            this.base.add(0);
            this.check.add(0);
            addFreeLink(i);
        }
    }

    private boolean insert(String str, int value, boolean cover) throws DictionaryException {
        if (null == str || str.contains(String.valueOf(unuseChar))) return false;
        if ((value < 0) || ((value & leafBit) != 0)) return false;
        //change value into value
        value = setLeafValue(value);
        //将str转成内部使用的id列表
        int[] ids = charMap.toIdList(str + unuseChar);

        //begin
        int fromState = ROOT_INDEX;
        int toState = ROOT_INDEX;
        int ind = 0;
        while (ind < ids.length) {
            int c = ids[ind];
            toState = getBase(fromState) + c;
            //确保内部数组大小大于toState
            expandArray(toState);
            if (isEmpty(toState)) {
                //占用toState, 从link中删去
                delFreeLink(toState);
                //修改toState的base和check
                setCheck(toState, fromState);
                if (ind == ids.length - 1) {
                    this.number++;
                    setBase(toState, value);
                } else {
                    int nextChar = ids[ind + 1];
                    setBase(toState, getNextFreeBase(nextChar));
                }
            } else if (getCheck(toState) != fromState) {
                solveConflict(fromState, c);
                //redo this character
                continue;
            }

            fromState = toState;
            ind++;
        }
        if (cover) {
            setBase(toState, value);
        }
        return true;
    }

    private int moveChildren(SortedSet<Integer> children) throws DictionaryException {
        int minChild = children.first();
        int maxChild = children.last();
        int cur = 0;
        while (getCheck(cur) != 0) {
            if (cur > minChild + ROOT_BASE) {
                int tempBase = cur - minChild;
                boolean ok = true;
                for (Iterator<Integer> itr = children.iterator(); itr.hasNext(); ) {
                    int toPos = tempBase + itr.next();
                    if (toPos >= getSize()) {
//						//走到这里，说明前面的i-1个child能安置好，
//						//注意这里children数组是按从小到大排列好的
//						//所以第i个child超出数组大小，那么后面的所有child都会超出
//						//那么只需将数组扩大到能容纳maxChild的长度，返回tempBase即可。
//						int needLength = tempBase + maxChild;
//						expandArray(needLength);
//						return tempBase;
                        //事实证明以上策略比较短见，会浪费很多空间，导致最后更慢
                        ok = false;
                        break;
                    }
                    if (!isEmpty(toPos)) {
                        //不满足，枚举查找下一个链表中的位置
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    //所有children都能在现有长度下找到empty的位置
                    return tempBase;
                }
            }
            cur = -getCheck(cur);
        }
        //all position is not fit
        int oldSize = getSize();
        expandArray(oldSize + maxChild);
        return oldSize;
    }

    private void solveConflict(int parent, int newChild) throws DictionaryException {
        // The list of children values
        TreeSet<Integer> children = new TreeSet<Integer>();
        // Add the value-to-add
        children.add(new Integer(newChild));
        // Find all existing children and add them too.
        for (int c = 0; c < charMap.getCharsetrSize(); c++) {
            int tempNext = getBase(parent) + c;
            if (tempNext >= getSize()) break;
            if (tempNext < getSize() && getCheck(tempNext) == parent)
                children.add(new Integer(c));
        }

        int newBase = moveChildren(children);

        children.remove(new Integer(newChild));

        for (Integer child : children) {
            int c = child.intValue();
            //占用newBase + c
            delFreeLink(newBase + c);

            //set new check
            setCheck(newBase + c, parent);

            //copy oldBase to newBase
            setBase(newBase + c, getBase(getBase(parent) + c));

            int childBase = getBase(getBase(parent) + c);
            if (!isLeaf(childBase)) {
                //如果儿子不是叶子结点，那么儿子肯定有儿子，要把儿子的儿子的check数组更新到newBase
                for (int d = 0; d < charMap.getCharsetrSize(); d++) {
                    int nextPos = childBase + d;
                    if (nextPos >= getSize()) break;
                    if (nextPos < getSize() && getCheck(nextPos) == getBase(parent) + c) {
                        setCheck(nextPos, newBase + c);
                    }
                }
            }

            //回收oldBase = getBase(parent) + c
            addFreeLink(getBase(parent) + c);
        }
        setBase(parent, newBase);
    }

    /**
     * 获取已插入单词的个数
     *
     * @return
     */
    public int getNumbers() {
        return this.number;
    }

    /**
     * 覆盖的方式插入字符串和对应的值，如插入（"a", 0）和（"a", 1），最后"a" -> 1
     *
     * @param str   插入的字符串
     * @param value 对应的值
     * @return 返回成功或失败，现在都是成功
     */
    public boolean coverInsert(String str, int value) throws DictionaryException {
        return insert(str, value, true);
    }

    /**
     * 不覆盖的方式插入字符串和对应的值，如插入（"a", 0）和（"a", 1），最后"a" -> 0
     *
     * @param str   插入的字符串
     * @param value 对应的值
     * @return 返回成功或失败，现在都是成功
     */
    public boolean uncoverInsert(String str, int value) throws DictionaryException {
        return insert(str, value, false);
    }

    /**
     * 查找query中从start开始的最长匹配的词和值
     *
     * @param query 查找的字符串
     * @param start 从该位置开始查找
     * @return int[], 0->最大长度, 1->该词对应的值
     */
    public int[] find(String query, int start) {
        if (query == null || start >= query.length()) return new int[]{0, -1};
        int curState = ROOT_BASE;
        int maxLength = 0, lastVal = -1;
        for (int i = start; i < query.length(); i++) {
            int[] res = walkTrie(curState, query.substring(i, i + 1));
            if (res[0] == -1) break;
            curState = res[0];
            if (res[1] != -1) {
                maxLength = i - start + 1;
                lastVal = res[1];
            }
        }
        return new int[]{maxLength, lastVal};
    }

    /**
     * 查找query中从start开始的所有匹配的词和值
     *
     * @param query 查找的字符串
     * @param start 从该位置开始查找
     * @return int[], 0->长度, 1->该词对应的值
     */
    public List<int[]> findAll(String query, int start) {
        List<int[]> ret = new ArrayList<int[]>();
        if (query == null || start >= query.length()) return ret;
        int curState = ROOT_BASE;
        for (int i = start; i < query.length(); i++) {
            int[] res = walkTrie(curState, query.substring(i, i + 1));
            if (res[0] == -1) break;
            curState = res[0];
            if (res[1] != -1) {
                ret.add(new int[]{i - start + 1, res[1]});
            }
        }
        return ret;
    }

    /**
     * 取根结点，用于walkTrie
     *
     * @return 返回根结点State
     */
    public int getRoot() {
        return ROOT_BASE;
    }

    /**
     * 在curState当前状态下走str个字符，返回走str后的状态和值
     *
     * @param curState 当前状态
     * @param str      需转移的字符串
     * @return int[], 0->结束后的状态，1->值，若状态不存在或值不存在，返回-1
     */
    public int[] walkTrie(int curState, String str) {
        if (str == null || curState < ROOT_BASE || str.contains(String.valueOf(unuseChar))) return EMPTY_WALK_STATE;
        if (curState != ROOT_BASE && isEmpty(curState)) return EMPTY_WALK_STATE;
        int[] ids = charMap.toIdList(str);
        for (int i = 0; i < ids.length; i++) {
            int c = ids[i];
            if (getBase(curState) + c < getSize() &&
                    getCheck(getBase(curState) + c) == curState) {
                curState = getBase(curState) + c;
            } else {
                return EMPTY_WALK_STATE;
            }
        }
        //判断当前是否可以形成终止状态
        if (getCheck(getBase(curState) + unuseCharValue) == curState) {
            int value = getLeafValue(getBase(getBase(curState) + unuseCharValue));
            return new int[]{curState, value};
        }
        return new int[]{curState, -1};
    }

    /**
     * 删除该字符串str
     *
     * @param str 待删除的字符串
     * @return 不存在该字符串返回-1，否则返回该字符串的值（value）
     */
    public int delete(String str) {
        if (str == null) return -1;
        int curState = ROOT_BASE;
        int[] ids = charMap.toIdList(str);
        // +1 for unuseChar
        int[] path = new int[ids.length + 1];
        int i = 0;
        for (; i < ids.length; i++) {
            int c = ids[i];
            if (getBase(curState) + c < getSize() &&
                    getCheck(getBase(curState) + c) == curState) {
                curState = getBase(curState) + c;
                path[i] = curState;
            } else {
                break;
            }
        }
        int ret = -1;
        if (i == ids.length) {
            //存在该串
            //判断当前是否可以形成终止状态
            if (getCheck(getBase(curState) + unuseCharValue) == curState) {
                //assert isLeaf(getBase(getBase(curState) + unuseCharValue));
                this.number--;
                ret = getLeafValue(getBase(getBase(curState) + unuseCharValue));
                path[path.length - 1] = getBase(curState) + unuseCharValue;

                //开始删除独立的结点/状态
                for (int j = path.length - 1; j >= 0; j--) {
                    //判断该结点是否独立结点（是否有儿子）
                    boolean isLeaf = true;
                    int state = path[j];
                    for (int k = 0; k < charMap.getCharsetrSize(); k++) {
                        if (isLeaf(getBase(state))) break;
                        if (getBase(state) + k < getSize() && getCheck(getBase(state) + k) == state) {
                            isLeaf = false;
                            break;
                        }
                    }
                    if (isLeaf) {
                        addFreeLink(state);
                    } else {
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * debug
     *
     * @return
     */
    public int getEmptySize() {
        int cnt = 0;
        for (int i = 0; i < getSize(); i++) {
            if (isEmpty(i)) {
                cnt++;
            }
        }
        return cnt;
    }

    public int getMaximumValue() {
        return leafBit - 1;
    }
}
