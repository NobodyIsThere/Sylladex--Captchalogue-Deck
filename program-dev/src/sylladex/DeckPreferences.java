package sylladex;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ui.SButton;
import util.RW;
import util.Util;

public class DeckPreferences implements ActionListener, WindowListener, ChangeListener
{
	private JFrame preferences_frame;
	private JLabel preview;
	private JLabel author;
	private FetchModus modus;
	
	private Main deck;
	
	private static final File core_prefs_file = new File("preferences/preferences.txt");
	private static final File item_file = new File("files/index.txt");
	
	// Combo box choices
	// Captchalogue type
	public static final int LINK = 0;
	public static final int COPY = 1;
	public static final int MOVE = 2;
	
	// Click actions
	public static final int OPEN = 0;
	public static final int OPEN_AND_KEEP = 1;
	public static final int OPEN_AND_EJECT = 2;
	public static final int FLIP = 3;
	public static final int POPUP_MENU = 4;
	
	// Main preferences
	private static final int PREF_TOP = 0;
	private static final int PREF_AUTOHIDE_DOCK = 1;
	private static final int PREF_ALWAYS_ON_TOP_DOCK = 2;
	private static final int PREF_OFFSET = 3;
	private static final int PREF_CAPTCHALOGUE_MODE = 4;
	private static final int PREF_NAME_ITEMS = 5;
	private static final int PREF_AUTO_CAPTCHA = 6;
	private static final int PREF_AUTOHIDE_CARDS = 7;
	private static final int PREF_ALWAYS_ON_TOP_CARDS = 8;
	private static final int PREF_NUMBER_OF_CARDS = 9;
	private static final int PREF_FETCHMODUS = 10;
	
	private static final int PREF_LEFT_CLICK = 11;
	private static final int PREF_LEFT_MOD_CLICK = 12;
	private static final int PREF_RIGHT_CLICK = 13;
	private static final int PREF_RIGHT_MOD_CLICK = 14;
	
	//Core preferences
		//Declarations
			private ArrayList<Object> core_preferences = new ArrayList<Object>();
			//Dock
				private JComboBox<String> topbox;
				private JCheckBox ahdock;
				private JCheckBox aotdock;
				private JComboBox<String> captchalogue_mode;
				private JSlider dock_offset;
				private JCheckBox nameitems;
				private JCheckBox autocaptcha;
			//Cards
				private JCheckBox ahcards;
				private JCheckBox aotcards;
			//Modus
				private JButton modusbutton;
			//Mouse
				private JComboBox<String> leftclick;
				private JComboBox<String> leftmodclick;
				private JComboBox<String> rightclick;
				private JComboBox<String> rightmodclick;
		//Swing
			private JPanel sylladex_panel = new JPanel();
			private JPanel mouse_panel = new JPanel();
			private JPanel about_panel = new JPanel();
	//Modus preferences
		//Declarations
			private ArrayList<String> modus_preferences = new ArrayList<String>();
			private ArrayList<String> modus_items = new ArrayList<String>();
		//Swing
			private JPanel modus_panel = new JPanel();
			
	public DeckPreferences(Main m)
	{
		loadPreferences();
		this.deck = m;
		//Find the modus from loaded preferences and instantiate
		loadModus((String) core_preferences.get(PREF_FETCHMODUS));
		loadItems();
		
		createPreferencesFrame();
	}
	
	private void loadModus(String modus_classname)
	{
		URLClassLoader cl = null;
		try
		{
			String folder = modus_classname.substring(0, modus_classname.lastIndexOf("/"));
			String name = modus_classname.substring(modus_classname.indexOf("/") + 1);
			System.out.println("Folder: " + folder + " Name: " + name);
			File classes = new File("modi/" + folder);
			URL url = classes.toURI().toURL();
			URL[] urls = new URL[] {url};
			cl = new URLClassLoader(urls);
			Class<? extends FetchModus> custom_modus = cl.loadClass(name).asSubclass(FetchModus.class);

			//Load inner classes too
			for (String filename : classes.list())
			{
				if (filename.startsWith(name) && filename.contains("$"))
				{
					cl.loadClass(filename.replace(".class", ""));
				}
			}
			
			modus = custom_modus.getConstructor(Main.class).newInstance(deck);
			modus.initialSettings();
		}
		catch (Exception e){ e.printStackTrace(); }
		try	{ cl.close(); } catch (Exception x) { x.printStackTrace(); }
	}
	
	private void changeModus(String modus_classname)
	{
		core_preferences.set(PREF_FETCHMODUS, modus_classname);
		savePreferences();
		cleanUp(deck.getModusItems());
		modus_preferences = new ArrayList<String>();
		modus_items = new ArrayList<String>();

		loadModus(modus_classname);
		
		preferences_frame.setVisible(false);
		sylladex_panel = new JPanel();
		mouse_panel = new JPanel();
		modus_panel = new JPanel();
		about_panel = new JPanel();
		
		loadItems();
		
		createPreferencesFrame();
		
		deck.changeModus(modus);
	}
	
	//Loading
	private void loadPreferences()
	{
		try
		{
			Scanner prefscanner = new Scanner(new FileReader(core_prefs_file));
			while(prefscanner.hasNextLine())
			{ core_preferences.add(processPrefLine(prefscanner.nextLine())); }
			prefscanner.close();
		}
		catch (FileNotFoundException e)
		{
			if (new File("preferences/").exists())
			{
				createPreferences();
				loadPreferences();
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Could not write preferences! Check cd/" + core_prefs_file);
			}
		}
	}

	private void loadItems()
	{
		modus_preferences = new ArrayList<String>();
		
		File modus_file = new File(modus.getSettings().get_preferences_file());
		
		Scanner itemscanner = null;
		Scanner prefscanner = null;
		
		if (!item_file.exists())
		{
			try { Files.createFile(item_file.toPath()); } catch (Exception x) { x.printStackTrace(); }
		}
		if (!modus_file.exists())
		{
			try { Files.createFile(modus_file.toPath()); } catch (Exception x) { x.printStackTrace(); }
		}
		try
		{
			itemscanner = new Scanner(new FileReader(item_file));
			prefscanner = new Scanner(new FileReader(modus_file));
			while(itemscanner.hasNextLine())
			{ processItemsLine(itemscanner.nextLine()); }
			while(prefscanner.hasNextLine())
			{ processModusLine(prefscanner.nextLine()); }
		}
		catch (FileNotFoundException e)
		{
			Util.error("Unable to load modus preferences!");
		}
		finally
		{ itemscanner.close(); prefscanner.close(); }
	}
	
	private void savePreferences()
	{
		FileWriter writer;
		try
		{
			writer = new FileWriter(core_prefs_file);
			BufferedWriter bwriter = new BufferedWriter(writer);
			for (Object o : core_preferences)
			{
				if (o instanceof String)
				{
					bwriter.write("STRING " + ":");
				}
				else if (o instanceof Boolean)
				{
					bwriter.write("BOOL " + ":");
				}
				else if (o instanceof Integer)
				{
					bwriter.write("INT " + ":");
				}
				bwriter.write(o.toString()); bwriter.newLine();
			}
			bwriter.close();
			
			RW.writeFile(modus_preferences, new File(modus.getSettings().get_preferences_file()));
			
			RW.writeFile(modus_items, item_file);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Unable to write preferences!\n" + e.getLocalizedMessage());
		}
	}
	
	private void createPreferences()
	{
		try
		{
			FileWriter writer = new FileWriter(core_prefs_file);
			BufferedWriter bwriter = new BufferedWriter(writer);
			bwriter.write("BOOL top:false"); bwriter.newLine();
			bwriter.write("BOOL autohide_dock:false"); bwriter.newLine();
			bwriter.write("BOOL always_on_top_dock:true"); bwriter.newLine();
			bwriter.write("INT captchalogue_mode:0"); bwriter.newLine();
			bwriter.write("INT offset:0"); bwriter.newLine();
			bwriter.write("BOOL name_items:true"); bwriter.newLine();
			bwriter.write("BOOL auto_captcha:false"); bwriter.newLine();
			bwriter.write("BOOL autohide_cards:false"); bwriter.newLine();
			bwriter.write("BOOL always_on_top_cards:true"); bwriter.newLine();
			bwriter.write("INT number_of_cards:4"); bwriter.newLine();
			bwriter.write("STRING fetchmodus:canon/StackModus"); bwriter.newLine();
			
			bwriter.write("INT leftclick:0"); bwriter.newLine();
			bwriter.write("INT leftmodclick:2"); bwriter.newLine();
			bwriter.write("INT rightclick:4"); bwriter.newLine();
			bwriter.write("INT rightmodclick:3");
			bwriter.close();
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Unable to read/write preferences file! Check cd/" + core_prefs_file);
			System.exit(1);
		}
	}
	
	private Object processPrefLine(String line)
	{
		String type = line.substring(0, line.indexOf(" "));
		if (type.contains("STRING"))
		{
			return line.substring(line.indexOf(":") + 1);
		}
		else if (type.contains("BOOL"))
		{
			return Boolean.parseBoolean(line.substring(line.indexOf(":") + 1));
		}
		else if (type.contains("INT"))
		{
			return Integer.parseInt(line.substring(line.indexOf(":") + 1));
		}
		return 1;
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
				if(name.endsWith(".fmi"))
				{
					return true;
				}
				return false;
			}
		};
		return dir.list(filter);
	}

	//Exiting
	protected void cleanUp(ArrayList<SylladexItem> item_list)
	{
		modus_preferences = modus.getPreferences();
		modus_items = new ArrayList<String>();
		for (SylladexItem item : item_list)
		{
			modus_items.add(item.getSaveString());
		}
		core_preferences.set(PREF_NUMBER_OF_CARDS, deck.getCards().size());
		savePreferences();
	}
	
	//Access functions
	/**
	 * @return True if the dock is set to display at the top of the screen. False otherwise.
	 */
	public boolean top()
	{
		return (boolean) core_preferences.get(PREF_TOP);
	}
	
	/**
	 * @return True if the dock is set to hide automatically. False otherwise.
	 */
	public boolean autohide_dock()
	{
		return (boolean) core_preferences.get(PREF_AUTOHIDE_DOCK);
	}
	
	/**
	 * @return True if the dock is set always to be on top. False otherwise.
	 */
	public boolean always_on_top_dock()
	{
		return (boolean) core_preferences.get(PREF_ALWAYS_ON_TOP_DOCK);
	}
	
	/**
	 * @return True if the program is in "USB Mode".
	 */
	public int captchalogue_mode()
	{
		return (int) core_preferences.get(PREF_CAPTCHALOGUE_MODE);
	}
	
	/**
	 * @return The offset of the dock from the top/bottom of the screen.
	 */
	public int offset()
	{
		return (int) core_preferences.get(PREF_OFFSET);
	}
	
	/**
	 * @return True if the program asks for the names of images when captchalogued. False otherwise.
	 */
	public boolean name_items()
	{
		return (boolean) core_preferences.get(PREF_NAME_ITEMS);
	}
	
	/**
	 * @return True if the cards are set to auto-hide. False otherwise.
	 */
	public boolean autohide_cards()
	{
		return (boolean) core_preferences.get(PREF_AUTOHIDE_CARDS);
	}
	
	/**
	 * @return True if the cards are set always to be on top. False otherwise.
	 */
	public boolean always_on_top_cards()
	{
		return (boolean) core_preferences.get(PREF_ALWAYS_ON_TOP_CARDS);
	}
	
	public int number_of_cards()
	{
		return (int) core_preferences.get(PREF_NUMBER_OF_CARDS);
	}
	
	public boolean auto_captcha()
	{
		return (boolean) core_preferences.get(PREF_AUTO_CAPTCHA);
	}
	
	/**
	 * @return The modus preferences.
	 */
	public ArrayList<String> getModusPreferences()
	{
		return modus_preferences;
	}
	
	/**
	 * @return The captchalogued items.
	 */
	public ArrayList<String> getModusItems()
	{
		return modus_items;
	}
	
	/**
	 * @return The current fetch modus.
	 */
	public FetchModus getModus()
	{
		return modus;
	}
	
	public int leftClickAction()
	{
		return (int) core_preferences.get(PREF_LEFT_CLICK);
	}
	
	public int leftModClickAction()
	{
		return (int) core_preferences.get(PREF_LEFT_MOD_CLICK);
	}
	
	public int rightClickAction()
	{
		return (int) core_preferences.get(PREF_RIGHT_CLICK);
	}
	
	public int rightModClickAction()
	{
		return (int) core_preferences.get(PREF_RIGHT_MOD_CLICK);
	}
	
	//Swing
	private void createPreferencesFrame()
	{
		preferences_frame = new JFrame();
		preferences_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		preferences_frame.addWindowListener(this);
		preferences_frame.setIconImage(Util.createImageIcon(modus.getSettings().get_card_image()).getImage());
		JTabbedPane tabbedpane = new JTabbedPane();
		
		preferences_frame.setBackground(modus.getSettings().get_background_color());
		
		populateSylladexPanel();
		populateMousePanel();
		populateModusPanel();
		populateAboutPanel();
				
		tabbedpane.addTab("Sylladex", sylladex_panel);
		tabbedpane.addTab("Mouse", mouse_panel);
		tabbedpane.addTab("Fetch Modus", modus_panel);
		tabbedpane.addTab("About", about_panel);
		
		preferences_frame.add(tabbedpane);
		preferences_frame.setResizable(false);
		preferences_frame.pack();
		preferences_frame.setLocationRelativeTo(null);
	}
	
	/**
	 * Shows the preferences window.
	 */
	public void showPreferencesFrame()
	{
		preferences_frame.setVisible(true);
	}
	
	private void populateSylladexPanel()
	{
		sylladex_panel.setLayout(new BoxLayout(sylladex_panel, BoxLayout.PAGE_AXIS));
		//Components
		String[] topstrings = { "Bottom", "Top" };
		topbox = new JComboBox<String>(topstrings);
			if((boolean) core_preferences.get(PREF_TOP))
			{ topbox.setSelectedIndex(1); } else { topbox.setSelectedIndex(0); }
			topbox.addActionListener(this);
		
		dock_offset = new JSlider(JSlider.HORIZONTAL, -90, 200, 
				(int) core_preferences.get(PREF_OFFSET));
			dock_offset.addChangeListener(this);
		
		ahdock = new JCheckBox("Auto-hide");
			ahdock.setSelected((boolean) core_preferences.get(PREF_AUTOHIDE_DOCK));
			ahdock.addActionListener(this);
		aotdock = new JCheckBox("Always on top");
			aotdock.setSelected((boolean) core_preferences.get(PREF_ALWAYS_ON_TOP_DOCK));
			aotdock.addActionListener(this);
			
		captchalogue_mode = new JComboBox<String>();
			captchalogue_mode.addItem("Link");
			captchalogue_mode.addItem("Copy");
			captchalogue_mode.addItem("Move");
			captchalogue_mode.setSelectedIndex((int) core_preferences.get(PREF_CAPTCHALOGUE_MODE));
			captchalogue_mode.addActionListener(this);
			
		autocaptcha = new JCheckBox("Auto-captchalogue clipboard");
			autocaptcha.setSelected((boolean) core_preferences.get(PREF_AUTO_CAPTCHA));
			autocaptcha.addActionListener(this);
			
		nameitems = new JCheckBox("Always prompt for image names");
			nameitems.setSelected((boolean) core_preferences.get(PREF_NAME_ITEMS));
			nameitems.addActionListener(this);
			
		ahcards = new JCheckBox("Auto-hide");
			ahcards.setSelected((boolean) core_preferences.get(PREF_AUTOHIDE_CARDS));
			ahcards.addActionListener(this);
		aotcards = new JCheckBox("Always on top");
			aotcards.setSelected((boolean) core_preferences.get(PREF_ALWAYS_ON_TOP_CARDS));
			aotcards.addActionListener(this);
		
		JPanel buttonpanel = new JPanel();
			modusbutton = new SButton("Select Fetch Modus");
				modusbutton.addActionListener(this);
				buttonpanel.add(modusbutton);
			JButton removebutton = new JButton("Remove card");
				removebutton.addActionListener(this);
				removebutton.setActionCommand("remove card");
				buttonpanel.add(removebutton);
			
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
		
		miscpanel.add(nameitems);
		miscpanel.add(autocaptcha);
		miscpanel.add(captchalogue_mode); miscpanel.add(new JLabel("files to sylladex"));
		
		sylladex_panel.add(dockpanel);
		sylladex_panel.add(cardpanel);
		sylladex_panel.add(miscpanel);
		sylladex_panel.add(buttonpanel);
	}
	
	private void populateMousePanel()
	{
		mouse_panel.setLayout(new BoxLayout(mouse_panel, BoxLayout.PAGE_AXIS));
		
		mouse_panel.add(new JLabel("Left click: "));
		leftclick = new JComboBox<String>();
			leftclick.addItem("Open");
			leftclick.addItem("Open and keep");
			leftclick.addItem("Open and eject");
			leftclick.addItem("Flip");
			leftclick.addItem("Open popup menu");
			leftclick.setSelectedIndex((int) core_preferences.get(PREF_LEFT_CLICK));
			leftclick.addActionListener(this);
			mouse_panel.add(leftclick);
		
		mouse_panel.add(new JLabel("Shift left click: "));
		leftmodclick = new JComboBox<String>();
			leftmodclick.addItem("Open");
			leftmodclick.addItem("Open and keep");
			leftmodclick.addItem("Open and eject");
			leftmodclick.addItem("Flip");
			leftmodclick.addItem("Open popup menu");
			leftmodclick.setSelectedIndex((int) core_preferences.get(PREF_LEFT_MOD_CLICK));
			leftmodclick.addActionListener(this);
			mouse_panel.add(leftmodclick);
			
		mouse_panel.add(new JLabel("Right click: "));
		rightclick = new JComboBox<String>();
			rightclick.addItem("Open");
			rightclick.addItem("Open and keep");
			rightclick.addItem("Open and eject");
			rightclick.addItem("Flip");
			rightclick.addItem("Open popup menu");
			rightclick.setSelectedIndex((int) core_preferences.get(PREF_RIGHT_CLICK));
			rightclick.addActionListener(this);
			mouse_panel.add(rightclick);
			
		mouse_panel.add(new JLabel("Shift right click: "));
		rightmodclick = new JComboBox<String>();
			rightmodclick.addItem("Open");
			rightmodclick.addItem("Open and keep");
			rightmodclick.addItem("Open and eject");
			rightmodclick.addItem("Flip");
			rightmodclick.addItem("Open popup menu");
			rightmodclick.setSelectedIndex((int) core_preferences.get(PREF_RIGHT_MOD_CLICK));
			rightmodclick.addActionListener(this);
			mouse_panel.add(rightmodclick);
	}
	
	private void populateModusPanel()
	{
		modus_panel.add(modus.getPreferencesPanel());
	}
	
	private void populateAboutPanel()
	{
		String string =
			"<html>" +
			"<b>Concept and art:</b><br/>" +
			"Andrew Hussie<br/>" +
			"<a href=\"http://www.mspaintadventures.com\">www.mspaintadventures.com</a><br/>" +
			"-----<br/>" +
			"<b>Sylladex Architect:</b><br/>" +
			"gumptiousCreator<br/>" +
			"-----<br/>" +
			"<b>Additional Codesmiths:</b><br/>" +
			"evacipatedBox<br/>" +
			"mezzoEmrys<br/>" +
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
	/**
	 * Shows the modus selection window.
	 */
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
		
		int offset = 0;
		int xpos = 0;
		int ypos = 0;
		for(String s : modistrings)
		{
			ModusThumbnail thumbnail = new ModusThumbnail(s);
			JLabel label = thumbnail.getLabel();
			
			xpos = ( (295-100-50)/modistrings.length )*offset;
			ypos = ( (376-127)/modistrings.length )*offset;
			
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
			if(topbox.getSelectedIndex()==1)
			{ core_preferences.set(PREF_TOP, true); }
			else { core_preferences.set(PREF_TOP, false); }
		}
		else if(source == modusbutton)
		{
			createAndShowModusBrowser();
		}
		else if(source == ahdock)
		{
			core_preferences.set(PREF_AUTOHIDE_DOCK, ahdock.isSelected());
		}
		else if(source == aotdock)
		{
			core_preferences.set(PREF_ALWAYS_ON_TOP_DOCK, aotdock.isSelected());
		}
		else if(source == ahcards)
		{
			core_preferences.set(PREF_AUTOHIDE_CARDS, ahcards.isSelected());
		}
		else if(source == aotcards)
		{
			core_preferences.set(PREF_ALWAYS_ON_TOP_CARDS, aotcards.isSelected());
		}
		else if(source == nameitems)
		{
			core_preferences.set(PREF_NAME_ITEMS, nameitems.isSelected());
		}
		else if(source == captchalogue_mode)
		{
			core_preferences.set(PREF_CAPTCHALOGUE_MODE, captchalogue_mode.getSelectedIndex());
		}
		else if(source == autocaptcha)
		{
			core_preferences.set(PREF_AUTO_CAPTCHA, autocaptcha.isSelected());
		}
		else if(e.getActionCommand().equals("remove card"))
		{
			deck.removeCard();
		}
		else if (e.getSource().equals(leftclick))
		{
			core_preferences.set(PREF_LEFT_CLICK, leftclick.getSelectedIndex());
		}
		else if (e.getSource().equals(leftmodclick))
		{
			core_preferences.set(PREF_LEFT_MOD_CLICK, leftmodclick.getSelectedIndex());
		}
		else if (e.getSource().equals(rightclick))
		{
			core_preferences.set(PREF_RIGHT_CLICK, rightclick.getSelectedIndex());
		}
		else if (e.getSource().equals(rightmodclick))
		{
			core_preferences.set(PREF_RIGHT_MOD_CLICK, rightmodclick.getSelectedIndex());
		}
		deck.refreshDock();
		deck.refreshCardHolder();
		if((boolean) core_preferences.get(PREF_AUTOHIDE_DOCK)){ deck.hideDock(); }
		if((boolean) core_preferences.get(PREF_AUTOHIDE_CARDS)) { deck.getCardHolder().setVisible(false); }
	}
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == dock_offset)
		{
			core_preferences.set(PREF_OFFSET, dock_offset.getValue());
		}
		deck.showDock();
	}
	
	private class ModusThumbnail implements MouseListener
	{
		JLabel label;
		Icon smallimage;
		ImageIcon largeimage;

		ArrayList<String> info;
		
		public ModusThumbnail(String s)
		{
			info = RW.readFile(new File("modi/" + s));
			
			largeimage = Util.createImageIcon("modi/" + info.get(2));
			smallimage = Util.getSizedIcon(largeimage.getImage(), 100, 127);
			label = new JLabel(smallimage);
			label.setHorizontalAlignment(JLabel.LEFT);
			label.addMouseListener(this);
			
			if(modus.getSettings().get_name().equals(info.get(0)))
			{
				preview.setIcon(largeimage);
				author.setText(info.get(1));
			}
		}

		public JLabel getLabel()
		{
			return label;
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			changeModus(info.get(3));
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			label.setHorizontalAlignment(JLabel.CENTER);
			label.repaint();
			
			preview.setIcon(largeimage);
			preview.repaint();
			author.setText(info.get(1));
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
