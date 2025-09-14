package org.pancakelab.model;

import java.util.Objects;
import java.util.UUID;

public class Order {
    private final UUID id;
    private final Address address;
    private Status status;

    public Order(Address address) {
        this.id = UUID.randomUUID();
        this.address = address;
        this.status = Status.Pending;
    }

    public UUID getId() {
        return id;
    }

    public Address getAddress() {
      return address;
    }

    public Status getStatus() {
      return status;
    }

    public void setOrderStatus(Status status) {
      this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public enum Status {
      Pending, Completed, Prepared
    }
}
