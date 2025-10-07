'use client';

import { useQuery } from '@tanstack/react-query';
import { servicesApi } from '@/api';
import DashboardLayout from '@/components/layout/DashboardLayout';
import { Card } from '@/components/common/Card';
import { Badge } from '@/components/common/Badge';
import { Loading } from '@/components/common/Loading';
import { Blocks, Check, X } from 'lucide-react';
import Link from 'next/link';

export default function ServicesPage() {
  const { data: services, isLoading } = useQuery({
    queryKey: ['services'],
    queryFn: async () => {
      const response = await servicesApi.getAll();
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

  const enabledServices = services?.filter((s) => s.enabled) || [];
  const disabledServices = services?.filter((s) => !s.enabled) || [];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-neutral-dark">Services</h1>
          <p className="text-gray-600 mt-1">
            Connectez des services pour créer des automations
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-50 rounded-lg">
                <Blocks className="w-6 h-6 text-info" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Total services</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {services?.length || 0}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-green-50 rounded-lg">
                <Check className="w-6 h-6 text-success" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Disponibles</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {enabledServices.length}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-red-50 rounded-lg">
                <X className="w-6 h-6 text-error" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Indisponibles</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {disabledServices.length}
                </p>
              </div>
            </div>
          </Card>
        </div>

        {/* Services Grid */}
        <div>
          <h2 className="text-xl font-semibold text-neutral-dark mb-4">
            Services disponibles
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {enabledServices.map((service) => (
              <Link key={service.id} href={`/services/${service.id}`}>
                <Card className="p-6 hover:shadow-lg hover:border-primary transition-all cursor-pointer h-full">
                  <div className="flex items-start justify-between mb-4">
                    <div className="w-12 h-12 bg-gradient-to-br from-primary to-accent rounded-lg flex items-center justify-center text-white font-bold text-xl">
                      {service.name.charAt(0)}
                    </div>
                    <Badge variant="success">Disponible</Badge>
                  </div>

                  <h3 className="text-lg font-semibold text-neutral-dark mb-2">
                    {service.displayName}
                  </h3>
                  <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                    {service.description}
                  </p>

                  <div className="flex items-center gap-4 text-sm text-gray-500 border-t border-gray-100 pt-4">
                    <span className="flex items-center gap-1">
                      <span className="font-medium text-primary">
                        {service.actions.length}
                      </span>
                      action{service.actions.length > 1 ? 's' : ''}
                    </span>
                    <span className="flex items-center gap-1">
                      <span className="font-medium text-accent">
                        {service.reactions.length}
                      </span>
                      réaction{service.reactions.length > 1 ? 's' : ''}
                    </span>
                  </div>

                  {service.requiresAuth && (
                    <div className="mt-3">
                      <Badge variant="info" className="text-xs">
                        Authentification requise
                      </Badge>
                    </div>
                  )}
                </Card>
              </Link>
            ))}
          </div>
        </div>

        {/* Disabled Services */}
        {disabledServices.length > 0 && (
          <div>
            <h2 className="text-xl font-semibold text-neutral-dark mb-4">
              Services indisponibles
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {disabledServices.map((service) => (
                <Card
                  key={service.id}
                  className="p-6 opacity-60 cursor-not-allowed"
                >
                  <div className="flex items-start justify-between mb-4">
                    <div className="w-12 h-12 bg-gray-200 rounded-lg flex items-center justify-center text-gray-500 font-bold text-xl">
                      {service.name.charAt(0)}
                    </div>
                    <Badge variant="gray">Indisponible</Badge>
                  </div>

                  <h3 className="text-lg font-semibold text-neutral-dark mb-2">
                    {service.displayName}
                  </h3>
                  <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                    {service.description}
                  </p>

                  <div className="flex items-center gap-4 text-sm text-gray-500 border-t border-gray-100 pt-4">
                    <span>{service.actions.length} actions</span>
                    <span>{service.reactions.length} réactions</span>
                  </div>
                </Card>
              ))}
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}
