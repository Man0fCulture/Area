'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Link from 'next/link';
import { useAuth } from '@/contexts/AuthContext';
import { RegisterRequest, OAuthProvider } from '@/types';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Card } from '@/components/common/Card';
import { Mail, Lock, User, Loader2 } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { authApi } from '@/api';

const registerSchema = z.object({
  email: z.string().email('Email invalide'),
  password: z.string().min(8, 'Le mot de passe doit contenir au moins 8 caractères'),
  confirmPassword: z.string(),
  username: z.string().min(3, 'Le nom d\'utilisateur doit contenir au moins 3 caractères'),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Les mots de passe ne correspondent pas',
  path: ['confirmPassword'],
});

type RegisterFormData = z.infer<typeof registerSchema>;

export default function RegisterPage() {
  const { register: registerUser } = useAuth();
  const [isLoading, setIsLoading] = useState(false);

  const { data: providersData } = useQuery({
    queryKey: ['oauth-providers'],
    queryFn: async () => {
      const response = await authApi.oauth.getProviders();
      return response.data;
    },
  });

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);
    try {
      const registerData: RegisterRequest = {
        email: data.email,
        password: data.password,
        username: data.username,
      };
      await registerUser(registerData);
    } catch (error) {
      console.error('Registration failed:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleOAuthLogin = async (provider: OAuthProvider) => {
    try {
      const response = await authApi.oauth.init({
        provider: provider.name,
        redirectUri: `${window.location.origin}/auth/callback/${provider.name}`,
      });

      const width = 500;
      const height = 600;
      const left = window.screen.width / 2 - width / 2;
      const top = window.screen.height / 2 - height / 2;

      window.open(
        response.data.authUrl,
        'OAuth Login',
        `width=${width},height=${height},left=${left},top=${top}`
      );
    } catch (error) {
      console.error('OAuth init failed:', error);
    }
  };

  const providers = providersData?.providers || [];

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-md p-8">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-neutral-dark mb-2">
            Créer un compte
          </h1>
          <p className="text-gray-600">
            Rejoignez AREA et automatisez vos workflows
          </p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Nom d'utilisateur"
            type="text"
            placeholder="johndoe"
            error={errors.username?.message}
            {...register('username')}
          />

          <Input
            label="Email"
            type="email"
            placeholder="vous@example.com"
            error={errors.email?.message}
            {...register('email')}
          />

          <Input
            label="Mot de passe"
            type="password"
            placeholder="••••••••"
            error={errors.password?.message}
            {...register('password')}
          />

          <Input
            label="Confirmer le mot de passe"
            type="password"
            placeholder="••••••••"
            error={errors.confirmPassword?.message}
            {...register('confirmPassword')}
          />

          <Button
            type="submit"
            className="w-full"
            disabled={isLoading}
          >
            {isLoading ? (
              <>
                <Loader2 className="animate-spin mr-2" size={20} />
                Création en cours...
              </>
            ) : (
              'Créer un compte'
            )}
          </Button>
        </form>

        {providers.length > 0 && (
          <>
            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300" />
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">ou</span>
              </div>
            </div>

            <div className="space-y-3">
              {providers.map((provider) => (
                <Button
                  key={provider.name}
                  variant="secondary"
                  onClick={() => handleOAuthLogin(provider)}
                  className="w-full"
                >
                  Continuer avec {provider.displayName}
                </Button>
              ))}
            </div>
          </>
        )}

        <p className="text-center text-sm text-gray-600 mt-6">
          Vous avez déjà un compte?{' '}
          <Link href="/login" className="text-primary hover:underline font-medium">
            Se connecter
          </Link>
        </p>
      </Card>
    </div>
  );
}
