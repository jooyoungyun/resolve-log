package com.resolvelog.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUser {
  @Id private UUID id;

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  @Column(nullable = false, length = 30)
  private String displayName;

  @Column(nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false)
  private Instant createdAt;

  protected AppUser() {}

  public AppUser(String email, String displayName, String passwordHash) {
    this.id = UUID.randomUUID();
    this.email = email;
    this.displayName = displayName;
    this.passwordHash = passwordHash;
    this.createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPasswordHash() {
    return passwordHash;
  }
}
