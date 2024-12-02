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

    @Column(name = "purchaseDate", nullable = false)
    private Instant purchaseDate;

    @Column(name = "totalAmount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "Person_id", nullable = false)
    private Person person;

    @Lob
    @Column(name = "notes")
    private String notes;

    @Column(name = "fatora_number")
    private Integer fatoraNumber;

    @OneToMany(mappedBy = "purchaseTransaction")
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "purchaseTransaction")
    private Set<Purchasedetail> purchasedetails = new LinkedHashSet<>();

}