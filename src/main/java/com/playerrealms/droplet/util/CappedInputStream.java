package com.playerrealms.droplet.util;

import java.io.IOException;
import java.io.InputStream;

public class CappedInputStream extends InputStream {

	private InputStream parent;

	private int cap, read;
	
	public CappedInputStream(InputStream parent, int cap) {
		this.parent = parent;
		this.cap = cap;
		read = 0;
	}
	
	@Override
	public int read() throws IOException {
		read++;
		if(read > cap) {
			parent.close();
			throw new CapExceededError();
		}
		return parent.read();
	}

	public static class CapExceededError extends IOException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7033618507118546828L;
		
	}
	
}
