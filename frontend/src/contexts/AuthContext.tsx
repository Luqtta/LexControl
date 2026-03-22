import { createContext, useContext, useMemo, useState, useEffect } from 'react';
import type { AuthResponse, User } from '../types';
import * as authService from '../services/auth';
import { setAuthToken, setRefreshCallback } from '../services/api';

type AuthContextValue = {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<AuthResponse>;
  register: (payload: authService.RegisterPayload) => Promise<{ message: string }>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<string | null>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function loadUser(): User | null {
  const raw = localStorage.getItem('user');
  if (!raw) return null;
  try {
    return JSON.parse(raw) as User;
  } catch {
    return null;
  }
}

function loadTokenExpiry(): number | null {
  const raw = localStorage.getItem('tokenExpiry');
  if (!raw) return null;
  return parseInt(raw, 10);
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(loadUser());
  const [tokenExpiry, setTokenExpiry] = useState<number | null>(loadTokenExpiry());

  // Initialize from localStorage on mount (for page refresh)
  useEffect(() => {
    const savedUser = loadUser();
    if (savedUser) {
      setUser(savedUser);
      const expiry = loadTokenExpiry();
      setTokenExpiry(expiry);
      // Note: token is not recovered from storage, user must authenticate
    }
  }, []);

  // Update API token when token changes
  useEffect(() => {
    setAuthToken(token);
  }, [token]);

  // Register refresh token callback for API interceptor
  useEffect(() => {
    const refreshTokenFn = async () => {
      try {
        const newResponse = await authService.refreshAccessToken();
        setToken(newResponse.accessToken);
        setTokenExpiry(newResponse.expiresAt);
        localStorage.setItem('tokenExpiry', newResponse.expiresAt.toString());
        return newResponse.accessToken;
      } catch (error) {
        // If refresh fails, logout
        await logout();
        return null;
      }
    };

    setRefreshCallback(refreshTokenFn);
  }, []);

  // Check and refresh token if needed
  useEffect(() => {
    if (!tokenExpiry) return;

    const checkRefresh = setInterval(() => {
      const now = Date.now() / 1000;
      // Refresh if token will expire in less than 2 minutes
      if (tokenExpiry - now < 120) {
        refreshToken();
      }
    }, 30000); // Check every 30 seconds

    return () => clearInterval(checkRefresh);
  }, [tokenExpiry]);

  const login = async (email: string, password: string) => {
    const response = await authService.login({ email, password });
    setToken(response.accessToken);
    setUser(response.user);
    setTokenExpiry(response.expiresAt);
    localStorage.setItem('user', JSON.stringify(response.user));
    localStorage.setItem('tokenExpiry', response.expiresAt.toString());
    return response;
  };

  const register = (payload: authService.RegisterPayload) => authService.register(payload);

  const logout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      // Even if logout fails on server, clear local state
      console.error('Logout error:', error);
    }
    setToken(null);
    setUser(null);
    setTokenExpiry(null);
    localStorage.removeItem('user');
    localStorage.removeItem('tokenExpiry');
  };

  const refreshToken = async (): Promise<string | null> => {
    try {
      const newResponse = await authService.refreshAccessToken();
      setToken(newResponse.accessToken);
      setTokenExpiry(newResponse.expiresAt);
      localStorage.setItem('tokenExpiry', newResponse.expiresAt.toString());
      return newResponse.accessToken;
    } catch (error) {
      // If refresh fails, logout
      await logout();
      return null;
    }
  };

  const value = useMemo(
    () => ({ user, token, login, register, logout, refreshToken }),
    [user, token, tokenExpiry]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
