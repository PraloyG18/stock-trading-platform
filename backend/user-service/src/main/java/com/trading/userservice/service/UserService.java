package com.trading.userservice.service;

import com.trading.userservice.dto.AuthResponse;
import com.trading.userservice.dto.LoginRequest;
import com.trading.userservice.dto.RegisterRequest;
import com.trading.userservice.dto.UserResponse;
import com.trading.userservice.entity.User;
import com.trading.userservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_REGISTERED_TOPIC = "user.registered";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /*
    * Register a new Trader
    * */

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering User: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email is already registered" + request.getEmail()
            );
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setWalletBalance(
                request.getInitialDeposit() != null
                ? request.getInitialDeposit()
                : BigDecimal.valueOf(10000)
        );

        User savedUser = userRepository.save(user);

        log.info("User Registered: {}", savedUser.getId());

        // Publish user.registered event

        Map<String, Object> userRegisteredEvent = new HashMap<>();

        userRegisteredEvent.put("userId", savedUser.getId());
        userRegisteredEvent.put("email", savedUser.getEmail());
        userRegisteredEvent.put("firstName", savedUser.getFirstName());
        userRegisteredEvent.put("lastName", savedUser.getLastName());
        userRegisteredEvent.put("walletBalance", savedUser.getWalletBalance());

        kafkaTemplate.send(USER_REGISTERED_TOPIC, savedUser.getId(), userRegisteredEvent);

        // AccessToken
        String token = generateAccessToken(savedUser.getId(), savedUser.getEmail());

        // Refresh Token
        String refreshToken = generateRefreshToken(savedUser.getId());

        return buildAuthResponse(savedUser, token, refreshToken);
    }

    /*
    * Login
    * */

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + request.getEmail()
                ));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid Credentials");
        }

        log.info("Login Successful: {}", user.getId());

        // AccessToken
        String token = generateAccessToken(user.getId(),user.getEmail());

        // Refresh Token
        String refreshToken = generateRefreshToken(user.getId());

        return buildAuthResponse(user, token, refreshToken);
    }

    /*
    * Get User By ID
    * Called by other services via Feign (internal)
    * */

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found" + userId
                ));

        return mapToResponse(user);
    }

    /*
     * Add Funds to wallet
     * */


    @Transactional
    public UserResponse addFunds(String userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId
                ));

        user.setWalletBalance(
                user.getWalletBalance().add(amount)
        );

        User savedUser = userRepository.save(user);

        log.info("Funds added: {} to user: {}", amount, userId);

        return mapToResponse(savedUser);
    }


    /*
     * Deduct Funds to wallet - Called on BUY Order
     * */


    @Transactional
    public UserResponse deductFunds(String userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Deduction amount must be greater than zero"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId
                ));

        if (user.getWalletBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }

        user.setWalletBalance(
                user.getWalletBalance().subtract(amount)
        );

        User savedUser = userRepository.save(user);

        log.info("Funds deducted: {} from user: {}", amount, userId);

        return mapToResponse(savedUser);
    }


    /*
    * Credit Funds to wallet - Called on SELL Order
    * */


    @Transactional
    public UserResponse creditFunds(String userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Credit amount must be greater than zero"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + userId
                ));

        user.setWalletBalance(
                user.getWalletBalance().add(amount)
        );

        User savedUser = userRepository.save(user);

        log.info("Funds credited: {} to user: {}", amount, userId);

        return mapToResponse(savedUser);
    }


    private UserResponse mapToResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setWalletBalance(user.getWalletBalance());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    private String generateAccessToken(String userId, String email) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("tokenType", "ACCESS")
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSignInKey())
                .compact();
    }

    private String generateRefreshToken(String userId) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("tokenType", "REFRESH")
                .subject(userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse response = new AuthResponse();

        response.setAccessToken(accessToken);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setRefreshToken(refreshToken);
        response.setWalletBalance(user.getWalletBalance());

        return response;
    }
}