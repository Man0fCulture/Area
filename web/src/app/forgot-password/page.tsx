'use client';

import { useState } from 'react';
import Link from 'next/link';
import { Card } from '@/components/common/Card';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitted, setIsSubmitted] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    // Simuler l'envoi (fonctionnalité non implémentée dans l'API)
    setTimeout(() => {
      setIsSubmitted(true);
      setIsLoading(false);
      toast.success('Email envoyé ! Vérifiez votre boîte de réception.');
    }, 1000);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-md p-8">
        <Link
          href="/login"
          className="inline-flex items-center text-sm text-gray-600 hover:text-primary mb-6"
        >
          <ArrowLeft size={16} className="mr-2" />
          Retour à la connexion
        </Link>

        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-neutral-dark mb-2">
            Mot de passe oublié ?
          </h1>
          <p className="text-gray-600">
            Entrez votre email et nous vous enverrons un lien de réinitialisation
          </p>
        </div>

        {!isSubmitted ? (
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Email"
              type="email"
              placeholder="vous@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />

            <Button
              type="submit"
              className="w-full"
              disabled={isLoading || !email}
              isLoading={isLoading}
            >
              Envoyer le lien
            </Button>
          </form>
        ) : (
          <div className="text-center py-6">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg
                className="w-8 h-8 text-success"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
            <h3 className="text-lg font-semibold text-neutral-dark mb-2">
              Email envoyé !
            </h3>
            <p className="text-gray-600 mb-6">
              Vérifiez votre boîte de réception à <strong>{email}</strong>
            </p>
            <Link href="/login">
              <Button variant="secondary" className="w-full">
                Retour à la connexion
              </Button>
            </Link>
          </div>
        )}

        <p className="text-center text-sm text-gray-500 mt-6">
          Cette fonctionnalité sera disponible prochainement
        </p>
      </Card>
    </div>
  );
}
