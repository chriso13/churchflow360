package com.churchflow360.authservice.repository;

import com.churchflow360.authservice.model.AccountStatus;
import com.churchflow360.authservice.model.Role;
import com.churchflow360.authservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByMembershipNumber(String membershipNumber);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByMembershipNumber(String membershipNumber);

    Page<User> findByRoleOrderByFullNameAsc(Role role, Pageable pageable);
    Page<User> findByStatusOrderByCreatedAtDesc(AccountStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<User> search(@Param("q") String query, Pageable pageable);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.lockedUntil = null WHERE u.id = :id")
    void resetFailedAttempts(@Param("id") UUID id);

    Page<User> findByRole(Role role, Pageable pageable);
}