package com.resolvelog.journal;

import com.resolvelog.goal.Goal;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

public final class EntryDtos {
  private EntryDtos() {}

  public record Request(
      @NotNull JournalEntry.Status status,
      @Size(max = 4000) String note,
      @Min(0) @Max(1440) int minutes) {}

  public record View(
      UUID id,
      UUID goalId,
      String goalTitle,
      Goal.Category category,
      LocalDate date,
      JournalEntry.Status status,
      String note,
      int minutes,
      Instant updatedAt) {
    public static View of(JournalEntry e) {
      return new View(
          e.getId(),
          e.getGoal().getId(),
          e.getGoal().getTitle(),
          e.getGoal().getCategory(),
          e.getEntryDate(),
          e.getStatus(),
          e.getNote(),
          e.getMinutes(),
          e.getUpdatedAt());
    }
  }

  public record PageView(List<View> items, long totalElements, int currentPage, int totalPages) {}
}
