/* ModifiedPdfStamper.java
 *
 * Copyright (C) 2015, Tomas Pecina <tomas@pecina.cz>
 *
 * This file is part of cz.pecina.pdf, a suite of PDF processing applications.
 *
 * This application is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.         
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.pecina.pdf.addpdfmeta;

import java.lang.reflect.Field;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.DocumentException;
import java.util.logging.Logger;
import cz.pecina.pdf.addpdfmeta.ModifiedXmpWriter;

/**
 * Modified PDF stamper.
 *
 * @author @AUTHOR@
 * @version @VERSION@
 */
public class ModifiedPdfStamper extends PdfStamper {

    // static logger
    private static final Logger log = Logger.getLogger(ModifiedPdfStamper.class.getName());

    // metadata input stream
    private InputStream metadataInputStream;

    /** Starts the process of adding extra content to an existing PDF
     * document.
     * <p>
     * The reader will be closed when this PdfStamper is closed
     *
     * @param  reader the original document. It cannot be reused
     * @param  os the output stream
     * @param  metadataInputStream the metadata input stream
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public ModifiedPdfStamper(final PdfReader reader, final OutputStream os, final InputStream metadataInputStream) throws DocumentException, IOException {
        super(reader, os);
	this.metadataInputStream = metadataInputStream;
    }

    /**
     * Closes the document. No more content can be written after the
     * document is closed.
     * <p>
     * If closing a signed document with an external signature the closing must be done
     * in the <CODE>PdfSignatureAppearance</CODE> instance.
     *
     * @throws DocumentException on error
     * @throws IOException on error
     */
    @Override
    public void close() throws DocumentException, IOException {    
	createXmpMetadata();
	try {
	    Field field = PdfWriter.class.getDeclaredField("xmpWriter");
	    field.setAccessible(true);
	    field.set(stamper, new ModifiedXmpWriter(metadataInputStream));
	} catch (Exception exception) {
	    System.err.println("Unexpected error in ModifiedPdfStamper, exception: " + exception);
	    log.fine("Unexpected error in ModifiedPdfStamper, exception: " + exception);
	    System.exit(1);
	}
	super.close();
    }
}
