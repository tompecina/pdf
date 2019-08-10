/* SignPdf.java
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

import java.util.Calendar;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.Security;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.CertificateInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.util.logging.Logger;
import cz.pecina.pdf.signpdf.Parameters;
import cz.pecina.pdf.signpdf.SignatureEvent;

/**
 * Sign PDF file.
 *
 * @author @AUTHOR@
 * @version @VERSION@
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

	Parameters parameters = new Parameters(args);

	String inFileName = parameters.getFileName(0);
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
	    KeyStore keyStore = KeyStore.getInstance("pkcs12");
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
	    PdfReader reader = new PdfReader(inputData);
	    if ((parameters.getSignatureFieldName() != null) && !reader.getAcroFields().getBlankSignatureNames().contains(parameters.getSignatureFieldName())) {
		System.err.println("Field not found");
		log.fine("Field not found");
		System.exit(1);
	    }
	    final OutputStream fileOutputStream = new FileOutputStream(outFileName);
	    PdfStamper stamper;
	    if (parameters.getSignatureAppend()) {
		stamper = PdfStamper.createSignature(reader, fileOutputStream, '\0', null, true);
	    } else {
		stamper = PdfStamper.createSignature(reader, fileOutputStream, '\0');
	    }
	    final PdfSignatureAppearance signatureAppearance = stamper.getSignatureAppearance();
	    if (parameters.getReason() != null) {
		signatureAppearance.setReason(parameters.getReason());
	    }
	    if (parameters.getLocation() != null) {
		signatureAppearance.setLocation(parameters.getLocation());
	    }
	    if (parameters.getContact() != null) {
		signatureAppearance.setContact(parameters.getContact());
	    }
	    signatureAppearance.setCertificationLevel(parameters.getCertificationLevel());
	    PrivateKeySignature signature = new PrivateKeySignature(key, DigestAlgorithms.SHA256, provider.getName());
	    BouncyCastleDigest digest = new BouncyCastleDigest();
	    signatureAppearance.setSignatureEvent(new SignatureEvent(parameters.getReason(), parameters.getLocation(), parameters.getContact()));
	    if (parameters.getSignatureFieldName() != null) {
		signatureAppearance.setVisibleSignature(parameters.getSignatureFieldName());
		final PdfTemplate n0 = signatureAppearance.getLayer(0);
		float left = n0.getBoundingBox().getLeft();
		float bottom = n0.getBoundingBox().getBottom();
		float width = n0.getBoundingBox().getWidth();
		float height = n0.getBoundingBox().getHeight();
		n0.setColorFill(BaseColor.WHITE);
		n0.rectangle(left, bottom, width, height);
		n0.fill();
		final PdfTemplate n2 = signatureAppearance.getLayer(2);
		left = n2.getBoundingBox().getLeft();
		bottom = n2.getBoundingBox().getBottom();
		width = n2.getBoundingBox().getWidth();
		height = n2.getBoundingBox().getHeight();
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
		final Image image = Image.getInstance(SignPdf.class.getResource("graphics/" + imageFileName));
		image.scaleToFit(10000f, 36f);
		image.setAbsolutePosition(0f, (height - 36f));
		n2.addImage(image);
		final String resourcePath = "cz/pecina/pdf/signpdf";
		final BaseFont brm = BaseFont.createFont(resourcePath + "/fonts/Carlito-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		final BaseFont bbf = BaseFont.createFont(resourcePath + "/fonts/Carlito-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		n2.beginText();
		n2.setTextMatrix((image.getScaledWidth() + 8f), (height - 7f));
		n2.setLeading(8.5f);
		n2.setFontAndSize(brm, 7f);
		n2.showText("Digitálně podepsal: ");
		n2.setFontAndSize(bbf, 7f);
		n2.showText(CertificateInfo.getSubjectFields((X509Certificate)(certificateChain[0])).getField("CN"));
		n2.setFontAndSize(brm, 7f);
		n2.newlineText();
		n2.showText("Certifikát: " + CertificateInfo.getSubjectFields((X509Certificate)(certificateChain[0])).getField("OU"));
		n2.newlineText();
		n2.showText("Vydal: " + CertificateInfo.getIssuerFields((X509Certificate)(certificateChain[0])).getField("CN"));
		n2.newlineText();
		final Calendar dt = signatureAppearance.getSignDate();
		n2.showText(String.format("Datum a čas:  %02d.%02d.%d %02d:%02d:%02d", dt.get(dt.DAY_OF_MONTH), (dt.get(dt.MONTH) + 1), dt.get(dt.YEAR), dt.get(dt.HOUR_OF_DAY), dt.get(dt.MINUTE), dt.get(dt.SECOND)));
		n2.endText();
	    }
	    MakeSignature.signDetached(signatureAppearance, digest, signature, certificateChain, null, null, null, 4096, MakeSignature.CryptoStandard.CMS);
	    stamper.close();
	    fileOutputStream.close();
    	} catch (Exception exception) {
	    System.err.println("Error processing files, exception: " + exception);
	    log.fine("Error processing files, exception: " + exception);
	    System.exit(1);
	}
	
	log.fine("Application terminated normally");
    }
}
