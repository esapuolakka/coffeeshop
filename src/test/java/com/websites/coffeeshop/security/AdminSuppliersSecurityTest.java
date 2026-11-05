package com.websites.coffeeshop.security;

import com.websites.coffeeshop.model.Supplier;
import com.websites.coffeeshop.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
class AdminSuppliersSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRepository supplierRepository;

    private Long supplierIdForTest;

    @BeforeEach
    void setUp() {
        supplierRepository.deleteAll();
        Supplier s = new Supplier();
        s.setName("Test Supplier");
        s.setContactPerson("Jane Doe");
        s.setContactPersonEmail("jane@example.com");
        supplierIdForTest = supplierRepository.save(s).getId();
    }

    // ---- List page (/admin/suppliers) ----

    @Test
    void anonymousCannotAccessSupplierList() throws Exception {
        mockMvc.perform(get("/admin/suppliers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotAccessSupplierList() throws Exception {
        mockMvc.perform(get("/admin/suppliers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessSupplierList() throws Exception {
        mockMvc.perform(get("/admin/suppliers"))
                .andExpect(status().isOk())
                .andExpect(view().name("adminSuppliers"));
    }

    // ---- Detail page (/admin/suppliers/{id}) ----

    @Test
    @WithMockUser(roles = "USER")
    void userCannotAccessSupplierDetails() throws Exception {
        mockMvc.perform(get("/admin/suppliers/" + supplierIdForTest))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessSupplierDetails() throws Exception {
        // csrf(): the page renders forms; create the token before the response is committed
        mockMvc.perform(get("/admin/suppliers/" + supplierIdForTest).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("adminSupplierDetails"));
    }

    // ---- Create (/admin/suppliers POST) ----

    @Test
    @WithMockUser(roles = "USER")
    void userCannotCreateSupplier() throws Exception {
        mockMvc.perform(post("/admin/suppliers")
                        .param("name", "Blocked Supplier")
                        .param("contactPerson", "Nobody")
                        .param("contactPersonEmail", "nobody@example.com")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateSupplier() throws Exception {
        mockMvc.perform(post("/admin/suppliers")
                        .param("name", "New Supplier")
                        .param("contactPerson", "John Smith")
                        .param("contactPersonEmail", "john@example.com")
                        .with(csrf()))
                .andExpect(status().isCreated());

        assertThat(supplierRepository.findAll())
                .anyMatch(s -> "New Supplier".equals(s.getName()));
    }

    // ---- Update (/admin/suppliers/{id} POST) ----

    @Test
    @WithMockUser(roles = "USER")
    void userCannotUpdateSupplier() throws Exception {
        mockMvc.perform(post("/admin/suppliers/" + supplierIdForTest)
                        .param("name", "Hacked Supplier")
                        .param("contactPerson", "Hacker")
                        .param("contactPersonEmail", "hack@example.com")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanUpdateSupplier() throws Exception {
        mockMvc.perform(post("/admin/suppliers/" + supplierIdForTest)
                        .param("name", "Updated Supplier")
                        .param("contactPerson", "Updated Person")
                        .param("contactPersonEmail", "updated@example.com")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        Supplier updated = supplierRepository.findById(supplierIdForTest).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Supplier");
    }

    // ---- Delete (/admin/suppliers/{id}/delete DELETE) ----

    @Test
    @WithMockUser(roles = "USER")
    void userCannotDeleteSupplier() throws Exception {
        mockMvc.perform(delete("/admin/suppliers/" + supplierIdForTest + "/delete")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteSupplier() throws Exception {
        mockMvc.perform(delete("/admin/suppliers/" + supplierIdForTest + "/delete")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(supplierRepository.findById(supplierIdForTest)).isEmpty();
    }
}
