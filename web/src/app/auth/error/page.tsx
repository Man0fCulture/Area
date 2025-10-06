'use client';

import { Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { Card } from '@/components/common/Card';
import { Button } from '@/components/common/Button';
import { AlertCircle } from 'lucide-react';
import Link from 'next/link';
import { Loading } from '@/components/common/Loading';

function OAuthErrorContent() {
  const searchParams = useSearchParams();
  const error = searchParams?.get('error') || 'Une erreur est survenue lors de l\'authentification';

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#FF6B35] via-[#FFB627] to-[#FF6B35] flex items-center justify-center p-4">
      <Card className="w-full max-w-md">
        <div className="text-center">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-[#E74C3C] bg-opacity-10 rounded-2xl mb-4">
            <AlertCircle className="w-8 h-8 text-[#E74C3C]" />
          </div>
          <h1 className="text-2xl font-bold text-[#2C3E50] mb-2">Erreur d&apos;authentification</h1>
          <p className="text-[#95A5A6] mb-6">{error}</p>

          <div className="space-y-3">
            <Link href="/login" className="block">
              <Button className="w-full">
                Retour à la connexion
              </Button>
            </Link>
            <Link href="/register" className="block">
              <Button variant="ghost" className="w-full">
                Créer un compte
              </Button>
            </Link>
          </div>
        </div>
      </Card>
    </div>
  );
}

export default function OAuthErrorPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-gradient-to-br from-[#FF6B35] via-[#FFB627] to-[#FF6B35] flex items-center justify-center">
        <Loading size="lg" />
      </div>
    }>
      <OAuthErrorContent />
    </Suspense>
  );
}
