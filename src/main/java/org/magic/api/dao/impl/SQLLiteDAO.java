package org.magic.api.dao.impl;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.magic.api.interfaces.abstracts.extra.AbstractMagicSQLDAO;
import org.magic.services.MTGConstants;
import org.magic.services.tools.FileTools;

public class SQLLiteDAO extends AbstractMagicSQLDAO {

	@Override
	public Map<String,Long> getDBSize() {

		var map = new HashMap<String,Long>();
			map.put("file",FileTools.sizeOf(getFile(SERVERNAME)));
			return map;
	}

	@Override
	public Map<String, String> getDefaultAttributes() {

		var m = super.getDefaultAttributes();

		m.put(SERVERNAME, Paths.get(MTGConstants.DATA_DIR.getAbsolutePath(),"sqlite-db").toFile().getAbsolutePath());
		m.put(LOGIN, "SA");
		m.put(DB_NAME, "");

		return m;
	}

	@Override
	protected String getdbSizeQuery() {
		return null;
	}

	@Override
	public String getName() {
		return "SQLite";
	}

	@Override
	protected String getAutoIncrementKeyWord() {
		return "INTEGER";// primary key column is autoincrement
	}

	@Override
	protected String getjdbcnamedb() {
		return getName().toLowerCase();
	}

	@Override
	protected String beanStorage() {
		return "json";
	}


	@Override
	protected String longTextStorage() {
		return "TEXT";
	}

	@Override
	protected String createListStockSQL() {
		return "select * from stocks where collection=? and JSON_EnumExtraCT(mcard,'$.name')=?";

	}

}
