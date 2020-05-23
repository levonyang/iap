package com.haizhi.iap.tag.trie;

public interface CharacterMapping {

    /**
     * double array trie 初始化时的数组大小
     */
    public int getInitSize();

    /**
     * 返回字符集大小
     */
    public int getCharsetrSize();

    /**
     * 字符对应的id
     */
    public int toId(char character);

    /**
     * id对应的字符
     */
    public char toCharacter(int id);

    /**
     * 返回字符串对应的内部id列表
     *
     * @param str 字符串
     * @return id列表
     */
    public int[] toIdList(String str);
}
