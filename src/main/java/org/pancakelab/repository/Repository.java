package org.pancakelab.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<K, V> {
  V save(V item);
  Optional<V> find(K itemId);
  List<V> findAll();
  V remove(K itemId);
}
