package org.magic.api.network.impl;
import java.awt.Color;
import java.io.IOException;
import java.time.Instant;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.apache.activemq.artemis.api.core.client.ClientConsumer;
import org.apache.activemq.artemis.api.core.client.ClientMessage;
import org.apache.activemq.artemis.api.core.client.ClientProducer;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.Logger;
import org.magic.api.beans.JsonMessage;
import org.magic.api.beans.JsonMessage.MSG_TYPE;
import org.magic.api.exports.impl.JsonExport;
import org.magic.api.interfaces.MTGNetworkClient;
import org.magic.game.model.Player;
import org.magic.game.model.Player.STATUS;
import org.magic.services.logging.MTGLogger;

public class ActiveMQNetworkClient implements MTGNetworkClient {

	private ClientSession session;
	private ClientProducer producer;
	private ClientConsumer consumer;
	private ClientSessionFactory factory;
	private ServerLocator locator;
	private Player player;
	private Logger logger = MTGLogger.getLogger(ActiveMQNetworkClient.class);
	private JsonExport serializer = new JsonExport();
	
	@Override
	public void join(Player p, String url,String adress) throws IOException {
		this.player = p;
		player.setOnlineConnectionTimeStamp(Instant.now().toEpochMilli());
		player.setState(STATUS.CONNECTED);
		player.setId(RandomUtils.nextLong());
		try {
			locator = ActiveMQClient.createServerLocator(url);
		} catch (Exception e) {
			throw new IOException(e); 
		}
		
		try {
			
			factory=  locator.createSessionFactory();
		} catch (Exception e) {
			throw new IOException(e); 
		}
		try {
			session = factory.createSession(p.getName(),"password",false,true,true,true, 0, "ID-"+player.getId());
			
		} catch (ActiveMQException e) {
			throw new IOException(e); 
		}
		
		try {
			session.start();
		} catch (ActiveMQException e) {
			throw new IOException(e);
		}
		
		switchAddress(adress);
		
		sendMessage(new JsonMessage(player,"connected",Color.black,MSG_TYPE.CONNECT));
		
	}
	
	@Override
	public void changeStatus(STATUS selectedItem) throws IOException {
		player.setState(selectedItem);
		sendMessage(new JsonMessage(player,selectedItem.name(),Color.black,MSG_TYPE.CHANGESTATUS));
	}

	@Override
	public void switchAddress(String adress) throws IOException {
		try {
			producer = session.createProducer(adress);
		} catch (ActiveMQException e) {
			throw new IOException(e);
		}

		try {
			var cqc = createQueueConf(adress);
			session.createQueue(cqc);
			consumer = session.createConsumer(cqc.getName());
		} catch (ActiveMQException e) {
			throw new IOException(e);
		}
	}
	

	private QueueConfiguration createQueueConf(String adress) {
		var cqc = new QueueConfiguration();
		cqc.setAddress(adress);
		cqc.setName("queue-"+player.getId());
		cqc.setDurable(true);
		cqc.setAutoCreated(true);
		cqc.setConfigurationManaged(true);
		cqc.setRoutingType(RoutingType.MULTICAST);
		cqc.setAutoCreateAddress(true);
		
		return cqc;
	}

	@Override
	public JsonMessage consume() throws IOException {
		
		ClientMessage msg;
		try {
			msg = consumer.receive();
			logger.debug("consume {}",msg);
		} catch (ActiveMQException e) {
			throw new IOException(e);
		}
		
		if(msg==null)
			return null;
		
		return   serializer.fromJson(msg.getBodyBuffer().readString(),JsonMessage.class);
	}

	

	@Override
	public void sendMessage(JsonMessage obj) throws IOException {
		var message = session.createMessage(obj.getTypeMessage()==MSG_TYPE.TALK);
		var jsonMsg = serializer.toJson(obj);
		message.getBodyBuffer().writeString(jsonMsg);
		
		try {
			producer.send(message);
			logger.debug("send {}",obj);
		} catch (ActiveMQException e) {
			throw new IOException(e);
		}		
	}
	 
	
	
	@Override
	public void sendMessage(String text, Color c) throws IOException {
	
		sendMessage(new JsonMessage(player,text,c,MSG_TYPE.TALK));
		
	}

	@Override
	public void logout() throws IOException {
		try {
			sendMessage(new JsonMessage(player,"disconnect",Color.black,MSG_TYPE.DISCONNECT));
			session.close();
		} catch (ActiveMQException e) {
			throw new IOException(e);
		}
		locator.close();
		factory.close();
	
		try {
			producer.close();
		} catch (ActiveMQException e) {
			throw new IOException(e);
		}
		try {
			consumer.close();
		} catch (ActiveMQException e) {
			throw new IOException(e);
		}
		
	}

	@Override
	public boolean isActive() {
		return session!=null && !session.isClosed();
	}

	public Player getPlayer() {
		return player;
	}




}
