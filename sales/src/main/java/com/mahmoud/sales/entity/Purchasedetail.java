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
@Table(name = "purchasedetail")
public class Purchasedetail {
    @EmbeddedId
    private PurchasedetailId id;

    @MapsId("purchasetransactionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "purchase_transaction_id", nullable = false)
    private Purchasetransaction purchaseTransaction;

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @Column(name = "purchasing_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasingPrice;

    @Column(name = "comulative_price", precision = 10, scale = 2)
    private BigDecimal comulativePrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

}