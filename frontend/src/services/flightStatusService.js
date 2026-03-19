import api, { unwrapApiResponse } from "../utils/api";

export const getFlightStatus = async (flightId) => {
  const response = await api.get(`/api/flight-status/${flightId}`);
  return unwrapApiResponse(response.data);
};

export const getFlightTimeline = async (flightId) => {
  const response = await api.get(`/api/flight-status/${flightId}/timeline`);
  return unwrapApiResponse(response.data);
};

export const listFlightStatuses = async (page = 0, size = 10) => {
  const response = await api.get(`/api/flight-status?page=${page}&size=${size}`);
  return unwrapApiResponse(response.data);
};
