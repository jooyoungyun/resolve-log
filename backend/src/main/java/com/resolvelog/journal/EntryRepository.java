package com.resolvelog.journal;

import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface EntryRepository
    extends JpaRepository<JournalEntry, UUID>, JpaSpecificationExecutor<JournalEntry> {
  Optional<JournalEntry> findByGoalIdAndEntryDate(UUID goalId, LocalDate date);

  boolean existsByGoalIdAndEntryDateBefore(UUID goalId, LocalDate date);

  boolean existsByGoalIdAndEntryDateAfter(UUID goalId, LocalDate date);

  @EntityGraph(attributePaths = "goal")
  List<JournalEntry> findByGoalOwnerIdAndEntryDateBetweenOrderByEntryDateDescUpdatedAtDesc(
      UUID owner, LocalDate from, LocalDate to);
}
