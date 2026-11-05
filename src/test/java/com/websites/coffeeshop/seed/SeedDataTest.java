package com.websites.coffeeshop.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.websites.coffeeshop.model.Category;
import com.websites.coffeeshop.model.Item;
import com.websites.coffeeshop.model.Manufacturer;
import com.websites.coffeeshop.model.Supplier;
import com.websites.coffeeshop.repository.CategoryRepository;
import com.websites.coffeeshop.repository.ItemRepository;
import com.websites.coffeeshop.repository.ManufacturerRepository;
import com.websites.coffeeshop.repository.SupplierRepository;

import javax.sql.DataSource;

/**
 * The seed data is loaded on every start of the application (also in production,
 * against a persistent database). It must tolerate being loaded again, and rows
 * the application creates afterwards must not collide with the seeded ids.
 */
@SpringBootTest(properties = {
    "spring.sql.init.mode=always",
    // own in-memory database so seeding does not leak into other tests
    "spring.datasource.url=jdbc:h2:mem:seedtest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1" })
class SeedDataTest {

  @Autowired
  private DataSource dataSource;
  @Autowired
  private JdbcTemplate jdbc;
  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ManufacturerRepository manufacturerRepository;
  @Autowired
  private SupplierRepository supplierRepository;

  @Test
  void seedDataIsLoaded() {
    assertThat(itemRepository.count()).isGreaterThan(0);
    assertThat(categoryRepository.count()).isGreaterThanOrEqualTo(9);
  }

  @Test
  void seedDataCanBeLoadedAgainWithoutErrorsOrDuplicates() {
    long items = itemRepository.count();
    long manufacturers = manufacturerRepository.count();

    new ResourceDatabasePopulator(new ClassPathResource("data.sql")).execute(dataSource);

    assertThat(itemRepository.count()).isEqualTo(items);
    assertThat(manufacturerRepository.count()).isEqualTo(manufacturers);
  }

  @Test
  void rowsCreatedAfterSeedingDoNotCollideWithSeededIds() {
    Manufacturer manufacturer = manufacturerRepository.save(manufacturer());
    Supplier supplier = supplierRepository.save(supplier());
    Category category = categoryRepository.save(category());

    Item item = new Item();
    item.setName("New item");
    item.setDescription("Created after seeding");
    item.setPrice(BigDecimal.TEN);
    item.setManufacturer(manufacturer);
    item.setSupplier(supplier);
    item.setCategory(category);
    item = itemRepository.save(item);

    Long maxSeededItemId = jdbc.queryForObject("select max(id) from item where name <> 'New item'", Long.class);
    assertThat(item.getId()).isGreaterThan(maxSeededItemId);
  }

  private Manufacturer manufacturer() {
    Manufacturer m = new Manufacturer();
    m.setName("New Manufacturer");
    m.setUrl("https://new.example.com");
    return m;
  }

  private Supplier supplier() {
    Supplier s = new Supplier();
    s.setName("New Supplier");
    s.setContactPerson("Bob");
    s.setContactPersonEmail("bob@example.com");
    return s;
  }

  private Category category() {
    Category c = new Category();
    c.setName("new-category");
    return c;
  }
}
