package com.haizhi.iap.follow.controller.model;

import com.google.common.base.Strings;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;

@Data
public class FileView {

    private String data;

    private String id;

    private String uri;

    public boolean checkBase64() {
        if (Strings.isNullOrEmpty(data)) {
            return false;
        }
        if (!data.contains(";base64,")) {
            return false;
        }
        return true;
    }

    public String contentType() {
        return data.substring(
                data.indexOf("data:") + 5,
                data.indexOf(";base64,")
        );
    }

    public byte[] bytes() {
        return Base64.decodeBase64(
                data.substring(data.indexOf(";base64,") + 8)
        );
    }
}