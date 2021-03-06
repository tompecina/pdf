/* ReadPdfStream.java
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

package cz.pecina.pdf.readpdfstream;

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Reads stream from PDF file.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class ReadPdfStream {

  // static logger
  private static final Logger log = Logger.getLogger(ReadPdfStream.class.getName());

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
        Option.builder("v")
        .longOpt("verbose")
        .desc("be more verbose")
        .build());
  }

  // for description see Object
  @Override
  public String toString() {
    return "ReadPdfStream";
  }

  /**
   * Prints usage information.
   *
   */
  private static void usage() {
    final HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp("readpdfstream [options] infile", options);
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
    } catch (final Exception exception) {
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

    final boolean verbose = (line.hasOption("v"));

    final String[] fileNames = line.getArgs();

    if (fileNames.length != 1) {
      usage();
      log.fine("Error in parameters");
      System.exit(1);
    }

    final String inFileName = fileNames[0];

    try {
      final PdfReader reader = new PdfReader(inFileName);
      final PdfDocument pdfDocument = new PdfDocument(reader);
      final PdfDictionary catalog = (PdfDictionary) pdfDocument.getCatalog().getPdfObject();
      final PdfName streamPdfName = new PdfName(streamType);
      if (!catalog.containsKey(streamPdfName)) {
        System.err.println("Stream '" + streamType + "' not found");
        log.fine("Stream not found");
        System.exit(1);
      }
      final PdfStream pdfStream = catalog.getAsStream(streamPdfName);
      if (verbose) {
        System.out.println("Dictionary:");
        for (PdfName key : pdfStream.keySet()) {
          System.out.println("  " + key + ": " + pdfStream.get(key));
        }
        System.out.println();
      }
      System.out.print(new String(pdfStream.getBytes(), "utf-8"));
    } catch (final Exception exception) {
      System.err.println("Error processing files, exception: " + exception);
      log.fine("Error processing files, exception: " + exception);
      System.exit(1);
    }

    log.fine("Application terminated normally");
  }
}
