package group.rohlik.order.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Order {

    private String id;

    private List<OrderedProduct> orderedProducts;

    private Double totalPrice;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;
}
