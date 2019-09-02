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

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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

  // font constants
  private static final float FONT_SIZE = 9f;
  private static final float FONT_LEADING = 11f;
  private static final float CHAR_SPACING = 0.1f;

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    log.fine("Application started");

    final Parameters parameters = new Parameters(args);

    float xOffset = parameters.getXOffset();
    float yOffset = parameters.getYOffset();
    final String text = parameters.getText();
    byte[] inputData = null;
    String outFileName = null;

    try {
      inputData = Files.readAllBytes(Paths.get(parameters.getFileName(0)));
      outFileName = parameters.getFileName(parameters.numberFileNames() - 1);
    } catch (Exception exception) {
      System.err.println("Error opening files, exception: " + exception);
      log.fine("Error opening files, exception: " + exception);
      System.exit(1);
    }

    try {
      final PdfReader reader = new PdfReader(new ByteArrayInputStream(inputData));
      final StampingProperties prop = new StampingProperties().preserveEncryption().useAppendMode();
      final PdfWriter writer = new PdfWriter(new FileOutputStream(outFileName));
      final PdfDocument pdfDocument = new PdfDocument(reader, writer, prop);
      final int pageNum = parameters.getPageNum();
      if (pageNum > pdfDocument.getNumberOfPages()) {
        System.err.println("Illegal page number");
        log.fine("Illegal page number");
        System.exit(1);
      }
      final PdfPage pdfPage = pdfDocument.getPage(pageNum);
      final PdfCanvas canvas = new PdfCanvas(pdfPage.newContentStreamBefore(), pdfPage.getResources(), pdfDocument);
      canvas.beginText();
      final String resourcePath = "cz/pecina/pdf";
      final PdfFont baseFont =
          PdfFontFactory.createFont(resourcePath + "/fonts/LiberationSans-Regular.ttf", PdfEncodings.IDENTITY_H, true);
      canvas.setFontAndSize(baseFont, FONT_SIZE);
      canvas.setCharacterSpacing(CHAR_SPACING);
      canvas.setLeading(FONT_LEADING);
      if (xOffset < 0) {
        xOffset = pdfPage.getPageSize().getWidth() + xOffset;
      }
      if (yOffset < 0) {
        yOffset = pdfPage.getPageSize().getHeight() + yOffset;
      }
      canvas.setTextMatrix(xOffset, yOffset);
      for (String line : text.split("^")) {
        canvas.showText(line);
        canvas.newlineText();
        canvas.endText();
      }
      pdfDocument.close();
      reader.close();
    } catch (Exception exception) {
      System.err.println("Error processing files, exception: " + exception);
      log.fine("Error processing files, exception: " + exception);
      System.exit(1);
    }

    log.fine("Application terminated normally");
  }
}
