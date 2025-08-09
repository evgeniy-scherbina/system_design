package com.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class AdClickEvent {
    @JsonProperty("ad_id")
    private String adId;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("campaign_id")
    private String campaignId;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("click_value")
    private Double clickValue;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("device_type")
    private String deviceType;

    // Default constructor for Jackson
    public AdClickEvent() {}

    public AdClickEvent(String adId, String userId, String campaignId, Instant timestamp, 
                       Double clickValue, String country, String deviceType) {
        this.adId = adId;
        this.userId = userId;
        this.campaignId = campaignId;
        this.timestamp = timestamp;
        this.clickValue = clickValue;
        this.country = country;
        this.deviceType = deviceType;
    }

    // Getters and setters
    public String getAdId() { return adId; }
    public void setAdId(String adId) { this.adId = adId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Double getClickValue() { return clickValue; }
    public void setClickValue(Double clickValue) { this.clickValue = clickValue; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    @Override
    public String toString() {
        return "AdClickEvent{" +
                "adId='" + adId + '\'' +
                ", userId='" + userId + '\'' +
                ", campaignId='" + campaignId + '\'' +
                ", timestamp=" + timestamp +
                ", clickValue=" + clickValue +
                ", country='" + country + '\'' +
                ", deviceType='" + deviceType + '\'' +
                '}';
    }
} 