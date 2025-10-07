'use client';

import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { servicesApi, areasApi } from '@/api';
import { useRouter } from 'next/navigation';
import DashboardLayout from '@/components/layout/DashboardLayout';
import { Card } from '@/components/common/Card';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Textarea } from '@/components/common/Textarea';
import { Loading } from '@/components/common/Loading';
import { ArrowLeft, ArrowRight, Check, Zap, RefreshCw } from 'lucide-react';
import { Service, Action, Reaction, AreaAction, AreaReaction } from '@/types';
import toast from 'react-hot-toast';
import Link from 'next/link';

type Step = 1 | 2 | 3 | 4;

export default function CreateAreaPage() {
  const router = useRouter();
  const [step, setStep] = useState<Step>(1);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  // Action state
  const [selectedActionService, setSelectedActionService] = useState<Service | null>(null);
  const [selectedAction, setSelectedAction] = useState<Action | null>(null);
  const [actionConfig, setActionConfig] = useState<Record<string, string>>({});

  // Reactions state
  const [reactions, setReactions] = useState<Array<{
    service: Service;
    reaction: Reaction;
    config: Record<string, string>;
  }>>([]);
  const [currentReactionService, setCurrentReactionService] = useState<Service | null>(null);
  const [currentReaction, setCurrentReaction] = useState<Reaction | null>(null);
  const [currentReactionConfig, setCurrentReactionConfig] = useState<Record<string, string>>({});

  const { data: services, isLoading: servicesLoading } = useQuery({
    queryKey: ['services'],
    queryFn: async () => {
      const response = await servicesApi.getAll();
      return response.data.filter((s) => s.enabled);
    },
  });

  const createMutation = useMutation({
    mutationFn: async () => {
      const areaAction: AreaAction = {
        serviceId: selectedActionService!.id,
        actionId: selectedAction!.id,
        config: actionConfig,
      };

      const areaReactions: AreaReaction[] = reactions.map((r) => ({
        serviceId: r.service.id,
        reactionId: r.reaction.id,
        config: r.config,
      }));

      return areasApi.create({
        name,
        description,
        action: areaAction,
        reactions: areaReactions,
      });
    },
    onSuccess: (response) => {
      toast.success('AREA créée avec succès !');
      router.push(`/areas/${response.data.id}`);
    },
    onError: () => {
      toast.error('Erreur lors de la création de l\'AREA');
    },
  });

  const handleAddReaction = () => {
    if (currentReactionService && currentReaction) {
      setReactions([
        ...reactions,
        {
          service: currentReactionService,
          reaction: currentReaction,
          config: currentReactionConfig,
        },
      ]);
      setCurrentReactionService(null);
      setCurrentReaction(null);
      setCurrentReactionConfig({});
    }
  };

  const handleRemoveReaction = (index: number) => {
    setReactions(reactions.filter((_, i) => i !== index));
  };

  const canProceedStep1 = name.trim().length > 0;
  const canProceedStep2 = selectedActionService && selectedAction;
  const canProceedStep3 = reactions.length > 0;

  const handleCreate = () => {
    createMutation.mutate();
  };

  if (servicesLoading) {
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
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Link href="/areas">
            <Button variant="ghost" size="sm">
              <ArrowLeft size={20} />
            </Button>
          </Link>
          <div>
            <h1 className="text-3xl font-bold text-neutral-dark">
              Créer une AREA
            </h1>
            <p className="text-gray-600 mt-1">
              Automatisez vos tâches en quelques clics
            </p>
          </div>
        </div>

        {/* Progress Steps */}
        <Card className="p-6">
          <div className="flex items-center justify-between">
            {[1, 2, 3, 4].map((s) => (
              <div key={s} className="flex items-center">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center font-medium ${
                    step >= s
                      ? 'bg-gradient-to-r from-primary to-accent text-white'
                      : 'bg-gray-200 text-gray-500'
                  }`}
                >
                  {s}
                </div>
                {s < 4 && (
                  <div
                    className={`w-16 h-1 mx-2 ${
                      step > s ? 'bg-primary' : 'bg-gray-200'
                    }`}
                  />
                )}
              </div>
            ))}
          </div>
          <div className="flex justify-between mt-2 text-sm">
            <span className={step === 1 ? 'text-primary font-medium' : 'text-gray-500'}>
              Informations
            </span>
            <span className={step === 2 ? 'text-primary font-medium' : 'text-gray-500'}>
              Action
            </span>
            <span className={step === 3 ? 'text-primary font-medium' : 'text-gray-500'}>
              Réactions
            </span>
            <span className={step === 4 ? 'text-primary font-medium' : 'text-gray-500'}>
              Résumé
            </span>
          </div>
        </Card>

        {/* Step 1: Basic Information */}
        {step === 1 && (
          <Card className="p-6 space-y-4">
            <h2 className="text-xl font-semibold text-neutral-dark">
              Informations de base
            </h2>
            <Input
              label="Nom de l&apos;AREA"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Ex: Notification par email toutes les heures"
              required
            />
            <Textarea
              label="Description (optionnel)"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Décrivez ce que fait votre AREA..."
              rows={4}
            />
            <div className="flex justify-end">
              <Button
                onClick={() => setStep(2)}
                disabled={!canProceedStep1}
              >
                Suivant
                <ArrowRight size={20} className="ml-2" />
              </Button>
            </div>
          </Card>
        )}

        {/* Step 2: Select Action */}
        {step === 2 && (
          <div className="space-y-4">
            <Card className="p-6">
              <div className="flex items-center gap-3 mb-4">
                <Zap className="w-6 h-6 text-primary" />
                <h2 className="text-xl font-semibold text-neutral-dark">
                  Choisir une action
                </h2>
              </div>

              {!selectedActionService ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {services?.map((service) => (
                    <div
                      key={service.id}
                      onClick={() => setSelectedActionService(service)}
                      className="p-4 border border-gray-200 rounded-lg hover:border-primary hover:shadow-sm cursor-pointer transition"
                    >
                      <h3 className="font-medium text-neutral-dark">
                        {service.displayName}
                      </h3>
                      <p className="text-sm text-gray-600 mt-1">
                        {service.actions.length} action(s) disponible(s)
                      </p>
                    </div>
                  ))}
                </div>
              ) : !selectedAction ? (
                <>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setSelectedActionService(null)}
                    className="mb-4"
                  >
                    <ArrowLeft size={16} className="mr-2" />
                    Changer de service
                  </Button>
                  <div className="space-y-3">
                    {selectedActionService.actions.map((action) => (
                      <div
                        key={action.id}
                        onClick={() => setSelectedAction(action)}
                        className="p-4 border border-gray-200 rounded-lg hover:border-primary hover:shadow-sm cursor-pointer transition"
                      >
                        <h3 className="font-medium text-neutral-dark">
                          {action.name}
                        </h3>
                        <p className="text-sm text-gray-600 mt-1">
                          {action.description}
                        </p>
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setSelectedAction(null)}
                    className="mb-4"
                  >
                    <ArrowLeft size={16} className="mr-2" />
                    Changer d'action
                  </Button>
                  <div className="p-4 bg-gray-50 rounded-lg mb-4">
                    <p className="text-sm font-medium text-neutral-dark">
                      {selectedAction.name}
                    </p>
                    <p className="text-sm text-gray-600 mt-1">
                      {selectedAction.description}
                    </p>
                  </div>

                  {selectedAction.parameters.length > 0 && (
                    <div className="space-y-3">
                      <p className="font-medium text-neutral-dark">
                        Configuration
                      </p>
                      {selectedAction.parameters.map((param) => (
                        <Input
                          key={param.name}
                          label={param.name}
                          type={param.type === 'number' ? 'number' : 'text'}
                          placeholder={param.description}
                          required={param.required}
                          value={actionConfig[param.name] || ''}
                          onChange={(e) =>
                            setActionConfig({
                              ...actionConfig,
                              [param.name]: e.target.value,
                            })
                          }
                        />
                      ))}
                    </div>
                  )}
                </>
              )}
            </Card>

            <div className="flex justify-between">
              <Button variant="secondary" onClick={() => setStep(1)}>
                <ArrowLeft size={20} className="mr-2" />
                Retour
              </Button>
              <Button
                onClick={() => setStep(3)}
                disabled={!canProceedStep2}
              >
                Suivant
                <ArrowRight size={20} className="ml-2" />
              </Button>
            </div>
          </div>
        )}

        {/* Step 3: Select Reactions */}
        {step === 3 && (
          <div className="space-y-4">
            <Card className="p-6">
              <div className="flex items-center gap-3 mb-4">
                <RefreshCw className="w-6 h-6 text-accent" />
                <h2 className="text-xl font-semibold text-neutral-dark">
                  Choisir des réactions
                </h2>
              </div>

              {reactions.length > 0 && (
                <div className="space-y-2 mb-6">
                  {reactions.map((reaction, idx) => (
                    <div
                      key={idx}
                      className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                    >
                      <div>
                        <p className="font-medium text-neutral-dark">
                          {reaction.service.displayName}: {reaction.reaction.name}
                        </p>
                        <p className="text-sm text-gray-600">
                          {reaction.reaction.description}
                        </p>
                      </div>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleRemoveReaction(idx)}
                        className="text-error"
                      >
                        Retirer
                      </Button>
                    </div>
                  ))}
                </div>
              )}

              {!currentReactionService ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {services?.map((service) => (
                    <div
                      key={service.id}
                      onClick={() => setCurrentReactionService(service)}
                      className="p-4 border border-gray-200 rounded-lg hover:border-accent hover:shadow-sm cursor-pointer transition"
                    >
                      <h3 className="font-medium text-neutral-dark">
                        {service.displayName}
                      </h3>
                      <p className="text-sm text-gray-600 mt-1">
                        {service.reactions.length} réaction(s) disponible(s)
                      </p>
                    </div>
                  ))}
                </div>
              ) : !currentReaction ? (
                <>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setCurrentReactionService(null)}
                    className="mb-4"
                  >
                    <ArrowLeft size={16} className="mr-2" />
                    Changer de service
                  </Button>
                  <div className="space-y-3">
                    {currentReactionService.reactions.map((reaction) => (
                      <div
                        key={reaction.id}
                        onClick={() => setCurrentReaction(reaction)}
                        className="p-4 border border-gray-200 rounded-lg hover:border-accent hover:shadow-sm cursor-pointer transition"
                      >
                        <h3 className="font-medium text-neutral-dark">
                          {reaction.name}
                        </h3>
                        <p className="text-sm text-gray-600 mt-1">
                          {reaction.description}
                        </p>
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setCurrentReaction(null)}
                    className="mb-4"
                  >
                    <ArrowLeft size={16} className="mr-2" />
                    Changer de réaction
                  </Button>
                  <div className="p-4 bg-gray-50 rounded-lg mb-4">
                    <p className="text-sm font-medium text-neutral-dark">
                      {currentReaction.name}
                    </p>
                    <p className="text-sm text-gray-600 mt-1">
                      {currentReaction.description}
                    </p>
                  </div>

                  {currentReaction.parameters.length > 0 && (
                    <div className="space-y-3 mb-4">
                      <p className="font-medium text-neutral-dark">
                        Configuration
                      </p>
                      {currentReaction.parameters.map((param) => (
                        <Input
                          key={param.name}
                          label={param.name}
                          type={param.type === 'number' ? 'number' : 'text'}
                          placeholder={param.description}
                          required={param.required}
                          value={currentReactionConfig[param.name] || ''}
                          onChange={(e) =>
                            setCurrentReactionConfig({
                              ...currentReactionConfig,
                              [param.name]: e.target.value,
                            })
                          }
                        />
                      ))}
                    </div>
                  )}

                  <Button onClick={handleAddReaction}>
                    Ajouter cette réaction
                  </Button>
                </>
              )}
            </Card>

            <div className="flex justify-between">
              <Button variant="secondary" onClick={() => setStep(2)}>
                <ArrowLeft size={20} className="mr-2" />
                Retour
              </Button>
              <Button
                onClick={() => setStep(4)}
                disabled={!canProceedStep3}
              >
                Suivant
                <ArrowRight size={20} className="ml-2" />
              </Button>
            </div>
          </div>
        )}

        {/* Step 4: Summary */}
        {step === 4 && (
          <div className="space-y-4">
            <Card className="p-6">
              <h2 className="text-xl font-semibold text-neutral-dark mb-4">
                Récapitulatif
              </h2>

              <div className="space-y-4">
                <div>
                  <p className="text-sm text-gray-600">Nom</p>
                  <p className="font-medium text-neutral-dark">{name}</p>
                </div>

                {description && (
                  <div>
                    <p className="text-sm text-gray-600">Description</p>
                    <p className="font-medium text-neutral-dark">
                      {description}
                    </p>
                  </div>
                )}

                <div className="border-t border-gray-200 pt-4">
                  <p className="text-sm text-gray-600 mb-2">Flow</p>
                  <div className="flex items-center gap-2 flex-wrap">
                    <div className="px-3 py-2 bg-primary/10 text-primary rounded-lg font-medium">
                      {selectedActionService?.displayName}: {selectedAction?.name}
                    </div>
                    <span className="text-gray-400">→</span>
                    {reactions.map((reaction, idx) => (
                      <div key={idx}>
                        <div className="px-3 py-2 bg-accent/10 text-accent rounded-lg font-medium">
                          {reaction.service.displayName}: {reaction.reaction.name}
                        </div>
                        {idx < reactions.length - 1 && (
                          <span className="text-gray-400 mx-2">→</span>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </Card>

            <div className="flex justify-between">
              <Button variant="secondary" onClick={() => setStep(3)}>
                <ArrowLeft size={20} className="mr-2" />
                Retour
              </Button>
              <Button
                onClick={handleCreate}
                disabled={createMutation.isPending}
              >
                {createMutation.isPending ? (
                  'Création...'
                ) : (
                  <>
                    <Check size={20} className="mr-2" />
                    Créer l&apos;AREA
                  </>
                )}
              </Button>
            </div>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}
