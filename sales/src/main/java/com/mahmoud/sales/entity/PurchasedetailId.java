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
public class PurchasedetailId implements java.io.Serializable {
    private static final long serialVersionUID = 973050222285819923L;
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "PurchaseTransaction_id", nullable = false)
    private Integer purchasetransactionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PurchasedetailId entity = (PurchasedetailId) o;
        return Objects.equals(this.purchasetransactionId, entity.purchasetransactionId) &&
                Objects.equals(this.id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purchasetransactionId, id);
    }

}