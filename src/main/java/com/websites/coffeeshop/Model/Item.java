package com.websites.coffeeshop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "item")
public class Item {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_item")
  // Starts above the ids that data.sql inserts explicitly. The name is not the old
  // Hibernate default (<table>_seq) on purpose: databases created by an older version
  // still have that sequence starting at 1, and reusing it produced negative ids.
  @SequenceGenerator(name = "seq_item", sequenceName = "seq_item", initialValue = 1000, allocationSize = 50)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "price")
  private BigDecimal price;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "image_id")
  private Image image;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /** Seed data sets created_at in SQL; items created in the app get it here. */
  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id", nullable = false)
  private Supplier supplier;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manufacturer_id", nullable = false)
  private Manufacturer manufacturer;

  public Item() {
  }

  public Item(Long id, String name, String description, BigDecimal price, Image image,
      LocalDateTime createdAt, Category category, Supplier supplier, Manufacturer manufacturer) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = price;
    this.image = image;
    this.createdAt = createdAt;
    this.category = category;
    this.supplier = supplier;
    this.manufacturer = manufacturer;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public Manufacturer getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(Manufacturer manufacturer) {
    this.manufacturer = manufacturer;
  }

  @Override
  public String toString() {
    return "Item{id=" + id + ", name='" + name + "'}";
  }
}
