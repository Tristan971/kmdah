package moe.tristan.kmdah.service.images.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import moe.tristan.kmdah.mangadex.image.ImageMode;
import moe.tristan.kmdah.service.images.ImageSpec;
import moe.tristan.kmdah.service.metrics.ImageMetrics;

@SpringBootTest(classes = ImageValidationService.class)
class ImageValidationServiceTest {

    private static final String SAMPLE = "moe/tristan/kmdah/service/images/validation/sample.png";
    private static final String SAMPLE_HASH = "d85863442597225e38dc3318e213cbfa172479f43d1f59c2d925adf3c9423aba";

    @MockBean
    private ImageMetrics imageMetrics;

    @Autowired
    private ImageValidationService imageValidationService;

    @Test
    void validateCorrectHash() throws IOException {
        ImageSpec imageSpec = new ImageSpec(
            ImageMode.DATA,
            "chapterid",
            "fileid-" + SAMPLE_HASH + ".png"
        );

        ClassPathResource classPathResource = new ClassPathResource(SAMPLE);
        byte[] bytes = StreamUtils.copyToByteArray(classPathResource.getInputStream());

        assertThat(imageValidationService.validate(imageSpec, OptionalLong.empty(), bytes)).isTrue();
        verify(imageMetrics).recordValidation(true);
    }

    @Test
    void validateIncorrectHash() throws IOException {
        ImageSpec imageSpec = new ImageSpec(
            ImageMode.DATA,
            "chapterid",
            "fileid-" + "not the correct hash m8" + ".png"
        );

        ClassPathResource classPathResource = new ClassPathResource(SAMPLE);
        byte[] bytes = StreamUtils.copyToByteArray(classPathResource.getInputStream());

        assertThat(imageValidationService.validate(imageSpec, OptionalLong.empty(), bytes)).isFalse();
        verify(imageMetrics).recordValidation(false);
    }

}
