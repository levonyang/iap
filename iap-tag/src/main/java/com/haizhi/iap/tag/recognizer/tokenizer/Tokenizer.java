package com.haizhi.iap.tag.recognizer.tokenizer;


import com.haizhi.iap.tag.recognizer.meta.QueryToken;

import java.util.List;

public interface Tokenizer {
    public List<QueryToken> tokenize(String str);
}
