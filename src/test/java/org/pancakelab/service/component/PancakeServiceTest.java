package org.pancakelab.service.component;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.exception.OrderUpdateException;
import org.pancakelab.exception.PancakeNotFoundException;
import org.pancakelab.model.Address;
import org.pancakelab.model.Delivery;
import org.pancakelab.model.Order;
import org.pancakelab.model.Pancake;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.repository.PancakeRepository;
import org.pancakelab.service.OrderLog;
import org.pancakelab.service.PancakeService;
import org.pancakelab.validators.AddressValidator;
import org.pancakelab.validators.PancakeValidator;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PancakeServiceTest {

  private final OrderRepository orderRepository = new OrderRepository();
  private final PancakeRepository pancakeRepository = new PancakeRepository();

  private final PancakeService pancakeService = new PancakeService(
      new AddressValidator(),
      new PancakeValidator(),
      orderRepository,
      pancakeRepository,
      new OrderLog()
  );

  private UUID orderId;
  private UUID pancake1Id;
  private UUID pancake2Id;

  @Test
  @org.junit.jupiter.api.Order(1)
  void newOrder() {
    orderId = pancakeService.startOrder(new Address(1, 1));

    List<Order> orders = orderRepository.findAll();

    assertThat(orders)
        .hasSize(1)
        .extracting(Order::getStatus, Order::getAddress)
        .containsExactly(tuple(Order.Status.Pending, new Address(1, 1)));

    assertThat(pancakeRepository.findAll())
        .hasSize(0);
  }

  @Test
  @org.junit.jupiter.api.Order(2)
  void addPancakes() throws OrderNotFoundException, PancakeNotFoundException {
    pancake1Id = pancakeService.addPancake(orderId);
    pancake2Id = pancakeService.addPancake(orderId);

    pancakeService.addIngredient(pancake1Id, Pancake.Ingredient.MilkChocolate);
    pancakeService.addIngredient(pancake1Id, Pancake.Ingredient.Hazelnuts);
    pancakeService.addIngredient(pancake1Id, Pancake.Ingredient.WhippedCream);

    pancakeService.addIngredient(pancake2Id, Pancake.Ingredient.DarkChocolate);
    pancakeService.addIngredient(pancake2Id, Pancake.Ingredient.WhippedCream);

    assertThat(pancakeRepository.findAll())
        .hasSize(2)
        .map(Pancake::getDescription)
        .containsExactlyInAnyOrder(
            "Pancake with 2 ingredients: dark chocolate, whipped cream",
            "Pancake with 3 ingredients: milk chocolate, hazelnuts, whipped cream"
        );
  }

  @Test
  @org.junit.jupiter.api.Order(3)
  void completeOrder() throws OrderUpdateException {
    pancakeService.completeOrder(orderId);

    assertThat(orderRepository.findAll())
        .hasSize(1)
        .extracting(Order::getStatus)
        .containsExactly(Order.Status.Completed);
  }

  @Test
  @org.junit.jupiter.api.Order(4)
  void prepareOrder() throws OrderUpdateException {
    pancakeService.prepareOrder(orderId);

    assertThat(orderRepository.findAll())
        .hasSize(1)
        .extracting(Order::getStatus)
        .containsExactly(Order.Status.Prepared);
  }

  @Test
  @org.junit.jupiter.api.Order(5)
  void deliverOrder() throws OrderUpdateException {
    Delivery delivery = pancakeService.deliverOrder(orderId);

    assertThat(orderRepository.findAll())
        .hasSize(0);

    assertThat(pancakeRepository.findAll())
        .hasSize(0);

    assertThat(delivery.getOrder().getStatus())
        .isEqualTo(Order.Status.Prepared);

    assertThat(delivery.getOrder().getAddress())
        .isEqualTo(new Address(1, 1));

    assertThat(delivery.getPancakes())
        .hasSize(2)
        .containsExactlyInAnyOrder(
            "Pancake with 2 ingredients: dark chocolate, whipped cream",
            "Pancake with 3 ingredients: milk chocolate, hazelnuts, whipped cream"
        );
  }
}
