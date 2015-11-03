
# UEDDLConvert User Exit description

## Authors:
 - Filipe Almeida (filipe.almeida@softinsa.com)
 - Frank Ketelaars (fketelaars@nl.ibm.com)

## Change log
- 2015-08-19
  - Initial delivery to customer


- 2015-09-23
  - Fixed issue with foreign key with cascade delete, now also properly removed.

  - Added logging for the substrings which are removed and statements which are suppressed.

  - Fixed issue with the removal of check constraints. Added suppression of ALTER TABLE ADD CONSTRAINT CHECK



- 2015-09-24  
  - Fixed logging of event when resulting statement is suppressed (null)


- 2015-09-25  
  - Also match newline characters in CHECK constraint



- 2015-09-28
  - Also check constraint with VALIDATE/NOVALIDATE clause


- 2015-09-30  
  - Remove SUPPLEMENTAL LOG GROUP clauses


- 2015-10-01  
  - Optionally replace VARCHAR2(nn) by VARCHAR2(nn CHAR) to accommodate special characters when converted to UTF-8


- 2015-10-08
  - Suppress statement ALTER TABLE SHRINK SPACE.

  - Create generic stored procedure that will execute DDL only if object exists, applied for

  - DROP TABLE, DROP INDEX, ALTER INDEX
Suppress the TABLESPACE '$deleted$xxx' clause



- 2015-10-12
  - Suppress statement ALTER INDEX SHRINK SPACE

  - New property: suppressTables. In this property you can specify tables for which DDLs must be suppressed. This can be used for example to let CDC continue after a DDL for a certain table

- 2015-10-13
  - Suppress statement CREATE BITMAP INDEX

  - Remove CONSTRAINT FOREIGN KEY ON DELETE SET NULL clauses


## Description
This user exit facilitates converting DDL statements before they are executed against the target database and is to be used in a rule-based replication definition. At the time of initial delivery, the user exit transforms the following DDL statements:

- CREATE TABLE: Remove foreign key constraints, remove check constraints

- ALTER TABLE ADD CONSTRAINT FOREIGN KEY: Suppress

- ALTER TABLE ADD CONSTRAINT CHECK: Suppress

- DROP TABLE: Change to call stored procedure SP_DROP_TABLE_IF_EXISTS

- GRANT...ON...TO: Suppress

- CREATE INDEX: Suppress (non-unique key index creations are suppressed)

The user exit can be activated by specifying it at the subscription level; it handles only DDL events and is
invoked before the DDL statement is executed on the target while there is still a chance to transform or suppress
the DDL statement in question.

At subscription initialization, the user exit reads the UEDDLConvert.properties file which controls the behaviour of
the user exit such as whether DDL statements are transformed and in which directory the DDL statements are logged.

## Usage instructions
In the CDC Management Console, right-click on the rule-based subscription and select "User exit...", then specify the name UEDDLConvert.

## Compilation instructions
The user exit has a dependency on the ts.jar file. To compile, do the following:

```
cd src
javac -d ../bin -cp <cdc_home>/lib:<cdc_home>/lib/ts.jar UETrace.java
javac -d ../bin -cp <cdc_home>/lib:<cdc_home>/lib/ts.jar UEDDLConvertSettings.java
javac -d ../bin -cp <cdc_home>/lib:<cdc_home>/lib/ts.jar UEDDLConvert.java
```

This will render 3 class files which can then be deployed on the CDC target engine.

(TODO: ANT BUILD)

## [Alternative] Compilation automation with Ant
This project supports build automation with [Ant](http://ant.apache.org/). To use configure build automation you must [download](http://ant.apache.org/bindownload.cgi) and [configure](http://ant.apache.org/manual/index.html) Ant on your machine.

Once you have setup Ant. You need to ask the developers for the libraries (and samples) necessary to run and test the scripts. These libraries cannot be posted here due to proprietary issues.

You can setup the place where the script will load the libraries from. For that simply go to the build.properties file and change the following property with the desired path.

```
cdc.home=c:\\cdc\\lib # change to your desired path
```
Afterwards open up the terminal and go to the project folder and build the project through Ant.

```terminal
> cd C:\dev\ddl_parser
> ant
Buildfile: C:\dev\ddl_parser\build.xml

init:
    [mkdir] Created dir: C:\dev\ddl_parser\build
    [mkdir] Created dir: C:\dev\ddl_parser\build\classes
    [mkdir] Created dir: C:\dev\ddl_parser\build\jar
    [mkdir] Created dir: C:\dev\ddl_parser\dist
    [mkdir] Created dir: C:\dev\ddl_parser\bin

check.dir:

compile-true:
     [echo] Found c:\cdc\lib. Getting jars.
    [javac] Compiling 5 source files to C:\dev\ddl_parser\bin

compile-false:

compile:

BUILD SUCCESSFUL
Total time: 4 seconds
```

You can also clean the .class files from the project if you so desire.

```terminal
> ant clean
Buildfile: C:\dev\ddl_parser\build.xml

clean:
   [delete] Deleting directory C:\dev\ddl_parser\build\classes
   [delete] Deleting directory C:\dev\ddl_parser\dist
   [delete] Deleting directory C:\dev\ddl_parser\bin
   [delete] Deleting directory C:\dev\ddl_parser\build

BUILD SUCCESSFUL
Total time: 0 seconds
```

If the libraries configuration is wrong in some way (no directory found or files non-exists), the ant compile will echo out one of the following errors.

```terminal
> ant
BUILD FAILED
C:\dev\ddl_parser\build.xml:24: c:\cdc\lib does not exist.

> ant
compile-false:
     [echo] Library at c:\cdc\lib is empty. Please request it to the developers.
```

## Implementation instructions
Implementing the user exit must be done through the following steps:

- Create the SP_DROP_TABLE_IF_EXISTS stored procedure for CDC target Oracle user; source can be
  found in UEStoredProcedure.sql

- Copy all .class files to the <cdc_home>/lib directory of the target engine

- Copy the UEDDLConvert.properties file to the <cdc_home> directory of the target engine

- Modify the properties to match your requirements

- Restart the CDC target engine
