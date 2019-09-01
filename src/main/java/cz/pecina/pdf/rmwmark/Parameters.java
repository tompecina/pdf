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

package cz.pecina.pdf.rmwmark;

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
    helpFormatter.printHelp("rmwmark [options] infile [outfile]", options);
    System.out.println("\nThe source code is available from <https://github.com/tompecina/pdf>.");
  }

  // parsed parameters
  private String[] fileNames;

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

    if ((remArgs.length < 1) || (remArgs.length > 2)) {
      usage();
      log.fine("Error in parameters");
      System.exit(1);
    }

    fileNames = Arrays.copyOfRange(remArgs, 0, remArgs.length);

    log.fine("Parameters set up");
  }
}
