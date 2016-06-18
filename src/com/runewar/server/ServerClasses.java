package com.runewar.server;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;

import com.runewar.config.Configuration;
import com.runewar.database.MySQL;


public abstract class ServerClasses {
	protected boolean available = false;
	protected boolean initialized = false;
	protected ServerSocket serverSocket;
	protected MySQL db;
	protected HashMap<String, Client> clients;
	protected ArrayList<Client> initializingClients;
	protected Configuration config;
	protected Handler handler;
	public boolean IsAvailable() {
		return available;
	}
	public boolean IsInitialized() {
		return initialized;
	}
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	public MySQL getDatabase() {
		return db;
	}
	public HashMap<String, Client> getClients() {
		return clients;
	}
	public ArrayList<Client> getInitializingClients() {
		return initializingClients;
	}
	public Configuration getConfig() {
		return config;
	}
	public Handler getHandler() {
		return handler;
	}
	protected abstract void Init();
	public abstract void Start();
	public abstract void Stop();
}
