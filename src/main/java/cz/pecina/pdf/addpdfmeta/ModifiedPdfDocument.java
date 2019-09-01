/* ModifiedPdfDocument.java
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

package cz.pecina.pdf.addpdfmeta;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.util.logging.Logger;


/**
 * Modified PDF document.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class ModifiedPdfDocument extends PdfDocument {

  // static logger
  private static final Logger log = Logger.getLogger(ModifiedPdfDocument.class.getName());

  /**
   * Starts the process of adding extra content to an existing PDF
   * document.
   *
   * <p>The reader will be closed when this PdfDocument is closed
   *
   * @param  reader the original document. It cannot be reused
   * @param  writer the new writer
   * @param  metadata the metadata
   */
  public ModifiedPdfDocument(final PdfReader reader, final PdfWriter writer, final byte[] metadata) {
    super(reader, writer);
    xmpMetadata = metadata;
  }

  // for description see PdfDocument
  protected void updateXmpMetadata() {
  }
}
