package com.makemytrip.modules.pricing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "price_freezes")
@CompoundIndex(name = "user_entity_idx", def = "{'userId': 1, 'entityId': 1}")
public class PriceFreeze {
    @Id
    private String id;

    @Indexed
    private String userId;
    private String entityId;
    private String entityType;
    private double frozenPrice;
    private LocalDateTime frozenAt;

    @Indexed
    private LocalDateTime expiresAt;

    private boolean used;

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return !used && !isExpired();
    }
}
