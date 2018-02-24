package org.overture.score.tools;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.icgc.dcc.storage.core.model.ObjectSpecification;

@RequiredArgsConstructor
public class MetadataGenerator {

  @NonNull private final SimplePartCalculator partCalculator;

  public ObjectSpecification generate(@NonNull String objectId,
      @NonNull String objectMd5, final long objectSize){
    val spec = new ObjectSpecification();
    spec.setObjectId(objectId);
    spec.setObjectKey(objectId);
    spec.setObjectMd5(objectMd5);
    spec.setObjectSize(objectSize);
    spec.setRelocated(false);
    spec.setUploadId(objectId);
    val parts = partCalculator.divide(objectSize);
    spec.setParts(parts);
    return spec;
  }

  public static MetadataGenerator createMetadataGenerator(int minPartSize) {
    return new MetadataGenerator(new SimplePartCalculator(minPartSize));
  }

}
