package group.rohlik.order.model.mapper;

import group.rohlik.order.model.api.OrderedProduct;
import group.rohlik.order.model.entity.OrderedProductEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderedProductMapper implements Mapper<OrderedProduct, OrderedProductEntity> {

    @Override
    public OrderedProduct toApi(OrderedProductEntity source) {
        if (source == null) {
            return null;
        }

        var target = new OrderedProduct();
        target.setId(source.getId());
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());

        return target;
    }

    @Override
    public OrderedProductEntity toEntity(OrderedProduct source) {
        if (source == null) {
            return null;
        }

        var target = new OrderedProductEntity();
        target.setId(source.getId());
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());

        return target;
    }
}
