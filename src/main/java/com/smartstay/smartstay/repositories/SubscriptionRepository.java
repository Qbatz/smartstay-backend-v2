package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    Subscription findTopByHostel_HostelIdOrderByCreatedAtDesc(String hostelId);

    Optional<Subscription> findBySubscriptionIdAndHostel_HostelId(String subscriptionId, String hostelId);
}
