import axios from 'axios';

const defaultBaseURL = '/api';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? defaultBaseURL,
  timeout: 60000,
});

export function buildApiUrl(path: string, params?: Record<string, string>) {
  return apiClient.getUri({
    url: path,
    params,
  });
}

