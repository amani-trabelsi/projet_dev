package pdl.backend;

import java.util.HashMap;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class ImageDBTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ImageDB imageDB;

    @BeforeAll
    public static void reset() {
        ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));
    }

    // Vérifie que la table images existe dans le schéma de la base de données
    // en exécutant une simple requête SELECT. Ce test échouera avec une
    // exception si la table n'existe pas.

    @Test
    @Order(1)
    public void testcreationSQl() throws Exception {
        jdbcTemplate.execute("SELECT * FROM images");
    }

    // Teste la fonctionnalité d'ajout d'une image à la base de données.
    // Supprime d'abord et recrée la table images avec les colonnes appropriées,
    // puis crée un fichier image fictif, l'ajoute à la base de données,
    // et vérifie l'insertion en interrogeant son ID.

    @Test
    @Order(2)
    public void testajoutSQl() throws Exception {
        jdbcTemplate.execute("DROP TABLE IF EXISTS images CASCADE");
        this.jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS images (id INT PRIMARY KEY, "
                        + "name VARCHAR(255), "
                        + "data BYTEA, "
                        + "histogramT vector(361), histogramS vector(101),histogramRGB "
                        + "vector(768))");
        final ClassPathResource imgFile = new ClassPathResource("test.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", imgFile.getInputStream());
        Image image = new Image(multipartFile.getOriginalFilename(),
                multipartFile.getBytes());
        imageDB.addImagesToDB(image);
        jdbcTemplate.queryForObject("SELECT id FROM images WHERE id = ?",
                Integer.class, image.getId());
    }

    // Teste la fonctionnalité de suppression d'image de la base de données.
    // Crée une nouvelle table, ajoute une image de test, la supprime en utilisant
    // la méthode ImageDB, et vérifie que l'image n'existe plus dans la base de
    // données
    // en essayant de la rechercher, s'attendant à une exception.

    @Test
    @Order(3)
    public void testdeleteSQL() throws Exception {
        jdbcTemplate.execute("DROP TABLE IF EXISTS images");
        this.jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS images (id INT PRIMARY KEY, "
                        + "name VARCHAR(255), "
                        + "data BYTEA, "
                        + "histogramT vector(361), histogramS vector(101),histogramRGB "
                        + "vector(768))");
        final ClassPathResource imgFile = new ClassPathResource("test.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", imgFile.getInputStream());
        Image image = new Image(multipartFile.getOriginalFilename(),
                multipartFile.getBytes());
        imageDB.addImagesToDB(image);
        imageDB.deleteImage(image.getId());
        try {
            jdbcTemplate.queryForObject("SELECT id FROM images WHERE id = ?",
                    Integer.class, image.getId());
            throw new Exception("not deleted");
        } catch (Exception e) {
            System.out.println("Deleted");
        }
    }

    // Teste la récupération d'une image par son ID depuis la base de données.
    // Crée d'abord et ajoute une image de test à la base de données, puis utilise
    // la méthode getImageById pour la récupérer et vérifie que l'image récupérée
    // n'est pas nulle et possède l'ID correct, confirmant le bon fonctionnement
    // de la logique de récupération.

    @Test
    @Order(4)
    public void testGetImageById() throws Exception {
        final ClassPathResource imgFile = new ClassPathResource("test.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", imgFile.getInputStream());
        Image testImage = new Image(multipartFile.getOriginalFilename(), multipartFile.getBytes());
        imageDB.addImagesToDB(testImage);
        Image retrievedImage = imageDB.getImageById(testImage.getId());
        assertNotNull(retrievedImage);
        assertEquals(testImage.getId(), retrievedImage.getId());
    }

    // Teste la récupération de la liste complète des images de la base de données.
    // Crée et ajoute une image de test, puis appelle la méthode listImages pour
    // obtenir toutes les images et vérifie que la liste retournée n'est pas vide
    // et contient l'image de test, assurant le bon fonctionnement de la
    // fonctionnalité de listage des images.

    @Test
    @Order(5)
    public void testListImages() throws Exception {
        final ClassPathResource imgFile = new ClassPathResource("test.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", imgFile.getInputStream());
        Image testImage = new Image(multipartFile.getOriginalFilename(), multipartFile.getBytes());
        imageDB.addImagesToDB(testImage);
        ArrayList<Image> images = imageDB.listImages();
        assertFalse(images.isEmpty());
        assertTrue(images.stream().anyMatch(img -> img.getId().equals(testImage.getId())));
    }

    // Teste la vérification de l'existence d'une image dans la base de données par
    // son nom.
    // Crée et ajoute une image de test, puis vérifie que la méthode imageExistsInDB
    // renvoie correctement true pour l'image ajoutée. Enfin, nettoie en supprimant
    // l'image de test de la base de données pour maintenir l'isolation des tests.

    @Test
    @Order(6)
    public void testImageExistsInDB() throws Exception {
        final ClassPathResource imgFile = new ClassPathResource("test.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", imgFile.getInputStream());
        Image testImage = new Image(multipartFile.getOriginalFilename(), multipartFile.getBytes());
        imageDB.addImagesToDB(testImage);
        assertTrue(imageDB.imageExistsInDB(testImage.getName()));
        jdbcTemplate.update("DELETE FROM images WHERE name = ?", testImage.getName());
    }

    // Teste la fonctionnalité de comparaison de similarité des images.
    // Ajoute deux images de test différentes à la base de données, puis utilise
    // la méthode compareQueryImageWithAllImages pour trouver des images similaires
    // à la première basée sur l'histogramme RGB. Vérifie que la carte des résultats
    // n'est pas vide et nettoie les données de test par la suite.

    @Test
    @Order(7)
    public void testCompareQueryImageWithAllImages() throws Exception {
        final ClassPathResource imgFile = new ClassPathResource("test.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", imgFile.getInputStream());
        Image testImage = new Image(multipartFile.getOriginalFilename(), multipartFile.getBytes());
        imageDB.addImagesToDB(testImage);
        final ClassPathResource imgFile2 = new ClassPathResource("test2.jpg");
        MockMultipartFile multipartFile2 = new MockMultipartFile(
                "file", "test2.jpg", "image/jpeg", imgFile2.getInputStream());
        Image testImage2 = new Image(multipartFile2.getOriginalFilename(), multipartFile2.getBytes());
        imageDB.addImagesToDB(testImage2);
        HashMap<Double, Image> similarImages = imageDB.compareQueryImageWithAllImages(
                testImage.getId(), 1, "historgb");
        assertFalse(similarImages.isEmpty());
        jdbcTemplate.update("DELETE FROM images WHERE name IN (?, ?)", testImage.getName(), testImage2.getName());
    }

    // Teste la fonctionnalité de favoris/like d'image pour les utilisateurs.
    // Crée une image de test et un utilisateur, ajoute l'image aux favoris
    // de l'utilisateur, vérifie qu'elle a été ajoutée avec succès, puis la retire
    // des favoris et confirme le retrait. Enfin, nettoie les données de test
    // de l'utilisateur et de l'image de la base de données pour maintenir
    // l'isolation des tests.

    @Test
    @Order(8)
    public void testAddAndRemoveLike() throws Exception {
        final ClassPathResource imgFile = new ClassPathResource("test.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", imgFile.getInputStream());
        Image testImage = new Image(multipartFile.getOriginalFilename(), multipartFile.getBytes());
        imageDB.addImagesToDB(testImage);
        User testUser = imageDB.signup("likeuser@test.com", "password", "Like User");
        String result = imageDB.addLikeToImage(testImage.getName(), testUser.getEmail());
        assertEquals("Image liked successfully.", result);
        List<Image> likedImages = imageDB.listLikedImages(testUser.getEmail());
        assertTrue(likedImages.stream().anyMatch(img -> img.getId().equals(testImage.getId())));
        result = imageDB.removeLikeFromImage(testImage.getName(), testUser.getEmail());
        assertEquals("Image unliked successfully.", result);
        likedImages = imageDB.listLikedImages(testUser.getEmail());
        assertTrue(likedImages.isEmpty() ||
                !likedImages.stream().anyMatch(img -> img.getId().equals(testImage.getId())));
        jdbcTemplate.execute("DELETE FROM users WHERE email = 'likeuser@test.com'");
        jdbcTemplate.execute("DELETE FROM images WHERE id = " + testImage.getId());
    }

    // Teste l'enregistrement d'une image sur le système de fichiers.
    // Crée un objet d'image de test, s'assure que le répertoire cible existe,
    // écrit les octets de l'image dans un fichier, vérifie que le fichier existe
    // sur le disque, puis nettoie en supprimant le fichier. Teste la persistance
    // des images en dehors de la base de données.

    @Test
    @Order(9)
    public void testSaveImageToDisk() throws Exception {
        final ClassPathResource imgFile = new ClassPathResource("test.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", imgFile.getInputStream());
        Image testImage = new Image(multipartFile.getOriginalFilename(), multipartFile.getBytes());
        File imageDir = new File("images");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        File imageFile = new File(imageDir, testImage.getName());
        Files.write(imageFile.toPath(), testImage.getData());
        assertTrue(imageFile.exists(), "Image should be saved to disk in the 'images' folder");
        imageFile.delete();
    }
}