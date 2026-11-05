package com.websites.coffeeshop.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.websites.coffeeshop.model.Image;
import com.websites.coffeeshop.model.Item;
import com.websites.coffeeshop.repository.ItemRepository;
import com.websites.coffeeshop.storage.ImageStorage;

/**
 * Gives the seeded products their pictures.
 *
 * <p>
 * The pictures ship with the application (src/main/resources/seed-images). A file
 * named {@code <item id>-<anything>.jpg} belongs to the item with that id. At
 * startup each picture is copied to the configured image storage (S3 when
 * deployed, a local directory otherwise) and linked to its item.
 *
 * <p>
 * Safe to run on every start: an item that already has an image is left alone,
 * so pictures changed in the admin area are never overwritten, and files for
 * items that do not exist are ignored.
 */
@Component
@ConditionalOnProperty(name = "app.seed-images.enabled", havingValue = "true", matchIfMissing = true)
public class SeedImageLoader implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(SeedImageLoader.class);

  private static final Pattern FILE_NAME = Pattern.compile("^(\\d+)(?:-.*)?\\.(jpg|jpeg|png)$",
      Pattern.CASE_INSENSITIVE);

  private final ItemRepository itemRepository;
  private final ImageStorage imageStorage;
  private final TransactionTemplate transactionTemplate;
  private final String location;

  public SeedImageLoader(ItemRepository itemRepository, ImageStorage imageStorage,
      TransactionTemplate transactionTemplate,
      @Value("${app.seed-images.location:classpath*:seed-images/*}") String location) {
    this.itemRepository = itemRepository;
    this.imageStorage = imageStorage;
    this.transactionTemplate = transactionTemplate;
    this.location = location;
  }

  @Override
  public void run(String... args) throws IOException {
    Resource[] resources = new PathMatchingResourcePatternResolver().getResources(location);
    // same order on every start, so the image ids are reproducible
    Arrays.sort(resources, Comparator.comparing(r -> String.valueOf(r.getFilename())));

    int attached = 0;
    int failed = 0;
    for (Resource resource : resources) {
      String filename = resource.getFilename();
      Optional<Long> itemId = itemIdOf(filename);
      if (itemId.isEmpty()) {
        continue; // README.md, CREDITS.md, ...
      }
      try {
        if (Boolean.TRUE.equals(transactionTemplate.execute(status -> attach(itemId.get(), filename, resource)))) {
          attached++;
        }
      } catch (RuntimeException e) {
        // a broken picture must never stop the application from starting
        failed++;
        log.warn("Could not attach seed image {}", filename, e);
      }
    }
    log.info("Seed images: {} attached, {} failed, {} files checked", attached, failed, resources.length);
  }

  /** Returns true when the image was attached, false when the item is missing or has an image. */
  private boolean attach(Long itemId, String filename, Resource resource) {
    Item item = itemRepository.findById(itemId).orElse(null);
    if (item == null || item.getImage() != null) {
      return false;
    }

    byte[] content;
    try {
      content = resource.getContentAsByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read " + filename, e);
    }

    String mediaType = filename.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
    String storageKey = "seed/" + filename;
    imageStorage.put(storageKey, content, mediaType);

    Image image = new Image();
    image.setName(filename);
    image.setMediaType(mediaType);
    image.setSize((long) content.length);
    image.setStorageKey(storageKey);
    item.setImage(image);
    itemRepository.save(item);
    return true;
  }

  /** "06-moka-pot.jpg" and "6.png" belong to item 6; other names belong to no item. */
  static Optional<Long> itemIdOf(String filename) {
    if (filename == null) {
      return Optional.empty();
    }
    Matcher matcher = FILE_NAME.matcher(filename);
    return matcher.matches() ? Optional.of(Long.parseLong(matcher.group(1))) : Optional.empty();
  }
}
