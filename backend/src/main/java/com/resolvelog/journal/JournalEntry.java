package com.resolvelog.journal;

import com.resolvelog.goal.Goal;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(
    name = "journal_entry",
    uniqueConstraints = @UniqueConstraint(columnNames = {"goal_id", "entry_date"}))
public class JournalEntry {
  public enum Status {
    DONE,
    PARTIAL,
    SKIPPED
  }

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "goal_id", nullable = false)
  private Goal goal;

  @Column(nullable = false)
  private LocalDate entryDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Status status;

  @Column(nullable = false, length = 4000)
  private String note;

  @Column(nullable = false)
  private int minutes;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected JournalEntry() {}

  public JournalEntry(Goal goal, LocalDate date, EntryDtos.Request r, Instant now) {
    id = UUID.randomUUID();
    this.goal = goal;
    entryDate = date;
    createdAt = now;
    apply(r, now);
  }

  public void apply(EntryDtos.Request r, Instant now) {
    status = r.status();
    note = r.note() == null ? "" : r.note().trim();
    minutes = r.minutes();
    updatedAt = now;
  }

  public UUID getId() {
    return id;
  }

  public Goal getGoal() {
    return goal;
  }

  public LocalDate getEntryDate() {
    return entryDate;
  }

  public Status getStatus() {
    return status;
  }

  public String getNote() {
    return note;
  }

  public int getMinutes() {
    return minutes;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
