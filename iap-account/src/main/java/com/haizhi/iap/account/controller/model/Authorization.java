package com.haizhi.iap.account.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Authorization {

    public static final String DEFAULT_TOKEN_TYPE = "Bearer";

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("token_type")
    private String tokenType = DEFAULT_TOKEN_TYPE;

    @JsonProperty("expires_in")
    private long expiresIn;

    private String state;

    private String code;

    @JsonProperty("client_id")
    private String clientId;

    private String openid;

    private String phone;

    private String transactionKey;

    private String platform;

}
