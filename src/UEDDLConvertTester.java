import java.util.ArrayList;

public class UEDDLConvertTester {

	public static void main(String[] args) throws Exception {
		UEDDLConvert convert = new UEDDLConvert();
		convert.init();

		ArrayList<String> statements = new ArrayList<String>();

		statements
				.add(" CREATE TABLE \"RMS_MC\".\"ADDR\" \n"
						+ "   (\t\"ADDR_KEY\" NUMBER(6,0) NOT NULL ENABLE, \n"
						+ "\t\"MODULE\" VARCHAR2(4) NOT NULL ENABLE, \n"
						+ "\t\"KEY_VALUE_1\" VARCHAR2(20) NOT NULL ENABLE, \n"
						+ "\t\"KEY_VALUE_2\" VARCHAR2(20), \n"
						+ "\t\"SEQ_NO\" NUMBER(4,0) NOT NULL ENABLE, \n"
						+ "\t\"ADDR_TYPE\" VARCHAR2(2) NOT NULL ENABLE, \n"
						+ "\t\"PRIMARY_ADDR_IND\" VARCHAR2(1) NOT NULL ENABLE, \n"
						+ "\t\"ADD_1\" VARCHAR2(30) NOT NULL ENABLE, \n"
						+ "\t\"ADD_2\" VARCHAR2(30), \n"
						+ "\t\"ADD_3\" VARCHAR2(30), \n"
						+ "\t\"CITY\" VARCHAR2(20) NOT NULL ENABLE, \n"
						+ "\t\"STATE\" VARCHAR2(3), \n"
						+ "\t\"COUNTRY_ID\" VARCHAR2(3) NOT NULL ENABLE, \n"
						+ "\t\"POST\" VARCHAR2(10), \n"
						+ "\t\"CONTACT_NAME\" VARCHAR2(20), \n"
						+ "\t\"CONTACT_PHONE\" VARCHAR2(20), \n"
						+ "\t\"CONTACT_TELEX\" VARCHAR2(20), \n"
						+ "\t\"CONTACT_FAX\" VARCHAR2(20), \n"
						+ "\t\"CONTACT_EMAIL\" VARCHAR2(100), \n"
						+ "\t\"ORACLE_VENDOR_SITE_ID\" NUMBER(15,0), \n"
						+ "\t CONSTRAINT \"PK_ADDR\" PRIMARY KEY (\"ADDR_KEY\")\n"
						+ "  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS \n"
						+ "  STORAGE(INITIAL 1048576 NEXT 131072 MINEXTENTS 1 MAXEXTENTS 2147483645\n"
						+ "  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1\n"
						+ "  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)\n"
						+ "  TABLESPACE \"IDX001\"  ENABLE, \n"
						+ "\t CONSTRAINT \"CHK_ADDR_MODULE\" CHECK (module in ('SUPP','PTNR')\n"
						+ "     AND (module = 'PTNR' and key_value_1 in ( 'BK','FF','FA','AG','BR','IM','AP',\n"
						+ "                                               'CO','CN','S1', 'S2','S3', 'EV',\n"
						+ "                                               'IA','EC','ES' ) )\n"
						+ "      OR (module = 'PTNR' AND TO_NUMBER (key_value_1) = TRUNC (key_value_1) )\n"
						+ "      OR (module = 'SUPP' and key_value_2 is NULL)) ENABLE, \n"
						+ "\t CONSTRAINT \"ADR_CNT_FK\" FOREIGN KEY (\"COUNTRY_ID\")\n"
						+ "\t  REFERENCES \"RMS_MC\".\"COUNTRY\" (\"COUNTRY_ID\") ENABLE, \n"
						+ "\t CONSTRAINT \"ADR_STA_FK\" FOREIGN KEY (\"STATE\")\n"
						+ "\t  REFERENCES \"RMS_MC\".\"STATE\" (\"STATE\") ENABLE\n"
						+ "   ) SEGMENT CREATION IMMEDIATE \n"
						+ "  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 \n"
						+ " NOCOMPRESS LOGGING\n"
						+ "  STORAGE(INITIAL 1048576 NEXT 2097152 MINEXTENTS 1 MAXEXTENTS 2147483645\n"
						+ "  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1\n"
						+ "  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)\n"
						+ "  TABLESPACE \"DAT002\" ");

		statements
				.add("CREATE TABLE \"RMS_MC\".\"NB_DEPHEAD\"  (\"STORE\" NUMBER(4,0) NOT NULL ENABLE, \"SALES_DATE\" DATE NOT NULL ENABLE, \"REG_DATE\" DATE NOT NULL ENABLE, \"OBSERV\" VARCHAR2(20), "
						+ "\"STATUS\" VARCHAR2(1) NOT NULL ENABLE, \"GROSS_VALUE\" NUMBER(12,2), \"COMISSION\" NUMBER(12,2), "
						+ "\"USER_ID\" VARCHAR2(30), \"APPROVAL_DATE\" DATE, \"DW_STATUS\" VARCHAR2(1) NOT NULL ENABLE, "
						+ "CONSTRAINT \"NB_DEPHEAD_UK\" UNIQUE (\"STORE\", \"SALES_DATE\") "
						+ "USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS "
						+ "STORAGE(INITIAL 106496 NEXT 2097152 MINEXTENTS 1 MAXEXTENTS 2147483645 "
						+ "PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL "
						+ "DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT) "
						+ "TABLESPACE \"IDX002\" ENABLE, "
						+ "CONSTRAINT \"NB_DEPHEAD_ST_CHK\" CHECK (STATUS in ( 'A', 'I','E' )) ENABLE, "
						+ "CONSTRAINT \"NB_DEPHEAD_DW_STATUS_CHK\" CHECK (DW_STATUS in ( 'N', 'S','P' )) ENABLE, "
						+ "CONSTRAINT \"NB_DEPHEAD_ST_FK\" "
						+ "FOREIGN KEY (\"STORE\") REFERENCES \"RMS_MC\".\"STORE\" (\"STORE\") ENABLE) "
						+ "SEGMENT CREATION IMMEDIATE PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING "
						+ "STORAGE(INITIAL 106496 NEXT 131072 MINEXTENTS 1 MAXEXTENTS 2147483645 PCTINCREASE 0 FREELISTS 1 FREELIST "
						+ "GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)"
						+ " TABLESPACE \"DAT006\"");
		statements
				.add("CREATE INDEX \"RMS_MC\".\"NB_DEPHEAD_I1\" ON \"RMS_MC\".\"NB_DEPHEAD\" (\"USER_ID\") PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS STORAGE(INITIAL 131072 NEXT 2097152 MINEXTENTS 1 MAXEXTENTS 2147483645 PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1| BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)"
						+ "TABLESPACE \"IDX002\" "
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"STORE\" IS 'C�digo da Loja.'"
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"SALES_DATE\" IS 'Data em que foram realizadas as vendas.' "
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"REG_DATE\" IS 'Data da recolha dos dep�sitos pela empresa de transporte de valores.'"
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"OBSERV\" IS 'Por conven��o o nome de quem fez o lan�amento ou outra informa��o pertinente.' "
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"STATUS\" IS 'Status ( Approved, Initialized ).' "
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"GROSS_VALUE\" IS 'Somat�rio do valor l�quido de movimentos electr�nicos.' "
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"COMISSION\" IS 'Somat�rio do valor das comiss�es referentes aos movimentos electr�nicos.' "
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"USER_ID\" IS 'User ID que realizou o lan�amento.' "
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"APPROVAL_DATE\" IS 'Data de aprova��o do lan�amento.' "
						+ "COMMENT ON COLUMN \"RMS_MC\".\"NB_DEPHEAD\".\"DW_STATUS\" IS 'DataWarehouse Status (Sent or Not sent).' "
						+ "COMMENT ON TABLE \"RMS_MC\".\"NB_DEPHEAD\" IS 'Tabela que cont�m dep�sitos banc�rios de numer�rio (NR), cheques (CH), cr�ditos efectuados nas lojas - CREDIFIN (CF) e no Valor Total dos Fechos Contabil�sticos dos POS''s "
						+ "desse dia de Vendas assim como os Dep�sitos Banc�rios de Cheques Recuperados (CR).'");
		statements
				.add("GRANT SELECT ON \"RMS_MC\".\"NB_DEPHEAD\" TO \"TATA_RO\")");
		statements
				.add("GRANT SELECT ON \"RMS_MC\".\"NB_DEPHEAD\" TO \"RMSMCHRO\"");

		statements
				.add("grant insert on \"RMS_MC\".\"NB_DEPHEAD\" TO \"FCL_MC\" ");
		statements
				.add("create table cdcdemoa.prodrep (productid number(5,0),repno number(3,0), constraint fk_repno foreign key (repno) references cdcdemoa.salesrep (repno) enable)");
		statements
				.add("create table cdcdemoa.prodrep (productid number(5,0),repno number(3,0), constraint fk_repno foreign key (repno, repno2) references cdcdemoa.salesrep (repno, repno2))");
		statements.add("drop table cdcdemoa.prodrep");
		statements
				.add("CREATE TABLE \"RMS_MC\".\"ITEM_ZONE_PRICE\"    (	\"ITEM\" NUMBER(8,0) NOT NULL ENABLE, "
						+ "	\"ZONE_GROUP_ID\" NUMBER(4,0) NOT NULL ENABLE, 	\"ZONE_ID\" NUMBER(4,0) NOT NULL ENABLE, 	"
						+ "\"UNIT_RETAIL\" NUMBER(20,4) NOT NULL ENABLE, 	\"MULTI_UNITS\" NUMBER(12,4), 	\"MULTI_UNIT_RETAIL\" NUMBER(20,4), 	"
						+ "\"BASE_RETAIL_IND\" VARCHAR2(1) NOT NULL ENABLE, 	 "
						+ "CONSTRAINT \"PK_ITEM_ZONE_PRICE\" PRIMARY KEY (\"ITEM\", \"ZONE_GROUP_ID\", \"ZONE_ID\")  "
						+ "USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS   "
						+ "STORAGE(INITIAL 2097152 NEXT 2097152 MINEXTENTS 1 MAXEXTENTS 2147483645  PCTINCREASE 0 "
						+ "FREELISTS 1 FREELIST GROUPS 1  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)  "
						+ "TABLESPACE \"IDX003\"  ENABLE, 	\n "
						+ "CONSTRAINT \"CHK_ITEM_ZONE_PRC_BASE_RET_IND\" CHECK (BASE_RETAIL_IND IN('Y', 'N')) ENABLE, 	 \n"
						+ "SUPPLEMENTAL LOG DATA (ALL) COLUMNS, 	 "
						+ "CONSTRAINT \"IZP_PZN_FK\" FOREIGN KEY (\"ZONE_GROUP_ID\", \"ZONE_ID\")	  "
						+ "REFERENCES \"RMS_MC\".\"PRICE_ZONE\" (\"ZONE_GROUP_ID\", \"ZONE_ID\") ENABLE   ) "
						+ "SEGMENT CREATION IMMEDIATE   PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255  "
						+ "NOCOMPRESS LOGGING  STORAGE(INITIAL 2097152 NEXT 2097152 MINEXTENTS 1 MAXEXTENTS 2147483645  "
						+ "PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1  BUFFER_POOL DEFAULT "
						+ "FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)  TABLESPACE \"DAT006\"");
		statements
				.add("CREATE TABLE \"RMS_MC\".\"CHECK_CONSTRAINT_TEST\"    (	\"ITEM\" NUMBER(8,0) NOT NULL ENABLE, "
						+ "	\"ZONE_GROUP_ID\" NUMBER(4,0) NOT NULL ENABLE, 	\"ZONE_ID\" NUMBER(4,0) NOT NULL ENABLE, 	"
						+ "\"UNIT_RETAIL\" NUMBER(20,4) NOT NULL ENABLE, 	\"MULTI_UNITS\" NUMBER(12,4), 	\"MULTI_UNIT_RETAIL\" NUMBER(20,4), 	"
						+ "\"BASE_RETAIL_IND\" VARCHAR2(1) NOT NULL ENABLE, 	\n "
						+ "CONSTRAINT \"PK_ITEM_ZONE_PRICE\" PRIMARY KEY (\"ITEM\", \"ZONE_GROUP_ID\", \"ZONE_ID\")  "
						+ "USING INDEX   \n "
						+ "TABLESPACE \"IDX003\"  ENABLE, 	\n "
						+ "CONSTRAINT \"CHK_ITEM_ZONE_PRC_BASE_RET_IND\" CHECK (BASE_RETAIL_IND IN('Y', 'N')), 	 \n "
						+ "CONSTRAINT \"CHK_ITEM_ZONE_ZONE\" CHECK (ZONE_ID < 9990), 	 \n"
						+ "SUPPLEMENTAL LOG DATA (ALL) COLUMNS, 	 \n"
						+ "CONSTRAINT \"IZP_PZN_FK\" FOREIGN KEY (\"ZONE_GROUP_ID\", \"ZONE_ID\")	  "
						+ "REFERENCES \"RMS_MC\".\"PRICE_ZONE\" (\"ZONE_GROUP_ID\", \"ZONE_ID\") ENABLE   ) "
						+ "SEGMENT CREATION IMMEDIATE   PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255  "
						+ "NOCOMPRESS LOGGING  STORAGE(INITIAL 2097152 NEXT 2097152 MINEXTENTS 1 MAXEXTENTS 2147483645  "
						+ "PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1  BUFFER_POOL DEFAULT "
						+ "FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)  TABLESPACE \"DAT006\"");
		statements
				.add("ALTER TABLE \"RMS_MC\".\"ITEM_ZONE_PRICE\""
						+ "ADD CONSTRAINT \"IZP_PZN_FK\" FOREIGN KEY (\"ZONE_GROUP_ID\", \"ZONE_ID\")	  "
						+ "REFERENCES \"RMS_MC\".\"PRICE_ZONE\" (\"ZONE_GROUP_ID\", \"ZONE_ID\") ENABLE   )");
		try {
			for (String statement : statements) {
				System.out.println("***********INPUT***********");
				System.out.println(statement);
				System.out.println("***********OUTPUT***********");
				System.out.println(convert.modifyStatement(statement,
						"2015-09-27", "CDCDEMOA", "CDCDEMOA", "PRODREP"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
