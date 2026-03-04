import api from "@/utils/api";

export const addReview = async (review) => {
  try {
    const res = await api.post(`/api/reviews`, review);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getReviewsByBookingId = async (bookingId) => {
  try {
    const res = await api.get(`/api/reviews/booking/${bookingId}`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getReviewsByUserId = async (userId) => {
  try {
    const res = await api.get(`/api/reviews/user/${userId}`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const deleteReview = async (id) => {
  try {
    await api.delete(`/api/reviews/${id}`);
  } catch (error) {
    console.log(error);
    throw error;
  }
};
