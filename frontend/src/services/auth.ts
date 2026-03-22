import { api } from './api';
import type { AuthResponse } from '../types';
import { ApiErrorHandler } from '../utils/errorHandler';

export type RegisterPayload = {
  name: string;
  email: string;
  password: string;
};

export type LoginPayload = {
  email: string;
  password: string;
};

export async function login(payload: LoginPayload): Promise<AuthResponse> {
  try {
    const { data } = await api.post<AuthResponse>('/auth/login', payload);
    return data;
  } catch (error) {
    // Re-throw with handled error message
    const message = ApiErrorHandler.getErrorMessage(error);
    const err = new Error(message);
    (err as any).code = ApiErrorHandler.getErrorCode(error);
    throw err;
  }
}

export async function register(payload: RegisterPayload): Promise<{ message: string }> {
  try {
    const { data } = await api.post<{ message: string }>('/auth/register', payload);
    return data;
  } catch (error) {
    // Re-throw with handled error message
    const message = ApiErrorHandler.getErrorMessage(error);
    const err = new Error(message);
    (err as any).code = ApiErrorHandler.getErrorCode(error);
    throw err;
  }
}

export async function logout(): Promise<void> {
  try {
    await api.post('/auth/logout');
  } catch (error) {
    // Log but don't throw - logout should always succeed on frontend
    console.error('Logout request failed:', error);
  }
}

export async function refreshAccessToken(): Promise<AuthResponse> {
  try {
    // Backend automatically reads refreshToken cookie
    const { data } = await api.post<AuthResponse>('/auth/refresh', {});
    return data;
  } catch (error) {
    // Re-throw with handled error message
    const message = ApiErrorHandler.getErrorMessage(error);
    const err = new Error(message);
    (err as any).code = ApiErrorHandler.getErrorCode(error);
    throw err;
  }
}
