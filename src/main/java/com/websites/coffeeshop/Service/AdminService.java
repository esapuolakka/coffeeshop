package com.websites.coffeeshop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.websites.coffeeshop.repository.CategoryRepository;
import com.websites.coffeeshop.repository.DiscountRepository;
import com.websites.coffeeshop.repository.ImageRepository;
import com.websites.coffeeshop.repository.ItemRepository;
import com.websites.coffeeshop.repository.ManufacturerRepository;
import com.websites.coffeeshop.repository.SupplierRepository;
import com.websites.coffeeshop.model.Category;
import com.websites.coffeeshop.model.Discount;
import com.websites.coffeeshop.model.Image;
import com.websites.coffeeshop.model.Item;
import com.websites.coffeeshop.model.ItemDTO;
import com.websites.coffeeshop.model.Manufacturer;
import com.websites.coffeeshop.model.Supplier;
import com.websites.coffeeshop.storage.ImageStorage;

@Service
public class AdminService {

  private static final Logger log = LoggerFactory.getLogger(AdminService.class);

  @Autowired
  private ImageStorage imageStorage;
  @Autowired
  private ItemRepository itemRepository;
  @Autowired
  private SupplierRepository supplierRepository;
  @Autowired
  private ManufacturerRepository manufacturerRepository;
  @Autowired
  private ImageRepository imageRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private DiscountRepository discountRepository;

  public List<Item> getAllItems() {
    return itemRepository.findAll();
  }

  public Page<Item> getAllByCategory(Long categoryId, Pageable pageable) {
    return itemRepository.findByCategoryId(categoryId, pageable);
  }

  public Item getItemById(Long id) {
    return itemRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Item not found"));
  }

  public Long getNextItemId(Long id) {
    Optional<Item> nextItem = itemRepository.findNextItem(id);
    return nextItem.map(Item::getId).orElse(null);
  }

  public Long getPreviousItemId(Long id) {
    Optional<Item> previousItem = itemRepository.findPreviousItem(id);
    return previousItem.map(Item::getId).orElse(null);
  }

  public Image getImageById(Long id) {
    return imageRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Image not found"));
  }

  public byte[] getImageContent(Image image) {
    return imageStorage.get(image.getStorageKey());
  }

  public List<Manufacturer> getAllManufacturers() {
    List<Manufacturer> manufacturers = manufacturerRepository.findAll();
    if (manufacturers == null) {
      return new ArrayList<>();
    }
    return manufacturers;
  }

  public Manufacturer getManufacturerById(Long id) {
    return manufacturerRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Manufacturer not found"));
  }

  public Supplier getSupplierById(Long id) {
    return supplierRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Supplier not found"));
  }

  public List<Item> getItemsByManufacturer(Long id) {
    return itemRepository.findByManufacturerId(id);
  }

  public List<Item> getItemsBySupplier(Long id) {
    return itemRepository.findBySupplierId(id);
  }

  public List<Supplier> getAllSuppliers() {
    List<Supplier> suppliers = supplierRepository.findAll();
    if (suppliers == null) {
      return new ArrayList<>();
    }
    return suppliers;
  }

  public List<Image> getAllImages() {
    return imageRepository.findAll();
  }

  public List<Category> getAllCategories() {
    List<Category> categories = categoryRepository.findAll();
    if (categories == null) {
      return new ArrayList<>();
    }
    return categories;
  }

  public Item addNewItem(ItemDTO item, MultipartFile file) {
    Item newItem = new Item();
    // Tarkistaa onko kentät tyhjiä
    validateItem(item);

    newItem.setName(item.getName());
    newItem.setDescription(item.getDescription());
    newItem.setPrice(item.getPrice());

    Manufacturer manufacturer = manufacturerRepository.findById(item.getManufacturer().getId())
        .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

    newItem.setManufacturer(manufacturer);

    Supplier supplier = supplierRepository.findById(item.getSupplier().getId())
        .orElseThrow(() -> new RuntimeException("Supplier not found"));

    newItem.setSupplier(supplier);

    Category category = categoryRepository.findById(item.getCategoryId())
        .orElseThrow(() -> new RuntimeException("Category not found"));

    newItem.setCategory(category);

    if (file != null && !file.isEmpty()) {
      Image newImage = createImageFromFile(file);
      newItem.setImage(newImage);
    }

    try {
      return itemRepository.save(newItem);
    } catch (RuntimeException e) {
      if (newItem.getImage() != null) {
        deleteStoredImageQuietly(newItem.getImage().getStorageKey());
      }
      throw e;
    }
  }

  @Transactional
  public Item updateItem(ItemDTO updatedItem, MultipartFile file) {

    Item existingItem = itemRepository.findById(updatedItem.getId())
        .orElseThrow(() -> new RuntimeException("Item not found"));

    // Tarkistaa onko kentät tyhjiä
    validateItem(updatedItem);

    Manufacturer manufacturer = manufacturerRepository.findById(updatedItem.getManufacturer().getId())
        .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

    Supplier supplier = supplierRepository.findById(updatedItem.getSupplier().getId())
        .orElseThrow(() -> new RuntimeException("Supplier not found"));

    Category category = categoryRepository.findById(updatedItem.getCategoryId())
        .orElseThrow(() -> new RuntimeException("Category not found"));

    // Takistaa onko muutoksia tehty. Mikäli ei, ei päivitetä
    boolean isImageUpdated = file != null && !file.isEmpty();

    if (existingItem.getName().equals(updatedItem.getName()) &&
        existingItem.getDescription().equals(updatedItem.getDescription()) &&
        existingItem.getPrice().equals(updatedItem.getPrice()) &&
        existingItem.getManufacturer().getId().equals(manufacturer.getId()) &&
        existingItem.getSupplier().getId().equals(supplier.getId())
        && existingItem.getCategory().getId().equals(category.getId()) &&
        !isImageUpdated) {
      return existingItem;
    }

    Image replacedImage = isImageUpdated ? existingItem.getImage() : null;
    String replacedKey = replacedImage != null ? replacedImage.getStorageKey() : null;

    if (isImageUpdated) {
      Image newImage = createImageFromFile(file);
      existingItem.setImage(newImage);
      // if the transaction does not commit, the freshly uploaded object is orphaned
      deleteStoredImageAfterTransaction(newImage.getStorageKey(), false);
    }

    existingItem.setName(updatedItem.getName());
    existingItem.setDescription(updatedItem.getDescription());
    existingItem.setPrice(updatedItem.getPrice());
    existingItem.setManufacturer(manufacturer);
    existingItem.setSupplier(supplier);
    existingItem.setCategory(category);

    Item saved = itemRepository.save(existingItem);

    if (replacedImage != null) {
      imageRepository.delete(replacedImage);
      // only once the database no longer references it
      deleteStoredImageAfterTransaction(replacedKey, true);
    }
    return saved;
  }

  private void validateItem(ItemDTO updatedItem) {
    if (updatedItem.getName() == null || updatedItem.getName().isEmpty()) {
      throw new RuntimeException("Name cannot be empty");
    }
    if (updatedItem.getDescription() == null || updatedItem.getDescription().isEmpty()) {
      throw new RuntimeException("Description cannot be empty");
    }
    if (updatedItem.getPrice() == null || updatedItem.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Price must be greater than zero");
    }
    if (updatedItem.getCategoryId() == null) {
      throw new RuntimeException("Category ID cannot be null");
    }
    if (updatedItem.getManufacturer() == null) {
      throw new RuntimeException("Manufacturer cannot be null");
    }
    if (updatedItem.getSupplier() == null) {
      throw new RuntimeException("Supplier cannot be null");
    }
  }

  private Image createImageFromFile(MultipartFile file) {

    // Kuvan tulee olla PNG tia JPEG ja koko alle 1MB
    if (!("image/png".equals(file.getContentType()) ||
        "image/jpeg".equals(file.getContentType()) ||
        "image/jpg".equals(file.getContentType()))) {
      throw new IllegalArgumentException("Only PNG and JPEG images are allowed");
    }

    if (file.getSize() > 1024 * 1024)
      throw new IllegalArgumentException("Image size must be less than 1MB");

    try {
      String extension = "image/png".equals(file.getContentType()) ? ".png" : ".jpg";
      String storageKey = "images/" + UUID.randomUUID() + extension;
      imageStorage.put(storageKey, file.getBytes(), file.getContentType());

      Image newImage = new Image();
      newImage.setName(file.getOriginalFilename());
      newImage.setMediaType(file.getContentType());
      newImage.setSize(file.getSize());
      newImage.setStorageKey(storageKey);
      return newImage;
    } catch (IOException e) {
      throw new RuntimeException("Failed to store image", e);
    }
  }

  /** Cleanup must never hide the original problem or fail a request that already succeeded. */
  private void deleteStoredImageQuietly(String key) {
    if (key == null) {
      return;
    }
    try {
      imageStorage.delete(key);
    } catch (RuntimeException e) {
      log.warn("Could not delete stored image {}", key, e);
    }
  }

  /**
   * Deletes the stored object once the current transaction has completed: when it
   * committed ({@code onCommit}) or when it did not. The key is passed as a string
   * because a lazily loaded Image cannot be read after the session is closed.
   */
  private void deleteStoredImageAfterTransaction(String key, boolean onCommit) {
    if (key == null) {
      return;
    }
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      if (onCommit) {
        deleteStoredImageQuietly(key);
      }
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCompletion(int status) {
        if ((status == STATUS_COMMITTED) == onCommit) {
          deleteStoredImageQuietly(key);
        }
      }
    });
  }

  public Manufacturer addManufacturer(Manufacturer manufacturer) {
    if (manufacturer.getName() == null || manufacturer.getName().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be empty");
    }
    if (manufacturer.getUrl() == null || manufacturer.getUrl().isEmpty()) {
      throw new IllegalArgumentException("URL cannot be empty");
    }
    return manufacturerRepository.save(manufacturer);
  }

  public Manufacturer updateManufacturer(Manufacturer manufacturer) {
    Manufacturer existingManufacturer = manufacturerRepository.findById(manufacturer.getId())
        .orElseThrow(() -> new RuntimeException("Manufacturer not found"));

    if (manufacturer.getName() == null || manufacturer.getName().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be empty");
    }
    if (manufacturer.getUrl() == null || manufacturer.getUrl().isEmpty()) {
      throw new IllegalArgumentException("URL cannot be empty");
    }
    if (existingManufacturer.getName().equals(manufacturer.getName()) &&
        existingManufacturer.getUrl().equals(manufacturer.getUrl())) {
      return existingManufacturer;
    }

    return manufacturerRepository.save(manufacturer);
  }

  public Supplier addSupplier(Supplier supplier) {
    if (supplier.getName() == null || supplier.getName().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be empty");
    }
    if (supplier.getContactPerson() == null || supplier.getContactPerson().isEmpty()) {
      throw new IllegalArgumentException("Contact person cannot be empty");
    }
    if (supplier.getContactPersonEmail() == null || supplier.getContactPersonEmail().isEmpty()) {
      throw new IllegalArgumentException("Contact person email cannot be empty");
    }
    return supplierRepository.save(supplier);
  }

  public Supplier updateSupplier(Supplier supplier) {
    Supplier existingSupplier = supplierRepository.findById(supplier.getId())
        .orElseThrow(() -> new RuntimeException("Supplier not found"));

    if (supplier.getName() == null || supplier.getName().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be empty");
    }
    if (supplier.getContactPerson() == null || supplier.getContactPerson().isEmpty()) {
      throw new IllegalArgumentException("Contact person cannot be empty");
    }
    if (supplier.getContactPersonEmail() == null || supplier.getContactPersonEmail().isEmpty()) {
      throw new IllegalArgumentException("Contact person email cannot be empty");
    }
    if (existingSupplier.getName().equals(supplier.getName()) &&
        existingSupplier.getContactPerson().equals(supplier.getContactPerson()) &&
        existingSupplier.getContactPersonEmail().equals(supplier.getContactPersonEmail())) {
      return existingSupplier;
    }

    return supplierRepository.save(supplier);
  }

  @Transactional
  public void deleteItem(Long id) {
    Item item = itemRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    Image image = item.getImage();
    String key = image != null ? image.getStorageKey() : null;
    itemRepository.deleteById(id); // cascades to the image row
    deleteStoredImageAfterTransaction(key, true);
  }

  public void deleteManufacturer(Long id) {
    if (manufacturerRepository.existsById(id)) {
      manufacturerRepository.deleteById(id);
    } else {
      throw new RuntimeException("Manufacturer not found with id: " + id);
    }
  }

  public void deleteSupplier(Long id) {
    if (supplierRepository.existsById(id)) {
      supplierRepository.deleteById(id);
    } else {
      throw new RuntimeException("Supplier not found with id: " + id);
    }
  }

  public Discount getDiscount() {
    return discountRepository.findAll().stream()
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Discount not found"));
  }

  public Discount updateDiscount(double discount) {
    Discount existingDiscount = getDiscount();
    existingDiscount.setDiscount(discount);
    return discountRepository.save(existingDiscount);
  }

}
