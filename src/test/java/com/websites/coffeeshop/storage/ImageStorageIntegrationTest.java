package com.websites.coffeeshop.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.websites.coffeeshop.model.Category;
import com.websites.coffeeshop.model.Item;
import com.websites.coffeeshop.model.ItemDTO;
import com.websites.coffeeshop.model.Manufacturer;
import com.websites.coffeeshop.model.Supplier;
import com.websites.coffeeshop.repository.CategoryRepository;
import com.websites.coffeeshop.repository.ImageRepository;
import com.websites.coffeeshop.repository.ItemRepository;
import com.websites.coffeeshop.repository.ManufacturerRepository;
import com.websites.coffeeshop.repository.SupplierRepository;
import com.websites.coffeeshop.service.AdminService;

/** Image bytes live in the ImageStorage, the database keeps only metadata and the key. */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc
class ImageStorageIntegrationTest {

  private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 1, 2, 3 };
  private static final byte[] PNG_2 = { (byte) 0x89, 'P', 'N', 'G', 9, 9, 9 };

  @Autowired
  private AdminService adminService;
  @Autowired
  private ImageStorage imageStorage;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private ImageRepository imageRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ManufacturerRepository manufacturerRepository;
  @Autowired
  private SupplierRepository supplierRepository;

  private Manufacturer manufacturer;
  private Supplier supplier;
  private Category category;

  @BeforeEach
  void setUp() {
    itemRepository.deleteAll();
    imageRepository.deleteAll();
    categoryRepository.deleteAll();
    manufacturerRepository.deleteAll();
    supplierRepository.deleteAll();

    manufacturer = new Manufacturer();
    manufacturer.setName("BrewCo");
    manufacturer.setUrl("https://brewco.example.com");
    manufacturer = manufacturerRepository.save(manufacturer);

    supplier = new Supplier();
    supplier.setName("Supply Co");
    supplier.setContactPerson("Alice");
    supplier.setContactPersonEmail("alice@example.com");
    supplier = supplierRepository.save(supplier);

    category = new Category();
    category.setName("Espresso Machines");
    category = categoryRepository.save(category);
  }

  @Test
  void uploadedImageBytesGoToStorageNotToDatabase() {
    Item item = adminService.addNewItem(dto("Machine"), png(PNG));

    String key = item.getImage().getStorageKey();
    assertThat(key).startsWith("images/").endsWith(".png");
    assertThat(imageStorage.get(key)).isEqualTo(PNG);
    assertThat(item.getImage().getSize()).isEqualTo(PNG.length);
  }

  @Test
  void publicImageEndpointServesBytesFromStorageWithCacheHeaders() throws Exception {
    Item item = adminService.addNewItem(dto("Machine"), png(PNG));

    mockMvc.perform(get("/products/{category}/{id}/image", category.getName(), item.getImage().getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType("image/png"))
        .andExpect(content().bytes(PNG))
        .andExpect(header().string("Cache-Control", containsString("max-age=86400")))
        .andExpect(header().string("Cache-Control", containsString("public")));
  }

  @Test
  void replacingTheImageRemovesTheOldStoredObject() {
    Item item = adminService.addNewItem(dto("Machine"), png(PNG));
    String oldKey = item.getImage().getStorageKey();

    ItemDTO update = dto("Machine");
    update.setId(item.getId());
    adminService.updateItem(update, png(PNG_2));

    String newKey = imageRepository.findAll().get(0).getStorageKey();
    assertThat(newKey).isNotEqualTo(oldKey);
    assertThat(imageStorage.get(newKey)).isEqualTo(PNG_2);
    assertThatThrownBy(() -> imageStorage.get(oldKey)).isInstanceOf(ImageNotFoundException.class);
    assertThat(imageRepository.count()).isEqualTo(1);
  }

  @Test
  void editingTextFieldsKeepsTheExistingImage() {
    Item item = adminService.addNewItem(dto("Machine"), png(PNG));
    String key = item.getImage().getStorageKey();

    ItemDTO update = dto("Renamed machine");
    update.setId(item.getId());
    Item updated = adminService.updateItem(update, null);

    assertThat(updated.getName()).isEqualTo("Renamed machine");
    assertThat(imageRepository.findAll()).extracting("storageKey").containsExactly(key);
    assertThat(imageStorage.get(key)).isEqualTo(PNG);
  }

  @Test
  void deletingTheItemRemovesTheStoredObject() {
    Item item = adminService.addNewItem(dto("Machine"), png(PNG));
    String key = item.getImage().getStorageKey();

    adminService.deleteItem(item.getId());

    assertThatThrownBy(() -> imageStorage.get(key)).isInstanceOf(ImageNotFoundException.class);
    assertThat(imageRepository.count()).isZero();
  }

  private MockMultipartFile png(byte[] bytes) {
    return new MockMultipartFile("image", "item.png", "image/png", bytes);
  }

  private ItemDTO dto(String name) {
    ItemDTO dto = new ItemDTO();
    dto.setName(name);
    dto.setDescription("A great item");
    dto.setPrice(BigDecimal.valueOf(99.99));
    dto.setCategoryId(category.getId());
    dto.setManufacturer(manufacturer);
    dto.setSupplier(supplier);
    return dto;
  }
}
