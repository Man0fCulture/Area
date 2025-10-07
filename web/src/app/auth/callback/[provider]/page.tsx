'use client';

import { Suspense, useEffect } from 'react';
import { useRouter, useSearchParams, useParams } from 'next/navigation';
import { Loading } from '@/components/common/Loading';
import toast from 'react-hot-toast';

function OAuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const params = useParams();
  const provider = params?.provider as string;

  useEffect(() => {
    const code = searchParams?.get('code');
    const state = searchParams?.get('state');
    const error = searchParams?.get('error');

    if (error) {
      toast.error(`Erreur d'authentification: ${error}`);
      router.push('/login');
      return;
    }

    if (code && state) {
      toast.success(`Connexion avec ${provider} réussie !`);
      router.push('/dashboard');
    } else {
      toast.error('Paramètres OAuth manquants');
      router.push('/login');
    }
  }, [searchParams, router, provider]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#FF6B35] via-[#FFB627] to-[#FF6B35] flex items-center justify-center">
      <Loading size="lg" text="Authentification en cours..." />
    </div>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-gradient-to-br from-[#FF6B35] via-[#FFB627] to-[#FF6B35] flex items-center justify-center">
        <Loading size="lg" text="Authentification en cours..." />
      </div>
    }>
      <OAuthCallbackContent />
    </Suspense>
  );
}
