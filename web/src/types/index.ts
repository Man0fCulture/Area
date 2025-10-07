export interface User {
  id: string;
  email: string;
  username: string;
  createdAt: number;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface OAuthProvider {
  name: string;
  displayName: string;
  enabled: boolean;
  iconUrl?: string;
}

export interface OAuthInitRequest {
  provider: string;
  redirectUri?: string;
}

export interface OAuthInitResponse {
  authUrl: string;
  state: string;
}

export interface LinkedAccount {
  provider: string;
  email?: string;
  linkedAt: string;
  displayName: string;
}

export interface Parameter {
  name: string;
  type: string;
  required: boolean;
  description?: string;
}

export interface Action {
  id: string;
  name: string;
  description: string;
  parameters: Parameter[];
}

export interface Reaction {
  id: string;
  name: string;
  description: string;
  parameters: Parameter[];
}

export interface Service {
  id: string;
  name: string;
  displayName: string;
  description: string;
  category: string;
  requiresAuth: boolean;
  enabled: boolean;
  actions: Action[];
  reactions: Reaction[];
}

export interface AreaAction {
  serviceId: string;
  actionId: string;
  config: Record<string, unknown>;
}

export interface AreaReaction {
  serviceId: string;
  reactionId: string;
  config: Record<string, unknown>;
}

export interface Area {
  id: string;
  userId: string;
  name: string;
  description?: string;
  action: AreaAction;
  reactions: AreaReaction[];
  active: boolean;
  executionCount?: number;
  lastTriggeredAt?: number;
  createdAt: number;
  updatedAt: number;
}

export interface CreateAreaRequest {
  name: string;
  description?: string;
  action: AreaAction;
  reactions: AreaReaction[];
}

export interface UpdateAreaRequest {
  name?: string;
  description?: string;
  active?: boolean;
}

export interface AreaExecution {
  id: string;
  areaId: string;
  status: 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'FAILED';
  startedAt: number;
  completedAt?: number;
  actionData?: Record<string, unknown>;
  error?: string | null;
}

export interface DashboardStats {
  activeAreasCount: number;
  executionsToday: number;
  connectedServicesCount: number;
  lastActivity?: string;
}

export interface AboutInfo {
  client: {
    host: string;
  };
  server: {
    current_time: number;
    services: Array<{
      name: string;
      actions: Array<{
        name: string;
        description: string;
      }>;
      reactions: Array<{
        name: string;
        description: string;
      }>;
    }>;
  };
}

export interface WebhookRegisterRequest {
  areaId: string;
}

export interface WebhookRegisterResponse {
  webhookId: string;
  webhookUrl: string;
}

export interface ApiError {
  message: string;
  statusCode: number;
  errors?: Record<string, string[]>;
}
