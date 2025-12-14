package com.c1se_01.roomiego.dto;

public class DistanceMatrixResult {
  private final double distanceKm;
  private final int distanceMeters;
  private final String distanceText;
  private final int durationMinutes;
  private final int durationSeconds;
  private final String durationText;
  private final String travelMode;

  public DistanceMatrixResult(double distanceKm, int distanceMeters, String distanceText,
      int durationMinutes, int durationSeconds, String durationText, String travelMode) {
    this.distanceKm = distanceKm;
    this.distanceMeters = distanceMeters;
    this.distanceText = distanceText;
    this.durationMinutes = durationMinutes;
    this.durationSeconds = durationSeconds;
    this.durationText = durationText;
    this.travelMode = travelMode;
  }

  public double getDistanceKm() {
    return distanceKm;
  }

  public int getDistanceMeters() {
    return distanceMeters;
  }

  public String getDistanceText() {
    return distanceText;
  }

  public int getDurationMinutes() {
    return durationMinutes;
  }

  public int getDurationSeconds() {
    return durationSeconds;
  }

  public String getDurationText() {
    return durationText;
  }

  public String getTravelMode() {
    return travelMode;
  }
  
}
