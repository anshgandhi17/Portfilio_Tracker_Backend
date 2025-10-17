package com.ansh.portfilio_tracker.Controller;

import com.ansh.portfilio_tracker.Classes.CreateHoldingRequest;
import com.ansh.portfilio_tracker.Classes.Holding;
import com.ansh.portfilio_tracker.Service.HoldingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/api/holdings")
@CrossOrigin(origins = "http://localhost:5173") // remove/change for production
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;

    @GetMapping("/{symbol}")
    public ResponseEntity<Holding> getHolding(@PathVariable String symbol) {
        Holding holding = holdingService.getHolding(symbol);
        if (holding != null) {
            return ResponseEntity.ok(holding);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<Collection<Holding>> getAllHoldings() {
        return ResponseEntity.ok(holdingService.getAllHoldings());
    }

    @PostMapping
    public ResponseEntity<Holding> createHolding(@Valid @RequestBody CreateHoldingRequest request) {
        Holding holding = holdingService.createHolding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(holding);
    }

    @PostMapping("/portfolio/{portfolioId}")
    public ResponseEntity<Holding> addHoldingToPortfolio(
            @PathVariable UUID portfolioId,
            @Valid @RequestBody CreateHoldingRequest request) {
        request.setPortfolioId(portfolioId);
        Holding holding = holdingService.createHolding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(holding);
    }

    @PutMapping("/{symbol}/refresh")
    public ResponseEntity<Holding> refreshHoldingPrice(@PathVariable String symbol) {
        Holding holding = holdingService.getHolding(symbol);
        if (holding != null) {
            return ResponseEntity.ok(holding);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}