package com.kangfru.paysecuritiestest.repository;

import com.kangfru.paysecuritiestest.repository.entity.StockPriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {

}
