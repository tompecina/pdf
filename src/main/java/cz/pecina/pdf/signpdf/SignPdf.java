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

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.element.Image;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.CertificateInfo;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.SignatureUtil;
import com.itextpdf.svg.converter.SvgConverter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.logging.Logger;
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

  // estimated signature size
  private static final int SIGN_SIZE = 4096;

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
  public static void main(final String[] args) {
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
      key = (PrivateKey) keyStore.getKey(alias, parameters.getPassword());
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
      if (
          (parameters.getSignatureFieldName() != null)
          && !(new SignatureUtil(signer.getDocument())).getBlankSignatureNames()
          .contains(parameters.getSignatureFieldName())) {
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
      signer.setSignatureEvent(
          new SignatureEvent(parameters.getReason(), parameters.getLocation(), parameters.getContact()));
      if (parameters.getSignatureFieldName() != null) {
        signer.setFieldName(parameters.getSignatureFieldName());

        final PdfFormXObject n0 = signatureAppearance.getLayer0();
        final Rectangle bbox0 = n0.getBBox().toRectangle();
        final PdfCanvas canvas0 = new PdfCanvas(n0, signer.getDocument());
        canvas0.setFillColor(ColorConstants.WHITE);
        canvas0.rectangle(bbox0);
        canvas0.fill();
        canvas0.release();

        final PdfFormXObject n2 = signatureAppearance.getLayer2();
        final Rectangle bbox2 = n2.getBBox().toRectangle();
        final float fieldLeft = bbox2.getLeft();
        final float fieldBottom = bbox2.getBottom();
        final float fieldWidth = bbox2.getWidth();
        final float fieldHeight = bbox2.getHeight();
        final PdfCanvas canvas2 = new PdfCanvas(n2, signer.getDocument());

        final String imageFilename = parameters.getImageFilename();
        InputStream imageStream = null;
        boolean svg = false;
        if (imageFilename == null) {
          imageStream = SignPdf.class.getResourceAsStream(
              "graphics/seal" + ((parameters.getCertificationLevel() > 0) ? "cer" : "appr") + ".png");
        } else {
          svg = imageFilename.toLowerCase().endsWith(".svg");
          imageStream = new FileInputStream(imageFilename);
        }
        final Image image =
            svg
              ? SvgConverter.convertToImage(imageStream, signer.getDocument())
              : new Image(ImageDataFactory.create(imageStream.readAllBytes()));
        final float imageOrigWidth = image.getImageWidth();
        final float imageOrigHeight = image.getImageHeight();
        float imageWidth = parameters.getImageWidth();
        float imageHeight = parameters.getImageHeight();
        float imageScaleX = 1f;
        float imageScaleY = 1f;
        if ((imageWidth > 0f) && (imageHeight > 0f)) {
          imageScaleX = imageWidth / imageOrigWidth;
          imageScaleY = imageHeight / imageOrigHeight;
        } else if (imageWidth > 0f) {
          imageScaleX = imageScaleY = imageWidth / imageOrigWidth;
          imageHeight = imageOrigHeight * imageScaleY;
        } else if (imageHeight > 0f) {
          imageScaleX = imageScaleY = imageHeight / imageOrigHeight;
          imageWidth = imageOrigWidth * imageScaleX;
        } else {
          imageWidth = imageOrigWidth;
          imageHeight = imageOrigHeight;
        }
        if (!svg) {
          imageScaleX = imageWidth;
          imageScaleY = imageHeight;
        }
        float imageX = parameters.getImageX();
        if (parameters.getImageXDir()) {
          imageX = fieldWidth - imageWidth - imageX;
        }
        float imageY = parameters.getImageY();
        if (parameters.getImageYDir()) {
          imageY = fieldHeight - imageHeight - imageY;
        }
        imageX += fieldLeft;
        imageY += fieldBottom;
        canvas2.addXObject(image.getXObject(), new Rectangle(imageX, imageY, imageScaleX, imageScaleY));

        float textX = parameters.getTextX();
        if (parameters.getTextXDir()) {
          textX += fieldWidth - textX;
        }
        float textY = parameters.getTextY();
        if (parameters.getTextYDir()) {
          textY = fieldHeight - textY;
        }
        textX += fieldLeft;
        textY += fieldBottom;
        final PdfFont brm = PdfFontFactory.createFont(parameters.getRegularFontFilename(), PdfEncodings.IDENTITY_H, true);
        final PdfFont bbf = PdfFontFactory.createFont(parameters.getBoldFontFilename(), PdfEncodings.IDENTITY_H, true);
        final float fontSize = parameters.getFontSize();
        final float leading = parameters.getLeading();
        canvas2.setFillColor(parameters.getFontColor());
        canvas2.beginText();
        canvas2.setTextMatrix(textX, textY);
        canvas2.setLeading(leading);
        canvas2.setFontAndSize(brm, fontSize);
        canvas2.showText("Digitálně podepsal: ");
        canvas2.setFontAndSize(bbf, fontSize);
        canvas2.showText(CertificateInfo.getSubjectFields((X509Certificate) certificateChain[0]).getField("CN"));
        canvas2.setFontAndSize(brm, fontSize);
        canvas2.newlineText();
        canvas2.showText(
            "Certifikát: " + CertificateInfo.getSubjectFields((X509Certificate) certificateChain[0]).getField("OU"));
        canvas2.newlineText();
        canvas2.showText(
            "Vydal: " + CertificateInfo.getIssuerFields((X509Certificate) certificateChain[0]).getField("CN"));
        canvas2.newlineText();
        final Calendar dt = signer.getSignDate();
        canvas2.showText(String.format(
            "Datum a čas:  %02d.%02d.%d %02d:%02d:%02d",
            dt.get(dt.DAY_OF_MONTH), (dt.get(dt.MONTH) + 1), dt.get(dt.YEAR),
            dt.get(dt.HOUR_OF_DAY), dt.get(dt.MINUTE), dt.get(dt.SECOND)));
        canvas2.endText();
        canvas2.release();
      }
      signer.signDetached(digest, signature, certificateChain, null, null, null, SIGN_SIZE, PdfSigner.CryptoStandard.CMS);
      reader.close();
    } catch (Exception exception) {
      System.err.println("Error processing files, exception: " + exception);
      log.fine("Error processing files, exception: " + exception);
      System.exit(1);
    }

    log.fine("Application terminated normally");
  }
}
