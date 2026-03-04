package com.makemytrip.modules.cancellation.repository;

import com.makemytrip.modules.cancellation.model.RefundTracker;
import com.makemytrip.modules.cancellation.model.RefundStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RefundTracker entity
 */
@Repository
public interface RefundTrackerRepository extends MongoRepository<RefundTracker, String> {
    
    // Find refund tracker by cancellation ID
    Optional<RefundTracker> findByCancellationId(String cancellationId);
    
    // Find all refund trackers by status
    List<RefundTracker> findByStatus(RefundStatus status);
    
    // Find all pending refunds
    List<RefundTracker> findByStatusIn(List<RefundStatus> statuses);
}
