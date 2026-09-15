package com.resolvelog.goal;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "goal")
public class Goal {
  public enum Category {
    HEALTH,
    STUDY,
    WORK,
    LIFE,
    FINANCE,
    OTHER
  }

  @Id private UUID id;

  @Column(nullable = false)
  private UUID ownerId;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, length = 1000)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Category category;

  @Column(nullable = false)
  private LocalDate startDate;

  private LocalDate endDate;

  @Column(nullable = false)
  private boolean archived;

  @Version private long version;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @ElementCollection
  @CollectionTable(name = "goal_weekday", joinColumns = @JoinColumn(name = "goal_id"))
  @Column(name = "weekday", nullable = false)
  private Set<Integer> weekdays = new HashSet<>();

  protected Goal() {}

  public Goal(UUID ownerId, GoalDtos.Request r, Instant now) {
    this.id = UUID.randomUUID();
    this.ownerId = ownerId;
    this.createdAt = now;
    apply(r, now);
  }

  public void apply(GoalDtos.Request r, Instant now) {
    title = r.title().trim();
    reason = r.reason() == null ? "" : r.reason().trim();
    category = r.category();
    startDate = r.startDate();
    endDate = r.endDate();
    archived = r.archived();
    weekdays.clear();
    weekdays.addAll(r.weekdays());
    updatedAt = now;
  }

  public boolean scheduled(LocalDate date) {
    return !archived
        && !date.isBefore(startDate)
        && (endDate == null || !date.isAfter(endDate))
        && weekdays.contains(date.getDayOfWeek().getValue());
  }

  public UUID getId() {
    return id;
  }

  public UUID getOwnerId() {
    return ownerId;
  }

  public String getTitle() {
    return title;
  }

  public String getReason() {
    return reason;
  }

  public Category getCategory() {
    return category;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public boolean isArchived() {
    return archived;
  }

  public long getVersion() {
    return version;
  }

  public Set<Integer> getWeekdays() {
    return weekdays;
  }
}
