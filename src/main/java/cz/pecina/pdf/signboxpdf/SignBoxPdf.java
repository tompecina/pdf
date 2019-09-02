/* SignBoxPdf.java
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

package cz.pecina.pdf.signboxpdf;

import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
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
public class SignBoxPdf {

  // static logger
  private static final Logger log = Logger.getLogger(SignBoxPdf.class.getName());

  // for description see Object
  @Override
  public String toString() {
    return "SignBoxPdf";
  }

  // background color
  private static final DeviceRgb BG_COLOR = new DeviceRgb(.9f, .92f, 1f);

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    log.fine("Application started");

    final Parameters parameters = new Parameters(args);

    final float xOffset = parameters.getXOffset();
    final float yOffset = parameters.getYOffset();
    final float width = parameters.getWidth();
    final float height = parameters.getHeight();
    final int page = parameters.getPage();
    final String signatureFieldName = parameters.getSignatureFieldName();
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
      final PdfWriter writer = new PdfWriter(new FileOutputStream(outFileName));
      final PdfDocument pdfDocument = new PdfDocument(reader, writer);
      final Rectangle rect = new Rectangle(xOffset, yOffset, xOffset + width, yOffset + height);
      final PdfFormField field = PdfFormField.createSignature(pdfDocument, rect);
      field.setFieldName(signatureFieldName);
      field.setPage(page);
      final PdfCanvas canvas = new PdfCanvas(pdfDocument.getPage(page));
      final PdfExtGState extGState = new PdfExtGState();
      extGState.setBlendMode(PdfExtGState.BM_MULTIPLY);
      canvas.setExtGState(extGState);
      canvas.rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
      canvas.setFillColor(BG_COLOR);
      canvas.fill();
      canvas.release();
      pdfDocument.close();
      writer.close();
      reader.close();
    } catch (Exception exception) {
      System.err.println("Error processing files, exception: " + exception);
      log.fine("Error processing files, exception: " + exception);
      System.exit(1);
    }

    log.fine("Application terminated normally");
  }
}
