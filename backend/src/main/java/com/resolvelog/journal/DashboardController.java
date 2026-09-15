package com.resolvelog.journal;

import com.resolvelog.goal.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Transactional(readOnly = true)
public class DashboardController {
  public record Card(GoalDtos.View goal, EntryDtos.View entry) {}

  public record DayCount(LocalDate date, long done, long partial, long skipped, int minutes) {}

  public record Dashboard(
      LocalDate date,
      LocalDate today,
      List<Card> items,
      long doneCount,
      int targetCount,
      int minutes,
      int completionRate,
      List<DayCount> week) {}

  public record CalendarView(String month, List<DayCount> days) {}

  private final GoalRepository goals;
  private final EntryRepository entries;
  private final Clock clock;

  public DashboardController(GoalRepository goals, EntryRepository entries, Clock clock) {
    this.goals = goals;
    this.entries = entries;
    this.clock = clock;
  }

  @GetMapping("/dashboard")
  Dashboard dashboard(
      @AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) LocalDate date) {
    UUID owner = UUID.fromString(jwt.getSubject());
    LocalDate selected = date == null ? LocalDate.now(clock) : date;
    var records =
        entries.findByGoalOwnerIdAndEntryDateBetweenOrderByEntryDateDescUpdatedAtDesc(
            owner, selected.minusDays(6), selected);
    var todayEntries =
        records.stream()
            .filter(e -> e.getEntryDate().equals(selected))
            .collect(Collectors.toMap(e -> e.getGoal().getId(), e -> e));
    var cards =
        goals.findByOwnerIdOrderByCreatedAtDesc(owner).stream()
            .filter(g -> g.scheduled(selected) || todayEntries.containsKey(g.getId()))
            .map(
                g ->
                    new Card(
                        GoalDtos.View.of(g),
                        todayEntries.containsKey(g.getId())
                            ? EntryDtos.View.of(todayEntries.get(g.getId()))
                            : null))
            .toList();
    long done =
        cards.stream()
            .filter(c -> c.entry() != null && c.entry().status() == JournalEntry.Status.DONE)
            .count();
    int minutes = todayEntries.values().stream().mapToInt(JournalEntry::getMinutes).sum();
    return new Dashboard(
        selected,
        LocalDate.now(clock),
        cards,
        done,
        cards.size(),
        minutes,
        cards.isEmpty() ? 0 : (int) Math.round(100.0 * done / cards.size()),
        counts(records, selected.minusDays(6), selected));
  }

  @GetMapping("/calendar")
  CalendarView calendar(@AuthenticationPrincipal Jwt jwt, @RequestParam String month) {
    YearMonth ym;
    try {
      ym = YearMonth.parse(month);
    } catch (Exception e) {
      throw com.resolvelog.common.ApiException.bad("월은 YYYY-MM 형식으로 입력해 주세요.");
    }
    var records =
        entries.findByGoalOwnerIdAndEntryDateBetweenOrderByEntryDateDescUpdatedAtDesc(
            UUID.fromString(jwt.getSubject()), ym.atDay(1), ym.atEndOfMonth());
    return new CalendarView(ym.toString(), counts(records, ym.atDay(1), ym.atEndOfMonth()));
  }

  private List<DayCount> counts(List<JournalEntry> records, LocalDate from, LocalDate to) {
    var grouped = records.stream().collect(Collectors.groupingBy(JournalEntry::getEntryDate));
    return from.datesUntil(to.plusDays(1))
        .map(
            d -> {
              var rows = grouped.getOrDefault(d, List.of());
              return new DayCount(
                  d,
                  count(rows, JournalEntry.Status.DONE),
                  count(rows, JournalEntry.Status.PARTIAL),
                  count(rows, JournalEntry.Status.SKIPPED),
                  rows.stream().mapToInt(JournalEntry::getMinutes).sum());
            })
        .toList();
  }

  private long count(List<JournalEntry> rows, JournalEntry.Status status) {
    return rows.stream().filter(e -> e.getStatus() == status).count();
  }
}
