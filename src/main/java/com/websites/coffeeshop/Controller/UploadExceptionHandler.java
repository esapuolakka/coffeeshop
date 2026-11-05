package com.websites.coffeeshop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * A file larger than spring.servlet.multipart.max-file-size is rejected while the
 * request is being parsed, before any controller is chosen, so this handler has to
 * be global (a handler inside AdminController never sees it). Without it the
 * browser only gets an empty 413 and the admin page can only say "Adding failed".
 */
@ControllerAdvice
public class UploadExceptionHandler {

  static final String MESSAGE = "Image size must be less than 1MB";

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<String> handleTooLarge(MaxUploadSizeExceededException e) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).contentType(MediaType.TEXT_PLAIN).body(MESSAGE);
  }
}
