import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reads the properties on UEDDLConvert.properties Meant to be applied on
 * loading the user exit
 */

public class UEDDLConvertSettings {

	UETrace trace;

	String ddlStatementsLogFileDirectory;
	boolean applyModifiedStatements;
	boolean suppressAllStatements;
	private String suppressTables;
	public List<String> suppressedTablesList = new ArrayList<String>();
	boolean replaceVarchar2;
	String cdcDBUser;
	boolean debug;

	// Individual regex patterns
	Pattern createTable;
	Pattern createTableConstraintCheckNoEnable;
	Pattern createTableConstraintPKUsingIndex;

	Pattern varchar2Type;
	Pattern createUniqueIndex;

	// Grouped regex patterns
	ArrayList<Pattern> suppressedClauses = new ArrayList<Pattern>();
	ArrayList<Pattern> executeIfExistsOperations = new ArrayList<Pattern>();
	ArrayList<Pattern> suppressedStatements = new ArrayList<Pattern>();
	ArrayList<String> replacedStrings = new ArrayList<String>();

	public UEDDLConvertSettings(String propertiesFileName) throws Exception {
		trace = new UETrace();
		String currentDirectory = System.getProperty("user.dir")
				+ File.separator;

		CompositeConfiguration config = new CompositeConfiguration();
		config.addConfiguration(new SystemConfiguration());
		config.addConfiguration(new PropertiesConfiguration(currentDirectory
				+ propertiesFileName));

		trace.writeAlways("Getting UEDDLConvert.properties");

		debug = config.getBoolean("debug", false);
		applyModifiedStatements = config.getBoolean("applyModifiedStatements",
				true);
		suppressAllStatements = config.getBoolean("suppressAllStatements",
				false);

		suppressTables = config.getString("suppressTables", "");
		// Convert suppressedTables to list of suppressed tables
		if (!suppressTables.isEmpty()) {
			String[] tables = suppressTables.split(";", -1);
			for (String tableName : tables) {
				if (!tableName.isEmpty())
					suppressedTablesList.add(tableName);
			}
		}

		replaceVarchar2 = config.getBoolean("replaceVarchar2", false);

		ddlStatementsLogFileDirectory = config.getString(
				"ddlStatementsLogFileDirectory", "log");

		cdcDBUser = config.getString("cdcDBUser");

		createTable = Pattern.compile(config.getString("createTable"),
				Pattern.CASE_INSENSITIVE);

		createTableConstraintCheckNoEnable = Pattern.compile(
				config.getString("createTableConstraintCheckNoEnable"),
				Pattern.CASE_INSENSITIVE);

		createTableConstraintPKUsingIndex = Pattern.compile(
				config.getString("createTableConstraintPKUsingIndex"),
				Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

		varchar2Type = Pattern.compile(config.getString("varchar2Type"),
				Pattern.CASE_INSENSITIVE);

		createUniqueIndex = Pattern
				.compile(config.getString("createUniqueIndex"),
						Pattern.CASE_INSENSITIVE);

		// The statements included in this collection are fully suppressed
		String[] suppressedStatementsProperties = config.getString(
				"suppressedStatements").split(";");
		for (String suppressedStatementProperty : suppressedStatementsProperties) {
			suppressedStatements.add(Pattern.compile(
					config.getString(suppressedStatementProperty),
					Pattern.CASE_INSENSITIVE + Pattern.DOTALL));
		}

		// The clauses included in this collection are suppressed
		String[] suppressedClausesProperties = config.getString(
				"suppressedClauses").split(";");
		for (String suppressedClauseProperty : suppressedClausesProperties) {
			suppressedClauses.add(Pattern.compile(
					config.getString(suppressedClauseProperty),
					Pattern.CASE_INSENSITIVE + Pattern.DOTALL));
		}

		// The statements in this collection are only executed if the object
		// exists
		String[] executeIfExistsProperties = config
				.getString("executeIfExists").split(";");
		for (String executeIfExistsProperty : executeIfExistsProperties) {
			executeIfExistsOperations.add(Pattern.compile(
					config.getString(executeIfExistsProperty),
					Pattern.CASE_INSENSITIVE + Pattern.DOTALL));
		}

		// The strings included in this collection are replaced by something
		// else
		String[] replacedStringsProperties = config
				.getString("replacedStrings").split(";");
		for (String replacedStringProperty : replacedStringsProperties) {
			replacedStrings.add(config.getString(replacedStringProperty));
		}

		// Log all properties
		logProperties(config);

	}

	private void logProperties(CompositeConfiguration config) {
		log("applyModifiedStatements", applyModifiedStatements);
		log("suppressAllStatements", suppressAllStatements);
		log("suppressTables", suppressTables);
		log("replaceVarchar2", replaceVarchar2);
		log("cdcDBUser", cdcDBUser);
		log("debug", debug);

		log("createTable", createTable);
		// log("createTableConstraintFK", createTableConstraintFK);
		// log("createTableConstraintCheck", createTableConstraintCheck);
		log("createTableConstraintCheckNoEnable",
				createTableConstraintCheckNoEnable);
		log("createTableConstraintPKUsingIndex",
				createTableConstraintPKUsingIndex);
		// log("createTableSupplementalLog", createTableSupplementalLog);
		// log("createTableDeletedTablespace", createTableDeletedTablespace);

		log("varchar2Type", varchar2Type);

		// log("alterTableAddConstraintFK", alterTableAddConstraintFK);
		// log("alterTableAddConstraintCheck", alterTableAddConstraintCheck);
		// log("alterTableShrinkSpace", alterTableShrinkSpace);

		// log("dropTable", dropTable);

		// log("createIndex", createIndex);
		// log("createBitmapIndex", createBitmapIndex);
		log("createUniqueIndex", createUniqueIndex);

		// log("alterIndex", alterIndex);
		// log("alterIndexShrinkSpace", alterIndexShrinkSpace);

		// log("dropIndex", dropIndex);

		// log("grantOnTo", grantOnTo);

		log("suppressedStatements", config.getString("suppressedStatements"));
		for (Pattern suppressedStatement : suppressedStatements)
			log("--", suppressedStatement);
		log("suppressedClauses", config.getString("suppressedClauses"));
		for (Pattern suppressedClause : suppressedClauses)
			log("--", suppressedClause);
		log("executeIfExists", config.getString("executeIfExists"));
		for (Pattern executeIfExistsOperation : executeIfExistsOperations)
			log("--", executeIfExistsOperation);
		log("replacedStrings", config.getString("replacedStrings"));
		for (String replacedString : replacedStrings)
			log("--",
					replacedString.split(";")[0] + " --> "
							+ replacedString.split(";")[1]);

	}

	private void log(String propertyName, Object property) {
		trace.writeAlways(String.format("%s: %s", propertyName,
				property.toString()));
	}

}
