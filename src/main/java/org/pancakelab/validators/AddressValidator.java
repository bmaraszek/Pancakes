package org.pancakelab.validators;

import org.pancakelab.model.Address;

public class AddressValidator implements Validator<Address> {
  @Override
  public void validate(Address in) {
    validateBuilding(in.buildingNumber());
    validateRoom(in.roomNumber());
  }

  private void validateBuilding(int buildingNumber) {
    if(buildingNumber < 1 || buildingNumber > 100) {
      throw new IllegalArgumentException("Building number must be between 0 and 100");
    }
  }

  private void validateRoom(int roomNumber) {
    if(roomNumber < 1 || roomNumber > 100) {
      throw new IllegalArgumentException("Room number must be between 0 and 100");
    }
  }
}
