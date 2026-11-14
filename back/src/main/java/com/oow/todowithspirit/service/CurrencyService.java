package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.currency.CurrencyReason;
import com.oow.todowithspirit.domain.currency.CurrencyTransaction;
import com.oow.todowithspirit.domain.currency.CurrencyTransactionRepository;
import com.oow.todowithspirit.domain.currency.CurrencyWallet;
import com.oow.todowithspirit.domain.currency.CurrencyWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 할 일 완료에 대한 재화를 지급한다. 같은 (taskId, completionDate)에는 한 번만 지급된다.
     * 이미 지급 내역이 있으면 INSERT 하지 않고 0을 반환한다.
     * (UNIQUE 위반 예외를 잡는 방식은 호출한 트랜잭션 전체를 rollback-only로 만들어 500이 나므로 사용하지 않음)
     * @return 이번 호출로 실제 지급된 금액 (이미 지급된 적이 있으면 0)
     */
    @Transactional
    public long creditForTaskComplete(Long userId, long amount, Long taskId, LocalDate completionDate) {
        if (currencyTransactionRepository.existsByTaskIdAndTaskCompletionDateAndReason(
                taskId, completionDate, CurrencyReason.TASK_COMPLETE)) {
            log.info("[creditForTaskComplete] Already credited. userId={}, taskId={}, date={}", userId, taskId, completionDate);
            return 0L;
        }

        currencyTransactionRepository.save(CurrencyTransaction.ofTaskComplete(userId, amount, taskId, completionDate));
        getOrCreateWallet(userId).credit(amount);
        return amount;
    }

    /**
     * 완료 취소 시 해당 날짜의 지급 내역을 되돌린다. 지급된 적이 없으면 아무 일도 하지 않는다.
     */
    @Transactional
    public void reverseTaskComplete(Long userId, Long taskId, LocalDate completionDate) {
        currencyTransactionRepository
                .findByTaskIdAndTaskCompletionDateAndReason(taskId, completionDate, CurrencyReason.TASK_COMPLETE)
                .ifPresent(transaction -> reverse(userId, transaction));
    }

    /**
     * 단발성 할 일 완료 취소용: 날짜와 상관없이 해당 할 일의 지급 내역을 모두 되돌린다.
     */
    @Transactional
    public void reverseAllTaskComplete(Long userId, Long taskId) {
        currencyTransactionRepository.findAllByTaskIdAndReason(taskId, CurrencyReason.TASK_COMPLETE)
                .forEach(transaction -> reverse(userId, transaction));
    }

    private void reverse(Long userId, CurrencyTransaction transaction) {
        currencyTransactionRepository.delete(transaction);
        getOrCreateWallet(userId).debit(transaction.getAmount());
    }

    /**
     * task와 무관한 재화 소비(장식 구매, 정령 만나기 등). 잔액이 모자라면 차감 없이 예외를 던진다.
     */
    @Transactional
    public void debit(Long userId, long amount, CurrencyReason reason) {
        if (getBalance(userId) < amount) {
            throw new ApiException(ErrorCode.INSUFFICIENT_BALANCE, "잔액이 부족합니다");
        }

        currencyTransactionRepository.save(CurrencyTransaction.of(userId, -amount, reason));
        getOrCreateWallet(userId).debit(amount);
    }

    private CurrencyWallet getOrCreateWallet(Long userId) {
        return currencyWalletRepository.findById(userId)
                .orElseGet(() -> currencyWalletRepository.save(new CurrencyWallet(userId)));
    }
}
