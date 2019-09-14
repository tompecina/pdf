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
import com.itextpdf.kernel.pdf.PdfName;
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
import java.io.FileOutputStream;
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

  // general drawing parameters
  private static class GenPar implements Cloneable {

    // color parameters
    public Color fillColor = DEFAULT_FILL_COLOR;
    public Color strokeColor = DEFAULT_STROKE_COLOR;

    // drawing parameters
    public float lineWidth = DEFAULT_LINE_WIDTH;

    // for description see Object
    @Override
    public GenPar clone() {
      try {
        return (GenPar) super.clone();
      } catch (final CloneNotSupportedException exception) {
        error("Error during cloning: " + exception.getMessage());
        return null;
      }
    }
  }

  // font parameters
  private static class FontPar implements Cloneable {

    // font parameters
    public String fontFilename = DEFAULT_FONT_FILENAME;
    public float fontSize = DEFAULT_FONT_SIZE;
    public float leading = DEFAULT_LEADING;
    public float characterSpacing = DEFAULT_CHARACTER_SPACING;
    public float wordSpacing = DEFAULT_WORD_SPACING;
    public float textRise = DEFAULT_TEXT_RISE;
    public int textRenderingMode = DEFAULT_TEXT_RENDERING_MODE;
    public float horizontalScaling = DEFAULT_TEXT_HORIZONTAL_SCALING;

    // for description see Object
    @Override
    public FontPar clone() {
      try {
        return (FontPar) super.clone();
      } catch (final CloneNotSupportedException exception) {
        error("Error during cloning: " + exception.getMessage());
        return null;
      }
    }
  }

  // report error and exit
  private static void error(final String message) {
    log.fine(message);
    System.err.println(message);
    System.exit(1);
  }

  // make array of double sub-parameters
  private static double[] makeArrDouble(final int num, final Parameter par, final String message) {
    final double[] res = new double[num];
    for (int i = 0; i < num; i++) {
      final SubParameter sub = par.getSubParameter(i);
      if (sub == null) {
        error(message);
      }
      res[i] = sub.getAsDouble();
    }
    return res;
  }

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    log.fine("Application started");

    final Parameters par = new Parameters(args);

    byte[] inputData = null;
    String outFileName = null;

    try {
      inputData = Files.readAllBytes(Paths.get(par.getFileName(0)));
      outFileName = par.getFileName(par.numberFileNames() - 1);
    } catch (final Exception exception) {
      error("Error opening files, exception: " + exception);
    }

    try {
      final PdfReader reader = new PdfReader(new ByteArrayInputStream(inputData));
      final StampingProperties prop = new StampingProperties().preserveEncryption().useAppendMode();
      final PdfWriter writer = new PdfWriter(new FileOutputStream(outFileName));
      final PdfDocument doc = new PdfDocument(reader, writer, prop);
      final int numPages = doc.getNumberOfPages();
      final boolean[] pageNums = new boolean[numPages];
      pageNums[0] = true;
      final List<List<Parameter>> commands = new ArrayList<>();
      for (int pageNum = 0; pageNum < numPages; pageNum++) {
        commands.add(new ArrayList<Parameter>());
      }
      final Map<String, PdfXObject> images = new HashMap<>();
      final Map<String, FontProgram> fontPrograms = new HashMap<>();

      for (Parameter command: par.getParameters()) {
        final String name = command.getOption().getName();

        switch (name) {

          case "pages": {
            Arrays.fill(pageNums, false);
            for (SubParameter sub: command.getSubParameters()) {
              final String[] range = sub.getAsString().split("-", 2);
              if (range.length == 1) {
                final int num = Integer.parseInt(range[0]);
                if ((num < 1) || (num >= numPages)) {
                  error("Invalid page number: " + range[0]);
                }
                pageNums[num - 1] = true;
              } else {
                final int from = (range[0].length() == 0) ? 1 : Integer.parseInt(range[0]);
                final int to = (range[1].length() == 0) ? numPages : Integer.parseInt(range[1]);
                if (from > to) {
                  error("Invalid range of page numbers: " + sub);
                }
                for (int pageNum = from; pageNum <= to; pageNum++) {
                  pageNums[pageNum - 1] = true;
                }
              }
            }
            break;
          }

          default: {

            switch (name) {

              case "image": {
                final String imageFilename = command.getSubParameter(0).getAsString();
                final Image image = imageFilename.toLowerCase().endsWith(".svg") ?
                    SvgConverter.convertToImage(new FileInputStream(imageFilename), doc) :
                    new Image(ImageDataFactory.create(imageFilename));
                images.put(imageFilename, image.getXObject());
                break;
              }

              case "font-file": {
                final String fontFilename = command.getSubParameter(0).getAsString();
                try {
                  fontPrograms.put(fontFilename, FontProgramFactory.createFont(fontFilename));
                } catch (final Exception exception) {
                  error("Failed to process font: " + fontFilename);
                }
                break;
              }

              case "text": {
                final SubParameter sub = command.getKwSubParameter("ff");
                if (sub != null) {
                  final String fontFilename = sub.getAsString();
                  try {
                    fontPrograms.put(fontFilename, FontProgramFactory.createFont(fontFilename));
                  } catch (final Exception exception) {
                    error("Failed to process font: " + fontFilename);
                  }
                }
                break;
              }
            }

            for (int pageNum = 0; pageNum < numPages; pageNum++) {
              if (pageNums[pageNum]) {
                commands.get(pageNum).add(command);
              }
            }
          }
        }
      }

      for (int pageNum = 0; pageNum < numPages; pageNum++) {
        if (!commands.get(pageNum).isEmpty()) {
          final PdfPage page = doc.getPage(pageNum + 1);
          final float pageWidth = page.getPageSize().getWidth();
          final float pageHeight = page.getPageSize().getHeight();
          final PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), doc);
          canvas.saveState();
          boolean textBegun = false;
          final GenPar genPar = new GenPar();
          final FontPar fontPar = new FontPar();

          final Map<String, PdfFont> fonts = new HashMap<>();
          fonts.put(DEFAULT_FONT_FILENAME, PdfFontFactory.createFont(DEFAULT_FONT_FILENAME, PdfEncodings.IDENTITY_H, true));
          for (String name : fontPrograms.keySet()) {
            if (!fonts.containsKey(name)) {
              fonts.put(name, PdfFontFactory.createFont(name, PdfEncodings.IDENTITY_H, true));
            }
          }

          for (Parameter command: commands.get(pageNum)) {
            switch (command.getOption().getName()) {

              case "arc": {
                final double[] arr = makeArrDouble(6, command, "Invalid arc parameters");
                canvas.arc(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5]);
                break;
              }

              case "char-spacing": {
                fontPar.characterSpacing = command.getSubParameter(0).getAsFloat();
                break;
              }

              case "circle": {
                final double[] arr = makeArrDouble(3, command, "Invalid circle parameters");
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
                genPar.fillColor = WebColors.getRGBColor(command.getSubParameter(0).getAsString());
                canvas.setFillColor(genPar.fillColor);
                genPar.strokeColor =
                    WebColors.getRGBColor(command.getSubParameter(command.getNumSubParameters() - 1).getAsString());
                canvas.setStrokeColor(genPar.strokeColor);
                break;
              }

              case "curve-from-to": {
                final double[] arr = makeArrDouble(4, command, "Invalid Bézier curve parameters");
                canvas.curveFromTo(arr[0], arr[1], arr[2], arr[3]);
                break;
              }

              case "curve-to": {
                final int numPar = command.getNumSubParameters();
                final double[] arr = makeArrDouble(numPar, command, "Invalid Bézier curve parameters");
                switch (numPar) {
                  case 4: {
                    canvas.curveTo(arr[0], arr[1], arr[2], arr[3]);
                    break;
                  }
                  case 6: {
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
                final double[] arr = makeArrDouble(4, command, "Invalid ellipse parameters");
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
                genPar.fillColor = WebColors.getRGBColor(command.getSubParameter(0).getAsString());
                canvas.setFillColor(genPar.fillColor);
                break;
              }

              case "fill-stroke": {
                canvas.fillStroke();
                break;
              }

              case "font-file": {
                fontPar.fontFilename = command.getSubParameter(0).getAsString();
                break;
              }

              case "font-size": {
                fontPar.fontSize = command.getSubParameter(0).getAsFloat();
                break;
              }

              case "image": {
                final String imageFilename = (String) command.getSubParameter(0).getAsString();
                final PdfXObject image = images.get(imageFilename);
                final float imageOrigWidth = image.getWidth();
                final float imageOrigHeight = image.getHeight();
                final SubParameter width = command.getKwSubParameter("w");
                float imageWidth = ((width == null) || width.isEmpty()) ? 0f : width.getAsFloat();
                final SubParameter height = command.getKwSubParameter("h");
                float imageHeight = ((height == null) || height.isEmpty()) ? 0f : height.getAsFloat();
                float imageScaleX = 1f;
                float imageScaleY = 1f;
                if ((imageWidth > 0f) && (imageHeight > 0f)) {
                  imageScaleX = imageWidth / imageOrigWidth;
                  imageScaleY = imageHeight / imageOrigHeight;
                } else if (imageWidth > 0f) {
                  imageScaleX = imageWidth / imageOrigWidth;
                  imageScaleY = imageScaleX;
                  imageHeight = imageOrigHeight * imageScaleY;
                } else if (imageHeight > 0f) {
                  imageScaleX = imageHeight / imageOrigHeight;
                  imageScaleY = imageScaleX;
                  imageWidth = imageOrigWidth * imageScaleX;
                } else {
                  imageWidth = imageOrigWidth;
                  imageHeight = imageOrigHeight;
                }
                if (!imageFilename.toLowerCase().endsWith(".svg")) {
                  imageScaleX = imageWidth;
                  imageScaleY = imageHeight;
                }
                final SubParameter posX = command.getSubParameter(1);
                final SubParameter posY = command.getSubParameter(2);
                if ((posX.isEmpty()) || posY.isEmpty()) {
                  error("Invalid image position");
                }
                float imageX = Math.abs(posX.getAsFloat());
                if (posX.getAsString().startsWith("-")) {
                  imageX += pageWidth - imageWidth;
                }
                float imageY = Math.abs(posY.getAsFloat());
                if (posY.getAsString().startsWith("-")) {
                  imageY += pageHeight - imageHeight;
                }
                final SubParameter corner = command.getKwSubParameter("c");
                if (corner != null) {
                  switch (corner.getAsString()) {
                    case "ll": {
                      break;
                    }
                    case "lr": {
                      imageX -= imageWidth;
                      break;
                    }
                    case "ul": {
                      imageY -= imageHeight;
                      break;
                    }
                    case "ur": {
                      imageX -= imageWidth;
                      imageY -= imageHeight;
                      break;
                    }
                    default: {
                      error("Invalid corner");
                    }
                  }
                }
                canvas.addXObject(image, new Rectangle(imageX, imageY, imageScaleX, imageScaleY));
                break;
              }

              case "line-to": {
                final double[] arr = makeArrDouble(2, command, "Invalid line parameters");
                canvas.lineTo(arr[0], arr[1]);
                break;
              }

              case "leading": {
                fontPar.leading = command.getSubParameter(0).getAsFloat();
                break;
              }

              case "line-cap-style": {
                canvas.setLineCapStyle(command.getSubParameter(0).getAsInt());
                break;
              }

              case "line-dash": {
                int numPar = command.getNumSubParameters();
                final float phase = command.getSubParameter(--numPar).getAsFloat();
                final float[] pattern = new float[numPar];
                for (int i = 0; i < numPar; i++) {
                  pattern[i] = command.getSubParameter(i).getAsFloat();
                }
                canvas.setLineDash(pattern, phase);
                break;
              }

              case "line-join-style": {
                canvas.setLineJoinStyle(command.getSubParameter(0).getAsInt());
                break;
              }

              case "line-width": {
                genPar.lineWidth = command.getSubParameter(0).getAsFloat();
                canvas.setLineWidth(genPar.lineWidth);
                break;
              }

              case "literal": {
                final SubParameter sub = command.getSubParameter(0);
                if (sub == null) {
                  error("Empty literal");
                }
                canvas.writeLiteral(sub.getAsString());
                break;
              }

              case "miter-limit": {
                canvas.setMiterLimit(command.getSubParameter(0).getAsFloat());
                break;
              }

              case "move-to": {
                final double[] arr = makeArrDouble(2, command, "Invalid position parameters");
                canvas.moveTo(arr[0], arr[1]);
                break;
              }

              case "rectangle": {
                final double[] arr = makeArrDouble(4, command, "Invalid rectangle parameters");
                arr[0] = Math.abs(arr[0]);
                if (command.getSubParameter(0).getAsString().startsWith("-")) {
                  arr[0] += pageWidth - arr[2];
                }
                arr[1] = Math.abs(arr[1]);
                if (command.getSubParameter(1).getAsString().startsWith("-")) {
                  arr[1] += pageHeight - arr[3];
                }
                final SubParameter corner = command.getKwSubParameter("c");
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
                canvas.rectangle(arr[0], arr[1], arr[2], arr[3]);
                break;
              }

              case "round-rectangle": {
                final double[] arr = makeArrDouble(5, command, "Invalid round rectangle parameters");
                arr[0] = Math.abs(arr[0]);
                if (command.getSubParameter(0).getAsString().startsWith("-")) {
                  arr[0] += pageWidth - arr[2];
                }
                arr[1] = Math.abs(arr[1]);
                if (command.getSubParameter(1).getAsString().startsWith("-")) {
                  arr[1] += pageHeight - arr[3];
                }
                final SubParameter corner = command.getKwSubParameter("c");
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
                canvas.roundRectangle(arr[0], arr[1], arr[2], arr[3], arr[4]);
                break;
              }

              case "stroke": {
                canvas.stroke();
                break;
              }

              case "stroke-color": {
                genPar.strokeColor = WebColors.getRGBColor(command.getSubParameter(0).getAsString());
                canvas.setStrokeColor(genPar.strokeColor);
                break;
              }

              case "text": {
                final GenPar tempGenPar = genPar.clone();
                final FontPar tempFontPar = fontPar.clone();
                if (!textBegun) {
                  canvas.beginText();
                  textBegun = true;
                }
                if (command.hasKwSubParameter("cs")) {
                  tempFontPar.characterSpacing = command.getKwSubParameter("cs").getAsFloat();
                }
                if (command.hasKwSubParameter("fc")) {
                  tempGenPar.fillColor = WebColors.getRGBColor(command.getKwSubParameter("fc").getAsString());
                }
                if (command.hasKwSubParameter("ff")) {
                  tempFontPar.fontFilename = command.getKwSubParameter("ff").getAsString();
                }
                if (command.hasKwSubParameter("hs")) {
                  tempFontPar.horizontalScaling = command.getKwSubParameter("hs").getAsFloat();
                }
                if (command.hasKwSubParameter("lw")) {
                  tempGenPar.lineWidth = command.getKwSubParameter("lw").getAsFloat();
                }
                if (command.hasKwSubParameter("ps")) {
                  tempFontPar.fontSize = command.getKwSubParameter("ps").getAsFloat();
                }
                if (command.hasKwSubParameter("rm")) {
                  tempFontPar.textRenderingMode = command.getKwSubParameter("rm").getAsInt();
                }
                if (command.hasKwSubParameter("sc")) {
                  tempGenPar.strokeColor = WebColors.getRGBColor(command.getKwSubParameter("sc").getAsString());
                }
                if (command.hasKwSubParameter("tr")) {
                  tempFontPar.textRise = command.getKwSubParameter("tr").getAsFloat();
                }
                if (command.hasKwSubParameter("ws")) {
                  tempFontPar.wordSpacing = command.getKwSubParameter("ws").getAsFloat();
                }

                canvas.setFillColor(tempGenPar.fillColor);
                canvas.setStrokeColor(tempGenPar.strokeColor);
                canvas.setLineWidth(tempGenPar.lineWidth);
                canvas.setFontAndSize(fonts.get(tempFontPar.fontFilename), tempFontPar.fontSize);
                canvas.setLeading(tempFontPar.leading);
                canvas.setCharacterSpacing(tempFontPar.characterSpacing);
                canvas.setWordSpacing(tempFontPar.wordSpacing);
                canvas.setTextRise(tempFontPar.textRise);
                canvas.setTextRenderingMode(tempFontPar.textRenderingMode);
                canvas.setHorizontalScaling(tempFontPar.horizontalScaling);
                final int numSubPar = command.getNumSubParameters();
                if (numSubPar == 3) {
                  final SubParameter subX = command.getSubParameter(1);
                  final SubParameter subY = command.getSubParameter(2);
                  if ((subX == null) || (subY == null)) {
                    error("Invalid text position");
                  }
                  float textX = Math.abs(subX.getAsFloat());
                  if (subX.getAsString().startsWith("-")) {
                    textX += pageWidth;
                  }
                  float textY = Math.abs(subY.getAsFloat());
                  if (subY.getAsString().startsWith("-")) {
                    textY += pageWidth;
                  }
                  canvas.setTextMatrix(textX, textY);
                } else if (numSubPar != 1) {
                  error("Invalid text position parameters");
                }
                String text = command.getSubParameter(0).getAsString();
                if (!text.isEmpty()) {
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
                }

                if (!tempGenPar.fillColor.equals(genPar.fillColor)) {
                  canvas.setFillColor(genPar.fillColor);
                }
                if (!tempGenPar.strokeColor.equals(genPar.strokeColor)) {
                  canvas.setStrokeColor(genPar.strokeColor);
                }
                if (tempGenPar.lineWidth != genPar.lineWidth) {
                  canvas.setLineWidth(genPar.lineWidth);
                }
                break;
              }

              case "text-horizontal-scaling": {
                fontPar.horizontalScaling = command.getSubParameter(0).getAsFloat();
                break;
              }

              case "text-matrix": {
                if (!textBegun) {
                  canvas.beginText();
                  textBegun = true;
                }
                final float[] matrix = new float[6];
                for (int i = 0; i < 6; i++) {
                  final SubParameter sub = command.getSubParameter(i);
                  if (sub == null) {
                    error("Invalid text matrix");
                  }
                  matrix[i] = sub.getAsFloat();
                }
                canvas.setTextMatrix(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
                break;
              }

              case "text-pos": {
                if (!textBegun) {
                  canvas.beginText();
                  textBegun = true;
                }
                final SubParameter subX = command.getSubParameter(0);
                final SubParameter subY = command.getSubParameter(1);
                if ((subX == null) || (subY == null)) {
                  error("Invalid text position");
                }
                float textX = Math.abs(subX.getAsFloat());
                if (subX.getAsString().startsWith("-")) {
                  textX += pageWidth;
                }
                float textY = Math.abs(subY.getAsFloat());
                if (subY.getAsString().startsWith("-")) {
                  textY += pageWidth;
                }
                canvas.setTextMatrix(textX, textY);
                break;
              }

              case "text-rendering-mode": {
                fontPar.textRenderingMode = command.getSubParameter(0).getAsInt();
                break;
              }

              case "text-rise": {
                fontPar.textRise = command.getSubParameter(0).getAsFloat();
                break;
              }

              case "word-spacing": {
                fontPar.wordSpacing = command.getSubParameter(0).getAsFloat();
                break;
              }

            }
          }

          if (textBegun) {
            canvas.endText();
            textBegun = true;
          }

          canvas.restoreState();
          canvas.release();
        }
      }
      doc.close();
      reader.close();
    } catch (final NumberFormatException exception) {
      error("Invalid number format: " + exception.getMessage());
    } catch (final PdfException exception) {
      error("Error during PDF operation: " + exception.getMessage());
    } catch (final Exception exception) {
      error("Error processing files, exception: " + exception);
    }

    log.fine("Application terminated normally");
  }
}
