import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Reads the properties on UEDDLConvert.properties Meant to be applied on
 * loading the user exit
 */

public class UEDDLConvertSettings {

    UETrace trace;

    String alterTableLogFileDirectory;
    boolean applyModifiedStatements;
    boolean suppressAllStatements;
    private String suppressTables;
    public List<String> suppressedTablesList = new ArrayList<String>();
    boolean replaceVarchar2;
    String cdcDBUser;
    boolean debug;

    // regex patterns

    Pattern createTable;
    Pattern createTableConstraintFK;
    Pattern createTableConstraintCheck;
    Pattern createTableConstraintCheckNoEnable;
    Pattern createTableConstraintPKUsingIndex;
    Pattern createTableSupplementalLog;
    Pattern createTableDeletedTablespace;

    Pattern varchar2Type;

    Pattern alterTableAddConstraintFK;
    Pattern alterTableAddConstraintCheck;
    Pattern alterTableShrinkSpace;

    Pattern dropTable;

    Pattern createIndex;
    Pattern createBitmapIndex;
    Pattern createUniqueIndex;

    Pattern alterIndex;
    Pattern alterIndexShrinkSpace;

    Pattern dropIndex;

    Pattern grantOnTo;

    // Regex Operations
    Pattern[] suppressedOperations;
    Pattern[] executeIfExistsOperations;
    Pattern[] removeOperations;

    public UEDDLConvertSettings(String propertiesFileName) throws Exception {
        trace = new UETrace();
        String currentDirectory = System.getProperty("user.dir")
                + File.separator;
        Properties properties = new Properties();
        try {
            trace.writeAlways("Getting UEDDLConvert.properties");
            FileInputStream propertiesStream = new FileInputStream(
                    currentDirectory + propertiesFileName);
            properties.load(propertiesStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        debug = Boolean.parseBoolean(properties.getProperty("debug", "false"));
        applyModifiedStatements = Boolean.parseBoolean(properties.getProperty(
                "applyModifiedStatements", "true"));
        suppressAllStatements = Boolean.parseBoolean(properties.getProperty(
                "suppressAllStatements", "false"));
        suppressTables = properties.getProperty("suppressTables", "");
        // Convert suppressedTables to list of suppressed tables
        if (!suppressTables.isEmpty()) {
            String[] tables = suppressTables.split(";", -1);
            for (String tableName : tables) {
                if (!tableName.isEmpty())
                    suppressedTablesList.add(tableName);
            }
        }

        replaceVarchar2 = Boolean.parseBoolean(properties.getProperty(
                "replaceVarchar2", "false"));
        alterTableLogFileDirectory = properties.getProperty(
                "alterTableLogFileDirectory", "log");
        cdcDBUser = properties.getProperty("cdcDBUser");

        createTable = Pattern.compile(properties.getProperty("createTable"),
                Pattern.CASE_INSENSITIVE);

        createTableConstraintFK = Pattern.compile(properties.getProperty("createTableConstraintFK"),
                Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        createTableConstraintCheck = Pattern.compile(properties.getProperty("createTableConstraintCheck"),
                Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        createTableConstraintCheckNoEnable = Pattern.compile(properties.getProperty("createTableConstraintCheckNoEnable"),
                Pattern.CASE_INSENSITIVE);

        createTableConstraintPKUsingIndex = Pattern.compile(properties.getProperty("createTableConstraintPKUsingIndex"),
                Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        createTableSupplementalLog = Pattern.compile(properties.getProperty("createTableSupplementalLog"),
                Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        createTableDeletedTablespace = Pattern.compile(properties.getProperty("createTableDeletedTablespace"),
                Pattern.CASE_INSENSITIVE);

        varchar2Type = Pattern.compile(properties.getProperty("varchar2Type"),
                Pattern.CASE_INSENSITIVE);

        alterTableAddConstraintFK = Pattern.compile(properties.getProperty("alterTableAddConstraintFK"),
                Pattern.CASE_INSENSITIVE);

        alterTableAddConstraintCheck = Pattern.compile(properties.getProperty("alterTableAddConstraintCheck"),
                Pattern.CASE_INSENSITIVE);

        alterTableShrinkSpace = Pattern.compile(properties.getProperty("alterTableShrinkSpace"),
                Pattern.CASE_INSENSITIVE);

        dropTable = Pattern.compile(properties.getProperty("dropTable"),
                Pattern.CASE_INSENSITIVE);

        createIndex = Pattern.compile(properties.getProperty("createIndex"),
                Pattern.CASE_INSENSITIVE);

        createBitmapIndex = Pattern.compile(properties.getProperty("createBitmapIndex"),
                Pattern.CASE_INSENSITIVE);

        createUniqueIndex = Pattern.compile(properties.getProperty("createUniqueIndex"),
                Pattern.CASE_INSENSITIVE);

        alterIndex = Pattern.compile(properties.getProperty("alterIndex"),
                Pattern.CASE_INSENSITIVE);

        alterIndexShrinkSpace = Pattern.compile(properties.getProperty("alterIndexShrinkSpace"),
                Pattern.CASE_INSENSITIVE);

        grantOnTo = Pattern.compile(properties.getProperty("grantOnTo"),
                Pattern.CASE_INSENSITIVE);

        dropIndex = Pattern.compile(properties.getProperty("dropIndex"),
                Pattern.CASE_INSENSITIVE);

        // Operations

        // The operations included in this collection are fully suppressed
        suppressedOperations = new Pattern[]{
                alterTableAddConstraintFK,
                alterTableAddConstraintCheck,
                alterTableShrinkSpace,
                createIndex,
                createBitmapIndex,
                alterIndexShrinkSpace,
                grantOnTo
        };

        executeIfExistsOperations = new Pattern[]{
                dropTable,
                dropIndex,
                alterIndex
        };

        removeOperations = new Pattern[]{
                createTableConstraintFK,
                createTableSupplementalLog,
                createTableDeletedTablespace
        };


//		trace.writeAlways("debug: " + debug);
//		trace.writeAlways("applyModifiedStatements: " + applyModifiedStatements);
//		trace.writeAlways("suppressAllStatements: " + suppressAllStatements);
//		trace.writeAlways("suppressTables: " + suppressTables);
//		trace.writeAlways("replaceVarchar2: " + replaceVarchar2);
//		trace.writeAlways("alterTableLogFileDirectory: "
//				+ alterTableLogFileDirectory);
//		trace.writeAlways("cdcDBUser: " + cdcDBUser);

    }
}
