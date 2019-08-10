/* AddPdfMeta.java
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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import java.util.logging.Logger;
import cz.pecina.pdf.addpdfmeta.ModifiedPdfStamper;

/**
 * Adds metadata to existing PDF file.
 *
 * @author @AUTHOR@
 * @version @VERSION@
 */
public class AddPdfMeta {

    // static logger
    private static final Logger log = Logger.getLogger(AddPdfMeta.class.getName());

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
    }
    
    // for description see Object
    @Override
    public String toString() {
	return "AddPdfMeta";
    }

    /**
     *
     * Prints usage information.
     *
     */
    private static void usage() {
	final HelpFormatter helpFormatter = new HelpFormatter();
	helpFormatter.printHelp("addpdfmeta [options] infile metadatafile [outfile]", options);
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

	final String[] fileNames = line.getArgs();
	
	if ((fileNames.length < 2) || (fileNames.length > 3)) {
	    usage();
	    log.fine("Error in parameters");
	    System.exit(1);
	}

	String inFileName = fileNames[0];
	final String metadataFileName = fileNames[1];
	String outFileName = null;
	final String tempFileName = "/tmp/addpdfmeta.pdf";
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
	    final OutputStream fileOutputStream = new FileOutputStream(outFileName);
	    final PdfStamper stamper = new ModifiedPdfStamper(reader, fileOutputStream, new FileInputStream(new File(metadataFileName)));
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
