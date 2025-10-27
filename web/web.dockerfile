# === Étape 1 : Build de l’application ===
FROM node:20-alpine AS builder

# Définit le dossier de travail
WORKDIR /app

# Copie uniquement les fichiers nécessaires à l’installation
COPY package*.json ./

# Installe les dépendances
RUN npm install

# Copie le reste du projet
COPY . .

# Build de l’application Next.js pour la production
RUN npm run build

# === Étape 2 : Image finale, légère et optimisée ===
FROM node:20-alpine AS runner

# Définit le dossier de travail
WORKDIR /app
ENV NODE_ENV=production

# Copie uniquement le nécessaire depuis l’étape précédente
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/next.config.ts ./next.config.ts

# Expose le port (80 interne à ton container)
EXPOSE 80

# Commande de lancement (Next.js en mode production)
CMD ["npm", "run", "start", "--", "-p", "80"]