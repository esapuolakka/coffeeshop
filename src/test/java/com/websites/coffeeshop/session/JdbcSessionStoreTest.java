package com.websites.coffeeshop.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import com.websites.coffeeshop.model.Cart;

/**
 * Verifies that sessions (and the Cart stored in them) are persisted in the
 * database, which is what lets several app instances share one session.
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@SuppressWarnings("unchecked")
class JdbcSessionStoreTest {

  // Raw type: the concrete JdbcSession type is package-private in Spring Session.
  @SuppressWarnings("rawtypes")
  @Autowired
  private SessionRepository sessions;

  @Test
  void cartSurvivesRoundTripThroughSessionStore() {
    Session session = (Session) sessions.createSession();
    Cart cart = new Cart();
    cart.addItem(1L, "Espresso", new BigDecimal("3.50"), 3.0, 2);
    session.setAttribute("cart", cart);
    session.setAttribute("totalPrice", new BigDecimal("7.00"));
    sessions.save(session);

    // A fresh lookup by id, as another instance would do, reads from the database.
    Session loaded = (Session) sessions.findById(session.getId());

    assertNotNull(loaded);
    Cart loadedCart = loaded.getAttribute("cart");
    assertEquals(1, loadedCart.getItems().size());
    assertEquals("Espresso", loadedCart.getItems().get(0).getName());
    assertEquals(2, loadedCart.getItems().get(0).getQuantity());
    assertEquals(new BigDecimal("7.00"), loaded.<BigDecimal>getAttribute("totalPrice"));
  }

  @Test
  void deletedSessionIsGone() {
    Session session = (Session) sessions.createSession();
    session.setAttribute("x", "y");
    sessions.save(session);
    sessions.deleteById(session.getId());

    assertNull(sessions.findById(session.getId()));
  }
}
