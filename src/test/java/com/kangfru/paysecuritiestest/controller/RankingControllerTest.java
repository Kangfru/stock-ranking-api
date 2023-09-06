package com.kangfru.paysecuritiestest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kangfru.paysecuritiestest.model.BaseResponse;
import com.kangfru.paysecuritiestest.model.RankingResponse;
import com.kangfru.paysecuritiestest.service.RankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * Controller 전체 테스트
 * 해당 Class는 통합 테스트로 시행한다.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
public class RankingControllerTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("엔드포인트 테스트 - 인기순 가져오기")
    void test_popularRanking_endPoint() throws Exception {
        ResponseEntity<RankingResponse> response = restTemplate.getForEntity("/api/v1/stock/ranking/popular?page=1", RankingResponse.class);
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isNotNull();
        then(response.getBody().getResultCode()).isEqualTo("0");
        System.out.printf("result stocks : %s %n", response.getBody().getStocks());
    }

    @Test
    @DisplayName("엔드포인트 테스트 - 가격 상승 순 가져오기")
    void test_highPrice_endPoint() throws Exception {
        ResponseEntity<RankingResponse> response = restTemplate.getForEntity("/api/v1/stock/ranking/high?page=1", RankingResponse.class);
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isNotNull();
        then(response.getBody().getResultCode()).isEqualTo("0");
        System.out.printf("result stocks : %s %n", response.getBody().getStocks());
    }

    @Test
    @DisplayName("엔드포인트 테스트 - 가격 하락 순 가져오기")
    void test_lowPrice_endPoint() throws Exception {
        ResponseEntity<RankingResponse> response = restTemplate.getForEntity("/api/v1/stock/ranking/low?page=1", RankingResponse.class);
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isNotNull();
        then(response.getBody().getResultCode()).isEqualTo("0");
        System.out.printf("result stocks : %s %n", response.getBody().getStocks());
    }

    @Test
    @DisplayName("엔드포인트 테스트 - 거래량 순 가져오기")
    void test_volume_endPoint() throws Exception {
        ResponseEntity<RankingResponse> response = restTemplate.getForEntity("/api/v1/stock/ranking/volume?page=1", RankingResponse.class);
        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(response.getBody()).isNotNull();
        then(response.getBody().getResultCode()).isEqualTo("0");
        System.out.printf("result stocks : %s %n", response.getBody().getStocks());
    }

    @Test
    @DisplayName("엔드포인트 테스트 - 필수 파라미터 누락 시 400 Error")
    void test_requiredParameterEmpty() throws Exception {
        ResponseEntity<BaseResponse> response = restTemplate.getForEntity("/api/v1/stock/ranking/volume", BaseResponse.class);
        then(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        then(response.getBody().getResultCode()).isEqualTo("400");
    }


}
