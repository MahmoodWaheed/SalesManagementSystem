package com.mahmoud.sales.entity;

import jakarta.persistence.*;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ObservableValue;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "location", length = 50)
    private String location;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @ColumnDefault("0.00")
    @Column(name = "open_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal openBalance;

    @OneToMany(mappedBy = "person")
    private Set<Payment> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "person")
    private Set<Phone> phones = new LinkedHashSet<>();

    @OneToMany(mappedBy = "person")
    private Set<Purchasetransaction> purchasetransactions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "person")
    private Set<Transaction> transactions = new LinkedHashSet<>();

}