/* RmOPass.java
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

package cz.pecina.pdf.rmopass;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Remove owner password from PDF file.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class RmOPass {

  // static logger
  private static final Logger log = Logger.getLogger(RmOPass.class.getName());

  // for description see Object
  @Override
  public String toString() {
    return "RmOPass";
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
      final ModifiedPdfReader reader = new ModifiedPdfReader(new ByteArrayInputStream(inputData));
      reader.resetEncrypted();
      final PdfWriter writer = new PdfWriter(new FileOutputStream(outFileName));
      final PdfDocument outDoc = new PdfDocument(reader, writer);
      outDoc.close();
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
