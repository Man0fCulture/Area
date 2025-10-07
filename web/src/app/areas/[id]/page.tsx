'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { areasApi, servicesApi } from '@/api';
import DashboardLayout from '@/components/layout/DashboardLayout';
import { Card } from '@/components/common/Card';
import { Badge } from '@/components/common/Badge';
import { Button } from '@/components/common/Button';
import { Loading } from '@/components/common/Loading';
import { Toggle } from '@/components/common/Toggle';
import { ArrowLeft, Activity, Calendar, Trash2, Play } from 'lucide-react';
import Link from 'next/link';
import { use, useState } from 'react';
import toast from 'react-hot-toast';
import { useRouter } from 'next/navigation';
import { Modal } from '@/components/common/Modal';

export default function AreaDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const queryClient = useQueryClient();
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);

  const { data: area, isLoading: areaLoading } = useQuery({
    queryKey: ['area', id],
    queryFn: async () => {
      const response = await areasApi.getById(id);
      return response.data;
    },
  });

  const { data: services } = useQuery({
    queryKey: ['services'],
    queryFn: async () => {
      const response = await servicesApi.getAll();
      return response.data;
    },
  });

  const { data: executions, isLoading: executionsLoading } = useQuery({
    queryKey: ['executions', id],
    queryFn: async () => {
      const response = await areasApi.getExecutions(id, 10);
      return response.data;
    },
  });

  const toggleMutation = useMutation({
    mutationFn: async () => {
      if (area?.active) {
        return areasApi.deactivate(id);
      } else {
        return areasApi.activate(id);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['area', id] });
      queryClient.invalidateQueries({ queryKey: ['areas'] });
      toast.success('AREA mise à jour');
    },
    onError: () => {
      toast.error('Erreur lors de la mise à jour');
    },
  });

  const testMutation = useMutation({
    mutationFn: () => areasApi.test(id),
    onSuccess: () => {
      toast.success('Test lancé avec succès');
      queryClient.invalidateQueries({ queryKey: ['executions', id] });
    },
    onError: () => {
      toast.error('Erreur lors du test');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => areasApi.delete(id),
    onSuccess: () => {
      toast.success('AREA supprimée');
      router.push('/areas');
    },
    onError: () => {
      toast.error('Erreur lors de la suppression');
    },
  });

  if (areaLoading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-64">
          <Loading />
        </div>
      </DashboardLayout>
    );
  }

  if (!area) {
    return (
      <DashboardLayout>
        <div className="text-center py-12">
          <p className="text-gray-600">AREA introuvable</p>
          <Link href="/areas">
            <Button variant="secondary" className="mt-4">
              Retour aux AREAs
            </Button>
          </Link>
        </div>
      </DashboardLayout>
    );
  }

  const actionService = services?.find((s) => s.id === area.action.serviceId);
  const actionDef = actionService?.actions.find((a) => a.id === area.action.actionId);

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Link href="/areas">
            <Button variant="ghost" size="sm">
              <ArrowLeft size={20} />
            </Button>
          </Link>
          <div className="flex-1">
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-bold text-neutral-dark">
                {area.name}
              </h1>
              <Badge variant={area.active ? 'success' : 'gray'}>
                {area.active ? 'Actif' : 'Inactif'}
              </Badge>
            </div>
            {area.description && (
              <p className="text-gray-600 mt-1">{area.description}</p>
            )}
          </div>
          <div className="flex items-center gap-3">
            <Toggle
              checked={area.active}
              onCheckedChange={() => toggleMutation.mutate()}
            />
            <Button
              variant="secondary"
              size="sm"
              onClick={() => testMutation.mutate()}
              disabled={testMutation.isPending}
            >
              <Play size={18} className="mr-2" />
              Tester
            </Button>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setDeleteModalOpen(true)}
              className="text-error hover:bg-red-50"
            >
              <Trash2 size={18} />
            </Button>
          </div>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-50 rounded-lg">
                <Activity className="w-6 h-6 text-info" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Total exécutions</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {area.executionCount || 0}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-green-50 rounded-lg">
                <Activity className="w-6 h-6 text-success" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Statut</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {area.active ? 'Actif' : 'Inactif'}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-50 rounded-lg">
                <Calendar className="w-6 h-6 text-primary" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Dernière exécution</p>
                <p className="text-sm font-bold text-neutral-dark">
                  {area.lastTriggeredAt
                    ? new Date(area.lastTriggeredAt).toLocaleString()
                    : 'Jamais'}
                </p>
              </div>
            </div>
          </Card>
        </div>

        {/* Flow Visualization */}
        <Card className="p-6">
          <h2 className="text-xl font-semibold text-neutral-dark mb-4">
            Workflow
          </h2>
          <div className="flex items-center gap-3 flex-wrap">
            <div className="p-4 bg-primary/10 border-2 border-primary rounded-lg">
              <p className="text-xs text-gray-600 mb-1">ACTION</p>
              <p className="font-semibold text-neutral-dark">
                {actionService?.displayName || 'Service inconnu'}
              </p>
              <p className="text-sm text-gray-700 mt-1">
                {actionDef?.name || area.action.actionId}
              </p>
              {Object.keys(area.action.config).length > 0 && (
                <div className="mt-2 text-xs text-gray-600">
                  Config: {JSON.stringify(area.action.config)}
                </div>
              )}
            </div>

            <div className="text-2xl text-gray-400">→</div>

            {area.reactions.map((reaction, idx) => {
              const reactionService = services?.find(
                (s) => s.id === reaction.serviceId
              );
              const reactionDef = reactionService?.reactions.find(
                (r) => r.id === reaction.reactionId
              );

              return (
                <div key={idx} className="flex items-center gap-3">
                  <div className="p-4 bg-accent/10 border-2 border-accent rounded-lg">
                    <p className="text-xs text-gray-600 mb-1">
                      RÉACTION {idx + 1}
                    </p>
                    <p className="font-semibold text-neutral-dark">
                      {reactionService?.displayName || 'Service inconnu'}
                    </p>
                    <p className="text-sm text-gray-700 mt-1">
                      {reactionDef?.name || reaction.reactionId}
                    </p>
                    {Object.keys(reaction.config).length > 0 && (
                      <div className="mt-2 text-xs text-gray-600">
                        Config: {JSON.stringify(reaction.config)}
                      </div>
                    )}
                  </div>
                  {idx < area.reactions.length - 1 && (
                    <div className="text-2xl text-gray-400">→</div>
                  )}
                </div>
              );
            })}
          </div>
        </Card>

        {/* Recent Executions */}
        <Card className="p-6">
          <h2 className="text-xl font-semibold text-neutral-dark mb-4">
            Exécutions récentes
          </h2>
          {executionsLoading ? (
            <div className="flex justify-center py-8">
              <Loading />
            </div>
          ) : executions && executions.length > 0 ? (
            <div className="space-y-3">
              {executions.map((execution) => (
                <div
                  key={execution.id}
                  className="flex items-center justify-between p-4 border border-gray-200 rounded-lg"
                >
                  <div className="flex items-center gap-4">
                    <Badge
                      variant={
                        execution.status === 'SUCCESS'
                          ? 'success'
                          : execution.status === 'FAILED'
                          ? 'error'
                          : 'info'
                      }
                    >
                      {execution.status}
                    </Badge>
                    <div>
                      <p className="text-sm text-gray-600">
                        {new Date(execution.startedAt).toLocaleString()}
                      </p>
                      {execution.error && (
                        <p className="text-xs text-error mt-1">
                          {execution.error}
                        </p>
                      )}
                    </div>
                  </div>
                  {execution.completedAt && (
                    <span className="text-sm text-gray-500">
                      Durée:{' '}
                      {((execution.completedAt - execution.startedAt) / 1000).toFixed(
                        2
                      )}
                      s
                    </span>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              <Activity className="w-12 h-12 mx-auto mb-3 opacity-30" />
              <p>Aucune exécution pour le moment</p>
            </div>
          )}
        </Card>
      </div>

      {/* Delete Modal */}
      <Modal
        open={deleteModalOpen}
        onOpenChange={() => setDeleteModalOpen(false)}
        title="Supprimer l'AREA"
      >
        <div className="space-y-4">
          <p className="text-gray-600">
            Êtes-vous sûr de vouloir supprimer cette AREA ? Cette action est
            irréversible et supprimera également tout l&apos;historique d&apos;exécution.
          </p>
          <div className="flex gap-3 justify-end">
            <Button
              variant="secondary"
              onClick={() => setDeleteModalOpen(false)}
            >
              Annuler
            </Button>
            <Button
              variant="primary"
              onClick={() => deleteMutation.mutate()}
              className="bg-error hover:bg-error/90"
              disabled={deleteMutation.isPending}
            >
              Supprimer
            </Button>
          </div>
        </div>
      </Modal>
    </DashboardLayout>
  );
}
