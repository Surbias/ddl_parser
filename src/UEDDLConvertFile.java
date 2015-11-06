import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.datamirror.ts.target.publication.userexit.UserExitException;

public class UEDDLConvertFile {

	public static final Pattern SQL_STATEMENT = Pattern.compile("([^;])+;\\s*",
			Pattern.CASE_INSENSITIVE);

	UEDDLConvert convert;

	public static void main(String[] args) throws Exception {

		if (args.length < 1 || args.length > 2) {
			System.err
					.println("Syntax: UEDDLConvertFile <InputFile> [<OutputFile>]");
			System.exit(-1);
		}

		String inputFile = args[0];
		String outputFile = null;
		if (args.length == 2)
			outputFile = args[1];
		new UEDDLConvertFile(inputFile, outputFile);

	}

	UEDDLConvertFile(String inputFile, String outputFile) throws IOException,
			UserExitException {
		convert = new UEDDLConvert();
		convert.init();

		log("Converting input file " + inputFile + " to " + outputFile);

		// Read entire input file into a string  
		String inputSQLString = readFileToString(inputFile);
		// log(inputSQLString);

		// Look for SQL statements terminated with a semicolon and convert
		String outputSQLString = "";
		Matcher sqlMatcher = SQL_STATEMENT.matcher(inputSQLString);
		int i = 1;
		int j = 0;
		while (sqlMatcher.find()) {
			String originalStatement = sqlMatcher.group();
			log("-- New statement --");
			log("Original: " + originalStatement);
			String convertedStatement = convert.modifyStatement(
					originalStatement, "2015-09-27", "DUMMY", "DUMMY", "DUMMY");
			log("Converted: " + convertedStatement);
			if (convertedStatement != null) {
				outputSQLString += convertedStatement;
				j++;
			}
			i++;
		}

		log("Conversion completed, " + i + " statements read, " + j
				+ " converted");
		if (outputFile != null) {
			log("Writing results to output file " + outputFile);
			writeSTringToFile(outputFile, outputSQLString);
		}
		log("Finished successfully");
	}

	private void writeSTringToFile(String outputFile, String outputFilestring)
			throws IOException {
		File fout = new File(outputFile);
		FileOutputStream fos = new FileOutputStream(fout);
		fos.write(outputFilestring.getBytes());
		fos.close();
	}

	// private String readFileToString(String fileName) throws IOException {
	//
	// File fin = new File(fileName);
	// FileInputStream fis = new FileInputStream(fin);
	// BufferedReader in = new BufferedReader(new InputStreamReader(fis));
	// char[] chrArr = new char[(int) fin.length()];
	// while (in.ready() == false) {
	// }
	// in.read(chrArr);
	// in.close();
	// return new String(chrArr);
	// }

	private String readFileToString(String fileName) throws IOException {

		String outString = "";

		BufferedReader br = new BufferedReader(new FileReader(
				new File(fileName)));
		for (String line; (line = br.readLine()) != null;) {
			if (!line.startsWith("--"))
				outString += line + "\n";
		}
		br.close();
		return outString;
	}

	private void log(String message) {
		System.out.println(message);
	}
}
