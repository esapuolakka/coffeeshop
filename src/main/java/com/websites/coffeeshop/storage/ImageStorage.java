package com.websites.coffeeshop.storage;

/**
 * Where the bytes of uploaded product images live. The database only keeps
 * metadata and the storage key; implementations: local directory (dev, tests)
 * and Amazon S3 (deployment).
 */
public interface ImageStorage {

  void put(String key, byte[] content, String contentType);

  /** @throws ImageNotFoundException if nothing is stored under the key */
  byte[] get(String key);

  /** Removes the object; a missing key is not an error. */
  void delete(String key);
}
