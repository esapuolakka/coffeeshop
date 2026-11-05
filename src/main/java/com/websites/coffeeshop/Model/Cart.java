package com.websites.coffeeshop.model;

import java.io.Serializable;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {
  private static final long serialVersionUID = 1L;

  private List<CartItem> items = new ArrayList<>();

  public void addItem(Long productId, String productName, BigDecimal price, double discountedPrice, int quantity) {
    for (CartItem item : items) {
      if (item.getProductId().equals(productId)) {
        item.setQuantity(item.getQuantity() + quantity);
        return;
      }
    }
    items.add(new CartItem(productId, productName, price, discountedPrice, quantity));
  }

  public void removeItem(Long productId) {
    items.removeIf(item -> item.getProductId().equals(productId));
  }

  public void clear() {
    items.clear();
  }

  public List<CartItem> getItems() {
    return items;
  }

  public BigDecimal getTotalPrice(BigDecimal discount, boolean isVIP) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    for (CartItem item : items) {
      BigDecimal itemPrice = item.getPrice();
      if (isVIP) {
        itemPrice = itemPrice.subtract(itemPrice.multiply(discount).divide(BigDecimal.valueOf(100)));
        itemPrice = itemPrice.setScale(2, RoundingMode.HALF_UP);
      }
      totalPrice = totalPrice.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
    }
    return totalPrice;
  }
}
