package com.kangfru.paysecuritiestest.configuration;

import com.kangfru.paysecuritiestest.model.RankingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cache Manager 단위 테스트
 */
public class CacheManagerTest {

    private CacheManager cacheManager;

    @BeforeEach
    public void setUp() {
        cacheManager = new CacheManager();
    }

    @Test
    @DisplayName("캐시 기본 동작 테스트 - Cache 내에 데이터가 없을 경우 - isPresent false")
    public void test_isPresent_notPresent() {
        assertFalse(cacheManager.isPresent("keyNotPresent"));
    }

    @Test
    @DisplayName("캐시 기본 동작 테스트 - Cache 내에 데이터가 있을 경우 - isPresent true")
    public void test_isPresent_present() {
        RankingResponse rankingResponse = RankingResponse.builder().build();
        cacheManager.put("test", rankingResponse).block();
        assertTrue(cacheManager.isPresent("test"));
    }

    @Test
    @DisplayName("캐시 기본 동작 테스트 - put 이후 get")
    public void test_put_and_get() {
        RankingResponse rankingResponse = RankingResponse.builder().build();
        Mono<RankingResponse> saved = cacheManager.put("test", rankingResponse);

        StepVerifier.create(saved)
                .expectNext(rankingResponse)
                .verifyComplete();

        assertTrue(cacheManager.isPresent("test"));

        Mono<RankingResponse> fetched = cacheManager.get("test");

        StepVerifier.create(fetched)
                .expectNext(rankingResponse)
                .verifyComplete();
    }

}
