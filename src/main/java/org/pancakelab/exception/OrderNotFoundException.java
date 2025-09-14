package org.pancakelab.exception;

import java.util.UUID;

public class OrderNotFoundException extends OrderUpdateException {
  public OrderNotFoundException(UUID orderId) {
    super("Order with id: " + orderId + " not found");
  }
}
