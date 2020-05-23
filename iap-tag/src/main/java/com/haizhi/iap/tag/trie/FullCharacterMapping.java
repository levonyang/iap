package com.haizhi.iap.tag.trie;

import java.io.Serializable;


public class FullCharacterMapping implements CharacterMapping, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5373081724058805276L;
    private static final int N = Character.MAX_VALUE;

    public int getInitSize() {
        return N;
    }

    public int getCharsetrSize() {
        return N;
    }

    public int toId(char character) {
        return (int) character;
    }

    public char toCharacter(int id) {
        return (char) id;
    }

    public int[] toIdList(String str) {
        int[] ret = new int[str.length()];
        for (int i = 0; i < str.length(); i++) {
            ret[i] = toId(str.charAt(i));
        }
        return ret;
    }
}