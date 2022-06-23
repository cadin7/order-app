package group.rohlik.order.model.entity;

import group.rohlik.order.model.api.OrderStatus;
import group.rohlik.order.model.api.PaymentStatus;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@EqualsAndHashCode(of = "id")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = AUTO, generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    private String id;

    @OneToMany(cascade = ALL)
    private List<OrderedProductEntity> orderedProductEntities;

    private Double totalPrice;

    @Enumerated(STRING)
    private OrderStatus orderStatus;

    @Enumerated(STRING)
    private PaymentStatus paymentStatus;
}
