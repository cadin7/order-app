package group.rohlik.order.event;

public record ProductEvent(
        String productId,
        Integer quantity
) {
}
