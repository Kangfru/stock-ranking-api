package com.kangfru.paysecuritiestest.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheKey {

    POPULAR("POPULAR", "인기순 저장 캐시"),
    HIGH("HIGH", "전 일 기준 고가 저장 캐시"),
    LOW("LOW", "전일기준 하락 저장 캐시"),
    VOLUME("VOLUME", "거래량 기준 저장 캐시");

    private final String key;

    private final String description;

}
