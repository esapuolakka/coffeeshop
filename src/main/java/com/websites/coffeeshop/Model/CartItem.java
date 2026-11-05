package com.websites.coffeeshop.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long productId;
  private String name;
  private BigDecimal price;
  private double discountedPrice;
  private int quantity;

  public CartItem(Long productId, String name, BigDecimal price, double discountedPrice, int quantity) {
    this.productId = productId;
    this.name = name;
    this.price = price;
    this.discountedPrice = discountedPrice;
    this.quantity = quantity;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public double getDiscountedPrice() {
    return discountedPrice;
  }

  public void setDiscountedPrice(double discountedPrice) {
    this.discountedPrice = discountedPrice;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
