package org.pancakelab.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Pancake {

  private final UUID id;
  private final UUID orderId;
  private final List<Ingredient> ingredients = new ArrayList<>();

  public Pancake(UUID orderId) {
    this.id = UUID.randomUUID();
    this.orderId = orderId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public Pancake addIngredient(Ingredient in) {
    ingredients.add(in);
    return this;
  }

  public List<Ingredient> getIngredients() {
    return Collections.unmodifiableList(ingredients);
  }

  public String getDescription() {
    if(ingredients.isEmpty()) {
      return "plain pancake";
    }
    return String.format("Pancake with %d ingredients: %s",
        ingredients.size(),
        ingredients.stream().map(i -> i.getDescription()).collect(Collectors.joining(", ")));
  }

  public enum Ingredient {
    MilkChocolate("milk chocolate"),
    DarkChocolate("dark chocolate"),
    Hazelnuts("hazelnuts"),
    WhippedCream("whipped cream");

    private final String description;

    Ingredient(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}
