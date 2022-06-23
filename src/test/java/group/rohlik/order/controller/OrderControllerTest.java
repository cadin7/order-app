package group.rohlik.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import group.rohlik.order.model.api.Order;
import group.rohlik.order.model.api.OrderedProduct;
import group.rohlik.order.model.entity.OrderEntity;
import group.rohlik.order.model.entity.OrderedProductEntity;
import group.rohlik.order.model.mapper.OrderMapper;
import group.rohlik.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static group.rohlik.order.model.api.OrderStatus.PLACED;
import static group.rohlik.order.model.api.PaymentStatus.COMPLETED;
import static group.rohlik.order.model.api.PaymentStatus.PENDING;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {OrderController.class, OrderService.class})
public class OrderControllerTest {

    private static final String ORDERS_URL = "/orders";
    public static final String SLASH = "/";

    @MockBean
    private OrderService orderService;
    @MockBean
    private OrderMapper orderMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    private Order order;
    private OrderEntity orderEntity;

    @BeforeEach
    void setup() {
        order = getOrder();
        orderEntity = getOrderEntity();
    }

    @Test
    void createOrder_expectPlacedOrder() throws Exception {
        when(orderMapper.toEntity(order)).thenReturn(orderEntity);
        when(orderService.createOrder(orderEntity)).thenReturn(new ResponseEntity<>(orderEntity, OK));

        mockMvc.perform(post(ORDERS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.orderStatus").value(order.getOrderStatus().name().toLowerCase()))
                .andExpect(jsonPath("$.paymentStatus").value(order.getPaymentStatus().name().toLowerCase()));

        verify(orderMapper).toEntity(order);
        verify(orderService).createOrder(orderEntity);
    }

    @Test
    void deleteOrder_expectOrderDeleted() throws Exception {
        when(orderMapper.toApi(orderEntity)).thenReturn(order);
        when(orderService.deleteOrder(orderEntity.getId())).thenReturn(orderEntity);

        mockMvc.perform(delete(ORDERS_URL + SLASH + orderEntity.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderEntity.getId()))
                .andExpect(jsonPath("$.orderStatus").value(orderEntity.getOrderStatus().name().toLowerCase()))
                .andExpect(jsonPath("$.paymentStatus").value(orderEntity.getPaymentStatus().name().toLowerCase()));

        verify(orderMapper).toApi(orderEntity);
        verify(orderService).deleteOrder(orderEntity.getId());
    }

    @Test
    void updatePayment_expectUpdatePaymentAndOrder() throws Exception {
        OrderEntity payedOrderEntity = getPayedOrderEntity();
        Order payedOrder = getPayedOrder();

        when(orderMapper.toApi(payedOrderEntity)).thenReturn(payedOrder);
        when(orderService.updatePayment(orderEntity.getId(), COMPLETED)).thenReturn(payedOrderEntity);

        mockMvc.perform(put(ORDERS_URL + SLASH + orderEntity.getId())
                        .param("paymentStatus", COMPLETED.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payedOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payedOrder.getId()))
                .andExpect(jsonPath("$.orderStatus").value(payedOrder.getOrderStatus().name().toLowerCase()))
                .andExpect(jsonPath("$.paymentStatus").value(payedOrder.getPaymentStatus().name().toLowerCase()));

        verify(orderMapper).toApi(payedOrderEntity);
        verify(orderService).updatePayment(orderEntity.getId(), COMPLETED);
    }

    private Order getOrder() {
        Order order = new Order();
        order.setId("1");
        order.setOrderStatus(PLACED);
        order.setPaymentStatus(PENDING);
        order.setOrderedProducts(singletonList(getOrderProduct()));
        return order;
    }

    private Order getPayedOrder() {
        Order order = getOrder();
        order.setPaymentStatus(COMPLETED);
        return order;
    }

    private OrderedProduct getOrderProduct() {
        OrderedProduct orderedProduct = new OrderedProduct();
        orderedProduct.setId("1");
        orderedProduct.setProductId("1");
        orderedProduct.setQuantity(1);
        return orderedProduct;
    }

    private OrderEntity getOrderEntity() {
        OrderEntity entity = new OrderEntity();
        entity.setId("1");
        entity.setTotalPrice(5.0);
        entity.setOrderStatus(PLACED);
        entity.setPaymentStatus(PENDING);
        entity.setOrderedProductEntities(singletonList(getOrderProductEntity()));
        return entity;
    }

    private OrderEntity getPayedOrderEntity() {
        OrderEntity entity = getOrderEntity();
        entity.setPaymentStatus(COMPLETED);
        return entity;
    }

    private OrderedProductEntity getOrderProductEntity() {
        OrderedProductEntity entity = new OrderedProductEntity();
        entity.setId("1");
        entity.setProductId("1");
        entity.setQuantity(1);
        return entity;
    }
}
