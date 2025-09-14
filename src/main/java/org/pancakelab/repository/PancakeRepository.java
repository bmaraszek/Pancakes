package org.pancakelab.repository;

import org.pancakelab.model.Pancake;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PancakeRepository implements Repository<UUID, Pancake> {

  private final ConcurrentHashMap<UUID, Pancake> database = new ConcurrentHashMap<>();

  @Override
  public Pancake save(Pancake item) {
    return database.put(item.getId(), item);
  }

  @Override
  public Optional<Pancake> find(UUID itemId) {
    return Optional.ofNullable(database.get(itemId));
  }

  public Collection<Pancake> findByOrderId(UUID orderId) {
    return findAll().stream()
        .filter(p -> p.getOrderId().equals(orderId))
        .toList();
  }

  @Override
  public List<Pancake> findAll() {
    return database.values().stream().toList();
  }

  @Override
  public Pancake remove(UUID itemId) {
    return database.remove(itemId);
  }

  @Override
  public void removeAll(Collection<Pancake> items) {
    items.forEach(i -> remove(i.getId()));
  }
}
