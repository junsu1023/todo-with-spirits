package com.oow.todowithspirit.domain.currency;

import com.oow.todowithspirit.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "currency_wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CurrencyWallet extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private long balance = 0;

    public CurrencyWallet(Long userId) {
        this.userId = userId;
    }

    public void credit(long amount) {
        this.balance += amount;
    }

    public void debit(long amount) {
        this.balance = Math.max(0, this.balance - amount);
    }
}
