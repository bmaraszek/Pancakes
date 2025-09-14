package org.pancakelab.service.validators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pancakelab.model.Address;
import org.pancakelab.validators.AddressValidator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressValidatorTest {

  @Mock
  private Address address;
  private AddressValidator validator = new AddressValidator();

  @ParameterizedTest
  @CsvSource({"1", "10", "100"})
  public void validateCorrectBuildingNumber(int buildingNumber) {
    when(address.buildingNumber()).thenReturn(buildingNumber);
    when(address.roomNumber()).thenReturn(1);
    Assertions.assertDoesNotThrow(() -> validator.validate(address));
  }

  @ParameterizedTest
  @CsvSource({"-1", "0", "101"})
  public void validateIncorrectBuildingNumber(int buildingNumber) {
    when(address.buildingNumber()).thenReturn(buildingNumber);
    assertThrows(IllegalArgumentException.class, () -> validator.validate(address));
  }

  @ParameterizedTest
  @CsvSource({"1", "10", "100"})
  public void validateCorrectRoomNumber(int roomNumber) {
    when(address.roomNumber()).thenReturn(roomNumber);
    when(address.buildingNumber()).thenReturn(1);
    assertDoesNotThrow(() -> validator.validate(address));
  }

  @ParameterizedTest
  @CsvSource({"-1", "0", "101"})
  public void validateIncorrectRoomNumber(int roomNumber) {
    when(address.roomNumber()).thenReturn(roomNumber);
    when(address.buildingNumber()).thenReturn(1);
    assertThrows(IllegalArgumentException.class, () -> validator.validate(address));
  }
}
