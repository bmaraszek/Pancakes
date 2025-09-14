package org.pancakelab.util;

import org.pancakelab.exception.OrderUpdateException;

import java.util.concurrent.ConcurrentHashMap;

public class EntityLockManager<K> {
  private final ConcurrentHashMap<K, Object> locks = new ConcurrentHashMap<>();

  private Object getLock(K key) {
    return locks.computeIfAbsent(key, k -> new Object());
  }

  public <T, E extends OrderUpdateException> T withLock(K key, ThrowingFunction<K, T, E> action) throws E {
    synchronized (getLock(key)) {
      return action.apply(key);
    }
  }

  public void removeLock(K key) {
    locks.remove(key);
  }
}
