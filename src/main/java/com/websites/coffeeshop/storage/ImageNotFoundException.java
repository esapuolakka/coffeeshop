package com.websites.coffeeshop.storage;

public class ImageNotFoundException extends RuntimeException {

  public ImageNotFoundException(String key) {
    super("Image not found in storage: " + key);
  }

  public ImageNotFoundException(String key, Throwable cause) {
    super("Image not found in storage: " + key, cause);
  }
}
