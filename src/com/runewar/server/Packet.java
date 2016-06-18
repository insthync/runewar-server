package com.runewar.server;

import java.io.*;
import java.net.*;

public class Packet {
	public final Socket socket;
	public BufferedReader in; 
	public PrintWriter out; 
	public Packet(Socket socket) {
		this.socket = socket;
		
		try {
			this.out = new PrintWriter(socket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.socket.setSoTimeout(30000); // Set time out to 30 seconds
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

    public void Connect(SocketAddress ip, int port)
    {
        try {
			socket.connect(ip, port);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
    }

    public void Close()
    {
        try {
			socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
    }

    public Boolean isConnected()
    {
        return socket.isConnected();
    }

    public Boolean isClosed()
    {
        return socket.isClosed();
    }
}
