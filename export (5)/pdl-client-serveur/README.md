# PDL Client-Serveur

Application web de traitement et de gestion d’images, composée d’un frontend développé avec Vue.js et d’un backend Java Spring Boot connecté à une base de données PostgreSQL.

## 📌 Présentation du projet

Ce projet permet de manipuler des images depuis une interface web et de réaliser des opérations liées à leur analyse et à leur comparaison.

## ✨ Fonctionnalités

- Affichage et consultation des images.
- Importation d’images depuis l’interface utilisateur.
- Analyse des images à l’aide d’histogrammes.
- Comparaison et recherche d’images similaires.
- Communication entre le frontend et le backend.
- Stockage des données dans une base de données PostgreSQL.

## 🛠️ Technologies utilisées

### Frontend
- Vue.js 3
- Vite
- JavaScript
- Tailwind CSS

### Backend
- Java 17
- Spring Boot
- Maven
- API REST

### Base de données
- PostgreSQL 16
- Extension pgvector

## 📁 Structure du projet

```text
pdl-client-serveur/
├── frontend/                  # Interface utilisateur Vue.js
├── backend/                   # Application Spring Boot
│   ├── src/
│   │   └── main/
│   │       └── resources/
│   │           └── application.example.properties
│   └── pom.xml
├── .gitignore
└── README.md
```

## ⚙️ Prérequis

Avant de lancer le projet, installez :

- Java 17 ou une version compatible.
- Maven.
- Node.js et npm.
- PostgreSQL 16.
- Git.

## 🚀 Installation et lancement

### 1. Récupérer le projet

```bash
git clone https://github.com/amani-trabelsi/projet_dev.git
cd projet_dev
```

Si nécessaire, placez-vous dans le dossier qui contient les dossiers `frontend` et `backend`.

### 2. Configurer PostgreSQL

Créez une base de données nommée `amt` et configurez un utilisateur disposant des droits nécessaires.

Activez également l’extension pgvector si elle est utilisée par votre installation :

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 3. Configurer le backend

Depuis la racine du projet :

```bash
cp backend/src/main/resources/application.example.properties backend/src/main/resources/application.properties
```

Modifiez ensuite `backend/src/main/resources/application.properties` pour renseigner les paramètres de connexion à votre base de données locale.

Vérifiez notamment :
- Le port PostgreSQL.
- Le nom de la base de données.
- Le nom d’utilisateur.
- Le mot de passe.

**Ne publiez jamais votre fichier `application.properties` contenant des identifiants personnels.**

### 4. Lancer le backend

Dans un terminal :

```bash
cd backend
mvn spring-boot:run
```

Le backend démarre sur le port **8686**.

### 5. Installer et lancer le frontend

Dans un autre terminal :

```bash
cd frontend
npm install
npm run dev -- --host 0.0.0.0
```

Le frontend est accessible à l’adresse :

http://localhost:4000/

## 🔗 Accès à l’application

- **Frontend :** http://localhost:4000/
- **Backend :** http://localhost:8686/

Le backend doit être démarré pour que le frontend puisse communiquer avec lui.

## 🔒 Sécurité

- Ne publiez pas de mots de passe ni de clés secrètes.
- Utilisez une configuration locale pour les identifiants de la base de données.
- Conservez `application.example.properties` comme modèle de configuration.
- Vérifiez les fichiers suivis par Git avant chaque publication.

## 👩‍💻 Auteur

**Amani Trabelsi**

Projet de développement client-serveur.
