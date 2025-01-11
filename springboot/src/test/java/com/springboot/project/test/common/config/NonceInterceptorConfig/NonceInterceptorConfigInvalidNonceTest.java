package com.springboot.project.test.common.config.NonceInterceptorConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import com.springboot.project.test.common.BaseTest.BaseTest;
import io.reactivex.rxjava3.core.Flowable;

public class NonceInterceptorConfigInvalidNonceTest extends BaseTest {

    private String nonce;
    private String timestamp;

    @Test
    public void test() throws URISyntaxException {
        URI url = new URIBuilder("/").build();
        var httpHeaders = new HttpHeaders();
        httpHeaders.set("X-Nonce", nonce);
        httpHeaders.set("X-Timestamp", timestamp);
        var response = this.testRestTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(httpHeaders), Throwable.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid nonce", response.getBody().getMessage());
    }

    @BeforeEach
    public void beforeEach() throws URISyntaxException {
        this.nonce = StringUtils.join(Flowable.range(1, 4)
                .map(s -> UUID.randomUUID().toString())
                .toList()
                .blockingGet());
        this.timestamp = FastDateFormat.getInstance(this.dateFormatProperties.getUTC()).format(new Date());
    }
}
