package org.pancakelab.validators;

public interface Validator<T> {
  void validate(T in);
}
