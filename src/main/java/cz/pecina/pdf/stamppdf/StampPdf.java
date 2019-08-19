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

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
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

    /**
     * Main method.
     *
     * @param args command-line arguments
     */
    public static void main(final String args[]) {
	log.fine("Application started");

	Parameters parameters = new Parameters(args);

	float xOffset = parameters.getXOffset();
	float yOffset = parameters.getYOffset();
	String text = parameters.getText();
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
	    final PdfReader reader = new PdfReader(inputData);
	    final OutputStream fileOutputStream = new FileOutputStream(outFileName);
	    final PdfStamper stamper = new PdfStamper(reader, fileOutputStream, '\0', true);
	    final PdfContentByte over = stamper.getOverContent(1);
	    over.beginText();
	    final String resourcePath = "cz/pecina/pdf";
	    final BaseFont baseFont = BaseFont.createFont(resourcePath + "/fonts/LiberationSans-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
	    over.setFontAndSize(baseFont, 9);
	    over.setCharacterSpacing(0.1f);
	    over.setLeading(11);
	    if (xOffset < 0) {
	    	xOffset = reader.getPageSize(1).getWidth() + xOffset;
	    }
	    if (yOffset < 0) {
	    	yOffset = reader.getPageSize(1).getHeight() + yOffset;
	    }
	    over.setTextMatrix(xOffset, yOffset);
	    for (String line: text.split("^")) {
		over.showText(line);
		over.newlineText();
		over.endText();
	    }
	    stamper.close();
	    reader.close();
	    fileOutputStream.close();
    	} catch (Exception exception) {
	    System.err.println("Error processing files, exception: " + exception);
	    log.fine("Error processing files, exception: " + exception);
	    System.exit(1);
	}
	
	log.fine("Application terminated normally");
    }
}
