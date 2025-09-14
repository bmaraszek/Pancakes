package org.pancakelab.model;

import java.util.Objects;

public class Address {

  private final int buildingNumber;
  private final int roomNumber;

  public Address(int buildingNumber, int roomNumber) {
    this.buildingNumber = buildingNumber;
    this.roomNumber = roomNumber;
  }

  public int getBuildingNumber() {
    return buildingNumber;
  }

  public int getRoomNumber() {
    return roomNumber;
  }

  @Override
  public int hashCode() {
    return Objects.hash(buildingNumber, roomNumber);
  }

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Address)) {
      return false;
    }
    if(this == obj) {
      return true;
    }
    return Objects.equals(this.buildingNumber, ((Address) obj).buildingNumber) && Objects.equals(this.roomNumber, ((Address) obj).roomNumber);
  }
}
