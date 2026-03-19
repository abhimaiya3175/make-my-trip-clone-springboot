package com.makemytrip.modules.reviews;

import com.makemytrip.modules.reviews.controller.ReviewController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerUploadTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private com.makemytrip.modules.reviews.service.ReviewService reviewService;

    @MockitoBean
    private com.makemytrip.modules.auth.service.AuthService authService;

    @MockitoBean
    private com.makemytrip.security.JwtService jwtService;

    @Test
    public void uploadPhoto_rejectsNonImageContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "photo",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/api/reviews/upload-photo").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").value("Only JPEG, PNG, and WEBP formats are allowed"));
    }

    @Test
    public void uploadPhoto_rejectsFileOver5MB() throws Exception {
        byte[] largeContent = new byte[(5 * 1024 * 1024) + 10]; // 5MB + 10 bytes
        MockMultipartFile file = new MockMultipartFile(
                "photo",
                "large.jpeg",
                "image/jpeg",
                largeContent
        );

        mockMvc.perform(multipart("/api/reviews/upload-photo").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.message").value("File size exceeds 5MB limit"));
    }
}
