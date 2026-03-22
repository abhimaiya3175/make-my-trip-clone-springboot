import api, { unwrapApiResponse } from "@/utils/api";

// ======== Phase 1: Enhanced Review APIs ========

const normalizeEntityType = (value) => {
  if (!value) return value;
  return String(value).trim().toUpperCase();
};

/**
 * Create a new review
 * @param {string} userId - User ID (from auth)
 * @param {string} userName - User name (from auth)
 * @param {object} reviewData - { entityId, entityType, rating, text, photos[] }
 * @returns {Promise<Review>}
 */
export const createReview = async (userId, userName, reviewData) => {
  try {
    const payload = {
      ...reviewData,
      entityType: normalizeEntityType(reviewData?.entityType),
    };
    const res = await api.post(`/api/reviews`, payload);
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Create review error:', error);
    throw error;
  }
};

/**
 * Get paginated and sorted reviews for an entity
 * @param {object} params - { entityType, entityId, sortBy, page, size }
 * @returns {Promise<PageResponse<Review>>}
 */
export const getReviews = async ({ entityType, entityId, sortBy = 'latest', page = 1, size = 10 }) => {
  try {
    const res = await api.get(`/api/reviews`, {
      params: { entityType: normalizeEntityType(entityType), entityId, sortBy, page, size }
    });
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Get reviews error:', error);
    throw error;
  }
};

/**
 * Get a single review by ID
 * @param {string} reviewId - Review ID
 * @returns {Promise<Review>}
 */
export const getReviewById = async (reviewId) => {
  try {
    const res = await api.get(`/api/reviews/${reviewId}`);
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Get review by ID error:', error);
    throw error;
  }
};

/**
 * Update an existing review
 * @param {string} reviewId - Review ID
 * @param {string} userId - User ID
 * @param {object} reviewData - { entityId, entityType, rating, text, photos[] }
 * @returns {Promise<Review>}
 */
export const updateReview = async (reviewId, userId, reviewData) => {
  try {
    const payload = {
      ...reviewData,
      entityType: normalizeEntityType(reviewData?.entityType),
    };
    const res = await api.put(`/api/reviews/${reviewId}`, payload);
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Update review error:', error);
    throw error;
  }
};

/**
 * Vote a review as helpful (toggle)
 * @param {string} reviewId - Review ID
 * @param {string} userId - User ID
 * @returns {Promise<Review>}
 */
export const voteHelpful = async (reviewId, userId) => {
  try {
    const res = await api.put(`/api/reviews/${reviewId}/helpful`, {});
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Vote helpful error:', error);
    throw error;
  }
};

/**
 * Flag a review as inappropriate
 * @param {string} reviewId - Review ID
 * @param {string} userId - User ID
 * @param {string} reason - Flag reason
 * @returns {Promise<Review>}
 */
export const flagReview = async (reviewId, userId, reason) => {
  try {
    const res = await api.post(`/api/reviews/${reviewId}/flag`, { reason });
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Flag review error:', error);
    throw error;
  }
};

/**
 * Reply to a review
 * @param {string} reviewId - Review ID
 * @param {string} userId - User ID
 * @param {object} replyData - { text, userName, isOwner }
 * @returns {Promise<Review>}
 */
export const replyToReview = async (reviewId, userId, replyData) => {
  try {
    const res = await api.post(`/api/reviews/${reviewId}/reply`, replyData);
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Reply to review error:', error);
    throw error;
  }
};

/**
 * Delete a review
 * @param {string} reviewId - Review ID
 * @param {string} userId - User ID
 * @returns {Promise<void>}
 */
export const deleteReview = async (reviewId, userId) => {
  try {
    await api.delete(`/api/reviews/${reviewId}`);
  } catch (error) {
    console.error('Delete review error:', error);
    throw error;
  }
};

// ======== Legacy APIs (for backward compatibility) ========

export const addReview = async (review) => {
  try {
    const res = await api.post(`/api/reviews`, review);
    return unwrapApiResponse(res);
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getReviewsByBookingId = async (bookingId) => {
  try {
    const res = await api.get(`/api/reviews/booking/${bookingId}`);
    return unwrapApiResponse(res);
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getReviewsByUserId = async (userId) => {
  try {
    const res = await api.get(`/api/reviews/user/${userId}`);
    return unwrapApiResponse(res);
  } catch (error) {
    console.log(error);
    throw error;
  }
};
