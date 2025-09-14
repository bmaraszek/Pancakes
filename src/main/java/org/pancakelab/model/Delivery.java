package org.pancakelab.model;

import java.util.Collections;
import java.util.List;

public class Delivery {
  private Order order;
  private List<String> pancakes;

  public Delivery(Order order, List<String> pancakes) {
    this.order = order;
    this.pancakes = pancakes;
  }

  public Order getOrder() {
    return order;
  }

  public List<String> getPancakes() {
    return Collections.unmodifiableList(pancakes);
  }
}
