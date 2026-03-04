import api from "@/utils/api";

export const gethotel = async () => {
  try {
    const res = await api.get(`/hotel`);
    return res.data;
  } catch (error) {
    console.log(error);
  }
};

export const addhotel = async (hotelName, location, pricePerNight, availableRooms, amenities) => {
  try {
    const res = await api.post(`/admin/hotel`, {
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
    const res = await api.put(`/admin/hotel/${id}`, {
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
