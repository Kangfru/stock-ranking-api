package com.kangfru.paysecuritiestest.service;

import com.kangfru.paysecuritiestest.code.CacheKey;
import com.kangfru.paysecuritiestest.configuration.CacheManager;
import com.kangfru.paysecuritiestest.model.BaseResponse;
import com.kangfru.paysecuritiestest.model.RankingResponse;
import com.kangfru.paysecuritiestest.repository.StockExchangeRepository;
import com.kangfru.paysecuritiestest.repository.StockPriceHistoryRepository;
import com.kangfru.paysecuritiestest.repository.StockRepository;
import com.kangfru.paysecuritiestest.repository.StockViewRepository;
import com.kangfru.paysecuritiestest.repository.entity.Stock;
import com.kangfru.paysecuritiestest.repository.entity.StockExchange;
import com.kangfru.paysecuritiestest.repository.entity.StockPriceHistory;
import com.kangfru.paysecuritiestest.repository.entity.StockView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final StockViewRepository stockViewRepository;

    private final StockExchangeRepository stockExchangeRepository;

    private final StockPriceHistoryRepository stockPriceHistoryRepository;

    private final StockRepository stockRepository;

    private final CacheManager cacheManager;

    public RankingResponse getRankingByPopular(int page) {
        Page<StockView> stockViews = stockViewRepository.findStockViewByOrderByCountDesc(PageRequest.of(page - 1, 20));
        return RankingResponse.builder()
                .pageNumber(page)
                .totalPages(stockViews.getTotalPages())
                .stocks(stockViews.getContent()
                        .stream()
                        .map((stock) -> stockRepository.findLatestExchange(stock.getStock().getStockId()))
                        .collect(Collectors.toList()))
                .resultCode("0")
                .resultMessage("Success")
                .build();
    }

    public Mono<RankingResponse> getRankingByPopularMono(int page) {
        if (!cacheManager.isPresent(CacheKey.POPULAR.getKey() + "_" + page)) {
            RankingResponse rankingResponse = getRankingByPopular(page);
            return cacheManager.put(CacheKey.POPULAR.getKey() + "_" + page, rankingResponse);
        }
        return cacheManager.get(CacheKey.POPULAR.getKey() + "_" + page);
    }

    public RankingResponse getRankingByHighPrice(int page) {
        Page<Stock> result = stockRepository.findStockByOrderByHighPrice(page - 1, 20);
        // 어제 종가 대비 가장 많이 상승한 종목
        return RankingResponse.builder()
                .stocks(result.getContent().stream()
                        .map((stock) -> stockRepository.findLatestExchange(stock.getStockId()))
                        .collect(Collectors.toList()))
                .pageNumber(page)
                .totalPages(result.getTotalPages())
                .resultCode("0")
                .resultMessage("Success")
                .build();
    }

    public Mono<RankingResponse> getRankingByHighPriceMono(int page) {
        if (!cacheManager.isPresent(CacheKey.HIGH.getKey() + "_" + page)) {
            RankingResponse rankingResponse = getRankingByHighPrice(page);
            return cacheManager.put(CacheKey.HIGH.getKey() + "_" + page, rankingResponse);
        }
        return cacheManager.get(CacheKey.HIGH.getKey() + "_" + page);
    }

    public RankingResponse getRankingByLowPrice(int page) {
        // 어제 종가 대비 가장 많이 하락한 종목
        Page<Stock> result = stockRepository.findStockByOrderByLowPrice(page - 1, 20);
        return RankingResponse.builder()
                .stocks(result.getContent().stream()
                        .map((stock) -> stockRepository.findLatestExchange(stock.getStockId()))
                        .collect(Collectors.toList()))
                .pageNumber(page)
                .totalPages(result.getTotalPages())
                .resultCode("0")
                .resultMessage("Success")
                .build();
    }

    public Mono<RankingResponse> getRankingByLowPriceMono(int page) {
        if (!cacheManager.isPresent(CacheKey.LOW.getKey() + "_" + page)) {
            RankingResponse rankingResponse = getRankingByLowPrice(page);
            return cacheManager.put(CacheKey.LOW.getKey() + "_" + page, rankingResponse);
        }
        return cacheManager.get(CacheKey.LOW.getKey() + "_" + page);
    }

    public RankingResponse getRankingByVolume(int page) {
        Page<Stock> result = stockRepository.findStockByOrderByVolume(page - 1, 20);
        return RankingResponse.builder()
                .stocks(result.getContent().stream()
                        .map((stock) -> stockRepository.findLatestExchange(stock.getStockId()))
                        .collect(Collectors.toList()))
                .pageNumber(page)
                .totalPages(result.getTotalPages())
                .resultCode("0")
                .resultMessage("Success")
                .build();
    }

    public Mono<RankingResponse> getRankingByVolumeMono(int page) {
        if (!cacheManager.isPresent(CacheKey.VOLUME.getKey() + "_" + page)) {
            RankingResponse rankingResponse = getRankingByVolume(page);
            return cacheManager.put(CacheKey.VOLUME.getKey() + "_" + page, rankingResponse);
        }
        return cacheManager.get(CacheKey.VOLUME.getKey() + "_" + page);
    }

    public Mono<BaseResponse> randomlyChangeData() {
        List<Stock> stocks = stockRepository.findAll();
        List<StockView> stockViews = new ArrayList<>();
        List<StockExchange> stockExchanges = new ArrayList<>();
        List<StockPriceHistory> stockPriceHistories = new ArrayList<>();
        for (Stock stock: stocks) {
            Random random = new Random();
            StockView stockView = StockView.builder()
                    .stock(stock)
                    .count(random.nextInt(10000) + 1)
                    .build();
            stockViews.add(stockView);
            for (int i = 0; i < random.nextInt(100) + 1; i++) {
                // 현재 날짜와 시간을 가져온다.
                LocalDateTime now = LocalDateTime.now();

                // 오늘 날짜의 오전 9시와 오후 4시를 설정한다.
                LocalDateTime today9AM = now.with(LocalTime.of(9, 0));
                LocalDateTime today4PM = now.with(LocalTime.of(16, 0));

                // 랜덤한 시간을 뽑는다.
                LocalDateTime randomTime = getRandomTimeBetween(today9AM, today4PM);

                StockExchange stockExchange = StockExchange.builder()
                        .stock(stock)
                        .price(new BigDecimal(random.nextInt(100, 100_000)))
                        .exchangeTimestamp(randomTime)
                        .volume(random.nextInt(1, 10_000))
                        .exchangeType(random.nextInt(2) > 0 ? "SELL" : "BUY")
                        .build();
                stockExchanges.add(stockExchange);
            }
            // 어제 시가 및 종가 설정
            LocalDate yesterday = LocalDate.now().minusDays(1);
            // 시가를 랜덤하게 설정
            BigDecimal open = new BigDecimal(random.nextInt(100, 100_000));
            BigDecimal rate = BigDecimal.valueOf(random.nextDouble(-0.2, 0.2));
            BigDecimal close = open.multiply(BigDecimal.ONE.add(rate)).setScale(0, RoundingMode.HALF_UP);
            StockPriceHistory stockPriceHistory = StockPriceHistory.builder()
                    .priceDate(yesterday)
                    .open(open)
                    .close(close)
                    .stock(stock)
                    .build();
            stockPriceHistories.add(stockPriceHistory);
        }
        stockViewRepository.deleteAll();
        stockViewRepository.saveAll(stockViews);
        stockExchangeRepository.deleteAll();
        stockExchangeRepository.saveAll(stockExchanges);
        stockPriceHistoryRepository.deleteAll();
        stockPriceHistoryRepository.saveAll(stockPriceHistories);
        BaseResponse baseResponse = BaseResponse.builder()
                .resultCode("0")
                .resultMessage("Success")
                .build();

        // 새로운 데이터로 캐시 체인지
        manageCache();
        return Mono.just(baseResponse);
    }

    public void manageCache() {
        managePopularCache();
        manageHighCache();
        manageLowCache();
        manageVolumeCache();
    }

    private void manageVolumeCache() {
        log.info("==== caching Volume ranking ====");
        RankingResponse rankingResponse = getRankingByVolume(1);
        cacheManager.put(CacheKey.VOLUME.getKey() + "_1", rankingResponse).subscribe();
        for (int i = 2; i <= rankingResponse.getTotalPages(); i++) {
            rankingResponse = getRankingByVolume(i);
            cacheManager.put(CacheKey.VOLUME.getKey() + "_" + i, rankingResponse).subscribe();
        }
    }

    private void manageLowCache() {
        log.info("==== caching Low ranking ====");
        RankingResponse rankingResponse = getRankingByLowPrice(1);
        cacheManager.put(CacheKey.LOW.getKey() + "_1", rankingResponse).subscribe();
        for (int i = 2; i <= rankingResponse.getTotalPages(); i++) {
            rankingResponse = getRankingByLowPrice(i);
            cacheManager.put(CacheKey.LOW.getKey() + "_" + i, rankingResponse).subscribe();
        }
    }

    private void manageHighCache() {
        log.info("==== caching High ranking ====");
        RankingResponse rankingResponse = getRankingByHighPrice(1);
        cacheManager.put(CacheKey.HIGH.getKey() + "_1", rankingResponse).subscribe();
        for (int i = 2; i <= rankingResponse.getTotalPages(); i++) {
            rankingResponse = getRankingByHighPrice(i);
            cacheManager.put(CacheKey.HIGH.getKey() + "_" + i, rankingResponse).subscribe();
        }
    }

    private void managePopularCache() {
        log.info("==== caching popular ranking ====");
        RankingResponse rankingResponse = getRankingByPopular(1);
        cacheManager.put(CacheKey.POPULAR.getKey() + "_1", rankingResponse).subscribe();
        for (int i = 2; i <= rankingResponse.getTotalPages(); i++) {
            rankingResponse = getRankingByPopular(i);
            cacheManager.put(CacheKey.POPULAR.getKey() + "_" + i, rankingResponse).subscribe();
        }
    }

    private LocalDateTime getRandomTimeBetween(LocalDateTime start, LocalDateTime end) {
        long secondsDifference = ChronoUnit.SECONDS.between(start, end);
        long randomSecondsToAdd = ThreadLocalRandom.current().nextLong(0, secondsDifference + 1);

        return start.plusSeconds(randomSecondsToAdd);
    }
}
