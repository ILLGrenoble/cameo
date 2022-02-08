package fr.ill.ics.cameo.base;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import fr.ill.ics.cameo.base.impl.RequestSocketImpl;
import fr.ill.ics.cameo.factory.ImplFactory;
import fr.ill.ics.cameo.messages.JSON.Parser;
import fr.ill.ics.cameo.messages.Messages;

public class RequestSocket {

	private RequestSocketImpl impl;
	private Parser parser;

	public RequestSocket(Context context, int timeout, Parser parser) {
		this.impl = ImplFactory.createRequestSocket(context, timeout);
		this.parser = parser;
	}
			
	public void setTimeout(int timeout) {
		this.impl.setTimeout(timeout);
	}

	public void connect(String endpoint, String responderIdentity) {
		impl.connect(endpoint, responderIdentity);
	}
	
	public byte[][] request(byte[] part1) {
		 return impl.request(part1, -1);
	}
	
	public byte[][] request(byte[] part1, byte[] part2) {
		 return impl.request(part1, part2, -1);
	}
	
	public byte[][] request(byte[] part1, byte[] part2, byte[] part3) {
		 return impl.request(part1, part2, part3, -1);
	}
	
	public JSONObject requestJSON(JSONObject request, int timeout) throws ConnectionTimeout {
		
		byte[][] reply = impl.request(Messages.serialize(request), timeout);
		
		try {
			return parser.parse(Messages.parseString(reply[0]));
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public JSONObject requestJSON(JSONObject request) throws ConnectionTimeout {
		return requestJSON(request, -1);
	}
	
	public JSONObject requestJSON(JSONObject request, byte[] data, int timeout) throws ConnectionTimeout {
		
		byte[][] reply = impl.request(Messages.serialize(request), data, timeout);
		
		try {
			return parser.parse(Messages.parseString(reply[0]));
		}
		catch (ParseException e) {
			throw new UnexpectedException("Cannot parse response");
		}
	}
	
	public JSONObject requestJSON(JSONObject request, byte[] data) throws ConnectionTimeout {
		return requestJSON(request, data, -1);
	}
	
	public void terminate() {
		impl.terminate();
	}
}
