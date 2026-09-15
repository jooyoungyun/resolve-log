package com.resolvelog.auth;

import com.resolvelog.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  public record UserView(UUID id, String email, String displayName) {
    public static UserView of(AppUser u) {
      return new UserView(u.getId(), u.getEmail(), u.getDisplayName());
    }
  }

  public record AuthView(String accessToken, UserView user) {}

  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final JwtEncoder encoder;
  private final Clock clock;
  private final long hours;
  private final String dummyHash;

  public AuthService(
      UserRepository users,
      PasswordEncoder passwords,
      JwtEncoder encoder,
      Clock clock,
      @Value("${app.token-hours}") long hours) {
    this.users = users;
    this.passwords = passwords;
    this.encoder = encoder;
    this.clock = clock;
    this.hours = hours;
    this.dummyHash = passwords.encode(UUID.randomUUID().toString());
  }

  @Transactional
  public AuthView register(String email, String displayName, String password) {
    email = email.trim().toLowerCase(Locale.ROOT);
    if (password.getBytes(StandardCharsets.UTF_8).length > 72)
      throw ApiException.bad("비밀번호는 UTF-8 기준 72바이트 이하여야 합니다.");
    if (users.existsByEmail(email))
      throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "이미 가입된 이메일입니다.");
    var user =
        users.saveAndFlush(new AppUser(email, displayName.trim(), passwords.encode(password)));
    return token(user);
  }

  @Transactional(readOnly = true)
  public AuthView login(String email, String password) {
    var user = users.findByEmail(email.trim().toLowerCase(Locale.ROOT)).orElse(null);
    boolean matches =
        password.getBytes(StandardCharsets.UTF_8).length <= 72
            && passwords.matches(password, user == null ? dummyHash : user.getPasswordHash());
    if (user == null || !matches)
      throw new ApiException(HttpStatus.UNAUTHORIZED, "LOGIN_FAILED", "이메일 또는 비밀번호를 확인해 주세요.");
    return token(user);
  }

  @Transactional(readOnly = true)
  public UserView me(UUID id) {
    return UserView.of(users.findById(id).orElseThrow(ApiException::missing));
  }

  private AuthView token(AppUser user) {
    Instant now = clock.instant();
    var claims =
        JwtClaimsSet.builder()
            .issuer("resolve-log")
            .subject(user.getId().toString())
            .audience(List.of("resolve-log-web"))
            .issuedAt(now)
            .expiresAt(now.plus(Duration.ofHours(hours)))
            .build();
    var token =
        encoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    return new AuthView(token, UserView.of(user));
  }
}
