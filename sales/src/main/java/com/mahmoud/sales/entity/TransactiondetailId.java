package com.mahmoud.sales.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class TransactiondetailId implements java.io.Serializable {
    private static final long serialVersionUID = 3110659074258075905L;
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "Transaction_id", nullable = false)
    private Integer transactionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        TransactiondetailId entity = (TransactiondetailId) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.transactionId, entity.transactionId);
    }

    public TransactiondetailId() {}

    public TransactiondetailId(Integer id, Integer transactionId) {
        this.id = id;
        this.transactionId = transactionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionId);
    }

}