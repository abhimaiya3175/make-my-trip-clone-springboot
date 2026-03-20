import api, { unwrapApiResponse } from "@/utils/api";

export const getflight = async () => {
  try {
    const res = await api.get(`/api/flight`);
    return unwrapApiResponse(res);
  } catch (error) {
    console.log(error);
    return [];
  }
};

export const getFlightById = async (id) => {
  const res = await api.get(`/api/flight/${id}`);
  return unwrapApiResponse(res);
};

export const addflight = async (flightName, from, to, departureTime, arrivalTime, price, availableSeats) => {
  try {
    const res = await api.post(`/api/admin/flight`, {
      flightName,
      from,
      to,
      departureTime,
      arrivalTime,
      price,
      availableSeats,
    });
    return res.data;
  } catch (error) {
    console.log(error);
  }
};

export const editflight = async (id, flightName, from, to, departureTime, arrivalTime, price, availableSeats) => {
  try {
    const res = await api.put(`/api/admin/flight/${id}`, {
      flightName,
      from,
      to,
      departureTime,
      arrivalTime,
      price,
      availableSeats,
    });
    return res.data;
  } catch (error) {
    console.log(error);
  }
};
