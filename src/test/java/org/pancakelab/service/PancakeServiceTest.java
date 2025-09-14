package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pancakelab.exception.IncorrectOrderStatusException;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.exception.OrderUpdateException;
import org.pancakelab.exception.PancakeNotFoundException;
import org.pancakelab.model.Address;
import org.pancakelab.model.Delivery;
import org.pancakelab.model.Order;
import org.pancakelab.model.Pancake;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.repository.PancakeRepository;
import org.pancakelab.validators.AddressValidator;
import org.pancakelab.validators.PancakeValidator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PancakeServiceTest {

  @Mock
  private Address address;
  @Mock
  private AddressValidator addressValidator;
  @Mock
  private PancakeValidator pancakeValidator;
  @Mock
  private OrderRepository orderRepository;
  @Mock
  private PancakeRepository pancakeRepository;
  @Mock
  private Order order;
  @Mock
  private Pancake pancake;
  @Mock
  private UUID orderId;
  @Mock
  private UUID pancakeId;
  @Mock
  private OrderLog orderLog;
  @InjectMocks
  private PancakeService pancakeService;

  @Test
  void startOrderWithValidAddress() {
    doNothing().when(addressValidator).validate(any());
    when(orderRepository.save(any())).thenReturn(order);
    UUID uuid = pancakeService.startOrder(address);

    assertThat(uuid).isNotNull();
  }

  @Test
  void startOrderWithInvalidAddress() {
    doThrow(new IllegalArgumentException()).when(addressValidator).validate(any());

    assertThatThrownBy(() -> pancakeService.startOrder(address))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void addPancakeWhenOrderNotFound() {
    when(orderRepository.find(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pancakeService.addPancake(orderId))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  void addPancakeToValidOrder() throws OrderNotFoundException {
    when(orderRepository.find(any())).thenReturn(Optional.of(order));
    when(pancakeRepository.findByOrderId(orderId)).thenReturn(List.of(pancake));
    when(pancakeRepository.save(any())).thenReturn(pancake);
    UUID pancakeId = pancakeService.addPancake(orderId);

    assertThat(pancakeId).isNotNull();
    verify(orderLog, times(1)).logAddPancake(order, "plain pancake", List.of(pancake));
    verify(pancakeRepository, times(1)).save(any());
  }

  @Test
  void removePancakeWhenOrderNotFound() {
    when(orderRepository.find(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pancakeService.removePancake(orderId, pancakeId))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  void removePancakeFromWrongOrder() {
    when(orderRepository.find(orderId)).thenReturn(Optional.of(order));
    when(pancakeRepository.find(pancakeId)).thenReturn(Optional.of(pancake));
    when(pancake.getOrderId()).thenReturn(mock(UUID.class));
    when(order.getId()).thenReturn(orderId);

    assertThatThrownBy(() -> pancakeService.removePancake(orderId, pancakeId))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void removePancakeFromValidOrder() throws OrderUpdateException {
    when(orderRepository.find(orderId)).thenReturn(Optional.of(order));
    when(pancakeRepository.find(pancakeId)).thenReturn(Optional.of(pancake));
    when(pancakeRepository.findByOrderId(orderId)).thenReturn(List.of(pancake));
    when(pancakeRepository.remove(pancakeId)).thenReturn(pancake);
    when(pancake.getOrderId()).thenReturn(orderId);
    when(pancake.getDescription()).thenReturn("description");
    when(order.getId()).thenReturn(orderId);

    UUID result = pancakeService.removePancake(orderId, pancakeId);
    assertThat(result).isEqualTo(pancakeId);
    verify(pancakeRepository, times(1)).remove(pancakeId);
    verify(orderLog, times(1)).logRemovePancakes(order, "description", List.of(pancake));
  }

  @Test
  void addIngredientWhenPancakeNotFound() {
    when(pancakeRepository.find(pancakeId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pancakeService.addIngredient(pancakeId, Pancake.Ingredient.DarkChocolate))
        .isInstanceOf(PancakeNotFoundException.class);
  }

  @Test
  void addIngredientToValidPancake() throws PancakeNotFoundException {
    when(pancakeRepository.find(pancakeId)).thenReturn(Optional.of(pancake));
    when(pancakeRepository.save(pancake)).thenReturn(pancake);
    when(pancake.getId()).thenReturn(pancakeId);
    UUID saved = pancakeService.addIngredient(pancakeId, Pancake.Ingredient.DarkChocolate);

    assertThat(saved).isEqualTo(pancakeId);
  }

  @Test
  void pancakeWithTooManyIngredients() throws PancakeNotFoundException {
    when(pancakeRepository.find(pancakeId)).thenReturn(Optional.of(pancake));
    doThrow(IllegalStateException.class).when(pancakeValidator).validate(pancake);

    assertThatThrownBy(() -> pancakeService.addIngredient(pancakeId, Pancake.Ingredient.DarkChocolate))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void cancelOrderWhenOrderNotFound() {
    when(orderRepository.find(orderId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pancakeService.cancelOrder(orderId))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @Test
  void cancelOrder() throws OrderUpdateException {
    when(orderRepository.find(orderId)).thenReturn(Optional.of(order));
    when(pancakeRepository.findByOrderId(orderId)).thenReturn(List.of(pancake));
    when(orderRepository.remove(orderId)).thenReturn(order);
    when(pancake.getId()).thenReturn(pancakeId);

    assertThat(pancakeService.cancelOrder(orderId)).isEqualTo(orderId);
    verify(orderRepository, times(1)).remove(orderId);
    verify(pancakeRepository, times(1)).remove(pancakeId);
    verify(orderLog, times(1)).logCancelOrder(order, List.of(pancake));
  }

  @Test
  void completeOrder() throws OrderUpdateException {
    when(orderRepository.find(orderId)).thenReturn(Optional.of(order));
    when(order.getStatus()).thenReturn(Order.Status.Pending);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getId()).thenReturn(orderId);

    UUID result = pancakeService.completeOrder(orderId);

    assertThat((result)).isEqualTo(orderId);
    verify(order, times(1)).setOrderStatus(Order.Status.Completed);
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  void completeOrderWhenOrderNotFound() {
    when(orderRepository.find(orderId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pancakeService.completeOrder(orderId))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @ParameterizedTest
  @EnumSource(
      value = Order.Status.class,
      names = {"Pending"},
      mode = EnumSource.Mode.EXCLUDE
  )
  void completeOrderWhenWrongStatus(Order.Status status) {
    when(orderRepository.find(orderId)).thenReturn(Optional.of(order));
    when(order.getStatus()).thenReturn(status);

    assertThatThrownBy(() -> pancakeService.completeOrder(orderId))
        .isInstanceOf(IncorrectOrderStatusException.class);
  }

  @Test
  void prepareOrderWhenOrderNotFound() {
    when(orderRepository.find(orderId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pancakeService.prepareOrder(orderId))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @ParameterizedTest
  @EnumSource(
      value = Order.Status.class,
      names = {"Completed"},
      mode = EnumSource.Mode.EXCLUDE
  )
  void prepareOrderWhenWrongStatus(Order.Status status) {
    when(orderRepository.find(orderId)).thenReturn(Optional.of(order));
    when(order.getStatus()).thenReturn(status);

    assertThatThrownBy(() -> pancakeService.prepareOrder(orderId))
        .isInstanceOf(IncorrectOrderStatusException.class);
  }

  @Test
  void prepareOrder() throws OrderUpdateException {
    when(orderRepository.find(orderId)).thenReturn(Optional.of(order));
    when(order.getStatus()).thenReturn(Order.Status.Completed);
    when(orderRepository.save(order)).thenReturn(order);
    when(order.getId()).thenReturn(orderId);

    UUID result = pancakeService.prepareOrder(orderId);

    assertThat(result).isEqualTo(orderId);
    verify(order, times(1)).setOrderStatus(Order.Status.Prepared);
    verify(orderRepository, times(1)).save(order);
  }

  @Test
  void listPreparedOrders() {
    List<UUID> orders = List.of(orderId);
    when(orderRepository.findPrepared()).thenReturn(orders);

    assertThat(pancakeService.listPreparedOrders()).isEqualTo(orders);
  }

  @Test
  void listCompletedOrders() {
    when(orderRepository.findCompleted()).thenReturn(List.of(orderId));
    assertThat(pancakeService.listCompletedOrders()).containsExactly(orderId);
  }

  @Test
  void deliverOrderWhenOrderNotFound() {
    when(orderRepository.find(orderId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pancakeService.deliverOrder(orderId))
        .isInstanceOf(OrderNotFoundException.class);
  }

  @ParameterizedTest
  @EnumSource(
      value = Order.Status.class,
      names = {"Prepared"},
      mode = EnumSource.Mode.EXCLUDE
  )
  void deliverOrderWhenWrongStatus(Order.Status status) {
    when(orderRepository.find(orderId)).thenReturn(Optional.of(order));
    when(order.getStatus()).thenReturn(status);

    assertThatThrownBy(() -> pancakeService.deliverOrder(orderId))
        .isInstanceOf(IncorrectOrderStatusException.class);
  }

  @Test
  void deliverOrder() throws OrderUpdateException {
    when(orderRepository.find(orderId)).thenReturn(Optional.of(order));
    when(order.getStatus()).thenReturn(Order.Status.Prepared);
    when(pancakeRepository.findByOrderId(orderId)).thenReturn(List.of(pancake));
    when(pancake.getDescription()).thenReturn("test description");
    when(pancake.getId()).thenReturn(pancakeId);

    Delivery result = pancakeService.deliverOrder(orderId);
    assertThat(result.getOrder()).isEqualTo(order);
    assertThat(result.getPancakes()).isEqualTo(List.of("test description"));
  }
}
