package org.pancakelab.util;

@FunctionalInterface
public interface ThrowingFunction<K, R, E extends Exception> {
  R apply(K key) throws E;
}
