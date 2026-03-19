import api, { unwrapApiResponse } from "@/utils/api";

export const getRecommendations = async (userId, itemType) => {
  const params = itemType ? `?itemType=${itemType}` : "";
  const res = await api.get(`/api/recommendations/user/${userId}${params}`);
  return unwrapApiResponse(res.data);
};

export const getSimilarItems = async (itemType, itemId) => {
  const res = await api.get(`/api/recommendations/similar/${itemType}/${itemId}`);
  return unwrapApiResponse(res.data);
};

export const recordEvent = async (userId, eventType, entityId, entityType, metadata) => {
  const res = await api.post("/api/recommendations/events", {
    userId,
    eventType,
    entityId,
    entityType,
    metadata,
  });
  return unwrapApiResponse(res.data);
};

export const submitFeedback = async (userId, itemId, itemType, feedbackType) => {
  const res = await api.post("/api/recommendations/feedback", {
    userId,
    itemId,
    itemType,
    feedbackType,
  });
  return unwrapApiResponse(res.data);
};

export const getExplanation = async (itemId, userId) => {
  const res = await api.get(`/api/recommendations/${itemId}/explain?userId=${userId}`);
  return unwrapApiResponse(res.data);
};
