package com.harsh.expense.auth;

import com.harsh.expense.user.AppUser;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findAllByUserAndRevokedAtIsNull(AppUser user);

  @Modifying
  @Query(
      "update RefreshToken t set t.revokedAt = :now, t.reason = :reason where t.user = :user and t.revokedAt is null")
  int revokeAllActiveForUser(
      @Param("user") AppUser user, @Param("now") Instant now, @Param("reason") String reason);
}
