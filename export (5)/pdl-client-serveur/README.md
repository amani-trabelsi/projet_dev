#  Projet similarité

---

##  Backend

###  Serveur

Dans ce projet, nous avons commencé par la création et l'initialisation des tables **images** et **histogrammes** dans la base de données. Cela permet :
- *d'ajouter une image*
- *d'accéder à une image*
- *de supprimer une image*
- *de construire la liste des images disponibles*
- *de construire la liste des images similaire à une image donnée*

#### Structure des Tables 
#####  **Table `image`**
- La table image possède une clé primaire `id` attribuée automatiquement pour identifier de manière unique chaque image. Elle comprend également un champ `data` contenant les données binaires de l'image (sous forme de bytes) et un champ `name` qui stocke le nom du fichier.

#####  **Table `histogrammes`**
- La table histogrammes est utilisée pour stocker les histogrammes calculés pour chaque image. Elle contient une clé primaire et étrangère à la fois `id`, correspondant à l'ID de l'image associée, ainsi que deux autres champs, `histoRGB` et `histoTeinteSaturation`. (L'intégrité des données est garantie par une contrainte qui supprime automatiquement l'histogramme lié lorsque l'image associée est supprimée aussi.)
- Les histogrammes sont utilisés pour rechercher la similarité entre les images via la **distance euclidienne** dans la classe `HistoetSimilarite.java`.


### Communication
- Dans la partie communication, on a mis en place des requêtes HTTP entre le client et le serveur pour gérer l’ajout, la récupération et la suppression d’images, ainsi que la recherche d’images similaires.
- Les images sont échangées au format JSON, contenant leur ID, nom, type, taille et données encodées en base64 (Seuls les formats JPEG et PNG sont acceptés pour l’ajout d’images.)
- Pour la recherche d’images similaires, le serveur retourne une liste triée en fonction d’un score de similarité, calculé avec deux types de descripteurs : histoRGB, histoTeinteSaturation ou bien les deux à la fois.
- Enfin, les erreurs sont gérées avec des codes HTTP adaptés, comme 404 si une image n’existe pas ou 415 pour un format non pris en charge.

## Frontend

###  Client
- Le client communique avec le serveur pour afficher et gérer les images. Celles-ci sont récupérées en JSON, encodées en base64, puis décodées pour être affichées dans une galerie.
- En cliquant sur une image, l’utilisateur peut la voir en grand avec ses détails (nom, type, taille), la **télécharger** ou la **supprimer**. Il peut aussi **rechercher des images similaires** en choisissant un descripteur et un nombre de résultats, le serveur renvoie alors une liste classée par score de similarité.
- L’utilisateur peut aussi ajouter une image choisie dans son système de fichiers aux images disponibles sur le serveur
- L'interface utilisateur est conçue avec **Tailwind CSS**, offrant un design épuré et réactif.

##  Testé avec
 **Ubuntu ≥ 20.04**  
 **Google Chrome**


## Nouvelles fonctionnalités
- Expliquées dans le cahier des besoins