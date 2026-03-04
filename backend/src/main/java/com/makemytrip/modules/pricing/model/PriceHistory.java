package com.makemytrip.modules.pricing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "price_history")
public class PriceHistory {
    @Id
    private String id;
    private String itemId;
    private String itemType;
    private double price;
    private String timestamp;
    private double demandFactor;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public double getDemandFactor() { return demandFactor; }
    public void setDemandFactor(double demandFactor) { this.demandFactor = demandFactor; }
}
