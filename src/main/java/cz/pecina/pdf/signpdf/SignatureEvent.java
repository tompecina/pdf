/* SignatureEvent.java
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

import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.signatures.PdfSignature;
import com.itextpdf.signatures.PdfSigner;

/**
 * Event removing unneeded fields.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class SignatureEvent implements PdfSigner.ISignatureEvent {

  // for description see Object
  @Override
  public String toString() {
    return "SignatureEvent";
  }

  // local copies of field settings
  private String reason;
  private String location;
  private String contact;

  /**
   * Default constructor.
   *
   * @param reason   signature reason
   * @param location signature location
   * @param contact  signer's contact information
   */
  public SignatureEvent(final String reason, final String location, final String contact) {
    this.reason = reason;
    this.location = location;
    this.contact = contact;
  }

  // for description see PdfSignatureAppearance.SignatureEvent
  @Override
  public void getSignatureDictionary(final PdfSignature sig) {
    final PdfDictionary dict = (PdfDictionary) sig.getPdfObject();
    if (reason == null) {
      dict.remove(PdfName.Reason);
    }
    if (location == null) {
      dict.remove(PdfName.Location);
    }
    if (contact == null) {
      dict.remove(PdfName.ContactInfo);
    }
  }
}
