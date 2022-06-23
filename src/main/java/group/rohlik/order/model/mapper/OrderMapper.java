package group.rohlik.order.model.mapper;

import group.rohlik.order.model.api.Order;
import group.rohlik.order.model.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMapper implements Mapper<Order, OrderEntity> {

    private final OrderedProductMapper orderedProductMapper;

    @Override
    public Order toApi(OrderEntity source) {
        if (source == null) {
            return null;
        }

        var target = new Order();
        target.setId(source.getId());
        target.setOrderedProducts(
                orderedProductMapper.toApi(
                        source.getOrderedProductEntities()));
        target.setTotalPrice(source.getTotalPrice());
        target.setOrderStatus(source.getOrderStatus());
        target.setPaymentStatus(source.getPaymentStatus());

        return target;
    }

    @Override
    public OrderEntity toEntity(Order source) {
        if (source == null) {
            return null;
        }

        var target = new OrderEntity();
        target.setId(source.getId());
        target.setOrderedProductEntities(
                orderedProductMapper.toEntity(
                        source.getOrderedProducts()));
        target.setTotalPrice(source.getTotalPrice());
        target.setOrderStatus(source.getOrderStatus());
        target.setPaymentStatus(source.getPaymentStatus());

        return target;
    }
}