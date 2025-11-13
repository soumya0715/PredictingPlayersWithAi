package com.DL4J.player_performance_ai.dto;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PlayerPerformanceDto implements Serializable {

    private Long id;
    private double average;
    private double strikeRate;
    private double bowlingAverage;
    private double economyRate;
    private int fieldingStats;
    private int label;
    
    
	@Override
	public String toString() {
		return "PlayerPerformanceDto [id=" + id + ", average=" + average + ", strikeRate=" + strikeRate
				+ ", bowlingAverage=" + bowlingAverage + ", economyRate=" + economyRate + ", fieldingStats="
				+ fieldingStats + ", label=" + label + "]";
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public double getAverage() {
		return average;
	}
	public void setAverage(double average) {
		this.average = average;
	}
	public double getStrikeRate() {
		return strikeRate;
	}
	public void setStrikeRate(double strikeRate) {
		this.strikeRate = strikeRate;
	}
	public double getBowlingAverage() {
		return bowlingAverage;
	}
	public void setBowlingAverage(double bowlingAverage) {
		this.bowlingAverage = bowlingAverage;
	}
	public double getEconomyRate() {
		return economyRate;
	}
	public void setEconomyRate(double economyRate) {
		this.economyRate = economyRate;
	}
	public int getFieldingStats() {
		return fieldingStats;
	}
	public void setFieldingStats(int fieldingStats) {
		this.fieldingStats = fieldingStats;
	}
	public int getLabel() {
		return label;
	}
	public void setLabel(int label) {
		this.label = label;
	}
    
    
}
