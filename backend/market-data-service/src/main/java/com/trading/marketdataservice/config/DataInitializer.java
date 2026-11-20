package com.trading.marketdataservice.config;

import com.trading.marketdataservice.entity.Stock;
import com.trading.marketdataservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final StockRepository stockRepository;

    @Override
    public void run(String... args) throws Exception {
        if (stockRepository.count() > 0) {
            log.info("Stocks already initialized - Skipping");
            return;
        }

        log.info("Initializing stock data...");

        List<Stock> stocks = List.of(

                // US Market(NASDAQ)

                new Stock("AAPL", "Apple Inc.",
                        BigDecimal.valueOf(189.50),
                        "NASDAQ", "USD", true),

                new Stock("MSFT", "Microsoft Corporation",
                        BigDecimal.valueOf(425.75),
                        "NASDAQ", "USD", true),

                new Stock("NVDA", "NVIDIA Corporation",
                        BigDecimal.valueOf(138.25),
                        "NASDAQ", "USD", true),

                new Stock("AMZN", "Amazon.com Inc.",
                        BigDecimal.valueOf(215.60),
                        "NASDAQ", "USD", true),


                // Indian Market (NSE)

                new Stock("RELIANCE", "Reliance Industries Limited",
                        BigDecimal.valueOf(1450.50),
                        "NSE", "INR", true),

                new Stock("TCS", "Tata Consultancy Services Limited",
                        BigDecimal.valueOf(3850.75),
                        "NSE", "INR", true),

                new Stock("INFY", "Infosys Limited",
                        BigDecimal.valueOf(1650.25),
                        "NSE", "INR", true),

                new Stock("HDFCBANK", "HDFC Bank Limited",
                        BigDecimal.valueOf(1725.80),
                        "NSE", "INR", true),


                // British Market (LSE)

                new Stock("AZN", "AstraZeneca PLC",
                        BigDecimal.valueOf(115.50),
                        "LSE", "GBP", true),

                new Stock("HSBA", "HSBC Holdings PLC",
                        BigDecimal.valueOf(8.25),
                        "LSE", "GBP", true)

        );

        stockRepository.saveAll(stocks);
        log.info("Initialized {} stocks successfully", stocks.size());
    }
}
