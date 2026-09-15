package com.resolvelog.goal;

import com.resolvelog.common.ApiException;
import com.resolvelog.journal.EntryRepository;
import java.time.Clock;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoalService {
  private final GoalRepository goals;
  private final EntryRepository entries;
  private final Clock clock;

  public GoalService(GoalRepository goals, EntryRepository entries, Clock clock) {
    this.goals = goals;
    this.entries = entries;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public List<GoalDtos.View> list(UUID owner) {
    return goals.findByOwnerIdOrderByCreatedAtDesc(owner).stream().map(GoalDtos.View::of).toList();
  }

  @Transactional(readOnly = true)
  public GoalDtos.View get(UUID owner, UUID id) {
    return GoalDtos.View.of(owned(owner, id));
  }

  public Goal owned(UUID owner, UUID id) {
    return goals.findByIdAndOwnerId(id, owner).orElseThrow(ApiException::missing);
  }

  public Goal lock(UUID owner, UUID id) {
    return goals.lockOwned(id, owner).orElseThrow(ApiException::missing);
  }

  @Transactional
  public GoalDtos.View create(UUID owner, GoalDtos.Request r) {
    validate(r);
    return GoalDtos.View.of(goals.saveAndFlush(new Goal(owner, r, clock.instant())));
  }

  @Transactional
  public GoalDtos.View update(UUID owner, UUID id, GoalDtos.Request r) {
    validate(r);
    Goal goal = lock(owner, id);
    if (r.version() == null || r.version() != goal.getVersion())
      throw new ApiException(
          HttpStatus.CONFLICT, "STALE_VERSION", "다른 창에서 변경된 결심입니다. 새로고침 후 다시 수정해 주세요.");
    if (entries.existsByGoalIdAndEntryDateBefore(id, r.startDate())
        || (r.endDate() != null && entries.existsByGoalIdAndEntryDateAfter(id, r.endDate())))
      throw ApiException.bad("기존 일지의 날짜가 포함되도록 결심 기간을 설정해 주세요.");
    goal.apply(r, clock.instant());
    goals.flush();
    return GoalDtos.View.of(goal);
  }

  @Transactional
  public void delete(UUID owner, UUID id) {
    goals.delete(lock(owner, id));
    goals.flush();
  }

  private void validate(GoalDtos.Request r) {
    if (r.endDate() != null && r.endDate().isBefore(r.startDate()))
      throw ApiException.bad("종료일은 시작일 이후여야 합니다.");
    if (new HashSet<>(r.weekdays()).size() != r.weekdays().size())
      throw ApiException.bad("반복 요일이 중복되었습니다.");
  }
}
