package com.resolvelog.goal;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
  @EntityGraph(attributePaths = "weekdays")
  List<Goal> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

  Optional<Goal> findByIdAndOwnerId(UUID id, UUID ownerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select g from Goal g where g.id=:id and g.ownerId=:owner")
  Optional<Goal> lockOwned(@Param("id") UUID id, @Param("owner") UUID owner);
}
