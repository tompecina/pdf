/* SignPdf.java
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


import java.util.Calendar;

import java.util.logging.Logger;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.KeyStore;
import java.security.Security;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.StampingProperties;

import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;

import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import com.itextpdf.kernel.geom.Rectangle;

import com.itextpdf.kernel.colors.ColorConstants;

import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFont;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import com.itextpdf.io.font.PdfEncodings;

import com.itextpdf.layout.element.Image;

import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.SignatureUtil;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.CertificateInfo;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * Sign PDF file.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class SignPdf {

    // static logger
    private static final Logger log = Logger.getLogger(SignPdf.class.getName());

    // for description see Object
    @Override
    public String toString() {
	return "SignPdf";
    }

    /**
     * Main method.
     *
     * @param args command-line arguments
     */
    public static void main(final String args[]) {
	log.fine("Application started");

	final Parameters parameters = new Parameters(args);

	final String inFileName = parameters.getFileName(0);
	byte[] inputData = null;
	String outFileName = null;
	PrivateKey key = null;
	Certificate[] certificateChain = null;

	try {
	    inputData = Files.readAllBytes(Paths.get(parameters.getFileName(0)));
	    outFileName = parameters.getFileName(parameters.numberFileNames() - 1);
	} catch (Exception exception) {
	    System.err.println("Error opening files, exception: " + exception);
	    log.fine("Error opening files, exception: " + exception);
	    System.exit(1);
	}

	BouncyCastleProvider provider = null;
	String alias = parameters.getAlias();
	try {
	    provider = new BouncyCastleProvider();
	    Security.addProvider(provider);
	    final KeyStore keyStore = KeyStore.getInstance("pkcs12");
	    keyStore.load(new FileInputStream(parameters.getKeyFileName()), parameters.getPassword());
	    if (alias != null) {
		if (!keyStore.containsAlias(alias)) {
		    System.err.println("Alias not found");
		    log.fine("Alias not found");
		    System.exit(1);
		}
	    } else {
		alias = keyStore.aliases().nextElement();
	    }
	    key = (PrivateKey)keyStore.getKey(alias, parameters.getPassword());
	    certificateChain = keyStore.getCertificateChain(alias);
	} catch (Exception exception) {
	    System.err.println("Error setting up cryptography, exception: " + exception);
	    log.fine("Error setting up cryptography, exception: " + exception);
	    System.exit(1);
	}

	try {
	    final PdfReader reader = new PdfReader(new ByteArrayInputStream(inputData));
	    final StampingProperties prop = new StampingProperties().preserveEncryption();
	    if (parameters.getSignatureAppend()) {
		prop.useAppendMode();
	    }
	    final PdfSigner signer = new PdfSigner(reader, new FileOutputStream(outFileName), prop);
	    if ((parameters.getSignatureFieldName() != null) && !(new SignatureUtil(signer.getDocument())).getBlankSignatureNames().contains(parameters.getSignatureFieldName())) {
		System.err.println("Field not found");
		log.fine("Field not found");
		System.exit(1);
	    }
	    final PdfSignatureAppearance signatureAppearance = signer.getSignatureAppearance().setReuseAppearance(false);
	    if (parameters.getReason() != null) {
	    	signatureAppearance.setReason(parameters.getReason());
	    }
	    if (parameters.getLocation() != null) {
	    	signatureAppearance.setLocation(parameters.getLocation());
	    }
	    if (parameters.getContact() != null) {
	    	signatureAppearance.setContact(parameters.getContact());
	    }
	    signer.setCertificationLevel(parameters.getCertificationLevel());
	    final PrivateKeySignature signature = new PrivateKeySignature(key, DigestAlgorithms.SHA256, provider.getName());
	    final BouncyCastleDigest digest = new BouncyCastleDigest();
	    signer.setSignatureEvent(new SignatureEvent(parameters.getReason(), parameters.getLocation(), parameters.getContact()));
	    if (parameters.getSignatureFieldName() != null) {
	    	signer.setFieldName(parameters.getSignatureFieldName());
	    	final PdfFormXObject n0 = signatureAppearance.getLayer0();
		Rectangle bbox = n0.getBBox().toRectangle();
	    	float left = bbox.getLeft();
	    	float bottom = bbox.getBottom();
	    	float width = bbox.getWidth();
	    	float height = bbox.getHeight();
		final PdfCanvas canvas0 = new PdfCanvas(n0, signer.getDocument());
		canvas0.setFillColor(ColorConstants.WHITE);
	    	canvas0.rectangle(left, bottom, width, height);
	    	canvas0.fill();
	    	final PdfFormXObject n2 = signatureAppearance.getLayer2();
		bbox = n2.getBBox().toRectangle();
	    	left = bbox.getLeft();
	    	bottom = bbox.getBottom();
	    	width = bbox.getWidth();
	    	height = bbox.getHeight();
	    	if (height < 35.99) {
	    	    System.err.println("Signature field is too small");
	    	    log.fine("Signature field is too small");
	    	    System.exit(1);
	    	}
	    	String imageFileName = null;
	    	if (parameters.getCertificationLevel() > 0) {
	    	    imageFileName = "sealcer.png";
	    	} else {
	    	    imageFileName = "sealappr.png";
	    	}
		final ImageData imageData = ImageDataFactory.create(SignPdf.class.getResource("graphics/" + imageFileName));
		final Image image = new Image(imageData, 0f, (height - 36f));
	    	image.scaleToFit(10000f, 36f);
		final PdfCanvas canvas2 = new PdfCanvas(n2, signer.getDocument());
		final Rectangle rect = new Rectangle(0f, (height - 36f), image.getImageScaledWidth(), image.getImageScaledHeight());
		canvas2.addImage(imageData, rect, false);
	    	final String resourcePath = "cz/pecina/pdf";
	    	final PdfFont brm = PdfFontFactory.createFont(resourcePath + "/fonts/Carlito-Regular.ttf", PdfEncodings.IDENTITY_H, true);
	    	final PdfFont bbf = PdfFontFactory.createFont(resourcePath + "/fonts/Carlito-Bold.ttf", PdfEncodings.IDENTITY_H, true);
	    	canvas2.beginText();
	    	canvas2.setTextMatrix((rect.getWidth() + 8f), (height - 7f));
	    	canvas2.setLeading(8.5f);
	    	canvas2.setFontAndSize(brm, 7f);
	    	canvas2.showText("Digitálně podepsal: ");
	    	canvas2.setFontAndSize(bbf, 7f);
	    	canvas2.showText(CertificateInfo.getSubjectFields((X509Certificate)(certificateChain[0])).getField("CN"));
	    	canvas2.setFontAndSize(brm, 7f);
	    	canvas2.newlineText();
	    	canvas2.showText("Certifikát: " + CertificateInfo.getSubjectFields((X509Certificate)(certificateChain[0])).getField("OU"));
	    	canvas2.newlineText();
	    	canvas2.showText("Vydal: " + CertificateInfo.getIssuerFields((X509Certificate)(certificateChain[0])).getField("CN"));
	    	canvas2.newlineText();
	    	final Calendar dt = signer.getSignDate();
	    	canvas2.showText(String.format("Datum a čas:  %02d.%02d.%d %02d:%02d:%02d", dt.get(dt.DAY_OF_MONTH), (dt.get(dt.MONTH) + 1), dt.get(dt.YEAR), dt.get(dt.HOUR_OF_DAY), dt.get(dt.MINUTE), dt.get(dt.SECOND)));
	    	canvas2.endText();
	    }
	    signer.signDetached(digest, signature, certificateChain, null, null, null, 4096, PdfSigner.CryptoStandard.CMS);
	    reader.close();
    	} catch (Exception exception) {
	    System.err.println("Error processing files, exception: " + exception);
	    log.fine("Error processing files, exception: " + exception);
	    System.exit(1);
	}
	
	log.fine("Application terminated normally");
    }
}
