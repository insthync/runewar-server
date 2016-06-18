package com.runewar;
import java.util.Scanner;

import com.runewar.server.Server;


public class Main {
	private Thread closeThread;
	private Runnable closeRunner;
	private Scanner scanner = null;
	private Server s = null;
	private boolean end = false;
    public void InitThread() {
    	closeRunner = new Runnable() {
            public synchronized void run() {
            	Stop();
            }
		};
    	closeThread = new Thread(closeRunner, "closeThread");
    }
    public Thread getCloseThread() {
    	return closeThread;
    }
	public void Init() {
		InitThread();
		scanner = new Scanner(System.in);
		s = new Server();
	}
	public void Run() {
		while (!end) {
			String cmd_set = scanner.nextLine();
			String[] cmd_set_splited = cmd_set.split(" ");
			String cmd = cmd_set_splited[0];
			if (cmd.equals("start")) {
				Start();
			}
			if (cmd.equals("stop")) {
				Stop();
			}
			if (cmd.equals("exit")) {
				Close();
			}
		}
	}
	public void Start() {
		if (!s.IsAvailable()) {
			System.out.println("Starting server...");
			s.Start();
		}
	}
	public void Stop() {
		if (s.IsAvailable()) {
			System.out.println("Stopping Server...");
			s.Stop();
		}
	}
	public void Close() {
		if (s.IsAvailable())
			Stop();
		scanner.close();
		end = true;
	}
	public static void main(String[] args) {
		Main m = new Main();
		m.Init();
		m.Start();
		m.Run();
		Runtime.getRuntime().addShutdownHook(m.getCloseThread());
		return;
	}
}
