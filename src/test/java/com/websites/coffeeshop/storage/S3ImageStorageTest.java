package com.websites.coffeeshop.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class S3ImageStorageTest {

  private final S3Client s3 = mock(S3Client.class);
  private final S3ImageStorage storage = new S3ImageStorage(s3, "demo-bucket");

  @Test
  void putUploadsToBucketWithKeyAndContentType() {
    storage.put("images/a.png", new byte[] { 1, 2 }, "image/png");

    ArgumentCaptor<PutObjectRequest> request = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3).putObject(request.capture(), any(RequestBody.class));
    assertThat(request.getValue().bucket()).isEqualTo("demo-bucket");
    assertThat(request.getValue().key()).isEqualTo("images/a.png");
    assertThat(request.getValue().contentType()).isEqualTo("image/png");
  }

  @Test
  void getReturnsObjectBytes() {
    when(s3.getObjectAsBytes(any(GetObjectRequest.class)))
        .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), new byte[] { 9, 8 }));

    assertThat(storage.get("images/a.png")).containsExactly(9, 8);

    ArgumentCaptor<GetObjectRequest> request = ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(s3).getObjectAsBytes(request.capture());
    assertThat(request.getValue().bucket()).isEqualTo("demo-bucket");
    assertThat(request.getValue().key()).isEqualTo("images/a.png");
  }

  @Test
  void getTranslatesNoSuchKey() {
    when(s3.getObjectAsBytes(any(GetObjectRequest.class)))
        .thenThrow(NoSuchKeyException.builder().message("nope").build());

    assertThatThrownBy(() -> storage.get("images/a.png")).isInstanceOf(ImageNotFoundException.class);
  }

  @Test
  void deleteRemovesObjectFromBucket() {
    storage.delete("images/a.png");

    ArgumentCaptor<DeleteObjectRequest> request = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3).deleteObject(request.capture());
    assertThat(request.getValue().bucket()).isEqualTo("demo-bucket");
    assertThat(request.getValue().key()).isEqualTo("images/a.png");
  }
}
