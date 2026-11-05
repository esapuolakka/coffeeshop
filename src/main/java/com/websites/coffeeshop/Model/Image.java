package com.websites.coffeeshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "image")
public class Image {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id")
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "media_type")
  private String mediaType;

  @Column(name = "size")
  private Long size;

  /** Key of the image bytes in the ImageStorage (local directory or S3 object key). */
  @Column(name = "storage_key")
  private String storageKey;

  public Image() {
  }

  public Image(Long id, String name, String mediaType, Long size, String storageKey) {
    this.id = id;
    this.name = name;
    this.mediaType = mediaType;
    this.size = size;
    this.storageKey = storageKey;
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

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public String getStorageKey() {
    return storageKey;
  }

  public void setStorageKey(String storageKey) {
    this.storageKey = storageKey;
  }
}
