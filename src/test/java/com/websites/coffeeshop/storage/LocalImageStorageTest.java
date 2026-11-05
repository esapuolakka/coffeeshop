package com.websites.coffeeshop.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalImageStorageTest {

  @TempDir
  Path dir;

  private LocalImageStorage storage;

  @BeforeEach
  void setUp() {
    storage = new LocalImageStorage(dir.toString());
  }

  @Test
  void storedImageCanBeReadBack() {
    storage.put("images/a.png", new byte[] { 1, 2, 3 }, "image/png");

    assertThat(storage.get("images/a.png")).containsExactly(1, 2, 3);
  }

  @Test
  void getThrowsForMissingKey() {
    assertThatThrownBy(() -> storage.get("images/missing.png"))
        .isInstanceOf(ImageNotFoundException.class);
  }

  @Test
  void deleteRemovesImageAndToleratesMissingKey() {
    storage.put("images/a.png", new byte[] { 1 }, "image/png");

    storage.delete("images/a.png");
    storage.delete("images/a.png");

    assertThatThrownBy(() -> storage.get("images/a.png")).isInstanceOf(ImageNotFoundException.class);
  }

  @Test
  void keysCannotEscapeTheStorageDirectory() {
    assertThatThrownBy(() -> storage.put("../evil.png", new byte[] { 1 }, "image/png"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> storage.get("images/../../etc/passwd"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> storage.delete(""))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
