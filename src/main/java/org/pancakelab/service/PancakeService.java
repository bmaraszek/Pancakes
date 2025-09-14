package org.pancakelab.service;

import org.pancakelab.exception.IncorrectOrderStatusException;
import org.pancakelab.exception.OrderUpdateException;
import org.pancakelab.exception.OrderNotFoundException;
import org.pancakelab.exception.PancakeNotFoundException;
import org.pancakelab.model.Address;
import org.pancakelab.model.Delivery;
import org.pancakelab.model.Order;
import org.pancakelab.model.Pancake;
import org.pancakelab.validators.AddressValidator;
import org.pancakelab.validators.PancakeValidator;
import org.pancakelab.repository.OrderRepository;
import org.pancakelab.repository.PancakeRepository;
import org.pancakelab.util.EntityLockManager;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PancakeService {

  private final AddressValidator addressValidator;
  private final PancakeValidator pancakeValidator;
  private final OrderRepository orderRepository;
  private final PancakeRepository pancakeRepository;
  private final OrderLog orderLog;

  private final EntityLockManager<UUID> orderLockManager = new EntityLockManager<>();
  private final EntityLockManager<UUID> pancakeLockManager = new EntityLockManager<>();


  public PancakeService(AddressValidator addressValidator,
                        PancakeValidator pancakeValidator,
                        OrderRepository orderRepository,
                        PancakeRepository pancakeRepository,
                        OrderLog orderLog) {
    this.addressValidator = addressValidator;
    this.pancakeValidator = pancakeValidator;
    this.orderRepository = orderRepository;
    this.pancakeRepository = pancakeRepository;
    this.orderLog = orderLog;
  }

  public UUID startOrder(Address address) {
    addressValidator.validate(address);
    Order order = new Order(address);
    orderRepository.save(order);
    return order.getId();
  }

  public UUID addPancake(UUID orderId) throws OrderNotFoundException {
    return orderLockManager.withLock(orderId, id -> {
      Order order = orderRepository.find(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
      Collection<Pancake> pancakesInOrder = pancakeRepository.findByOrderId(orderId);
      Pancake pancake = new Pancake(order.getId());
      orderLog.logAddPancake(order, pancake.getDescription(), pancakesInOrder);
      pancakeRepository.save(pancake);
      return pancake.getId();
    });
  }

  public UUID removePancake(UUID orderId, UUID pancakeId) throws OrderUpdateException {
    return orderLockManager.withLock(orderId, id -> {
      Order order = orderRepository.find(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
      Pancake pancake = pancakeRepository.find(pancakeId).orElseThrow(() -> new PancakeNotFoundException(pancakeId));
      if (!pancake.getOrderId().equals(order.getId())) {
        throw new IllegalArgumentException("Pancake with id " + pancakeId + " does not belong to order " + orderId);
      }
      Collection<Pancake> pancakesInOrder = pancakeRepository.findByOrderId(orderId);
      orderLog.logRemovePancakes(order, pancake.getDescription(), pancakesInOrder);
      return deletePancake(pancakeId);
    });
  }

  public UUID addIngredient(UUID pancakeId, Pancake.Ingredient ingredient) throws PancakeNotFoundException {
    return pancakeLockManager.withLock(pancakeId, id -> {
      Pancake pancake = pancakeRepository.find(id).orElseThrow(() -> new PancakeNotFoundException(pancakeId));
      pancake.addIngredient(ingredient);
      pancakeValidator.validate(pancake);
      return pancakeRepository.save(pancake).getId();
    });
  }

  public List<String> viewOrder(UUID orderId) throws OrderNotFoundException {
    orderRepository.find(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    Collection<Pancake> pancakes = pancakeRepository.findByOrderId(orderId);
    return pancakes.stream()
        .map(Pancake::getDescription)
        .toList();
  }

  public UUID cancelOrder(UUID orderId) throws OrderUpdateException {
    return orderLockManager.withLock(orderId, id -> {
      Order order = orderRepository.find(id).orElseThrow(() -> new OrderNotFoundException(orderId));
      orderLog.logCancelOrder(order, pancakeRepository.findByOrderId(orderId));

      pancakeRepository.findByOrderId(id).forEach(p -> deletePancake(p.getId()));
      return deleteOrder(id);
    });
  }

  public UUID completeOrder(UUID orderId) throws OrderUpdateException {
    return orderLockManager.withLock(orderId, id -> {
      Order order = orderRepository.find(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
      if (!order.getStatus().equals(Order.Status.Pending)) {
        throw new IncorrectOrderStatusException(orderId, Order.Status.Pending, order.getStatus());
      }
      order.setOrderStatus(Order.Status.Completed);
      return orderRepository.save(order).getId();
    });
  }

  public List<UUID> listCompletedOrders() {
    return orderRepository.findCompleted();
  }

  public UUID prepareOrder(UUID orderId) throws OrderUpdateException {
    return orderLockManager.withLock(orderId, id -> {
      Order order = orderRepository.find(id).orElseThrow(() -> new OrderNotFoundException(orderId));
      if (!order.getStatus().equals(Order.Status.Completed)) {
        throw new IncorrectOrderStatusException(orderId, Order.Status.Completed, order.getStatus());
      }
      order.setOrderStatus(Order.Status.Prepared);
      return orderRepository.save(order).getId();
    });
  }

  public List<UUID> listPreparedOrders() {
    return orderRepository.findPrepared();
  }

  public Delivery deliverOrder(UUID orderId) throws OrderUpdateException {
    return orderLockManager.withLock(orderId, id -> {
      Order order = orderRepository.find(id).orElseThrow(() -> new OrderNotFoundException(orderId));
      if (!order.getStatus().equals(Order.Status.Prepared)) {
        throw new IncorrectOrderStatusException(id, Order.Status.Prepared, order.getStatus());
      }

      orderLog.logDeliverOrder(order, pancakeRepository.findByOrderId(id));
      List<String> viewOrder = viewOrder(orderId);

      pancakeRepository.findByOrderId(id).forEach(p -> deletePancake(p.getId()));
      deleteOrder(id);

      return new Delivery(order, viewOrder);
    });
  }

  private UUID deleteOrder(UUID orderId) {
    orderRepository.remove(orderId);
    orderLockManager.removeLock(orderId);
    return orderId;
  }

  private UUID deletePancake(UUID pancakeId) {
    pancakeRepository.remove(pancakeId);
    pancakeLockManager.removeLock(pancakeId);
    return pancakeId;
  }
}
