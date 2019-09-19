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

      options = new Options();

      options.addOption("ar", "arc", 6)
          .addSubOption(ParameterType.Double);

      options.addOption("c", "color", 1, 2)
          .addSubOption(ParameterType.String);

      options.addOption("cft", "curve-from-to", 4)
          .addSubOption(ParameterType.Double);

      options.addOption("ci", "circle", 3)
          .addSubOption(ParameterType.Double);

      options.addOption("cp", "close-path");

      options.addOption("cpefs", "close-path-eo-fill-stroke");

      options.addOption("cpfs", "close-path-fill-stroke");

      options.addOption("cps", "close-path-stroke");

      options.addOption("cs", "char-spacing", 1)
          .addSubOption(ParameterType.Float);

      options.addOption("ct", "curve-to", 4, 6)
          .addSubOption(ParameterType.Double);

      options.addOption("ef", "eo-fill");

      options.addOption("efs", "eo-fill-stroke");

      options.addOption("el", "ellipse", 4)
          .addSubOption(ParameterType.Double);

      options.addOption("ep", "end-path");

      options.addOption("f", "fill");

      options.addOption("fc", "fill-color", 1)
          .addSubOption(ParameterType.String);

      options.addOption("ff", "font-file", 1)
          .addSubOption(ParameterType.String);

      options.addOption("fs", "fill-stroke");

      options.addOption("hs", "horizontal-scaling", 1)
          .addSubOption(ParameterType.PosFloat);

      options.addOption("i", "image", 3)
          .addSubOption(ParameterType.String)
          .addSubOption(ParameterType.Float)
          .addKwSubOption("w", ParameterType.Float)
          .addKwSubOption("h", ParameterType.Float)
          .addKwSubOption("c", ParameterType.String);

      options.addOption("lc", "line-cap-style", 1)
          .addSubOption(ParameterType.IntegerRange(0, 2));

      options.addOption("ld", "line-dash", 1, Integer.MAX_VALUE)
          .addSubOption(ParameterType.NonNegFloat);

      options.addOption("le", "leading", 1)
          .addSubOption(ParameterType.Float);

      options.addOption("lj", "line-join-style", 1)
          .addSubOption(ParameterType.IntegerRange(0, 2));

      options.addOption("lt", "line-to", 2)
          .addSubOption(ParameterType.Double);

      options.addOption("lw", "line-width", 1)
          .addSubOption(ParameterType.NonNegFloat);

      options.addOption("ml", "miter-limit", 1)
          .addSubOption(ParameterType.NonNegFloat);

      options.addOption("mt", "move-to", 2)
          .addSubOption(ParameterType.Double);

      options.addOption("p", "pages", 1, Integer.MAX_VALUE)
          .addSubOption(ParameterType.String);

      options.addOption("ps", "font-size", 1)
          .addSubOption(ParameterType.PosFloat);

      options.addOption("re", "rectangle", 4)
          .addSubOption(ParameterType.Double)
          .addSubOption(ParameterType.Double)
          .addSubOption(ParameterType.NonNegDouble)
          .addKwSubOption("c", ParameterType.String);

      options.addOption("rm", "text-rendering-mode", 1)
          .addSubOption(ParameterType.IntegerRange(0, 7));

      options.addOption("rr", "round-rectangle", 5)
          .addSubOption(ParameterType.Double)
          .addSubOption(ParameterType.Double)
          .addSubOption(ParameterType.NonNegDouble)
          .addKwSubOption("c", ParameterType.String);

      options.addOption("s", "stroke");

      options.addOption("sc", "stroke-color", 1)
          .addSubOption(ParameterType.String);

      options.addOption("t", "text", 1, 3)
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
          .addKwSubOption("ws", ParameterType.Float);

      options.addOption("tm", "text-matrix", 6)
          .addSubOption(ParameterType.Float);

      options.addOption("tp", "text-pos", 2)
          .addSubOption(ParameterType.Float);

      options.addOption("tr", "text-rise", 1)
          .addSubOption(ParameterType.Float);

      options.addOption("ws", "word-spacing", 1)
          .addSubOption(ParameterType.Float);

      options.addOption("x", "literal", 1)
          .addSubOption(ParameterType.String);

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
   */
  public void usage() {
    System.out.println("Usage:\n  stamppdf [COMMAND...] [--] INPUT-FILE [OUTPUT-FILE]");
    System.out.println("  stamppdf -?|--help");
    System.out.println("  stamppdf -V|--version");
    System.out.println("\nCommands:");
    System.out.println("\n  -ar|--arc X1:Y1:X2:Y2:ARC:EXTENT");
    System.out.println("    draw arc segment");
    System.out.println("\n  -c|--color COLOR|FILL-COLOR:STROKE-COLOR");
    System.out.println("    set color (default: black)");
    System.out.println("\n  -cft|--curve-from-to X1:Y1:X3:Y3");
    System.out.println("    append Bézier curve");
    System.out.println("\n  -ci|--circle X:Y:R");
    System.out.println("    draw circle");
    System.out.println("\n  -cp|--close-path");
    System.out.println("    close path");
    System.out.println("\n  -cpefs|--close-path-eo-fill-stroke");
    System.out.println("    close path, fill and stroke using even-odd fill rule");
    System.out.println("\n  -cpfs|--close-path-fill-stroke");
    System.out.println("    close path, fill and stroke");
    System.out.println("\n  -cps|--close-path-stroke");
    System.out.println("    close path and stroke");
    System.out.println("\n  -cs|--char-spacing");
    System.out.println("    set character spacing (default: 0)");
    System.out.println("\n  -ct|--curve-to [X1:Y1:]X2:Y2:X3:Y3");
    System.out.println("    append Bézier curve");
    System.out.println("\n  -ef|--eo-fill");
    System.out.println("    fill using even-odd fill rule");
    System.out.println("\n  -efs|--eo-fill-stroke");
    System.out.println("    fill and stroke using even-odd fill rule");
    System.out.println("\n  -el|--ellipse X1:Y1:X2:Y2");
    System.out.println("    draw arc segment");
    System.out.println("\n  -ep|--end-path");
    System.out.println("    end path without filling or stroking it");
    System.out.println("\n  -f|--fill");
    System.out.println("    fill curent path");
    System.out.println("\n  -fc|--fill-color COLOR");
    System.out.println("    set fill color (default: black)");
    System.out.println("\n  -ff|--font-file FONT");
    System.out.println("    set font (default: Liberation Sans Regular)");
    System.out.println("\n  -fs|--fill-stroke");
    System.out.println("    fill and stroke curent path");
    System.out.println("\n  -hs|--horizontal-scaling");
    System.out.println("    set horizontal font scaling in percent (default: 100)");
    System.out.println("\n  -i|--image IMAGE:x:y[:w=WIDTH][:h=HEIGHT][:c=CORNER]");
    System.out.println("    draw image (default: preserve original dimensions)");
    System.out.println("\n  -lc|--line-cap-style NUM");
    System.out.println("    set line cap style (0-2, default: 0)");
    System.out.println("\n  -ld|--line-dash NUM[:NUM...]");
    System.out.println("    set line dash style (default: \"0\", i.e. solid line)");
    System.out.println("\n  -le|--leading NUM");
    System.out.println("    set leading (default: 11)");
    System.out.println("\n  -lj|--line-join-style NUM");
    System.out.println("    set line join style (0-2, default: 0)");
    System.out.println("\n  -lt|--line-to X:Y");
    System.out.println("    draw straight line");
    System.out.println("\n  -lw|--line-width NUM");
    System.out.println("    set leading (default: 0)");
    System.out.println("\n  -ml|--miter-limit NUM");
    System.out.println("    set miter limit (default: 10)");
    System.out.println("\n  -mt|--move-to X:Y");
    System.out.println("    move cursor");
    System.out.println("\n  -p|--pages PAGE|[FROM]-[TO][:PAGE|[FROM]-[TO]...]");
    System.out.println("    apply subsequent commands to specified pages (initial: 1)");
    System.out.println("\n  -ps|--font-size NUM");
    System.out.println("    set font size (default: 9)");
    System.out.println("\n  -re|--rectangle X:Y:WIDTH:HEIGHT");
    System.out.println("    draw rectangle");
    System.out.println("\n  -rm|--text-rendering-mode NUM");
    System.out.println("    set text rendering mode (0-7, default: 0)");
    System.out.println("\n  -rr|--rounded-rectangle X:Y:WIDTH:HEIGHT:RADIUS");
    System.out.println("    draw rounded rectangle");
    System.out.println("\n  -s|--stroke");
    System.out.println("    stroke curent path");
    System.out.println("\n  -sc|--stroke-color COLOR");
    System.out.println("    set stroke color (default: black)");
    System.out.println("\n  -t|--text TEXT[:X:Y][:cs=CHAR-SPACING][:fc=FILL-COLOR][:ff=FONT]");
    System.out.println("            [:hs=HORIZONTAL-SCALING][:le=LEADING][:lw=LINE-WIDTH]");
    System.out.println("            [:ps=SIZE][:rm=RENDERING-MODE][:sc=STROKE-COLOR]");
    System.out.println("            [:tr=TEXT-RISE][:ws=WORD-SPACING]");
    System.out.println("    write text");
    System.out.println("\n  -tm|--text-matrix A:B:C:D:X:Y");
    System.out.println("    set text matrix");
    System.out.println("\n  -tp|--text-pos X:Y");
    System.out.println("    set text position");
    System.out.println("\n  -tr|--text-rise NUM");
    System.out.println("    set text rise (default: 0)");
    System.out.println("\n  -ws|--word-spacing NUM");
    System.out.println("    set word spacing (default: 0)");
    System.out.println("\n  -x|--literal STR");
    System.out.println("    write arbitrary string to content stream");
    System.out.println("\nNegative positions (incl. \"-0\") are calculated in reverse direction from the");
    System.out.println("edge of the page.");
    System.out.println("\nColors can be specified in hex or by name (e.g., \"magenta\").");
    System.out.println("\nText can include variables: \"{page}\" = page number, \"{pages}\" = total number");
    System.out.println("of pages, \"{filename}\" = file name, without path, \"{pathname}\" = file name,");
    System.out.println("incl. path. New line can be inserted by \"^\".");
    System.out.println("\nReference corner can be specified as: \"ll\" = lower left (default), \"lr\" =");
    System.out.println("lower right, \"ul\" = upper right, \"ur\" = upper right.");
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
      line = new SeqParser(':').parse(options, args, true);
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
