package mdn.jh.automation.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import mdn.jh.automation.Main;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.gui.logic.LogicUnitPanel;
import mdn.jh.automation.gui.sink.DataSinkPanel;
import mdn.jh.automation.gui.source.DataSourcePanel;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.components.bool.AND;
import mdn.jh.automation.io.logic.components.bool.INVERT;
import mdn.jh.automation.io.logic.components.bool.OR;
import mdn.jh.automation.io.logic.components.bool.XOR;
import mdn.jh.automation.io.logic.components.bool.RSFlipFlop;
import mdn.jh.automation.io.logic.components.bool.BooleanDelay;
import mdn.jh.automation.io.logic.components.bool.BooleanPulse;
import mdn.jh.automation.io.logic.components.number.Addition;
import mdn.jh.automation.io.logic.components.number.Counter;
import mdn.jh.automation.io.logic.components.number.Difference;
import mdn.jh.automation.io.logic.components.number.Division;
import mdn.jh.automation.io.logic.components.number.Multiplication;
import mdn.jh.automation.io.sink.DataSink;
import mdn.jh.automation.io.source.DataSource;

public class MainWindow extends JFrame {
//	FritzBox fritzBox = null;
	private static final long serialVersionUID = -1755502031020763094L;
	DataSource ds = null;
	DataSource ds1 = null;

	private DataPanelMoveable dataSourcePanelMoveable;
	private MouseHandler mh = new MouseHandler();

	// private int stateAddConnection = 1;

	/**
	 * Create the frame.
	 */

	private void startDeviceFactory() {
		CreateDevice deviceFactory=new CreateDevice();
		deviceFactory.setVisible(true);
	}
	
	
	public MainWindow() {
		setMinimumSize(new Dimension(1500, 1000));
		getContentPane().setSize(new Dimension(800, 800));
		getContentPane().setPreferredSize(new Dimension(800, 800));
		setPreferredSize(new Dimension(800, 800));
		setSize(new Dimension(1544, 1016));

		initialize();
		// fritzBox = Main.getMySmartHomeHandler().getMyFritzBox();
		getPanelLogicComponents().setLayout(null);
		getContentPane().add(getPanel_Center(), BorderLayout.CENTER);

		// test();
		getContentPane().addMouseListener(mh);
		getContentPane().addMouseMotionListener(mh);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 500, 500);
		initComponentSelection();
		resizeLogicPanel(0);
		initExistingComponents();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				getPanelLogicComponents().drawAllConnections();
			}
		});
	}// PanelTest2

	private void initComponentSelection() {
		addButtonToBooleanSelection("AND", AND.class);
		addButtonToBooleanSelection("OR", OR.class);
		addButtonToBooleanSelection("XOR", XOR.class);
		addButtonToBooleanSelection("RS Flip-Flop", RSFlipFlop.class);
		addButtonToBooleanSelection("NOT", INVERT.class);
		addButtonToBooleanSelection("Boolean Delay", BooleanDelay.class);
		addButtonToBooleanSelection("Boolean Pulse", BooleanPulse.class);
		getPanel_Select_Boolean_Components().add(new JLabel("Math"));
		addButtonToBooleanSelection("Addition", Addition.class);
		addButtonToBooleanSelection("Difference", Difference.class);
		addButtonToBooleanSelection("Multiplication", Multiplication.class);
		addButtonToBooleanSelection("Division", Division.class);
		addButtonToBooleanSelection("Counter", Counter.class);

	}

	private void initExistingComponents() {

		Iterator<Device> deviceIterator = Main.getMySmartHomeHandler().getDevices().iterator();
		Device tempDevice = null;
		while (deviceIterator.hasNext()) {
			tempDevice = deviceIterator.next();
			Iterator<DataSource> itSource = tempDevice.getDataSourceHandler().getMyDataSources().iterator();
			int x_off = 0;
			int y_off = 0;

			while (itSource.hasNext()) {
				DataSource temp = itSource.next();

				if (temp.getBounds() != null) {
					addDataSourceToView(temp, temp.getBounds());

				} else {
					addDataSourceToView(temp, 100 + x_off, 100 + y_off, 158, 150);
					x_off += 50;
					y_off += 50;
				}
			}

			Iterator<DataSink> itSink = tempDevice.getDataSinkHandler().getMyDataSinks().iterator();
			while (itSink.hasNext()) {
				DataSink temp = itSink.next();
				if (temp.getBounds() != null) {
					addDataSinkToView(temp, temp.getBounds());

				} else {
					addDataSinkToView(temp, 100 + x_off, 100 + y_off, 158, 150);
					x_off += 50;
					y_off += 50;
				}
			}

		}

		Iterator<LogicBase> logicBaseIterator = Main.getMySmartHomeHandler().getDataProcessors().iterator();

		while (logicBaseIterator.hasNext()) {
			LogicUnitPanel panelLogic = new LogicUnitPanel();

			LogicBase temp = logicBaseIterator.next();

			if (temp.getBounds() != null) {
				panelLogic.setBounds(temp.getBounds());
			} else {
				panelLogic.setBounds(0, 0, 158, 150);

			}
			panelLogic.setMyLogicBase(temp);
			panelLogic.setCopyAndPasteListener(getPanelLogicComponents());
			getPanelLogicComponents().add(panelLogic);

		}

	}

	private void addLogicBaseComponent(Class<LogicBase> booleanLogicBase) {
		if (booleanLogicBase == null)
			return;
		LogicBase b = null;
		try {
			b = (LogicBase) booleanLogicBase.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Main.getMySmartHomeHandler().registerNewDataProcessor(b);
		LogicUnitPanel panelLogic = new LogicUnitPanel();
		panelLogic.setBounds(0, 0, 158, 150);
		panelLogic.setMyLogicBase(b);
		panelLogic.setCopyAndPasteListener(getPanelLogicComponents());
		getPanelLogicComponents().add(panelLogic);

		updateLogicPanel();
	}

	private void addButtonToBooleanSelection(String name, Class logicBase) {
		JButton b = new JButton(name);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addLogicBaseComponent(logicBase);
			}
		});
		getPanel_Select_Boolean_Components()
				.setLayout(new BoxLayout(getPanel_Select_Boolean_Components(), BoxLayout.Y_AXIS));
		getPanel_Select_Boolean_Components().add(b);

	}

	/**
	 * negative values size is subtracted
	 * 
	 * @param add
	 */
	private void resizeLogicPanel(int add) {

		Rectangle rect = getPanelLogicComponents().getBounds();
		getPanelLogicComponents()
				.setPreferredSize(new Dimension((int) rect.getWidth() + add, (int) rect.getHeight() + add));
		getScrollPaneLogicComponents().setViewportView(getPanelLogicComponents());
		lblWidth.setText("Width: " + getPanelLogicComponents().getBounds().getWidth());
		lblHeight.setText("Height: " + getPanelLogicComponents().getBounds().getHeight());
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		setTitle("Home Automation by JHR");
		setBounds(100, 100, 1572, 1003);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		getContentPane().add(getPanelTop(), BorderLayout.NORTH);
		getScrollPaneLogicComponents().setViewportView(getPanelLogicComponents());
		getContentPane().add(getPanelEast(), BorderLayout.EAST);
	}

	private void addDataSinkToView(DataSink dataSink, Rectangle bounds) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DataSinkPanel panel = new DataSinkPanel();
				panel.setBounds(bounds);
				panel.setMyDataSink(dataSink);
				panel.setCopyAndPasteListener(getPanelLogicComponents());
				getPanelLogicComponents().add(panel);
				updateLogicPanel();
			}
		});
	}

	private void addDataSinkToView(DataSink dataSink, int x, int y, int width, int height) {
		addDataSinkToView(dataSink, new Rectangle(x, y, width, height));
	}

	private void addDataSourceToView(DataSource dataSource, Rectangle bounds) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DataSourcePanel panel = new DataSourcePanel();
				panel.setBounds(bounds);
				panel.setMyDataSource(dataSource);
				panel.setCopyAndPasteListener(getPanelLogicComponents());
				getPanelLogicComponents().add(panel);
				updateLogicPanel();
			}
		});

	}

	private void addDataSourceToView(DataSource dataSource, int x, int y, int width, int height) {
		addDataSourceToView(dataSource, new Rectangle(x, y, width, height));
	}

	private void addDataSinkAction() {
		CreateActionDialog s = new CreateActionDialog(this);
		s.setVisible(true);

		if (s.getSelectedDataSink() == null) {
			return;
		}

		addDataSinkToView(s.getSelectedDataSink(), 100, 100, 158, 150);
	}

	private void addDataSource() {
		// SelectDataSourceDialog s = new SelectDataSourceDialog(this,ds);
		CreateDataSourceDialog s = new CreateDataSourceDialog(this);
		s.setVisible(true);

		if (s.getSelectedDataSource() == null) {
			return;
		}
		addDataSourceToView(s.getSelectedDataSource(), 100, 100, 158, 150);
	}

	private void updateLogicPanel() {
		getScrollPaneLogicComponents().setViewportView(getPanelLogicComponents());
	}

	private JPanel panelTop;
	private PanelCanvasLogicComponents panelLogicComponents;
	private JPanel panelEast;
	private JButton btnNewButton_2;
	private JButton btNewFritzSource;
	private JPanel panel_Center;
	private JScrollPane scrollPaneLogicComponents;
	private JPanel panel_SelectComponents;
	private JLabel lblNewLabel;
	private JPanel panel_Select_Boolean_Components;
	private JButton btnNewConnection;
	private JPanel panel_1;
	private JButton btnNewButton_1;
	private JButton btnNewButton_3;
	private JPanel panel_2;
	private JLabel lblWidth;
	private JLabel lblHeight;
	private JButton btNewAction;
	private JButton btnNewButton_4;

	private JPanel getPanelTop() {
		if (panelTop == null) {
			panelTop = new JPanel();
			panelTop.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			panelTop.add(getBtnNewButton_2());
			panelTop.add(getBtnNewButton_4());
		}
		return panelTop;
	}

	private PanelCanvasLogicComponents getPanelLogicComponents() {
		if (panelLogicComponents == null) {
			panelLogicComponents = new PanelCanvasLogicComponents();
			panelLogicComponents.setLayout(null);

		}
		return panelLogicComponents;
	}

	private JPanel getPanelEast() {
		if (panelEast == null) {
			panelEast = new JPanel();
			panelEast.setLayout(new BorderLayout(0, 0));
			panelEast.add(getPanel_SelectComponents(), BorderLayout.CENTER);
		}
		return panelEast;
	}

	private class MouseHandler extends MouseAdapter {
		int dx, dy;

		@Override
		public void mouseDragged(MouseEvent arg0) {

			if (dataSourcePanelMoveable == null)
				return;
			if (dataSourcePanelMoveable == null)
				return;
			if (dataSourcePanelMoveable.beweglich) {
				dataSourcePanelMoveable.setXpos(arg0.getXOnScreen() - dx);
				dataSourcePanelMoveable.setYpos(arg0.getYOnScreen() - dy);
				Rectangle b = getPanelLogicComponents().getBounds();

				// dataSourcePanelMoveable.bewegeDich(b.getMaxX(),b.getMaxY() );
				dataSourcePanelMoveable.movePanel(b.getWidth(), b.getHeight());
			} // if
		}// Dragged

		@Override
		public void mouseMoved(MouseEvent arg0) {
		}// Moved

		@Override
		public void mousePressed(MouseEvent arg0) {
			if (dataSourcePanelMoveable == null)
				return;
			dataSourcePanelMoveable.beweglich = true;
			dx = arg0.getXOnScreen() - dataSourcePanelMoveable.getXpos();
			dy = arg0.getYOnScreen() - dataSourcePanelMoveable.getYpos();
		}// Pressed

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if (dataSourcePanelMoveable == null)
				return;
			dataSourcePanelMoveable.beweglich = false;
			dx = 0;
			dy = 0;
		}// Released

		@Override
		public void mouseEntered(MouseEvent arg0) {
			try {
				dataSourcePanelMoveable = (DataPanelMoveable) arg0.getSource();
			} catch (Exception e) {
				return;
			}

		}// Entered

	}// MouseHandler

	private JButton getBtnNewButton_2() {
		if (btnNewButton_2 == null) {
			btnNewButton_2 = new JButton("Save");
			btnNewButton_2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Main.save();
				}
			});
		}
		return btnNewButton_2;
	}

	private JButton getBtNewFritzSource() {
		if (btNewFritzSource == null) {
			btNewFritzSource = new JButton("New Data Source");
			btNewFritzSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addDataSource();
				}
			});
		}
		return btNewFritzSource;
	}

	private JPanel getPanel_Center() {
		if (panel_Center == null) {
			panel_Center = new JPanel();
			panel_Center.setLayout(new BorderLayout(0, 0));
			panel_Center.add(getScrollPaneLogicComponents(), BorderLayout.CENTER);
		}
		return panel_Center;
	}

	private JScrollPane getScrollPaneLogicComponents() {
		if (scrollPaneLogicComponents == null) {
			scrollPaneLogicComponents = new JScrollPane();
			scrollPaneLogicComponents.setColumnHeaderView(getPanel_1());
			scrollPaneLogicComponents.setRowHeaderView(getPanel_2());
		}
		return scrollPaneLogicComponents;
	}

	private JPanel getPanel_SelectComponents() {
		if (panel_SelectComponents == null) {
			panel_SelectComponents = new JPanel();
			panel_SelectComponents.setLayout(new BoxLayout(panel_SelectComponents, BoxLayout.Y_AXIS));
			panel_SelectComponents.add(getLblNewLabel());
			panel_SelectComponents.add(getBtNewFritzSource());
			panel_SelectComponents.add(getBtNewAction());
			panel_SelectComponents.add(getPanel_Select_Boolean_Components());
		}
		return panel_SelectComponents;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Add Components");
		}
		return lblNewLabel;
	}

	private JPanel getPanel_Select_Boolean_Components() {
		if (panel_Select_Boolean_Components == null) {
			panel_Select_Boolean_Components = new JPanel();
		}
		return panel_Select_Boolean_Components;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.add(getBtnNewButton_1());
			panel_1.add(getBtnNewButton_3());
		}
		return panel_1;
	}

	private JButton getBtnNewButton_1() {
		if (btnNewButton_1 == null) {
			btnNewButton_1 = new JButton("Size +");
			btnNewButton_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					resizeLogicPanel(50);
				}
			});
		}
		return btnNewButton_1;
	}

	private JButton getBtnNewButton_3() {
		if (btnNewButton_3 == null) {
			btnNewButton_3 = new JButton("Size -");
			btnNewButton_3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					resizeLogicPanel(-50);
				}
			});
		}
		return btnNewButton_3;
	}

	private JPanel getPanel_2() {
		if (panel_2 == null) {
			panel_2 = new JPanel();
			panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
			panel_2.add(getLblWidth());
			panel_2.add(getLblHeight());
		}
		return panel_2;
	}

	private JLabel getLblWidth() {
		if (lblWidth == null) {
			lblWidth = new JLabel("New label");
		}
		return lblWidth;
	}

	private JLabel getLblHeight() {
		if (lblHeight == null) {
			lblHeight = new JLabel("New label");
		}
		return lblHeight;
	}

	private JButton getBtNewAction() {
		if (btNewAction == null) {
			btNewAction = new JButton("New DataSink / Action");
			btNewAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addDataSinkAction();
				}
			});
		}
		return btNewAction;
	}
	private JButton getBtnNewButton_4() {
		if (btnNewButton_4 == null) {
			btnNewButton_4 = new JButton("Add new Device");
			btnNewButton_4.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					startDeviceFactory();
				}
			});
		}
		return btnNewButton_4;
	}
}
