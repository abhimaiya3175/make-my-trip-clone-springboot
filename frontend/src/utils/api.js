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


api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      const hadToken =
        typeof window !== "undefined" && localStorage
          ? Boolean(localStorage.getItem("authToken"))
          : false;

      if (typeof window !== "undefined" && localStorage) {
        localStorage.removeItem("authToken");
        localStorage.removeItem("user");
      }
      if (
        hadToken &&
        typeof window !== "undefined" &&
        !window.location.pathname.includes("/auth/") &&
        window.location.pathname !== "/"
      ) {
        window.location.href = "/";
      }
    }
    return Promise.reject(error);
  }
);
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

