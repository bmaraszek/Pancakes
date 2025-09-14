package org.pancakelab.service.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pancakelab.model.Pancake;
import org.pancakelab.validators.PancakeValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PancakeValidatorTest {

  @Mock
  private Pancake pancake;
  @Mock
  private List<Pancake.Ingredient> ingredients;
  private PancakeValidator pancakeValidator = new PancakeValidator();

  @BeforeEach
  void setUp() {
    when(pancake.getIngredients()).thenReturn(ingredients);
  }

  @Test
  void pancakeWithNoIngredientsIsAllowed() {
    when(ingredients.size()).thenReturn(0);
    assertDoesNotThrow(() -> pancakeValidator.validate(pancake));
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4, 5})
  void pancakeWithValidIngredientsIsAllowed(int numberOfIngredients) {
    when(ingredients.size()).thenReturn(numberOfIngredients);
    assertDoesNotThrow(() -> pancakeValidator.validate(pancake));
  }

  @Test
  void pancakeIngredientLimitExceeded() {
    when(ingredients.size()).thenReturn(6);
    assertThrows(IllegalStateException.class, () -> pancakeValidator.validate(pancake));
  }
}
