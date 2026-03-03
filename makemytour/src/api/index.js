import axios from "axios";

const BACKEND_URL = "http://localhost:8080";

export const login = async (email, password) => {
  try {
    const url = `${BACKEND_URL}/user/login?email=${email}&password=${password}`;
    const res = await axios.post(url);
    const data = res.data;
    // console.log(data);
    return data;
  } catch (error) {
    throw error;
  }
};

export const signup = async (
  firstName,
  lastName,
  email,
  phoneNumber,
  password
) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/user/signup`, {
      firstName,
      lastName,
      email,
      phoneNumber,
      password,
    });
    const data = res.data;
    // console.log(data);
    return data;
  } catch (error) {
    throw error;
  }
};

export const getuserbyemail = async (email) => {
  try {
    const res = await axios.get(`${BACKEND_URL}/user/email?email=${email}`);
    const data = res.data;
    return data;
  } catch (error) {
    throw error;
  }
};

export const editprofile = async (
  id,
  firstName,
  lastName,
  email,
  phoneNumber
) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/user/edit?id=${id}`, {
      firstName,
      lastName,
      email,
      phoneNumber,
    });
    const data = res.data;
    return data;
  } catch (error) {}
};
export const getflight = async () => {
  try {
    const res = await axios.get(`${BACKEND_URL}/flight`);
    const data = res.data;
    return data;
  } catch (error) {
    console.log(data);
  }
};

export const addflight = async (
  flightName,
  from,
  to,
  departureTime,
  arrivalTime,
  price,
  availableSeats
) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/admin/flight`, {
      flightName,
      from,
      to,
      departureTime,
      arrivalTime,
      price,
      availableSeats,
    });
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const editflight = async (
  id,
  flightName,
  from,
  to,
  departureTime,
  arrivalTime,
  price,
  availableSeats
) => {
  try {
    const res = await axios.put(`${BACKEND_URL}/admin/flight/${id}`, {
      flightName,
      from,
      to,
      departureTime,
      arrivalTime,
      price,
      availableSeats,
    });
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const gethotel = async () => {
  try {
    const res = await axios.get(`${BACKEND_URL}/hotel`);
    const data = res.data;
    return data;
  } catch (error) {
    console.log(data);
  }
};

export const addhotel = async (
  hotelName,
  location,
  pricePerNight,
  availableRooms,
  amenities
) => {
  try {
    const res = await axios.post(`${BACKEND_URL}/admin/hotel`, {
      hotelName,
      location,
      pricePerNight,
      availableRooms,
      amenities,
    });
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const edithotel = async (
  id,
  hotelName,
  location,
  pricePerNight,
  availableRooms,
  amenities
) => {
  try {
    const res = await axios.put(`${BACKEND_URL}/admin/hotel/${id}`, {
      hotelName,
      location,
      pricePerNight,
      availableRooms,
      amenities,
    });
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const handleflightbooking = async (userId, flightId, seats, price, date) => {
  try {
    let url = `${BACKEND_URL}/booking/flight?userId=${userId}&flightId=${flightId}&seats=${seats}&price=${price}`;
    if (date) {
      // Extract YYYY-MM-DD from ISO string or datetime to avoid timezone issues
      const dateStr = typeof date === 'string' ? date.substring(0, 10) : date;
      url += `&date=${dateStr}`;
    }
    const res = await axios.post(url);
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

export const handlehotelbooking = async (userId, hotelId, rooms, price, date) => {
  try {
    let url = `${BACKEND_URL}/booking/hotel?userId=${userId}&hotelId=${hotelId}&rooms=${rooms}&price=${price}`;
    if (date) {
      const dateStr = typeof date === 'string' ? date.substring(0, 10) : date;
      url += `&date=${dateStr}`;
    }
    const res = await axios.post(url);
    const data = res.data;
    return data;
  } catch (error) {
    console.log(error);
  }
};

// ===================== CANCELLATION & REFUND APIs =====================

export const getCancellationReasons = async () => {
  try {
    const res = await axios.get(`${BACKEND_URL}/api/cancellation/reasons`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getRefundStatuses = async () => {
  try {
    const res = await axios.get(`${BACKEND_URL}/api/cancellation/refund-statuses`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getCancellationPreview = async (
  bookingId,
  bookingType,
  quantityToCancel,
  totalQuantity,
  originalPrice,
  travelDateTimeString
) => {
  try {
    console.log("[API] getCancellationPreview request:", {
      bookingId,
      bookingType,
      quantityToCancel,
      totalQuantity,
      originalPrice,
      travelDateTimeString,
    });
    const res = await axios.get(`${BACKEND_URL}/api/cancellation/preview`, {
      params: {
        bookingId,
        bookingType,
        quantityToCancel,
        totalQuantity,
        originalPrice,
        travelDateTimeString,
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
  userId,
  cancellationRequest,
  totalQuantity,
  originalPrice,
  travelDateTimeString
) => {
  try {
    console.log("[API] processCancellation request:", {
      userId,
      cancellationRequest,
      totalQuantity,
      originalPrice,
      travelDateTimeString,
    });
    const res = await axios.post(
      `${BACKEND_URL}/api/cancellation/cancel`,
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
    const res = await axios.get(
      `${BACKEND_URL}/api/cancellation/refund-status/${cancellationId}`
    );
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
    
    const url = `${BACKEND_URL}/api/cancellation/user/${userId}/cancellations`;
    console.log("[API] getUserCancellations request", {
      userId,
      userIdType: typeof userId,
      fullUrl: url,
      backendUrl: BACKEND_URL
    });
    
    const res = await axios.get(url);
    console.log("[API] getUserCancellations SUCCESS. Response:", res.data);
    return res.data;
  } catch (error) {
    console.error("[API] getUserCancellations FAILED", {
      userId,
      url: error?.response?.config?.url,
      status: error?.response?.status,
      statusText: error?.response?.statusText,
      data: error?.response?.data,
      message: error.message
    });
    throw error;
  }
};

export const getCancellationDetails = async (cancellationId) => {
  try {
    const res = await axios.get(
      `${BACKEND_URL}/api/cancellation/${cancellationId}`
    );
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};
