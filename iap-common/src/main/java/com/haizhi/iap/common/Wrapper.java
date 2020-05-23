package com.haizhi.iap.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

/**
 * Created by chenbo on 16/6/15.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wrapper {

    /**
     * @deprecated <br/>
     * bad design. use {@link #ok(Object)} instead <br/>
     * 一方面，OKBuilder是单例的，多个http request设置其data属性，高并发情况下会导致数据不一致。<br/>
     * 另一方面，http是无状态协议，OKBuilder的设计违背了这个初衷。
     */
    public static final WrapperBuilder OKBuilder = Wrapper.builder().status(0).msg("OK");
    public static final Wrapper OK = Wrapper.builder().status(0).msg("OK").build();

    public static final Wrapper ERROR = Wrapper.builder().status(-1).msg("ERROR").build();
    /**
     * @deprecated <br/>
     * bad design. Same reason with OKBuilder. use {@link #error(String)} instead
     */
    public static final WrapperBuilder ERRORBuilder = Wrapper.builder().status(-1);

    private int status;

    private String msg;

    private Object data;

    public Wrapper(int status, String msg) {
        this(status, msg, null);
    }

    public static Wrapper ok(Object data)
    {
        return Wrapper.builder().status(0).msg("OK").data(data).build();
    }

    public static Wrapper error(String msg)
    {
        return Wrapper.builder().status(-1).msg(msg).data(null).build();
    }

    public String json() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce("status", status);
        jsonObject.putOnce("msg", msg);
        return jsonObject.toString();
    }
}
