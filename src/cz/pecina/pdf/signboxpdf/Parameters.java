/* Parameters.java
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

package cz.pecina.pdf.signboxpdf;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import java.util.logging.Logger;
import java.util.Arrays;

/**
 * Parse command line and extract parameters.
 *
 * @author @AUTHOR@
 * @version @VERSION@
 */
public class Parameters {

    // static logger
    private static final Logger log = Logger.getLogger(Parameters.class.getName());

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
    	    Option.builder("p")
    	         .longOpt("page")
    	         .hasArg()
    	         .type(Number.class)
    	         .argName("PAGE")
    	         .desc("page number (default: 1)")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("f")
    	         .longOpt("field")
    	         .hasArg()
    	         .argName("FIELD")
    	         .desc("signature field name (default: Signature)")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("w")
    	         .longOpt("width")
    	         .hasArg()
    	         .type(Float.class)
    	         .argName("WIDTH")
    	         .desc("signature field width (default: 180)")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("h")
    	         .longOpt("height")
    	         .hasArg()
    	         .type(Float.class)
    	         .argName("HEIGHT")
    	         .desc("signature field height (default: 36)")
    	         .build()
    	    );
    }
    
    // for description see Object
    @Override
    public String toString() {
	return "Parameters";
    }

    /**
     *
     * Prints usage information.
     *
     */
    public void usage() {
	final HelpFormatter helpFormatter = new HelpFormatter();
	helpFormatter.printHelp("signboxpdf [options] x y infile [outfile]", options);
        System.out.println("\nNotes:");
        System.out.println("  - Negative values of x/y mean offset from the right/top page margin");
	System.out.println("\nThe source code is available from <https://github.com/tompecina/pdf>.");
    }

    // parsed parameters
    private float xOffset;
    private float yOffset;
    private float width = 180;
    private float height = 36;
    private int page = 1;
    private String signatureFieldName = "Signature";
    private String[] fileNames;

    /**
     * Gets the x offset.
     *
     * @return x offset
     */
    public float getXOffset() {
	return xOffset;
    }
    
    /**
     * Gets the y offset.
     *
     * @return y offset
     */
    public float getYOffset() {
	return yOffset;
    }
    
    /**
     * Gets the page number.
     *
     * @return page number
     */
    public int getPage() {
	return page;
    }
    
    /**
     * Gets signature field name.
     *
     * @return signatrue field name
     */
    public String getSignatureFieldName() {
	return signatureFieldName;
    }
    
    /**
     * Gets file names.
     *
     * @return file names as string array
     */
    public String[] getFileNames() {
	return fileNames;
    }
    
    /**
     * Gets number of file names.
     *
     * @return number of file names
     */
    public int numberFileNames() {
	return fileNames.length;
    }
    
    /**
     * Gets file name.
     *
     * @param  n file name index
     * @return file names as string array
     */
    public String getFileName(final int n) {
	return fileNames[n];
    }
    
    public Float getWidth() {
	return width;
    }
    
    public Float getHeight() {
	return height;
    }
    
    /**
     * Default constructor.
     *
     * @param args command-line arguments
     */
    public Parameters(final String args[]) {
	log.fine("Parameters started");

	if ((args == null) || (args.length < 1)) {
	    usage();
	    log.fine("Error in parameters");
	    System.exit(1);
	}

	final CommandLineParser parser = new DefaultParser();
	CommandLine line = null;
	try {
	    line = parser.parse(options, args, true);
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

	if (line.hasOption("f")) {
	    signatureFieldName = line.getOptionValue("f");
	}

	if (line.hasOption("p")) {
	    try {
		page = ((Number)line.getParsedOptionValue("p")).intValue();
	    } catch (Exception exception) {
		System.err.println("Error in page number, exception: " + exception);
		log.fine("Failed to parse page number, exception: " + exception);
		System.exit(1);
	    }
	    if (page <= 0) {
		System.err.println("Page number must be positive");
		log.fine("Page number out of range");
		System.exit(1);
	    }
	}

	if (line.hasOption("w")) {
	    try {
		width = ((Float)line.getParsedOptionValue("w")).floatValue();
	    } catch (Exception exception) {
		System.err.println("Error in width, exception: " + exception);
		log.fine("Failed to parse width, exception: " + exception);
		System.exit(1);
	    }
	    if (width <= 0) {
		System.err.println("Width must be positive");
		log.fine("Width out of range");
		System.exit(1);
	    }
	}

	if (line.hasOption("h")) {
	    try {
		height = ((Float)line.getParsedOptionValue("h")).floatValue();
	    } catch (Exception exception) {
		System.err.println("Error in height, exception: " + exception);
		log.fine("Failed to parse height, exception: " + exception);
		System.exit(1);
	    }
	    if (height <= 0) {
		System.err.println("Height must be positive");
		log.fine("Height out of range");
		System.exit(1);
	    }
	}

	final String[] remArgs = line.getArgs();

	if ((remArgs.length < 3) || (remArgs.length > 4)) {
	    usage();
	    log.fine("Error in parameters");
	    System.exit(1);
	}

	xOffset = Float.parseFloat(remArgs[0]);
	yOffset = Float.parseFloat(remArgs[1]);
	fileNames = Arrays.copyOfRange(remArgs, 2, remArgs.length);

	log.fine("Parameters set up");
    }
}
