package com.churchflow360.financeservice.repository;

import com.churchflow360.financeservice.model.OfferingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface OfferingItemRepository extends JpaRepository<OfferingItem, UUID> {

    List<OfferingItem> findByOfferingIdOrderByCreatedAtAsc(UUID offeringId);

    List<OfferingItem> findByMembershipNumberOrderByCreatedAtDesc(String membershipNumber);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM OfferingItem i WHERE i.offering.id = :offeringId")
    BigDecimal sumByOfferingId(@Param("offeringId") UUID offeringId);
}