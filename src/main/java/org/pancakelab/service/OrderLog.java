package org.pancakelab.service;

import org.pancakelab.model.Order;
import org.pancakelab.model.Pancake;

import java.util.Collection;

public class OrderLog {
    private static final StringBuilder log = new StringBuilder();

    public void logAddPancake(Order order, String description, Collection<Pancake> pancakes) {
        long pancakesInOrder = pancakes.stream().filter(p -> p.getOrderId().equals(order.getId())).count();

        log.append("Added pancake with description '%s' ".formatted(description))
           .append("to order %s containing %d pancakes, ".formatted(order.getId(), pancakesInOrder))
           .append("for building %d, room %d.".formatted(order.getAddress().getBuildingNumber(), order.getAddress().getRoomNumber()));
    }

    public void logRemovePancakes(Order order, String description, Collection<Pancake> pancakes) {
        long pancakesInOrder = pancakes.stream().filter(p -> p.getOrderId().equals(order.getId())).count();

        log.append("Removed pancake with description '%s' ".formatted(description))
           .append("from order %s now containing %d pancakes, ".formatted(order.getId(), pancakesInOrder))
           .append("for building %d, room %d.".formatted(order.getAddress().getBuildingNumber(), order.getAddress().getRoomNumber()));
    }

    public void logCancelOrder(Order order, Collection<Pancake> pancakes) {
        long pancakesInOrder = pancakes.stream().filter(p -> p.getOrderId().equals(order.getId())).count();
        log.append("Cancelled order %s with %d pancakes ".formatted(order.getId(), pancakesInOrder))
           .append("for building %d, room %d.".formatted(order.getAddress().getBuildingNumber(), order.getAddress().getRoomNumber()));
    }

    public void logDeliverOrder(Order order, Collection<Pancake> pancakes) {
        long pancakesInOrder = pancakes.stream().filter(p -> p.getOrderId().equals(order.getId())).count();
        log.append("Order %s with %d pancakes ".formatted(order.getId(), pancakesInOrder))
           .append("for building %d, room %d out for delivery.".formatted(order.getAddress().getBuildingNumber(), order.getAddress().getRoomNumber()));
    }
}
