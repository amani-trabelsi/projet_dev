package pdl.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class ImageControllerTests {

    @Autowired 
    private MockMvc mockMvc;

    @BeforeAll
    public static void reset() {
        ReflectionTestUtils.setField(Image.class, "count", Long.valueOf(0));
    }

    @Test
    @Order(1)
    public void getImageListShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/images"))
               .andDo(print())
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(2)
    public void getImageShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/images/9999"))
               .andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @Order(3)
    public void createImageShouldReturnSuccess() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", 
            new ClassPathResource("test.jpg").getInputStream());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(multipartFile))
               .andDo(print())
               .andExpect(status().isCreated());
    }

	@Test
	@Order(4)
	public void getImageShouldReturnSuccess() throws Exception {
		mockMvc.perform(get("/images/0"))
			   .andDo(print())
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
    @Test
    @Order(5)
    public void deleteImagesShouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/images"))
               .andDo(print())
               .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @Order(6)
    public void deleteImageShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/images/9999"))
               .andDo(print())
               .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @Disabled("Active ce test supprime une image localement à chaque exécution")
    public void deleteImageShouldReturnSuccess() throws Exception {
        // Suppose qu'une image avec ID 0 existe déjà
        mockMvc.perform(delete("/images/0"))
               .andDo(print())
               .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    public void createImageShouldReturnUnsupportedMediaType() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", "test.txt", "text/plain", "Test".getBytes());
        
        mockMvc.perform(MockMvcRequestBuilders.multipart("/images").file(multipartFile))
               .andDo(print())
               .andExpect(status().isUnsupportedMediaType())
               .andExpect(content().string("Only JPEG and PNG file format supported"));
    }
}