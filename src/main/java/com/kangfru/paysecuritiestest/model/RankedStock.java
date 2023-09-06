package com.kangfru.paysecuritiestest.model;

import com.kangfru.paysecuritiestest.repository.entity.Stock;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Getter
@Builder
@Jacksonized
@ToString
public class RankedStock {

    private BigDecimal currentPrice;

    private BigDecimal fluctuationRate;

    private Stock stock;

    public static RankedStock fromStock(BigDecimal currentPrice, BigDecimal fluctuationRate, Stock stock) {
        return new RankedStock(currentPrice, fluctuationRate, stock);
    }


}
