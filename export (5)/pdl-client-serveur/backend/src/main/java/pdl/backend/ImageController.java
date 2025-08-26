package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDB imageDB;

  @Autowired
  public ImageController(ImageDB imageDB) {
    this.imageDB = imageDB;
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<ObjectNode> getImage(@PathVariable("id") long id) {

    Image image = imageDB.getImageById(id);

    if (image != null) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      objectNode.put("type", image.getType());
      objectNode.put("size", image.getSize());
      String base64Data = Base64.getEncoder().encodeToString(image.getData());
      objectNode.put("bytes", base64Data);
      return ResponseEntity.status(HttpStatus.OK).body(objectNode);
    }
    ObjectNode errorNode = mapper.createObjectNode();
    errorNode.put("error", "Image not found");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorNode);
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {

    Image image = imageDB.getImageById(id);

    if (image != null) {
      imageDB.deleteImage(id);
      return ResponseEntity.status(HttpStatus.OK).body("Image id=" + id + " deleted.");
    }
    ObjectNode errorNode = mapper.createObjectNode();
    errorNode.put("error", "Image not found");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorNode);
  }

  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

    String contentType = file.getContentType();
    if ((!contentType.equals(MediaType.IMAGE_JPEG.toString()))
        && (!contentType.equals(MediaType.IMAGE_PNG.toString()))) {
      return new ResponseEntity<>("Only JPEG and PNG file format supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    try {
      imageDB.addImageToDB(new Image(file.getOriginalFilename(), file.getBytes()));
    } catch (IOException e) {
      return new ResponseEntity<>("Failure to read file", HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>("Image uploaded", HttpStatus.CREATED);
  }

  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ArrayNode getImageList() {
    List<Image> images = imageDB.listImages();
    ArrayNode nodes = mapper.createArrayNode();
    for (Image image : images) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      objectNode.put("type", image.getType());
      objectNode.put("size", image.getSize());
      String base64Data = Base64.getEncoder().encodeToString(image.getData());
      objectNode.put("bytes", base64Data);
      nodes.add(objectNode);
    }
    return nodes;
  }

  @RequestMapping(value = "/images/{id}/similar", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> getSimilarImages(
      @PathVariable("id") long id,
      @RequestParam("number") int number,
      @RequestParam("descriptor") String descriptor) {

    try {
      Descriptor descriptorEnum = Descriptor.fromString(descriptor);

      Image queryImage = imageDB.getImageById(id);
      if (queryImage == null) {
        ObjectNode errorNode = mapper.createObjectNode();
        errorNode.put("error", "Image not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorNode);
      }
      HashMap<Double, Image> similarImages = imageDB.compareQueryImageWithAllImages(id, number,
          descriptorEnum.getValue());
      ArrayNode similarImagesNode = mapper.createArrayNode();
      for (Double i : similarImages.keySet()) {
        ObjectNode imageNode = mapper.createObjectNode();
        imageNode.put("score", i);
        imageNode.put("id", similarImages.get(i).getId());
        imageNode.put("name", similarImages.get(i).getName());
        imageNode.put("type", similarImages.get(i).getType());
        imageNode.put("size", similarImages.get(i).getSize());
        String base64Data = Base64.getEncoder().encodeToString(similarImages.get(i).getData());
        imageNode.put("bytes", base64Data);
        similarImagesNode.add(imageNode);
      }
      return ResponseEntity.status(HttpStatus.OK).body(similarImagesNode);

    } catch (IllegalArgumentException e) {
      ObjectNode errorNode = mapper.createObjectNode();
      errorNode.put("error", "Bad descriptor. Must be 'histoRGB', 'histoTeinteSaturation' or 'combined'.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorNode);
    }
  }

  @RequestMapping(value = "/images/{id}/like", method = RequestMethod.POST)
  public ResponseEntity<?> likeImage(@PathVariable("id") long id, @RequestParam("email") String email,
      @RequestParam("name") String name, @RequestParam("password") String password) {
    if (password.isEmpty() || !(imageDB.verifierUser(email, password))) {
      ObjectNode errorNode = mapper.createObjectNode();
      errorNode.put("error", "password invalide");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorNode);
    }
    Image image = imageDB.getImageByName(name);
    if (image != null) {
      String msg = imageDB.addLikeToImage(name, email);
      return ResponseEntity.status(HttpStatus.OK).body(msg);
    }
    ObjectNode errorNode = mapper.createObjectNode();
    errorNode.put("error", "Image not found");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorNode);
  }

  @RequestMapping(value = "/images/{id}/like", method = RequestMethod.DELETE)
  public ResponseEntity<?> unlikeImage(@PathVariable("id") long id, @RequestParam("email") String email,
      @RequestParam("name") String name, @RequestParam("password") String password) {
    if (password.isEmpty() || !(imageDB.verifierUser(email, password))) {
      ObjectNode errorNode = mapper.createObjectNode();
      errorNode.put("error", "password invalide");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorNode);
    }
    Image image = imageDB.getImageByName(name);
    if (image != null) {
      String msg = imageDB.removeLikeFromImage(name, email);
      return ResponseEntity.status(HttpStatus.OK).body(msg);
    }
    ObjectNode errorNode = mapper.createObjectNode();
    errorNode.put("error", "Image not found");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorNode);
  }

  @RequestMapping(value = "/images/liked", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<ArrayNode> getLikedImageList(@RequestParam("email") String email,
      @RequestParam("password") String password) {
    if (password.isEmpty() || !(imageDB.verifierUser(email, password))) {
      ObjectNode errorNode = mapper.createObjectNode();
      errorNode.put("error", "password invalide");
      ArrayNode error = mapper.createArrayNode();
      error.add(errorNode);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    List<Image> likedImages = imageDB.listLikedImages(email);
    ArrayNode nodes = mapper.createArrayNode();

    for (Image image : likedImages) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      objectNode.put("type", image.getType());
      objectNode.put("size", image.getSize());
      String base64Data = Base64.getEncoder().encodeToString(image.getData());
      objectNode.put("bytes", base64Data);
      nodes.add(objectNode);
    }

    return ResponseEntity.ok(nodes);
  }

  @RequestMapping(value = "/images/login", method = RequestMethod.POST)
  public ResponseEntity<?> login(@RequestParam("email") String email, @RequestParam("password") String password) {
    System.out.println("start");
    User testLogin = imageDB.login(email, password);
    if (testLogin != null) {
      return ResponseEntity.status(HttpStatus.OK).body(testLogin);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  @RequestMapping(value = "/images/signup", method = RequestMethod.POST)
  public ResponseEntity<?> signup(@RequestParam("email") String email, @RequestParam("password") String password,
      @RequestParam("name") String name) {
    System.out.println("start");
    User userSignup = imageDB.signup(email, password, name);
    if (userSignup != null) {
      return ResponseEntity.status(HttpStatus.OK).body(userSignup);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  public static void BrightnessAdjustment(Planar<GrayU8> image, int delta) {
    for (int band = 0; band < image.getNumBands(); band++) {
      GrayU8 band_image = image.getBand(band);
      for (int y = 0; y < band_image.height; y++) {
        for (int x = 0; x < band_image.width; x++) {
          int pixelValue = band_image.get(x, y);
          int newPixelValue = Math.max(0, Math.min(255, pixelValue + delta));
          band_image.set(x, y, newPixelValue);
        }
      }
    }
  }

  @RequestMapping(value = "/images/{id}/brightness", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ResponseEntity<?> adjustBrightness(
      @PathVariable("id") long id,
      @RequestParam("delta") int delta) {

    Image queryImage = imageDB.getImageById(id);
    if (queryImage == null) {
      ObjectNode errorNode = mapper.createObjectNode();
      errorNode.put("error", "Image not found");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorNode);
    }
    try {
      // crée un flux d'entrée à partir des données de l'image
      ByteArrayInputStream bais = new ByteArrayInputStream(queryImage.getData());
      // conversion en buffered image
      BufferedImage bufferedImage = ImageIO.read(bais);
      if (bufferedImage == null) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid image format.");
      }
      // convertir en planar
      Planar<GrayU8> planarImage = ConvertBufferedImage.convertFromPlanar(bufferedImage, null, true, GrayU8.class);
      BrightnessAdjustment(planarImage, delta);
      // convertir le resultat en buffered
      BufferedImage adjustedImage = ConvertBufferedImage.convertTo(planarImage, null, true);
      // créeer le flus de sortie
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      // ecrire le type de l'image
      String imageType = queryImage.getType().split("/")[1];
      ImageIO.write(adjustedImage, imageType, baos);

      // image encodée en base 64
      String base64Data = Base64.getEncoder().encodeToString(baos.toByteArray());

      // reponse
      ObjectNode responseNode = mapper.createObjectNode();
      responseNode.put("id", queryImage.getId());
      responseNode.put("name", queryImage.getName() + "_brightness");
      responseNode.put("type", queryImage.getType());
      responseNode.put("size", base64Data.length());
      responseNode.put("bytes", base64Data);

      return ResponseEntity.status(HttpStatus.OK).body(responseNode);

    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing image.");
    }
  }

}
