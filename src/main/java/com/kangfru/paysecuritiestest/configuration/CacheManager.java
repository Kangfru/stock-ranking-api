package com.kangfru.paysecuritiestest.configuration;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kangfru.paysecuritiestest.model.RankingResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Component
public class CacheManager {

    private final AsyncCache<String, RankingResponse> cache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(600L))
            .maximumSize(10_000L)
            .buildAsync();

    public boolean isPresent(String key) {
        return this.cache.asMap().containsKey(key);
    }

    public Mono<RankingResponse> get(String key) {
        return Mono.fromFuture(() -> this.cache.getIfPresent(key));
    }

    public Mono<RankingResponse> put(String key, RankingResponse rankingResponse) {
        CompletableFuture<RankingResponse> future = new CompletableFuture<>();
        future.complete(rankingResponse);
        this.cache.put(key, future);
        return Mono.fromFuture(() -> future);
    }
}
