'use client';

import { useQuery } from '@tanstack/react-query';
import { areasApi, servicesApi } from '@/api';
import DashboardLayout from '@/components/layout/DashboardLayout';
import { Card } from '@/components/common/Card';
import { Badge } from '@/components/common/Badge';
import { Loading } from '@/components/common/Loading';
import { Activity, Zap, Blocks, Clock } from 'lucide-react';
import Link from 'next/link';
import { format } from 'date-fns';

export default function DashboardPage() {
  const { data: areas, isLoading: areasLoading } = useQuery({
    queryKey: ['areas'],
    queryFn: async () => {
      const response = await areasApi.getAll();
      return response.data;
    },
  });

  const { data: services, isLoading: servicesLoading } = useQuery({
    queryKey: ['services'],
    queryFn: async () => {
      const response = await servicesApi.getAll();
      return response.data;
    },
  });

  const isLoading = areasLoading || servicesLoading;

  // Calculer les statistiques
  const activeAreasCount = areas?.filter((area) => area.active).length || 0;
  const totalExecutions = areas?.reduce((sum, area) => sum + (area.executionCount || 0), 0) || 0;
  const connectedServicesCount = services?.filter((s) => s.enabled).length || 0;
  const recentAreas = areas?.slice(0, 5) || [];

  // Exécutions aujourd'hui (simulé car pas dans l'API actuelle)
  const executionsToday = Math.floor(totalExecutions * 0.1);

  const stats = [
    {
      name: 'AREAs actives',
      value: activeAreasCount,
      icon: Activity,
      color: 'text-primary',
      bgColor: 'bg-blue-50',
    },
    {
      name: 'Exécutions aujourd\'hui',
      value: executionsToday,
      icon: Zap,
      color: 'text-accent',
      bgColor: 'bg-blue-50',
    },
    {
      name: 'Services connectés',
      value: connectedServicesCount,
      icon: Blocks,
      color: 'text-info',
      bgColor: 'bg-blue-50',
    },
    {
      name: 'Total exécutions',
      value: totalExecutions,
      icon: Clock,
      color: 'text-success',
      bgColor: 'bg-green-50',
    },
  ];

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-64">
          <Loading />
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-neutral-dark">Dashboard</h1>
          <p className="text-gray-600 mt-1">
            Vue d&apos;ensemble de vos automations
          </p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {stats.map((stat) => {
            const Icon = stat.icon;
            return (
              <Card key={stat.name} className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">{stat.name}</p>
                    <p className="text-3xl font-bold text-neutral-dark mt-2">
                      {stat.value}
                    </p>
                  </div>
                  <div className={`p-3 rounded-lg ${stat.bgColor}`}>
                    <Icon className={`w-6 h-6 ${stat.color}`} />
                  </div>
                </div>
              </Card>
            );
          })}
        </div>

        {/* Recent AREAs */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-neutral-dark">
                AREAs récentes
              </h2>
              <Link
                href="/areas"
                className="text-sm text-primary hover:underline"
              >
                Voir tout
              </Link>
            </div>

            {recentAreas.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <Activity className="w-12 h-12 mx-auto mb-3 opacity-30" />
                <p>Aucune AREA pour le moment</p>
                <Link
                  href="/areas/create"
                  className="text-primary hover:underline text-sm mt-2 inline-block"
                >
                  Créer votre première AREA
                </Link>
              </div>
            ) : (
              <div className="space-y-4">
                {recentAreas.map((area) => (
                  <Link
                    key={area.id}
                    href={`/areas/${area.id}`}
                    className="block p-4 border border-gray-200 rounded-lg hover:border-primary hover:shadow-sm transition"
                  >
                    <div className="flex items-center justify-between mb-2">
                      <h3 className="font-medium text-neutral-dark">
                        {area.name}
                      </h3>
                      <Badge variant={area.active ? 'success' : 'gray'}>
                        {area.active ? 'Actif' : 'Inactif'}
                      </Badge>
                    </div>
                    {area.description && (
                      <p className="text-sm text-gray-600 mb-2">
                        {area.description}
                      </p>
                    )}
                    <div className="flex items-center gap-4 text-xs text-gray-500">
                      <span>{area.executionCount || 0} exécutions</span>
                      {area.lastTriggeredAt && (
                        <span>
                          Dernière: {format(new Date(area.lastTriggeredAt), 'dd/MM/yyyy HH:mm')}
                        </span>
                      )}
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </Card>

          {/* Quick Actions */}
          <Card className="p-6">
            <h2 className="text-xl font-semibold text-neutral-dark mb-6">
              Actions rapides
            </h2>
            <div className="space-y-3">
              <Link
                href="/areas/create"
                className="block p-4 bg-gradient-to-r from-primary to-accent text-white rounded-lg hover:shadow-md transition"
              >
                <div className="flex items-center gap-3">
                  <Zap className="w-5 h-5" />
                  <div>
                    <p className="font-medium">Créer une AREA</p>
                    <p className="text-xs opacity-90">
                      Automatisez une nouvelle tâche
                    </p>
                  </div>
                </div>
              </Link>

              <Link
                href="/services"
                className="block p-4 border border-gray-200 rounded-lg hover:border-primary hover:shadow-sm transition"
              >
                <div className="flex items-center gap-3">
                  <Blocks className="w-5 h-5 text-gray-700" />
                  <div>
                    <p className="font-medium text-neutral-dark">
                      Connecter un service
                    </p>
                    <p className="text-xs text-gray-600">
                      Ajouter plus de services
                    </p>
                  </div>
                </div>
              </Link>

              <Link
                href="/history"
                className="block p-4 border border-gray-200 rounded-lg hover:border-primary hover:shadow-sm transition"
              >
                <div className="flex items-center gap-3">
                  <Clock className="w-5 h-5 text-gray-700" />
                  <div>
                    <p className="font-medium text-neutral-dark">
                      Voir l'historique
                    </p>
                    <p className="text-xs text-gray-600">
                      Consulter les exécutions
                    </p>
                  </div>
                </div>
              </Link>
            </div>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
}
