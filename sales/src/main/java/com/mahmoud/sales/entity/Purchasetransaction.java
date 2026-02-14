package com.mahmoud.sales.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "purchasetransaction")
public class Purchasetransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "purchase_date", nullable = false)
    private Instant purchaseDate;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "withdrawal_fee", precision = 10, scale = 2)
    private BigDecimal withdrawalFee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "Person_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sales_rep_id")
    private Employee salesRep;

    @Lob
    @Column(name = "notes")
    private String notes;

    @Column(name = "fatora_number")
    private Integer fatoraNumber;

    // NEW: Transaction type for purchase (INVOICE or RETURN)
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 45)
    private PurchaseTransactionType transactionType;

    @OneToMany(mappedBy = "purchaseTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "purchaseTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Purchasedetail> purchasedetails = new LinkedHashSet<>();

}
