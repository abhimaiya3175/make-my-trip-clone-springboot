package com.makemytrip.modules.pricing.repository;

import com.makemytrip.modules.pricing.model.PriceSnapshot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceSnapshotRepository extends MongoRepository<PriceSnapshot, String> {
    List<PriceSnapshot> findByEntityIdOrderBySnapshotTimeDesc(String entityId);
    List<PriceSnapshot> findByEntityIdAndSnapshotTimeBetweenOrderBySnapshotTimeAsc(
            String entityId, LocalDateTime start, LocalDateTime end);
    List<PriceSnapshot> findByEntityIdOrderBySnapshotTimeDesc(String entityId, Pageable pageable);
}
