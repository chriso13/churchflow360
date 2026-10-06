package com.churchflow360.smallgroups.repository;

import com.churchflow360.smallgroups.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SmallGroupRepository extends JpaRepository<SmallGroup, UUID> {
    Page<SmallGroup> findByStatusOrderByNameAsc(GroupStatus status, Pageable pageable);
    Page<SmallGroup> findByGroupTypeOrderByNameAsc(GroupType type, Pageable pageable);
    List<SmallGroup> findByLeaderMembershipNumberOrderByNameAsc(String membershipNumber);

    @Query("SELECT g FROM SmallGroup g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(g.description) LIKE LOWER(CONCAT('%',:q,'%'))")
    Page<SmallGroup> search(@Param("q") String query, Pageable pageable);

    long countByStatus(GroupStatus status);
    long countByGroupType(GroupType type);
}
