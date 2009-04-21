package org.mondemand.transport;

import java.io.IOException;
import java.net.InetAddress;

import org.mondemand.Context;
import org.mondemand.LogMessage;
import org.mondemand.StatsMessage;
import org.mondemand.Transport;
import org.mondemand.TransportException;

import org.lwes.Event;
import org.lwes.EventSystemException;
import org.lwes.emitter.EventEmitter;
import org.lwes.emitter.MulticastEventEmitter;
import org.lwes.emitter.UnicastEventEmitter;

public class LWESTransport implements Transport {
	/********************
	 * Constants        *
	 ********************/
	private static final String LOG_EVENT = "MonDemand::LogMsg";
	private static final String STATS_EVENT = "MonDemand::StatsMsg";
	
	/***********************
	 * Instance attributes *
	 ***********************/
	private EventEmitter emitter = null;

	/**
	 * Creates an initializes a LWES transport.
	 * @param address the address to send events to, can be a multicast or unicast address
	 * @param port the port to send events to
	 * @param networkInterface the network interface to use, or null to specify the default
	 * @throws TransportException
	 */
	public LWESTransport(InetAddress address, int port, InetAddress networkInterface) throws TransportException {
		this(address, port, networkInterface, 1);
	}

	/**
	 * Creates and initializes a LWES transport.
	 * @param address the address to send events to, can be a multicast or unicast address
	 * @param port the port to send events to
	 * @param networkInterface the network interface to use, or null to specify the default
	 * @param ttl for multicast addresses, the TTL value to use
	 * @throws TransportException
	 */
	public LWESTransport(InetAddress address, int port, InetAddress networkInterface, int ttl) throws TransportException {
		if(address == null) return;
		
		// detect the address type and initialize accordingly
		if(address.isMulticastAddress()) {
			MulticastEventEmitter m = new MulticastEventEmitter();
			m.setMulticastAddress(address);
			m.setMulticastPort(port);
			m.setInterface(networkInterface);
			m.setTimeToLive(ttl);
			emitter = m;
		} else {
			UnicastEventEmitter u = new UnicastEventEmitter();
			u.setAddress(address);
			u.setPort(port);
			emitter = u;
		}
	
		try {
			emitter.initialize();
		} catch(Exception e) {
			throw new TransportException("Unable to inialize emitter", e);
		}
	}

	public void sendLogs(String programId, LogMessage[] messages, Context[] contexts) throws TransportException {
		if(messages == null || contexts == null || emitter == null) return;

		try {
			// create the event and set parameters
			Event logMsg = emitter.createEvent(LOG_EVENT, false);
			logMsg.setString("prog_id", programId);
			logMsg.setUInt16("num", messages.length);
		
			// for each log message, set the appropriate fields
			for(int i=0; i<messages.length; ++i) {
				logMsg.setString("f" + i, messages[i].getFilename());
				logMsg.setUInt32("l" + i, messages[i].getLine());
				logMsg.setUInt32("p" + i, messages[i].getLevel());
				logMsg.setString("m" + i, messages[i].getMessage());
				
				if(messages[i].getRepeat() > 1) {
					logMsg.setUInt16("r" + i, messages[i].getRepeat());
				}
			}
			
			// set the contextual data in the event
			if(contexts.length > 0) {
				logMsg.setUInt16("ctxt_num", contexts.length);
				for(int i=0; i<contexts.length; ++i) {
					logMsg.setString("ctxt_k" + i, contexts[i].getKey());
					logMsg.setString("ctxt_v" + i, contexts[i].getValue());
				}
			}
			
			// emit the event
			emitter.emit(logMsg);
		} catch(EventSystemException ese) {
			throw new TransportException("Unable to create event", ese);
		} catch(Exception e) {
			throw new TransportException("Error sending log event", e);
		}
	}

	public void sendStats(String programId, StatsMessage[] messages, Context[] contexts) throws TransportException {
		if(messages == null || contexts == null || emitter == null) return;

		try {
			// create the event
			Event statsMsg = emitter.createEvent(STATS_EVENT, false);
			statsMsg.setString("prog_id", programId);
			statsMsg.setUInt16("num", messages.length);
		
			// for each statistic, set the values
			for(int i=0; i<messages.length; ++i) {
				statsMsg.setString("k" + i, messages[i].getKey());
				statsMsg.setInt64("l" + i, messages[i].getCounter());
			}
			
			// set the contextual data in the event
			if(contexts.length > 0) {
				statsMsg.setUInt16("ctxt_num", contexts.length);
				for(int i=0; i<contexts.length; ++i) {
					statsMsg.setString("ctxt_k" + i, contexts[i].getKey());
					statsMsg.setString("ctxt_v" + i, contexts[i].getValue());
				}
			}
			
			// emit the event
			emitter.emit(statsMsg);
		} catch(EventSystemException ese) {
			throw new TransportException("Unable to create event", ese);
		} catch(Exception e) {
			throw new TransportException("Error sending log event", e);
		}
	}

	public void shutdown() throws TransportException {
		try {
			emitter.shutdown();
		} catch(IOException e) {
			throw new TransportException("Unable to shutdown emitter");
		}
	}

}
