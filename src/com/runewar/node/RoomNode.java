package com.runewar.node;

import com.runewar.misc.ClientOnlineStatus;
import com.runewar.misc.RoomNodeEndState;
import com.runewar.misc.RoomNodeReadyState;
import com.runewar.misc.RoomNodeState;
import com.runewar.server.Client;

public class RoomNode {
	private Client creatorClient;
	private Client joinerClient;
	public final long createdTime;
	public int RoomState;
	public int EndState;
	public int ReadyState;
	public RoomNode(Client creatorClient, Client joinerClient) {
		RoomState = RoomNodeState.request;
		ReadyState = RoomNodeReadyState.none;
		EndState = RoomNodeEndState.none;
		createdTime = System.currentTimeMillis();
		this.creatorClient = creatorClient;
		this.joinerClient = joinerClient;
		this.creatorClient.CurrentRoom = this;
		this.joinerClient.CurrentRoom = this;
	}
	
	public Client getCreatorClient() {
		return creatorClient;
	}
	
	public Client getJoinerClient() {
		return joinerClient;
	}
	
	public void requestAccepted() {
		creatorClient.OnlineStatus = ClientOnlineStatus.busy;
		joinerClient.OnlineStatus = ClientOnlineStatus.busy;
		RoomState = RoomNodeState.start;
	}
	
	public void requestDeclined() {
		kickAll();
	}
	
	public void gameLoaded() {
		RoomState = RoomNodeState.play;
	}
	
	public void gameEnded() {
		kickAll();
	}
	
	public void kickAll() {
		creatorClient.OnlineStatus = ClientOnlineStatus.online;
		joinerClient.OnlineStatus = ClientOnlineStatus.online;
		joinerClient.CurrentRoom = null;
		creatorClient.CurrentRoom = null;
		joinerClient = null;
		creatorClient = null;
		RoomState = RoomNodeState.end;
		ReadyState = RoomNodeReadyState.none;
		EndState = RoomNodeEndState.none;
	}
}