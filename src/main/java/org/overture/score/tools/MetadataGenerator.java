package org.overture.score.tools;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.icgc.dcc.storage.core.model.ObjectSpecification;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static lombok.AccessLevel.PRIVATE;

/**
 * If part.sourceMd5 is null, then md5 checking of each part is DISABLED when downloading. To calculate the md5 for
 * each part, create a S3 Part url, download it and then calculate the md5. Then set the sourceMd5 value.
 */
@RequiredArgsConstructor(access = PRIVATE)
public class MetadataGenerator {

  @NonNull private final SimplePartCalculator partCalculator;
  private final boolean doMd5check;
  private final PartMd5Generator partMd5Generator;
  @NonNull private final String bucketName;
  private final int numThreads;
  @NonNull private final String bucketDir;

  @SneakyThrows
  public ObjectSpecification generate(@NonNull String objectId,
      @NonNull String objectMd5, final long objectSize){
    val spec = new ObjectSpecification();
    spec.setObjectId(objectId);

    // This is not required, but helps keep naming consistent. The getter for this field is never used in the code
    spec.setObjectKey(bucketDir+"/"+objectId);

    spec.setObjectMd5(objectMd5);
    spec.setObjectSize(objectSize);
    spec.setRelocated(false);
    spec.setUploadId(objectId);
    val parts = partCalculator.divide(objectSize);

    if (doMd5check) {
      val executorService = newFixedThreadPool(numThreads);
      for (val part : parts){
        executorService.submit(() -> {
          part.setSourceMd5(partMd5Generator.generateMd5(bucketName, objectId, part));
        } );
      }
      executorService.shutdown();
      executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
    } else {
      // Not recommended. Part downloads will never by md5 verified.
      parts.forEach(p -> p.setSourceMd5(null));
    }
    spec.setParts(parts);
    return spec;
  }

  public static MetadataGenerator createMetadataGenerator(int minPartSize, String bucketName, String bucketDir) {
    return new MetadataGenerator(new SimplePartCalculator(minPartSize),
        false, null, bucketName, -1, bucketDir);
  }

  public static MetadataGenerator createMetadataGenerator(int minPartSize, PartMd5Generator partMd5Generator,
      int numthreads, String bucketName, String bucketDir) {
    return new MetadataGenerator(new SimplePartCalculator(minPartSize), true, partMd5Generator, bucketName,
        numthreads, bucketDir );
  }

}
