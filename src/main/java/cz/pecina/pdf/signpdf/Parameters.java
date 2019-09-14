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

package cz.pecina.pdf.signpdf;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
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

  // image defaults
  private static final float DEFAULT_IMAGE_WIDTH = 0f;
  private static final float DEFAULT_IMAGE_HEIGHT = 0f;
  private static final float DEFAULT_IMAGE_X = 0f;
  private static final boolean DEFAULT_IMAGE_X_DIR = false;
  private static final float DEFAULT_IMAGE_Y = 0f;
  private static final boolean DEFAULT_IMAGE_Y_DIR = false;

  // font and text defaults
  private static final String RESOURCE_PATH = "cz/pecina/pdf";
  private static final String DEFAULT_REGULAR_FONT = RESOURCE_PATH + "/fonts/Carlito-Regular.ttf";
  private static final String DEFAULT_BOLD_FONT = RESOURCE_PATH + "/fonts/Carlito-Bold.ttf";
  private static final float DEFAULT_FONT_SIZE = 7f;
  private static final float DEFAULT_LEADING = 8.5f;
  private static final float DEFAULT_TEXT_X = 35f;
  private static final boolean DEFAULT_TEXT_X_DIR = false;
  private static final float DEFAULT_TEXT_Y = 7f;
  private static final boolean DEFAULT_TEXT_Y_DIR = true;

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
        Option.builder("k")
        .longOpt("key")
        .hasArg()
        .argName("FILE")
        .desc("(required) key file")
        .build());
    options.addOption(
        Option.builder("p")
        .longOpt("pass")
        .hasArg()
        .argName("PASSWORD")
        .desc("password")
        .build());
    options.addOption(
        Option.builder("c")
        .longOpt("cert-level")
        .hasArg()
        .type(Number.class)
        .argName("LEVEL")
        .desc("certification level (O-3)")
        .build());
    options.addOption(
        Option.builder("f")
        .longOpt("field")
        .hasArg()
        .argName("FIELD")
        .desc("signature field name (if none, invisible signature is created)")
        .build());
    options.addOption(
        Option.builder("a")
        .longOpt("append")
        .desc("append signature as an update")
        .build());
    options.addOption(
        Option.builder("r")
        .longOpt("reason")
        .hasArg()
        .argName("REASON")
        .desc("reason")
        .build());
    options.addOption(
        Option.builder("l")
        .longOpt("location")
        .hasArg()
        .argName("LOCATION")
        .desc("location")
        .build());
    options.addOption(
        Option.builder("o")
        .longOpt("contact")
        .hasArg()
        .argName("CONTACT")
        .desc("contact information")
        .build());
    options.addOption(
        Option.builder("A")
        .longOpt("alias")
        .hasArg()
        .argName("ALIAS")
        .desc("alias in keystore (in none, first alias is used)")
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("image-file")
        .hasArg()
        .argName("FILENAME")
        .desc("name of file containing the image")
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("image-width")
        .hasArg()
        .type(Number.class)
        .argName("IMAGE-WIDTH")
        .desc("image width")
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("image-height")
        .hasArg()
        .type(Number.class)
        .argName("IMAGE-HEIGHT")
        .desc("image height")
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("image-x")
        .hasArg()
        .type(Number.class)
        .argName("POS")
        .desc(String.format("image x-position (default: %s%s)", (DEFAULT_IMAGE_X_DIR ? "-" : ""), DEFAULT_IMAGE_X))
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("image-y")
        .hasArg()
        .type(Number.class)
        .argName("POS")
        .desc(String.format("image y-position (default: %s%s)", (DEFAULT_IMAGE_Y_DIR ? "-" : ""), DEFAULT_IMAGE_Y))
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("regular-font")
        .hasArg()
        .argName("FILENAME")
        .desc("name of file containing regular font")
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("bold-font")
        .hasArg()
        .argName("FILENAME")
        .desc("name of file containing bold font")
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("font-size")
        .hasArg()
        .type(Number.class)
        .argName("SIZE")
        .desc(String.format("font size in points (default: %s)", DEFAULT_FONT_SIZE))
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("leading")
        .hasArg()
        .type(Number.class)
        .argName("LEADING")
        .desc(String.format("leading in points (default: %s)", DEFAULT_LEADING))
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("font-color")
        .hasArg()
        .argName("RRGGBB")
        .desc("font color")
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("text-x")
        .hasArg()
        .type(Number.class)
        .argName("POS")
        .desc(String.format("text x-position (default: %s%s)", (DEFAULT_TEXT_X_DIR ? "-" : ""), DEFAULT_TEXT_X))
        .build());
    options.addOption(
        Option.builder(null)
        .longOpt("text-y")
        .hasArg()
        .type(Number.class)
        .argName("POS")
        .desc(String.format("text y-position (default: %s%s)", (DEFAULT_TEXT_Y_DIR ? "-" : ""), DEFAULT_TEXT_Y))
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
    helpFormatter.printHelp("signpdf [options] infile [outfile]", options);
    System.out.print("\nNegative POS values are applied in the opposite direction.");
    System.out.println("\nThe source code is available from <https://github.com/tompecina/pdf>.");
  }

  // parsed parameters
  private String keyFileName;
  private char[] password = null;
  private int certificationLevel = 1;
  private String signatureFieldName = null;
  private boolean signatureAppend;
  private String reason = null;
  private String location = null;
  private String contact = null;
  private String alias = null;
  private String imageFilename = null;
  private float imageWidth = DEFAULT_IMAGE_WIDTH;
  private float imageHeight = DEFAULT_IMAGE_HEIGHT;
  private float imageX = DEFAULT_IMAGE_X;
  private float imageY = DEFAULT_IMAGE_Y;
  private boolean imageXDir = DEFAULT_IMAGE_X_DIR;
  private boolean imageYDir = DEFAULT_IMAGE_Y_DIR;
  private String regularFontFilename = DEFAULT_REGULAR_FONT;
  private String boldFontFilename = DEFAULT_BOLD_FONT;
  private float fontSize = DEFAULT_FONT_SIZE;
  private float leading = DEFAULT_LEADING;
  private Color fontColor = DeviceRgb.BLACK;
  private int fontGreen = 0;
  private int fontBlue = 0;
  private float textX = DEFAULT_TEXT_X;
  private float textY = DEFAULT_TEXT_Y;
  private boolean textXDir = DEFAULT_TEXT_X_DIR;
  private boolean textYDir = DEFAULT_TEXT_Y_DIR;
  private String[] fileNames;

  /**
   * Gets key file name.
   *
   * @return key file name
   */
  public String getKeyFileName() {
    return keyFileName;
  }

  /**
   * Gets password.
   *
   * @return password as character array
   */
  public char[] getPassword() {
    return password;
  }

  /**
   * Gets certification level.
   *
   * @return certification level
   */
  public int getCertificationLevel() {
    return certificationLevel;
  }

  /**
   * Gets signature field name.
   *
   * @return signature field name
   */
  public String getSignatureFieldName() {
    return signatureFieldName;
  }

  /**
   * Gets signature append flag.
   *
   * @return signature append flag (true=append)
   */
  public boolean getSignatureAppend() {
    return signatureAppend;
  }

  /**
   * Gets signature reason.
   *
   * @return signature reason
   */
  public String getReason() {
    return reason;
  }

  /**
   * Gets signature location.
   *
   * @return signature location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Gets signer's contact information.
   *
   * @return signer's contact information
   */
  public String getContact() {
    return contact;
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
   * Gets alias.
   *
   * @return alias
   */
  public String getAlias() {
    return alias;
  }

  /**
   * Gets the image width.
   *
   * @return the image width (0 = not set)
   */
  public float getImageWidth() {
    return imageWidth;
  }

  /**
   * Gets the image height.
   *
   * @return the image height (0 = not set)
   */
  public float getImageHeight() {
    return imageHeight;
  }

  /**
   * Gets the name of the file containing the image.
   *
   * @return name of file containing the image
   */
  public String getImageFilename() {
    return imageFilename;
  }

  /**
   * Gets image x-position.
   *
   * @return image x-position
   */
  public float getImageX() {
    return imageX;
  }

  /**
   * Gets image x-position direction.
   *
   * @return image x-position direction (false = normal, true = opposite)
   */
  public boolean getImageXDir() {
    return imageXDir;
  }

  /**
   * Gets image y-position.
   *
   * @return image y-position
   */
  public float getImageY() {
    return imageY;
  }

  /**
   * Gets image y-position direction.
   *
   * @return image y-position direction (false = normal, true = opposite)
   */
  public boolean getImageYDir() {
    return imageYDir;
  }

  /**
   * Gets the name of the file containing the regular font.
   *
   * @return name of file containing the regular font
   */
  public String getRegularFontFilename() {
    return regularFontFilename;
  }

  /**
   * Gets the name of the file containing the bold font.
   *
   * @return name of file containing the bold font
   */
  public String getBoldFontFilename() {
    return boldFontFilename;
  }

  /**
   * Gets font size.
   *
   * @return font size in points
   */
  public float getFontSize() {
    return fontSize;
  }

  /**
   * Gets leading.
   *
   * @return leading in points
   */
  public float getLeading() {
    return leading;
  }

  /**
   * Gets the font color.
   *
   * @return font color
   */
  public Color getFontColor() {
    return fontColor;
  }

  /**
   * Gets text x-position.
   *
   * @return text x-position
   */
  public float getTextX() {
    return textX;
  }

  /**
   * Gets text x-position direction.
   *
   * @return text x-position direction (false = normal, true = opposite)
   */
  public boolean getTextXDir() {
    return textXDir;
  }

  /**
   * Gets text y-position.
   *
   * @return text y-position
   */
  public float getTextY() {
    return textY;
  }

  /**
   * Gets text y-position direction.
   *
   * @return text y-position direction (false = normal, true = opposite)
   */
  public boolean getTextYDir() {
    return textYDir;
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

  // process signature-related parameters
  private void processSig(final CommandLine line) {

    if (!line.hasOption("k")) {
      System.err.println("Key file is required");
      log.fine("Error in paramters, missing key file");
      System.exit(1);
    }

    keyFileName = line.getOptionValue("k");
    signatureAppend = line.hasOption("a");

    if (line.hasOption("p")) {
      password = line.getOptionValue("p").toCharArray();
    }

    if (line.hasOption("c")) {
      try {
        certificationLevel = ((Number) line.getParsedOptionValue("c")).intValue();
      } catch (final Exception exception) {
        System.err.println("Error in certification level, exception: " + exception);
        log.fine("Failed to parse certification level, exception: " + exception);
        System.exit(1);
      }
      if ((certificationLevel < 0) || (certificationLevel > 3)) {
        System.err.println("Certification level must be 0-3");
        log.fine("Certification level out of range");
        System.exit(1);
      }
    }

    if (line.hasOption("f")) {
      signatureFieldName = line.getOptionValue("f");
    }

    if (line.hasOption("r")) {
      reason = line.getOptionValue("r");
    }

    if (line.hasOption("l")) {
      location = line.getOptionValue("l");
    }

    if (line.hasOption("o")) {
      contact = line.getOptionValue("o");
    }

    if (line.hasOption("A")) {
      alias = line.getOptionValue("A");
    }
  }

  // process appearance-related parameters
  private void processApp(final CommandLine line) {

    if (line.hasOption("image-file")) {
      imageFilename = line.getOptionValue("image-file");
    }

    if (line.hasOption("image-width")) {
      try {
        imageWidth = Float.parseFloat(line.getOptionValue("image-width"));
      } catch (final Exception exception) {
        System.err.println("Error in width, exception: " + exception);
        log.fine("Failed to parse width, exception: " + exception);
        System.exit(1);
      }
      if (imageWidth <= 0) {
        System.err.println("Width must be positive");
        log.fine("Width out of range");
        System.exit(1);
      }
    }

    if (line.hasOption("image-height")) {
      try {
        imageHeight = Float.parseFloat(line.getOptionValue("image-height"));
      } catch (final Exception exception) {
        System.err.println("Error in height, exception: " + exception);
        log.fine("Failed to parse height, exception: " + exception);
        System.exit(1);
      }
      if (imageHeight <= 0) {
        System.err.println("Height must be positive");
        log.fine("Height out of range");
        System.exit(1);
      }
    }

    if (line.hasOption("image-x")) {
      try {
        imageX = Math.abs(Float.parseFloat(line.getOptionValue("image-x")));
      } catch (final Exception exception) {
        System.err.println("Error in image x-position, exception: " + exception);
        log.fine("Failed to parse image x-position, exception: " + exception);
        System.exit(1);
      }
      imageXDir = line.getOptionValue("image-x").startsWith("-");
    }

    if (line.hasOption("image-y")) {
      try {
        imageY = Math.abs(Float.parseFloat(line.getOptionValue("image-y")));
      } catch (final Exception exception) {
        System.err.println("Error in image y-position, exception: " + exception);
        log.fine("Failed to parse image y-position, exception: " + exception);
        System.exit(1);
      }
      imageYDir = line.getOptionValue("image-y").startsWith("-");
    }

    if (line.hasOption("regular-font")) {
      regularFontFilename = line.getOptionValue("regular-font");
    }

    if (line.hasOption("bold-font")) {
      boldFontFilename = line.getOptionValue("bold-font");
    }

    if (line.hasOption("font-size")) {
      try {
        fontSize = Float.parseFloat(line.getOptionValue("font-size"));
      } catch (final Exception exception) {
        System.err.println("Error in font size, exception: " + exception);
        log.fine("Failed to parse font size, exception: " + exception);
        System.exit(1);
      }
      if (fontSize <= 0) {
        System.err.println("Font size must be positive");
        log.fine("Font size out of range");
        System.exit(1);
      }
    }

    if (line.hasOption("leading")) {
      try {
        leading = Float.parseFloat(line.getOptionValue("leading"));
      } catch (final Exception exception) {
        System.err.println("Error in leading, exception: " + exception);
        log.fine("Failed to parse leading, exception: " + exception);
        System.exit(1);
      }
      if (leading <= 0) {
        System.err.println("Leading must be positive");
        log.fine("Leading out of range");
        System.exit(1);
      }
    }

    if (line.hasOption("font-color")) {
      final String color = line.getOptionValue("font-color");
      if (color.length() != 6) {
        System.err.println("Invalid hex color string length");
        log.fine("Invalid hex color string length");
        System.exit(1);
      }
      try {
        fontColor = new DeviceRgb(
            Integer.parseInt(color.substring(0, 2), 16),
            Integer.parseInt(color.substring(2, 4), 16),
            Integer.parseInt(color.substring(4, 6), 16));
      } catch (final Exception exception) {
        System.err.println("Error in color string, exception: " + exception);
        log.fine("Failed to parse color string, exception: " + exception);
        System.exit(1);
      }
    }

    if (line.hasOption("text-x")) {
      try {
        textX = Math.abs(Float.parseFloat(line.getOptionValue("text-x")));
      } catch (final Exception exception) {
        System.err.println("Error in text x-position, exception: " + exception);
        log.fine("Failed to parse text x-position, exception: " + exception);
        System.exit(1);
      }
      textXDir = line.getOptionValue("text-x").startsWith("-");
    }

    if (line.hasOption("text-y")) {
      try {
        textY = Math.abs(Float.parseFloat(line.getOptionValue("text-y")));
      } catch (final Exception exception) {
        System.err.println("Error in text y-position, exception: " + exception);
        log.fine("Failed to parse text y-position, exception: " + exception);
        System.exit(1);
      }
      textYDir = line.getOptionValue("text-y").startsWith("-");
    }
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

    fileNames = line.getArgs();

    if ((fileNames.length < 1) || (fileNames.length > 2)) {
      usage();
      log.fine("Error in parameters");
      System.exit(1);
    }

    processSig(line);

    processApp(line);

    log.fine("Parameters set up");
  }
}
