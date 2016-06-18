package com.runewar.server;

public class ServerClassesThread extends ServerClasses implements Runnable {

	protected Thread runner;
	public ServerClassesThread() {
		super();
		Init();
	}
	@Override
	protected void Init() {
		// TODO Auto-generated method stub
	}

	@Override
	public void Start() {
		// TODO Auto-generated method stub
		runner = new Thread(this);
		runner.start();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void Stop() {
		// TODO Auto-generated method stub
		while (runner != null && runner.isAlive())
			runner.stop();
		runner = null;
	}

	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		
	}

}
