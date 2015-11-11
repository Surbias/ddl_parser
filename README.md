
# UEDDLConvert User Exit description

## Authors:
 - Filipe Almeida (filipe.almeida@softinsa.com)
 - Frank Ketelaars (fketelaars@nl.ibm.com)

## Description
This user exit facilitates converting DDL statements before they are executed against the target database and is to be used in a rule-based replication definition. The user exit transforms the following DDL statements:

- CREATE TABLE: Remove foreign key constraints, remove check constraints

- ALTER TABLE ADD CONSTRAINT FOREIGN KEY: Suppress

- ALTER TABLE ADD CONSTRAINT CHECK: Suppress

- DROP TABLE: Change to call stored procedure SP_EXECUTE_IF_EXISTS

- GRANT...ON...TO: Suppress

- CREATE INDEX: Suppress (non-unique key index creations are suppressed)

- CREATE TABLE: Change to call stored procedure SP_CREATE_TABLE which executes the CREATE TABLE and then a series of DDL statements defined for the table

The user exit can be activated by specifying it at the subscription level; it handles only DDL events and is
invoked before the DDL statement is executed on the target while there is still a chance to transform or suppress
the DDL statement in question.

At subscription initialization, the user exit reads the UEDDLConvert.properties file which controls the behaviour of
the user exit such as whether DDL statements are transformed and in which directory the DDL statements are logged.

## Usage instructions
In the CDC Management Console, right-click on the rule-based subscription and select "User exit...", then specify the name UEDDLConvert.

## Compilation instructions (using Ant)

This project supports build automation with [Ant](http://ant.apache.org/). To use configure build automation you must [download](http://ant.apache.org/bindownload.cgi) and [configure](http://ant.apache.org/manual/index.html) Ant on your machine.

Once you have setup Ant. You need to ask the developers for the libraries (and samples) necessary to run and test the scripts. These libraries cannot be posted here due to proprietary issues.

You can setup the place where the script will load the cdc library and the usual java libraries from. For that simply go to the build.properties file and change the following properties with the desired paths.

```
cdc.home=c:\\cdc\\lib # change to your desired path
apache.commons=c:\\cdc\\apache_commons
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

- Create the stored procedures and post-create DDL table for CDC target Oracle user; source can be
  found in UEStoredProcedure.sql

- Copy all .class files to the <cdc_home>/lib/user directory of the target engine
- Copy the Apache commons jar files to the <cdc_home>/lib/user directory of the target engine: commons-configuration-1.9.jar, commons-lang-2.6.jar, commons-logging-1.1.3.jar
- Edit the <cdc_home>/conf/system.cp and append the following paths to the classpath: lib/user/*.jar:lib/user

- Copy the UEDDLConvert.properties file to the <cdc_home> directory of the target engine

- Modify the properties to match your requirements

- Restart the CDC target engine
