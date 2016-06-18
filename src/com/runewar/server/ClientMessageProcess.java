package com.runewar.server;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.runewar.database.DBUsers;
import com.runewar.database.MySQL;
import com.runewar.misc.ClientOnlineStatus;
import com.runewar.misc.PacketHeader;
import com.runewar.misc.ReceivedMessage;
import com.runewar.misc.RoomNodeEndState;
import com.runewar.misc.RoomNodeReadyState;
import com.runewar.misc.RoomNodeState;
import com.runewar.node.RoomNode;

public class ClientMessageProcess {
	public static void Process(Client client, String message) throws ClassNotFoundException, IOException, JsonSyntaxException {
		//System.out.println("Receive message: " + message);
		if (client == null)
			return;
		// Another instance for work
		Packet packet = client.getPacket();
		Handler handler = client.getHandler();
		MySQL db = client.getDatabase();
		if (message.contains("<policy-file-request/>")) {
			/*
			StringBuffer policyBuffer = new StringBuffer();
			policyBuffer.append("<?xml version=\"1.0\"?><cross-domain-policy>");
			policyBuffer.append("<allow-access-from domain=\"*\" to-ports=\"*\" />");
			policyBuffer.append("</cross-domain-policy>");
			String returnPolicy = policyBuffer.toString() + '\0';
			//byte[] returnPolicyBytes = returnPolicy.getBytes();
			System.out.println("Returning cross-domain-policy...\n" + returnPolicy);
			//packet.socket.getOutputStream().write(returnPolicyBytes, 0, returnPolicyBytes.length);
			//packet.socket.getOutputStream().flush();
			packet.out.println(returnPolicy);
			packet.out.flush();
			*/
			client.Stop();
			return;
		}
		// An messages
		Gson gson = new Gson();
		ReceivedMessage receivedMessage = gson.fromJson(message, ReceivedMessage.class);
		short key = receivedMessage.key;
		String[] values = receivedMessage.values;
		ReceivedMessage returningMessage;
		Client targetClient;
		RoomNode targetRoom;
		switch (key) {
		case PacketHeader.login:
			if (values != null && values.length == 2) {
				String LoginID = values[0];
				String LoginToken = values[1];
				// Checking for login id and login token 
				if (DBUsers.checkLogin(db, LoginID, LoginToken)) {
					// If valid login id and login token
					if ((targetClient = handler.findClient(LoginID)) != null) {
						// If old instance of login id is in the game
						// Force to remove it
						targetClient.Stop();
						handler.Clients.remove(LoginID);
					}
					handler.addClient(LoginID, client);
					System.out.println("User ID = " + LoginID + " login to the game.");
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.login_success;
					packet.out.println(gson.toJson(returningMessage));
				} else {
					client.Stop();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.login_fail;
					packet.out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.online_status:
			if (values != null && values.length == 1) {
				String OnlineCheckingTargetID = values[0];
				if ((targetClient = handler.findClient(OnlineCheckingTargetID)) != null) {
					// If found user return their status
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.online_status_return;
					returningMessage.values = new String[2];
					returningMessage.values[0] = OnlineCheckingTargetID;
					returningMessage.values[1] = "" + targetClient.OnlineStatus;
					packet.out.println(gson.toJson(returningMessage));
				} else {
					// If not found user return offline status
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.online_status_return;
					returningMessage.values = new String[2];
					returningMessage.values[0] = OnlineCheckingTargetID;
					returningMessage.values[1] = "" + ClientOnlineStatus.offline;
					packet.out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.send_request:
			if (values != null && values.length == 1) {
				String RequestTargetID = values[0];
				if ((targetClient = handler.findClient(RequestTargetID)) != null 
					&& targetClient.OnlineStatus == ClientOnlineStatus.online
					&& !targetClient.equals(client)) {
					// Tell requester that request made successfully
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.send_request_success;
					packet.out.println(gson.toJson(returningMessage));
					// Tell receiver that they received request
					returningMessage.key = PacketHeader.request_receive;
					returningMessage.values = new String[1];
					returningMessage.values[0] = client.ID;
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
					// Create new room to handle receiver acception and gameplay
					handler.createRoom(client, targetClient);
				} else {
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.send_request_fail;
					packet.out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.request_accept:
			if (values != null && values.length == 1) {
				String AcceptTargetID = values[0];
				if ((targetClient = handler.findClient(AcceptTargetID)) != null
					&& client.OnlineStatus ==  ClientOnlineStatus.online
					&& targetClient.OnlineStatus == ClientOnlineStatus.online
					&& checkRoom(client, targetClient, RoomNodeState.request)) {
					targetRoom = client.CurrentRoom;
					// Tell accepter that accept made successfully
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.request_accept_success;
					packet.out.println(gson.toJson(returningMessage));
					// Tell requester that they're start as host
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_start_as_host;
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
					// Tell accepter that they're start as client
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_start_as_client;
					packet.out.println(gson.toJson(returningMessage));
					// Change room state to start
					targetRoom.requestAccepted();
				} else {
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.request_accept_fail;
					packet.out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.request_decline:
			if (values != null && values.length == 1) {
				String DeclineTargetID = values[0];
				if ((targetClient = handler.findClient(DeclineTargetID)) != null
					&& client.OnlineStatus ==  ClientOnlineStatus.online
					&& targetClient.OnlineStatus == ClientOnlineStatus.online
					&& checkRoom(client, targetClient, RoomNodeState.request)) {
					targetRoom = client.CurrentRoom;
					// Tell decliner that decline made successfully
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.request_decline_success;
					packet.out.println(gson.toJson(returningMessage));
					// Tell requester that they're unable to request
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.request_expire;
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
					// Change room state to end
					targetRoom.requestDeclined();
				} else {
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.request_decline_fail;
					packet.out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_load_finish:
			if (values == null) {
				if ((targetRoom = client.CurrentRoom) != null && targetRoom.RoomState == RoomNodeState.start) {
					targetClient = targetRoom.getCreatorClient();
					if (targetRoom.ReadyState != RoomNodeReadyState.ready) {
						++targetRoom.ReadyState;
						if (targetRoom.ReadyState == RoomNodeReadyState.ready) {
							// When all clients are ready then start by host (start count down)
							returningMessage = new ReceivedMessage();
							returningMessage.key = PacketHeader.game_load_finish;
							targetClient.getPacket().out.println(gson.toJson(returningMessage));
							targetRoom.gameLoaded();
						}
					}
				}
			}
			break;
		case PacketHeader.game_host_count_down:
			if (values != null && values.length == 1) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_host_count_down;
					returningMessage.values = new String[1];
					returningMessage.values[0] = values[0]; // Count down number
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_host_start:
			if (values == null) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_host_start;
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_set_rune:
			if (values != null && values.length == 2) {
				String playerIndex;
				if ((targetRoom = client.CurrentRoom) != null
					&& targetRoom.RoomState == RoomNodeState.play) {
					if (client.equals(targetRoom.getCreatorClient())) {
						targetClient = targetRoom.getJoinerClient();
						playerIndex = "0";
					} else {
						targetClient = targetRoom.getCreatorClient();
						playerIndex = "1";
					}
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_set_rune;
					returningMessage.values = new String[3];
					returningMessage.values[0] = playerIndex;
					returningMessage.values[1] = values[0]; // set born index
					returningMessage.values[2] = values[1]; // set char index
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_spawn_character:
			if (values != null && values.length == 2) {
				String playerIndex;
				if ((targetRoom = client.CurrentRoom) != null
					&& targetRoom.RoomState == RoomNodeState.play) {
					if (client.equals(targetRoom.getCreatorClient())) {
						playerIndex = "0";
					} else {
						playerIndex = "1";
					}
					targetClient = targetRoom.getCreatorClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_spawn_character;
					returningMessage.values = new String[3];
					returningMessage.values[0] = playerIndex;
					returningMessage.values[1] = values[0]; // rune type
					returningMessage.values[2] = values[1]; // rune level
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_append_character:
			if (values != null && values.length == 4) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_append_character;
					returningMessage.values = new String[4];
					returningMessage.values[0] = values[0]; // player index
					returningMessage.values[1] = values[1]; // character id
					returningMessage.values[2] = values[2]; // rune type
					returningMessage.values[3] = values[3]; // rune level
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_update_entity:
			if (values != null && values.length == 5) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_update_entity;
					returningMessage.values = new String[5];
					returningMessage.values[0] = values[0]; // character id
					returningMessage.values[1] = values[1]; // hp
					returningMessage.values[2] = values[2]; // state
					returningMessage.values[3] = values[3]; // x
					returningMessage.values[4] = values[4]; // y
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_append_character_damage:
			if (values != null && values.length == 4) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_append_character_damage;
					returningMessage.values = new String[4];
					returningMessage.values[0] = values[0]; // character id
					returningMessage.values[1] = values[1]; // damage type
					returningMessage.values[2] = values[2]; // damage taken
					returningMessage.values[3] = values[3]; // from character id
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_character_attack_to:
			if (values != null && values.length == 2) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_character_attack_to;
					returningMessage.values = new String[2];
					returningMessage.values[0] = values[0]; // character id
					returningMessage.values[1] = values[1]; // from character id
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_use_cannon:
			if (values != null && values.length == 1) {
				String playerIndex;
				if ((targetRoom = client.CurrentRoom) != null
					&& targetRoom.RoomState == RoomNodeState.play) {
					if (client.equals(targetRoom.getCreatorClient())) {
						playerIndex = "0";
					} else {
						playerIndex = "1";
					}
					targetClient = targetRoom.getCreatorClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_use_cannon;
					returningMessage.values = new String[2];
					returningMessage.values[0] = playerIndex;
					returningMessage.values[1] = values[0]; // collected cannon power
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_cannon_to:
			if (values != null && values.length == 4) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_cannon_to;
					returningMessage.values = new String[4];
					returningMessage.values[0] = values[0]; // player index
					returningMessage.values[1] = values[1]; // character id
					returningMessage.values[2] = values[2]; // cannon index
					returningMessage.values[3] = values[3]; // collected cannon power
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_use_meteor:
			if (values == null) {
				String playerIndex;
				if ((targetRoom = client.CurrentRoom) != null
					&& targetRoom.RoomState == RoomNodeState.play) {
					if (client.equals(targetRoom.getCreatorClient())) {
						playerIndex = "0";
					} else {
						playerIndex = "1";
					}
					targetClient = targetRoom.getCreatorClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_use_meteor;
					returningMessage.values = new String[1];
					returningMessage.values[0] = playerIndex;
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_meteor_to:
			if (values != null && values.length == 1) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_meteor_to;
					returningMessage.values = new String[1];
					returningMessage.values[0] = values[0]; // character id
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_use_heal:
			if (values == null) {
				String playerIndex;
				if ((targetRoom = client.CurrentRoom) != null
					&& targetRoom.RoomState == RoomNodeState.play) {
					if (client.equals(targetRoom.getCreatorClient())) {
						playerIndex = "0";
					} else {
						playerIndex = "1";
					}
					targetClient = targetRoom.getCreatorClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_use_heal;
					returningMessage.values = new String[1];
					returningMessage.values[0] = playerIndex;
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_heal_to:
			if (values != null && values.length == 1) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_heal_to;
					returningMessage.values = new String[1];
					returningMessage.values[0] = values[0]; // character id
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_use_stun:
			if (values == null) {
				String playerIndex;
				if ((targetRoom = client.CurrentRoom) != null
					&& targetRoom.RoomState == RoomNodeState.play) {
					if (client.equals(targetRoom.getCreatorClient())) {
						playerIndex = "0";
					} else {
						playerIndex = "1";
					}
					targetClient = targetRoom.getCreatorClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_use_stun;
					returningMessage.values = new String[1];
					returningMessage.values[0] = playerIndex;
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_stun_to:
			if (values != null && values.length == 1) {
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_stun_to;
					returningMessage.values = new String[1];
					returningMessage.values[0] = values[0]; // character id
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_result:
			if (values != null && values.length == 1) {
				String winnerPlayerIndex = values[0];
				String reward_gold = "";
				String reward_exp = "";
				if ((targetRoom = client.CurrentRoom) != null
					&& client.equals(targetRoom.getCreatorClient())
					&& targetRoom.RoomState == RoomNodeState.play) {
					targetClient = targetRoom.getJoinerClient();
					returningMessage = new ReceivedMessage();
					returningMessage.key = PacketHeader.game_result;
					returningMessage.values = new String[3];
					returningMessage.values[0] = winnerPlayerIndex; // Result
					returningMessage.values[1] = reward_gold; // Gold
					returningMessage.values[2] = reward_exp; // Gold
					targetClient.getPacket().out.println(gson.toJson(returningMessage));
					packet.out.println(gson.toJson(returningMessage));
				}
			}
			break;
		case PacketHeader.game_end:
			if (values == null) {
				if ((targetRoom = client.CurrentRoom) != null && targetRoom.RoomState == RoomNodeState.play) {
					if (targetRoom.EndState != RoomNodeEndState.end) {
						++targetRoom.EndState;
						if (targetRoom.EndState == RoomNodeEndState.end) {
							targetRoom.kickAll();
						}
					}
				}
			}
			break;
		case PacketHeader.ping:
			if (values == null) {
				returningMessage = new ReceivedMessage();
				returningMessage.key = PacketHeader.ping;
				packet.out.println(gson.toJson(returningMessage));
			}
			break;
		}
	}
	
	public static Boolean checkRoom(Client client, Client targetClient, int RoomState) {
		return (!targetClient.equals(client) 
				&& client.CurrentRoom != null
				&& targetClient.CurrentRoom != null
				&& client.CurrentRoom.equals(targetClient.CurrentRoom)
				&& client.CurrentRoom.RoomState == RoomState);
	}
}
