package com.springboot.project.test.controller.ResourceController;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import com.springboot.project.common.StorageResource.RangeClassPathResource;
import com.springboot.project.test.common.BaseTest.BaseTest;
import io.reactivex.rxjava3.core.Flowable;
import lombok.SneakyThrows;

public class ResourceControllerUploadMergeTest extends BaseTest {
    private List<String> urlList;

    @Test
    public void test() throws URISyntaxException {
        var urlOfResource = this.fromLongTermTask(new Supplier<ResponseEntity<String>>() {

            @Override
            @SneakyThrows
            public ResponseEntity<String> get() {
                var urlOfMerge = new URIBuilder("/upload/merge").build();
                return testRestTemplate.postForEntity(urlOfMerge, urlList, String.class);
            }

        }, new ParameterizedTypeReference<String>() {
        }).getBody();
        assertTrue(urlOfResource.startsWith("/resource/"));
        var result = this.testRestTemplate.getForEntity(new URIBuilder(urlOfResource).build(), byte[].class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MediaType.IMAGE_JPEG, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().getContentDisposition().isInline());
        assertEquals("default.jpg", result.getHeaders().getContentDisposition().getFilename());
        assertEquals(StandardCharsets.UTF_8, result.getHeaders().getContentDisposition().getCharset());
        assertEquals(9287, result.getBody().length);
        assertNotNull(result.getHeaders().getETag());
        assertTrue(result.getHeaders().getETag().startsWith("\""));
        assertTrue(result.getHeaders().getETag().endsWith("\""));
        assertEquals("max-age=86400, no-transform, public", result.getHeaders().getCacheControl());
        assertEquals(9287, result.getHeaders().getContentLength());
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        var imageResource = new ClassPathResource("image/default.jpg");
        var everySize = 100;
        this.urlList = Flowable
                .range(0,
                        new BigDecimal(imageResource.contentLength())
                                .divide(new BigDecimal(everySize), 100, RoundingMode.FLOOR)
                                .setScale(0, RoundingMode.CEILING).intValue())
                .map(startIndex -> {
                    var url = new URIBuilder("/upload/resource").build();
                    var body = new LinkedMultiValueMap<Object, Object>();
                    var rangeLength = everySize;
                    if (imageResource.contentLength() < startIndex * everySize + everySize) {
                        rangeLength = Long.valueOf(imageResource.contentLength() - startIndex * everySize).intValue();
                    }
                    body.add("file", new RangeClassPathResource("image/default.jpg",
                            startIndex * everySize, rangeLength));
                    var response = this.testRestTemplate.postForEntity(url, body, String.class);
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    return response.getBody();
                })
                .toList()
                .blockingGet();
    }
}
