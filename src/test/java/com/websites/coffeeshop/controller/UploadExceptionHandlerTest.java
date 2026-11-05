package com.websites.coffeeshop.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class UploadExceptionHandlerTest {

  @Test
  void anOversizedUploadIsAnswered413WithAReadableMessage() {
    var response = new UploadExceptionHandler().handleTooLarge(new MaxUploadSizeExceededException(1024 * 1024));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
    assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.TEXT_PLAIN);
    assertThat(response.getBody()).isEqualTo("Image size must be less than 1MB");
  }
}
