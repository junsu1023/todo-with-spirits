package com.oow.todowithspirit.domain.currency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyWalletRepository extends JpaRepository<CurrencyWallet, Long> {
}
