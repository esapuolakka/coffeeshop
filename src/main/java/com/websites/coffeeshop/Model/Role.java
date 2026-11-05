package com.websites.coffeeshop.model;

import java.util.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="role")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
  // starts above the ids that data.sql inserts explicitly
  @SequenceGenerator(name = "role_seq", sequenceName = "role_seq", initialValue = 1000, allocationSize = 50)
  @Column(name="id")
  private Long id;

  @Column(name="name")
  private String name;
  
  @ManyToMany(mappedBy = "roles")
  private Set<User> users;

  public Role() {
  }

  public Role(Long id, String name, Set<User> users) {
    this.id = id;
    this.name = name;
    this.users = users;
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

  public Set<User> getUsers() {
    return users;
  }

  public void setUsers(Set<User> users) {
    this.users = users;
  }
}
