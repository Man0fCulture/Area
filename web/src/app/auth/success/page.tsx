'use client';

import { Suspense, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { Loading } from '@/components/common/Loading';
import { setTokens } from '@/api/client';
import { useAuth } from '@/contexts/AuthContext';
import toast from 'react-hot-toast';

function OAuthSuccessContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { setUserFromToken } = useAuth();

  useEffect(() => {
    const accessToken = searchParams?.get('access_token');
    const refreshToken = searchParams?.get('refresh_token');
    const error = searchParams?.get('error');

    if (error) {
      toast.error(`Erreur d'authentification: ${error}`);
      router.push('/login');
      return;
    }

    if (accessToken && refreshToken) {
      // Store tokens
      setTokens(accessToken, refreshToken);

      // Decode and set user from token
      setUserFromToken(accessToken);

      toast.success('Connexion réussie !');
      router.push('/dashboard');
    } else {
      toast.error('Tokens manquants');
      router.push('/login');
    }
  }, [searchParams, router, setUserFromToken]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#FF6B35] via-[#FFB627] to-[#FF6B35] flex items-center justify-center">
      <Loading size="lg" text="Authentification en cours..." />
    </div>
  );
}

export default function OAuthSuccessPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-gradient-to-br from-[#FF6B35] via-[#FFB627] to-[#FF6B35] flex items-center justify-center">
        <Loading size="lg" text="Authentification en cours..." />
      </div>
    }>
      <OAuthSuccessContent />
    </Suspense>
  );
}
