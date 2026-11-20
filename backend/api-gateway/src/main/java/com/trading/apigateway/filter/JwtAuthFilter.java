
package com.trading.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final SecretKey secretKey;

    public JwtAuthFilter(@Value("${jwt.secret}") String jwtSecret) {
        super(Config.class);
        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    @Override
    public GatewayFilter apply(Config config) {

        return ((exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            String path = request.getPath().toString();

            // Skip JWT for public endpoints
            if (isPublicEndpoint(path)) {
                log.debug("Public endpoint - skipping JWT: {}", path);
                return chain.filter(exchange);
            }

            // Check Authorization header
            String authHeader = request.getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || authHeader.isBlank()) {
                log.warn("Missing Authorization header: {}", path);

                return unauthorizedResponse(
                        exchange,
                        "Authorization header missing"
                );
            }

            // Validate Bearer token format
            if (!authHeader.startsWith("Bearer ")) {
                return unauthorizedResponse(
                        exchange,
                        "Invalid Authorization format"
                );
            }

            String token = authHeader.substring(7);

            try {

                // Extract JWT claims
                Claims claims = extractClaims(token);

                String userId = claims.get("userId", String.class);
                String email = claims.getSubject();

                String tokenType = claims.get("tokenType", String.class);

                // Only ACCESS tokens are allowed for protected API endpoints
                if (!"ACCESS".equals(tokenType)) {
                    return unauthorizedResponse(
                            exchange,
                            "Invalid token type"
                    );
                }

                if (userId == null || userId.isBlank()) {
                    return unauthorizedResponse(
                            exchange,
                            "Invalid JWT: userId missing"
                    );
                }

                if (email == null || email.isBlank()) {
                    return unauthorizedResponse(
                            exchange,
                            "Invalid JWT: email missing"
                    );
                }

                log.debug("Authenticated request for userId: {}", userId);

                // Add authenticated user information to request
                ServerHttpRequest mutatedRequest = exchange.getRequest()
                        .mutate()
                        .headers(headers -> {
                            headers.remove("X-User-Id");
                            headers.set("X-User-Id", userId);
                        })
                        .build();

                return chain.filter(
                        exchange.mutate()
                                .request(mutatedRequest)
                                .build()
                );

            } catch (Exception e) {

                log.warn("Invalid JWT token: {}", e.getMessage());

                return unauthorizedResponse(
                        exchange,
                        "Invalid or expired token"
                );
            }
        });
    }

    // Identify endpoints that don't require authentication
    /* private boolean isPublicEndpoint(String path) {

        List<String> publicEndpoints = List.of(
                "/api/auth/register",
                "/api/auth/login",
                "/actuator/health",
                "/ws"
        );

        return publicEndpoints.contains(path);
    }*/

    private boolean isPublicEndpoint(String path) {

        List<String> publicEndpoints = List.of(
                "/api/v1/users/register",
                "/api/v1/users/login",
                "/actuator/health"
        );

        return publicEndpoints.contains(path);
    }

    // Extract claims from JWT
    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Return unauthorized response
    private Mono<Void> unauthorizedResponse(
            ServerWebExchange exchange,
            String message) {

        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {
                    "status": 401,
                    "error": "Unauthorized",
                    "message": "%s"
                }
                """.formatted(message);

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
