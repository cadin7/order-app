package group.rohlik.order.service;

import group.rohlik.order.exceptions.OrderNotFoundException;
import group.rohlik.order.model.api.MissingProduct;
import group.rohlik.order.model.api.Product;
import group.rohlik.order.model.entity.OrderEntity;
import group.rohlik.order.model.entity.OrderedProductEntity;
import group.rohlik.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static group.rohlik.order.model.api.OrderStatus.PLACED;
import static group.rohlik.order.model.api.PaymentStatus.CANCELLED;
import static group.rohlik.order.model.api.PaymentStatus.PENDING;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServiceTest {

    private static final String ORDER_NOT_FOUND = "Order with ID: %s was not found!";

    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductApiClient productApiClient;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private DirectExchange directExchange;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_expectOrderCreated() {
        OrderEntity orderEntity = getOrderEntity();
        Product product = getProduct();

        when(productApiClient.getOrder(orderEntity.getOrderedProductEntities().get(0).getProductId()))
                .thenReturn(Optional.of(product));
        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(orderEntity)).thenReturn(orderEntity);

        ResponseEntity<Object> responseEntity = orderService.createOrder(orderEntity);

        assertEquals(orderEntity, responseEntity.getBody());

        verify(orderRepository).save(orderEntity);
    }

    @Test
    void createOrder_expectMissingProduct() {
        MissingProduct missingProduct = getMissingProduct();
        OrderedProductEntity orderedProductEntity = getOrderProductEntity();
        orderedProductEntity.setQuantity(2);
        OrderEntity orderEntity = getOrderEntity();
        orderEntity.setOrderedProductEntities(singletonList(orderedProductEntity));
        Product product = getProduct();

        when(productApiClient.getOrder(orderEntity.getOrderedProductEntities().get(0).getProductId()))
                .thenReturn(Optional.of(product));

        ResponseEntity<Object> responseEntity = orderService.createOrder(orderEntity);

        assertEquals(singletonList(missingProduct), responseEntity.getBody());

        verify(productApiClient).getOrder(orderEntity.getOrderedProductEntities().get(0).getProductId());
    }

    @Test
    void deleteOrder_expectOrderDeleted() {
        OrderEntity orderEntity = getOrderEntity();

        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));

        OrderEntity deletedOrderEntity = orderService.deleteOrder(orderEntity.getId());

        assertEquals(orderEntity, deletedOrderEntity);

        verify(orderRepository).findById(orderEntity.getId());
        verify(orderRepository).delete(orderEntity);
    }

    @Test
    void deleteOrder_expectOrderNotFoundException() {
        OrderEntity orderEntity = getOrderEntity();

        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.deleteOrder(orderEntity.getId()));

        String expectedMessage = format(ORDER_NOT_FOUND, orderEntity.getId());

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void updatePayment_expectUpdatedOrder() {
        OrderEntity orderEntity = getOrderEntity();
        OrderEntity cancelledOrderEntity = getOrderEntity();
        cancelledOrderEntity.setPaymentStatus(CANCELLED);

        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.of(orderEntity));
        when(orderRepository.save(cancelledOrderEntity)).thenReturn(cancelledOrderEntity);

        OrderEntity updatedOrderEntity = orderService.updatePayment(orderEntity.getId(), CANCELLED);

        assertEquals(cancelledOrderEntity, updatedOrderEntity);

        verify(orderRepository).findById(orderEntity.getId());
        verify(orderRepository).save(cancelledOrderEntity);
    }

    @Test
    void updatePayment_expectOrderNotFoundException() {
        OrderEntity orderEntity = getOrderEntity();

        when(orderRepository.findById(orderEntity.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.updatePayment(orderEntity.getId(), CANCELLED));

        String expectedMessage = format(ORDER_NOT_FOUND, orderEntity.getId());

        assertEquals(expectedMessage, exception.getMessage());
    }

    private OrderEntity getOrderEntity() {
        OrderEntity entity = new OrderEntity();
        entity.setId("1");
        entity.setTotalPrice(1.0);
        entity.setOrderStatus(PLACED);
        entity.setPaymentStatus(PENDING);
        entity.setOrderedProductEntities(singletonList(getOrderProductEntity()));
        return entity;
    }

    private OrderedProductEntity getOrderProductEntity() {
        OrderedProductEntity entity = new OrderedProductEntity();
        entity.setId("1");
        entity.setProductId("1");
        entity.setQuantity(1);
        return entity;
    }

    private Product getProduct() {
        Product product = new Product();
        product.setId("1");
        product.setName("apple");
        product.setUnitPrice(1.0);
        product.setStockQuantity(1);
        return product;
    }

    private MissingProduct getMissingProduct() {
        MissingProduct missingProduct = new MissingProduct();
        missingProduct.setName("apple");
        missingProduct.setMissingQuantity(1);
        return missingProduct;
    }
}
