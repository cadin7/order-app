package group.rohlik.order.service;

import group.rohlik.order.model.api.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Component
public class ProductApiClient {

    private final String baseUrl;

    private final RestTemplate template;

    public ProductApiClient(@Value("${product-service-location:NOT_DEFINED}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.template = new RestTemplate();
    }

    public Optional<Product> getOrder(String productId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/products/")
                .path(productId)
                .toUriString();

        try {
            return ofNullable(template.getForObject(url, Product.class));
        } catch (HttpClientErrorException exception) {
            return empty();
        }
    }
}
