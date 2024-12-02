package com.mahmoud.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "transactiondetail")
public class Transactiondetail {
    @EmbeddedId
    private TransactiondetailId id;

    @MapsId("transactionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "Transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @Column(name = "sellingPrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "comulativePrice", nullable = false, precision = 10, scale = 2)
    private BigDecimal comulativePrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

}