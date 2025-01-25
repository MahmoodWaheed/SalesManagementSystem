package com.mahmoud.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "item_balance", nullable = false)
    private Double itemBalance;

    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "purchasing_price", precision = 10, scale = 2)
    private BigDecimal purchasingPrice;

    @OneToMany(mappedBy = "item")
    private Set<Purchasedetail> purchasedetails = new LinkedHashSet<>();

    @OneToMany(mappedBy = "item")
    private Set<Transactiondetail> transactiondetails = new LinkedHashSet<>();

}