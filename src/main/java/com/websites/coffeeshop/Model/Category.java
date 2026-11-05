package com.websites.coffeeshop.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "category")
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
  // starts above the ids that data.sql inserts explicitly
  @SequenceGenerator(name = "category_seq", sequenceName = "category_seq", initialValue = 1000, allocationSize = 50)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "parent_category_id")
  private Long categoryId;

  @OneToMany(mappedBy = "category")
  private List<Item> categoryItems;

  public Category() {
  }

  public Category(Long id, String name, Long categoryId, List<Item> categoryItems) {
    this.id = id;
    this.name = name;
    this.categoryId = categoryId;
    this.categoryItems = categoryItems;
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

  public Long getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
  }

  public List<Item> getCategoryItems() {
    return categoryItems;
  }

  public void setCategoryItems(List<Item> categoryItems) {
    this.categoryItems = categoryItems;
  }

  @Override
  public String toString() {
    return "Category{id=" + id + ", name='" + name + "'}";
  }
}
