package sylladex;
import java.util.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DeckPreferences implements ActionListener, WindowListener, ChangeListener
{
	private JFrame preferences_frame;
	private JLabel preview;
	private JLabel author;
	private FetchModus modus;
	
	private Main m;
	
	private static final String core_prefs_file = "modi/prefs/preferences.txt";
	
	//Core preferences
		//Declarations
			private HashMap<String,String> core_preferences = new HashMap<String,String>();
			//Dock
				private boolean top; private JComboBox topbox;
				private boolean autohide_dock; private JCheckBox ahdock;
				private boolean always_on_top_dock; private JCheckBox aotdock;
				private boolean usb_mode; private JCheckBox usbmode;
				private int offset; private JSlider dock_offset;
			//Cards
				private boolean autohide_cards; private JCheckBox ahcards;
				private boolean always_on_top_cards; private JCheckBox aotcards;
				private boolean copy; private JCheckBox copybox;
			//Modus
				private String fetchmodus = "StackModus"; private JButton modusbutton;
		//Swing
			private JPanel sylladex_panel = new JPanel();
			private JPanel about_panel = new JPanel();
	//Modus preferences
		//Declarations
			private ArrayList<String> modus_preferences = new ArrayList<String>();
			private ArrayList<String> modus_items = new ArrayList<String>();
		//Swing
			private JPanel modus_panel = new JPanel();
			
	public DeckPreferences(Main m) throws FileNotFoundException
	{
		loadPreferences();
		this.m = m;
		//Find the modus from loaded preferences and instantiate
		try
		{
			File classes = new File("modi/");
			URL url = classes.toURI().toURL();
			URL[] urls = new URL[] {url};
			ClassLoader cl = new URLClassLoader(urls);
			Class<?> custom_modus = cl.loadClass(fetchmodus);
			modus = (FetchModus) custom_modus.getConstructor(m.getClass()).newInstance(m);
			loadItems();
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e){e.printStackTrace();}
		catch (SecurityException e){e.printStackTrace();}
		catch (InvocationTargetException e){e.printStackTrace();}
		catch (NoSuchMethodException e){e.printStackTrace();}
		catch (InstantiationException e){e.printStackTrace();}
		catch (IllegalAccessException e){e.printStackTrace();}
		
		createPreferencesFrame();
	}
	
	private void changeModus(Class<? extends FetchModus> c)
	{
		fetchmodus = c.getSimpleName();
		setPreferencesHashMap();
		savePreferences();
		cleanUp();
		modus_preferences = new ArrayList<String>();
		modus_items = new ArrayList<String>();
		try
		{
			modus = c.getConstructor(m.getClass()).newInstance(m);
		}
		catch (IllegalArgumentException e){e.printStackTrace();}
		catch (SecurityException e){e.printStackTrace();}
		catch (InvocationTargetException e){e.printStackTrace();}
		catch (NoSuchMethodException e){e.printStackTrace();}
		catch (InstantiationException e){e.printStackTrace();}
		catch (IllegalAccessException e){e.printStackTrace();}
		
		preferences_frame.setVisible(false);
		sylladex_panel = new JPanel();
		modus_panel = new JPanel();
		about_panel = new JPanel();
		try
		{
			loadPreferences();
			loadItems();
		}
		catch (FileNotFoundException e){ e.printStackTrace(); }
		createPreferencesFrame();
		
		m.changeModus(modus);
	}
	
	//Loading
	private void loadPreferences() throws FileNotFoundException
	{
		File prefs_file = new File(core_prefs_file);
		
		Scanner prefscanner = new Scanner(new FileReader(prefs_file));
		
		try
		{
			while(prefscanner.hasNextLine())
			{ processPrefLine(prefscanner.nextLine()); }
		}
		finally
		{ prefscanner.close(); }
		
		interpretPreferences(core_preferences);
	}

	private void loadItems()
	{
		modus_preferences = new ArrayList<String>();
		
		File modus_item_file = new File(modus.item_file);
		File modus_file = new File(modus.prefs_file);
		
		Scanner itemscanner = null;
		Scanner prefscanner = null;
		
		if(modus_item_file.exists() && modus_file.exists())
		{
			try
			{
				itemscanner = new Scanner(new FileReader(modus_item_file));
				prefscanner = new Scanner(new FileReader(modus_file));
				while(itemscanner.hasNextLine())
				{ processItemsLine(itemscanner.nextLine()); }
				while(prefscanner.hasNextLine())
				{ processModusLine(prefscanner.nextLine()); }
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			finally
			{ itemscanner.close(); prefscanner.close(); }
		}
	}
	
	private void interpretPreferences(HashMap<String,String> preferences)
	{
		top = Boolean.parseBoolean(preferences.get("top"));
		autohide_dock = Boolean.parseBoolean(preferences.get("autohide_dock"));
		always_on_top_dock = Boolean.parseBoolean(preferences.get("always_on_top_dock"));
		usb_mode = Boolean.parseBoolean(preferences.get("usb_mode"));
		offset = Integer.parseInt(preferences.get("offset"));
		
		autohide_cards = Boolean.parseBoolean(preferences.get("autohide_cards"));
		always_on_top_cards = Boolean.parseBoolean(preferences.get("always_on_top_cards"));
		copy = Boolean.parseBoolean(preferences.get("copy"));
		
		fetchmodus = preferences.get("fetchmodus");
	}
	
	private void setPreferencesHashMap()
	{
		String tops = new Boolean(top).toString();
		String autohide_docks = new Boolean(autohide_dock).toString();
		String always_on_top_docks = new Boolean(always_on_top_dock).toString();
		String usb_modes = new Boolean(usb_mode).toString();
		String offsets = new Integer(offset).toString();
		String autohide_cardss = new Boolean(autohide_cards).toString();
		String always_on_top_cardss = new Boolean(always_on_top_cards).toString();
		String copys = new Boolean(copy).toString();
		//No need for fetchmodus.toString!
		
		core_preferences.put("top", tops);
		core_preferences.put("autohide_dock", autohide_docks);
		core_preferences.put("always_on_top_dock", always_on_top_docks);
		core_preferences.put("usb_mode", usb_modes);
		core_preferences.put("offset", offsets);
		core_preferences.put("autohide_cards", autohide_cardss);
		core_preferences.put("always_on_top_cards", always_on_top_cardss);
		core_preferences.put("copy", copys);
		core_preferences.put("fetchmodus", fetchmodus);
	}
	
	private void savePreferences()
	{
		FileWriter writer;
		try
		{
			writer = new FileWriter(core_prefs_file);
			BufferedWriter bwriter = new BufferedWriter(writer);
			bwriter.write("top:" + core_preferences.get("top")); bwriter.newLine();
			bwriter.write("autohide_dock:" + core_preferences.get("autohide_dock")); bwriter.newLine();
			bwriter.write("always_on_top_dock:" + core_preferences.get("always_on_top_dock")); bwriter.newLine();
			bwriter.write("usb_mode:" + core_preferences.get("usb_mode")); bwriter.newLine();
			bwriter.write("offset:" + core_preferences.get("offset")); bwriter.newLine();
			bwriter.write("autohide_cards:" + core_preferences.get("autohide_cards")); bwriter.newLine();
			bwriter.write("always_on_top_cards:" + core_preferences.get("always_on_top_cards")); bwriter.newLine();
			bwriter.write("copy:" + core_preferences.get("copy")); bwriter.newLine();
			bwriter.write("fetchmodus:" + core_preferences.get("fetchmodus"));
			bwriter.close();
			
			writer = new FileWriter(modus.prefs_file);
			bwriter = new BufferedWriter(writer);
			for(String string : modus_preferences)
			{
				bwriter.write(string); bwriter.newLine();
			}
			bwriter.close();
			
			writer = new FileWriter(modus.item_file);
			bwriter = new BufferedWriter(writer);
			for(String string : modus_items)
			{
				bwriter.write(string); bwriter.newLine();
			}
			bwriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void processPrefLine(String line)
	{
		Scanner scanner = new Scanner(line);
		scanner.useDelimiter(":");
		if(scanner.hasNext())
		{
			String name = scanner.next();
			String value = scanner.next();
			core_preferences.put(name, value);
		}
	}
	
	private void processModusLine(String line)
	{
		modus_preferences.add(line);
	}
	
	private void processItemsLine(String line)
	{
		modus_items.add(line);
	}
	
	private String[] getModiAsStringArray()
	{
		File dir = new File("modi/");
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				if(name.endsWith(".class") && !name.contains("$"))
				{
					return true;
				}
				return false;
			}
		};
		return dir.list(filter);
	}

	//Exiting
	protected void cleanUp()
	{
		modus_preferences = modus.getPreferences();
		modus_items = modus.getItems();
		savePreferences();
	}
	
	//Access functions
	public boolean top()
	{
		return top;
	}
	
	public boolean autohide_dock()
	{
		return autohide_dock;
	}
	
	public boolean always_on_top_dock()
	{
		return always_on_top_dock;
	}
	
	public boolean usb_mode()
	{
		return usb_mode;
	}
	
	public int offset()
	{
		return offset;
	}
	
	public boolean autohide_cards()
	{
		return autohide_cards;
	}
	
	public boolean always_on_top_cards()
	{
		return always_on_top_cards;
	}
	
	public boolean copy_instead_of_move()
	{
		return copy;
	}
	
	public ArrayList<String> getModusPreferences()
	{
		return modus_preferences;
	}
	
	public ArrayList<String> getModusItems()
	{
		return modus_items;
	}
	
	public FetchModus getModus()
	{
		return modus;
	}
	
	//Swing
	private void createPreferencesFrame()
	{
		preferences_frame = new JFrame();
		preferences_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		preferences_frame.addWindowListener(this);
		JTabbedPane tabbedpane = new JTabbedPane();
		
		preferences_frame.setBackground(modus.getBackgroundColour());
		
		populateSylladexPanel();
		populateModusPanel();
		populateAboutPanel();
				
		tabbedpane.addTab("Sylladex", sylladex_panel);
		tabbedpane.addTab("Fetch Modus", modus_panel);
		tabbedpane.addTab("About", about_panel);
		
		preferences_frame.add(tabbedpane);
		preferences_frame.setResizable(false);
		preferences_frame.pack();
		preferences_frame.setLocationRelativeTo(null);
	}
	
	public void showPreferencesFrame()
	{
		preferences_frame.setVisible(true);
	}
	
	private void populateSylladexPanel()
	{
		sylladex_panel.setLayout(new BoxLayout(sylladex_panel, BoxLayout.PAGE_AXIS));
		//Components
		String[] topstrings = { "Bottom", "Top" };
		topbox = new JComboBox(topstrings);
			if(top){ topbox.setSelectedIndex(1); } else { topbox.setSelectedIndex(0); }
			topbox.addActionListener(this);
		
		dock_offset = new JSlider(JSlider.HORIZONTAL, 0, 200, offset);
			dock_offset.addChangeListener(this);
		
		ahdock = new JCheckBox("Auto-hide");
			ahdock.setSelected(autohide_dock);
			ahdock.addActionListener(this);
		aotdock = new JCheckBox("Always on top");
			aotdock.setSelected(always_on_top_dock);
			aotdock.addActionListener(this);
			
		usbmode = new JCheckBox("USB mode (files are copied to sylladex)");
			usbmode.setSelected(usb_mode);
			usbmode.addActionListener(this);
			usbmode.setEnabled(false);
			
		copybox = new JCheckBox("Copy outgoing files rather than moving them.");
			copybox.setSelected(copy);
			copybox.addActionListener(this);
			copybox.setEnabled(false);
			
		ahcards = new JCheckBox("Auto-hide");
			ahcards.setSelected(autohide_cards);
			ahcards.addActionListener(this);
		aotcards = new JCheckBox("Always on top");
			aotcards.setSelected(always_on_top_cards);
			aotcards.addActionListener(this);
		
		JPanel buttonpanel = new JPanel();
			modusbutton = new JButton("Select Fetch Modus");
				modusbutton.addActionListener(this);
				buttonpanel.add(modusbutton);
			JButton addbutton = new JButton("Add card");
				addbutton.addActionListener(this);
				addbutton.setActionCommand("add card");
				buttonpanel.add(addbutton);
			
		JPanel dockpanel = new JPanel(); dockpanel.setLayout(new BoxLayout(dockpanel, BoxLayout.PAGE_AXIS));
		JPanel cardpanel = new JPanel(); cardpanel.setLayout(new BoxLayout(cardpanel, BoxLayout.PAGE_AXIS));
		JPanel miscpanel = new JPanel(); miscpanel.setLayout(new BoxLayout(miscpanel, BoxLayout.PAGE_AXIS));
		buttonpanel.setLayout(new FlowLayout());
		
		dockpanel.setBorder(BorderFactory.createTitledBorder("Dock"));
		cardpanel.setBorder(BorderFactory.createTitledBorder("Cards"));
		miscpanel.setBorder(BorderFactory.createTitledBorder("Misc"));
		
		dockpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		cardpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		miscpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		buttonpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		dockpanel.add(topbox);
		dockpanel.add(dock_offset);
		dockpanel.add(ahdock);
		dockpanel.add(aotdock);
		
		cardpanel.add(ahcards);
		cardpanel.add(aotcards);
		
		miscpanel.add(usbmode);
		miscpanel.add(copybox);
		
		sylladex_panel.add(dockpanel);
		sylladex_panel.add(cardpanel);
		sylladex_panel.add(miscpanel);
		sylladex_panel.add(buttonpanel);
	}
	
	private void populateModusPanel()
	{
		modus_panel.add(modus.getPreferencesPanel());
	}
	
	private void populateAboutPanel()
	{
		String string =
			"<html>" +
			"<b>Sylladex Architect:</b><br/>" +
			"gumptiousCreator<br/>" +
			"-----<br/>" +
			"<b>Additional Codesmiths:</b><br/>" +
			"evacipatedBox<br/>" +
			"Nokob<br/>" +
			"-----<br/>" +
			"<b>Additional Appearance Sculptors:</b><br/>" +
			"aquaMarinist<br/>" +
			"-----<br/>" +
			"<b>Pixel Filchers:</b><br/>" +
			"aquaMarinist<br/>" +
			"The Cool<br/>" +
			"ZDG";
		about_panel.setLayout(new BoxLayout(about_panel, BoxLayout.PAGE_AXIS));
		about_panel.add(new JLabel(string));
	}

	//Modus browser
	public void createAndShowModusBrowser()
	{
		JFrame browser = new JFrame();
		browser.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		browser.setSize(625, 449);
		browser.setResizable(false);
		browser.setLayout(null);
		
		JPanel previewpanel = new JPanel();
		previewpanel.setBounds(310,10,295,401);
		previewpanel.setLayout(null);
		
		preview = new JLabel();
		preview.setBounds(0,0,295,376);
		
		author = new JLabel();
		author.setBounds(0,380,295,25);
		author.setHorizontalAlignment(JLabel.CENTER);
		
		previewpanel.add(preview);
		previewpanel.add(author);
		
		JLayeredPane modi = new JLayeredPane();
		modi.setBounds(10,10,295,376);
		populateModi(modi);
		
		browser.add(modi);
		browser.add(previewpanel);
		browser.setLocationRelativeTo(null);
		browser.setVisible(true);
	}
	
	private void populateModi(JLayeredPane pane)
	{
		String[] modistrings = getModiAsStringArray();
		ArrayList<FetchModus> modi = new ArrayList<FetchModus>();
		try
		{
			File classes = new File("modi/");
			URL url = classes.toURI().toURL();
			URL[] urls = new URL[] {url};
			ClassLoader cl = new URLClassLoader(urls);
			for(String modusstring : modistrings)
			{
				modusstring = modusstring.replaceAll("\\.class", "");
				Class<?> modus = cl.loadClass(modusstring);
				modi.add((FetchModus)modus.getConstructor(m.getClass()).newInstance((Object)null));
			}
		}
		catch (MalformedURLException e){e.printStackTrace();}
		catch (ClassNotFoundException e){e.printStackTrace();}
		catch (IllegalArgumentException e){e.printStackTrace();}
		catch (SecurityException e){e.printStackTrace();}
		catch (InstantiationException e){e.printStackTrace();}
		catch (IllegalAccessException e){e.printStackTrace();}
		catch (InvocationTargetException e){e.printStackTrace();}
		catch (NoSuchMethodException e){e.printStackTrace();}
		
		int offset = 0;
		int xpos = 0;
		int ypos = 0;
		for(FetchModus modus : modi)
		{
			ModusThumbnail thumbnail = new ModusThumbnail(modus);
			JLabel label = thumbnail.getLabel();
			
			xpos = ( (295-100-50)/modi.size() )*offset;
			ypos = ( (376-127)/modi.size() )*offset;
			
			label.setBounds(xpos, ypos, 295-xpos, 127);
			pane.setLayer(label, offset);
			pane.add(label);
			offset++;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if(source == topbox)
		{
			if(topbox.getSelectedIndex()==1){ top=true; } else { top=false; }
		}
		else if(source == modusbutton)
		{
			createAndShowModusBrowser();
		}
		else if(source == ahdock)
		{
			autohide_dock = ahdock.isSelected();
		}
		else if(source == aotdock)
		{
			always_on_top_dock = aotdock.isSelected();
		}
		else if(source == ahcards)
		{
			autohide_cards = ahcards.isSelected();
		}
		else if(source == aotcards)
		{
			always_on_top_cards = aotcards.isSelected();
		}
		else if(source == usbmode)
		{
			usb_mode = usbmode.isSelected();
		}
		else if(source == copybox)
		{
			copy = copybox.isSelected();
		}
		else if(e.getActionCommand().equals("add card"))
		{
			m.getModus().addCard();
		}
		m.refreshDock();
		m.refreshCardHolder();
		if(autohide_dock){ m.hideDock(); }
		if(autohide_cards) { m.getCardHolder().setVisible(false); }
	}
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == dock_offset)
		{
			offset = dock_offset.getValue();
		}
		m.showDock();
	}
	private class ModusThumbnail implements MouseListener
	{
		JLabel label;
		Icon smallimage;
		ImageIcon largeimage;
		FetchModus mymodus;
		public ModusThumbnail(FetchModus mod)
		{
			mymodus = mod;
			largeimage = Main.createImageIcon(mymodus.info_image);
			smallimage = Main.getSizedIcon(largeimage.getImage(), 100, 127);
			label = new JLabel(smallimage);
			label.setHorizontalAlignment(JLabel.LEFT);
			label.addMouseListener(this);
			
			if(modus.getClass().getSimpleName().equals(mymodus.getClass().getSimpleName()))
			{
				preview.setIcon(largeimage);
				author.setText(mymodus.info_author);
			}
		}

		public JLabel getLabel()
		{
			return label;
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			changeModus(mymodus.getClass());
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			label.setHorizontalAlignment(JLabel.CENTER);
			label.repaint();
			
			preview.setIcon(largeimage);
			preview.repaint();
			author.setText(mymodus.info_author);
		}

		@Override
		public void mouseExited(MouseEvent e)
		{
			label.setHorizontalAlignment(JLabel.LEFT);
			label.repaint();
		}

		@Override
		public void mousePressed(MouseEvent arg0){}
		@Override
		public void mouseReleased(MouseEvent arg0){}
	}

	@Override
	public void windowActivated(WindowEvent arg0){}
	@Override
	public void windowClosed(WindowEvent w)
	{
		
	}
	@Override
	public void windowClosing(WindowEvent w)
	{
		setPreferencesHashMap();
		savePreferences();
	}
	@Override
	public void windowDeactivated(WindowEvent arg0){}
	@Override
	public void windowDeiconified(WindowEvent arg0){}
	@Override
	public void windowIconified(WindowEvent arg0){}
	@Override
	public void windowOpened(WindowEvent arg0){}

}
