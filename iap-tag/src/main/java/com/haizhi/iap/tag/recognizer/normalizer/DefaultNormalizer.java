package com.haizhi.iap.tag.recognizer.normalizer;


import com.haizhi.iap.tag.recognizer.util.NormalizeUtil;

public class DefaultNormalizer implements Normalizer {

    public String normalize(String str) {
        if (str == null)
            return "";
        // 全角转半角
        String normalizedStr = NormalizeUtil.doubleChar2SingleChar(str);
        // fix全角转半角的遗漏c2a0
        normalizedStr = NormalizeUtil.replaceC2A0(normalizedStr);
        // 繁体转简体
        normalizedStr = NormalizeUtil.traditional2Simplified2(normalizedStr);
        // 合并多个空格
        normalizedStr = NormalizeUtil.normalizeWhitespace(normalizedStr);
        // 去掉非英文字母之间的空格
        normalizedStr = NormalizeUtil.removeUselessSpace(normalizedStr);

        return normalizedStr.toLowerCase().trim();
    }

}
