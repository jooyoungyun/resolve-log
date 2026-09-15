package com.resolvelog.journal;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class JournalController {
  private final JournalService service;

  public JournalController(JournalService service) {
    this.service = service;
  }

  @PutMapping("/goals/{goalId}/entries/{date}")
  EntryDtos.View save(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID goalId,
      @PathVariable LocalDate date,
      @Valid @RequestBody EntryDtos.Request r) {
    return service.save(UUID.fromString(jwt.getSubject()), goalId, date, r);
  }

  @DeleteMapping("/goals/{goalId}/entries/{date}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(
      @AuthenticationPrincipal Jwt jwt, @PathVariable UUID goalId, @PathVariable LocalDate date) {
    service.delete(UUID.fromString(jwt.getSubject()), goalId, date);
  }

  @GetMapping("/entries")
  EntryDtos.PageView list(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam LocalDate from,
      @RequestParam LocalDate to,
      @RequestParam(required = false) UUID goalId,
      @RequestParam(required = false) JournalEntry.Status status,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.list(UUID.fromString(jwt.getSubject()), from, to, goalId, status, page, size);
  }
}
