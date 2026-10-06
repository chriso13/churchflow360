package com.churchflow360.memberservice.repository;

import com.churchflow360.memberservice.model.Member;
import com.churchflow360.memberservice.model.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID>,
        JpaSpecificationExecutor<Member> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    // ── Find by membership number ──────────────────────────────────────────────
    Optional<Member> findByMembershipNumber(String membershipNumber);

    Page<Member> findByMembershipStatus(MemberStatus status, Pageable pageable);

    // ── Search WITHOUT status filter ───────────────────────────────────────────
    @Query(
            value = """
            SELECT * FROM public.members m
            WHERE (CAST(:search AS text) IS NULL
                OR LOWER(m.first_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
                OR LOWER(m.last_name)  LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
                OR LOWER(m.email)      LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))
            """,
            countQuery = """
            SELECT COUNT(*) FROM public.members m
            WHERE (CAST(:search AS text) IS NULL
                OR LOWER(m.first_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
                OR LOWER(m.last_name)  LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
                OR LOWER(m.email)      LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))
            """,
            nativeQuery = true
    )
    Page<Member> searchWithoutStatus(@Param("search") String search,
                                     Pageable pageable);

    // ── Search WITH status filter ──────────────────────────────────────────────
    @Query(
            value = """
            SELECT * FROM public.members m
            WHERE m.membership_status = CAST(:status AS member_status)
            AND (CAST(:search AS text) IS NULL
                OR LOWER(m.first_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
                OR LOWER(m.last_name)  LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
                OR LOWER(m.email)      LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))
            """,
            countQuery = """
            SELECT COUNT(*) FROM public.members m
            WHERE m.membership_status = CAST(:status AS member_status)
            AND (CAST(:search AS text) IS NULL
                OR LOWER(m.first_name) LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
                OR LOWER(m.last_name)  LIKE LOWER(CONCAT('%', CAST(:search AS text), '%'))
                OR LOWER(m.email)      LIKE LOWER(CONCAT('%', CAST(:search AS text), '%')))
            """,
            nativeQuery = true
    )
    Page<Member> searchWithStatus(@Param("status") String status,
                                  @Param("search") String search,
                                  Pageable pageable);

    long countByMembershipStatus(MemberStatus status);
}