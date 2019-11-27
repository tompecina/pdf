/* RmWmark.java
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

import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.AreaBreakType;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Remove watermark from PDF file.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class RmWmark {

  // static logger
  private static final Logger log = Logger.getLogger(RmWmark.class.getName());

  // for description see Object
  @Override
  public String toString() {
    return "RmWmark";
  }

  // set small value to zero
  private static float norm(final float arg) {
    return ((arg > -1f) && (arg < 1f)) ? 0f : arg;
  }

  // event listener
  private static class Listener implements IEventListener {

    private Document doc;
    private PdfDocument outDoc;
    private boolean firstPage = true;

    Listener(final Document doc) {
      this.doc = doc;
      this.outDoc = doc.getPdfDocument();
    }

    public void eventOccurred(final IEventData data, final EventType type) {
      final ImageRenderInfo renderInfo = (ImageRenderInfo) data;
      final Matrix ctm = renderInfo.getImageCtm();
      final PdfImageXObject imageXObject = renderInfo.getImage().copyTo(outDoc);
      final float i11 = norm(ctm.get(Matrix.I11));
      final float i12 = norm(ctm.get(Matrix.I12));
      final float i21 = norm(ctm.get(Matrix.I21));
      final float i22 = norm(ctm.get(Matrix.I22));
      final float width = Math.max(Math.abs(i11), Math.abs(i12));
      final float height = Math.max(Math.abs(i21), Math.abs(i22));
      final Image image = new Image(imageXObject, 0, 0);
      image.setRotationAngle(Math.atan2(Math.signum(i12), Math.signum(i11)));
      image.setPadding(0);
      image.scaleAbsolute(width, height);
      if (!firstPage) {
        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
      }
      firstPage = false;
      doc.add(image);
    }

    public Set<EventType> getSupportedEvents() {
      return EnumSet.of(EventType.RENDER_IMAGE);
    }
  }

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    log.fine("Application started");

    final Parameters parameters = new Parameters(args);

    byte[] inputData = null;
    String outFileName = null;

    try {
      inputData = Files.readAllBytes(Paths.get(parameters.getFileName(0)));
      outFileName = parameters.getFileName(parameters.numberFileNames() - 1);
    } catch (final Exception exception) {
      System.err.println("Error opening files, exception: " + exception);
      log.fine("Error opening files, exception: " + exception);
      System.exit(1);
    }

    try {
      final ModPdfReader reader = new ModPdfReader(new ByteArrayInputStream(inputData));
      reader.resetEncrypted();
      final PdfDocument inDoc = new PdfDocument(reader);
      final int numberPages = inDoc.getNumberOfPages();
      final PdfDocumentContentParser parser = new PdfDocumentContentParser(inDoc);
      final PdfWriter writer = new PdfWriter(new FileOutputStream(outFileName));
      final PdfDocument outDoc = new PdfDocument(writer);
      final Document doc = new Document(outDoc);
      final IEventListener listener = new Listener(doc);

      for (int pageNumber = 1; pageNumber <= numberPages; pageNumber++) {
        final PdfPage inPage = inDoc.getPage(pageNumber);
        outDoc.addNewPage();
        final PdfPage outPage = outDoc.getPage(pageNumber);
        outPage.setMediaBox(inPage.getMediaBox());
        outPage.setRotation(inPage.getRotation());
        parser.processContent(pageNumber, listener);
      }
      doc.close();
      writer.close();
      reader.close();
    } catch (final Exception exception) {
      System.err.println("Error processing files, exception: " + exception);
      log.fine("Error processing files, exception: " + exception);
      System.exit(1);
    }

    log.fine("Application terminated normally");
  }
}
