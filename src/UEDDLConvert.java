import com.datamirror.ts.target.publication.userexit.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UEDDLConvert implements SubscriptionUserExitIF {

	private static PrintWriter logWriter;

	// Date format used for inclusion in the file name
	private static SimpleDateFormat dateFormatFile = new SimpleDateFormat(
			"yyyy_MM_dd_hh_mm_ss");
	// Date format used for logging the entries
	private static SimpleDateFormat dateFormatISO = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss.SSS");

	// If an operation is suppressed, it is replaced by a dummy operation
	private static final String NO_OPERATION = "SELECT 'X' from DUAL";

	// Settings retrieved from properties file
	private static UEDDLConvertSettings settings;
	private static UETrace trace;

	private String sourceSystemID;
	private boolean pkConstraintUsingIndex = false;

	/**
	 * Generic init method to allow tester class to work
	 */
	public void init() {
		try {
			logWriter = null;

			settings = new UEDDLConvertSettings("UEDDLConvert.properties");

			trace = new UETrace();
			trace.init(settings.debug);
			trace.writeAlways("Subscription user exit initialization");
		} catch (Exception e) {
			trace.writeAlways(String.format(
					".properties configuration error.\nError: %s",
					e.getMessage()));
		}
	}

	/**
	 * This method is called once when the subscription is started. It prepares
	 * the environment for processing the subscription events (DDL changes).
	 */
	public void init(SubscriptionEventPublisherIF publisher)
			throws UserExitException {
		// Subscribe to the BEFORE_DDL event only (the processSubscriptionEvent
		// is invoked before the DDL executes)
		publisher.unsubscribeEvent(SubscriptionEventTypes.ALL_EVENTS);
		publisher.subscribeEvent(SubscriptionEventTypes.BEFORE_DDL_EVENT);

		sourceSystemID = publisher.getSourceSystemID();

		init();

	}

	/**
	 * This method is called before each DDL change. The method interrogates the
	 * DDL statement and modifies it through the modifyStatement property. After
	 * doing so, the before and after statements are logged and the new
	 * statement is returned to CDC for execution.
	 */
	public boolean processSubscriptionEvent(SubscriptionEventIF event)
			throws UserExitException {

		// Reset variables
		pkConstraintUsingIndex = false;

		// Get the statements to be processed
		String[] ddlStatementsArray = event.getDdlStatements();
		String[] modifiedDdlStatementsArray = event.getDdlStatements();

		try {
			for (int i = 0; i < ddlStatementsArray.length; i++) {
				String commitTimestamp = event.getCommitTimeStamp().toString();
				modifiedDdlStatementsArray[i] = modifyStatement(
						ddlStatementsArray[i], commitTimestamp,
						event.getDdlSchema(), event.getDdlTableSchema(),
						event.getDdlTableName());
				// If the transformed DDL statement is null, suppress operation
				if (modifiedDdlStatementsArray[i] == null) {
					modifiedDdlStatementsArray[i] = NO_OPERATION;
				} else {
					event.logEvent("Modified statement: "
							+ modifiedDdlStatementsArray[i]);
				}
			}
			// If instructed to apply the modified statements, invoke the CDC
			// method
			if (settings.applyModifiedStatements) {
				event.setDdlStatement(modifiedDdlStatementsArray);
			}
		} catch (Exception e) {
			trace.writeAlways(e.getMessage());
		}

		// Return true to apply the DDL statement(s)
		return true;
	}

	/**
	 * This method is invoked when the subscription that uses the user exit
	 * stops.
	 */
	public void finish() {
		// Close the output file (in case there is one)
		if (logWriter != null)
			logWriter.close();
	}

	/**
	 * This method logs the DDL statement in the current output file. If the
	 * output file does not exist yet, it will be created and a header row is
	 * written.
	 */
	private void logDdl(String statement, String modifiedStatement,
			String commitTimeStamp, String ddlSchema, String tableSchema,
			String tableName) throws UserExitException {
		try {
			String filePath, logFormat;

			trace.write("DDL Statement for schema " + ddlSchema
					+ ", table schema " + tableSchema + ", table name "
					+ tableName);

			// If this is the first time, create an output file
			if (logWriter == null) {
				// If the logDdl method is used during autonomous testing, the
				// sourceSystemID is null, provide value
				if (sourceSystemID == null)
					sourceSystemID = "UEDDLConvert";
				String fileDate = dateFormatFile.format(new Date());
				filePath = settings.ddlStatementsLogFileDirectory
						+ File.separator + sourceSystemID + "_DDL_" + fileDate
						+ ".log";
				try {
					trace.write("Writing new log file: " + filePath);
					logWriter = new PrintWriter(filePath);
				} catch (FileNotFoundException e) {
					throw new UserExitException(String.format(
							"File %s not found.\nError: %s", filePath,
							e.getMessage()));
				}

				// Write header
				logWriter
						.println("CommitTimeStamp\tDDLSchema\tDDLTableSchema\tDDLTableName\tApplyTimeStamp\tDDLStatement\tModifiedDDLStatement");
			}

			String applyTimestamp = dateFormatISO.format(new Date());
			logFormat = commitTimeStamp + "\t" + ddlSchema + "\t" + tableSchema
					+ "\t" + tableName + "\t" + applyTimestamp + "\t"
					+ cleanDdlStatement(statement) + "\t"
					+ cleanDdlStatement(modifiedStatement);
			trace.write(logFormat);
			logWriter.println(logFormat);
			logWriter.flush();
		} catch (Exception e) {
			trace.writeAlways(String.format("Error in logDdl. error: %s",
					e.getMessage()));
		}
	}

	/**
	 * This method removes tabs and new lines from the logged DDL statements to
	 * that the log file can be read using a spreadsheet utility
	 */
	private String cleanDdlStatement(String ddlStatement) {
		String cleanedStatement = ddlStatement;
		if (cleanedStatement != null) {
			cleanedStatement = ddlStatement.replaceAll("\\t", "");
			cleanedStatement = ddlStatement.replaceAll("\\n", "");
		}
		return cleanedStatement;
	}

	/**
	 * This is the main function that calls all DDL transformation methods. If
	 * new transformations must take place, create a new method to do the
	 * transformation and invoke it from this function.
	 * 
	 * @throws UserExitException
	 */
	String modifyStatement(String originalStatement, String commitTimeStamp,
			String ddlSchema, String tableSchema, String tableName)
			throws UserExitException {

		String modifiedStatement = originalStatement;
		String qualifiedTable = tableSchema + "." + tableName;

		if (!settings.suppressAllStatements) {
			if (!settings.suppressedTablesList.contains(qualifiedTable)) {
				// Process a pipeline of DDL transformations on the current
				// statement

				// First collect information about the statement (to be used
				// later)
				isCreateTablePrimaryKeyUsingIndex(modifiedStatement);

				// Check if the statement must be fully suppressed
				modifiedStatement = suppressStatements(modifiedStatement);

				// Remove clauses that must be suppressed
				modifiedStatement = suppressClauses(modifiedStatement);

				// Suppress Check constraint clauses not terminated by enable
				modifiedStatement = removeCHECKConstraintWithoutEnable(modifiedStatement);

				// Replace strings indicated in the configuration
				modifiedStatement = replaceStrings(modifiedStatement);

				// Transform VARCHAR2(nn) to CHAR semantics
				modifiedStatement = replaceVarchar2(modifiedStatement);

				// Suppress the creation of a unique index if the create table
				// already implicitly creates it
				modifiedStatement = suppressUniqueIndex(modifiedStatement,
						tableSchema, tableName);

				// If the statement is a CREATE TABLE, execute through a stored
				// procedure
				modifiedStatement = executeCreateTable(modifiedStatement,
						tableSchema, tableName);
				// The executeDDLIfExists transformation should be the last one
				// in the pipeline
				modifiedStatement = executeDDLIfExists(modifiedStatement,
						tableSchema);
			} else {
				trace.write("Statement for table "
						+ qualifiedTable
						+ " will be suppressed because table is in suppressed tables list: "
						+ originalStatement);
			}
		} else {
			trace.write("Statement will be suppressed because suppressAllStatements is true: "
					+ originalStatement);
		}

		// Log the statement and modified statement to the output file
		logDdl(originalStatement, modifiedStatement, commitTimeStamp,
				ddlSchema, tableSchema, tableName);

		return modifiedStatement;
	}

	/**
	 * Suppresses any statements specified in the configuration
	 */
	private String suppressStatements(String statement) {
		String modifiedStatement = statement;
		if (statement != null) {
			for (Pattern suppressedStatement : settings.suppressedStatements) {
				Matcher ssMatcher = suppressedStatement.matcher(statement);
				if (ssMatcher.find()) {
					trace.write("Statement will be suppressed: " + statement);
					modifiedStatement = null;
				}
			}
		}
		return modifiedStatement;
	}

	/**
	 * Suppresses any clauses specified in the configuration
	 */
	private String suppressClauses(String statement) {
		String modifiedStatement = statement;
		if (statement != null) {
			for (Pattern suppressedClause : settings.suppressedClauses) {
				Matcher scMatcher = suppressedClause.matcher(statement);
				// First report all clauses that will be suppressed
				while (scMatcher.find()) {
					trace.write("Clause will be removed from statement: "
							+ scMatcher.group());
				}
				modifiedStatement = scMatcher.replaceAll("");
			}
		}
		return modifiedStatement;
	}

	/**
	 * Replaced VARCHAR2(nn) by VARCHAR2(nn CHAR)
	 */
	private String replaceVarchar2(String statement) {
		String modifiedStatement = statement;
		if (settings.replaceVarchar2 && statement != null) {
			Matcher varchar2TypeMatcher = settings.varchar2Type
					.matcher(statement);
			StringBuffer modifiedStatementBuffer = new StringBuffer();
			// Log all the column types that will be replaced
			while (varchar2TypeMatcher.find()) {
				trace.write("Type of column will be changed to CHAR semantics: "
						+ varchar2TypeMatcher.group());
				varchar2TypeMatcher.appendReplacement(
						modifiedStatementBuffer,
						varchar2TypeMatcher.group(1)
								+ varchar2TypeMatcher.group(2) + " CHAR"
								+ varchar2TypeMatcher.group(3));
			}
			varchar2TypeMatcher.appendTail(modifiedStatementBuffer);
			modifiedStatement = modifiedStatementBuffer.toString();
		}
		return modifiedStatement;
	}

	/**
	 * Replace any strings specified in the configuration
	 */
	private String replaceStrings(String statement) {
		String modifiedStatement = statement;
		if (statement != null) {
			for (String replacedString : settings.replacedStrings) {
				String stringToReplace = replacedString.split(";")[0];
				String stringReplacedWith = replacedString.split(";")[1];
				Pattern stringToReplacePattern = Pattern.compile(
						stringToReplace, Pattern.CASE_INSENSITIVE
								+ Pattern.DOTALL);
				Matcher rsMatcher = stringToReplacePattern.matcher(statement);
				// First report all clauses that will be suppressed
				while (rsMatcher.find()) {
					trace.write("String will be replaced: " + rsMatcher.group()
							+ " --> " + stringReplacedWith);
				}
				modifiedStatement = rsMatcher.replaceAll(stringReplacedWith);
			}
		}
		return modifiedStatement;
	}

	/**
	 * Replaces certain statements with a call to stored procedure
	 * SP_EXECUTE_DDL_IF_EXISTS
	 */
	private String executeDDLIfExists(String statement, String tableSchema) {
		String modifiedStatement = statement;
		String objectType = "";
		String objectSchema = "";
		String objectName = "";
		if (statement != null) {
			for (Pattern ifExistOperation : settings.executeIfExistsOperations) {
				Matcher executeDDLMatcher = ifExistOperation.matcher(statement);
				if (executeDDLMatcher.find()) {
					// Extract the object schema and name from the SQL statement
					objectType = executeDDLMatcher.group(1).toUpperCase();
					objectSchema = executeDDLMatcher.group(3);
					if (objectSchema == null)
						objectSchema = tableSchema;
					objectName = executeDDLMatcher.group(4);
					trace.write("Operation on " + objectType + " "
							+ objectSchema + "." + objectName
							+ " will be executed only if the object exists: "
							+ statement);
					modifiedStatement = "CALL " + settings.cdcDBUser
							+ ".SP_EXECUTE_DDL_IF_EXISTS('" + objectType
							+ "','" + objectSchema + "','" + objectName + "','"
							+ statement + "')";
				}
			}
		}
		return modifiedStatement;
	}

	/**
	 * Removes the CHECK constraint from the CREATE TABLE statement if the
	 * constraint does not have an ENABLE clause. First the check constraint is
	 * located using a regular expression, after which the parenthesis matching
	 * the opening parenthesis of the condition is searched. Once found, the
	 * entire check constraint is removed. The method makes the safe assumption
	 * that the condition is syntactically correct.
	 */
	private String removeCHECKConstraintWithoutEnable(String statement) {
		String modifiedStatement = statement;
		if (statement != null) {

			Matcher checkConstraintMatcherNoEnable = settings.createTableConstraintCheckNoEnable
					.matcher(modifiedStatement);

			// Repetitively find the check constraint
			while (checkConstraintMatcherNoEnable.find()) {

				char[] statementChars = modifiedStatement.toCharArray();
				int parenthesisCounter = 1;

				for (int i = checkConstraintMatcherNoEnable.end(); i < statementChars.length; i++) {
					char currentChar = statementChars[i];

					if (currentChar == '(') {
						parenthesisCounter += 1;
					} else if (currentChar == ')') {
						parenthesisCounter -= 1;
					} else if (parenthesisCounter <= 0) {
						trace.write("Check constraint clause will be removed: "
								+ modifiedStatement.substring(
										checkConstraintMatcherNoEnable.start(),
										i));
						modifiedStatement = modifiedStatement.substring(0,
								checkConstraintMatcherNoEnable.start())
								+ modifiedStatement.substring(i);
						break;
					}
				}

				// End of the CHECK condition has been found, remove
				checkConstraintMatcherNoEnable = settings.createTableConstraintCheckNoEnable
						.matcher(modifiedStatement);
			}
		}
		return modifiedStatement;
	}

	/**
	 * Checks if the statement is a CREATE TABLE with primary key constraint
	 * using index If so, it sets variable pk... to true.
	 */
	private void isCreateTablePrimaryKeyUsingIndex(String statement) {
		if (statement != null) {
			Matcher uniqueIndexMatcher = settings.createTableConstraintPKUsingIndex
					.matcher(statement);
			if (uniqueIndexMatcher.find()) {
				trace.write("CREATE TABLE with CONSTRAINT PRIMARY KEY USING INDEX found, unique indexes will be suppressed");
				pkConstraintUsingIndex = true;
			}
		}
	}

	/**
	 * Replaces CREATE UNIQUE INDEX if the current table already has a PRIMARY
	 * KEY ... USING INDEX clause
	 */
	private String suppressUniqueIndex(String statement, String tableSchema,
			String tableName) {
		String modifiedStatement = statement;
		if (statement != null) {
			Matcher uniqueIndexMatcher = settings.createUniqueIndex
					.matcher(statement);
			if (uniqueIndexMatcher.find() && pkConstraintUsingIndex) {
				trace.write("CREATE UNIQUE INDEX found but CREATE TABLE already created unique index, statement will be suppressed: "
						+ statement);
				modifiedStatement = null;
			}
		}
		return modifiedStatement;
	}

	/**
	 * Checks if the original statement is CREATE TABLE and calls a stored
	 * procedure that executes additional statements to be executed after the
	 * CREATE TABLE
	 */
	private String executeCreateTable(String statement, String tableSchema,
			String tableName) {
		String modifiedStatement = statement;
		if (modifiedStatement != null) {
			Matcher createTableMatcher = settings.createTable
					.matcher(statement);
			if (createTableMatcher.find()) {
				trace.write("CREATE TABLE found, will be executed through stored procedure SP_CREATE_TABLE");
				// Replace any quotes in the CREATE TABLE string so that they
				// are interpreted correctly
				modifiedStatement = modifiedStatement.replaceAll("'", "'''");
				modifiedStatement = "CALL " + settings.cdcDBUser
						+ ".SP_CREATE_TABLE('" + settings.cdcDBUser + "','"
						+ tableSchema + "','" + tableName + "','"
						+ modifiedStatement + "')";
			}
		}
		return modifiedStatement;
	}
}
