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
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sales_rep_id", nullable = false)
    private Employee salesRep;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "Person_id", nullable = false)
    private Person person;

    @Column(name = "note", length = 45)
    private String note;

    @OneToMany(mappedBy = "transaction")
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "transaction")
    private Set<Transactiondetail> transactiondetails = new LinkedHashSet<>();

    public void setAmount(BigDecimal amount) {
    }

    public void setTransactionType(String transactionType) {
    }
}