package com.websites.coffeeshop.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.websites.coffeeshop.model.Category;
import com.websites.coffeeshop.model.Item;
import com.websites.coffeeshop.model.Manufacturer;
import com.websites.coffeeshop.model.Supplier;
import com.websites.coffeeshop.repository.CategoryRepository;
import com.websites.coffeeshop.repository.ItemRepository;
import com.websites.coffeeshop.repository.ManufacturerRepository;
import com.websites.coffeeshop.repository.SupplierRepository;

/**
 * A database that was created by an older version of the app already contains
 * sequences with the names Hibernate used then (starting at 1). The id generators
 * must not reuse them: with a generator that expects a sequence starting at 1000,
 * Hibernate counted back from the old sequence and handed out negative ids (-47).
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:legacyvolume;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
    // run the legacy script before Hibernate touches the schema, and let Hibernate only
    // update it (as on a real database); create-drop would silently remove the old sequences
    "spring.jpa.defer-datasource-initialization=false",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:legacy-sequences.sql",
    "spring.sql.init.data-locations=classpath:legacy-no-data.sql",
    "app.seed-images.enabled=false" })
class LegacyVolumeTest {

  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ManufacturerRepository manufacturerRepository;
  @Autowired
  private SupplierRepository supplierRepository;

  @Test
  void idsAreNotNegativeWhenOldSequencesAreLyingAround() {
    Item item = itemRepository.save(newItem("Beans"));

    assertThat(item.getId()).as("item id").isGreaterThanOrEqualTo(1000);
    assertThat(item.getCategory().getId()).as("category id").isGreaterThanOrEqualTo(1000);
    assertThat(item.getManufacturer().getId()).as("manufacturer id").isGreaterThanOrEqualTo(1000);
    assertThat(item.getSupplier().getId()).as("supplier id").isGreaterThanOrEqualTo(1000);
  }

  private Item newItem(String name) {
    Manufacturer manufacturer = new Manufacturer();
    manufacturer.setName("BrewCo " + name);
    manufacturer.setUrl("https://brewco.example.com");

    Supplier supplier = new Supplier();
    supplier.setName("Supply Co " + name);
    supplier.setContactPerson("Alice");
    supplier.setContactPersonEmail("alice@example.com");

    Category category = new Category();
    category.setName("coffee " + name);

    Item item = new Item();
    item.setName(name);
    item.setDescription("Fresh");
    item.setPrice(BigDecimal.TEN);
    item.setManufacturer(manufacturerRepository.save(manufacturer));
    item.setSupplier(supplierRepository.save(supplier));
    item.setCategory(categoryRepository.save(category));
    return item;
  }

  @Test
  void aNewItemKnowsWhenItWasCreated() {
    Item saved = itemRepository.save(newItem("Timestamped"));

    assertThat(saved.getCreatedAt()).as("created_at").isNotNull();
  }
}
