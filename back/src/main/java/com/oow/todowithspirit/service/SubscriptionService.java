package com.oow.todowithspirit.service;

import com.oow.todowithspirit.domain.subscription.Subscription;
import com.oow.todowithspirit.domain.subscription.SubscriptionRepository;
import com.oow.todowithspirit.dto.subscription.SubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public SubscriptionResponse getSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseGet(() -> subscriptionRepository.save(Subscription.createDefault(userId)));

        return SubscriptionResponse.from(subscription);
    }
}