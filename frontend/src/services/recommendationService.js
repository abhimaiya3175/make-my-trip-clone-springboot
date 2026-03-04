import api from "@/utils/api";

export const getRecommendations = async (userId) => {
  try {
    const res = await api.get(`/api/recommendations/user/${userId}`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getSimilarItems = async (itemType, itemId) => {
  try {
    const res = await api.get(`/api/recommendations/similar/${itemType}/${itemId}`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};
