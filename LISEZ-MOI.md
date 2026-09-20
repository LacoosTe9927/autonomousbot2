# Autonomous Bot — mod Minecraft 100% local

Ce mod ajoute une créature **"Bot Autonome"** qui explore, coupe du bois,
mine de la pierre/du minerai, se construit un abri la nuit, se défend et
"monte de niveau" toute seule — **sans clé API, sans IA externe, sans
connexion internet en jeu**. Toute la "décision" est du code Java classique
(recherche du bloc le plus proche, seuils de ressources, heure du jour) dans
`src/main/java/com/autonomousbot/mod/`.

## ⚠️ Important — sois honnête avec toi-même sur ce que j'ai pu tester

Je n'ai **aucun accès réseau** dans mon environnement, donc je n'ai pas pu
télécharger les bibliothèques Minecraft/Fabric ni compiler ce mod pour
vérifier qu'il compile sans la moindre erreur. Le code suit les conventions
standards de l'API Fabric pour Minecraft 1.21.1, mais **il est possible qu'il
y ait 1 ou 2 petites erreurs de compilation** (noms de méthodes qui changent
légèrement d'une version à l'autre). Si `gradlew build` affiche une erreur,
**colle-moi le message d'erreur** et je corrige immédiatement — c'est rapide
à fixer une fois qu'on voit le message exact.

## Compiler le mod via GitHub (le plus simple — rien à installer chez toi)

1. Crée un compte sur [github.com](https://github.com) si tu n'en as pas.
2. Clique sur **New repository**, donne-lui un nom (ex: `autonomousbot`), laisse-le en **Public** ou **Private**, clique **Create repository**.
3. Sur la page du nouveau repo, clique **uploading an existing file** (ou glisse-dépose), puis **dézippe** `autonomousbot-mod.zip` sur ton ordinateur et glisse **tout le contenu du dossier** `autonomousbot` (pas le dossier lui-même, son contenu) dans la zone d'upload de GitHub. Valide avec **Commit changes**.
4. Va dans l'onglet **Actions** en haut du repo. Une compilation démarre automatiquement (grâce au fichier `.github/workflows/build.yml` inclus). Attends 2-3 minutes que le rond jaune devienne une coche verte ✅.
5. Clique sur le run terminé, descends jusqu'à **Artifacts**, télécharge `autonomousbot-jar` — c'est un zip contenant ton fichier `.jar` compilé, prêt à mettre dans ton dossier `mods` Minecraft (voir section "Installer et jouer" ci-dessous).

Si la coche est rouge ❌ au lieu de verte, clique dessus, ouvre l'étape "Build with Gradle" et copie-colle-moi le message d'erreur — je corrige le code et tu recommences (juste re-uploader le fichier corrigé).

## Compiler le mod chez toi (alternative)

1. Installe le JDK 21 (Temurin/Adoptium, gratuit).
2. Dans le dossier `autonomousbot`, ouvre un terminal et lance :
   - Windows : `gradlew.bat build`
   - Mac/Linux : `./gradlew build`
   (la première fois, Gradle télécharge Minecraft/Fabric, ça prend quelques minutes)
3. Le fichier `.jar` compilé apparaît dans `build/libs/autonomousbot-1.0.0.jar`.

## Installer et jouer

1. Installe **Fabric Loader** pour Minecraft 1.21.1 (fabricmc.net/use/installer).
2. Lance une fois le profil Fabric dans le launcher Minecraft pour créer le dossier `mods`.
3. Copie `autonomousbot-1.0.0.jar` **et** le mod **Fabric API** (à télécharger sur Modrinth/CurseForge, version 1.21.1) dans le dossier `.minecraft/mods`.
4. Lance Minecraft avec le profil Fabric, crée un monde.
5. En jeu, ouvre l'inventaire créatif (mode créatif) ou tape en commande :
   `/give @p autonomousbot:autonomous_bot_spawn_egg`
6. Pose l'œuf pour faire apparaître le Bot Autonome. Il se met en action tout
   seul immédiatement : il part chercher du bois, puis de la pierre, se bâtit
   un abri au coucher du soleil, combat les monstres qui l'attaquent, et
   annonce dans le chat quand il "monte de niveau" (plus de vie, plus de
   dégâts) au fur et à mesure qu'il accumule des ressources.

## Ce qu'il fait concrètement (aucune IA, juste des règles)

- **Bûcheron** : cherche le bloc de bois le plus proche dans un rayon de 12 blocs, s'y déplace, le casse.
- **Mineur** : cherche pierre/minerai le plus proche (rayon 10), le casse.
- **Bâtisseur** : la nuit (ou si sa vie est basse) et s'il a assez de pierre, il s'entoure d'un petit abri en cobblestone.
- **Combattant** : attaque au corps-à-corps tout monstre hostile à proximité, riposte s'il est attaqué.
- **Évolution** : tous les 32 × niveau ressources récoltées, il gagne un niveau (jusqu'à 10) → plus de vie, plus de dégâts. C'est ça, le "il évolue tout seul" — un système de seuils, pas une IA.

## Personnaliser

- Rayon de recherche, vitesse de coupe/minage : en haut de `ChopTreeGoal.java` / `MineBlockGoal.java`.
- Apparence : actuellement il réutilise le modèle et la texture du Golem de Fer (`AutonomousBotModClient.java`) pour éviter d'avoir besoin d'un fichier 3D/texture personnalisé. Tu peux le changer si tu sais faire un modèle/texture custom.
- Stats de départ (vie, dégâts, vitesse) : `AutonomousBotEntity.createAttributes()`.
