/* AddPdfStream.java
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

package cz.pecina.pdf.addpdfstream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import java.util.Map;
import java.util.HashMap;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfDictionary;
import java.util.logging.Logger;

/**
 * Adds stream to existing PDF file.
 *
 * @author @AUTHOR@
 * @version @VERSION@
 */
public class AddPdfStream {

    // static logger
    private static final Logger log = Logger.getLogger(AddPdfStream.class.getName());

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
	    Option.builder("t")
	         .longOpt("type")
	         .hasArg()
    	         .argName("TYPE")
	         .desc("type of the stream in the catalog (default: Data)")
	         .build()
	    );
	options.addOption(
	    Option.builder("d")
	         .longOpt("dictionary")
	         .hasArg()
    	         .argName("DATA")
	         .desc("data copied to the stream dictionary, entered as key-value pairs: KEY:VALUE,KEY:VALUE...")
	         .build()
	    );
	options.addOption(
	    Option.builder("c")
	         .longOpt("compress")
	         .desc("compress the stream")
	         .build()
	    );
	options.addOption(
	    Option.builder("l")
	         .longOpt("level")
	         .hasArg()
    	         .type(Number.class)
    	         .argName("LEVEL")
	         .desc("compression level (0-9)")
	         .build()
	    );
    }
    
    // for description see Object
    @Override
    public String toString() {
	return "AddPdfStream";
    }

    /**
     *
     * Prints usage information.
     *
     */
    private static void usage() {
	final HelpFormatter helpFormatter = new HelpFormatter();
	helpFormatter.printHelp("addpdfstream [options] infile streamfile [outfile]", options);
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

	String streamType = "Data";
	if (line.hasOption("t")) {
	    streamType = line.getOptionValue("t");
	}

	final Map<String,String> pairs = new HashMap<>();
	if (line.hasOption("d")) {
	    try {
		for (String pairString: line.getOptionValue("d").split(",")) {
		    final String[] pair = pairString.split(":");
		    pairs.put(pair[0], pair[1]);
		}
	    } catch (Exception exception) {
		System.err.println("Error in dictionary pairs, exception: " + exception);
		log.fine("FFailed to parse dictionary pairs, exception: " + exception);
		System.exit(1);
	    }
	}
	
	final boolean compress = (line.hasOption("c"));
	int compressionLevel = PdfStream.DEFAULT_COMPRESSION;
	if (line.hasOption("l")) {
	    try {
		compressionLevel = ((Number)line.getParsedOptionValue("l")).intValue();
	    } catch (Exception exception) {
		System.err.println("Error in compression level, exception: " + exception);
		log.fine("Failed to parse compression level, exception: " + exception);
		System.exit(1);
	    }
	    if ((compressionLevel < 0) || (compressionLevel > 9)) {
		System.err.println("Compression level must be 0-9");
		log.fine("Compression level out of range");
		System.exit(1);
	    }
	}
	if (!compress) {
	    compressionLevel = PdfStream.NO_COMPRESSION;
	}
	
	final String[] fileNames = line.getArgs();
	
	if ((fileNames.length < 2) || (fileNames.length > 3)) {
	    usage();
	    log.fine("Error in parameters");
	    System.exit(1);
	}

	String inFileName = fileNames[0];
	final String streamDataFileName = fileNames[1];
	String outFileName = null;
	final String tempFileName = "/tmp/addpdfstream.pdf";
	try {
	    if (fileNames.length == 2) {
		Files.copy(Paths.get(inFileName), Paths.get(tempFileName), StandardCopyOption.REPLACE_EXISTING);
		outFileName = inFileName;
		inFileName = tempFileName;
	    } else {
		outFileName = fileNames[2];
	    }
	} catch (Exception exception) {
	    System.err.println("Error opening files, exception: " + exception);
	    log.fine("Error opening files, exception: " + exception);
	    System.exit(1);
	}

	try {
	    final PdfReader reader = new PdfReader(inFileName);
	    final ByteArrayOutputStream streamData = new ByteArrayOutputStream();
	    final InputStream streamDataInputStream = new FileInputStream(streamDataFileName);
	    while (true) {
	    	int d = streamDataInputStream.read();
	    	if (d < 0)
	    	    break;
	    	streamData.write(d);
	    }
	    final PdfStream stream = new PdfStream(streamData.toByteArray());
	    for (String key: pairs.keySet()) {
		stream.put(new PdfName(key), new PdfName(pairs.get(key)));
	    }
	    if (compress) {
		stream.flateCompress(compressionLevel);
	    }
	    final PdfName streamPdfName = new PdfName(streamType);
	    final PdfDictionary catalog = reader.getCatalog();
	    if (catalog.contains(streamPdfName)) {
		System.out.println("Removing");
		catalog.remove(streamPdfName);
	    }
	    catalog.put(streamPdfName, reader.addPdfObject(stream));
	    final OutputStream fileOutputStream = new FileOutputStream(outFileName);
	    final PdfStamper stamper = new PdfStamper(reader, fileOutputStream);
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
