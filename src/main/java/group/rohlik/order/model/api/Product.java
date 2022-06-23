package group.rohlik.order.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Product {

    private String id;

    private String name;

    private Integer stockQuantity;

    private Double unitPrice;
}
