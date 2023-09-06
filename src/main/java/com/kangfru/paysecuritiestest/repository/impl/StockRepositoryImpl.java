package com.kangfru.paysecuritiestest.repository.impl;

import com.kangfru.paysecuritiestest.model.RankedStock;
import com.kangfru.paysecuritiestest.repository.entity.Stock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class StockRepositoryImpl implements StockCustomRepository {

    private final EntityManager entityManager;

    @Override
    public Page<Stock> findStockByOrderByPopular(int page, int limit) {
        return null;
    }

    @Override
    public Page<Stock> findStockByOrderByHighPrice(int page, int limit) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        TypedQuery<Stock> query = entityManager.createQuery(
                "SELECT s " +
                        "FROM Stock s " +
                        "JOIN StockExchange se ON s.stockId = se.stock.stockId " +
                        "JOIN StockPriceHistory sph ON s.stockId = sph.stock.stockId " +
                        "WHERE se.exchangeTimestamp = (SELECT MAX(se2.exchangeTimestamp) FROM StockExchange se2 WHERE se2.stock = s) " +
                        "AND sph.priceDate = :yesterday " +
//                        "AND (se.price - sph.close) > 0 " +
                        "ORDER BY (se.price - sph.close) / sph.close DESC", Stock.class);
        query.setParameter("yesterday", yesterday);

        return getPagedStocks(page, limit, query);
    }

    @Override
    public Page<Stock> findStockByOrderByLowPrice(int page, int limit) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        TypedQuery<Stock> query = entityManager.createQuery(
                "SELECT s " +
                        "FROM Stock s " +
                        "JOIN StockExchange se ON s.stockId = se.stock.stockId " +
                        "JOIN StockPriceHistory sph ON s.stockId = sph.stock.stockId " +
                        "WHERE se.exchangeTimestamp = (SELECT MAX(se2.exchangeTimestamp) FROM StockExchange se2 WHERE se2.stock = s) " +
                        "AND sph.priceDate = :yesterday " +
//                        "AND (se.price - sph.close) < 0 " +
                        "ORDER BY (se.price - sph.close) / sph.close ASC", Stock.class);
        query.setParameter("yesterday", yesterday);

        return getPagedStocks(page, limit, query);
    }

    @Override
    public Page<Stock> findStockByOrderByVolume(int page, int limit) {
        TypedQuery<Stock> query = entityManager.createQuery(
                "SELECT s FROM Stock s JOIN StockExchange se ON s.stockId = se.stock.stockId " +
                        "GROUP BY s.stockId " +
                        "ORDER BY SUM(se.volume) DESC", Stock.class);

        return getPagedStocks(page, limit, query);
    }

    @Override
    public RankedStock findLatestExchange(long stockId) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Query query = entityManager.createQuery(
                "SELECT s, se.price, ((se.price - sph.close) / sph.close) FROM Stock s " +
                        "JOIN StockExchange se ON s.stockId = se.stock.stockId " +
                        "JOIN StockPriceHistory sph ON s.stockId = sph.stock.stockId " +
                        "WHERE sph.priceDate = :yesterday " +
                        "AND se.exchangeTimestamp = (SELECT MAX(se2.exchangeTimestamp) FROM StockExchange se2 WHERE se2.stock = s) " +
                        "AND s.stockId = :stockId ");
        query.setParameter("stockId", stockId);
        query.setParameter("yesterday", yesterday);

        Object[] result = (Object[]) query.getSingleResult();

        Stock stock = (Stock) result[0];
        BigDecimal price = (BigDecimal) result[1];
        BigDecimal fluctuationRate = (BigDecimal) result[2];
        fluctuationRate = fluctuationRate.setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

        return RankedStock.builder()
                .currentPrice(price)
                .fluctuationRate(fluctuationRate)
                .stock(stock)
                .build();

    }

    private Page<Stock> getPagedStocks(int page, int limit, TypedQuery<Stock> query) {
        query.setFirstResult((page) * limit);
        query.setMaxResults(limit);

        List<Stock> result = query.getResultList();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Stock> countRoot = countQuery.from(Stock.class);
        countQuery.select(cb.count(countRoot));
        Long totalResultCount = entityManager.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(result, PageRequest.of(page, limit), totalResultCount);
    }
}
