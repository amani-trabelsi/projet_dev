package pdl.backend;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.http.MediaType;

public class Image {
  private static Long count = Long.valueOf(0);
  private Long id;
  private String name;
  private byte[] data;
  private String type;
  private String size;

  public Image(final String name, final byte[] data) {
    id = count++;
    this.name = name;
    this.data = data;
    this.type = getImageMediaType().toString();
    this.size = getImageSize();

  }

  public Image(final Long id, final String name, final byte[] data) {
    this.id = id;
    this.name = name;
    this.data = data;
    this.type = getImageMediaType().toString();
    this.size = getImageSize();
  }

  private MediaType getImageMediaType() {
    String lowerCaseName = name.toLowerCase();

    if (lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".jpg")) {
      return MediaType.IMAGE_JPEG;
    }
    if (lowerCaseName.endsWith(".png")) {
      return MediaType.IMAGE_PNG;
    }

    return MediaType.APPLICATION_OCTET_STREAM;
  }

  private String getImageSize() {
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
      BufferedImage bufferedImage = ImageIO.read(inputStream);

      if (bufferedImage != null) {
        return bufferedImage.getWidth() + "x" + bufferedImage.getHeight();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return "Inconnu";
  }

  public Long getId() {
    return this.id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public byte[] getData() {
    return data;
  }

  public String getSize() {
    return size;
  }

  public String getType() {
    return type;
  }
}
