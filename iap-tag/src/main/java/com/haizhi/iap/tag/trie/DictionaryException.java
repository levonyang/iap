package com.haizhi.iap.tag.trie;

public class DictionaryException extends RuntimeException {

    private static final long serialVersionUID = -185291843586124508L;

    public DictionaryException(Throwable cause) {
        super(cause);
    }

    public DictionaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
