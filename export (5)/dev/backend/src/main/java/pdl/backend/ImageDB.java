package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.postgresql.util.PGobject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

@Repository
public class ImageDB implements InitializingBean {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        initializeDatabase();
        loadImagesAndHistos();
    }

    private static final String IMAGE_FOLDER = "images";
    private HistoEtSimilarite histo = new HistoEtSimilarite();

    private void initializeDatabase() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS histogrammes CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS images CASCADE");

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS images (" +
                        "id BIGSERIAL PRIMARY KEY, " +
                        "name VARCHAR(255), " +
                        "data BYTEA)");

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS histogrammes (" +
                        "id BIGSERIAL PRIMARY KEY, " +
                        "histoRGB VECTOR(256), " +
                        "histoTeinteSaturation VECTOR(256), " +
                        "FOREIGN KEY (id) REFERENCES images(id) ON DELETE CASCADE" +
                        ")");

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "email VARCHAR(255) PRIMARY KEY, " +
                        "name VARCHAR(255), " +
                        "password VARCHAR(255)," +
                        "favorites VARCHAR(255)[] DEFAULT '{}'" +
                        ")");

    }

    // verifier l'extension de l'image a enregistrer
    private boolean isValidImage(String fileName) {
        if (!fileName.contains(".")) {
            return false;
        }
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return ImageType.isSupported(extension); // Utiliser `extension` ici
    }

    // convertir le file a enregistrer en objet Image pour l'ajouter a la base de
    // donnée
    private Image convertFileToImage(File file) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            String name = file.getName();
            return new Image(name, data);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la lecture du fichier : " + file.getName(), e);
        }
    }

    // Pour calculer les histogrammes
    private Planar<GrayU8> convertToPlanarGrayU8(byte[] imageData) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            BufferedImage bufferedImage = ImageIO.read(bis);
            if (bufferedImage == null) {
                throw new IOException("L'image est invalide ou ne peut pas être lue.");
            }
            Planar<GrayU8> planarImage = new Planar<>(GrayU8.class, bufferedImage.getWidth(), bufferedImage.getHeight(),
                    3);
            ConvertBufferedImage.convertFrom(bufferedImage, planarImage, true);
            return planarImage;
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
            return null;
        }
    }

    // accéder au images dans le dossier images et ajouter les avec leurs
    // histogrammes dans la base de donnée
    private void loadImagesAndHistos() throws IOException {
        File folder = new File(IMAGE_FOLDER);
        try {
            if (!folder.exists() || !folder.isDirectory()) {
                throw new RuntimeException(
                        "Le dossier 'images' est introuvable. Veuillez le créer avant de lancer le serveur.");
            }
        } catch (RuntimeException e) {
            System.err.println("Erreur : " + e.getMessage());
        }

        File[] files = folder.listFiles((dir, name) -> isValidImage(name));
        if (files != null) {
            for (File file : files) {
                Image image = convertFileToImage(file);
                addImagesToDB(image);
            }
        }
    }

    // ajouter dans la base de donnée les images stockées dans le dossier et leur
    // histogrammes
    public void addImagesToDB(Image image) {
        String sql = "INSERT INTO images (id, name, data) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, image.getId(), image.getName(), image.getData());
        Planar<GrayU8> planarImage = convertToPlanarGrayU8(image.getData());
        if (planarImage != null) {
            try {
                Map<String, Double> histogramRGB = histo.histoRGB(planarImage);
                double[][] histogramTeinteSaturation = histo.histoTeinteSaturation(planarImage);
                addHistogramToDB(image.getId(), histogramRGB, histogramTeinteSaturation);
            } catch (IllegalArgumentException e) {
                System.err.println("Erreur lors du calcul de l'histogramme RGB : " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Erreur inattendue : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("Erreur : L'image n'a pas pu être convertie en Planar<GrayU8>.");
        }
    }

    public void addHistogramToDB(Long imageId, Map<String, Double> histogramRGB, double[][] histogramTS) {
        try {
            PGobject histoRGBObj = histo.convertHistoRGBToVector(histogramRGB);
            PGobject histoTSObj = histo.convertHistoTSToVector(histogramTS); // Assume you update this method too

            String sql = "INSERT INTO histogrammes (id, histoRGB, histoTeinteSaturation) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, imageId, histoRGBObj, histoTSObj);
        } catch (Exception e) {
            System.err.println("Erreur SQL lors de l'ajout des histogrammes : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // tester si le nom de l'image existe dans le dossier ou pas si oui il générer
    // un nouveau nom en ajoutant le numéro de copie a coté de nom et apres stocker
    // l'image dans le dossier et la base de donnée
    public void addImageToDB(Image image) {
        String originalName = image.getName();
        String baseName = originalName;
        String extension = "";

        // Extraire le nom de base et l'extension
        if (originalName.contains(".")) {
            int lastDotIndex = originalName.lastIndexOf(".");
            baseName = originalName.substring(0, lastDotIndex);
            extension = originalName.substring(lastDotIndex);
        }

        // Vérifier si le nom existe déjà et générer un nouveau avec le numero de copies
        // si nécessaire
        String newName = originalName;
        int copyNumber = 1;

        while (imageExistsInDB(newName) || imageExistsOnDisk(newName)) {
            newName = baseName + "(" + copyNumber + ")" + extension;
            copyNumber++;
        }
        if (!newName.equals(originalName)) {
            image.setName(newName);
        }
        saveImageToDisk(image);
        addImagesToDB(image);
    }

    // verifier si l'image existe dans la base de donnée
    public boolean imageExistsInDB(String imageName) {
        String sql = "SELECT COUNT(*) FROM images WHERE name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, imageName);
        return count != null && count > 0;
    }

    // verifier si l'image existe dans le dossier images
    private boolean imageExistsOnDisk(String imageName) {
        File imageFile = new File(IMAGE_FOLDER, imageName);
        return imageFile.exists();
    }

    // stocher l'image dans le dossier images
    private void saveImageToDisk(Image image) {
        File folder = new File(IMAGE_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File imageFile = new File(folder, image.getName());
        try (OutputStream os = new FileOutputStream(imageFile)) {
            os.write(image.getData());
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement de l'image : " + e.getMessage());
        }
    }

    // supprimer une image de la base de donnée et de dossier images
    public void deleteImage(Long id) {
        String sql = "SELECT name FROM images WHERE id = ?";
        String imageName = jdbcTemplate.queryForObject(sql, String.class, id);

        String sql1 = "DELETE FROM histogrammes WHERE id = ?";
        String sql2 = "DELETE FROM images WHERE id = ?";
        int rowsAffected1 = jdbcTemplate.update(sql1, id);
        int rowsAffected2 = jdbcTemplate.update(sql2, id);

        if (rowsAffected1 > 0 && rowsAffected2 > 0) {
            File imageFile = new File(IMAGE_FOLDER, imageName);
            if (imageFile.exists() && imageFile.delete()) {
                System.out.println("Image supprimée du disque : " + imageFile.getAbsolutePath());
            } else {
                System.err.println("Erreur : Impossible de supprimer l'image du disque.");
            }

            System.out.println("Image avec ID " + id + " supprimée avec succès");
        } else {
            System.out.println("Pas d'image avec nom " + imageName + " à supprimer");
        }
    }

    public Image getImageById(Long id) {
        String sql = "SELECT * FROM images WHERE id = ?";
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, id);
            Long imageId = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");
            byte[] data = (byte[]) row.get("data");
            return new Image(imageId, name, data);
        } catch (Exception e) {
            return null;
        }
    }

    // renvoie la liste des images stockées dans la base de donnée
    public ArrayList<Image> listImages() {
        String sql = "SELECT * FROM images";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        ArrayList<Image> images = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long imageId = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");
            byte[] data = (byte[]) row.get("data");
            images.add(new Image(imageId, name, data));
        }
        return images;
    }

    // renvoie la liste des images similaire a une image donnée
    public HashMap<Double, Image> compareQueryImageWithAllImages(Long queryImageId, int N, String descriptor) {
        HashMap<Double, Image> topNSimilarImages = new HashMap<>();
        String sql;

        Descriptor descriptorType = Descriptor.fromString(descriptor);
        switch (descriptorType) {
            case HISTO_RGB:
                sql = "SELECT i.id, i.name, i.data, 1 / (1 + (h.histoRGB <-> q.histoRGB)) AS similarity " +
                        "FROM images i " +
                        "JOIN histogrammes h ON i.id = h.id " +
                        "JOIN histogrammes q ON q.id = ? " +
                        "WHERE i.id != ? " +
                        "ORDER BY similarity DESC LIMIT ?";
                break;

            case HISTO_TEINTE_SATURATION:
                sql = "SELECT i.id, i.name, i.data, 1 / (1 + (h.histoTeinteSaturation <-> q.histoTeinteSaturation)) AS similarity "
                        +
                        "FROM images i " +
                        "JOIN histogrammes h ON i.id = h.id " +
                        "JOIN histogrammes q ON q.id = ? " +
                        "WHERE i.id != ? " +
                        "ORDER BY similarity DESC LIMIT ?";
                break;

            case COMBINED:
                sql = "SELECT i.id, i.name, i.data, 1 / (1 + ((h.histoRGB <-> q.histoRGB) + (h.histoTeinteSaturation <-> q.histoTeinteSaturation)) / 2) AS similarity "
                        +
                        "FROM images i " +
                        "JOIN histogrammes h ON i.id = h.id " +
                        "JOIN histogrammes q ON q.id = ? " +
                        "WHERE i.id != ? " +
                        "ORDER BY similarity DESC LIMIT ?";
                break;

            default:
                throw new IllegalArgumentException("Descripteur non reconnu.");
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, queryImageId, queryImageId, N);

        for (Map<String, Object> row : rows) {
            Long imageId = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");
            byte[] data = (byte[]) row.get("data");
            Double similarity = (Double) row.get("similarity");
            System.out.println(similarity);
            topNSimilarImages.put(similarity, new Image(imageId, name, data));
        }

        return topNSimilarImages;
    }

    // Récupérer la liste des favoris de l'utilisateur et vérifier si l'image est
    // déjà likée sinon update la ligne de la base de donnée et ajuter l'image dans
    // la liste
    public String addLikeToImage(String imageName, String email) {

        String sqlCheckLike = "SELECT favorites FROM users WHERE email = ?";
        String[] likedList = jdbcTemplate.queryForObject(sqlCheckLike, new Object[] { email },
                (rs, rowNum) -> (String[]) rs.getArray("favorites").getArray());

        if (likedList != null && Arrays.asList(likedList).contains(imageName)) {
            System.out.println("Image " + imageName + " is already liked.");
            return "Image is already liked.";
        }

        String sqlUpdateFavorites = "UPDATE users SET favorites = array_append(favorites, ?) WHERE email = ?";
        jdbcTemplate.update(sqlUpdateFavorites, imageName, email);

        System.out.println("Image " + imageName + " liked successfully.");
        return "Image liked successfully.";
    }

    // Vérifier si l'image est bien dans les favoris
    public String removeLikeFromImage(String imageName, String email) {
        String sqlCheckLike = "SELECT favorites FROM users WHERE email = ?";
        String[] likedList = jdbcTemplate.queryForObject(sqlCheckLike, new Object[] { email },
                (rs, rowNum) -> (String[]) rs.getArray("favorites").getArray());

        if (likedList == null || !Arrays.asList(likedList).contains(imageName)) {
            System.out.println("Image " + imageName + " was not liked by user " + email + ".");
            return "Image was not liked at all.";
        }

        String sqlUpdateFavorites = "UPDATE users SET favorites = array_remove(favorites, ?) WHERE email = ?";
        jdbcTemplate.update(sqlUpdateFavorites, imageName, email);

        System.out.println("Image " + imageName + " unliked successfully by user " + email + ".");
        return "Image unliked successfully.";
    }

    // Récupérer la liste des images likées par l'utilisateur
    // Récupérer les détails des images dans cette liste
    public List<Image> listLikedImages(String email) {
        String sqlFavorites = "SELECT favorites FROM users WHERE email = ?";
        String[] likedList = jdbcTemplate.queryForObject(sqlFavorites, new Object[] { email },
                (rs, rowNum) -> (String[]) rs.getArray("favorites").getArray());

        if (likedList == null || likedList.length == 0) {
            return new ArrayList<>();
        }

        String sql = "SELECT id, name, data FROM images WHERE name = ANY(?)";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, (Object) likedList);

        List<Image> likedImages = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long imageId = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");
            byte[] data = (byte[]) row.get("data");
            likedImages.add(new Image(imageId, name, data));
        }
        return likedImages;
    }

    // verifier si le mail existe dans la table users si il existe pas en ajoute le
    public User signup(String email, String password, String name) {
        String sql = "SELECT * FROM users WHERE email = ?";
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, email);
            if (!rows.isEmpty()) {
                return null; // user already exists
            }

            String hashedPassword = encoder.encode(password);
            String sqlIn = "INSERT INTO users (email, name, password) VALUES (?, ?, ?)";
            jdbcTemplate.update(sqlIn, email, name, hashedPassword);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
        return new User(name, password, email);
    }

    // chercher le user et verifier son mot de passe dans la table users
    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, email);
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (row == null || row.isEmpty()) {
                return null;
            }

            String name = (String) row.get("name");
            String storedPassword = (String) row.get("password");
            if (!encoder.matches(password, storedPassword)) {
                return null;
            }

            return new User(name, password, email);

        } catch (Exception e) {
            System.out.println("An error occurred during login: " + e.getMessage());
            return null;
        }
    }

    boolean verifierUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";
        Map<String, Object> row = jdbcTemplate.queryForMap(sql, email);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String storedPassword = (String) row.get("password");
        return encoder.matches(password, storedPassword);
    }

    // pour la liste des favorites car on stocke les images favorit par e nom et pas
    public Image getImageByName(String Imagename) {
        String sql = "SELECT * FROM images WHERE name = ?";
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, Imagename);
            Long imageId = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");
            byte[] data = (byte[]) row.get("data");
            return new Image(imageId, name, data);
        } catch (Exception e) {
            return null;
        }
    }

}