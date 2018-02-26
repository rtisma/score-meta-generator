package org.overture.score.tools;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.net.HttpHeaders;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.storage.core.model.Part;
import org.icgc.dcc.storage.core.util.Parts;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.amazonaws.HttpMethod.GET;
import static com.google.common.net.HttpHeaders.RANGE;

@RequiredArgsConstructor
public class PartMd5Generator {

  @NonNull private final AmazonS3 s3Client;
  private final int expiration;
  private final RestTemplate dataTemplate;

  @SneakyThrows
  public String generateMd5(@NonNull String bucketName, @NonNull String objectKey, @NonNull Part part){
    val url = getDownloadPartUrl( bucketName, objectKey, part, getExpirationDate());
    val channel = new FileDataChannel(new File(objectKey),part.getPartSize());
    part.setUrl(url); //optional. not needed
    return dataTemplate.execute(new URI(url), HttpMethod.GET,

        request -> request.getHeaders().set(HttpHeaders.RANGE, Parts.getHttpRangeValue(part)),

        response -> {
          try (HashingInputStream his = new HashingInputStream(Hashing.md5(), response.getBody())) {
            channel.readFrom(his);
            return his.hash().toString();
          }
        });
  }

  public String getDownloadPartUrl(String bucketName, String key, Part part, Date expiration) {
    GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, key, GET);
    req.setExpiration(expiration);

    req.putCustomRequestHeader(RANGE, Parts.getHttpRangeValue(part));
    return s3Client.generatePresignedUrl(req).toString();
  }

  private Date getExpirationDate() {
    val now = LocalDateTime.now();
    return Date.from(now.plusDays(expiration).atZone(ZoneId.systemDefault()).toInstant());
  }

}
