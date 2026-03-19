package com.makemytrip.modules.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "recommendations")
@CompoundIndex(name = "user_item_idx", def = "{'userId': 1, 'itemType': 1, 'itemId': 1}")
@CompoundIndex(name = "user_generated_idx", def = "{'userId': 1, 'createdAt': -1}")
public class Recommendation {
    @Id
    private String id;

    @Indexed
    private String userId;
    private String itemId;
    private String itemType;    // "FLIGHT" or "HOTEL"
    private double score;       // 0.0 - 1.0 relevance score
    private String reason;      // human-readable explanation
    private List<String> tags;  // e.g. ["beach", "weekend", "budget"]

    @Indexed(expireAfter = "86400s")
    private LocalDateTime createdAt;
}
