package com.kangfru.paysecuritiestest.repository;

import com.kangfru.paysecuritiestest.repository.entity.StockExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockExchangeRepository extends JpaRepository<StockExchange, Long> {
}
