'use client';

import { Suspense, useEffect } from 'react';
import { useRouter, useSearchParams, useParams } from 'next/navigation';
import { Loading } from '@/components/common/Loading';
import { useAuth } from '@/contexts/AuthContext';
import { apiClient, setTokens } from '@/api/client';
import toast from 'react-hot-toast';

function OAuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const params = useParams();
  const { setUserFromToken } = useAuth();
  const provider = params?.provider as string;

  useEffect(() => {
    const processOAuthCallback = async () => {
      const code = searchParams?.get('code');
      const state = searchParams?.get('state');
      const error = searchParams?.get('error');

      if (error) {
        toast.error(`Erreur d&apos;authentification: ${error}`);
        router.push('/login');
        return;
      }

      if (!code) {
        toast.error('Paramètres OAuth manquants');
        router.push('/login');
        return;
      }

      try {
        // Exchange code for tokens
        const response = await apiClient.post(`/api/auth/oauth/${provider}/token`, {
          code,
          redirectUri: `${window.location.origin}/auth/callback/${provider}`,
          state,
        });

        const { accessToken, refreshToken } = response.data;

        if (accessToken && refreshToken) {
          setTokens(accessToken, refreshToken);
          setUserFromToken(accessToken);
          toast.success(`Connexion avec ${provider} réussie !`);
          router.push('/dashboard');
        } else {
          throw new Error('Tokens manquants dans la réponse');
        }
      } catch (error) {
        console.error('OAuth callback error:', error);
        const errorMessage = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Erreur lors de l\'authentification';
        toast.error(errorMessage);
        router.push('/login');
      }
    };

    processOAuthCallback();
  }, [searchParams, router, provider, setUserFromToken]);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <Loading size="lg" text="Authentification en cours..." />
    </div>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Loading size="lg" text="Authentification en cours..." />
      </div>
    }>
      <OAuthCallbackContent />
    </Suspense>
  );
}
