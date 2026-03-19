import axios from "axios";

const BACKEND_URL =
  process.env.NEXT_PUBLIC_BACKEND_URL ||
  process.env.NEXT_PUBLIC_API_URL ||
  "http://localhost:8080";

const api = axios.create({
  baseURL: BACKEND_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  if (typeof window !== "undefined" && localStorage) {
    const token = localStorage.getItem("authToken");
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

export const unwrapApiResponse = (resOrBody) => {
  // Handle both unwrapApiResponse(axiosRes) and unwrapApiResponse(axiosRes.data)
  const body = resOrBody?.data !== undefined && resOrBody?.status ? resOrBody.data : resOrBody;
  if (body && typeof body === "object" && "success" in body) {
    return body.success ? body.data : body;
  }
  return body;
};

export default api;
export { BACKEND_URL };
