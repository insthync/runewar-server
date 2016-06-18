package com.runewar.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.runewar.GlobalVariables;
import com.runewar.config.Configuration;
import com.runewar.database.MySQL;
import com.runewar.misc.GameExp;
import com.runewar.misc.StreamStringConversion;


public class Server extends ServerClasses {

	private ClientAcceptThread acceptThread = null;
	public Server() {
		Init();
	}
	
	protected void Init() {
		try {
			clients = new HashMap<String, Client>();
			initializingClients = new ArrayList<Client>();
			config = new Configuration();
			handler = new Handler(this, clients);
			db = new MySQL(config.getServerConfig().dbhost, config.getServerConfig().dbport, config.getServerConfig().dbuser, config.getServerConfig().dbpass, config.getServerConfig().dbname);
			acceptThread = new ClientAcceptThread(this);
			readExpConfig();
			initialized = true;
		} catch (Exception ex) {
			System.err.println("Errors in Init from Server...");
			ex.printStackTrace();
			initialized = false;
		}
	}
	
	private void readExpConfig() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(config.getServerConfig().service_url + "game_exp.php");
		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release
			if (entity != null) {
				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = StreamStringConversion.convertStreamToString(instream);
				// Closing the input stream will trigger connection release
				instream.close();
				Gson gson = new Gson();
				GameExp gameExp = gson.fromJson(result, GameExp.class);
				String[] splitedPlayerExp = gameExp.player_exp.split(",");
				GlobalVariables.player_exp = new int[splitedPlayerExp.length];
				String[] splitedGainExp = gameExp.gain_exp.split(",");
				GlobalVariables.gain_exp = new int[splitedGainExp.length];
				for (int i = 0; i < splitedPlayerExp.length; ++i) {
					GlobalVariables.player_exp[i] = Integer.parseInt(splitedPlayerExp[i]);
				}
				for (int i = 0; i < splitedGainExp.length; ++i) {
					GlobalVariables.gain_exp[i] = Integer.parseInt(splitedGainExp[i]);
				}
			}
		} catch (ClientProtocolException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void Start() {
		int port = config.getServerConfig().port;
		if (initialized) {
			available = true;
			try {
				System.out.println("Server listening port: " + port);
				serverSocket = new ServerSocket(port);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			acceptThread.Start();
			handler.Start();
			System.out.println("Server started.");
		}
	}
	
	public void Stop() {
		if (initialized) {
			available = false;
			try {
				serverSocket.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			acceptThread.Stop();
			handler.Stop();
			try {
				for (Client client : initializingClients) {
					initializingClients.remove(client);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				for (Client client : clients.values()) {
					client.Stop();
					clients.remove(client.ID);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.out.println("Server stopped.");
		}
	}
}
