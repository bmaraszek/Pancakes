package org.pancakelab.exception;

import org.pancakelab.model.Order;

import java.util.UUID;

public class IncorrectOrderStatusException extends OrderUpdateException {
  public IncorrectOrderStatusException(UUID orderId, Order.Status expected, Order.Status actual) {
    super("Order " + orderId + " was expected to be in status: " + expected + " but was in status: " + actual);
  }
}
