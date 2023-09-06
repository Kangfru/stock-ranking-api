package com.kangfru.paysecuritiestest.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@SuperBuilder
@Jacksonized
public class RankingResponse extends BaseResponse {

    private List<RankedStock> stocks;

    private int pageNumber;

    private int totalPages;

}
