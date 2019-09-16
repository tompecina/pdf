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

import cz.pecina.seqparser.CommandLine;
import cz.pecina.seqparser.Option;
import cz.pecina.seqparser.Options;
import cz.pecina.seqparser.Parameter;
import cz.pecina.seqparser.ParameterType;
import cz.pecina.seqparser.ParseException;
import cz.pecina.seqparser.SeqParser;
import java.util.List;
import java.util.logging.Logger;

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
  private static Options options;

  static {

    try {

      options = new Options().setSep(':');

      options.addOption(new Option("ar", "arc", 6)
          .addSubOption(ParameterType.Double));

      options.addOption(new Option("c", "color", 1, 2)
          .addSubOption(ParameterType.String));

      options.addOption(new Option("cft", "curve-from-to", 4)
          .addSubOption(ParameterType.Double));

      options.addOption(new Option("ci", "circle", 3)
          .addSubOption(ParameterType.Double));

      options.addOption(new Option("cp", "close-path"));

      options.addOption(new Option("cpefs", "close-path-eo-fill-stroke"));

      options.addOption(new Option("cpfs", "close-path-fill-stroke"));

      options.addOption(new Option("cps", "close-path-stroke"));

      options.addOption(new Option("cs", "char-spacing", 1)
          .addSubOption(ParameterType.Float));

      options.addOption(new Option("ct", "curve-to", 4, 6)
          .addSubOption(ParameterType.Double));

      options.addOption(new Option("ef", "eo-fill"));

      options.addOption(new Option("efs", "eo-fill-stroke"));

      options.addOption(new Option("el", "ellipse", 4)
          .addSubOption(ParameterType.Double));

      options.addOption(new Option("ep", "end-path"));

      options.addOption(new Option("f", "fill"));

      options.addOption(new Option("fc", "fill-color", 1)
          .addSubOption(ParameterType.String));

      options.addOption(new Option("ff", "font-file", 1)
          .addSubOption(ParameterType.String));

      options.addOption(new Option("fs", "fill-stroke"));

      options.addOption(new Option("hs", "horizontal-scaling", 1)
          .addSubOption(ParameterType.PosFloat));

      options.addOption(new Option("i", "image", 3)
          .addSubOption(ParameterType.String)
          .addSubOption(ParameterType.Float)
          .addKwSubOption("w", ParameterType.Float)
          .addKwSubOption("h", ParameterType.Float)
          .addKwSubOption("c", ParameterType.String));

      options.addOption(new Option("lc", "line-cap-style", 1)
          .addSubOption(ParameterType.IntegerRange(0, 2)));

      options.addOption(new Option("ld", "line-dash", 1, Integer.MAX_VALUE)
          .addSubOption(ParameterType.NonNegFloat));

      options.addOption(new Option("le", "leading", 1)
          .addSubOption(ParameterType.Float));

      options.addOption(new Option("lj", "line-join-style", 1)
          .addSubOption(ParameterType.IntegerRange(0, 2)));

      options.addOption(new Option("lt", "line-to", 2)
          .addSubOption(ParameterType.Double));

      options.addOption(new Option("lw", "line-width", 1)
          .addSubOption(ParameterType.NonNegFloat));

      options.addOption(new Option("ml", "miter-limit", 1)
          .addSubOption(ParameterType.NonNegFloat));

      options.addOption(new Option("mt", "move-to", 2)
          .addSubOption(ParameterType.Double));

      options.addOption(new Option("p", "pages", 1, Integer.MAX_VALUE)
          .addSubOption(ParameterType.String));

      options.addOption(new Option("ps", "font-size", 1)
          .addSubOption(ParameterType.PosFloat));

      options.addOption(new Option("re", "rectangle", 4)
          .addSubOption(ParameterType.Double)
          .addSubOption(ParameterType.Double)
          .addSubOption(ParameterType.NonNegDouble)
          .addKwSubOption("c", ParameterType.String));

      options.addOption(new Option("rm", "text-rendering-mode", 1)
          .addSubOption(ParameterType.IntegerRange(0, 7)));

      options.addOption(new Option("rr", "round-rectangle", 5)
          .addSubOption(ParameterType.Double)
          .addSubOption(ParameterType.Double)
          .addSubOption(ParameterType.NonNegDouble)
          .addKwSubOption("c", ParameterType.String));

      options.addOption(new Option("s", "stroke"));

      options.addOption(new Option("sc", "stroke-color", 1)
          .addSubOption(ParameterType.String));

      options.addOption(new Option("t", "text", 1, 3)
          .addSubOption(ParameterType.String)
          .addSubOption(ParameterType.Float)
          .addKwSubOption("cs", ParameterType.Float)
          .addKwSubOption("fc", ParameterType.String)
          .addKwSubOption("ff", ParameterType.String)
          .addKwSubOption("hs", ParameterType.Float)
          .addKwSubOption("le", ParameterType.Float)
          .addKwSubOption("lw", ParameterType.NonNegFloat)
          .addKwSubOption("ps", ParameterType.PosFloat)
          .addKwSubOption("rm", ParameterType.NonNegInteger)
          .addKwSubOption("sc", ParameterType.String)
          .addKwSubOption("tr", ParameterType.Float)
          .addKwSubOption("ws", ParameterType.Float));

      options.addOption(new Option("tm", "text-matrix", 6)
          .addSubOption(ParameterType.Float));

      options.addOption(new Option("tp", "text-pos", 2)
          .addSubOption(ParameterType.Float));

      options.addOption(new Option("tr", "text-rise", 1)
          .addSubOption(ParameterType.Float));

      options.addOption(new Option("ws", "word-spacing", 1)
          .addSubOption(ParameterType.Float));

      options.addOption(new Option("x", "literal", 1)
          .addSubOption(ParameterType.String));

    } catch (final ParseException exception) {
      log.fine("Error in options: " + exception.getMessage());
      System.exit(1);
    }
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
    System.out.println("usage: stamppdr [command...] [--] input-file [output_file]");
    System.out.println("TBA");
  }

  // parsed parameters
  private List<Parameter> parameters;
  private String[] fileNames;

  /**
   * Gets the parameters.
   *
   * @return the parameters
   */
  public List<Parameter> getParameters() {
    return parameters;
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

    if ((args == null) || (args.length == 0)) {
      usage();
      log.fine("Error in parameters");
      System.exit(1);
    }

    if (args[0].equals("-?") || args[0].equals("--help")) {
      usage();
      log.fine("Application terminated normally");
      System.exit(0);
    }

    if (args[0].equals("-V") || args[0].equals("--version")) {
      System.err.println("1.0.0");
      log.fine("Application terminated normally");
      System.exit(0);
    }

    CommandLine line = null;
    try {
      line = new SeqParser().parse(options, args, true);
    } catch (final Exception exception) {
      System.err.println("Failed to parse the command line, exception: " + exception);
      usage();
      log.fine("Failed to parse the command line, exception: " + exception);
      System.exit(1);
    }

    fileNames = new String[line.getRemArgs().size()];
    fileNames = line.getRemArgs().toArray(fileNames);
    final int fnLen = fileNames.length;
    if ((fnLen < 1) || (fnLen > 2)) {
      usage();
      log.fine("Error in parameters");
      System.exit(1);
    }

    parameters = line.getParameters();

    log.fine("Parameters set up");
  }
}
