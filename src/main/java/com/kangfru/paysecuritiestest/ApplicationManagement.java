package com.kangfru.paysecuritiestest;

import com.kangfru.paysecuritiestest.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationManagement implements ApplicationRunner {

    private final RankingService rankingService;

    public void init() {
        log.info("=========== initialize start ==========");
        initData();
        log.info("=========== initialize end ==========");
    }

    private void initData() {
        rankingService.randomlyChangeData();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        init();
    }
}
