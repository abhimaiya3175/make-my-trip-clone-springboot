export const REQUEST_STATE = {
  IDLE: "idle",
  LOADING: "loading",
  SUCCESS: "success",
  ERROR: "error",
};

export const createRequestState = (state = REQUEST_STATE.IDLE, error = null) => ({
  state,
  error,
});

export const getErrorMessage = (error, fallback = "Something went wrong") => {
  return (
    error?.response?.data?.error?.message ||
    error?.response?.data?.error ||
    error?.response?.data?.message ||
    error?.message ||
    fallback
  );
};
