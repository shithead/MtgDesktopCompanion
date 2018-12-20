package org.magic.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.magic.api.beans.MagicCard;
import org.magic.api.beans.MagicEdition;
import org.magic.api.beans.OrderEntry;
import org.magic.api.beans.OrderEntry.TYPE_TRANSACTION;
import org.magic.api.exports.impl.JsonExport;

public class FinancialBookService {

	private File tamponFile;
	private List<OrderEntry> entries;
	protected Logger logger = MTGLogger.getLogger(this.getClass());
	protected JsonExport serializer;
	
	
	
	public FinancialBookService() {
		entries = new ArrayList<>();
		serializer= new JsonExport();
	}
	
	public List<OrderEntry> getOrderFor(MagicCard mc)
	{
		return getEntries().stream().filter(o->o.getDescription().toLowerCase().contains(mc.getName().toLowerCase())).collect(Collectors.toList());
	}
	
	public List<OrderEntry> getOrderBetween(Date start,Date end)
	{
		return getEntries().stream().filter(o->o.getTransationDate().after(start) && o.getTransationDate().before(end)).collect(Collectors.toList());
	}
	
	public List<OrderEntry> getOrderAt(Date d)
	{
		return getEntries().stream().filter(o->o.getTransationDate().equals(d)).collect(Collectors.toList());
	}
	
	
	public List<OrderEntry> getOrderFor(MagicEdition ed)
	{
		return getEntries().stream().filter(o->o.getEdition()!=null && o.getEdition().equals(ed)).collect(Collectors.toList());
	}

	public List<OrderEntry> getOrderFor(TYPE_TRANSACTION type)
	{
		return getEntries().stream().filter(o->o.getTypeTransaction()==type).collect(Collectors.toList());
	}

	public double getTotal(List<OrderEntry> order)
	{
		return order.stream().filter(o->o.getTypeTransaction()==TYPE_TRANSACTION.BUY).mapToDouble(OrderEntry::getItemPrice).sum()-order.stream().filter(o->o.getTypeTransaction()==TYPE_TRANSACTION.SELL).mapToDouble(OrderEntry::getItemPrice).sum();
	}
	
	public List<OrderEntry> getEntries()
	{
		return entries;
	}
	
	public List<Date> getOrdersDate()
	{
		Set<Date> d = new HashSet<>();
		getEntries().forEach(o->d.add(o.getTransationDate()));
		return new ArrayList<>(d);
	}
	
	public void loadFinancialBook()
	{
		loadFinancialBook(Paths.get(MTGConstants.DATA_DIR.getAbsolutePath(), "financialBook.json").toFile());
	}
	
	
	public void loadFinancialBook(File tamponFile)
	{
		this.tamponFile=tamponFile;
		if(tamponFile.exists())
		{
			try {
				entries= serializer.fromJsonList(FileUtils.readFileToString(tamponFile,MTGConstants.DEFAULT_ENCODING),OrderEntry.class);
			} catch (IOException e) {
				logger.error("error loading " + tamponFile,e);
			}
		}
		else
		{
			logger.error(tamponFile + " doesn't exist");
		}
	}

	public void saveBook(List<OrderEntry> items) throws IOException {
		
	/*	int i=1;
		for(OrderEntry e : items)
			e.setId(i++);
		*/
		
		FileUtils.write(tamponFile, serializer.toJson(items),MTGConstants.DEFAULT_ENCODING.displayName());
	}
	

	
}
