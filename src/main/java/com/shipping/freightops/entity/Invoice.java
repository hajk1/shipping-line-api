package com.shipping.freightops.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Invoice {
  @Id private String id;
  @OneToOne private FreightOrder order;

  public Invoice(FreightOrder order, String id) {
    this.id = id;
    this.order = order;
  }
}
