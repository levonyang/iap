package com.haizhi.iap.follow.utils;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Created by chenbo on 17/1/9.
 */
public class MultipartFileUtil {

    public static final String TXT = "txt";
    public static final String XLS = "xls";
    public static final String XLSX = "xlsx";

    public static String readExtension(String filename) {
        return filename.indexOf(".") < 0 ? "" : filename.substring(filename.lastIndexOf(".") + 1, filename.length());
    }

    public static String readFilename(MultipartBody body, String contentId) {
        Attachment attachment = body.getAttachment(contentId);
        if (attachment != null) {
            return readFilename(attachment.getHeaders());
        }
        return null;
    }

    public static String readFilename(MultivaluedMap header) {
        String[] contentDisposition = String.valueOf(header.getFirst("Content-Disposition")).split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String exactFileName = name[1].trim().replaceAll("\"", "");
                return exactFileName;
            }
        }
        return "unknown";
    }
}
