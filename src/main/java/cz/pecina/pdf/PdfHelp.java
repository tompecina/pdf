/* PdfJava.java
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

package cz.pecina.pdf;

import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


/**
 * Display list of available utilities and exit.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class PdfHelp {

  // static logger
  private static final Logger LOG = Logger.getLogger(PdfHelp.class.getName());

  // for description see Object
  @Override
  public String toString() {
    return "PdfHelp";
  }

  // options
  private static final Options OPTIONS = new Options();

  static {
    OPTIONS.addOption(
                      Option.builder("V")
                      .longOpt("version")
                      .desc("show version")
                      .build()
                      );
  }

  /**
   * Prints usage information.
   *
   */
  private static void usage() {
    final HelpFormatter helpFormatter = new HelpFormatter();
    System.out.println("A suite of PDF processing utilities:");
    System.out.println(" - AddPdfMeta - add metadata to PDF");
    System.out.println(" - AddPdfStream - add stream to PDF");
    System.out.println(" - InspectPdf - show PDF metadata");
    System.out.println(" - PdfToXml - convert PDF to XML file");
    System.out.println(" - ReadPdfStream - read stream from PDF");
    System.out.println(" - RmWmark - strip text elements from PDF");
    System.out.println(" - SignBoxPdf - add signing box to PDF");
    System.out.println(" - SignPdf - digitally sign PDF");
    System.out.println(" - StampPdf - stamp PDF with simple text information");
    System.out.println("\nThe source code is available from <https://github.com/tompecina/pdf>.");
  }

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    LOG.fine("Application started");

    final CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    try {
      line = parser.parse(OPTIONS, args);
    } catch (Exception exception) {
      usage();
      LOG.fine("Failed to parse the command line, exception: " + exception);
      System.exit(1);
    }

    if (line.hasOption("V")) {
      System.err.println("1.0.0");
    } else {
      usage();
    }

    LOG.fine("Application terminated normally");
  }
}
