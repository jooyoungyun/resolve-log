package com.resolvelog.goal;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
  private final GoalService service;

  public GoalController(GoalService service) {
    this.service = service;
  }

  @GetMapping
  List<GoalDtos.View> list(@AuthenticationPrincipal Jwt jwt) {
    return service.list(owner(jwt));
  }

  @GetMapping("/{id}")
  GoalDtos.View get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    return service.get(owner(jwt), id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  GoalDtos.View create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody GoalDtos.Request r) {
    return service.create(owner(jwt), r);
  }

  @PutMapping("/{id}")
  GoalDtos.View update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @Valid @RequestBody GoalDtos.Request r) {
    return service.update(owner(jwt), id, r);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
    service.delete(owner(jwt), id);
  }

  private UUID owner(Jwt jwt) {
    return UUID.fromString(jwt.getSubject());
  }
}
