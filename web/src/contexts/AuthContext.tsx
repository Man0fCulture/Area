'use client';

import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { User, LoginRequest, RegisterRequest } from '@/types';
import { authApi } from '@/api';
import { setTokens, clearTokens, getAccessToken } from '@/api/client';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';
import { AxiosError } from 'axios';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  setUserFromToken: (token: string) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const parseJwt = (token: string): User | null => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    const payload = JSON.parse(jsonPayload);
    return {
      id: payload.sub || payload.userId || '',
      email: payload.email || '',
      username: payload.username || payload.name || '',
      createdAt: payload.createdAt || Date.now(),
    };
  } catch (error) {
    console.error('Error parsing JWT:', error);
    return null;
  }
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const token = getAccessToken();
    if (token) {
      const decodedUser = parseJwt(token);
      setUser(decodedUser);
    }
    setIsLoading(false);
  }, []);

  const login = async (data: LoginRequest) => {
    try {
      const response = await authApi.login(data);
      const { accessToken, refreshToken } = response.data;
      setTokens(accessToken, refreshToken);

      const decodedUser = parseJwt(accessToken);
      setUser(decodedUser);

      toast.success('Connexion réussie !');
      router.push('/dashboard');
    } catch (error) {
      console.error('Login error:', error);
      if (error instanceof AxiosError) {
        const errorMessage = error.response?.data?.message ||
                            error.response?.data?.error ||
                            'Erreur de connexion. Vérifiez vos identifiants.';
        toast.error(errorMessage);
      } else {
        toast.error('Erreur de connexion. Vérifiez vos identifiants.');
      }
      throw error;
    }
  };

  const register = async (data: RegisterRequest) => {
    try {
      const response = await authApi.register(data);
      const { accessToken, refreshToken } = response.data;
      setTokens(accessToken, refreshToken);

      const decodedUser = parseJwt(accessToken);
      setUser(decodedUser);

      toast.success('Compte créé avec succès !');
      router.push('/dashboard');
    } catch (error) {
      console.error('Register error:', error);
      if (error instanceof AxiosError) {
        console.error('Error response:', error.response?.data);
        const errorMessage = error.response?.data?.message ||
                            error.response?.data?.error ||
                            'Erreur lors de la création du compte.';
        toast.error(errorMessage);
      } else {
        toast.error('Erreur lors de la création du compte.');
      }
      throw error;
    }
  };

  const logout = useCallback(() => {
    clearTokens();
    setUser(null);
    toast.success('Déconnexion réussie');
    router.push('/login');
  }, [router]);

  const setUserFromToken = useCallback((token: string) => {
    const decodedUser = parseJwt(token);
    setUser(decodedUser);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        setUserFromToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
