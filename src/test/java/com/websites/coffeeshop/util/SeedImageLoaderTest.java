package com.websites.coffeeshop.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.websites.coffeeshop.model.Image;
import com.websites.coffeeshop.model.Item;
import com.websites.coffeeshop.repository.ImageRepository;
import com.websites.coffeeshop.repository.ItemRepository;
import com.websites.coffeeshop.storage.ImageStorage;

/**
 * Starts the application with the real seed data and the real pictures from
 * src/main/resources/seed-images, as it happens in a deployment.
 */
@SpringBootTest(properties = {
    "spring.sql.init.mode=always",
    // own in-memory database so seeding does not leak into other tests
    "spring.datasource.url=jdbc:h2:mem:seedimg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1" })
@AutoConfigureMockMvc
class SeedImageLoaderTest {

  /** data.sql creates the items 1..80. */
  private static final long LAST_SEEDED_ITEM = 80;

  @Autowired
  private SeedImageLoader loader;
  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private ImageRepository imageRepository;
  @Autowired
  private ImageStorage imageStorage;
  @Autowired
  private MockMvc mockMvc;

  // ---- file name -> item id ----

  @ParameterizedTest
  @ValueSource(strings = { "06-moka-pot.jpg", "6.png", "80-hazelnut-coffee.JPG", "12-a-b-c.jpeg" })
  void fileNamesStartingWithAnItemIdBelongToThatItem(String filename) {
    assertThat(SeedImageLoader.itemIdOf(filename)).isPresent();
  }

  @Test
  void itemIdIsTheLeadingNumber() {
    assertThat(SeedImageLoader.itemIdOf("06-moka-pot.jpg")).contains(6L);
    assertThat(SeedImageLoader.itemIdOf("6.png")).contains(6L);
    assertThat(SeedImageLoader.itemIdOf("123-x.jpeg")).contains(123L);
  }

  @ParameterizedTest
  @ValueSource(strings = { "README.md", "CREDITS.md", "moka-pot.jpg", "06-moka-pot.gif", "06-moka-pot", "x06-pot.jpg",
      "-06.jpg" })
  void otherFilesBelongToNoItem(String filename) {
    assertThat(SeedImageLoader.itemIdOf(filename)).isEmpty();
  }

  @Test
  void nullFileNameBelongsToNoItem() {
    assertThat(SeedImageLoader.itemIdOf(null)).isEqualTo(Optional.empty());
  }

  // ---- startup behaviour ----

  @Test
  void everyPictureOfASeededItemIsAttachedAndStored() throws IOException {
    List<Resource> pictures = seedPictures();
    assertThat(pictures).as("seed pictures on the classpath").isNotEmpty();

    for (Resource picture : pictures) {
      long itemId = SeedImageLoader.itemIdOf(picture.getFilename()).orElseThrow();
      Image image = imageOfItem(itemId);

      assertThat(image).as("image of item %d (%s)", itemId, picture.getFilename()).isNotNull();
      assertThat(image.getStorageKey()).isEqualTo("seed/" + picture.getFilename());
      assertThat(image.getMediaType()).isEqualTo("image/jpeg");
      assertThat(image.getSize()).isEqualTo(picture.contentLength());
      assertThat(imageStorage.get(image.getStorageKey())).isEqualTo(picture.getContentAsByteArray());
    }
  }

  @Test
  void onlyItemsWithAPictureFileGetAnImage() throws IOException {
    // an image belongs to exactly one item, so the counts must match
    assertThat(imageRepository.count()).isEqualTo(seedPictures().size());
    assertThat(itemRepository.count()).isGreaterThanOrEqualTo(seedPictures().size());
  }

  @Test
  void seededPictureIsServedByTheShop() throws Exception {
    Resource first = seedPictures().get(0);
    long itemId = SeedImageLoader.itemIdOf(first.getFilename()).orElseThrow();
    Long imageId = imageOfItem(itemId).getId();

    mockMvc.perform(get("/products/anything/{id}/image", imageId))
        .andExpect(status().isOk())
        .andExpect(content().contentType("image/jpeg"))
        .andExpect(content().bytes(first.getContentAsByteArray()));
  }

  @Test
  void runningAgainChangesNothing() throws Exception {
    long images = imageRepository.count();
    Long firstImageId = imageRepository.findAll().get(0).getId();

    loader.run();

    assertThat(imageRepository.count()).isEqualTo(images);
    assertThat(imageRepository.existsById(firstImageId)).isTrue();
  }

  @Test
  @Transactional
  void aPictureChosenInTheAdminAreaIsNeverOverwritten() throws Exception {
    long itemId = SeedImageLoader.itemIdOf(seedPictures().get(0).getFilename()).orElseThrow();
    Item item = itemRepository.findById(itemId).orElseThrow();
    Image custom = new Image();
    custom.setName("custom.png");
    custom.setMediaType("image/png");
    custom.setSize(1L);
    custom.setStorageKey("images/custom.png");
    item.setImage(custom);
    itemRepository.saveAndFlush(item);

    loader.run();

    assertThat(itemRepository.findById(itemId).orElseThrow().getImage().getStorageKey())
        .isEqualTo("images/custom.png");
  }

  // ---- helpers ----

  /** Seed pictures that belong to one of the seeded items, sorted by file name. */
  private List<Resource> seedPictures() throws IOException {
    return Arrays.stream(new PathMatchingResourcePatternResolver().getResources("classpath*:seed-images/*"))
        .filter(r -> SeedImageLoader.itemIdOf(r.getFilename())
            .filter(id -> id >= 1 && id <= LAST_SEEDED_ITEM).isPresent())
        .sorted((a, b) -> a.getFilename().compareTo(b.getFilename()))
        .toList();
  }

  private Image imageOfItem(long itemId) {
    String prefix = String.format("%02d-", itemId);
    return imageRepository.findAll().stream()
        .filter(i -> i.getName().startsWith(prefix) || i.getName().startsWith(itemId + "."))
        .findFirst().orElse(null);
  }
}
