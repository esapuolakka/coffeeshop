package com.websites.coffeeshop.controller;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.websites.coffeeshop.model.Category;
import com.websites.coffeeshop.model.Image;
import com.websites.coffeeshop.model.Item;
import com.websites.coffeeshop.model.ItemDTO;
import com.websites.coffeeshop.model.Manufacturer;
import com.websites.coffeeshop.model.Supplier;
import com.websites.coffeeshop.model.User;
import com.websites.coffeeshop.service.AdminService;
import com.websites.coffeeshop.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

  private static final Logger logger = Logger.getLogger(AdminController.class.getName());

  @Autowired
  private AdminService adminService;

  @Autowired
  private UserService userService;

  @GetMapping
  public String adminHome(Model model) {
    return "admin";
  }

  @GetMapping("/products")
  public String allItemsList(@RequestParam(value = "categoryId", defaultValue = "1") Long categoryId, Model model,
      Pageable pageable) {
    Page<Item> items = adminService.getAllByCategory(categoryId, pageable);
    List<Manufacturer> manufacturers = adminService.getAllManufacturers();
    List<Supplier> suppliers = adminService.getAllSuppliers();
    List<Category> categories = adminService.getAllCategories();

    model.addAttribute("items", items);
    model.addAttribute("selectedCategory", categoryId);
    model.addAttribute("manufacturers", manufacturers);
    model.addAttribute("suppliers", suppliers);
    model.addAttribute("categories", categories);
    model.addAttribute("selectedCategory", categoryId);

    return "adminItems";
  }

  @PostMapping("/products")
  public ResponseEntity<Item> addNewItem(@ModelAttribute ItemDTO itemDTO,
      @RequestParam(value = "image", required = false) MultipartFile file) {
    Item newItem = adminService.addNewItem(itemDTO, file);
    return new ResponseEntity<>(newItem, HttpStatus.CREATED);
  }

  @GetMapping("/products/{id}")
  public String itemDetailPage(@PathVariable Long id, Model model) {
    Item item = adminService.getItemById(id);
    Long nextItem = adminService.getNextItemId(id);
    Long previousItem = adminService.getPreviousItemId(id);
    List<Manufacturer> manufacturers = adminService.getAllManufacturers();
    List<Supplier> suppliers = adminService.getAllSuppliers();
    List<Image> images = adminService.getAllImages();
    List<Category> categories = adminService.getAllCategories();

    model.addAttribute("item", item);
    model.addAttribute("nextItem", nextItem);
    model.addAttribute("previousItem", previousItem);
    model.addAttribute("manufacturers", manufacturers);
    model.addAttribute("suppliers", suppliers);
    model.addAttribute("images", images);
    model.addAttribute("categories", categories);

    return "adminItemDetails";
  }

  @GetMapping("/products/{id}/image")
  public ResponseEntity<byte[]> viewItemImage(@PathVariable Long id) {
    Image image = adminService.getImageById(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(image.getMediaType()))
        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
        .body(adminService.getImageContent(image));
  }

  @PostMapping("/products/{id}")
  public String updateItem(
      @PathVariable Long id,
      @ModelAttribute ItemDTO itemDTO,
      @RequestParam(value = "image", required = false) MultipartFile file) throws IOException {
    logger.warning("ItemDTO: " + itemDTO);
    itemDTO.setId(id);
    adminService.updateItem(itemDTO, file);

    return "redirect:/admin/products/" + id;
  }

  @DeleteMapping("/products/{id}/delete")
  public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
    adminService.deleteItem(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping("/manufacturers")
  public String allManufacturers(Model model) {
    List<Manufacturer> manufacturers = adminService.getAllManufacturers();
    model.addAttribute("manufacturers", manufacturers);
    return "adminManufacturers";
  }

  @PostMapping("/manufacturers")
  public ResponseEntity<Manufacturer> addNewManufacturer(@ModelAttribute Manufacturer manufacturer) {
    Manufacturer newManufacturer = adminService.addManufacturer(manufacturer);
    return new ResponseEntity<>(newManufacturer, HttpStatus.CREATED);
  }

  @GetMapping("/manufacturers/{id}")
  public String manufacturerDetailPage(@PathVariable Long id, Model model) {
    Manufacturer manufacturer = adminService.getManufacturerById(id);
    List<Item> manufacturerItems = adminService.getItemsByManufacturer(id);
    model.addAttribute("manufacturer", manufacturer);
    model.addAttribute("manufacturerItems", manufacturerItems);
    return "adminManufacturerDetails";
  }

  @PostMapping("/manufacturers/{id}")
  public String updateManufacturer(@PathVariable Long id, @ModelAttribute Manufacturer manufacturer) {
    manufacturer.setId(id);
    adminService.updateManufacturer(manufacturer);
    return "redirect:/admin/manufacturers/" + id;
  }

  @DeleteMapping("/manufacturers/{id}/delete")
  public ResponseEntity<Void> deleteManufacturer(@PathVariable Long id) {
    adminService.deleteManufacturer(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping("/suppliers")
  public String allSuppliers(Model model) {
    List<Supplier> suppliers = adminService.getAllSuppliers();
    model.addAttribute("suppliers", suppliers);
    return "adminSuppliers";
  }

  @PostMapping("/suppliers")
  public ResponseEntity<Supplier> addNewSupplier(@ModelAttribute Supplier supplier) {
    Supplier newSupplier = adminService.addSupplier(supplier);
    return new ResponseEntity<>(newSupplier, HttpStatus.CREATED);
  }

  @GetMapping("/suppliers/{id}")
  public String supplierDetailPage(@PathVariable Long id, Model model) {
    Supplier supplier = adminService.getSupplierById(id);
    List<Item> supplierItems = adminService.getItemsBySupplier(id);
    model.addAttribute("supplier", supplier);
    model.addAttribute("supplierItems", supplierItems);
    return "adminSupplierDetails";
  }

  @PostMapping("/suppliers/{id}")
  public String updateSupplier(@PathVariable Long id, @ModelAttribute Supplier supplier) {
    supplier.setId(id);
    adminService.updateSupplier(supplier);
    return "redirect:/admin/suppliers/" + id;
  }

  @DeleteMapping("/suppliers/{id}/delete")
  public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
    adminService.deleteSupplier(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping("/users")
  public String allUsers(Model model) {
    List<User> users = userService.getAllUsers();
    model.addAttribute("users", users);
    return "adminUsers";
  }

  @PostMapping("/users/{id}")
  public String saveUserRole(@PathVariable Long id, @RequestParam String role) {
    userService.updateUserRole(id, role);
    return "redirect:/admin/users";
  }

  @PostMapping("/users/{id}/delete")
  public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      userService.deleteUserById(id);
      redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user.");
    }
    return "redirect:/admin/users";
  }

  @GetMapping("/settings")
  public String showSettingsPage(Model model) {
    double discount = adminService.getDiscount().getDiscount();
    model.addAttribute("discount", discount);
    return "adminSettings";
  }

  @PostMapping("/settings")
  public String saveDiscount(@RequestParam double discount) {
    adminService.updateDiscount(discount);
    return "redirect:/admin/settings";
  }
}
