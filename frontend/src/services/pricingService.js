import api from "@/utils/api";

export const getDynamicPrice = async (itemId, itemType) => {
  try {
    const res = await api.get(`/api/pricing/${itemType}/${itemId}`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getPriceHistory = async (itemId, itemType) => {
  try {
    const res = await api.get(`/api/pricing/history/${itemType}/${itemId}`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};
