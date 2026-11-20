package com.trading.portfolioservice.controller;

import com.trading.portfolioservice.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /*
    * Get my portfolio with live P & L
    * @param userId
    * @return
    * */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyPortfolio(
            @RequestHeader("X-User-Id") String userId) {

        return ResponseEntity.ok(portfolioService.getPortfolio(userId));
    }

    /*
     * Get my portfolio by userId -  For Internal Calls
     * @param userId
     * @return
     * */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getPortfolio(
            @PathVariable String userId) {

        return ResponseEntity.ok(portfolioService.getPortfolio(userId));
    }
}
