package gui;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import gui.panels.ThymioCapturePanel;
import gui.panels.ThymioSensorsPanel;
import gui.panels.ThymioVirtualPositionPanel;
import main.ThymioControlConsole;


public class ThymioGUI extends RobotGUI {
	private static final long serialVersionUID = 4178084911548724511L;
	private ThymioSensorsPanel readingsPanel;
	private ThymioCapturePanel capturePanel;
	private ThymioVirtualPositionPanel virtualPositionPanel;
	
	public ThymioGUI(ThymioControlConsole console) {
		this.console = console;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.err.println("Not able to set LookAndFeel for the current OS");
		}

		enableOSXFullscreen(this);
		buildGUI();
	}
	
	@Override
	protected void buildGUI() {
		setTitle("HANCAD/CORATAM Project - Thymio Remote Console");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(true);
		setFocusable(true);

		setLayout(new BorderLayout());

		createPanels();
		createInfoPanel();
		createCameraPanel();

		pack();
		setLocationRelativeTo(null);
	}


	private void createCameraPanel() {
		capturePanel = new ThymioCapturePanel();
		add(capturePanel, BorderLayout.CENTER);
	}
	
	private void createInfoPanel() {
		JPanel rightPanel = new JPanel(new BorderLayout());

		// Motors
		rightPanel.add(motorsPanel, BorderLayout.NORTH);

		readingsPanel = new ThymioSensorsPanel();
		rightPanel.add(readingsPanel, BorderLayout.CENTER);
		
		virtualPositionPanel = new ThymioVirtualPositionPanel();
		rightPanel.add(virtualPositionPanel, BorderLayout.SOUTH);
		
		add(rightPanel, BorderLayout.EAST);

		JPanel leftPanel = new JPanel(new BorderLayout());

		JPanel leftTopPanel = new JPanel();
		leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));
		leftTopPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);

		// Connection
		leftTopPanel.add(connectionPanel);

		// Behaviors
		leftTopPanel.add(commandPanel);
		leftPanel.add(leftTopPanel, BorderLayout.NORTH);

		// Messages
		leftPanel.add(msgPanel, BorderLayout.CENTER);

		add(leftPanel, BorderLayout.WEST);
	}
	
	public ThymioSensorsPanel getReadingsPanel() {
		return readingsPanel;
	}
	
	public ThymioCapturePanel getCapturePanel() {
		return capturePanel;
	}
	
	public ThymioVirtualPositionPanel getVirtualPositionPanel() {
		return virtualPositionPanel;
	}
	
}