import api from "@/utils/api";

export const handleflightbooking = async (userId, flightId, seats, price, date) => {
  try {
    let url = `/booking/flight?userId=${userId}&flightId=${flightId}&seats=${seats}&price=${price}`;
    if (date) {
      const dateStr = typeof date === 'string' ? date.substring(0, 10) : date;
      url += `&date=${dateStr}`;
    }
    const res = await api.post(url);
    return res.data;
  } catch (error) {
    console.log(error);
  }
};

export const handlehotelbooking = async (userId, hotelId, rooms, price, date) => {
  try {
    let url = `/booking/hotel?userId=${userId}&hotelId=${hotelId}&rooms=${rooms}&price=${price}`;
    if (date) {
      const dateStr = typeof date === 'string' ? date.substring(0, 10) : date;
      url += `&date=${dateStr}`;
    }
    const res = await api.post(url);
    return res.data;
  } catch (error) {
    console.log(error);
  }
};
