package com.oow.todowithspirit.dto.currency;

import com.oow.todowithspirit.domain.currency.CurrencyTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CurrencyResponse {

    private final long balance;
    private final List<TransactionEntry> transactions;

    public static CurrencyResponse of(long balance, List<CurrencyTransaction> transactions) {
        List<TransactionEntry> entries = transactions.stream()
                .map(TransactionEntry::from)
                .toList();
        return new CurrencyResponse(balance, entries);
    }

    @Getter
    @AllArgsConstructor
    public static class TransactionEntry {
        private final Long id;
        private final long amount;
        private final String reason;
        private final LocalDateTime createdAt;

        public static TransactionEntry from(CurrencyTransaction transaction) {
            return new TransactionEntry(
                    transaction.getId(),
                    transaction.getAmount(),
                    transaction.getReason().name(),
                    transaction.getCreatedAt()
            );
        }
    }
}
