/* ModPdfReader.java
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

import com.itextpdf.kernel.pdf.PdfReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Modified PdfReader allowing encryption flag reset.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class ModPdfReader extends PdfReader {

  // static logger
  private static final Logger log = Logger.getLogger(ModPdfReader.class.getName());

  // for description see Object
  @Override
  public String toString() {
    return "ModPdfReader";
  }

  /**
   * Reads and parses a PDF document.
   *
   * @param stream the InputStream containing the document
   * @throws IOException on error
   */
  public ModPdfReader(final InputStream stream) throws IOException {
    super(stream);
  }

  /**
   * Resets the encrypted flag.
   */
  public void resetEncrypted() {
    setUnethicalReading(true);
    encrypted = false;
  }
}
