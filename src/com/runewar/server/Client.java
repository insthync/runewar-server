package com.runewar.server;

import java.net.Socket;

import com.runewar.misc.ClientOnlineStatus;
import com.runewar.node.RoomNode;

public class Client extends ServerClasses {
    public String ID;
    public int OnlineStatus;
    public RoomNode CurrentRoom;
	private Packet packet = null;
    private ClientMessageReceiverThread messageReceiverThread;
	public Client(Server server, Socket clientSocket) {
		this.ID = null;
		this.OnlineStatus = ClientOnlineStatus.offline;
		this.CurrentRoom = null;
		this.packet = new Packet(clientSocket);
		this.db = server.getDatabase().clone();
		this.config = server.getConfig();
		this.handler = server.getHandler();
		this.clients = server.getClients();
		this.initializingClients = server.getInitializingClients();
		Init();
		this.initialized = true;
	}
	public Packet getPacket() {
		return packet;
	}
	@Override
	protected void Init() {
		messageReceiverThread = new ClientMessageReceiverThread(this);
	}
	@Override
	public void Start() {
		if (initialized && !available) {
			available = true;
			if (!initializingClients.contains(this)) {
				initializingClients.add(this);
			}
			System.out.println("New client accepted from " + packet.socket.getInetAddress().getHostAddress() + ".");
			messageReceiverThread.Start();
		}
	}
	@Override
	public void Stop() {
		if (initialized && available) {
			available = false;
			OnlineStatus = ClientOnlineStatus.offline;
			if (initializingClients.contains(this)) {
				initializingClients.remove(this);
			}
			System.out.println("Client from " + packet.socket.getInetAddress().getHostAddress() + " disconnected.");
			messageReceiverThread.Stop();
			if (!db.isClosed())
				db.Close();
			if (!packet.isClosed())
				packet.Close();
		}
	}
}
