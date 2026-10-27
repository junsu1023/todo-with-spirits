package com.oow.todowithspirit.domain.currency;

import com.oow.todowithspirit.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "currency_transactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"task_id", "task_completion_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurrencyTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CurrencyReason reason;

    // TASK_COMPLETE 지급의 멱등성 키 (taskId, taskCompletionDate). 다른 reason은 둘 다 null
    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_completion_date")
    private LocalDate taskCompletionDate;

    public static CurrencyTransaction ofTaskComplete(Long userId, long amount, Long taskId, LocalDate completionDate) {
        CurrencyTransaction transaction = new CurrencyTransaction();
        transaction.userId = userId;
        transaction.amount = amount;
        transaction.reason = CurrencyReason.TASK_COMPLETE;
        transaction.taskId = taskId;
        transaction.taskCompletionDate = completionDate;
        return transaction;
    }
}
