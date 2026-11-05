package com.websites.coffeeshop.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name="supplier")
public class Supplier {
  
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "supplier_seq")
  // starts above the ids that data.sql inserts explicitly
  @SequenceGenerator(name = "supplier_seq", sequenceName = "supplier_seq", initialValue = 1000, allocationSize = 50)
  @Column(name="id")
  private Long id;

  @Column(name="name")
  private String name;

  @Column(name="contact_person")
  private String contactPerson;

  @Column(name="contact_person_email")
  private String contactPersonEmail;

  @OneToMany(mappedBy="supplier")
  private List<Item> supplierItems;

  public Supplier() {
  }

  public Supplier(Long id, String name, String contactPerson, String contactPersonEmail, List<Item> supplierItems) {
    this.id = id;
    this.name = name;
    this.contactPerson = contactPerson;
    this.contactPersonEmail = contactPersonEmail;
    this.supplierItems = supplierItems;
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

  public String getContactPerson() {
    return contactPerson;
  }

  public void setContactPerson(String contactPerson) {
    this.contactPerson = contactPerson;
  }

  public String getContactPersonEmail() {
    return contactPersonEmail;
  }

  public void setContactPersonEmail(String contactPersonEmail) {
    this.contactPersonEmail = contactPersonEmail;
  }

  public List<Item> getSupplierItems() {
    return supplierItems;
  }

  public void setSupplierItems(List<Item> supplierItems) {
    this.supplierItems = supplierItems;
  }
}
