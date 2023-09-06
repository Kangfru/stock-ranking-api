package com.kangfru.paysecuritiestest.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
public class BaseResponse {

    private String resultCode;

    private String resultMessage;

}
