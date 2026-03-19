import React, { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import StarRating from "./StarRating";
import { HelpfulButton } from "./HelpfulButton";
import { FlagButton } from "./FlagButton";
import { ReplySection } from "./ReplySection";
import { getReviews, voteHelpful, flagReview, replyToReview } from "@/services/reviewService";
import { REQUEST_STATE } from "@/utils/requestState";

interface Review {
  id: string;
  userId: string;
  userName?: string;
  entityId: string;
  entityType: string;
  rating: number;
  text: string;
  photos?: string[];
  helpfulCount: number;
  helpfulVoters: string[];
  flagged: boolean;
  replies: Array<{
    userId: string;
    userName: string;
    text: string;
    isOwner: boolean;
    timestamp: string;
  }>;
  createdAt: string;
  updatedAt: string;
  // Legacy fields
  bookingId?: string;
  comment?: string;
}

interface ReviewListProps {
  entityType: 'FLIGHT' | 'HOTEL';
  entityId: string;
  currentUserId?: string;
  currentUserName?: string;
  isOwner?: boolean;
}

const SORT_OPTIONS = [
  { value: 'latest', label: 'Latest' },
  { value: 'helpful', label: 'Most Helpful' },
  { value: 'rating', label: 'Highest Rating' }
];

const ReviewList: React.FC<ReviewListProps> = ({ 
  entityType, 
  entityId, 
  currentUserId,
  currentUserName,
  isOwner = false
}) => {
  const [reviews, setReviews] = useState<Review[]>([]);
  const [requestState, setRequestState] = useState(REQUEST_STATE.IDLE);
  const [sortBy, setSortBy] = useState('latest');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [hasMore, setHasMore] = useState(false);

  useEffect(() => {
    loadReviews();
  }, [entityType, entityId, sortBy, page]);

  const loadReviews = async () => {
    setRequestState(REQUEST_STATE.LOADING);
    try {
      const response = await getReviews({
        entityType,
        entityId,
        sortBy,
        page,
        size: 10
      });
      
      setReviews(response.items);
      setTotalPages(response.totalPages);
      setHasMore(response.hasNext);
      setRequestState(REQUEST_STATE.SUCCESS);
    } catch (error) {
      console.error('Failed to load reviews:', error);
      setRequestState(REQUEST_STATE.ERROR);
    }
  };

  const handleVoteHelpful = async (reviewId: string) => {
    if (!currentUserId) return;
    
    try {
      const updatedReview = await voteHelpful(reviewId, currentUserId);
      setReviews(reviews.map(r => r.id === reviewId ? updatedReview : r));
    } catch (error) {
      throw error;
    }
  };

  const handleFlag = async (reviewId: string, reason: string) => {
    if (!currentUserId) return;
    
    try {
      await flagReview(reviewId, currentUserId, reason);
      await loadReviews(); // Refresh the list
    } catch (error) {
      throw error;
    }
  };

  const handleReply = async (reviewId: string, text: string, userName: string, isOwnerReply: boolean) => {
    if (!currentUserId) return;
    
    try {
      const updatedReview = await replyToReview(reviewId, currentUserId, {
        text,
        userName,
        isOwner: isOwnerReply
      });
      setReviews(reviews.map(r => r.id === reviewId ? updatedReview : r));
    } catch (error) {
      throw error;
    }
  };

  if (requestState === REQUEST_STATE.LOADING && reviews.length === 0) {
    return (
      <div className="flex justify-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (requestState === REQUEST_STATE.ERROR) {
    return (
      <div className="text-center py-8">
        <p className="text-red-600">Failed to load reviews. Please try again.</p>
        <button 
          onClick={loadReviews}
          className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          Retry
        </button>
      </div>
    );
  }

  if (!reviews || reviews.length === 0) {
    return (
      <div className="text-center py-8">
        <p className="text-gray-500 text-sm">No reviews yet. Be the first to review!</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Sort Dropdown */}
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold">Customer Reviews</h3>
        <select
          value={sortBy}
          onChange={(e) => {
            setSortBy(e.target.value);
            setPage(1);
          }}
          className="px-3 py-1.5 border border-gray-300 rounded-md text-sm focus:ring-2 focus:ring-blue-500"
        >
          {SORT_OPTIONS.map(option => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      {/* Reviews List */}
      {reviews.map((review) => (
        <Card key={review.id} className="hover:shadow-md transition-shadow">
          <CardHeader className="pb-3">
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <CardTitle className="text-base font-semibold">
                  {review.userName || "Anonymous"}
                </CardTitle>
                <StarRating rating={review.rating} readonly size="sm" />
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs text-gray-400">
                  {new Date(review.createdAt).toLocaleDateString()}
                </span>
                <FlagButton
                  reviewId={review.id}
                  userId={currentUserId}
                  onFlag={handleFlag}
                />
              </div>
            </div>
          </CardHeader>
          
          <CardContent>
            <p className="text-sm text-gray-700 mb-3">
              {review.text || review.comment}
            </p>

            {/* Photos */}
            {review.photos && review.photos.length > 0 && (
              <div className="flex gap-2 mb-3 overflow-x-auto">
                {review.photos.map((photo, index) => (
                  <img
                    key={index}
                    src={photo}
                    alt={`Review photo ${index + 1}`}
                    className="w-24 h-24 object-cover rounded-md border border-gray-200"
                  />
                ))}
              </div>
            )}

            {/* Helpful Button */}
            <div className="mb-2">
              <HelpfulButton
                reviewId={review.id}
                initialCount={review.helpfulCount}
                initialVoted={currentUserId ? review.helpfulVoters.includes(currentUserId) : false}
                userId={currentUserId}
                onVote={handleVoteHelpful}
              />
            </div>

            {/* Replies Section */}
            <ReplySection
              reviewId={review.id}
              replies={review.replies || []}
              userId={currentUserId}
              userName={currentUserName}
              isOwner={isOwner}
              onReply={handleReply}
            />
          </CardContent>
        </Card>
      ))}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-6">
          <button
            onClick={() => setPage(p => Math.max(1, p - 1))}
            disabled={page === 1}
            className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Previous
          </button>
          <span className="px-4 py-2 text-sm text-gray-600">
            Page {page} of {totalPages}
          </span>
          <button
            onClick={() => setPage(p => p + 1)}
            disabled={!hasMore}
            className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default ReviewList;
