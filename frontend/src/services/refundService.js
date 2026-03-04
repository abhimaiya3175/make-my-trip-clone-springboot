import api from "@/utils/api";

export const getCancellationReasons = async () => {
  try {
    const res = await api.get(`/api/cancellation/reasons`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getRefundStatuses = async () => {
  try {
    const res = await api.get(`/api/cancellation/refund-statuses`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getCancellationPreview = async (
  bookingId, bookingType, quantityToCancel, totalQuantity, originalPrice, travelDateTimeString
) => {
  try {
    console.log("[API] getCancellationPreview request:", {
      bookingId, bookingType, quantityToCancel, totalQuantity, originalPrice, travelDateTimeString,
    });
    const res = await api.get(`/api/cancellation/preview`, {
      params: {
        bookingId, bookingType, quantityToCancel, totalQuantity, originalPrice, travelDateTimeString,
      },
    });
    console.log("[API] getCancellationPreview response:", res.data);
    return res.data;
  } catch (error) {
    console.error("[API] getCancellationPreview error:", error?.response?.data || error);
    throw error;
  }
};

export const processCancellation = async (
  userId, cancellationRequest, totalQuantity, originalPrice, travelDateTimeString
) => {
  try {
    console.log("[API] processCancellation request:", {
      userId, cancellationRequest, totalQuantity, originalPrice, travelDateTimeString,
    });
    const res = await api.post(
      `/api/cancellation/cancel`,
      cancellationRequest,
      {
        params: { totalQuantity, originalPrice, travelDateTimeString },
        headers: { "X-User-ID": userId },
      }
    );
    console.log("[API] processCancellation response:", res.data);
    return res.data;
  } catch (error) {
    console.error("[API] processCancellation error:", error?.response?.data || error);
    throw error;
  }
};

export const getRefundStatus = async (cancellationId) => {
  try {
    const res = await api.get(`/api/cancellation/refund-status/${cancellationId}`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getUserCancellations = async (userId) => {
  try {
    if (!userId) {
      console.error("[API] getUserCancellations called with empty userId!", { userId, type: typeof userId });
      throw new Error("userId is required");
    }
    
    const url = `/api/cancellation/user/${userId}/cancellations`;
    console.log("[API] getUserCancellations request", {
      userId, userIdType: typeof userId, fullUrl: url,
    });
    
    const res = await api.get(url);
    console.log("[API] getUserCancellations SUCCESS. Response:", res.data);
    return res.data;
  } catch (error) {
    console.error("[API] getUserCancellations FAILED", {
      userId, url: error?.response?.config?.url, status: error?.response?.status,
      statusText: error?.response?.statusText, data: error?.response?.data, message: error.message,
    });
    throw error;
  }
};

export const getCancellationDetails = async (cancellationId) => {
  try {
    const res = await api.get(`/api/cancellation/${cancellationId}`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};
