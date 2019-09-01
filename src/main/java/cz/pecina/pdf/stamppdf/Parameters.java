/* Parameters.java
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

package cz.pecina.pdf.stamppdf;

import java.util.Arrays;

import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


/**
 * Parse command line and extract parameters.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class Parameters {

  // static logger
  private static final Logger log = Logger.getLogger(Parameters.class.getName());

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
        Option.builder("p")
        .longOpt("page")
        .hasArg()
        .type(Number.class)
        .argName("PAGE")
        .desc("page number (default: 1)")
        .build());
  }

  // for description see Object
  @Override
  public String toString() {
    return "Parameters";
  }

  /**
   * Prints usage information.
   *
   */
  public void usage() {
    final HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp("stamppdf [options] x y text infile [outfile]", options);
    System.out.println("\nNotes:");
    System.out.println("  - Negative values of x/y mean offset from the right/top page margin");
    System.out.println("  - Character ^ in text is replaced with a newline");
    System.out.println("\nThe source code is available from <https://github.com/tompecina/pdf>.");
  }

  // parsed parameters
  private int pageNum = 1;
  private float xOffset;
  private float yOffset;
  private String text;
  private String[] fileNames;

  /**
   * Gets the page number.
   *
   * @return page number
   */
  public int getPageNum() {
    return pageNum;
  }

  /**
   * Gets the x offset.
   *
   * @return x offset
   */
  public float getXOffset() {
    return xOffset;
  }

  /**
   * Gets the y offset.
   *
   * @return y offset
   */
  public float getYOffset() {
    return yOffset;
  }

  /**
   * Gets the text.
   *
   * @return text
   */
  public String getText() {
    return text;
  }

  /**
   * Gets file names.
   *
   * @return file names as string array
   */
  public String[] getFileNames() {
    return fileNames;
  }

  /**
   * Gets number of file names.
   *
   * @return number of file names
   */
  public int numberFileNames() {
    return fileNames.length;
  }

  /**
   * Gets file name.
   *
   * @param  n file name index
   * @return file names as string array
   */
  public String getFileName(final int n) {
    return fileNames[n];
  }

  /**
   * Default constructor.
   *
   * @param args command-line arguments
   */
  public Parameters(final String[] args) {
    log.fine("Parameters started");

    if ((args == null) || (args.length < 1)) {
      usage();
      log.fine("Error in parameters");
      System.exit(1);
    }

    final CommandLineParser parser = new DefaultParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args, true);
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

    final String[] remArgs = line.getArgs();

    if ((remArgs.length < 4) || (remArgs.length > 5)) {
      usage();
      log.fine("Error in parameters");
      System.exit(1);
    }

    xOffset = Float.parseFloat(remArgs[0]);
    yOffset = Float.parseFloat(remArgs[1]);
    text = remArgs[2];
    fileNames = Arrays.copyOfRange(remArgs, 3, remArgs.length);

    log.fine("Parameters set up");
  }
}
