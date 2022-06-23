package group.rohlik.order.exceptions;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class OrderControllerAdvice {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(OrderNotFoundException.class)
    ApiError handleProductNotFoundException(OrderNotFoundException exception) {
        return new ApiError(exception.getMessage());
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(ProductNotFoundException.class)
    ApiError handleProductNotFoundException(ProductNotFoundException exception) {
        return new ApiError(exception.getMessage());
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(PaymentStatusException.class)
    ApiError handlePaymentStatusException(PaymentStatusException exception) {
        return new ApiError(exception.getMessage());
    }

}

record ApiError(String message) {
}


