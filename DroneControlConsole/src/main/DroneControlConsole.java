package main;

import gui.DroneGUI;
import gui.RobotGUI;
import network.DroneConsoleMessageHandler;
import network.broadcast.ConsoleBroadcastHandler;
import network.server.ServerConnectionListener;
import threads.BehaviorMessageThread;
import threads.ConnectionThread;
import threads.MotorUpdateThread;
import threads.NetworkActivationsUpdateThread;
import threads.UpdateThread;

import commoninterface.network.messages.InformationRequest.MessageType;

import dataObjects.ConsoleMotorSpeeds;
import dataObjects.DronesSet;

public class DroneControlConsole extends RobotControlConsole {
	
	private DronesSet dronesSet = new DronesSet();

	private ServerConnectionListener mobileAppServer;
	private ConsoleBroadcastHandler consoleBroadcastHandler;

	public DroneControlConsole() {
		try {
			motorSpeeds = new ConsoleMotorSpeeds();

			mobileAppServer = new ServerConnectionListener(this);

			gui = setupGUI();
			setupGamepad();

			consoleBroadcastHandler = new ConsoleBroadcastHandler(this);

			messageHandler = new DroneConsoleMessageHandler(this);
			messageHandler.start();

			// Special case which does not depend on a connection and should
			// only be started once
			ConnectionThread t = new ConnectionThread(this,
					gui.getConnectionPanel());
			t.start();

			addShutdownHooks();

			gui.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createUpdateThreads() {

		for (UpdateThread t : updateThreads)
			t.stopExecuting();

		updateThreads.clear();

		updateThreads.add(new UpdateThread(this,
				((DroneGUI) gui).getGPSPanel(), MessageType.GPS));
		updateThreads.add(new UpdateThread(this, gui.getMessagesPanel(),
				MessageType.SYSTEM_STATUS));
		updateThreads.add(new MotorUpdateThread(this, gui.getMotorsPanel()));
		updateThreads.add(new UpdateThread(this, ((DroneGUI) gui)
				.getCompassPanel(), MessageType.COMPASS));
		updateThreads.add(new BehaviorMessageThread(this, gui
				.getCommandPanel()));
		updateThreads.add(new NetworkActivationsUpdateThread(this, gui
				.getNeuralActivationsPanel(), MessageType.NEURAL_ACTIVATIONS));

		for (UpdateThread t : updateThreads)
			t.start();

	}

	@Override
	public RobotGUI setupGUI() {
		return new DroneGUI(this);
	}

	public static void main(String[] args) {
		new DroneControlConsole();
	}

	public DronesSet getDronesSet() {
		return dronesSet;
	}

	public ConsoleBroadcastHandler getConsoleBroadcastHandler() {
		return consoleBroadcastHandler;
	}

	public ServerConnectionListener getMobileAppServer() {
		return mobileAppServer;
	}
	
	@Override
	protected void addShutdownHooks() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				disconnect();

				if (consoleBroadcastHandler != null) {
					consoleBroadcastHandler.closeConnections();
					consoleBroadcastHandler = null;
				}

				mobileAppServer.stopServer();
			}
		});
	}
}
