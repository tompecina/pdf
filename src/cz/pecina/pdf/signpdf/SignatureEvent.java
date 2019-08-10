/* SignatureEvent.java
 *
 * Copyright (C) 2015, Tomas Pecina <tomas@pecina.cz>
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

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfName;

/**
 * Event removing unneeded fields.
 *
 * @author @AUTHOR@
 * @version @VERSION@
 */
public class SignatureEvent implements PdfSignatureAppearance.SignatureEvent {

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
    public SignatureEvent(String reason, String location, String contact) {
	this.reason = reason;
	this.location = location;
	this.contact = contact;
    }

    // for description see PdfSignatureAppearance.SignatureEvent
    public void getSignatureDictionary(PdfDictionary sig) {
	if (reason == null) {
	    sig.remove(PdfName.REASON);
	}
	if (location == null) {
	    sig.remove(PdfName.LOCATION);
	}
	if (contact == null) {
	    sig.remove(PdfName.CONTACTINFO);
	}
    }
}
