package com.kangfru.paysecuritiestest.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

@Entity
@Table(name = "STOCK_PRICE_HISTORY")
@SequenceGenerator(
        name = "STOCK_PRICE_HISTORY_SEQ_GENERATOR",
        sequenceName = "STOCK_PRICE_HISTORY_SEQ",
        allocationSize = 1
)
public class StockPriceHistory {

    @Id
    @Column(name = "HISTORY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STOCK_PRICE_HISTORY_SEQ_GENERATOR")
    private long stockPriceHistoryId;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal high;

    private BigDecimal low;

    @Column(name = "PRICE_DATE")
    private LocalDate priceDate;

    @ManyToOne
    @JoinColumn(name = "STOCK_ID")
    private Stock stock;

}
