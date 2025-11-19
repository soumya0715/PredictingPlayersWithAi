package com.DL4J.player_performance_ai.service;

import com.DL4J.player_performance_ai.ai.PlayerAIModel;
import com.DL4J.player_performance_ai.dto.PlayerPerformanceDto;
import com.DL4J.player_performance_ai.model.PlayerPerformance;
import com.DL4J.player_performance_ai.repository.PlayerPerformanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PlayerPerformanceService {

    private final PlayerPerformanceRepository repository;
    private final PlayerAIModel playerAIModel;
    private static final String MODEL_PATH = "src/main/resources/player_model.zip";

//    @Autowired
    public PlayerPerformanceService(PlayerPerformanceRepository repository,PlayerAIModel playerAIModel) {
        this.repository = repository;
        this.playerAIModel = playerAIModel;
    }

    public List<PlayerPerformanceDto> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public PlayerPerformanceDto add(PlayerPerformanceDto dto) {
        PlayerPerformance performance = repository.save(toEntity(dto));
        playerAIModel.trainModel();  // Train the model after adding new data
        return toDto(performance);
    }

    /**
     * Update an existing player performance by ID.
     * @param id The ID of the player performance to update.
     * @param dto The updated player performance data.
     * @return The updated PlayerPerformanceDto or null if the ID was not found.
     */
    public PlayerPerformanceDto update(Long id, PlayerPerformanceDto dto) {
        Optional<PlayerPerformance> existingPerformance = repository.findById(id);
        if (existingPerformance.isPresent()) {
            PlayerPerformance performance = existingPerformance.get();
            performance.setAverage(dto.getAverage());
            performance.setStrikeRate(dto.getStrikeRate());
            performance.setBowlingAverage(dto.getBowlingAverage());
            performance.setEconomyRate(dto.getEconomyRate());
            performance.setFieldingStats(dto.getFieldingStats());
            performance.setLabel(dto.getLabel());
            PlayerPerformance updatedPerformance = repository.save(performance);
            playerAIModel.trainModel();  // Train the model after adding new data
            return toDto(updatedPerformance);
        }
        return null;
    }

    /**
     * Delete a player performance by ID.
     * @param id The ID of the player performance to delete.
     * @return true if the performance was deleted, false otherwise.
     */
    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            playerAIModel.trainModel();  // Train the model after adding new data
            return true;
        }
        return false;
    }

    /**
     * Predicts if the player is suitable based on provided metrics.
     * @param features An array containing player metrics.
     * @return true if the player is suitable, false otherwise.
     */
    public boolean predictPlayerSuitability(float[] features) {
        return playerAIModel.predict(features);
    }

    private PlayerPerformance toEntity(PlayerPerformanceDto dto) {
        PlayerPerformance entity = new PlayerPerformance();
        entity.setAverage(dto.getAverage());
        entity.setStrikeRate(dto.getStrikeRate());
        entity.setBowlingAverage(dto.getBowlingAverage());
        entity.setEconomyRate(dto.getEconomyRate());
        entity.setFieldingStats(dto.getFieldingStats());
        entity.setLabel(dto.getLabel());
        return entity;
    }

    private PlayerPerformanceDto toDto(PlayerPerformance entity) {
        PlayerPerformanceDto dto = new PlayerPerformanceDto();
        dto.setId(entity.getId());
        dto.setAverage(entity.getAverage());
        dto.setStrikeRate(entity.getStrikeRate());
        dto.setBowlingAverage(entity.getBowlingAverage());
        dto.setEconomyRate(entity.getEconomyRate());
        dto.setFieldingStats(entity.getFieldingStats());
        dto.setLabel(entity.getLabel());
        return dto;
    }
    
    public List<PlayerPerformanceDto> getTopPlayers(int count) {
        return repository.findAll().stream()
                .sorted((p1, p2) -> Double.compare(
                        calculatePerformanceScore(p2),
                        calculatePerformanceScore(p1)))
                .limit(count)
                .map(this::toDto)
                .toList();
    }

    private double calculatePerformanceScore(PlayerPerformance player) {
        // Simple formula combining batting, bowling, and fielding stats
        return player.getAverage() * 0.4
                + player.getStrikeRate() * 0.2
                + (100 - player.getBowlingAverage()) * 0.2
                + (100 - player.getEconomyRate()) * 0.1
                + player.getFieldingStats() * 0.1;
    }

    
    public String comparePlayers(Long id1, Long id2) {
        Optional<PlayerPerformance> p1 = repository.findById(id1);
        Optional<PlayerPerformance> p2 = repository.findById(id2);

        if (p1.isEmpty() || p2.isEmpty()) {
            return "One or both player IDs not found.";
        }

        float[] features1 = {
                (float) p1.get().getAverage(),
                (float) p1.get().getStrikeRate(),
                (float) p1.get().getBowlingAverage(),
                (float) p1.get().getEconomyRate(),
                p1.get().getFieldingStats()
        };
        float[] features2 = {
                (float) p2.get().getAverage(),
                (float) p2.get().getStrikeRate(),
                (float) p2.get().getBowlingAverage(),
                (float) p2.get().getEconomyRate(),
                p2.get().getFieldingStats()
        };

        boolean player1Better = playerAIModel.predict(features1);
        boolean player2Better = playerAIModel.predict(features2);

        if (player1Better && !player2Better)
            return p1.get().getId() + " is predicted to be more suitable.";
        else if (!player1Better && player2Better)
            return p2.get().getId() + " is predicted to be more suitable.";
        else
            return "Both players are equally suitable based on AI prediction.";
    }

    
    public Map<String, Double> getAverageStats() {
        List<PlayerPerformance> players = repository.findAll();
        if (players.isEmpty()) return Map.of();

        double avgBat = players.stream().mapToDouble(PlayerPerformance::getAverage).average().orElse(0);
        double avgSR = players.stream().mapToDouble(PlayerPerformance::getStrikeRate).average().orElse(0);
        double avgBowl = players.stream().mapToDouble(PlayerPerformance::getBowlingAverage).average().orElse(0);
        double avgEco = players.stream().mapToDouble(PlayerPerformance::getEconomyRate).average().orElse(0);
        double avgField = players.stream().mapToInt(PlayerPerformance::getFieldingStats).average().orElse(0);

        return Map.of(
                "Average Batting", avgBat,
                "Strike Rate", avgSR,
                "Bowling Average", avgBowl,
                "Economy Rate", avgEco,
                "Fielding Stats", avgField
        );
    }

    public List<PlayerPerformanceDto> filterPlayers(double minAverage, double minStrikeRate, int minFielding) {
        return repository.findAll().stream()
                .filter(p -> p.getAverage() >= minAverage
                        && p.getStrikeRate() >= minStrikeRate
                        && p.getFieldingStats() >= minFielding)
                .map(this::toDto)
                .toList();
    }

    public Map<String, Double> getPerformanceTrend(Long id) {
        Optional<PlayerPerformance> player = repository.findById(id);
        if (player.isEmpty()) return Map.of("error", -1.0);

        PlayerPerformance p = player.get();
        // Mock "trend" over time (in a real system, you'd have time-series data)
        return Map.of(
                "Initial Score", calculatePerformanceScore(p) * 0.8,
                "Mid Season", calculatePerformanceScore(p) * 0.9,
                "Recent", calculatePerformanceScore(p)
        );
    }


    public void trainModel() {
        playerAIModel.trainModel();
    }
}

