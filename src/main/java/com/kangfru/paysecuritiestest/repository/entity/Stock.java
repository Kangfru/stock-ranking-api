package com.kangfru.paysecuritiestest.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

@Entity
@Table(name = "STOCK")
@SequenceGenerator(
        name = "STOCK_SEQ_GENERATOR",
        sequenceName = "STOCK_SEQ",
        allocationSize = 1
)
public class Stock {

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STOCK_SEQ_GENERATOR")
    @Column(name = "STOCK_ID")
    @Id
    private long stockId;

    private String name;

    private String code;

}
