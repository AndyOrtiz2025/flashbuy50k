package com.flashbuy.catalog.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flashbuy.catalog.model.Product;
import com.flashbuy.catalog.model.Season;
import com.flashbuy.catalog.repository.ProductRepository;
import com.flashbuy.catalog.repository.SeasonRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final SeasonRepository seasonRepository;
    private final ProductRepository productRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "catalog-service"));
    }

    @GetMapping("/seasons/active")
    public ResponseEntity<List<Season>> getActiveSeasons() {
        return ResponseEntity.ok(seasonRepository.findByIsActiveTrue());
    }

    @GetMapping("/seasons/{seasonId}/products")
    public ResponseEntity<List<Product>> getProductsBySeason(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(productRepository.findBySeasonIdAndIsVisibleTrue(seasonId));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable UUID productId) {
        return productRepository.findById(productId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}