package com.websites.coffeeshop.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.websites.coffeeshop.CoffeeshopApplication;
import com.websites.coffeeshop.repository.UserRepository;

/**
 * The prod profile takes its secrets from the environment and has no defaults for them.
 * The database here is H2 in PostgreSQL mode, standing in for RDS.
 */
@SpringBootTest(properties = {
    "SPRING_DATASOURCE_URL=jdbc:h2:mem:prodtest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
    "SPRING_DATASOURCE_USERNAME=sa",
    "SPRING_DATASOURCE_PASSWORD=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "APP_ADMIN_PASSWORD=Sup3r-secret-test-pw",
    "APP_REMEMBER_ME_KEY=test-remember-me-key" })
@ActiveProfiles("prod")
@AutoConfigureMockMvc
class ProdProfileTest {

  @Autowired
  private ApplicationContext context;
  @Autowired
  private Environment environment;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void adminPasswordComesFromTheEnvironmentNotTheDevelopmentDefault() {
    var admin = userRepository.findByUsername("admin").orElseThrow();

    assertThat(passwordEncoder.matches("Sup3r-secret-test-pw", admin.getPassword())).isTrue();
    assertThat(passwordEncoder.matches("admin123", admin.getPassword())).isFalse();
  }

  @Test
  void healthEndpointIsPublicAndReportsUp() throws Exception {
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void otherActuatorEndpointsAreNotExposed() throws Exception {
    for (String path : new String[] { "/actuator/env", "/actuator/beans", "/actuator/heapdump" }) {
      int status = mockMvc.perform(get(path)).andReturn().getResponse().getStatus();
      // 302 to the login page or 404: either way nothing is served
      assertThat(status).as(path).isIn(302, 401, 403, 404);
    }
  }

  @Test
  void h2ConsoleIsDisabledAndLoggingIsQuiet() {
    assertThat(environment.getProperty("spring.h2.console.enabled")).isEqualTo("false");
    assertThat(context.containsBean("h2Console")).isFalse();
    assertThat(environment.getProperty("logging.level.root")).isEqualTo("INFO");
  }

  @Test
  void startupFailsWithoutAnAdminPassword() {
    assertThatThrownBy(() -> new SpringApplicationBuilder(CoffeeshopApplication.class)
        .profiles("prod")
        .web(WebApplicationType.NONE)
        .run(
            // command-line arguments: highest precedence, so they beat application-prod.properties
            "--spring.main.banner-mode=off",
            "--SPRING_DATASOURCE_URL=jdbc:h2:mem:prodtest-nopw;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "--SPRING_DATASOURCE_USERNAME=sa",
            "--SPRING_DATASOURCE_PASSWORD=",
            "--spring.datasource.driver-class-name=org.h2.Driver",
            "--APP_REMEMBER_ME_KEY=test-remember-me-key"))
        .hasStackTraceContaining("APP_ADMIN_PASSWORD");
  }
}
