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

        CompositeConfiguration config = new CompositeConfiguration();
        config.addConfiguration(new SystemConfiguration());
        config.addConfiguration(
                new PropertiesConfiguration(currentDirectory + propertiesFileName)
        );

        trace.writeAlways("Getting UEDDLConvert.properties");

        //Boolean.parseBoolean(properties.getProperty("debug", "false"));
        debug = config.getBoolean("debug", false);
        // Boolean.parseBoolean(properties.getProperty("applyModifiedStatements", "true"));
        applyModifiedStatements = config.getBoolean("applyModifiedStatements", true);

        // Boolean.parseBoolean(properties.getProperty("suppressAllStatements", "false"));
        suppressAllStatements = config.getBoolean("suppressAllStatements", false);

        //properties.getProperty("suppressTables", "");
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

        alterTableLogFileDirectory = config.getString("alterTableLogFileDirectory", "log");

        cdcDBUser = config.getString("cdcDBUser");

        createTable = Pattern.compile(config.getString("createTable"),
                Pattern.CASE_INSENSITIVE);

        createTableConstraintFK = Pattern.compile(config.getString("createTableConstraintFK"),
                Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        createTableConstraintCheck = Pattern.compile(config.getString("createTableConstraintCheck"),
                Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        createTableConstraintCheckNoEnable = Pattern.compile(config.getString("createTableConstraintCheckNoEnable"),
                Pattern.CASE_INSENSITIVE);

        createTableConstraintPKUsingIndex = Pattern.compile(config.getString("createTableConstraintPKUsingIndex"),
                Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        createTableSupplementalLog = Pattern.compile(config.getString("createTableSupplementalLog"),
                Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

        createTableDeletedTablespace = Pattern.compile(config.getString("createTableDeletedTablespace"),
                Pattern.CASE_INSENSITIVE);

        varchar2Type = Pattern.compile(config.getString("varchar2Type"),
                Pattern.CASE_INSENSITIVE);

        alterTableAddConstraintFK = Pattern.compile(config.getString("alterTableAddConstraintFK"),
                Pattern.CASE_INSENSITIVE);

        alterTableAddConstraintCheck = Pattern.compile(config.getString("alterTableAddConstraintCheck"),
                Pattern.CASE_INSENSITIVE);

        alterTableShrinkSpace = Pattern.compile(config.getString("alterTableShrinkSpace"),
                Pattern.CASE_INSENSITIVE);

        dropTable = Pattern.compile(config.getString("dropTable"),
                Pattern.CASE_INSENSITIVE);

        createIndex = Pattern.compile(config.getString("createIndex"),
                Pattern.CASE_INSENSITIVE);

        createBitmapIndex = Pattern.compile(config.getString("createBitmapIndex"),
                Pattern.CASE_INSENSITIVE);

        createUniqueIndex = Pattern.compile(config.getString("createUniqueIndex"),
                Pattern.CASE_INSENSITIVE);

        alterIndex = Pattern.compile(config.getString("alterIndex"),
                Pattern.CASE_INSENSITIVE);

        alterIndexShrinkSpace = Pattern.compile(config.getString("alterIndexShrinkSpace"),
                Pattern.CASE_INSENSITIVE);

        grantOnTo = Pattern.compile(config.getString("grantOnTo"),
                Pattern.CASE_INSENSITIVE);

        dropIndex = Pattern.compile(config.getString("dropIndex"),
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

        logProperties();
    }

    private void logProperties(){
        log("applyModifiedStatements", applyModifiedStatements);
        log("suppressAllStatements", suppressAllStatements);
        log("suppressTables", suppressTables);
        log("replaceVarchar2", replaceVarchar2);
        log("cdcDBUser", cdcDBUser);
        log("debug", debug);

        log("createTable", createTable);
        log("createTableConstraintFK", createTableConstraintFK);
        log("createTableConstraintCheck", createTableConstraintCheck);
        log("createTableConstraintCheckNoEnable", createTableConstraintCheckNoEnable);
        log("createTableConstraintPKUsingIndex", createTableConstraintPKUsingIndex);
        log("createTableSupplementalLog", createTableSupplementalLog);
        log("createTableDeletedTablespace", createTableDeletedTablespace);

        log("varchar2Type", varchar2Type);

        log("alterTableAddConstraintFK", alterTableAddConstraintFK);
        log("alterTableAddConstraintCheck", alterTableAddConstraintCheck);
        log("alterTableShrinkSpace", alterTableShrinkSpace);

        log("dropTable", dropTable);

        log("createIndex", createIndex);
        log("createBitmapIndex", createBitmapIndex);
        log("createUniqueIndex", createUniqueIndex);

        log("alterIndex", alterIndex);
        log("alterIndexShrinkSpace", alterIndexShrinkSpace);

        log("dropIndex", dropIndex);

        log("grantOnTo", grantOnTo);
    }

    private void log(String propertyName, Object property){
        trace.writeAlways(
                String.format("%s: %s",
                        propertyName, property.toString()));
    }
}
