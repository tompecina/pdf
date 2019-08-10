/* ModifiedXmpWriter.java
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
 */

package cz.pecina.pdf.addpdfmeta;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import com.itextpdf.text.xml.xmp.XmpWriter;
import java.util.logging.Logger;

/**
 * Modified XMP writer.
 *
 * @author @AUTHOR@
 * @version @VERSION@
 */
public class ModifiedXmpWriter extends XmpWriter {

    // static logger
    private static final Logger log = Logger.getLogger(ModifiedXmpWriter.class.getName());

    // metadata input stream
    private InputStream metadataInputStream;

    /**
     * Creates an XmpWriter.
     *
     * @param  metadataInputStream the metadata input stream
     * @throws IOException
     */
    public ModifiedXmpWriter(final InputStream metadataInputStream) throws IOException {
	super(new ByteArrayOutputStream());
	this.metadataInputStream = metadataInputStream;
    }

    /**
     * Flushes and closes the XmpWriter.
     *
     */
    @Override
    public void serialize(OutputStream externalOutputStream) {
	try {
	    while (true) {
		int d = metadataInputStream.read();
		if (d < 0)
		    break;
		externalOutputStream.write(d);
	    }
	} catch (Exception exception) {
	    System.err.println("Unexpected error in ModifiedXmpWriter, exception: " + exception);
	    log.fine("Unexpected error in ModifiedXmpWriter, exception: " + exception);
	    System.exit(1);
	}
    }
}
