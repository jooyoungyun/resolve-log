package com.resolvelog.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
  public record ErrorBody(String code, String message, Map<String, String> fieldErrors) {}

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorBody> domain(ApiException e) {
    return ResponseEntity.status(e.status).body(new ErrorBody(e.code, e.getMessage(), Map.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest()
        .body(new ErrorBody("VALIDATION_ERROR", "입력값을 확인해 주세요.", errors));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<ErrorBody> malformed(Exception e) {
    return ResponseEntity.badRequest()
        .body(new ErrorBody("INVALID_INPUT", "날짜 또는 입력 형식이 올바르지 않습니다.", Map.of()));
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    ObjectOptimisticLockingFailureException.class
  })
  ResponseEntity<ErrorBody> conflict(Exception e) {
    return ResponseEntity.status(409)
        .body(new ErrorBody("CONFLICT", "중복된 값이거나 다른 창에서 변경되었습니다. 새로고침 후 확인해 주세요.", Map.of()));
  }
}
