package com.runewar.server;

import java.io.IOException;
import java.net.Socket;

public class ClientAcceptThread extends ServerClassesThread {
	private Server server = null;
	public ClientAcceptThread(Server server) {
		this.server = server;
		this.initializingClients = server.getInitializingClients();
		this.initialized = true;
	}
	public void run() {
		while (available) {
			try {
				Thread.sleep(500); // delay 500 ms
			} catch(InterruptedException ex) {
				ex.printStackTrace();
			}
			synchronized (initializingClients) {
				Socket clientSocket;
				try {
					clientSocket = server.getServerSocket().accept();
					Client client = new Client(server, clientSocket);
					client.Start();
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			// waiting for client
		}
	}
	public void Start() {
		available = true;
		super.Start();
	}
	public void Stop() {
		available = false;
		super.Stop();
	}
}
