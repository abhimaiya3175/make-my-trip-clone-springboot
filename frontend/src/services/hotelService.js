import api, { unwrapApiResponse } from "@/utils/api";

export const gethotel = async () => {
  try {
    const res = await api.get(`/api/hotel`);
    return res.data;
  } catch (error) {
    console.log(error);
    return [];
  }
};

export const getHotelById = async (id) => {
  const res = await api.get(`/api/hotel/${id}`);
  return unwrapApiResponse(res);
};

export const addhotel = async (hotelName, location, pricePerNight, availableRooms, amenities) => {
  try {
    const res = await api.post(`/api/admin/hotel`, {
      hotelName,
      location,
      pricePerNight,
      availableRooms,
      amenities,
    });
    return res.data;
  } catch (error) {
    console.log(error);
  }
};

export const edithotel = async (id, hotelName, location, pricePerNight, availableRooms, amenities) => {
  try {
    const res = await api.put(`/api/admin/hotel/${id}`, {
      hotelName,
      location,
      pricePerNight,
      availableRooms,
      amenities,
    });
    return res.data;
  } catch (error) {
    console.log(error);
  }
};
