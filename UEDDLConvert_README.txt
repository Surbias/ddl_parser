UEDDLConvert User Exit description
----------------------------------

Authors:	Filipe Almeida (filipe.almeida@softinsa.com)
			Frank Ketelaars (fketelaars@nl.ibm.com)
			
Change log
----------
2015-08-19	Initial delivery to customer
2015-09-23  Fixed issue with foreign key with cascade delete, now also properly removed
            Added logging for the substrings which are removed and statements which are suppressed
            Fixed issue with the removal of check constraints
            Added suppression of ALTER TABLE ADD CONSTRAINT CHECK
2015-09-24  Fixed logging of event when resulting statement is suppressed (null)
2015-09-25  Also match newline characters in CHECK constraint
2015-09-28  Also check constraint with VALIDATE/NOVALIDATE clause
2015-09-30  Remove SUPPLEMENTAL LOG GROUP clauses
2015-10-01  Optionally replace VARCHAR2(nn) by VARCHAR2(nn CHAR) to accommodate special characters
            when converted to UTF-8
2015-10-08	Suppress statement ALTER TABLE SHRINK SPACE
            Create generic stored procedure that will execute DDL only if object exists, applied for
            DROP TABLE, DROP INDEX, ALTER INDEX
            Suppress the TABLESPACE "_$deleted$xxx" clause
2015-10-12	Suppress statement ALTER INDEX SHRINK SPACE
            New property: suppressTables. In this property you can specify tables for which DDLs must be suppressed. This
            can be used for example to let CDC continue after a DDL for a certain table
2015-10-13	Suppress statement CREATE BITMAP INDEX
			Remove CONSTRAINT FOREIGN KEY ON DELETE SET NULL clauses

Description
-----------
This user exit facilitates converting DDL statements before they are executed against the target database and is to
be used in a rule-based replication definition. At the time of initial delivery, the user exit transforms the following
DDL statements:
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

Usage instructions
------------------
In the CDC Management Console, right-click on the rule-based subscription and select "User exit...", then 
specify the name UEDDLConvert.

Compilation instructions
------------------------
The user exit has a dependency on the ts.jar file. To compile, do the following:
cd src
javac -d ../bin -cp <cdc_home>/lib:<cdc_home>/lib/ts.jar UETrace.java 
javac -d ../bin -cp <cdc_home>/lib:<cdc_home>/lib/ts.jar UEDDLConvertSettings.java 
javac -d ../bin -cp <cdc_home>/lib:<cdc_home>/lib/ts.jar UEDDLConvert.java

This will render 3 class files which can then be deployed on the CDC target engine. 

Implementation instructions
---------------------------
Implementing the user exit must be done through the following steps:
- Create the SP_DROP_TABLE_IF_EXISTS stored procedure for CDC target Oracle user; source can be 
  found in UEStoredProcedure.sql
- Copy all .class files to the <cdc_home>/lib directory of the target engine
- Copy the UEDDLConvert.properties file to the <cdc_home> directory of the target engine
- Modify the properties to match your requirements
- Restart the CDC target engine
