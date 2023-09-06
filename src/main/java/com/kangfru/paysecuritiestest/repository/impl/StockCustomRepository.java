package com.kangfru.paysecuritiestest.repository.impl;

import com.kangfru.paysecuritiestest.model.RankedStock;
import com.kangfru.paysecuritiestest.repository.entity.Stock;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface StockCustomRepository {

    Page<Stock> findStockByOrderByPopular(int page, int limit);

    Page<Stock> findStockByOrderByHighPrice(int page, int limit);

    Page<Stock> findStockByOrderByLowPrice(int page, int limit);

    Page<Stock> findStockByOrderByVolume(int page, int limit);

    RankedStock findLatestExchange(long stockId);

}
