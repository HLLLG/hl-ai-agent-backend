import axios from 'axios';

const defaultBaseURL = '/api';
const envBaseURL = import.meta.env.VITE_API_BASE_URL?.trim();
const baseURL = import.meta.env.PROD ? defaultBaseURL : envBaseURL || defaultBaseURL;

export const apiClient = axios.create({
  baseURL,
  timeout: 60000,
});

export function buildApiUrl(path: string, params?: Record<string, string>) {
  return apiClient.getUri({
    url: path,
    params,
  });
}

