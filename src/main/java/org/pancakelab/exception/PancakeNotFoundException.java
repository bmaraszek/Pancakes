package org.pancakelab.exception;

import java.util.UUID;

public class PancakeNotFoundException extends OrderUpdateException {
  public PancakeNotFoundException(UUID pancakeId) {
    super("Pancake with id: " + pancakeId + " not found");
  }
}
