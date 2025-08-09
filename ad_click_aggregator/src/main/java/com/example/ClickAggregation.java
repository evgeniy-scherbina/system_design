package com.example;

import java.time.Instant;

public class ClickAggregation {
    private String campaignId;
    private String country;
    private String deviceType;
    private Instant windowStart;
    private Instant windowEnd;
    private Long clickCount;
    private Double totalValue;
    private Long uniqueUsers;

    public ClickAggregation() {}

    public ClickAggregation(String campaignId, String country, String deviceType, 
                           Instant windowStart, Instant windowEnd, 
                           Long clickCount, Double totalValue, Long uniqueUsers) {
        this.campaignId = campaignId;
        this.country = country;
        this.deviceType = deviceType;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.clickCount = clickCount;
        this.totalValue = totalValue;
        this.uniqueUsers = uniqueUsers;
    }

    // Getters and setters
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public Instant getWindowStart() { return windowStart; }
    public void setWindowStart(Instant windowStart) { this.windowStart = windowStart; }

    public Instant getWindowEnd() { return windowEnd; }
    public void setWindowEnd(Instant windowEnd) { this.windowEnd = windowEnd; }

    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }

    public Double getTotalValue() { return totalValue; }
    public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }

    public Long getUniqueUsers() { return uniqueUsers; }
    public void setUniqueUsers(Long uniqueUsers) { this.uniqueUsers = uniqueUsers; }

    @Override
    public String toString() {
        return "ClickAggregation{" +
                "campaignId='" + campaignId + '\'' +
                ", country='" + country + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                ", clickCount=" + clickCount +
                ", totalValue=" + totalValue +
                ", uniqueUsers=" + uniqueUsers +
                '}';
    }
} 