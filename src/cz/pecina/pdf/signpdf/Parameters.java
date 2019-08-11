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

package cz.pecina.pdf.signpdf;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import java.util.logging.Logger;

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
    	    Option.builder("k")
    	         .longOpt("key")
    	         .hasArg()
    	         .argName("FILE")
    	         .desc("(required) key file")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("p")
    	         .longOpt("pass")
    	         .hasArg()
    	         .argName("PASSWORD")
    	         .desc("password")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("c")
    	         .longOpt("cert-level")
    	         .hasArg()
    	         .type(Number.class)
    	         .argName("LEVEL")
    	         .desc("certification level (O-3)")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("f")
    	         .longOpt("field")
    	         .hasArg()
    	         .argName("FIELD")
    	         .desc("signature field name (if none, invisible signature is created)")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("a")
    	         .longOpt("append")
    	         .desc("append signature as an update")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("r")
    	         .longOpt("reason")
    	         .hasArg()
    	         .argName("REASON")
    	         .desc("reason")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("l")
    	         .longOpt("location")
    	         .hasArg()
    	         .argName("LOCATION")
    	         .desc("location")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("o")
    	         .longOpt("contact")
    	         .hasArg()
    	         .argName("CONTACT")
    	         .desc("contact information")
    	         .build()
    	    );
    	options.addOption(
    	    Option.builder("A")
    	         .longOpt("alias")
    	         .hasArg()
    	         .argName("ALIAS")
    	         .desc("alias in keystore (in none, first alias is used)")
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
	helpFormatter.printHelp("signpdf [options] infile [outfile]", options);
	System.out.println("\nThe source code is available from <https://github.com/tompecina/pdf>.");
    }

    // parsed parameters
    private String keyFileName;
    private char[] password = null;
    private int certificationLevel = 1;
    private String signatureFieldName = null;
    private boolean signatureAppend;
    private String reason = null;
    private String location = null;
    private String contact = null;
    private String alias = null;
    private String[] fileNames;

    /**
     * Gets key file name.
     *
     * @return key file name
     */
    public String getKeyFileName() {
	return keyFileName;
    }
    
    /**
     * Gets password.
     *
     * @return password as character array
     */
    public char[] getPassword() {
	return password;
    }
    
    /**
     * Gets certification level.
     *
     * @return certification level
     */
    public int getCertificationLevel() {
	return certificationLevel;
    }
    
    /**
     * Gets signature field name.
     *
     * @return signature field name
     */
    public String getSignatureFieldName() {
	return signatureFieldName;
    }
    
    /**
     * Gets signature append flag.
     *
     * @return signature append flag (true=append)
     */
    public boolean getSignatureAppend() {
	return signatureAppend;
    }
    
    /**
     * Gets signature reason.
     *
     * @return signature reason
     */
    public String getReason() {
	return reason;
    }
    
    /**
     * Gets signature location.
     *
     * @return signature location
     */
    public String getLocation() {
	return location;
    }
    
    /**
     * Gets signer's contact information.
     *
     * @return signer's contact information
     */
    public String getContact() {
	return contact;
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
     * Gets alias.
     *
     * @return alias
     */
    public String getAlias() {
	return alias;
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

	if (!line.hasOption("k")) {
	    System.err.println("Key file is required");
	    log.fine("Error in paramters, missing key file");
	    System.exit(1);
	}
	    
	fileNames = line.getArgs();

	if ((fileNames.length < 1) || (fileNames.length > 2)) {
	    usage();
	    log.fine("Error in parameters");
	    System.exit(1);
	}

	keyFileName = line.getOptionValue("k");
	signatureAppend = line.hasOption("a");

	if (line.hasOption("p")) {
	    password = line.getOptionValue("p").toCharArray();
	}

	if (line.hasOption("c")) {
	    try {
		certificationLevel = ((Number)line.getParsedOptionValue("c")).intValue();
	    } catch (Exception exception) {
		System.err.println("Error in certification level, exception: " + exception);
		log.fine("Failed to parse certification level, exception: " + exception);
		System.exit(1);
	    }
	    if ((certificationLevel < 0) || (certificationLevel > 3)) {
		System.err.println("Certification level must be 0-3");
		log.fine("Certification level out of range");
		System.exit(1);
	    }
	}

	if (line.hasOption("f")) {
	    signatureFieldName = line.getOptionValue("f");
	}

	if (line.hasOption("r")) {
	    reason = line.getOptionValue("r");
	}

	if (line.hasOption("l")) {
	    location = line.getOptionValue("l");
	}

	if (line.hasOption("o")) {
	    contact = line.getOptionValue("o");
	}

	if (line.hasOption("A")) {
	    alias = line.getOptionValue("A");
	}

	log.fine("Parameters set up");
    }
}
