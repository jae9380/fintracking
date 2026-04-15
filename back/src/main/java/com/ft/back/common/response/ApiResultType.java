package com.ft.back.common.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiResultType {
    SUCCESS,
    CREATED,
    NO_CONTENT,
    ERROR;
}