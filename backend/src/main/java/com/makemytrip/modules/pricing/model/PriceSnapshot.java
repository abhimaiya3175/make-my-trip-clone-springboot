package com.makemytrip.modules.pricing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "price_snapshots")
@CompoundIndex(name = "entity_time_idx", def = "{'entityId': 1, 'snapshotTime': -1}")
public class PriceSnapshot {
    @Id
    private String id;

    private String entityId;        // flightId or hotelId
    private String entityType;      // "FLIGHT" or "HOTEL"
    private double basePrice;
    private double finalPrice;
    private double totalMultiplier;
    private String appliedRules;    // comma-separated rule names
    private LocalDateTime snapshotTime;
}
