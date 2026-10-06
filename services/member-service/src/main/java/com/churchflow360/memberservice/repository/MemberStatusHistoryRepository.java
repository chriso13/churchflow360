package com.churchflow360.memberservice.repository;

import com.churchflow360.memberservice.model.MemberStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemberStatusHistoryRepository extends JpaRepository<MemberStatusHistory, UUID> {

    List<MemberStatusHistory> findByMemberIdOrderByChangedAtDesc(UUID memberId);

    Page<MemberStatusHistory> findByMemberId(UUID memberId, Pageable pageable);
}