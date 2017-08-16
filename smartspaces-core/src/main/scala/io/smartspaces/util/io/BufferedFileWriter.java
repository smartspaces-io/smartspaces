/**
 * 
 */
package io.smartspaces.util.io;

import java.io.BufferedWriter;

/**
 * Write to a buffered file writer.
 * 
 * @author Keith M. Hughes
 */
public interface BufferedFileWriter {

  /**
   * Write to the buffered writer.
   * 
   * @param writer
   *          the writer to write to
   */
  void write(BufferedWriter writer);
}
