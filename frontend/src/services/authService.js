import api from "@/utils/api";

export const login = async (email, password) => {
  try {
    const res = await api.post(`/user/login?email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`);
    return res.data;
  } catch (error) {
    console.log(error);
    return null;
  }
};

export const signup = async (firstName, lastName, email, phoneNumber, password) => {
  try {
    const res = await api.post(`/user/signup`, { firstName, lastName, email, phoneNumber, password });
    return res.data;
  } catch (error) {
    console.log(error);
    return null;
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
