package com.raffaele.squarecash4glass.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import android.util.Log;

public class CompressedFilesUtils {
  private static final String TAG = "CompressedFilesUtils";

  /**
   * Untar an input file into an output file.
   * 
   * The output file is created in the output folder, having the same name as
   * the input file, minus the '.tar' extension.
   * 
   * @param inputFile
   *          the input .tar file
   * @param outputDir
   *          the output directory file.
   * @throws IOException
   * @throws FileNotFoundException
   * 
   * @return The {@link List} of {@link File}s with the untared content.
   * @throws ArchiveException
   */
  public static List<File> unTar(final InputStream inputFile, final File outputDir) throws FileNotFoundException, IOException, ArchiveException {

    Log.i(TAG, String.format("Untaring %s to dir %s.", inputFile, outputDir.getAbsolutePath()));

    final List<File> untaredFiles = new LinkedList<File>();
    final InputStream is = inputFile;
    final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
    TarArchiveEntry entry = null;
    while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
      final File outputFile = new File(outputDir, entry.getName());
      if (entry.isDirectory()) {
        // Log.i(TAG, String.format("Attempting to write output directory %s.",
        // outputFile.getAbsolutePath()));
        // if (!outputFile.exists()) {
        // Log.i(TAG, String.format("Attempting to create output directory %s.",
        // outputFile.getAbsolutePath()));
        // if (!outputFile.mkdirs()) {
        // Log.i(TAG, String.format("Couldn't create directory %s.",
        // outputFile.getAbsolutePath()));
        // throw new IllegalStateException();
        // }
        // }
      } else {
        if (!outputFile.getParentFile().exists()) {
          Log.i(TAG, String.format("Attempting to create output directory %s.", outputFile.getParentFile().getAbsolutePath()));
          if (!outputFile.getParentFile().mkdirs()) {
            Log.i(TAG, String.format("Couldn't create directory %s.", outputFile.getParentFile().getAbsolutePath()));
            throw new IllegalStateException();
          } else {
            Log.i(TAG, String.format("created directory %s.", outputFile.getParentFile().getAbsolutePath()));
          }
        } else {
          Log.i(TAG, String.format("directory %s. already exists", outputFile.getParentFile().getAbsolutePath()));
        }

        Log.i(TAG, String.format("Creating output file %s.", outputFile.getAbsolutePath()));

        final OutputStream outputFileStream = new FileOutputStream(outputFile);
        IOUtils.copy(debInputStream, outputFileStream);
        outputFileStream.close();
      }
      untaredFiles.add(outputFile);
    }
    debInputStream.close();

    return untaredFiles;
  }

  // private static void createParentDirectory(File outputFile) {
  // if (outputFile.getParentFile()!=null){
  // createParentDirectory(outputFile.getParentFile());
  // }
  // if (!outputFile.exists()) {
  // Log.i(TAG, String.format("Attempting to create output directory %s.",
  // outputFile.getAbsolutePath()));
  // if (!outputFile.mkdirs()) {
  // Log.i(TAG, String.format("Couldn't create directory %s.",
  // outputFile.getAbsolutePath()));
  // throw new IllegalStateException();
  // }
  // }
  // }

  /**
   * Ungzip an input file into an output file.
   * <p>
   * The output file is created in the output folder, having the same name as
   * the input file, minus the '.gz' extension.
   * 
   * @param inputFile
   *          the input .gz file
   * @param outputDir
   *          the output directory file.
   * @throws IOException
   * @throws FileNotFoundException
   * 
   * @return The {@File} with the ungzipped content.
   */
  private static File unGzip(final File inputFile, final File outputDir) throws FileNotFoundException, IOException {

    Log.i(TAG, String.format("Ungzipping %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

    final File outputFile = new File(outputDir, inputFile.getName().substring(0, inputFile.getName().length() - 3));

    final GZIPInputStream in = new GZIPInputStream(new FileInputStream(inputFile));
    final FileOutputStream out = new FileOutputStream(outputFile);

    IOUtils.copy(in, out);

    in.close();
    out.close();

    return outputFile;
  }

}