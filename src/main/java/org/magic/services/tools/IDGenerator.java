package org.magic.services.tools;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.Logger;
import org.magic.api.beans.MagicCard;
import org.magic.services.logging.MTGLogger;

public class IDGenerator {

	static Logger logger = MTGLogger.getLogger(IDGenerator.class);

	private IDGenerator() {
	}

	public static String generateMD5(String s)
	{
		return DigestUtils.md5Hex(s).toUpperCase();
	}

	public static String generateSha256(String s)
	{
		return DigestUtils.sha256Hex(s).toUpperCase();
	}


	public static String generate(MagicCard mc) {
		
		try {
		var ed = mc.getCurrentSet();
		
		String number=ed.getNumber();


		if(number!=null&&number.isEmpty() )
			number=null;

		var id = String.valueOf((mc.getName() + ed + number + ed.getMultiverseid()));
		id = DigestUtils.sha1Hex(id);

		logger.trace("Generate ID for {}|{}|{}|{}->:{}",mc.getName(),ed,number,ed.getMultiverseid(),id);

		return id;
		
		}catch(Exception e)
		{
			logger.error("Error generating ID for {}",mc,e);
			return "";
		}
		
	}


}
