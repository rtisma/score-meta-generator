package org.overture.score.tools;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.overture.score.tools.MetadataGenerator.createMetadataGenerator;

@Slf4j
public class GeneratorTest {

  private static final int MIN_PART_SIZE = 20 * 1024 * 1024; //20 MB is the minimum part size
  private static final int MAX_NUM_PARTS = 10000;

  @Test
  public void testGenerateSmall(){
    val expectedPartSize = 500;
    val numParts = 5;
    val expectedObjectSize = numParts*expectedPartSize;
    val generator = createMetadataGenerator(expectedPartSize, "myBucket", "data");
    val spec = generator.generate("myObjectId1", "jdh39hdi20jdo3", expectedObjectSize);
    assertThat(spec.getParts().size()).isEqualTo(1);
  }

  @Test
  public void testGenerateBig(){
    val expectedPartSize = 7*MIN_PART_SIZE;
    val numParts = 5;
    val expectedObjectSize = numParts*expectedPartSize;
    val generator = createMetadataGenerator(expectedPartSize, "myBucket", "data");
    val spec = generator.generate("myObjectId1", "jdh39hdi20jdo3", expectedObjectSize);
    assertThat(spec.getParts().size()).isEqualTo(5);
    log.info(spec.toString());
  }

}
