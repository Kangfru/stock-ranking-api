package com.kangfru.paysecuritiestest.service;

import com.kangfru.paysecuritiestest.code.CacheKey;
import com.kangfru.paysecuritiestest.configuration.CacheManager;
import com.kangfru.paysecuritiestest.model.RankedStock;
import com.kangfru.paysecuritiestest.model.RankingResponse;
import com.kangfru.paysecuritiestest.repository.StockExchangeRepository;
import com.kangfru.paysecuritiestest.repository.StockPriceHistoryRepository;
import com.kangfru.paysecuritiestest.repository.StockRepository;
import com.kangfru.paysecuritiestest.repository.StockViewRepository;
import com.kangfru.paysecuritiestest.repository.entity.Stock;
import com.kangfru.paysecuritiestest.repository.entity.StockView;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ranking Service 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RankingServiceTest {


    @Mock
    private StockViewRepository stockViewRepository;

    @Mock
    private StockExchangeRepository stockExchangeRepository;

    @Mock
    private StockPriceHistoryRepository stockPriceHistoryRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private RankingService rankingService;

    @Test
    @DisplayName("인기 순 가져오기 - 캐쉬된 데이터가 있을 때 캐시 데이터를 가져오는 지 확인")
    @Order(1)
    public void test_getRankingByPopularMono_withCachedMono() {
        // given
        when(cacheManager.isPresent(any())).thenReturn(true);
        RankedStock rankedStock = RankedStock.builder().build();
        RankingResponse rankingResponseDummy = RankingResponse.builder()
                .stocks(Collections.singletonList(rankedStock))
                .build();
        Mono<RankingResponse> rankingResponseMono = Mono.just(rankingResponseDummy);
        when(cacheManager.get(CacheKey.POPULAR.getKey() + "_1")).thenReturn(rankingResponseMono);

        // act & assert
        StepVerifier.create(rankingService.getRankingByPopularMono(1))
                .expectNextMatches(rankingResponse -> !rankingResponse.getStocks().isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("인기 순 가져오기 - 캐쉬에 데이터가 없을 때 새롭게 캐시 등록을 수행하는 지 확인")
    @Order(2)
    public void test_getRankingByPopularMono_withNotCachedMono() {
        // given
        when(cacheManager.isPresent(any())).thenReturn(false);

        StockView stockView = StockView.builder()
                .stock(Stock.builder().build())
                .build();
        List<StockView> stockViews = Collections.singletonList(stockView);
        Pageable pageable = PageRequest.of(0, 20);
        Page<StockView> pagedResponse = new PageImpl<>(stockViews, pageable, stockViews.size());

        when(stockViewRepository.findStockViewByOrderByCountDesc(any())).thenReturn(pagedResponse);

        // when
        rankingService.getRankingByPopularMono(1);

        // then
        verify(cacheManager).put(eq(CacheKey.POPULAR.getKey() + "_1"), any(RankingResponse.class));
    }

    @Test
    @DisplayName("상승 순 가져오기 - 캐쉬된 데이터가 있을 때 캐시 데이터를 가져오는 지 확인")
    @Order(3)
    public void test_getRankingByHighMono_withCachedMono() {
        // given
        when(cacheManager.isPresent(any())).thenReturn(true);
        RankedStock rankedStock = RankedStock.builder().build();
        RankingResponse rankingResponseDummy = RankingResponse.builder()
                .stocks(Collections.singletonList(rankedStock))
                .build();
        Mono<RankingResponse> rankingResponseMono = Mono.just(rankingResponseDummy);
        when(cacheManager.get(CacheKey.HIGH.getKey() + "_1")).thenReturn(rankingResponseMono);

        // act & assert
        StepVerifier.create(rankingService.getRankingByHighPriceMono(1))
                .expectNextMatches(rankingResponse -> !rankingResponse.getStocks().isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("상승 순 가져오기 - 캐쉬에 데이터가 없을 때 새롭게 캐시 등록을 수행하는 지 확인")
    @Order(4)
    public void test_getRankingByHighMono_withNotCachedMono() {
        // given
        when(cacheManager.isPresent(any())).thenReturn(false);

        Stock stock = Stock.builder()
                .stockId(1L)
                .build();
        List<Stock> stocks = Collections.singletonList(stock);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Stock> pagedResponse = new PageImpl<>(stocks, pageable, stocks.size());

        when(stockRepository.findStockByOrderByHighPrice(anyInt(), anyInt())).thenReturn(pagedResponse);
        RankedStock rankedStock = RankedStock.fromStock(BigDecimal.ONE, BigDecimal.ONE, stock);
        when(stockRepository.findLatestExchange(stock.getStockId())).thenReturn(rankedStock);

        // when
        rankingService.getRankingByHighPriceMono(1);

        // then
        verify(cacheManager).put(eq(CacheKey.HIGH.getKey() + "_1"), any(RankingResponse.class));
    }
    @Test
    @DisplayName("하락 순 가져오기 - 캐쉬된 데이터가 있을 때 캐시 데이터를 가져오는 지 확인")
    @Order(5)
    public void test_getRankingByLowMono_withCachedMono() {
        // given
        when(cacheManager.isPresent(any())).thenReturn(true);
        RankedStock rankedStock = RankedStock.builder().build();
        RankingResponse rankingResponseDummy = RankingResponse.builder()
                .stocks(Collections.singletonList(rankedStock))
                .build();
        Mono<RankingResponse> rankingResponseMono = Mono.just(rankingResponseDummy);
        when(cacheManager.get(CacheKey.LOW.getKey() + "_1")).thenReturn(rankingResponseMono);

        // act & assert
        StepVerifier.create(rankingService.getRankingByLowPriceMono(1))
                .expectNextMatches(rankingResponse -> !rankingResponse.getStocks().isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("하락 순 가져오기 - 캐쉬에 데이터가 없을 때 새롭게 캐시 등록을 수행하는 지 확인")
    @Order(6)
    public void test_getRankingByLowMono_withNotCachedMono() {
        // given
        when(cacheManager.isPresent(any())).thenReturn(false);

        Stock stock = Stock.builder()
                .stockId(1L)
                .build();
        List<Stock> stocks = Collections.singletonList(stock);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Stock> pagedResponse = new PageImpl<>(stocks, pageable, stocks.size());

        when(stockRepository.findStockByOrderByLowPrice(anyInt(), anyInt())).thenReturn(pagedResponse);
        RankedStock rankedStock = RankedStock.fromStock(BigDecimal.ONE, BigDecimal.ONE, stock);
        when(stockRepository.findLatestExchange(stock.getStockId())).thenReturn(rankedStock);

        // when
        rankingService.getRankingByLowPriceMono(1);

        // then
        verify(cacheManager).put(eq(CacheKey.LOW.getKey() + "_1"), any(RankingResponse.class));
    }

    @Test
    @DisplayName("거래량 순 가져오기 - 캐쉬된 데이터가 있을 때 캐시 데이터를 가져오는 지 확인")
    @Order(7)
    public void test_getRankingByVolumeMono_withCachedMono() {
        // given
        when(cacheManager.isPresent(any())).thenReturn(true);
        RankedStock rankedStock = RankedStock.builder().build();
        RankingResponse rankingResponseDummy = RankingResponse.builder()
                .stocks(Collections.singletonList(rankedStock))
                .build();
        Mono<RankingResponse> rankingResponseMono = Mono.just(rankingResponseDummy);
        when(cacheManager.get(CacheKey.VOLUME.getKey() + "_1")).thenReturn(rankingResponseMono);

        // act & assert
        StepVerifier.create(rankingService.getRankingByVolumeMono(1))
                .expectNextMatches(rankingResponse -> !rankingResponse.getStocks().isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("거래량 순 가져오기 - 캐쉬에 데이터가 없을 때 새롭게 캐시 등록을 수행하는 지 확인")
    @Order(8)
    public void test_getRankingByVolumeMono_withNotCachedMono() {
        // given
        when(cacheManager.isPresent(any())).thenReturn(false);

        Stock stock = Stock.builder()
                .stockId(1L)
                .build();
        List<Stock> stocks = Collections.singletonList(stock);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Stock> pagedResponse = new PageImpl<>(stocks, pageable, stocks.size());

        when(stockRepository.findStockByOrderByVolume(anyInt(), anyInt())).thenReturn(pagedResponse);
        RankedStock rankedStock = RankedStock.fromStock(BigDecimal.ONE, BigDecimal.ONE, stock);
        when(stockRepository.findLatestExchange(stock.getStockId())).thenReturn(rankedStock);

        // when
        rankingService.getRankingByVolumeMono(1);

        // then
        verify(cacheManager).put(eq(CacheKey.VOLUME.getKey() + "_1"), any(RankingResponse.class));
    }

}
