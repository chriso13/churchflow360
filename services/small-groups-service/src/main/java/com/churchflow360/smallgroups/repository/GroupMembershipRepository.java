package com.churchflow360.smallgroups.repository;

import com.churchflow360.smallgroups.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, UUID> {
    List<GroupMembership> findByGroupIdAndIsActiveTrueOrderByRoleAsc(UUID groupId);
    List<GroupMembership> findByMembershipNumberAndIsActiveTrue(String membershipNumber);
    Optional<GroupMembership> findByGroupIdAndMembershipNumber(UUID groupId, String membershipNumber);
    long countByGroupIdAndIsActiveTrue(UUID groupId);
    boolean existsByGroupIdAndMembershipNumberAndIsActiveTrue(UUID groupId, String membershipNumber);
}
