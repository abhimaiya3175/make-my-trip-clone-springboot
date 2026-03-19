import api from "@/utils/api";

const extractErrorMessage = (error, fallbackMessage) => {
  const data = error?.response?.data;
  if (typeof data === "string" && data.trim()) {
    return data;
  }
  if (data?.message) {
    return data.message;
  }
  if (data?.error?.message) {
    return data.error.message;
  }
  if (error?.message) {
    return error.message;
  }
  return fallbackMessage;
};

export const login = async (email, password) => {
  try {
    const res = await api.post(`/user/login`, { email, password });
    const payload = res.data;
    if (payload?.token && typeof window !== "undefined" && localStorage) {
      localStorage.setItem("authToken", payload.token);
    }
    return payload?.user || null;
  } catch (error) {
    console.log(error);
    throw new Error(extractErrorMessage(error, "Login failed. Please try again."));
  }
};

export const signup = async (firstName, lastName, email, phoneNumber, password) => {
  try {
    const res = await api.post(`/user/signup`, { firstName, lastName, email, phoneNumber, password });
    return res.data;
  } catch (error) {
    console.log(error);
    throw new Error(extractErrorMessage(error, "Signup failed. Please try again."));
  }
};

export const getuserbyemail = async (email) => {
  try {
    const res = await api.get(`/user/email?email=${email}`);
    return res.data;
  } catch (error) {
    console.log(error);
    return null;
  }
};

export const editprofile = async (id, firstName, lastName, email, phoneNumber) => {
  try {
    const res = await api.post(`/user/edit?id=${encodeURIComponent(id)}`, { firstName, lastName, email, phoneNumber });
    return res.data;
  } catch (error) {
    console.log(error);
    return null;
  }
};
