package com.oow.todowithspirit.service;

import com.oow.todowithspirit.domain.currency.CurrencyReason;
import com.oow.todowithspirit.domain.currency.CurrencyTransaction;
import com.oow.todowithspirit.domain.currency.CurrencyTransactionRepository;
import com.oow.todowithspirit.domain.currency.CurrencyWallet;
import com.oow.todowithspirit.domain.currency.CurrencyWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CurrencyService {

    private final CurrencyWalletRepository currencyWalletRepository;
    private final CurrencyTransactionRepository currencyTransactionRepository;

    public long getBalance(Long userId) {
        return currencyWalletRepository.findById(userId)
                .map(CurrencyWallet::getBalance)
                .orElse(0L);
    }

    public List<CurrencyTransaction> getRecentTransactions(Long userId) {
        return currencyTransactionRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 할 일 완료에 대한 재화를 지급한다. 같은 (taskId, completionDate)에는 한 번만 지급된다 — 동시 요청이
     * 겹쳐도 DB UNIQUE 제약(task_id, task_completion_date)이 두 번째 삽입을 막아주므로 멱등하다.
     * @return 이번 호출로 실제 지급된 금액 (이미 지급된 적이 있으면 0)
     */
    @Transactional
    public long creditForTaskComplete(Long userId, long amount, Long taskId, LocalDate completionDate) {
        CurrencyTransaction transaction = CurrencyTransaction.ofTaskComplete(userId, amount, taskId, completionDate);
        try {
            currencyTransactionRepository.saveAndFlush(transaction);
        } catch (DataIntegrityViolationException e) {
            log.info("[creditForTaskComplete] Already credited. userId={}, taskId={}, date={}", userId, taskId, completionDate);
            return 0L;
        }

        getOrCreateWallet(userId).credit(amount);
        return amount;
    }

    /**
     * 완료 취소 시 해당 지급 내역을 되돌린다. 지급된 적이 없으면(이미 취소됐거나 원래 지급 실패) 아무 일도 하지 않는다.
     */
    @Transactional
    public void reverseTaskComplete(Long userId, Long taskId, LocalDate completionDate) {
        currencyTransactionRepository
                .findByTaskIdAndTaskCompletionDateAndReason(taskId, completionDate, CurrencyReason.TASK_COMPLETE)
                .ifPresent(transaction -> {
                    currencyTransactionRepository.delete(transaction);
                    getOrCreateWallet(userId).debit(transaction.getAmount());
                });
    }

    private CurrencyWallet getOrCreateWallet(Long userId) {
        return currencyWalletRepository.findById(userId)
                .orElseGet(() -> currencyWalletRepository.save(new CurrencyWallet(userId)));
    }
}
