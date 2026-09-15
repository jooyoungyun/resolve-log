package com.resolvelog.journal;

import com.resolvelog.common.ApiException;
import com.resolvelog.goal.GoalService;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JournalService {
  private final GoalService goals;
  private final EntryRepository entries;
  private final Clock clock;

  public JournalService(GoalService goals, EntryRepository entries, Clock clock) {
    this.goals = goals;
    this.entries = entries;
    this.clock = clock;
  }

  @Transactional
  public EntryDtos.View save(UUID owner, UUID goalId, LocalDate date, EntryDtos.Request r) {
    if (date.isAfter(LocalDate.now(clock))) throw ApiException.bad("미래 날짜의 수행 내역은 기록할 수 없습니다.");
    var goal = goals.lock(owner, goalId);
    var existing = entries.findByGoalIdAndEntryDate(goalId, date);
    if (existing.isEmpty() && !goal.scheduled(date))
      throw ApiException.bad("결심 기간과 반복 요일에 해당하는 날짜에 기록해 주세요. 보관한 결심에는 새 기록을 추가할 수 없습니다.");
    var entry = existing.orElseGet(() -> new JournalEntry(goal, date, r, clock.instant()));
    entry.apply(r, clock.instant());
    return EntryDtos.View.of(entries.saveAndFlush(entry));
  }

  @Transactional
  public void delete(UUID owner, UUID goalId, LocalDate date) {
    goals.lock(owner, goalId);
    entries.findByGoalIdAndEntryDate(goalId, date).ifPresent(entries::delete);
  }

  @Transactional(readOnly = true)
  public EntryDtos.PageView list(
      UUID owner,
      LocalDate from,
      LocalDate to,
      UUID goalId,
      JournalEntry.Status status,
      int page,
      int size) {
    validateRange(from, to);
    if (page < 1 || size < 1 || size > 100)
      throw ApiException.bad("페이지는 1 이상, 조회 크기는 1~100이어야 합니다.");
    Specification<JournalEntry> spec =
        (root, q, cb) -> {
          var conditions = new ArrayList<jakarta.persistence.criteria.Predicate>();
          conditions.add(cb.equal(root.get("goal").get("ownerId"), owner));
          conditions.add(cb.between(root.get("entryDate"), from, to));
          if (goalId != null) conditions.add(cb.equal(root.get("goal").get("id"), goalId));
          if (status != null) conditions.add(cb.equal(root.get("status"), status));
          return cb.and(conditions.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    var result =
        entries.findAll(
            spec,
            PageRequest.of(
                page - 1, size, Sort.by(Sort.Direction.DESC, "entryDate", "updatedAt", "id")));
    return new EntryDtos.PageView(
        result.map(EntryDtos.View::of).getContent(),
        result.getTotalElements(),
        page,
        Math.max(1, result.getTotalPages()));
  }

  public static void validateRange(LocalDate from, LocalDate to) {
    if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 366)
      throw ApiException.bad("조회 기간은 시작일 이후, 최대 367일 이내로 설정해 주세요.");
  }
}
