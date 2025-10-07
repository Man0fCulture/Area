'use client';

import { useAuth } from '@/contexts/AuthContext';
import DashboardLayout from '@/components/layout/DashboardLayout';
import { Card } from '@/components/common/Card';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { User, Mail, Key } from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';

export default function ProfilePage() {
  const { user } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [username, setUsername] = useState(user?.username || '');
  const [email, setEmail] = useState(user?.email || '');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleSave = async () => {
    try {
      // TODO: Implement profile update API
      toast.success('Profil mis à jour');
      setIsEditing(false);
    } catch (error) {
      toast.error('Erreur lors de la mise à jour');
      console.error(error);
    }
  };

  const handleChangePassword = async () => {
    if (newPassword !== confirmPassword) {
      toast.error('Les mots de passe ne correspondent pas');
      return;
    }
    if (newPassword.length < 8) {
      toast.error('Le mot de passe doit contenir au moins 8 caractères');
      return;
    }
    try {
      // TODO: Implement password change API
      toast.success('Mot de passe modifié');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (error) {
      toast.error('Erreur lors du changement de mot de passe');
      console.error(error);
    }
  };

  return (
    <DashboardLayout>
      <div className="max-w-4xl space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-neutral-dark">Profil</h1>
          <p className="text-gray-600 mt-1">
            Gérez vos informations personnelles
          </p>
        </div>

        {/* Profile Info */}
        <Card className="p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold text-neutral-dark">
              Informations personnelles
            </h2>
            <Button
              variant={isEditing ? 'secondary' : 'primary'}
              size="sm"
              onClick={() => (isEditing ? setIsEditing(false) : setIsEditing(true))}
            >
              {isEditing ? 'Annuler' : 'Modifier'}
            </Button>
          </div>

          <div className="space-y-4">
            <Input
              label="Nom d'utilisateur"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={!isEditing}
            />

            <Input
              label="Email"
              type="email"
              value={email}
              disabled={true}
            />

            {isEditing && (
              <div className="flex justify-end gap-3 pt-4">
                <Button variant="secondary" onClick={() => setIsEditing(false)}>
                  Annuler
                </Button>
                <Button onClick={handleSave}>Enregistrer</Button>
              </div>
            )}
          </div>
        </Card>

        {/* Change Password */}
        <Card className="p-6">
          <div className="flex items-center gap-3 mb-4">
            <Key className="w-6 h-6 text-primary" />
            <h2 className="text-xl font-semibold text-neutral-dark">
              Changer le mot de passe
            </h2>
          </div>

          <div className="space-y-4">
            <Input
              label="Mot de passe actuel"
              type="password"
              placeholder="••••••••"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
            />
            <Input
              label="Nouveau mot de passe"
              type="password"
              placeholder="••••••••"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
            <Input
              label="Confirmer le nouveau mot de passe"
              type="password"
              placeholder="••••••••"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />

            <div className="flex justify-end pt-4">
              <Button onClick={handleChangePassword}>Changer le mot de passe</Button>
            </div>
          </div>
        </Card>

        {/* Account Info */}
        <Card className="p-6">
          <h2 className="text-xl font-semibold text-neutral-dark mb-4">
            Informations du compte
          </h2>

          <div className="space-y-3 text-sm">
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">ID utilisateur</span>
              <span className="font-medium text-neutral-dark">{user?.id}</span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">Date de création</span>
              <span className="font-medium text-neutral-dark">
                {user?.createdAt
                  ? new Date(user.createdAt).toLocaleDateString()
                  : 'N/A'}
              </span>
            </div>
          </div>
        </Card>

        {/* Danger Zone */}
        <Card className="p-6 border-error/50">
          <h2 className="text-xl font-semibold text-error mb-4">Zone de danger</h2>
          <p className="text-gray-600 mb-4">
            Supprimer votre compte supprimera toutes vos AREAs et données de manière permanente.
            Cette action est irréversible.
          </p>
          <Button
            variant="primary"
            className="bg-error hover:bg-error/90"
            onClick={() => toast.error('Fonctionnalité non implémentée')}
          >
            Supprimer mon compte
          </Button>
        </Card>
      </div>
    </DashboardLayout>
  );
}
