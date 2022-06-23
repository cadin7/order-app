package group.rohlik.order.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.AUTO;

@Data
@EqualsAndHashCode(of = "id")
public class OrderedProduct {

    @Id
    @GeneratedValue(strategy = AUTO, generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    private String id;

    private String productId;

    private Integer quantity;
}
