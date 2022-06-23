package group.rohlik.order.service;

import group.rohlik.order.event.ProductEvent;
import group.rohlik.order.exceptions.OrderNotFoundException;
import group.rohlik.order.exceptions.ProductNotFoundException;
import group.rohlik.order.model.api.MissingProduct;
import group.rohlik.order.model.api.OrderStatus;
import group.rohlik.order.model.api.PaymentStatus;
import group.rohlik.order.model.entity.OrderEntity;
import group.rohlik.order.model.entity.OrderedProductEntity;
import group.rohlik.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static group.rohlik.order.model.api.PaymentStatus.CANCELLED;
import static group.rohlik.order.model.api.PaymentStatus.PENDING;
import static java.lang.Math.negateExact;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;
import static org.springframework.http.HttpStatus.OK;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_NOT_FOUND = "Order with ID: %s was not found!";
    private static final String PRODUCT_NOT_FOUND = "Product with ID: %s was not found!";

    private final OrderRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final DirectExchange directExchange;
    private final ProductApiClient productApiClient;

    public ResponseEntity<Object> createOrder(OrderEntity order) {
        final var missingProducts = order.getOrderedProductEntities()
                .stream()
                .map(this::getMissingProducts)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        return missingProducts.isEmpty() ?
                getSavedOrder(order) :
                new ResponseEntity<>(missingProducts, EXPECTATION_FAILED);
    }

    public OrderEntity deleteOrder(String orderId) {
        var orderToDelete = repository.findById(orderId);

        orderToDelete.ifPresent(this::deleteExistingOrder);

        return orderToDelete.orElseThrow(
                () -> new OrderNotFoundException(format(ORDER_NOT_FOUND, orderId)));
    }

    public OrderEntity updatePayment(String orderId, PaymentStatus paymentStatus) {
        var orderToUpdate = getOrderByIdOrThrow(orderId);

        orderToUpdate.setPaymentStatus(paymentStatus);

        if (paymentStatus == CANCELLED) {
            orderToUpdate.setOrderStatus(OrderStatus.CANCELLED);
            notifyProductUpdate(orderToUpdate, false);
        }

        return repository.save(orderToUpdate);
    }

    private Optional<MissingProduct> getMissingProducts(OrderedProductEntity orderedProductEntity) {
        final var product = productApiClient.getOrder(orderedProductEntity.getProductId())
                .orElseThrow(() -> new RuntimeException(
                        format(PRODUCT_NOT_FOUND, orderedProductEntity.getProductId())));

        final var quantity = orderedProductEntity.getQuantity();

        if (product.getStockQuantity() < quantity) {
            return of(
                    new MissingProduct(
                            product.getName(),
                            quantity - product.getStockQuantity()));
        }
        return empty();
    }

    private ResponseEntity<Object> getSavedOrder(OrderEntity orderEntity) {
        orderEntity.setTotalPrice(sumTotalPrice(orderEntity));
        orderEntity.setOrderStatus(OrderStatus.PLACED);
        orderEntity.setPaymentStatus(PENDING);

        uncompletedOrderScheduler(orderEntity);

        notifyProductUpdate(orderEntity, true);

        return new ResponseEntity<>(repository.save(orderEntity), OK);
    }

    private void uncompletedOrderScheduler(OrderEntity order) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.schedule(() -> verifyOrderPayment(order.getId()), 30, TimeUnit.MINUTES);
    }

    private void verifyOrderPayment(String orderId) {
        final var orderEntity = getOrderByIdOrThrow(orderId);

        if (orderEntity.getPaymentStatus() == PENDING) {
            orderEntity.setOrderStatus(OrderStatus.INVALID);
            orderEntity.setPaymentStatus(CANCELLED);

            repository.save(orderEntity);

            //TODO: notify isn't working here...
            notifyProductUpdate(orderEntity, false);
        }
    }

    private void notifyProductUpdate(OrderEntity order, boolean isNewOrder) {
        order.getOrderedProductEntities().forEach(product ->
                rabbitTemplate.convertAndSend(
                        directExchange.getName(),
                        "quantity.update",
                        new ProductEvent(product.getProductId(), getQuantity(product, isNewOrder))));
    }

    private Integer getQuantity(OrderedProductEntity product, boolean isNewOrder) {
        return isNewOrder ? product.getQuantity() : negateExact(product.getQuantity());
    }

    private double sumTotalPrice(OrderEntity orderEntity) {
        return orderEntity.getOrderedProductEntities()
                .stream()
                .mapToDouble(this::getTotalPricePerProduct)
                .sum();
    }

    private double getTotalPricePerProduct(OrderedProductEntity orderedProductEntity) {
        final var product = productApiClient.getOrder(orderedProductEntity.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(
                        format(PRODUCT_NOT_FOUND, orderedProductEntity.getProductId())));

        return product.getUnitPrice() * orderedProductEntity.getQuantity();
    }

    private void deleteExistingOrder(OrderEntity orderEntity) {
        notifyProductUpdate(orderEntity, false);

        repository.delete(orderEntity);
    }

    private OrderEntity getOrderByIdOrThrow(String orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(format(ORDER_NOT_FOUND, orderId)));
    }
}
