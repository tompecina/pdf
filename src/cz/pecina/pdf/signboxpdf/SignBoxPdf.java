/* SignBoxPdf.java
 *
 * Copyright (C) 2017, Tomas Pecina <tomas@pecina.cz>
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

package cz.pecina.pdf.signboxpdf;

import java.io.OutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfAppearance;
import java.util.logging.Logger;
import cz.pecina.pdf.signboxpdf.Parameters;

/**
 * Stamp PDF file.
 *
 * @author @AUTHOR@
 * @version @VERSION@
 */
public class SignBoxPdf {

    // static logger
    private static final Logger log = Logger.getLogger(SignBoxPdf.class.getName());

    // for description see Object
    @Override
    public String toString() {
	return "SignBoxPdf";
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
	float width = parameters.getWidth();
	float height = parameters.getHeight();
	int page = parameters.getPage();
	String signatureFieldName = parameters.getSignatureFieldName();
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
	    final PdfStamper stamper = new PdfStamper(reader, fileOutputStream, '\0');
	    final PdfWriter writer = stamper.getWriter();
	    final PdfFormField field = PdfFormField.createSignature(writer);
	    field.setFieldName(signatureFieldName);
	    field.setPage(page);
	    field.setWidget(
		new Rectangle(xOffset, yOffset, xOffset + width, yOffset + height),
		PdfAnnotation.HIGHLIGHT_NONE);
	    field.setFlags(PdfAnnotation.FLAGS_PRINT);
	    final PdfAppearance app = PdfAppearance.createAppearance(writer, width, height);
	    app.setColorFill(new BaseColor(.9f, .92f, 1f));
	    app.setLineWidth(0);
	    app.rectangle(0f, 0f, width, height);
	    app.fill();
	    field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, app);
	    stamper.addAnnotation(field, page);
	    stamper.close();
	    fileOutputStream.close();
	    reader.close();
    	} catch (Exception exception) {
	    System.err.println("Error processing files, exception: " + exception);
	    log.fine("Error processing files, exception: " + exception);
	    System.exit(1);
	}
	
	log.fine("Application terminated normally");
    }
}
