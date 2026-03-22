import axios from "axios";

const EXPLICIT_BACKEND_URL =
  process.env.NEXT_PUBLIC_BACKEND_URL || process.env.NEXT_PUBLIC_API_URL;

// In browser, use Next.js rewrite proxy by default to avoid CORS issues.
const BACKEND_URL =
  EXPLICIT_BACKEND_URL ||
  (typeof window === "undefined" ? "http://localhost:8080" : "/backend-api");

const api = axios.create({
  baseURL: BACKEND_URL,
  timeout: 15000,
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
    if (!error?.response && (error?.code === "ERR_NETWORK" || error?.message === "Network Error")) {
      const base = BACKEND_URL || "<empty baseURL>";
      const url = error?.config?.url || "<unknown endpoint>";
      error.message = `Network Error: unable to reach backend (${base}${url}). Ensure backend is running and frontend is restarted after config changes.`;
    }

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
    if (!body.success) {
      return body;
    }
    // Some endpoints return { success: true, ...payload } (without nested data).
    return body.data !== undefined ? body.data : body;
  }
  return body;
};

export default api;
export { BACKEND_URL };

