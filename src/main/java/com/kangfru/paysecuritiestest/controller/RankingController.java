package com.kangfru.paysecuritiestest.controller;

import com.kangfru.paysecuritiestest.model.BaseResponse;
import com.kangfru.paysecuritiestest.model.RankingResponse;
import com.kangfru.paysecuritiestest.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(("/api/v1"))
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/stock/ranking/popular")
    public Mono<RankingResponse> getRankingByPopular(@RequestParam int page) {
        return rankingService.getRankingByPopularMono(page);
    }

    @GetMapping("/stock/ranking/high")
    public Mono<RankingResponse> getRankingByHighPrice(@RequestParam int page) {
        return rankingService.getRankingByHighPriceMono(page);
    }

    @GetMapping("/stock/ranking/low")
    public Mono<RankingResponse> getRankingByLowPrice(@RequestParam int page) {
        return rankingService.getRankingByLowPriceMono(page);
    }

    @GetMapping("/stock/ranking/volume")
    public Mono<RankingResponse> getRankingByVolume(@RequestParam int page) {
        return rankingService.getRankingByVolumeMono(page);
    }

    @GetMapping("/stock/random")
    public Mono<BaseResponse> randomlyChangeData() {
        return rankingService.randomlyChangeData();
    }

}
