package com.runewar.server;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.runewar.misc.ClientOnlineStatus;
import com.runewar.misc.PacketHeader;
import com.runewar.misc.ReceivedMessage;
import com.runewar.misc.RoomNodeState;
import com.runewar.node.RoomNode;

public class Handler implements Runnable {
	public final long room_expire_time = 10000;
	public final HashMap<String, Client> Clients;
	public final ArrayList<RoomNode> Rooms;
	private Thread runner;
	private boolean available = false;
	public Handler(Server server, HashMap<String, Client> clients) {
		this.Clients = clients;
		this.Rooms = new ArrayList<RoomNode>();
	}
	
	public void checkExpiredRoom() {
		try {
			synchronized (Rooms) {
				for (int i = 0; i < Rooms.size(); ++i) {
					RoomNode Room = Rooms.get(i);
					if (System.currentTimeMillis() - Room.createdTime >= room_expire_time && Room.RoomState == RoomNodeState.request) {
						Gson gson = new Gson();
						ReceivedMessage returningMessage = new ReceivedMessage();
						returningMessage.key = PacketHeader.request_expire;
						Room.getCreatorClient().getPacket().out.println(gson.toJson(returningMessage));
						Room.kickAll();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void clearEndRooms() {
		try {
			synchronized (Rooms) {
				for (RoomNode room : Rooms) {
					if (room.RoomState == RoomNodeState.end) {
						Rooms.remove(room);
						Rooms.notify();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void clearOfflineClients() {
		try {
			synchronized (Clients) {
				for (Client client : Clients.values()) {
					if (client.ID != null && client.OnlineStatus == ClientOnlineStatus.offline) {
						client.Stop();
						Clients.remove(client.ID);
						Clients.notify();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public boolean isClientInRoom(Client client, RoomNode room) {
		return (client != null && room != null && client.CurrentRoom != null && client.CurrentRoom.equals(room));
	}
	
	public void createRoom(Client creator, Client joiner) {
		synchronized (Rooms) {
			if (creator != null && joiner != null) {
				Rooms.add(new RoomNode(creator, joiner));
				Rooms.notify();
			}
		}
	}
	
	public void addClient(String id, Client client) {
		synchronized (Clients) {
			if (id != null && client != null) {
				if (Clients.containsKey(id)) {
					Clients.remove(id);
				}
				client.ID = id;
				client.OnlineStatus = ClientOnlineStatus.online;
				Clients.put(id, client);
				Clients.notify();
				//System.out.println("Added client ID=" + id);
			}
		}
	}
	
	public Client findClient(String id) {
		synchronized (Clients) {
			//System.out.println("Finding client ID=" + id);
			if (Clients.containsKey(id)) {
				//System.out.println("Found client ID=" + id);
				return Clients.get(id);
			}
		}
		return null;
	}

	@Override
	public void run() {
		while (available) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			synchronized (Rooms) {
				checkExpiredRoom();
				clearEndRooms();
			}
			synchronized (Clients) {
				clearOfflineClients();
			}
		}
	}
	
	public void Start() {
		// TODO Auto-generated method stub
		available = true;
		runner = new Thread(this);
		runner.start();
	}

	@SuppressWarnings("deprecation")
	public void Stop() {
		// TODO Auto-generated method stub
		available = false;
		while (runner != null && runner.isAlive())
			runner.stop();
		runner = null;
	}
}
