/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package trafficsim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import util.Log;

/**
 * Entrypoint for Java Swing application.
 */
public class TrafficSim extends JFrame {
	public static void main(String[] args) {

		/* Create and display the form */
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new TrafficSim().setVisible(true);
			}
		});
	}

	/** Creates new form TrafficSim */
	public TrafficSim() {
		initComponents();
		// set frame size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = getSize(); // in case user has some default window size
		if (frameSize.getWidth() < 200) {
			frameSize.width = DEFAULT_WIDTH;
		}
		if (frameSize.getHeight() < 200) {
			frameSize.height = DEFAULT_HEIGHT;
		}
		this.setSize(frameSize);
		setLocation(
				new Point((int) (screenSize.width - frameSize.width) / 2,
						(int) (screenSize.height - frameSize.height) / 2));
	}

	private void initComponents() {
		pixelCoords = new PixelCoords();
		mainPanel = new JPanel();
		buttonsPanel = new JPanel();
		startButton = new JButton();
		pauseButton = new JButton();
		resetButton = new JButton();
		mainMenu = new JMenuBar();
		fileMenu = new JMenu();
		helpMenu = new JMenu();
		aboutMenuItem = new JMenuItem();
		exitMenuItem = new JMenuItem();
		loadMenuItem = new JMenuItem();
		fileChooser = new JFileChooser();
		FileNameExtensionFilter osmFilter = new FileNameExtensionFilter(osmDescription, "osm");
		fileChooser.addChoosableFileFilter(osmFilter);
		fileChooser.setCurrentDirectory(new File("resources"));
		// convenience for testing
		fileChooser.setSelectedFile(new File("resources/sanfrancisco.osm"));
		fileChooser.setFileFilter(osmFilter);
		panXLabel = new JLabel();
		panYLabel = new JLabel();
		perspectiveLabel = new JLabel();
		panXSlider = new JSlider();
		panYSlider = new JSlider();
		perspectiveSlider = new JSlider();
		setTitle("Traffic Sim");
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});
		mainPanel.setLayout(new GridBagLayout());
		buttonsPanel.setLayout(new FlowLayout());
		buttonsPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		startButton.setMnemonic('S');
		startButton.setText("Start");
		startButton.setToolTipText("Start running the traffic simulation.");
		startButton.setEnabled(false);
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				startButtonActionPerformed(evt);
			}
		});
		buttonsPanel.add(startButton);
		pauseButton.setMnemonic('P');
		pauseButton.setText("Pause");
		pauseButton.setToolTipText("Pause the traffic simulation.");
		pauseButton.setEnabled(false);
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pauseButtonActionPerformed(evt);
			}
		});
		buttonsPanel.add(pauseButton);
		resetButton.setMnemonic('R');
		resetButton.setText("Reset");
		resetButton.setToolTipText("Stop the traffic simulation and reset.");
		resetButton.setEnabled(false);
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetButtonActionPerformed(evt);
			}
		});
		buttonsPanel.add(resetButton);
		panXLabel.setText("Pan X:");
		panYLabel.setText("Pan Y:");
		perspectiveLabel.setText("Perspective:");
		int maxPan = 2000;
		panXSlider.setMaximum(maxPan);
		panXSlider.setMinimum(-maxPan);
		panYSlider.setMaximum(maxPan);
		panYSlider.setMinimum(-maxPan);
		panXSlider.setMinorTickSpacing(1);
		panYSlider.setMinorTickSpacing(1);
		panXSlider.setValue(0);
		panYSlider.setValue(0);
		panXSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				if (pixelCoords != null && sim != null && drawingPanel != null) {
					pixelCoords.setXFudge(panXSlider.getValue());
					drawingPanel.repaint();
				}
				Log.debug("X pan: " + panXSlider.getValue());
			}
		});
		panYSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				if (pixelCoords != null && sim != null && drawingPanel != null) {
					pixelCoords.setYFudge(panYSlider.getValue());
					drawingPanel.repaint();
				}
				Log.debug("Y pan: " + panYSlider.getValue());
			}
		});
		perspectiveSlider.setMaximum(200);
		perspectiveSlider.setMinimum(50);
		perspectiveSlider.setMinorTickSpacing(1);
		perspectiveSlider.setValue(108);
		perspectiveSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				if (pixelCoords != null && sim != null && drawingPanel != null) {
					pixelCoords.setScaleFudge(perspectiveSlider.getValue() * 0.01);
					drawingPanel.repaint();
				}
				Log.debug("Scale: " + perspectiveSlider.getValue() * 0.01);
			}
		});
		buttonsPanel.add(panXLabel);
		buttonsPanel.add(panXSlider);
		buttonsPanel.add(panYLabel);
		buttonsPanel.add(panYSlider);
		buttonsPanel.add(perspectiveLabel);
		buttonsPanel.add(perspectiveSlider);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		fileMenu.setMnemonic('F');
		fileMenu.setText("File");
		helpMenu.setMnemonic('H');
		helpMenu.setText("Help");
		aboutMenuItem.setMnemonic('A');
		aboutMenuItem.setText("About");
		aboutMenuItem.setToolTipText("About");
		aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				aboutMenuItemActionPerformed(evt);
			}
		});
		helpMenu.add(aboutMenuItem);
		loadMenuItem.setMnemonic('L');
		loadMenuItem.setText("Load");
		loadMenuItem.setToolTipText("Load");
		loadMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				loadMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(loadMenuItem);
		exitMenuItem.setMnemonic('E');
		exitMenuItem.setText("Exit");
		exitMenuItem.setToolTipText("Exit");
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(exitMenuItem);
		mainMenu.add(fileMenu);
		mainMenu.add(helpMenu);
		setJMenuBar(mainMenu);
	}

	private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		new About(this).setVisible(true);
	}

	private void startButtonActionPerformed(ActionEvent evt) {
		startButton.setEnabled(false);
		sim.start();
		drawingPanel.repaint();
		pauseButton.setEnabled(true);
		resetButton.setEnabled(true);
	}

	private void pauseButtonActionPerformed(ActionEvent evt) {
		pauseButton.setEnabled(false);
		sim.stop();
		startButton.setEnabled(true);
		resetButton.setEnabled(true);
	}

	private void resetButtonActionPerformed(ActionEvent evt) {
		resetButton.setEnabled(false);
		sim.reset();
		drawingPanel.repaint();
		startButton.setEnabled(true);
		pauseButton.setEnabled(false);
	}

	private void loadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		int returnVal = fileChooser.showOpenDialog(this);
		String fileTypeDescription = fileChooser.getFileFilter().getDescription();
		Log.info("File type description : " + fileTypeDescription);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				pixelCoords = new PixelCoords();
				drawingPanel = new DrawingPanel(pixelCoords);
				layeredPane = new JLayeredPane();
				layeredPane.setPreferredSize(getSize());
				drawingPanel.setBounds(0, 0, getSize().width, getSize().height);
				layeredPane.add(drawingPanel, 1);
				scrollPane = new ZoomPanScrollPane(layeredPane);
				scrollPane.setPreferredSize(getSize());
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 1;
				c.weighty = 1;
				c.fill = GridBagConstraints.BOTH;
				pixelCoords.setWidth(drawingPanel.getWidth());
				pixelCoords.setHeight(drawingPanel.getHeight());
				pixelCoords.setYFudge(panYSlider.getValue());
				pixelCoords.setScaleFudge(perspectiveSlider.getValue()*0.01);
				pixelCoords.setup(fileChooser.getSelectedFile().getAbsolutePath());
				mainPanel.add(scrollPane, c);
				mainPanel.validate();
				// needed to refresh panes without firing an action (like window resize, mouse event)
				mainPanel.validate();
				sim = new Sim(drawingPanel, pixelCoords);
				startButton.setEnabled(true);
				pauseButton.setEnabled(false);
				resetButton.setEnabled(false);
			} catch (Exception ex) {
				Log.error("Loading file " + fileChooser.getSelectedFile().getAbsolutePath());
				Log.error(ex.toString());
			}
		}
	}

	private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
		System.exit(0);
	}

	private void exitForm(java.awt.event.WindowEvent evt) {
		System.exit(0);
	}

	private Sim sim;
	private PixelCoords pixelCoords;
	private ZoomPanScrollPane scrollPane;
	private JLayeredPane layeredPane;
	private DrawingPanel drawingPanel;
	private JMenuBar mainMenu;
	private JMenu fileMenu;
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	private JMenuItem loadMenuItem;
	private JMenuItem exitMenuItem;
	private JPanel mainPanel;
	private JPanel buttonsPanel;
	private JButton startButton;
	private JButton pauseButton;
	private JButton resetButton;
	private JFileChooser fileChooser;
	private JLabel panXLabel;
	private JLabel panYLabel;
	private JLabel perspectiveLabel;
	private JSlider panXSlider;
	private JSlider panYSlider;
	private JSlider perspectiveSlider;
	private final String osmDescription = "OSM file";
	private final int DEFAULT_WIDTH = 1400;
	private final int DEFAULT_HEIGHT = 1000;
}
