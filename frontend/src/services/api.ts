import axios, { AxiosError } from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  timeout: 10000,
  withCredentials: true // Important: include cookies in requests
});

// Token management
let currentToken: string | null = null;
let refreshTokenPromise: Promise<string | null> | null = null;

export function setAuthToken(token: string | null) {
  currentToken = token;
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common.Authorization;
  }
}

export function setRefreshCallback(callback: () => Promise<string | null>) {
  api.refreshTokenCallback = callback;
}

// Augment the axios instance type so the refresh callback can be stored on it.
declare module 'axios' {
  interface AxiosInstance {
    refreshTokenCallback?: (() => Promise<string | null>) | null;
  }
}

(api as any).refreshTokenCallback = null;

api.interceptors.request.use((config) => {
  if (currentToken) {
    config.headers.Authorization = `Bearer ${currentToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as any;

    // Only retry once to prevent infinite loops
    if (
      error.response?.status === 401 &&
      !originalRequest._retried &&
      (api as any).refreshTokenCallback
    ) {
      originalRequest._retried = true;

      try {
        // Prevent multiple refresh requests
        if (!refreshTokenPromise) {
          refreshTokenPromise = (api as any).refreshTokenCallback();
        }

        const newToken = await refreshTokenPromise;
        refreshTokenPromise = null;

        if (newToken) {
          setAuthToken(newToken);
          // Retry the original request with new token
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed, redirect to login
        localStorage.removeItem('user');
        localStorage.removeItem('tokenExpiry');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // Handle other 401 errors (no refresh token available)
    if (error.response?.status === 401) {
      localStorage.removeItem('user');
      localStorage.removeItem('tokenExpiry');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    return Promise.reject(error);
  }
);
