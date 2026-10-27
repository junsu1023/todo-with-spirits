package com.oow.todowithspirit.domain.currency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyTransactionRepository extends JpaRepository<CurrencyTransaction, Long> {

    Optional<CurrencyTransaction> findByTaskIdAndTaskCompletionDateAndReason(
            Long taskId, LocalDate taskCompletionDate, CurrencyReason reason);

    List<CurrencyTransaction> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);
}
