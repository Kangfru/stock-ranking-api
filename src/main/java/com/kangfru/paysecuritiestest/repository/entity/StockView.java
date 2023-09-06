package com.kangfru.paysecuritiestest.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

@Entity
@Table(name = "STOCK_VIEW")
@SequenceGenerator(
        name = "STOCK_VIEW_SEQ_GENERATOR",
        sequenceName = "STOCK_VIEW_SEQ",
        allocationSize = 1
)
public class StockView {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STOCK_VIEW_SEQ_GENERATOR")
    @Column(name = "STOCK_VIEW_ID")
    private long stockViewId;

    @OneToOne
    @JoinColumn(name = "STOCK_ID")
    private Stock stock;

    private int count;

}
