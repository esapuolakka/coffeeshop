package com.websites.coffeeshop.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.websites.coffeeshop.model.Category;
import com.websites.coffeeshop.model.Manufacturer;
import com.websites.coffeeshop.model.Supplier;
import com.websites.coffeeshop.repository.CategoryRepository;
import com.websites.coffeeshop.repository.ImageRepository;
import com.websites.coffeeshop.repository.ItemRepository;
import com.websites.coffeeshop.repository.ManufacturerRepository;
import com.websites.coffeeshop.repository.SupplierRepository;

/**
 * Adding a product through the admin form (what the "add product" dialog posts).
 * The dialog only checks the HTTP status, so the endpoint must answer 201 with a
 * small body; it used to return the whole JPA entity, which cannot be serialized.
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc
class AdminProductUploadTest {

  private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 1, 2, 3 };
  private static final byte[] JPEG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 1, 2, 3 };

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
    category.setName("coffee");
    category = categoryRepository.save(category);
  }

  @Test
  void addingAProductWithAPngImageAnswersCreatedWithItsId() throws Exception {
    MvcResult result = mockMvc.perform(addProduct("Beans", image("a.png", "image/png", PNG)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andReturn();

    assertThat(result.getResponse().getContentAsString()).as("small body, not the whole entity graph")
        .hasSizeLessThan(100);
    assertThat(itemRepository.count()).isEqualTo(1);
    assertThat(imageRepository.count()).isEqualTo(1);
  }

  @Test
  void addingAProductWithAJpegImageWorks() throws Exception {
    mockMvc.perform(addProduct("Beans", image("a.jpg", "image/jpeg", JPEG)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber());

    assertThat(imageRepository.findAll()).extracting("mediaType").containsExactly("image/jpeg");
  }

  @Test
  void addingProductsOneAfterAnotherKeepsWorking() throws Exception {
    // the first call used to succeed and every later one failed with a 500
    for (int i = 1; i <= 3; i++) {
      mockMvc.perform(addProduct("Beans " + i, image("a.png", "image/png", PNG)))
          .andExpect(status().isCreated());
    }
    assertThat(itemRepository.count()).isEqualTo(3);
  }

  @Test
  void addingAProductWithoutAnImageWorks() throws Exception {
    mockMvc.perform(addProduct("Beans", null))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber());

    assertThat(imageRepository.count()).isZero();
  }

  @Test
  void anUnsupportedImageTypeIsRejectedWithAReadableMessage() throws Exception {
    mockMvc.perform(addProduct("Beans", image("a.gif", "image/gif", new byte[] { 1, 2, 3 })))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Only PNG and JPEG images are allowed")));

    assertThat(itemRepository.count()).isZero();
  }

  @Test
  void anImageOverOneMegabyteIsRejectedWithAReadableMessage() throws Exception {
    mockMvc.perform(addProduct("Beans", image("big.jpg", "image/jpeg", new byte[1024 * 1024 + 1])))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Image size must be less than 1MB")));

    assertThat(itemRepository.count()).isZero();
  }

  private MockMultipartHttpServletRequestBuilder addProduct(String name, MockMultipartFile image) {
    MockMultipartHttpServletRequestBuilder request = multipart("/admin/products");
    if (image != null) {
      request.file(image);
    }
    request.param("name", name)
        .param("description", "A product")
        .param("price", "9.90")
        .param("categoryId", String.valueOf(category.getId()))
        .param("manufacturer.id", String.valueOf(manufacturer.getId()))
        .param("supplier.id", String.valueOf(supplier.getId()));
    request.with(csrf()).with(user("admin").roles("ADMIN"));
    return request;
  }

  private MockMultipartFile image(String filename, String contentType, byte[] bytes) {
    return new MockMultipartFile("image", filename, contentType, bytes);
  }
}
