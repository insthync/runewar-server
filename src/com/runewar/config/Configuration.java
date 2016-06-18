package com.runewar.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Configuration {
	private ServerConfiguration serverCfg = null;
	private final String main_cfgfile = "conf/server.conf";
	public Configuration() {
		serverCfg = new ServerConfiguration();
		// File Reader
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(main_cfgfile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Error file \"" + main_cfgfile + "\" not found!, while reading configuration file");
			return;
		}
		if (scanner != null) {
			while (scanner.hasNextLine()) {
				readServerConfig(scanner.nextLine());
			}
			scanner.close();
		}
		System.out.println("Configuration initialized...");
	}
	public ServerConfiguration getServerConfig() {
		return serverCfg;
	}
	private void readServerConfig(String message) {
        String[] line = message.split(";");
        for (int i = 0; i < line.length; ++i ) {
        	String[] msg = line[i].split(":", 2);
        	// server cfg
           	if (msg[0].equals("port") && msg.length == 2) {
           		this.serverCfg.port = Integer.valueOf(msg[1]);
           	}
           	if (msg[0].equals("name") && msg.length == 2) {
           		this.serverCfg.name = msg[1];
           	}
           	if (msg[0].equals("dbhost") && msg.length == 2) {
           		this.serverCfg.dbhost = msg[1];
           	}
           	if (msg[0].equals("dbport") && msg.length == 2) {
           		this.serverCfg.dbport = Integer.valueOf(msg[1]);
           	}
           	if (msg[0].equals("dbuser") && msg.length == 2) {
           		this.serverCfg.dbuser = msg[1];
           	}
           	if (msg[0].equals("dbpass") && msg.length == 2) {
           		this.serverCfg.dbpass = msg[1];
           	}
           	if (msg[0].equals("dbname") && msg.length == 2) {
           		this.serverCfg.dbname = msg[1];
           	}
           	if (msg[0].equals("service_url") && msg.length == 2) {
           		this.serverCfg.service_url = msg[1];
           	}
        }
	}
}
