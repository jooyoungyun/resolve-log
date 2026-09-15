package com.resolvelog.common;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
  public final HttpStatus status;
  public final String code;

  public ApiException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public static ApiException bad(String message) {
    return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_INPUT", message);
  }

  public static ApiException missing() {
    return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "항목을 찾을 수 없습니다.");
  }
}
