/* StampPdf.java
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

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.WebColors;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfXObject;
import com.itextpdf.layout.element.Image;
import com.itextpdf.svg.converter.SvgConverter;
import cz.pecina.seqparser.Parameter;
import cz.pecina.seqparser.SubParameter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Stamp PDF file.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class StampPdf {

  // static logger
  private static final Logger log = Logger.getLogger(StampPdf.class.getName());

  // for description see Object
  @Override
  public String toString() {
    return "StampPdf";
  }

  // color defaults
  private static final Color DEFAULT_FILL_COLOR = WebColors.getRGBColor("black");
  private static final Color DEFAULT_STROKE_COLOR = WebColors.getRGBColor("black");

  // drawing defaults
  private static final float DEFAULT_LINE_WIDTH = 1f;

  // font defaults
  private static final String RESOURCE_PATH = "cz/pecina/pdf";
  private static final String DEFAULT_FONT_FILENAME = RESOURCE_PATH + "/fonts/LiberationSans-Regular.ttf";
  private static final float DEFAULT_FONT_SIZE = 9f;
  private static final float DEFAULT_LEADING = 11f;  // default = 0
  private static final float DEFAULT_CHARACTER_SPACING = 0f;
  private static final float DEFAULT_WORD_SPACING = 0f;
  private static final float DEFAULT_TEXT_RISE = 0f;
  private static final int DEFAULT_TEXT_RENDERING_MODE = 0;
  private static final float DEFAULT_TEXT_HORIZONTAL_SCALING = 100f;

  // font parameters
  private String fontFilename = DEFAULT_FONT_FILENAME;
  private float fontSize = DEFAULT_FONT_SIZE;
  private float leading = DEFAULT_LEADING;
  private float characterSpacing = DEFAULT_CHARACTER_SPACING;
  private float wordSpacing = DEFAULT_WORD_SPACING;
  private float textRise = DEFAULT_TEXT_RISE;
  private int textRenderingMode = DEFAULT_TEXT_RENDERING_MODE;
  private float horizontalScaling = DEFAULT_TEXT_HORIZONTAL_SCALING;

  // other fields
  private Parameters par;
  private PdfDocument doc;
  private int numPages;
  private boolean[] pageNums;
  private Map<String, PdfXObject> images = new HashMap<>();
  private Map<String, FontProgram> fontPrograms = new HashMap<>();
  private Map<String, PdfFont> fonts = new HashMap<>();
  private List<List<Parameter>> cmds = new ArrayList<>();
  private Parameter cmd;
  private SubParameter subParameter;
  private PdfCanvas canvas;
  private float pageWidth;
  private float pageHeight;
  private boolean textBegun = false;

  /**
   * Reports error and exits.
   *
   * @param message the error message to be logged and sent to stderr
   */
  protected static void error(final String message) {
    log.fine(message);
    System.err.println(message);
    System.exit(1);
  }

  /**
   * Adjusts x-type parameter.
   *
   * @param sub the sub-parameter containing the value
   * @return the adjusted x-coordinate
   */
  protected float adjustXFloat(final SubParameter sub) {
    float res = sub.getAsFloat();
    if (sub.getAsString().startsWith("-")) {
      res += pageWidth;
    }
    return res;
  }

  /**
   * Adjusts y-type parameter.
   *
   * @param sub the sub-parameter containing the value
   * @return the adjusted y-coordinate
   */
  protected float adjustYFloat(final SubParameter sub) {
    float res = sub.getAsFloat();
    if (sub.getAsString().startsWith("-")) {
      res += pageHeight;
    }
    return res;
  }

  /**
   * Adjusts x-type parameter.
   *
   * @param sub the sub-parameter containing the value
   * @return the adjusted x-coordinate
   */
  protected double adjustXDouble(final SubParameter sub) {
    double res = sub.getAsDouble();
    if (sub.getAsString().startsWith("-")) {
      res += pageWidth;
    }
    return res;
  }

  /**
   * Adjusts y-type parameter.
   *
   * @param sub the sub-parameter containing the value
   * @return the adjusted y-coordinate
   */
  protected double adjustYDouble(final SubParameter sub) {
    double res = sub.getAsDouble();
    if (sub.getAsString().startsWith("-")) {
      res += pageHeight;
    }
    return res;
  }

  /**
   * Adjusts the rectangle's position according to the reference corner.
   *
   * @param arr the rectagle array (llx, lly, width, height)
   */
  protected void adjustCorner(final float[] arr) {
    final SubParameter corner = cmd.getKwSubParameter("c");
    if (corner != null) {
      switch (corner.getAsString()) {
        case "ll": {
          break;
        }
        case "lr": {
          arr[0] -= arr[2];
          break;
        }
        case "ul": {
          arr[1] -= arr[3];
          break;
        }
        case "ur": {
          arr[0] -= arr[2];
          arr[1] -= arr[3];
          break;
        }
        default: {
          error("Invalid corner");
        }
      }
    }
  }

  /**
   * Adjusts the rectangle's position according to the reference corner.
   *
   * @param arr the rectagle array (llx, lly, width, height)
   */
  protected void adjustCorner(final double[] arr) {
    final SubParameter corner = cmd.getKwSubParameter("c");
    if (corner != null) {
      switch (corner.getAsString()) {
        case "ll": {
          break;
        }
        case "lr": {
          arr[0] -= arr[2];
          break;
        }
        case "ul": {
          arr[1] -= arr[3];
          break;
        }
        case "ur": {
          arr[0] -= arr[2];
          arr[1] -= arr[3];
          break;
        }
        default: {
          error("Invalid corner");
        }
      }
    }
  }

  /**
   * Extracts an array of double sub-parameters.
   *
   * @param num the number of parameters to be extracted from the sub-parameters
   * @param message the error message
   * @return the array of extracted parameters
   */
  protected double[] extractArr(final int num, final String message) {
    return extractArr(num, message, new int[0], new int[0]);
  }

  /**
   * Extracts an array of double sub-parameters, adjusting x- and y-coordinates as needed.
   *
   * @param num the number of parameters to be extracted from the sub-parameters
   * @param message the error message
   * @param parX list of sub-parameter indices for application of x-adjustment
   * @param parY list of sub-parameter indices for application of y-adjustment
   * @return the array of extracted parameters
   */
  protected double[] extractArr(final int num, final String message, final int[] parX, final int[] parY) {
    final double[] res = new double[num];
    for (int i = 0; i < num; i++) {
      final SubParameter sub = cmd.getSubParameter(i);
      if (sub == null) {
        error(message);
      }
      res[i] = sub.getAsDouble();
    }
    for (int i : parX) {
      final SubParameter sub = cmd.getSubParameter(i);
      if (sub.getAsString().startsWith("-")) {
        res[i] += pageWidth;
      }
    }
    for (int i : parY) {
      final SubParameter sub = cmd.getSubParameter(i);
      if (sub.getAsString().startsWith("-")) {
        res[i] += pageHeight;
      }
    }
    return res;
  }

  // first parsing pass
  private void pass1() throws FileNotFoundException, IOException {

    for (Parameter tempCmd : par.getParameters()) {
      cmd = tempCmd;

      final String name = cmd.getOption().getName();

      switch (name) {

        case "pages": {
          Arrays.fill(pageNums, false);
          for (SubParameter sub : cmd.getSubParameters()) {
            final String[] range = sub.getAsString().split("-", 2);
            if (range.length == 1) {
              final int num = Integer.valueOf(range[0]);
              if (num < 1) {
                error("Invalid page number: " + num);
              }
              if (num <= numPages) {
                pageNums[num - 1] = true;
              }
            } else {
              final int from = (range[0].length() == 0) ? 1 : Integer.valueOf(range[0]);
              final int to = (range[1].length() == 0) ? numPages : Integer.valueOf(range[1]);
              if (from < 1) {
                error("Invalid page number: " + from);
              }
              for (int pageNum = from; pageNum <= to; pageNum++) {
                if (pageNum <= numPages) {
                  pageNums[pageNum - 1] = true;
                }
              }
            }
          }
          break;
        }

        default: {

          switch (name) {

            case "image": {
              final String imageFilename = cmd.getSubParameter(0).getAsString();
              final Image image = imageFilename.toLowerCase().endsWith(".svg")
                  ? SvgConverter.convertToImage(new FileInputStream(imageFilename), doc)
                  : new Image(ImageDataFactory.create(imageFilename));
              images.put(imageFilename, image.getXObject());
              break;
            }

            case "font-file": {
              final String font = cmd.getSubParameter(0).getAsString();
              try {
                fontPrograms.put(font, FontProgramFactory.createFont(font));
              } catch (final Exception exception) {
                error("Failed to process font: " + font);
              }
              break;
            }

            case "text": {
              final SubParameter sub = cmd.getKwSubParameter("ff");
              if (sub != null) {
                final String font = sub.getAsString();
                try {
                  fontPrograms.put(font, FontProgramFactory.createFont(font));
                } catch (final Exception exception) {
                  error("Failed to process font: " + font);
                }
              }
              break;
            }

            default: {
              // no action
            }
          }

          for (int pageNum = 0; pageNum < numPages; pageNum++) {
            if (pageNums[pageNum]) {
              cmds.get(pageNum).add(cmd);
            }
          }
        }
      }
    }
  }

  // parse image
  private void parseImage() {

    final String imageFilename = (String) cmd.getSubParameter(0).getAsString();
    final PdfXObject image = images.get(imageFilename);
    final float imageOrigWidth = image.getWidth();
    final float imageOrigHeight = image.getHeight();
    final SubParameter width = cmd.getKwSubParameter("w");
    final float[] arr = new float[4];
    arr[2] = ((width == null) || width.isEmpty()) ? 0f : width.getAsFloat();
    final SubParameter height = cmd.getKwSubParameter("h");
    arr[3] = ((height == null) || height.isEmpty()) ? 0f : height.getAsFloat();
    float imageScaleX = 1f;
    float imageScaleY = 1f;
    if ((arr[2] > 0f) && (arr[3] > 0f)) {
      imageScaleX = arr[2] / imageOrigWidth;
      imageScaleY = arr[3] / imageOrigHeight;
    } else if (arr[2] > 0f) {
      imageScaleX = arr[2] / imageOrigWidth;
      imageScaleY = imageScaleX;
      arr[3] = imageOrigHeight * imageScaleY;
    } else if (arr[3] > 0f) {
      imageScaleX = arr[3] / imageOrigHeight;
      imageScaleY = imageScaleX;
      arr[2] = imageOrigWidth * imageScaleX;
    } else {
      arr[2] = imageOrigWidth;
      arr[3] = imageOrigHeight;
    }
    if (!imageFilename.toLowerCase().endsWith(".svg")) {
      imageScaleX = arr[2];
      imageScaleY = arr[3];
    }
    final SubParameter posX = cmd.getSubParameter(1);
    final SubParameter posY = cmd.getSubParameter(2);
    if ((posX.isEmpty()) || posY.isEmpty()) {
      error("Invalid image position");
    }
    arr[0] = adjustXFloat(posX);
    arr[2] = adjustYFloat(posY);
    adjustCorner(arr);
    canvas.addXObject(image, new Rectangle(arr[0], arr[1], imageScaleX, imageScaleY));
  }

  // begin text if needed
  private void beginText() {
    if (!textBegun) {
      canvas.beginText();
      textBegun = true;
    }
  }

  // end text if needed
  private void endText() {
    if (textBegun) {
      canvas.endText();
      textBegun = true;
    }
  }

  // parse text
  private void parseText(final int pageNum) {

    String tempFontFilename = fontFilename;
    float tempFontSize = fontSize;

    canvas.saveState();

    beginText();

    canvas.setCharacterSpacing(cmd.hasKwSubParameter("cs") ? cmd.getKwSubParameter("cs").getAsFloat() : characterSpacing);
    if (cmd.hasKwSubParameter("fc")) {
      canvas.setFillColor(WebColors.getRGBColor(cmd.getKwSubParameter("fc").getAsString()));
    }
    if (cmd.hasKwSubParameter("ff")) {
      tempFontFilename = cmd.getKwSubParameter("ff").getAsString();
    }
    canvas.setHorizontalScaling(cmd.hasKwSubParameter("hs") ? cmd.getKwSubParameter("hs").getAsFloat() : horizontalScaling);
    canvas.setLeading(cmd.hasKwSubParameter("le") ? cmd.getKwSubParameter("le").getAsFloat() : leading);
    if (cmd.hasKwSubParameter("lw")) {
      canvas.setLineWidth(cmd.getKwSubParameter("lw").getAsFloat());
    }
    if (cmd.hasKwSubParameter("ps")) {
      tempFontSize = cmd.getKwSubParameter("ps").getAsFloat();
    }
    canvas.setTextRenderingMode(cmd.hasKwSubParameter("rm") ? cmd.getKwSubParameter("rm").getAsInt() : textRenderingMode);
    if (cmd.hasKwSubParameter("sc")) {
      canvas.setStrokeColor(WebColors.getRGBColor(cmd.getKwSubParameter("sc").getAsString()));
    }
    canvas.setTextRise(cmd.hasKwSubParameter("tr") ? cmd.getKwSubParameter("tr").getAsFloat() : textRise);
    canvas.setWordSpacing(cmd.hasKwSubParameter("ws") ? cmd.getKwSubParameter("ws").getAsFloat() : wordSpacing);

    canvas.setFontAndSize(fonts.get(tempFontFilename), tempFontSize);

    final int numSubPar = cmd.getNumSubParameters();
    if (numSubPar == 3) {
      final SubParameter subX = cmd.getSubParameter(1);
      final SubParameter subY = cmd.getSubParameter(2);
      if ((subX == null) || (subY == null)) {
        error("Invalid text position");
      }
      final float textX = adjustXFloat(subX);
      final float textY = adjustYFloat(subY);
      canvas.setTextMatrix(textX, textY);
    } else if (numSubPar != 1) {
      error("Invalid text position parameters");
    }
    String text = cmd.getSubParameter(0).getAsString();
    text = text.replace("{page}", "" + (pageNum + 1));
    text = text.replace("{pages}", "" + numPages);
    final String inputFilename = par.getFileName(0);
    text = text.replace("{pathname}", inputFilename);
    final String[] parts = inputFilename.split(".+?/(?=[^/]+$)");
    text = text.replace("{filename}", parts[parts.length - 1]);
    final String[] lines = text.split("\\^");
    canvas.showText(lines[0]);
    for (int i = 1; i < lines.length; i++) {
      canvas.newlineShowText(lines[i]);
    }

    canvas.restoreState();
  }

  // second parsing pass
  @SuppressWarnings("checkstyle:MethodLength")
  private void pass2() throws IOException {

    for (int pageNum = 0; pageNum < numPages; pageNum++) {

      if (!cmds.get(pageNum).isEmpty()) {

        final PdfPage page = doc.getPage(pageNum + 1);
        pageWidth = page.getPageSize().getWidth();
        pageHeight = page.getPageSize().getHeight();
        canvas = new PdfCanvas(page, true);

        canvas.saveState();

        fonts.clear();
        fonts.put(DEFAULT_FONT_FILENAME, PdfFontFactory.createFont(DEFAULT_FONT_FILENAME, PdfEncodings.IDENTITY_H, true));
        for (String name : fontPrograms.keySet()) {
          if (!fonts.containsKey(name)) {
            fonts.put(name, PdfFontFactory.createFont(name, PdfEncodings.IDENTITY_H, true));
          }
        }

        for (Parameter tempCmd : cmds.get(pageNum)) {
          cmd = tempCmd;

          switch (cmd.getOption().getName()) {

            case "arc": {
              final double[] arr = extractArr(6, "Invalid arc parameters", new int[] {0, 2}, new int[] {1, 3});
              canvas.arc(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
              break;
            }

            case "char-spacing": {
              characterSpacing = cmd.getSubParameter(0).getAsFloat();
              break;
            }

            case "circle": {
              final double[] arr = extractArr(3, "Invalid circle parameters", new int[] {0}, new int[] {1});
              canvas.circle(arr[0], arr[1], arr[2]);
              break;
            }

            case "close-path": {
              canvas.closePath();
              break;
            }

            case "close-path-eo-fill-stroke": {
              canvas.closePathEoFillStroke();
              break;
            }

            case "close-path-fill-stroke": {
              canvas.closePathFillStroke();
              break;
            }

            case "close-path-stroke": {
              canvas.closePathStroke();
              break;
            }

            case "color": {
              canvas.setFillColor(WebColors.getRGBColor(cmd.getSubParameter(0).getAsString()));
              canvas.setStrokeColor(WebColors.getRGBColor(cmd.getSubParameter(cmd.getNumSubParameters() - 1).getAsString()));
              break;
            }

            case "curve-from-to": {
              final double[] arr = extractArr(4, "Invalid Bézier curve parameters", new int[] {0, 2}, new int[] {1, 3});
              canvas.curveFromTo(arr[0], arr[1], arr[2], arr[3]);
              break;
            }

            case "curve-to": {
              final int numPar = cmd.getNumSubParameters();
              switch (numPar) {
                case 4: {
                  final double[] arr =
                      extractArr(numPar, "Invalid Bézier curve parameters", new int[] {0, 2}, new int[] {1, 3});
                  canvas.curveTo(arr[0], arr[1], arr[2], arr[3]);
                  break;
                }
                case 6: {
                  final double[] arr =
                      extractArr(numPar, "Invalid Bézier curve parameters", new int[] {0, 2, 4}, new int[] {1, 3, 5});
                  canvas.curveTo(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
                  break;
                }
                default: {
                  error("Invalid number of Bézier curve parameters");
                }
              }
              break;
            }

            case "ellipse": {
              final double[] arr = extractArr(4, "Invalid ellipse parameters", new int[] {0, 2}, new int[] {1, 3});
              canvas.ellipse(arr[0], arr[1], arr[2], arr[3]);
              break;
            }

            case "end-path": {
              canvas.endPath();
              break;
            }

            case "eo-fill": {
              canvas.eoFill();
              break;
            }

            case "eo-fill-stroke": {
              canvas.eoFillStroke();
              break;
            }

            case "fill": {
              canvas.fill();
              break;
            }

            case "fill-color": {
              canvas.setFillColor(WebColors.getRGBColor(cmd.getSubParameter(0).getAsString()));
              break;
            }

            case "fill-stroke": {
              canvas.fillStroke();
              break;
            }

            case "font-file": {
              fontFilename = cmd.getSubParameter(0).getAsString();
              break;
            }

            case "font-size": {
              fontSize = cmd.getSubParameter(0).getAsFloat();
              break;
            }

            case "image": {
              parseImage();
              break;
            }

            case "line-to": {
              final double[] arr = extractArr(2, "Invalid line parameters", new int[] {0}, new int[] {1});
              canvas.lineTo(arr[0], arr[1]);
              break;
            }

            case "leading": {
              leading = cmd.getSubParameter(0).getAsFloat();
              break;
            }

            case "line-cap-style": {
              canvas.setLineCapStyle(cmd.getSubParameter(0).getAsInt());
              break;
            }

            case "line-dash": {
              int numPar = cmd.getNumSubParameters();
              final float phase = cmd.getSubParameter(--numPar).getAsFloat();
              final float[] pattern = new float[numPar];
              for (int i = 0; i < numPar; i++) {
                pattern[i] = cmd.getSubParameter(i).getAsFloat();
              }
              canvas.setLineDash(pattern, phase);
              break;
            }

            case "line-join-style": {
              canvas.setLineJoinStyle(cmd.getSubParameter(0).getAsInt());
              break;
            }

            case "line-width": {
              canvas.setLineWidth(cmd.getSubParameter(0).getAsFloat());
              break;
            }

            case "literal": {
              final SubParameter sub = cmd.getSubParameter(0);
              if (sub == null) {
                error("Empty literal");
              }
              canvas.writeLiteral(sub.getAsString());
              break;
            }

            case "miter-limit": {
              canvas.setMiterLimit(cmd.getSubParameter(0).getAsFloat());
              break;
            }

            case "move-to": {
              final double[] arr = extractArr(2, "Invalid position parameters", new int[] {0}, new int[] {1});
              canvas.moveTo(arr[0], arr[1]);
              break;
            }

            case "rectangle": {
              final double[] arr = extractArr(4, "Invalid rectangle parameters", new int[] {0}, new int[] {1});
              adjustCorner(arr);
              canvas.rectangle(arr[0], arr[1], arr[2], arr[3]);
              break;
            }

            case "round-rectangle": {
              final double[] arr = extractArr(5, "Invalid round rectangle parameters", new int[] {0}, new int[] {1});
              adjustCorner(arr);
              canvas.roundRectangle(arr[0], arr[1], arr[2], arr[3], arr[4]);
              break;
            }

            case "stroke": {
              canvas.stroke();
              break;
            }

            case "stroke-color": {
              canvas.setStrokeColor(WebColors.getRGBColor(cmd.getSubParameter(0).getAsString()));
              break;
            }

            case "text": {
              parseText(pageNum);
              break;
            }

            case "text-horizontal-scaling": {
              horizontalScaling = cmd.getSubParameter(0).getAsFloat();
              break;
            }

            case "text-matrix": {
              beginText();
              final float[] matrix = new float[6];
              for (int i = 0; i < 6; i++) {
                final SubParameter sub = cmd.getSubParameter(i);
                if (sub == null) {
                  error("Invalid text matrix");
                }
                if (i < 4) {
                  matrix[i] = sub.getAsFloat();
                } else if (i == 4) {
                  matrix[i] = adjustXFloat(sub);
                } else {
                  matrix[i] = adjustYFloat(sub);
                }
              }
              canvas.setTextMatrix(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
              break;
            }

            case "text-pos": {
              beginText();
              final SubParameter subX = cmd.getSubParameter(0);
              final SubParameter subY = cmd.getSubParameter(1);
              if ((subX == null) || (subY == null)) {
                error("Invalid text position");
              }
              final float textX = adjustXFloat(subX);
              final float textY = adjustYFloat(subY);
              canvas.setTextMatrix(textX, textY);
              break;
            }

            case "text-rendering-mode": {
              textRenderingMode = cmd.getSubParameter(0).getAsInt();
              break;
            }

            case "text-rise": {
              textRise = cmd.getSubParameter(0).getAsFloat();
              break;
            }

            case "word-spacing": {
              wordSpacing = cmd.getSubParameter(0).getAsFloat();
              break;
            }

            default: {
              // no action
            }

          }
        }

        endText();

        canvas.restoreState();
        canvas.release();
      }
    }
  }

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new StampPdf(args);
  }

  /**
   * Main constructor.
   *
   * @param args command-line arguments
   */
  public StampPdf(final String[] args) {
    log.fine("Application started");

    par = new Parameters(args);

    byte[] inputData = null;
    String outFileName = null;

    try {
      inputData = Files.readAllBytes(Paths.get(par.getFileName(0)));
      outFileName = par.getFileName(par.numberFileNames() - 1);
    } catch (final IOException exception) {
      error("Error opening files, exception: " + exception.getMessage());
    }

    try {

      final PdfReader reader = new PdfReader(new ByteArrayInputStream(inputData));
      final StampingProperties prop = new StampingProperties().preserveEncryption().useAppendMode();
      final PdfWriter writer = new PdfWriter(new FileOutputStream(outFileName));
      doc = new PdfDocument(reader, writer, prop);
      numPages = doc.getNumberOfPages();
      pageNums = new boolean[numPages];
      pageNums[0] = true;
      for (int pageNum = 0; pageNum < numPages; pageNum++) {
        cmds.add(new ArrayList<Parameter>());
      }

      pass1();

      pass2();

      doc.close();
      reader.close();

    } catch (final FileNotFoundException exception) {
      error("File not found, exception: " + exception.getMessage());

    } catch (final IOException exception) {
      error("Error opening files, exception: " + exception.getMessage());

    } catch (final NumberFormatException exception) {
      error("Invalid number format: " + exception.getMessage());

    } catch (final PdfException exception) {
      error("Error during PDF operation: " + exception.getMessage());

    } catch (final Exception exception) {
      error("Error processing files, exception: " + exception.getMessage());
    }

    log.fine("Application terminated normally");
  }
}
