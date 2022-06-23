package group.rohlik.order.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum OrderStatus {

    @JsonProperty("placed")
    PLACED,

    @JsonProperty("cancelled")
    CANCELLED,

    @JsonProperty("completed")
    COMPLETED,

    @JsonProperty("invalid")
    INVALID
}
