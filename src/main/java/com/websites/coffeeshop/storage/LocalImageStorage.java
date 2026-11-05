package com.websites.coffeeshop.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Stores images as files in a directory. Default for local development and tests. */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalImageStorage implements ImageStorage {

  private final Path root;

  public LocalImageStorage(@Value("${app.storage.local.dir:uploads}") String dir) {
    this.root = Path.of(dir).toAbsolutePath().normalize();
  }

  @Override
  public void put(String key, byte[] content, String contentType) {
    Path target = resolve(key);
    try {
      Files.createDirectories(target.getParent());
      Files.write(target, content);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to store image " + key, e);
    }
  }

  @Override
  public byte[] get(String key) {
    try {
      return Files.readAllBytes(resolve(key));
    } catch (NoSuchFileException e) {
      throw new ImageNotFoundException(key, e);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read image " + key, e);
    }
  }

  @Override
  public void delete(String key) {
    try {
      Files.deleteIfExists(resolve(key));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to delete image " + key, e);
    }
  }

  /** Resolves a key below the root and refuses anything that escapes it. */
  private Path resolve(String key) {
    Path path = root.resolve(key).normalize();
    if (!path.startsWith(root) || path.equals(root)) {
      throw new IllegalArgumentException("Invalid image key: " + key);
    }
    return path;
  }
}
