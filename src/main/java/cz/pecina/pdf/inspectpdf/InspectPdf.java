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

import com.itextpdf.forms.PdfAcroForm;

import com.itextpdf.forms.fields.PdfFormField;

import com.itextpdf.io.IOException;

import com.itextpdf.io.font.PdfEncodings;

import com.itextpdf.kernel.PdfException;

import com.itextpdf.kernel.geom.Rectangle;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfCatalog;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;

import com.itextpdf.kernel.pdf.annot.PdfWidgetAnnotation;

import com.itextpdf.signatures.CertificateInfo;
import com.itextpdf.signatures.CertificateUtil;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignaturePermissions;
import com.itextpdf.signatures.SignatureUtil;

import java.security.GeneralSecurityException;
import java.security.Security;

import java.security.cert.X509Certificate;

import java.util.List;
import java.util.Set;

import java.util.logging.Logger;

import org.bouncycastle.cert.X509CertificateHolder;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.bouncycastle.tsp.TimeStampToken;


/**
 * Inspect PDF file.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class InspectPdf {

  // static logger
  private static final Logger log = Logger.getLogger(InspectPdf.class.getName());

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

  // charset guess parameters
  private static final int IMP = 0;  // impossible
  private static final int REG = 1;  // regular
  private static final int LLK = 2;  // less likely
  private static final int UNL = 10;  // unlikely
  private static final float UNL_LIM = 0.2f;
  private static final char[] PDF_DOC_ENCODING_BYTE_LIKELYHOOD = {
    IMP, IMP, IMP, IMP,  IMP, IMP, IMP, IMP,  IMP, IMP, IMP, IMP,  IMP, IMP, IMP, IMP,  // 00
    IMP, IMP, IMP, IMP,  IMP, IMP, IMP, IMP,  UNL, UNL, UNL, UNL,  UNL, UNL, UNL, UNL,  // 10
    REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  // 20
    REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  // 30
    REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  // 40
    REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  // 50
    LLK, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  // 60
    REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, REG, REG,  REG, REG, LLK, IMP,  // 70
    UNL, UNL, UNL, LLK,  UNL, LLK, UNL, UNL,  LLK, LLK, UNL, UNL,  LLK, LLK, LLK, LLK,  // 80
    LLK, LLK, LLK, UNL,  UNL, LLK, LLK, LLK,  LLK, LLK, LLK, LLK,  LLK, LLK, LLK, IMP,  // 90
    LLK, LLK, LLK, LLK,  LLK, LLK, UNL, LLK,  UNL, LLK, LLK, LLK,  UNL, IMP, LLK, UNL,  // a0
    LLK, UNL, UNL, UNL,  UNL, UNL, UNL, UNL,  UNL, UNL, UNL, LLK,  UNL, UNL, UNL, LLK,  // b0
    LLK, LLK, LLK, LLK,  LLK, LLK, LLK, LLK,  LLK, LLK, LLK, LLK,  LLK, LLK, LLK, LLK,  // c0
    LLK, LLK, LLK, LLK,  LLK, LLK, LLK, UNL,  LLK, LLK, LLK, LLK,  LLK, LLK, LLK, LLK,  // d0
    LLK, LLK, LLK, LLK,  LLK, LLK, LLK, LLK,  LLK, LLK, LLK, LLK,  LLK, LLK, LLK, LLK,  // e0
    LLK, LLK, LLK, LLK,  LLK, LLK, LLK, UNL,  LLK, LLK, LLK, LLK,  LLK, LLK, LLK, LLK   // f0
  };

  // indentation level
  private static final int INDENT = 2;

  /**
   * Converts a PDF object to a string.
   *
   * @param obj the object to be printed
   * @param level the level of embedding (-1 - contracted)
   * @return the string value of obj
   *
   */
  private static String stringify(final PdfObject obj, final int level) {
    if ((level != 0) && obj.isIndirect()) {
      final PdfIndirectReference ref = obj.getIndirectReference();
      return String.format("%d %d R", ref.getObjNumber(), ref.getGenNumber());
    } else if (obj instanceof PdfDictionary) {
      final PdfDictionary dict = (PdfDictionary) obj;
      if (level < 0) {
        final StringBuilder res = new StringBuilder("<< ");
        for (PdfName key : dict.keySet()) {
          res.append(key.toString());
          res.append(' ');
          res.append(stringify(dict.get(key), -1));
          res.append(' ');
        }
        res.append(">>");
        return res.toString();
      } else {
        final String prefix = String.format("%" + ((level + 1) * INDENT) + "s", "");
        final StringBuilder res = new StringBuilder();
        final Set<PdfName> keys = dict.keySet();
        for (PdfName key : keys) {
          final PdfObject sub = dict.get(key);
          res.append(String.format("%n%s%s: %s", prefix, key.getValue(), stringify(sub, level + 1)));
        }
        return res.toString();
      }
    } else if (obj instanceof PdfArray) {
      final StringBuilder res = new StringBuilder("[");
      boolean notFirst = false;
      for (PdfObject sub : (PdfArray) obj) {
        if (notFirst) {
          res.append(' ');
        }
        notFirst = true;
        res.append(stringify(sub, -1));
      }
      res.append(']');
      return res.toString();
    } else if (obj instanceof PdfString) {
      final PdfString str = (PdfString) obj;
      final byte[] bytes = str.getValueBytes();
      final StringBuilder res = new StringBuilder();
      String substr = null;
      if ((bytes.length > 2) && (bytes[0] == (byte) 0xfe) && (bytes[1] == (byte) 0xff)) {
        try {
          substr = PdfEncodings.convertToString(bytes, PdfEncodings.UNICODE_BIG);
        } catch (IOException exception) {
          substr = null;
        }
      } else {
        int score = 0;
        for (byte ch : bytes) {
          final int inc = PDF_DOC_ENCODING_BYTE_LIKELYHOOD[ch & 0xff];
          if (inc == IMP) {
            score = 0;
            break;
          }
          score += inc;
        }
        if ((score > 0) && (((score - bytes.length) <= (UNL_LIM * bytes.length)))) {
          try {
            substr = PdfEncodings.convertToString(bytes, PdfEncodings.PDF_DOC_ENCODING);
          } catch (IOException exception) {
            substr = null;
          }
        }
      }
      if (substr == null) {
        res.append('<');
        for (byte ch : str.getValueBytes()) {
          res.append(String.format("%02X", ch));
        }
        res.append('>');
        return res.toString();
      }
      if (level < 0) {
        res.insert(0, '(');
        for (char ch : substr.toCharArray()) {
          if ((ch == '(') || (ch == ')')) {
            res.append("\\");
          }
          res.append(ch);
        }
        res.append(')');
        return res.toString();
      } else {
        return substr.toString();
      }
    }
    return obj.toString();
  }

  /**
   * Converts a PDF object to a string.
   *
   * @param obj the object to be printed
   * @return the string value of obj
   *
   */
  private static String stringify(final PdfObject obj) {
    return stringify(obj, 0);
  }

  // set up cryptography
  private static BouncyCastleProvider setUpCrypto() {
    BouncyCastleProvider provider = new BouncyCastleProvider();
    try {
      provider = new BouncyCastleProvider();
      Security.addProvider(provider);
    } catch (Exception exception) {
      System.err.println("Error setting up cryptography, exception: " + exception);
      log.fine("Error setting up cryptography, exception: " + exception);
      System.exit(1);
    }
    return provider;
  }

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    log.fine("Application started");

    final Parameters parameters = new Parameters(args);

    final boolean printMetadata = parameters.getPrintMetadata();
    final boolean listObjects = parameters.getListObjects();
    final String inFileName = parameters.getInFileName();

    final BouncyCastleProvider provider = setUpCrypto();

    try {
      final PdfReader reader = new PdfReader(inFileName);
      final PdfDocument pdfDocument = new PdfDocument(reader);

      System.out.println("Version: " + pdfDocument.getPdfVersion());

      System.out.println();
      System.out.println("Number of pages: " + pdfDocument.getNumberOfPages());

      final PdfDictionary trailer = pdfDocument.getTrailer();
      System.out.println();
      System.out.print("Trailer:");
      System.out.println(stringify(trailer));

      System.out.println();
      System.out.print("Info:");
      System.out.println(stringify(trailer.get(PdfName.Info)));
      System.out.println();

      final PdfCatalog catalog = pdfDocument.getCatalog();
      System.out.print("Catalog:");
      System.out.println(stringify(trailer.get(PdfName.Root)));
      System.out.println();

      if (printMetadata) {
        final byte[] metadata = pdfDocument.getXmpMetadata();
        System.out.println("Metadata:");
        if (metadata == null) {
          System.out.println("  None");
        } else {
          System.out.println(new String(metadata, "utf-8"));
        }
        System.out.println();
      }

      final SignatureUtil util = new SignatureUtil(pdfDocument);
      final List<String> names = util.getSignatureNames();
      if (!names.isEmpty()) {
        final PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDocument, false);
        SignaturePermissions permissions = null;
        for (String name : names) {
          System.out.println(String.format("Signature '%s':", name));
          System.out.println("  Signature covers whole document: " + yn(util.signatureCoversWholeDocument(name)));
          System.out.println("  Document revision: " + util.getRevision(name) + " of " + util.getTotalRevisions());
          PdfPKCS7 pkcs7 = util.readSignatureData(name);
          try {
            pkcs7 = util.readSignatureData(name);
            System.out.println("  Integrity check: " + yn(pkcs7.verifySignatureIntegrityAndAuthenticity()));
          } catch (PdfException | GeneralSecurityException exception) {
            System.out.println("  Integrity check: " + yn(false));
            System.out.println();
            continue;
          }
          final PdfFormField field = acroForm.getField(name);
          if (field != null) {
            final List<PdfWidgetAnnotation> widgets = field.getWidgets();
            if (!widgets.isEmpty()) {
              final PdfWidgetAnnotation widget = widgets.get(0);
              final Rectangle position = widget.getRectangle().toRectangle();
              if ((position.getWidth() == 0f) || (position.getHeight() == 0f)) {
                System.out.println("  Invisible signature");
              } else {
                final int page = pdfDocument.getPageNumber(widget.getPage());
                System.out.println(String.format(
                    "  Field on page %d; llx: %f, lly: %f, urx: %f; ury: %f",
                    page, position.getLeft(), position.getBottom(), position.getRight(), position.getTop()));
              }
            } else {
              System.out.println("  Invisible signature (no widget)");
            }
          } else {
            System.out.println("  Invisible signature (no field)");
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
            final X509CertificateHolder holder =
                (X509CertificateHolder) timeStamp.getCertificates().getMatches(null).iterator().next();
            System.out.println("  TimeStamp valid from: " + holder.getNotBefore());
            System.out.println("  TimeStamp valid to: " + holder.getNotAfter());
          }
          System.out.println("  Location: " + pkcs7.getLocation());
          System.out.println("  Reason: " + pkcs7.getReason());
          final PdfDictionary signatureDictionary = util.getSignatureDictionary(name);
          final PdfString contact = signatureDictionary.getAsString(PdfName.ContactInfo);
          System.out.println("  Contact info: " + contact);
          permissions = new SignaturePermissions(signatureDictionary, permissions);
          final String signatureType = (permissions.isCertification() ? "certification" : "approval");
          System.out.println("  Signature type: " + signatureType);
          System.out.println("  Filling out fields allowed: " + yn(permissions.isFillInAllowed()));
          System.out.println("  Adding annotations allowed: " + yn(permissions.isAnnotationsAllowed()));
          for (SignaturePermissions.FieldLock fieldLock : permissions.getFieldLocks()) {
            System.out.println("  Lock: " + fieldLock.toString());
          }
          for (X509Certificate chainCertificate : (X509Certificate[]) pkcs7.getSignCertificateChain()) {
            System.out.println();
            System.out.println("  Issuer: " + chainCertificate.getIssuerX500Principal());
            System.out.println("  Subject: " + chainCertificate.getSubjectX500Principal());
            System.out.println("  Valid from: " + chainCertificate.getNotBefore());
            System.out.println("  Valid to: " + chainCertificate.getNotAfter());
            System.out.println("  CRL: " + CertificateUtil.getCRLURL(chainCertificate));
            System.out.println();
          }
        }
      }

      if (listObjects) {
        final int numObjects = pdfDocument.getNumberOfPdfObjects();
        System.out.println("Number of objects: " + numObjects);
        System.out.println();
        for (int i = 0; i < numObjects; i++) {
          final PdfObject pdfObject = pdfDocument.getPdfObject(i);
          if (pdfObject != null) {
            System.out.println(String.format("%d:%s%n", i, stringify(pdfObject)));
          }
        }
      }

    } catch (Exception exception) {
      System.err.println("Error processing files, exception: " + exception);
      log.fine("Error processing files, exception: " + exception);
      System.exit(1);
    }

    log.fine("Application terminated normally");
  }
}
