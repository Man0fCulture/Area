import { apiClient } from './client';
import {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  OAuthProvider,
  OAuthInitRequest,
  OAuthInitResponse,
  LinkedAccount,
  Service,
  Area,
  CreateAreaRequest,
  UpdateAreaRequest,
  AreaExecution,
  AboutInfo,
  WebhookRegisterRequest,
  WebhookRegisterResponse,
} from '@/types';

export const authApi = {
  register: (data: RegisterRequest) =>
    apiClient.post<AuthResponse>('/api/auth/register', data),

  login: (data: LoginRequest) =>
    apiClient.post<AuthResponse>('/api/auth/login', data),

  refresh: (refreshToken: string) =>
    apiClient.post<AuthResponse>('/api/auth/refresh', { refreshToken }),

  oauth: {
    getProviders: () =>
      apiClient.get<{ providers: OAuthProvider[] }>('/api/auth/oauth/providers'),

    init: (data: OAuthInitRequest) =>
      apiClient.post<OAuthInitResponse>('/api/auth/oauth/init', data),

    link: (provider: string, accessToken: string) =>
      apiClient.post('/api/auth/oauth/link', { provider, accessToken }),

    unlink: (provider: string) =>
      apiClient.delete(`/api/auth/oauth/unlink`, { data: { provider } }),

    getLinkedAccounts: () =>
      apiClient.get<{ accounts: LinkedAccount[] }>('/api/auth/oauth/linked-accounts'),
  },
};

export const servicesApi = {
  getAll: () => apiClient.get<Service[]>('/api/services'),

  getById: (id: string) => apiClient.get<Service>(`/api/services/${id}`),
};

export const areasApi = {
  getAll: () => apiClient.get<Area[]>('/api/areas'),

  getById: (id: string) => apiClient.get<Area>(`/api/areas/${id}`),

  create: (data: CreateAreaRequest) =>
    apiClient.post<Area>('/api/areas', data),

  update: (id: string, data: UpdateAreaRequest) =>
    apiClient.patch<Area>(`/api/areas/${id}`, data),

  delete: (id: string) => apiClient.delete(`/api/areas/${id}`),

  activate: (id: string) => apiClient.post<Area>(`/api/areas/${id}/activate`),

  deactivate: (id: string) => apiClient.post<Area>(`/api/areas/${id}/deactivate`),

  test: (id: string) => apiClient.post(`/api/areas/${id}/test`),

  getExecutions: (id: string, limit?: number) =>
    apiClient.get<AreaExecution[]>(`/api/areas/${id}/executions`, { params: { limit } }),
};

export const webhooksApi = {
  trigger: (serviceId: string, hookId: string, data: unknown) =>
    apiClient.post(`/api/webhooks/${serviceId}/${hookId}`, data),
};

export const publicApi = {
  health: () => apiClient.get<{ status: string; timestamp: number }>('/health'),

  about: () => apiClient.get<AboutInfo>('/about.json'),
};
