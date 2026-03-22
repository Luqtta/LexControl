import axios, { AxiosError } from 'axios';

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  code: string;
  error: string;
  message: string;
  path: string;
  validationErrors: Array<{
    field: string;
    message: string;
  }>;
}

export class ApiErrorHandler {
  /**
   * Extract meaningful error message from API or network error
   */
  static getErrorMessage(error: unknown): string {
    if (!error) return 'An unknown error occurred';

    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ApiErrorResponse>;

      // Network error (no response from server)
      if (axiosError.code === 'ERR_NETWORK') {
        return 'Network error: unable to connect to the server';
      }

      // Timeout
      if (axiosError.code === 'ECONNABORTED') {
        return 'Request timeout: server took too long to respond';
      }

      // API returned error response
      if (axiosError.response?.data) {
        const data = axiosError.response.data;
        return data.message || data.error || 'Server error';
      }

      // HTTP error without data
      if (axiosError.response) {
        return `Server error (${axiosError.response.status}): ${axiosError.response.statusText}`;
      }

      return axiosError.message || 'Request failed';
    }

    if (error instanceof Error) {
      return error.message;
    }

    return String(error);
  }

  /**
   * Get error code to handle specific cases
   */
  static getErrorCode(error: unknown): string | null {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ApiErrorResponse>;
      return axiosError.response?.data?.code || null;
    }
    return null;
  }

  /**
   * Check if error is a specific type
   */
  static isErrorCode(error: unknown, code: string): boolean {
    return this.getErrorCode(error) === code;
  }

  /**
   * Check if error is a validation error (422)
   */
  static isValidationError(error: unknown): boolean {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ApiErrorResponse>;
      return axiosError.response?.status === 422;
    }
    return false;
  }

  /**
   * Check if error is authentication error (401)
   */
  static isAuthError(error: unknown): boolean {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ApiErrorResponse>;
      return axiosError.response?.status === 401;
    }
    return false;
  }

  /**
   * Check if error is too many requests (429)
   */
  static isRateLimited(error: unknown): boolean {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ApiErrorResponse>;
      return axiosError.response?.status === 429;
    }
    return false;
  }

  /**
   * Get all validation errors
   */
  static getValidationErrors(
    error: unknown
  ): Array<{ field: string; message: string }> {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<ApiErrorResponse>;
      return axiosError.response?.data?.validationErrors || [];
    }
    return [];
  }
}
