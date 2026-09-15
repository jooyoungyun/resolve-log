package com.resolvelog.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  record Register(
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(min = 2, max = 30) String displayName,
      @NotBlank @Size(min = 8, max = 64) String password) {}

  record Login(
      @NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 64) String password) {}

  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  AuthService.AuthView register(@Valid @RequestBody Register r) {
    return service.register(r.email(), r.displayName(), r.password());
  }

  @PostMapping("/login")
  AuthService.AuthView login(@Valid @RequestBody Login r) {
    return service.login(r.email(), r.password());
  }

  @GetMapping("/me")
  AuthService.UserView me(@AuthenticationPrincipal Jwt jwt) {
    return service.me(UUID.fromString(jwt.getSubject()));
  }
}
