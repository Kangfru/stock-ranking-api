package com.kangfru.paysecuritiestest.repository;

import com.kangfru.paysecuritiestest.repository.entity.Stock;
import com.kangfru.paysecuritiestest.repository.impl.StockCustomRepository;
import com.kangfru.paysecuritiestest.repository.impl.StockRepositoryImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long>, StockCustomRepository {

}
