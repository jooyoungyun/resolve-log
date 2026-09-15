package com.resolvelog.goal;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;

public final class GoalDtos {
  private GoalDtos() {}

  public record Request(
      @NotBlank @Size(max = 100) String title,
      @Size(max = 1000) String reason,
      @NotNull Goal.Category category,
      @NotNull LocalDate startDate,
      LocalDate endDate,
      @NotEmpty @Size(max = 7) List<@NotNull @Min(1) @Max(7) Integer> weekdays,
      boolean archived,
      Long version) {}

  public record View(
      UUID id,
      String title,
      String reason,
      Goal.Category category,
      LocalDate startDate,
      LocalDate endDate,
      List<Integer> weekdays,
      boolean archived,
      long version) {
    public static View of(Goal g) {
      return new View(
          g.getId(),
          g.getTitle(),
          g.getReason(),
          g.getCategory(),
          g.getStartDate(),
          g.getEndDate(),
          g.getWeekdays().stream().sorted().toList(),
          g.isArchived(),
          g.getVersion());
    }
  }
}
