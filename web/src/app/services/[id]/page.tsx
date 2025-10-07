'use client';

import { useQuery } from '@tanstack/react-query';
import { servicesApi } from '@/api';
import DashboardLayout from '@/components/layout/DashboardLayout';
import { Card } from '@/components/common/Card';
import { Badge } from '@/components/common/Badge';
import { Loading } from '@/components/common/Loading';
import { Button } from '@/components/common/Button';
import { ArrowLeft, Zap, RefreshCw } from 'lucide-react';
import Link from 'next/link';
import { use } from 'react';

export default function ServiceDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);

  const { data: service, isLoading } = useQuery({
    queryKey: ['service', id],
    queryFn: async () => {
      const response = await servicesApi.getById(id);
      return response.data;
    },
  });

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-64">
          <Loading />
        </div>
      </DashboardLayout>
    );
  }

  if (!service) {
    return (
      <DashboardLayout>
        <div className="text-center py-12">
          <p className="text-gray-600">Service introuvable</p>
          <Link href="/services">
            <Button variant="secondary" className="mt-4">
              Retour aux services
            </Button>
          </Link>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Link href="/services">
            <Button variant="ghost" size="sm">
              <ArrowLeft size={20} />
            </Button>
          </Link>
          <div className="flex-1">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 bg-gradient-to-br from-primary to-accent rounded-xl flex items-center justify-center text-white font-bold text-2xl">
                {service.name.charAt(0)}
              </div>
              <div>
                <h1 className="text-3xl font-bold text-neutral-dark">
                  {service.displayName}
                </h1>
                <div className="flex items-center gap-2 mt-1">
                  <Badge variant={service.enabled ? 'success' : 'gray'}>
                    {service.enabled ? 'Disponible' : 'Indisponible'}
                  </Badge>
                  {service.requiresAuth && (
                    <Badge variant="info">Authentification requise</Badge>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Description */}
        <Card className="p-6">
          <p className="text-gray-700">{service.description}</p>
          <div className="flex items-center gap-4 mt-4 pt-4 border-t border-gray-100">
            <span className="text-sm text-gray-600">
              <span className="font-medium text-neutral-dark">
                {service.actions.length}
              </span>{' '}
              action{service.actions.length > 1 ? 's' : ''}
            </span>
            <span className="text-sm text-gray-600">
              <span className="font-medium text-neutral-dark">
                {service.reactions.length}
              </span>{' '}
              réaction{service.reactions.length > 1 ? 's' : ''}
            </span>
            <span className="text-sm text-gray-600">
              Catégorie:{' '}
              <span className="font-medium text-neutral-dark">
                {service.category}
              </span>
            </span>
          </div>
        </Card>

        {/* Actions */}
        <div>
          <div className="flex items-center gap-3 mb-4">
            <Zap className="w-6 h-6 text-primary" />
            <h2 className="text-xl font-semibold text-neutral-dark">
              Actions ({service.actions.length})
            </h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {service.actions.map((action) => (
              <Card key={action.id} className="p-6">
                <h3 className="font-semibold text-neutral-dark mb-2">
                  {action.name}
                </h3>
                <p className="text-sm text-gray-600 mb-4">
                  {action.description}
                </p>
                {action.parameters.length > 0 && (
                  <div className="space-y-2">
                    <p className="text-xs font-medium text-gray-700">
                      Paramètres:
                    </p>
                    {action.parameters.map((param) => (
                      <div
                        key={param.name}
                        className="flex items-center gap-2 text-xs"
                      >
                        <Badge variant={param.required ? 'error' : 'gray'}>
                          {param.type}
                        </Badge>
                        <span className="text-gray-700">{param.name}</span>
                        {param.required && (
                          <span className="text-error">*</span>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </Card>
            ))}
          </div>
        </div>

        {/* Reactions */}
        <div>
          <div className="flex items-center gap-3 mb-4">
            <RefreshCw className="w-6 h-6 text-accent" />
            <h2 className="text-xl font-semibold text-neutral-dark">
              Réactions ({service.reactions.length})
            </h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {service.reactions.map((reaction) => (
              <Card key={reaction.id} className="p-6">
                <h3 className="font-semibold text-neutral-dark mb-2">
                  {reaction.name}
                </h3>
                <p className="text-sm text-gray-600 mb-4">
                  {reaction.description}
                </p>
                {reaction.parameters.length > 0 && (
                  <div className="space-y-2">
                    <p className="text-xs font-medium text-gray-700">
                      Paramètres:
                    </p>
                    {reaction.parameters.map((param) => (
                      <div
                        key={param.name}
                        className="flex items-center gap-2 text-xs"
                      >
                        <Badge variant={param.required ? 'error' : 'gray'}>
                          {param.type}
                        </Badge>
                        <span className="text-gray-700">{param.name}</span>
                        {param.required && (
                          <span className="text-error">*</span>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </Card>
            ))}
          </div>
        </div>

        {/* CTA */}
        <Card className="p-6 bg-gradient-to-r from-primary to-accent text-white">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-xl font-semibold mb-2">
                Prêt à automatiser ?
              </h3>
              <p className="opacity-90">
                Créez une AREA utilisant {service.displayName}
              </p>
            </div>
            <Link href="/areas/create">
              <Button variant="secondary" className="bg-white text-primary">
                Créer une AREA
              </Button>
            </Link>
          </div>
        </Card>
      </div>
    </DashboardLayout>
  );
}
