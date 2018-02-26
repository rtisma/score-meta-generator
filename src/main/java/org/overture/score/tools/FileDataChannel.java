package org.overture.score.tools;

import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

@AllArgsConstructor
public class FileDataChannel {

  private final File file;
  private final long length;

  public void readFrom(InputStream is) throws IOException {
    try (FileOutputStream os = new FileOutputStream(file)) {
      try (ReadableByteChannel fromChannel = Channels.newChannel(is)) {
        os.getChannel().transferFrom(fromChannel, 0, length);
      }
    }
  }

}
