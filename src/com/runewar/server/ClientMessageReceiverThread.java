package com.runewar.server;

import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ClientMessageReceiverThread extends ServerClassesThread {
	private Client client = null;
	public ClientMessageReceiverThread(Client client) {
		this.client = client;
		this.initialized = true;
	}
	public synchronized void run() {
		while (available) {
			Packet packet = client.getPacket();
	        if (packet.isConnected() && !packet.isClosed()) {
	            try {
	            	String readLine;
	            	// Receiving message.
	            	if (packet.in != null && (readLine = packet.in.readLine()) != null) {
	            		ClientMessageProcess.Process(client, readLine);
	            	}
	            } catch (SocketTimeoutException ex) {
	            	ex.printStackTrace();
	            	client.Stop();
		        	ThreadStopper threadStopper = new ThreadStopper(this);
		        	threadStopper.start();
		        	return;
				} catch (SocketException ex) {
	            	ex.printStackTrace();
	            	client.Stop();
		        	ThreadStopper threadStopper = new ThreadStopper(this);
		        	threadStopper.start();
		        	return;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
	        } else {
	            client.Stop();
	        	ThreadStopper threadStopper = new ThreadStopper(this);
	        	threadStopper.start();
	        	return;
	        }
		}
	}
	public void Start() {
		if (initialized) {
			super.Start();
			this.available = true;
		}
	}
	public void Stop() {
		if (initialized) {
			super.Stop();
			this.available = false;
		}
	}
	private class ThreadStopper extends Thread implements Runnable {
		private ServerClassesThread thread = null;
		public ThreadStopper(ServerClassesThread thread) {
			this.thread = thread;
		}
		public synchronized void run() {
			try {
				Thread.sleep(1000); // delay 1000 ms
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}
			thread.Stop();
		}
	}
}
