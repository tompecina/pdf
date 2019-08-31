/* PdfToXml.java
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

package cz.pecina.pdf.pdftoxml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;

import javax.xml.XMLConstants;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

import org.apache.pdfbox.pdmodel.PDDocument;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.Text;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


/**
 * Convert PDF file to XML.
 *
 * @author Tomáš Pecina
 * @version 1.0.0
 */
public class PdfToXml {

  // static logger
  private static final Logger log = Logger.getLogger(PdfToXml.class.getName());

  // XML file version
  private static final String PDF_XML_FILE_VERSION = "1.0";

  // XML namespace
  private static final String NAMESPACE = "http://www.pecina.cz";

  // XML Schema location prefix
  private static final String XSD_PREFIX = "http://www.pecina.cz/xsd/";

  // options
  private static final Options options = new Options();

  static {
    options.addOption(
        Option.builder("?")
        .longOpt("help")
        .desc("show usage information")
        .build());
    options.addOption(
        Option.builder("V")
        .longOpt("version")
        .desc("show version")
        .build());
    options.addOption(
        Option.builder("d")
        .longOpt("decompress")
        .desc("decompress streams")
        .build());
  }
    
  // for description see Object
  @Override
  public String toString() {
    return "PdfToXml";
  }

  // guess type of byte array and create proper representation
  private static Element createDataElement(final byte[] data, final String elementName) {
    final Element element = new Element(elementName, NAMESPACE);

    element.setAttribute("format", "text");
    try {
      final String string = new String(data, "ASCII");
      for (char ch: string.toCharArray()) {
        final int ord = (int)ch;
        if ((ord >= 0x7f) || ((ord < 0x20) && (ord != 0x09) && (ord != 0x0a) && (ord != 0x0d))) {
          throw new Exception();
        }
      }
      element.addContent(string);
      return element;
    } catch (Exception expected) { }

    if ((data.length > 2) && (data[0] == (0xfe - 0x100)) && (data[1] == (0xff - 0x100))) {
      try {
        element.addContent(new String(data, "UTF-16"));
        return element;
      } catch (Exception expected) { }
    }
  
    if ((data.length > 3) && (data[0] == (0xef - 0x100)) && (data[1] == (0xbb - 0x100)) && (data[2] == (0xbf - 0x100))) {
      try {
        element.addContent(new String(data, "UTF-8"));
        return element;
      } catch (Exception expected) { }
    }
  
    try {
      int esc = 0;
      int codePoint = 0;
      for (byte b: data) {
        if (esc > 0) {
          if ((b & 0xc0) != 0x80) {
            throw new Exception();
          }
          codePoint = (codePoint << 6) | (b & 0x3f);
          esc--;
        } else {
          if ((b & 0x80) == 0x00) {
            codePoint = (b & 0x7f);
          } else if ((b & 0xe0) == 0xc0) {
            codePoint = (b & 0x1f);
            esc = 1;
          } else if ((b & 0xf0) == 0xe0) {
            codePoint = (b & 0x0f);
            esc = 2;
          } else if ((b & 0xf8) == 0xf0) {
            codePoint = (b & 0x07);
            esc = 3;
          } else if ((b & 0xfc) == 0xf8) {
            codePoint = (b & 0x03);
            esc = 4;
          } else if ((b & 0xfe) == 0xfc) {
            codePoint = (b & 0x01);
            esc = 5;
          } else {
            throw new Exception();
          }
          if ((esc == 0) && (codePoint < 0x20) && (codePoint != 0x09) && (codePoint != 0x0a) && (codePoint != 0x0d)) {
            throw new Exception();
          }
        }
      }
      final String string = new String(data, "UTF-8");
      for (char ch: string.toCharArray()) {
        final int ord = (int)ch;
        if ((ord < 0x20) && (ord != 0x09) && (ord != 0x0a) && (ord != 0x0d)) {
          throw new Exception();
        }
      }
      element.addContent(string);
      return element;
    } catch (Exception expected) { }
    
    try {
      final String string = new String(data, "ISO-8859-1");
      for (char ch: string.toCharArray()) {
        final int ord = (int)ch;
        if ((ord < 0x20) && (ord != 0x09) && (ord != 0x0a) && (ord != 0x0d)) {
          throw new Exception();
        }
      }
      element.addContent(string);
      return element;
    } catch (Exception expected) { }
  
    element.setAttribute("format", "hex");
    for (byte b: data) {
      element.addContent(String.format("%02X", b));
    }
    return element;
  }

  private static Element createDataElement(final InputStream inputStream, final String elementName) throws IOException {
    final List<Byte> buffer = new ArrayList<>();
    while (true) {
      final int b = inputStream.read();
      if (b == -1) {
        break;
      }
      buffer.add((byte)b);
    }
    final byte[] bytes = new byte[buffer.size()];
    int i = 0;
    for (Object b: buffer.toArray()) {
      bytes[i++] = (Byte)b;
    }
    return createDataElement(bytes, elementName);
  }

  // PDF object element factory method
  private static Content createObjectElement(final COSBase object, final boolean decompress) throws IOException {
    if (object instanceof COSArray) {
      final Element arrayElement = new Element("array", NAMESPACE);
      for (COSBase item: (COSArray)object) {
        arrayElement.addContent(createObjectElement(item, decompress));
      }
      return arrayElement;
    }
    if (object instanceof COSBoolean) {
      final Element booleanElement = new Element("boolean", NAMESPACE);
      if (((COSBoolean)object).getValue()) {
        booleanElement.addContent("true");
      } else {
        booleanElement.addContent("false");
      }
      return booleanElement;
    }
    if (object instanceof COSStream) {
      final Element streamElement = new Element("stream", NAMESPACE);
      streamElement.addContent(createObjectElement(new COSDictionary((COSDictionary)object), decompress));
      if (decompress) {
        streamElement.addContent(createDataElement(((COSStream)object).createInputStream(), "data"));
      } else {
        streamElement.addContent(createDataElement(((COSStream)object).createRawInputStream(), "raw"));
      }
      return streamElement;
    }
    if (object instanceof COSDictionary) {
      final Element dictionaryElement = new Element("dictionary", NAMESPACE);
      for (COSName key: ((COSDictionary)object).keySet()) {
        final Element entryElement = new Element("entry", NAMESPACE);
        final Element keyElement = new Element("key", NAMESPACE);
        keyElement.addContent(key.getName());
        entryElement.addContent(keyElement);
        final Element valueElement = new Element("value", NAMESPACE);
        valueElement.addContent(createObjectElement(((COSDictionary)object).getItem(key), decompress));
        entryElement.addContent(valueElement);
        dictionaryElement.addContent(entryElement);
      }
      return dictionaryElement;
    }
    if (object instanceof COSObject) {
      final Element indirectReferenceElement = new Element("indirect-reference", NAMESPACE);
      final Element numberElement = new Element("number", NAMESPACE);
      numberElement.addContent("" + ((COSObject)object).getObjectNumber());
      indirectReferenceElement.addContent(numberElement);
      final Element generationElement = new Element("generation", NAMESPACE);
      generationElement.addContent("" + ((COSObject)object).getGenerationNumber());
      indirectReferenceElement.addContent(generationElement);
      return indirectReferenceElement;
    }
    if (object instanceof COSName) {
      final Element nameElement = new Element("name", NAMESPACE);
      nameElement.addContent(((COSName)object).getName());
      return nameElement;
    }
    if (object instanceof COSNull) {
      return new Element("null", NAMESPACE);
    }
    if (object instanceof COSFloat) {
      final Element numberElement = new Element("number", NAMESPACE);
      numberElement.addContent("" + ((COSFloat)object).intValue());
      return numberElement;
    }
    if (object instanceof COSInteger) {
      final Element numberElement = new Element("number", NAMESPACE);
      numberElement.addContent("" + ((COSInteger)object).intValue());
      return numberElement;
    }
    if (object instanceof COSString) {
      return createDataElement(((COSString)object).getBytes(), "string");
    }
    System.err.println("Bad PDF object type");
    log.fine("Bad PDF object type");
    System.exit(1);
    return null;
  }

  /**
   * Prints usage information.
   *
   */
  private static void usage() {
    final HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp("pdftoxml [options] infile [outfile]", options);
    System.out.println("\nThe source code is available from <https://github.com/tompecina/pdf>.");
  }

  /**
   * Main method.
   *
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
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
      System.err.println("1.0.0");
      log.fine("Application terminated normally");
      System.exit(0);
    }

    final boolean decompress = line.hasOption("d");

    final String[] fileNames = line.getArgs();
    PrintStream outPrintStream = null;
    try {
      if (fileNames.length == 1) {
        outPrintStream = System.out;
      } else if (fileNames.length == 2) {
        outPrintStream = new PrintStream(fileNames[1]);
      } else {
        usage();
        log.fine("Too few or too many filenames");
        System.exit(1);
      } 
    } catch (Exception exception) {
      System.err.println("Error opening files, exception: " + exception);
      log.fine("Error opening files, exception: " + exception);
      System.exit(1);
    }
    final String inFileName = fileNames[0];

    try {
      final PDDocument pdDocument = PDDocument.load(new File(inFileName));
      final COSDocument cosDocument = pdDocument.getDocument();
      
      final Element pdfElement = new Element("pdf", Namespace.getNamespace(NAMESPACE));
      final Namespace xsiNamespace = Namespace.getNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
      pdfElement.addNamespaceDeclaration(xsiNamespace);
      pdfElement.setAttribute(
          "schemaLocation", NAMESPACE + " " + XSD_PREFIX + "pdf-" + PDF_XML_FILE_VERSION + ".xsd", xsiNamespace);
      pdfElement.setAttribute("version", PDF_XML_FILE_VERSION);

      final Element versionElement = new Element("version", NAMESPACE);
      versionElement.addContent("" + cosDocument.getVersion());
      pdfElement.addContent(versionElement);

      final COSDictionary trailer = cosDocument.getTrailer();
      final Element trailerElement = new Element("trailer", NAMESPACE);
      trailerElement.addContent(createObjectElement(trailer, decompress));
      pdfElement.addContent(trailerElement);

      final Element contentElement = new Element("content", NAMESPACE);
      for (COSObject object: cosDocument.getObjects()) {
        final Element objectElement = new Element("object", NAMESPACE);
        objectElement.setAttribute("number", "" + object.getObjectNumber());
        objectElement.setAttribute("generation", "" + object.getGenerationNumber());
        objectElement.addContent(createObjectElement(object.getObject(), decompress));
        contentElement.addContent(objectElement);
      }
      pdfElement.addContent(contentElement);
      
      new XMLOutputter(Format.getRawFormat()).output(new Document(pdfElement), outPrintStream);
    } catch (Exception exception) {
      System.err.println("Error processing files, exception: " + exception);
      log.fine("Error processing files, exception: " + exception);
      System.exit(1);
    }

    log.fine("Application terminated normally");
  }
}
