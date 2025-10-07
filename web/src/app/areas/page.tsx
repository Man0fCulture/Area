'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { areasApi } from '@/api';
import DashboardLayout from '@/components/layout/DashboardLayout';
import { Card } from '@/components/common/Card';
import { Badge } from '@/components/common/Badge';
import { Button } from '@/components/common/Button';
import { Loading } from '@/components/common/Loading';
import { Toggle } from '@/components/common/Toggle';
import { Plus, Trash2, Edit, Activity, Calendar } from 'lucide-react';
import Link from 'next/link';
import { useState } from 'react';
import { Modal } from '@/components/common/Modal';
import toast from 'react-hot-toast';

export default function AreasPage() {
  const queryClient = useQueryClient();
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [areaToDelete, setAreaToDelete] = useState<string | null>(null);

  const { data: areas, isLoading } = useQuery({
    queryKey: ['areas'],
    queryFn: async () => {
      const response = await areasApi.getAll();
      return response.data;
    },
  });

  const toggleMutation = useMutation({
    mutationFn: async ({ id, active }: { id: string; active: boolean }) => {
      if (active) {
        return areasApi.deactivate(id);
      } else {
        return areasApi.activate(id);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['areas'] });
      toast.success('AREA mise à jour');
    },
    onError: () => {
      toast.error('Erreur lors de la mise à jour');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => areasApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['areas'] });
      toast.success('AREA supprimée');
      setDeleteModalOpen(false);
      setAreaToDelete(null);
    },
    onError: () => {
      toast.error('Erreur lors de la suppression');
    },
  });

  const handleToggle = (id: string, active: boolean) => {
    toggleMutation.mutate({ id, active });
  };

  const handleDeleteClick = (id: string) => {
    setAreaToDelete(id);
    setDeleteModalOpen(true);
  };

  const handleDeleteConfirm = () => {
    if (areaToDelete) {
      deleteMutation.mutate(areaToDelete);
    }
  };

  if (isLoading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-64">
          <Loading />
        </div>
      </DashboardLayout>
    );
  }

  const activeAreas = areas?.filter((area) => area.active) || [];
  const inactiveAreas = areas?.filter((area) => !area.active) || [];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-neutral-dark">Mes AREAs</h1>
            <p className="text-gray-600 mt-1">
              Gérez vos automations
            </p>
          </div>
          <Link href="/areas/create">
            <Button>
              <Plus size={20} className="mr-2" />
              Créer une AREA
            </Button>
          </Link>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-blue-50 rounded-lg">
                <Activity className="w-6 h-6 text-info" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Total AREAs</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {areas?.length || 0}
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
                <p className="text-sm text-gray-600">Actives</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {activeAreas.length}
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-4">
              <div className="p-3 bg-gray-50 rounded-lg">
                <Activity className="w-6 h-6 text-gray-400" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Inactives</p>
                <p className="text-2xl font-bold text-neutral-dark">
                  {inactiveAreas.length}
                </p>
              </div>
            </div>
          </Card>
        </div>

        {/* Areas List */}
        {areas && areas.length === 0 ? (
          <Card className="p-12 text-center">
            <Activity className="w-16 h-16 mx-auto mb-4 text-gray-300" />
            <h3 className="text-xl font-semibold text-neutral-dark mb-2">
              Aucune AREA pour le moment
            </h3>
            <p className="text-gray-600 mb-6">
              Créez votre première automation pour commencer
            </p>
            <Link href="/areas/create">
              <Button>
                <Plus size={20} className="mr-2" />
                Créer votre première AREA
              </Button>
            </Link>
          </Card>
        ) : (
          <div className="space-y-4">
            {areas?.map((area) => (
              <Card key={area.id} className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <Link href={`/areas/${area.id}`}>
                        <h3 className="text-lg font-semibold text-neutral-dark hover:text-primary cursor-pointer">
                          {area.name}
                        </h3>
                      </Link>
                      <Badge variant={area.active ? 'success' : 'gray'}>
                        {area.active ? 'Actif' : 'Inactif'}
                      </Badge>
                    </div>

                    {area.description && (
                      <p className="text-sm text-gray-600 mb-4">
                        {area.description}
                      </p>
                    )}

                    {/* Flow Visualization */}
                    <div className="flex items-center gap-2 mb-4 overflow-x-auto">
                      <div className="px-3 py-1 bg-primary/10 text-primary rounded text-sm font-medium whitespace-nowrap">
                        Action: {area.action.actionId}
                      </div>
                      <span className="text-gray-400">→</span>
                      {area.reactions.map((reaction, idx) => (
                        <div
                          key={idx}
                          className="px-3 py-1 bg-accent/10 text-accent rounded text-sm font-medium whitespace-nowrap"
                        >
                          Réaction: {reaction.reactionId}
                        </div>
                      ))}
                    </div>

                    <div className="flex items-center gap-6 text-sm text-gray-500">
                      <span className="flex items-center gap-1">
                        <Activity size={14} />
                        {area.executionCount || 0} exécutions
                      </span>
                      {area.lastTriggeredAt && (
                        <span className="flex items-center gap-1">
                          <Calendar size={14} />
                          Dernière: {new Date(area.lastTriggeredAt).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-3 ml-4">
                    <Toggle
                      checked={area.active}
                      onCheckedChange={() => handleToggle(area.id, area.active)}
                    />
                    <Link href={`/areas/${area.id}`}>
                      <Button variant="ghost" size="sm">
                        <Edit size={18} />
                      </Button>
                    </Link>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDeleteClick(area.id)}
                      className="text-error hover:bg-red-50"
                    >
                      <Trash2 size={18} />
                    </Button>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      <Modal
        open={deleteModalOpen}
        onOpenChange={() => setDeleteModalOpen(false)}
        title="Supprimer l&apos;AREA"
      >
        <div className="space-y-4">
          <p className="text-gray-600">
            Êtes-vous sûr de vouloir supprimer cette AREA ? Cette action est
            irréversible et supprimera également tout l&apos;historique d'exécution.
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
              onClick={handleDeleteConfirm}
              className="bg-error hover:bg-error/90"
            >
              Supprimer
            </Button>
          </div>
        </div>
      </Modal>
    </DashboardLayout>
  );
}
