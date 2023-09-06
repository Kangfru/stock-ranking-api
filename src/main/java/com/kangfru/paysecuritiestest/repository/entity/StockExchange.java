package com.kangfru.paysecuritiestest.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

@Entity
@Table(name = "STOCK_EXCHANGE")
@SequenceGenerator(
        name = "STOCK_EXCHANGE_SEQ_GENERATOR",
        sequenceName = "STOCK_EXCHANGE_SEQ",
        allocationSize = 1
)
public class StockExchange {

    @Id
    @Column(name = "EXCHANGE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STOCK_EXCHANGE_SEQ_GENERATOR")
    private long exchangeId;

    @Column(name = "EXCHANGE_TIMESTAMP")
    private LocalDateTime exchangeTimestamp;

    private BigDecimal price;

    private int volume;

    private String exchangeType;

    @ManyToOne
    @JoinColumn(name = "STOCK_ID")
    private Stock stock;

}
