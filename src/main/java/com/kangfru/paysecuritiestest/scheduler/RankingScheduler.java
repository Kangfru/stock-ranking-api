package com.kangfru.paysecuritiestest.scheduler;

import com.kangfru.paysecuritiestest.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingService rankingService;

    @Scheduled(cron = "0 * * * * *")
    public void rankingScheduler() {
        rankingService.manageCache();
    }

}
