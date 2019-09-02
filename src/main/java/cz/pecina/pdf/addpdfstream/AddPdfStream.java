/* AddPdfStream.java
 *
 * Copyright (C) 2015-19, Tomas Pecina <tomas@pecina.cz>
 *
 * This file is part of cz.pecina.pdf, a suite of PDF processing applications.
 *
 * This application is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The source code is available from <https://github.com/tompecina/pdf>.
 */

package cz.pecina.pdf.addpdfstream;

import com.itextpdf.kernel.pdf.CompressionConstants;
import com.itextpdf.kernel.pdf.PdfCatalog;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Adds stream to existing PDF file.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class AddPdfStream {

  // static logger
  private static final Logger log = Logger.getLogger(AddPdfStream.class.getName());

  // options
  private static final Options options = new Options();

  static {
    options.addOption(
        Option.builder("?")
        .longOpt("help")
        .desc("show usage information")
        .build());
    options.addOption(
        Option.builder("V")
        .longOpt("version")
        .desc("show version")
        .build());
    options.addOption(
        Option.builder("t")
        .longOpt("type")
        .hasArg()
        .argName("TYPE")
        .desc("type of the stream in the catalog (default: Data)")
        .build());
    options.addOption(
        Option.builder("d")
        .longOpt("dictionary")
        .hasArg()
        .argName("DATA")
        .desc("data copied to the stream dictionary, entered as key-value pairs: KEY:VALUE,KEY:VALUE...")
        .build());
    options.addOption(
        Option.builder("c")
        .longOpt("compress")
        .desc("compress the stream")
        .build());
    options.addOption(
        Option.builder("l")
        .longOpt("level")
        .hasArg()
        .type(Number.class)
        .argName("LEVEL")
        .desc(String.format(
            "compression level (%d-%d)", CompressionConstants.NO_COMPRESSION, CompressionConstants.BEST_COMPRESSION))
        .build());
  }

  // for description see Object
  @Override
  public String toString() {
    return "AddPdfStream";
  }

  /**
   * Prints usage information.
   *
   */
  private static void usage() {
    final HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp("addpdfstream [options] infile streamfile [outfile]", options);
    System.out.println("\nThe source code is available from <https://github.com/tompecina/pdf>.");
  }

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    log.fine("Application started");

    if ((args == null) || (args.length < 1)) {
      usage();
      log.fine("Error in parameters");
      System.exit(1);
    }

    final CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (Exception exception) {
      usage();
      log.fine("Failed to parse the command line, exception: " + exception);
      System.exit(1);
    }

    if (line.hasOption("?")) {
      usage();
      log.fine("Application terminated normally");
      System.exit(0);
    }

    if (line.hasOption("V")) {
      System.err.println("1.0.0");
      log.fine("Application terminated normally");
      System.exit(0);
    }

    final String streamType = (line.hasOption("t") ? line.getOptionValue("t") : "Data");

    final Map<String, String> pairs = new HashMap<>();
    if (line.hasOption("d")) {
      try {
        for (String pairString : line.getOptionValue("d").split(",")) {
          final String[] pair = pairString.split(":");
          pairs.put(pair[0], pair[1]);
        }
      } catch (Exception exception) {
        System.err.println("Error in dictionary pairs, exception: " + exception);
        log.fine("FFailed to parse dictionary pairs, exception: " + exception);
        System.exit(1);
      }
    }

    final boolean compress = (line.hasOption("c"));
    int compressionLevel = CompressionConstants.DEFAULT_COMPRESSION;
    if (line.hasOption("l")) {
      try {
        compressionLevel = ((Number) line.getParsedOptionValue("l")).intValue();
      } catch (Exception exception) {
        System.err.println("Error in compression level, exception: " + exception);
        log.fine("Failed to parse compression level, exception: " + exception);
        System.exit(1);
      }
      if ((compressionLevel < CompressionConstants.NO_COMPRESSION)
          || (compressionLevel > CompressionConstants.BEST_COMPRESSION)) {
        System.err.println(String.format(
            "Compression level must be (%d-%d)",
            CompressionConstants.NO_COMPRESSION, CompressionConstants.BEST_COMPRESSION));
        log.fine("Compression level out of range");
        System.exit(1);
      }
    }
    if (!compress) {
      compressionLevel = CompressionConstants.NO_COMPRESSION;
    }

    final String[] fileNames = line.getArgs();

    if ((fileNames.length < 2) || (fileNames.length > 3)) {
      usage();
      log.fine("Error in parameters");
      System.exit(1);
    }

    byte[] inputData = null;
    String outFileName = null;

    try {
      inputData = Files.readAllBytes(Paths.get(fileNames[0]));
      outFileName = fileNames[(fileNames.length == 2) ? 0 : 2];
    } catch (Exception exception) {
      System.err.println("Error opening files, exception: " + exception);
      log.fine("Error opening files, exception: " + exception);
      System.exit(1);
    }

    try {
      final PdfReader reader = new PdfReader(new ByteArrayInputStream(inputData));
      final PdfWriter writer = new PdfWriter(outFileName);
      final PdfDocument pdfDocument = new PdfDocument(reader, writer);
      final PdfStream pdfStream = new PdfStream(pdfDocument, new FileInputStream(fileNames[1]));
      for (String key : pairs.keySet()) {
        pdfStream.put(new PdfName(key), new PdfName(pairs.get(key)));
      }
      if (compress) {
        pdfStream.setCompressionLevel(compressionLevel);
      }
      final PdfName streamPdfName = new PdfName(streamType);
      final PdfCatalog catalog = pdfDocument.getCatalog();
      if (((PdfDictionary) catalog.getPdfObject()).get(streamPdfName) != null) {
        catalog.remove(streamPdfName);
      }
      catalog.put(streamPdfName, pdfStream);
      pdfDocument.close();
      writer.close();
      reader.close();
    } catch (Exception exception) {
      System.err.println("Error processing files, exception: " + exception);
      log.fine("Error processing files, exception: " + exception);
      System.exit(1);
    }

    log.fine("Application terminated normally");
  }
}
