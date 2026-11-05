package com.websites.coffeeshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "discount")
public class Discount {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_discount")
  // Starts above the ids that data.sql inserts explicitly. The name is not the old
  // Hibernate default (<table>_seq) on purpose: databases created by an older version
  // still have that sequence starting at 1, and reusing it produced negative ids.
  @SequenceGenerator(name = "seq_discount", sequenceName = "seq_discount", initialValue = 1000, allocationSize = 50)
  @Column(name = "id")
  private Long id;

  @Column(name = "discount_percentage")
  private double discount;

  public Discount() {
  }

  public Discount(double discount) {
    this.discount = discount;
  }

  public double getDiscount() {
    return discount;
  }

  public void setDiscount(double discount) {
    this.discount = discount;
  }
}

