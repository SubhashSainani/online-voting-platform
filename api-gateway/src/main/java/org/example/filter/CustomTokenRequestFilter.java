package org.example.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class CustomTokenRequestFilter extends AbstractGatewayFilterFactory<CustomTokenRequestFilter.Config> {

    public CustomTokenRequestFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Extract token from query param
            String token = request.getQueryParams().getFirst("token");

            if (token != null && !token.isEmpty()) {
                // Add token to the Authorization header
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("Authorization", "Bearer " + token)
                        .build();

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .build();

                return chain.filter(modifiedExchange);
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}