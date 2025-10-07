'use client';

import { useQuery } from '@tanstack/react-query';
import { areasApi } from '@/api';
import DashboardLayout from '@/components/layout/DashboardLayout';
import { Card } from '@/components/common/Card';
import { Badge } from '@/components/common/Badge';
import { Loading } from '@/components/common/Loading';
import { Clock, Activity, CheckCircle, XCircle } from 'lucide-react';
import Link from 'next/link';

export default function HistoryPage() {
  const { data: areas, isLoading: areasLoading } = useQuery({
    queryKey: ['areas'],
    queryFn: async () => {
      const response = await areasApi.getAll();
      return response.data;
    },
  });

  // Fetch executions for all areas
  const { data: allExecutions, isLoading: executionsLoading } = useQuery({
    queryKey: ['all-executions', areas?.map((a) => a.id)],
    queryFn: async () => {
      if (!areas || areas.length === 0) return [];

      const executionsPromises = areas.map(async (area) => {
        try {
          const response = await areasApi.getExecutions(area.id, 20);
          return response.data.map((exec) => ({
            ...exec,
            areaName: area.name,
          }));
        } catch (error) {
          console.error(`Error fetching executions for area ${area.id}:`, error);
          return [];
        }
      });

      const results = await Promise.all(executionsPromises);
      return results
        .flat()
        .sort((a, b) => b.startedAt - a.startedAt);
    },
    enabled: !!areas && areas.length > 0,
  });

  const isLoading = areasLoading || executionsLoading;

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-64">
          <Loading />
        </div>
      </DashboardLayout>
    );
  }

  const successCount =
    allExecutions?.filter((e) => e.status === 'SUCCESS').length || 0;
  const failedCount =
    allExecutions?.filter((e) => e.status === 'FAILED').length || 0;
  const totalCount = allExecutions?.length || 0;

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-neutral-dark">Historique</h1>
          <p className="text-gray-600 mt-1">
            Consultez l'historique des exécutions de vos AREAs
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-50 rounded-lg">
                <Activity className="w-6 h-6 text-info" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Total</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {totalCount}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-green-50 rounded-lg">
                <CheckCircle className="w-6 h-6 text-success" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Succès</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {successCount}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-red-50 rounded-lg">
                <XCircle className="w-6 h-6 text-error" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Échecs</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {failedCount}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-50 rounded-lg">
                <Clock className="w-6 h-6 text-primary" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Taux de succès</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {totalCount > 0
                    ? `${Math.round((successCount / totalCount) * 100)}%`
                    : '0%'}
                </p>
              </div>
            </div>
          </Card>
        </div>

        {/* Executions List */}
        <Card className="p-6">
          <h2 className="text-xl font-semibold text-neutral-dark mb-4">
            Exécutions récentes
          </h2>

          {!allExecutions || allExecutions.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              <Clock className="w-16 h-16 mx-auto mb-4 opacity-30" />
              <p className="text-lg">Aucune exécution pour le moment</p>
              <p className="text-sm mt-2">
                Les exécutions de vos AREAs apparaîtront ici
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {allExecutions.map((execution) => (
                <div
                  key={execution.id}
                  className="flex items-start justify-between p-4 border border-gray-200 rounded-lg hover:border-primary hover:shadow-sm transition"
                >
                  <div className="flex items-start gap-4 flex-1">
                    <div className="mt-1">
                      <Badge
                        variant={
                          execution.status === 'SUCCESS'
                            ? 'success'
                            : execution.status === 'FAILED'
                            ? 'error'
                            : execution.status === 'PROCESSING'
                            ? 'info'
                            : 'gray'
                        }
                      >
                        {execution.status}
                      </Badge>
                    </div>

                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <Link
                          href={`/areas/${execution.areaId}`}
                          className="font-medium text-neutral-dark hover:text-primary"
                        >
                          {execution.areaName}
                        </Link>
                      </div>

                      <div className="text-sm text-gray-600 mb-2">
                        {new Date(execution.startedAt).toLocaleString('fr-FR', {
                          dateStyle: 'medium',
                          timeStyle: 'medium',
                        })}
                      </div>

                      {execution.error && (
                        <div className="text-sm text-error bg-red-50 px-3 py-2 rounded mt-2">
                          <span className="font-medium">Erreur: </span>
                          {execution.error}
                        </div>
                      )}

                      {execution.actionData && (
                        <details className="mt-2">
                          <summary className="text-sm text-gray-600 cursor-pointer hover:text-primary">
                            Voir les données
                          </summary>
                          <pre className="text-xs bg-gray-50 p-3 rounded mt-2 overflow-x-auto">
                            {JSON.stringify(execution.actionData, null, 2)}
                          </pre>
                        </details>
                      )}
                    </div>
                  </div>

                  <div className="text-right ml-4">
                    {execution.completedAt && (
                      <div className="text-sm text-gray-500">
                        Durée:{' '}
                        <span className="font-medium">
                          {((execution.completedAt - execution.startedAt) / 1000).toFixed(
                            2
                          )}
                          s
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </DashboardLayout>
  );
}
