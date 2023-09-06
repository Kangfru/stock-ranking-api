package com.kangfru.paysecuritiestest.repository;

import com.kangfru.paysecuritiestest.repository.entity.StockView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockViewRepository extends JpaRepository<StockView, Long> {

    Page<StockView> findStockViewByOrderByCountDesc(Pageable pageable);

}
