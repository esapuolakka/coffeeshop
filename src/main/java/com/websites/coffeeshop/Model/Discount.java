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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_seq")
  // starts above the ids that data.sql inserts explicitly
  @SequenceGenerator(name = "discount_seq", sequenceName = "discount_seq", initialValue = 1000, allocationSize = 50)
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

