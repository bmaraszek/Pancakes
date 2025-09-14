package org.pancakelab.repository;

import org.pancakelab.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OrderRepository implements Repository<UUID, Order> {

  private final ConcurrentHashMap<UUID, Order> database = new ConcurrentHashMap<>();

  @Override
  public Order save(Order order) {
    return database.put(order.getId(), order);
  }

  @Override
  public Optional<Order> find(UUID itemId) {
    return Optional.ofNullable(database.get(itemId));
  }

  public List<UUID> findCompleted() {
    return findAll().stream()
        .filter(o -> o.getStatus().equals(Order.Status.Completed))
        .map(Order::getId)
        .toList();
  }

  public List<UUID> findPrepared() {
    return findAll().stream()
        .filter(o -> o.getStatus().equals(Order.Status.Prepared))
        .map(Order::getId)
        .toList();
  }

  @Override
  public List<Order> findAll() {
    return database.values().stream().toList();
  }

  @Override
  public Order remove(UUID itemId) {
    return database.remove(itemId);
  }

}
