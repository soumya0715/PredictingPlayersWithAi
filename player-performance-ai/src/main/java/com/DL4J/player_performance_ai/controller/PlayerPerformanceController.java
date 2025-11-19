package com.DL4J.player_performance_ai.controller;

import com.DL4J.player_performance_ai.dto.PlayerPerformanceDto;
import com.DL4J.player_performance_ai.service.PlayerPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Lock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/performance")
public class PlayerPerformanceController {

	@Autowired
	private final PlayerPerformanceService service;


    public PlayerPerformanceController(PlayerPerformanceService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlayerPerformanceDto> getAll() {
        return service.getAll();
    }

    @PostMapping
    public PlayerPerformanceDto add(@RequestBody PlayerPerformanceDto dto) {
        return service.add(dto);
    }

    // Update an existing player performance by ID
    @PutMapping("/{id}")
    public ResponseEntity<PlayerPerformanceDto> update(
            @PathVariable Long id, @RequestBody PlayerPerformanceDto dto) {
        PlayerPerformanceDto updatedDto = service.update(id, dto);
        return updatedDto != null ?
                ResponseEntity.ok(updatedDto) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Delete a player performance by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean isDeleted = service.delete(id);
        return isDeleted ?
                ResponseEntity.noContent().build() :
                ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    /**
     * Endpoint to predict if a player is suitable based on provided metrics.
     * Example JSON input:
     * {
     *   "average": 50.5,
     *   "strikeRate": 140.0,
     *   "bowlingAverage": 20.0,
     *   "economyRate": 4.2,
     *   "fieldingStats": 15
     * }
     */
    @PostMapping("/predict")
    public boolean predictPlayer(@RequestBody Map<String, Float> playerMetrics) {
        float[] features = new float[]{
                playerMetrics.get("average"),
                playerMetrics.get("strikeRate"),
                playerMetrics.get("bowlingAverage"),
                playerMetrics.get("economyRate"),
                playerMetrics.get("fieldingStats")
        };
        return service.predictPlayerSuitability(features);
    }

    /**
     * Endpoint to trigger model training with the latest data.
     * This will retrain the AI model using all player data in the database.
     */
    @PostMapping("/train")
    public ResponseEntity<String> trainModel() {
        try {
            service.trainModel();  // Call the service to train the model
            return ResponseEntity.ok("Model training started successfully.");
        } catch (Exception e) {
//            ("Error during model training", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to train the model.");
        }
    }
    
    
    /* get top N players based on overall performance metrics */
    @GetMapping("/top/{count}")
    public List<PlayerPerformanceDto> getTopPlayers(@PathVariable int count) {
        return service.getTopPlayers(count);
    }

    
   /*compare two players based on their predicted suitability and used ML training */
    @GetMapping("/compare/{id1}/{id2}")
    public ResponseEntity<String> comparePlayers(@PathVariable Long id1, @PathVariable Long id2) {
        return ResponseEntity.ok(service.comparePlayers(id1, id2));
    }
    

    /* Gives you a quick dataset summary (useful for dashboards) */
    @GetMapping("/stats/average")
    public Map<String, Double> getAverageStats() {
        return service.getAverageStats();
    }
    

    /* Find all players who exceed certain thresholds (e.g., batting average > 50). */
    @PostMapping("/filter")
    public List<PlayerPerformanceDto> filterPlayers(@RequestBody Map<String, Object> filterCriteria) {
        double minAverage = ((Number) filterCriteria.getOrDefault("minAverage", 0)).doubleValue();
        double minStrikeRate = ((Number) filterCriteria.getOrDefault("minStrikeRate", 0)).doubleValue();
        int minFielding = ((Number) filterCriteria.getOrDefault("minFielding", 0)).intValue();

        return service.filterPlayers(minAverage, minStrikeRate, minFielding);
    }

    
    /* Show player performance trend */
    @GetMapping("/trend/{id}")
    public Map<String, Double> getPerformanceTrend(@PathVariable Long id) {
        return service.getPerformanceTrend(id);
    }

   
    
    
}
