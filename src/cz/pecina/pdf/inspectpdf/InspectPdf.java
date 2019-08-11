/* InspectPdf.java
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

package cz.pecina.pdf.inspectpdf;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.security.Security;
import java.security.cert.X509Certificate;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.security.CertificateInfo;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.SignaturePermissions;
import com.itextpdf.text.pdf.security.CertificateUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.cert.X509CertificateHolder;
import java.util.logging.Logger;

/**
 * Inspect PDF file.
 *
 * @author @AUTHOR@
 * @version @VERSION@
 */
public class InspectPdf {

    // static logger
    private static final Logger log = Logger.getLogger(InspectPdf.class.getName());

    // options
    private static final Options options = new Options();
    static {
	options.addOption(
	    Option.builder("?")
	         .longOpt("help")
	         .desc("show usage information")
	         .build()
	    );
	options.addOption(
	    Option.builder("V")
	         .longOpt("version")
	         .desc("show version")
	         .build()
	    );
	options.addOption(
	    Option.builder("m")
	         .longOpt("metadata")
	         .desc("print metadata")
	         .build()
	    );
	options.addOption(
	    Option.builder("o")
	         .longOpt("objects")
	         .desc("list PDF objects")
	         .build()
	    );
    }
    
    // for description see Object
    @Override
    public String toString() {
	return "InspectPdf";
    }

    // return "yes" or "no"
    private static String yn(final boolean value) {
	if (value) {
	    return "yes";
	} else {
	    return "no";
	}
    }

    // print key:value pair, descending dictionary hierarchy tree
    private static void printPair(String prefix, Object idx, PdfObject object) {
	System.out.println(prefix + idx + ": " + object);
	if (object.isDictionary()) {
	    for (PdfName subKey: ((PdfDictionary)object).getKeys()) {
		printPair(prefix + "  ", subKey, ((PdfDictionary)object).get(subKey)); 
	    }
	} else if (object.isStream()) {
	    for (PdfName subKey: ((PdfDictionary)object).getKeys()) {
		printPair(prefix + "  ", subKey, ((PdfDictionary)object).get(subKey)); 
	    }
	}
    }

    /**
     *
     * Prints usage information.
     *
     */
    private static void usage() {
	final HelpFormatter helpFormatter = new HelpFormatter();
	helpFormatter.printHelp("inspectpdf [options] infile", options);
	System.out.println("\nThe source code is available from <https://github.com/tompecina/pdf>.");
    }

    /**
     * Main method.
     *
     * @param args command-line arguments
     */
    public static void main(final String args[]) {
	log.fine("Application started");

	if ((args == null) || (args.length < 1)) {
	    usage();
	    log.fine("Error in parameters");
	    System.exit(1);
	}

	final CommandLineParser parser = new DefaultParser();
	CommandLine line = null;
	try {
	    line = parser.parse(options, args);
	} catch (Exception exception) {
	    usage();
	    log.fine("Failed to parse the command line, exception: " + exception);
	    System.exit(1);
	}

	if (line.hasOption("?")) {
	    usage();
	    log.fine("Application terminated normally");
	    System.exit(0);
	}
	
	if (line.hasOption("V")) {
	    System.err.println("@VERSION@");
	    log.fine("Application terminated normally");
	    System.exit(0);
	}

	final boolean listObjects = line.hasOption("o");
	final boolean printMetadata = line.hasOption("m");

	BouncyCastleProvider provider = null;
	try {
	    provider = new BouncyCastleProvider();
	    Security.addProvider(provider);
	} catch (Exception exception) {
	    System.err.println("Error setting up cryptography, exception: " + exception);
	    log.fine("Error setting up cryptography, exception: " + exception);
	    System.exit(1);
	}

	try {
	    final PdfReader reader = new PdfReader((line.getArgs())[0]);

	    System.out.println("PDF version: 1." + reader.getPdfVersion());
	    System.out.println();

	    System.out.println("Number of pages: " + reader.getNumberOfPages());
	    System.out.println();

	    final PdfDictionary trailer = reader.getTrailer();
	    System.out.println("Trailer:");
	    for (PdfName key: trailer.getKeys()) {
		if (key.equals(new PdfName("ID"))) {
		    System.out.print("  " + key + ": [");
		    boolean first = true;
		    for (PdfObject part: (PdfArray)(trailer.get(key))) {
			if (first) {
			    first = false;
			} else {
			    System.out.print(" ");
			}
			System.out.print("<");
			for (byte ch: part.toString().getBytes()) {
			    System.out.print(String.format("%02X", ch));
			}
			System.out.print(">");
		    }
		    System.out.println("]");
		} else {
		    printPair("  ", key, trailer.get(key));
		}
	    }
	    System.out.println();

	    final HashMap<String,String> info = reader.getInfo();
	    System.out.println("Info:");
	    for (String key: info.keySet()) {
		System.out.println("  " + key + ": " + info.get(key));
	    }
	    System.out.println();
	    
	    final PdfDictionary catalog = reader.getCatalog();
	    System.out.println("Catalog:");
	    for (PdfName key: catalog.getKeys()) {
		printPair("  ", key, catalog.get(key));
	    }
	    System.out.println();

	    if (printMetadata) {
		final byte[] metadata = reader.getMetadata();
		System.out.println("Metadata:");
		if (metadata == null) {
		    System.out.println("  None");
		} else {
		    System.out.println(new String(metadata, "utf-8"));
		}
		System.out.println();
	    }
	    
	    final AcroFields acroFields = reader.getAcroFields();
	    final ArrayList<String> names = acroFields.getSignatureNames();
	    SignaturePermissions permissions = null;
	    for (String name: names) {
		System.out.println(String.format("Signature '%s':", name));
		System.out.println("  Signature covers whole document: " + yn(acroFields.signatureCoversWholeDocument(name)));
		System.out.println("  Document revision: " + acroFields.getRevision(name) + " of " + acroFields.getTotalRevisions());
		final PdfPKCS7 pkcs7 = acroFields.verifySignature(name);
		System.out.println("  Integrity check: " + yn(pkcs7.verify()));
		final List<AcroFields.FieldPosition> fieldPositions = acroFields.getFieldPositions(name);
		if (!fieldPositions.isEmpty()) {
		    final AcroFields.FieldPosition fieldPosition = fieldPositions.get(0);
		    final Rectangle position = fieldPosition.position;
		    if ((position.getWidth() == 0f) || (position.getHeight() == 0f)) {
			System.out.println("  Invisible signature");
		    } else {
			System.out.println(String.format("  Field on page %d; llx: %f, lly: %f, urx: %f; ury: %f", fieldPosition.page, position.getLeft(), position.getBottom(), position.getRight(), position.getTop()));
		    }
		}
		System.out.println("  Digest algorithm: " + pkcs7.getHashAlgorithm());
		System.out.println("  Encryption algorithm: " + pkcs7.getEncryptionAlgorithm());
		System.out.println("  Filter subtype: " + pkcs7.getFilterSubtype());
		final X509Certificate certificate = pkcs7.getSigningCertificate();
		System.out.println("  Name of the signer: " + CertificateInfo.getSubjectFields(certificate).getField("CN"));
		if (pkcs7.getSignName() != null) {
		    System.out.println("  Alternative name of the signer :" + pkcs7.getSignName());
		}
		System.out.println("  Signed on: " + pkcs7.getSignDate().getTime());
		if (pkcs7.getTimeStampDate() != null) {
		    System.out.println("  TimeStamp: " + pkcs7.getTimeStampDate().getTime());
		    final TimeStampToken timeStamp = pkcs7.getTimeStampToken();
		    System.out.println("  TimeStamp service: " + timeStamp.getTimeStampInfo().getTsa());
		    System.out.println("  TimeStamp verified: " + yn(pkcs7.verifyTimestampImprint()));
		    final X509CertificateHolder holder = (X509CertificateHolder)(timeStamp.getCertificates().getMatches(null).iterator().next());
		    System.out.println("  TimeStamp valid from: " + holder.getNotBefore());
		    System.out.println("  TimeStamp valid to: " + holder.getNotAfter());
		}
		System.out.println("  Location: " + pkcs7.getLocation());
		System.out.println("  Reason: " + pkcs7.getReason());
		final PdfDictionary signatureDictionary = acroFields.getSignatureDictionary(name);
		final PdfString contact = signatureDictionary.getAsString(PdfName.CONTACTINFO);
		System.out.println("  Contact info: " + contact);
		permissions = new SignaturePermissions(signatureDictionary, permissions);
		String signatureType;
		if (permissions.isCertification()) {
		    signatureType = "certification";
		} else {
		    signatureType = "approval";
		}
		System.out.println("  Signature type: " + signatureType);
		System.out.println("  Filling out fields allowed: " + yn(permissions.isFillInAllowed()));
		System.out.println("  Adding annotations allowed: " + yn(permissions.isAnnotationsAllowed()));
		for (SignaturePermissions.FieldLock fieldLock: permissions.getFieldLocks()) {
		    System.out.println("  Lock: " + fieldLock.toString());
		}
		for (X509Certificate chainCertificate: (X509Certificate[])pkcs7.getSignCertificateChain()) {
		    System.out.println();
		    System.out.println("  Issuer: " + chainCertificate.getIssuerX500Principal());
		    System.out.println("  Subject: " + chainCertificate.getSubjectX500Principal());
		    System.out.println("  Valid from: " + chainCertificate.getNotBefore());
		    System.out.println("  Valid to: " + chainCertificate.getNotAfter());
		    System.out.println("  CRL: " + CertificateUtil.getCRLURL(chainCertificate));
		    System.out.println();
		}
	    }

	    if (listObjects) {
		final int xrefSize = reader.getXrefSize();
		System.out.println("Number of objects: " + xrefSize);
		for (int i = 0; i < xrefSize; i++) {
		    try {
			final PdfObject pdfObject = reader.getPdfObject(i);
			if (pdfObject != null) {
			    printPair("  ", i, pdfObject);
			}
		    } catch (Exception e) { }			
		}
		System.out.println();
	    }
    	} catch (Exception exception) {
	    System.err.println("Error processing files, exception: " + exception);
	    log.fine("Error processing files, exception: " + exception);
	    System.exit(1);
	}

	log.fine("Application terminated normally");
    }
}
