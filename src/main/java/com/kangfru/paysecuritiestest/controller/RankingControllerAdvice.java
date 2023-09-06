package com.kangfru.paysecuritiestest.controller;

import com.kangfru.paysecuritiestest.model.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class RankingControllerAdvice {

    @ExceptionHandler(ServerWebInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<BaseResponse> handleMissingParameterException(ServerWebInputException e) {
        return Mono.just(BaseResponse.builder()
                .resultCode("400")
                .resultMessage(String.format("필수 파라미터가 누락되었습니다. [%s]", e.getMethodParameter().getParameterName()))
                .build());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<BaseResponse> handleUnknownException(Exception e) {
        return Mono.just(BaseResponse.builder()
                .resultCode("500")
                .resultMessage("알 수 없는 서버 에러")
                .build());
    }

}
