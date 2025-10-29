package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
