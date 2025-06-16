
# 🎵 AudioFilesSorter

Un outil Java simple pour trier automatiquement des fichiers audio (MP3) en se basant sur leurs métadonnées ID3 (artiste, album, année, etc.).

---

## 🚀 Fonctionnalités

- 🔍 Analyse récursive des fichiers `.mp3` dans un répertoire source.
- 🏷️ Récupération des tags via [Jaudiotagger](https://bitbucket.org/ijabz/jaudiotagger).
- 📂 Classement automatique des fichiers dans une structure répertoire `Artiste/Album/Titre`.
- 📋 Journalisation complète des opérations :
  - Fichiers sans tags
  - Erreurs de copie
  - Résumé global en fin d’exécution
- 🧪 Mode debug : permet de simuler les actions sans déplacer les fichiers
- 🧹 Nettoyage automatique des anciens fichiers de log au lancement

---

## 📁 Exemple de fonctionnement

### Avant

```
D:\mp3\Rock├── Track1.mp3
├── Album1│   └── Track2.mp3
```

### Après tri (dans `E:\sorted`)

```
E:\sorted└── The Beatles    └── Abbey Road        ├── 01 - Come Together.mp3
        └── 02 - Something.mp3
```

*Exemple basé sur les tags réels des fichiers audio.*

---

## ⚙️ Configuration

La configuration se fait actuellement **dans le code source**, dans la classe `SortMP3Directory.java` :

```java
static String inputDirectory = "D:\mp3\Rock";
static String outputDirectory = "E:\sorted";
static boolean debugMode = false;
```

- `inputDirectory` : dossier source à scanner
- `outputDirectory` : dossier de sortie (créé s’il n’existe pas)
- `debugMode = true` : simule les opérations (aucune copie de fichiers)

---

## 🛠️ Compilation

Le projet utilise **Maven**.

### Étapes :

1. Cloner le dépôt :
   ```bash
   git clone https://github.com/warnotte/AudioFileSorterJava.git
   cd AudioFileSorterJava/AudioFilesSorter
   ```

2. Compiler le projet :
   ```bash
   mvn clean package
   ```

---

## ▶️ Exécution

Lancer la classe principale :

```bash
java -cp "target/AudioFilesSorter.jar;lib/*" io.github.warnotte.SortMP3Directory
```

> 💡 Le projet inclut les fichiers `.jar` nécessaires dans le dossier `lib/` (notamment `jaudiotagger`).

---

## 📑 Logs

À chaque exécution, un dossier `logs/` est créé. Il contient :
- La liste des fichiers non taggés
- La liste des erreurs de copie
- Un résumé général des fichiers traités

---

## 📦 Dépendances

- [`jaudiotagger`](https://bitbucket.org/ijabz/jaudiotagger) – pour la lecture des métadonnées ID3

Les fichiers `.jar` sont déjà inclus dans `/lib`.

---

## 📄 Licence

Ce projet est publié sans licence explicite pour l’instant. N'hésite pas à en ajouter une (`MIT`, `Apache 2.0`, etc.) si besoin.

---

## 🤝 Contribution

Améliorations possibles :
- Prise en charge de la configuration via fichier externe (`.properties`, `YAML`, etc.)
- Interface graphique (JavaFX, Swing)
- Tri multi-format (FLAC, WAV…)
- Tri basé sur genre, année, ou durée
- Ajout d'une interface CLI plus robuste

Pull requests et suggestions bienvenues !
