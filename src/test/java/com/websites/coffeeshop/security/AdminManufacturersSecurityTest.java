package com.websites.coffeeshop.security;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.websites.coffeeshop.model.Manufacturer;
import com.websites.coffeeshop.repository.ManufacturerRepository;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
class AdminManufacturersSecurityTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ManufacturerRepository manufacturerRepository;

  private Long manufacturerId;

  @BeforeEach
  void setUp() {
    manufacturerRepository.deleteAll();
    Manufacturer manufacturer = new Manufacturer();
    manufacturer.setName("Test Manufacturer");
    manufacturer.setUrl("https://example.com");
    manufacturerId = manufacturerRepository.save(manufacturer).getId();
  }

  @Test
  void anonymousUserIsRedirectedToLoginForManufacturersPage() throws Exception {
    mockMvc.perform(get("/admin/manufacturers"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("/login")));
  }

  @Test
  void regularUserCannotAccessManufacturersPage() throws Exception {
    mockMvc.perform(get("/admin/manufacturers").with(user("regular").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUserCanAccessManufacturersPage() throws Exception {
    mockMvc.perform(get("/admin/manufacturers").with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(view().name("adminManufacturers"))
        .andExpect(model().attributeExists("manufacturers"));
  }

  @Test
  void regularUserCannotAccessManufacturerDetailsPage() throws Exception {
    mockMvc.perform(get("/admin/manufacturers/{id}", manufacturerId).with(user("regular").roles("USER")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUserCanAccessManufacturerDetailsPage() throws Exception {
    mockMvc.perform(get("/admin/manufacturers/{id}", manufacturerId).with(csrf()).with(user("admin").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(view().name("adminManufacturerDetails"))
        .andExpect(model().attributeExists("manufacturer"))
        .andExpect(model().attributeExists("manufacturerItems"));
  }

  @Test
  void regularUserCannotCreateManufacturer() throws Exception {
    mockMvc.perform(post("/admin/manufacturers")
            .with(user("regular").roles("USER"))
            .with(csrf())
            .param("name", "New Manufacturer")
            .param("url", "https://new.example.com"))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUserCanCreateManufacturer() throws Exception {
    mockMvc.perform(post("/admin/manufacturers")
            .with(user("admin").roles("ADMIN"))
            .with(csrf())
            .param("name", "New Manufacturer")
            .param("url", "https://new.example.com"))
        .andExpect(status().isCreated());

    boolean exists = manufacturerRepository.findAll().stream()
        .anyMatch(m -> "New Manufacturer".equals(m.getName()));
    if (!exists) {
      throw new AssertionError("Expected manufacturer to be created");
    }
  }

  @Test
  void regularUserCannotUpdateManufacturer() throws Exception {
    mockMvc.perform(post("/admin/manufacturers/{id}", manufacturerId)
            .with(user("regular").roles("USER"))
            .with(csrf())
            .param("name", "Updated Name")
            .param("url", "https://updated.example.com"))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUserCanUpdateManufacturer() throws Exception {
    mockMvc.perform(post("/admin/manufacturers/{id}", manufacturerId)
            .with(user("admin").roles("ADMIN"))
            .with(csrf())
            .param("name", "Updated Name")
            .param("url", "https://updated.example.com"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/manufacturers/" + manufacturerId));

    Manufacturer updated = manufacturerRepository.findById(manufacturerId)
        .orElseThrow(() -> new AssertionError("Manufacturer should exist"));
    if (!"Updated Name".equals(updated.getName())) {
      throw new AssertionError("Expected manufacturer name to be updated");
    }
  }

  @Test
  void regularUserCannotDeleteManufacturer() throws Exception {
    mockMvc.perform(delete("/admin/manufacturers/{id}/delete", manufacturerId)
            .with(user("regular").roles("USER"))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUserCanDeleteManufacturer() throws Exception {
    mockMvc.perform(delete("/admin/manufacturers/{id}/delete", manufacturerId)
            .with(user("admin").roles("ADMIN"))
            .with(csrf()))
        .andExpect(status().isNoContent());

    boolean exists = manufacturerRepository.existsById(manufacturerId);
    if (exists) {
      throw new AssertionError("Expected manufacturer to be deleted");
    }
  }
}
