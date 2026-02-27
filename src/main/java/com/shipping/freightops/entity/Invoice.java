package com.shipping.freightops.entity;

import jakarta.persistence.*;

@Entity
public class Invoice {
    @Id
    private String id;
    @OneToOne 
    private FreightOrder order;

    public Invoice() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FreightOrder getOrder() {
        return order;
    }

    public void setOrder(FreightOrder order) {
        this.order = order;
    }

    public Invoice(FreightOrder order,String id) {
        this.id = id;
        this.order = order;
    }
}
