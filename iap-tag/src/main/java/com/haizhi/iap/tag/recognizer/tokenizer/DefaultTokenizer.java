package com.haizhi.iap.tag.recognizer.tokenizer;


import com.haizhi.iap.tag.recognizer.meta.QueryToken;

import java.util.ArrayList;
import java.util.List;

public class DefaultTokenizer implements Tokenizer {

    public List<QueryToken> tokenize(String str) {
        ArrayList<QueryToken> tokens = new ArrayList<QueryToken>(str.length());
        int tokenCnt = -1;
        boolean lastCharIsAlpha = false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            boolean isAlpha = false;
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')) {
                isAlpha = true;
            }

            if (lastCharIsAlpha && isAlpha) {
                QueryToken token = tokens.get(tokenCnt);
                token.setValue(token.getValue() + c);
                token.setEnd(i + 1);
            } else {
                QueryToken token = new QueryToken();
                token.setStart(i);
                token.setEnd(i + 1);
                token.setValue(String.valueOf(c));
                tokens.add(token);

                tokenCnt++;
            }
            lastCharIsAlpha = isAlpha;
        }

        return tokens;
    }

}
