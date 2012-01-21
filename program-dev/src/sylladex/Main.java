package sylladex;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import sun.awt.shell.ShellFolder;
import com.sun.awt.AWTUtilities;

/** Holds all the cards. Also provides utility functions for the other classes. */
public class Main implements ActionListener, WindowListener
{
	//Should be called "Sylladex", but has to be called "Main".
	//Controls everything.
	
	private DeckPreferences prefs;
	private FetchModus modus;
	private FetchModusSettings modus_settings;
	private ArrayList<SylladexCard> sylladexcards = new ArrayList<SylladexCard>();
	private ArrayList<JLabel> icons = new ArrayList<JLabel>();
	
	private JWindow dock;
	private JWindow cardholder;
	private JLayeredPane pane;
	private JLayeredPane iconpane;
	private JLayeredPane cardpane;
	private int deckwidth;
	private int id;
	
	private SystemTray tray;
	private PopupMenu popup;
	private TrayIcon icon = new TrayIcon(createImageIcon("modi/stack/card.png").getImage());
	private PopupListener popuplistener = new PopupListener();
	
	private Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
	
	private DockListener docklistener = new DockListener();
	private CardListener cardlistener = new CardListener();
	private Timer dautohide_timer = new Timer(500, this);
	private Timer dautoshow_timer = new Timer(500, this);
	private Timer cautohide_timer = new Timer(2000, this);
	private boolean d_hidden = false;
	
	private MyClipboardOwner co = new MyClipboardOwner();
	
	public static void main(String[] args)
	{
		//Okay. Start everything going.
		new Main();
	}
	
	public Main()
	{
		//SynthLookAndFeel lf = new SynthLookAndFeel();
		try
		{
			//laf.load(Main.class.getResourceAsStream("files/theme.xml"), Main.class);
			//UIManager.setLookAndFeel(lf);
			
			prefs = new DeckPreferences(this);
		}
		//catch (Exception e) { //Don't use this lf }
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			//TODO: We can't continue without preferences.
		}
		modus = prefs.getModus();
		modus_settings = modus.getModusSettings();
		
		modus.setPreferences(prefs.getModusPreferences());
		modus.setItems(prefs.getModusItems());
		id = 0;
		createCardHolder();
		while(sylladexcards.size()<modus_settings.get_initial_card_number())
		{
			addCardWithoutRefresh();
		}
		createDock();
		refreshDock();
		modus.prepare();
		
		//Make sure contents are saved on Mac Cmd+Q
		if(isMac())
		{
			Runnable exithook = new Runnable()
			{
				public void run()
				{
					prefs.cleanUp();
				}
			};
			Runtime.getRuntime().addShutdownHook(new Thread(exithook,"Contents save hook (OSX)"));
		}
		
		if(prefs.autohide_cards()){ cardholder.setVisible(false); }
	}
	
	protected void changeModus(FetchModus m)
	{
		icons = new ArrayList<JLabel>();
		modus = m;
		modus_settings = m.getModusSettings();
		modus.setPreferences(prefs.getModusPreferences());
		modus.setItems(prefs.getModusItems());
		id = 0;
		sylladexcards.clear();
		cardholder.setVisible(false);
		cardholder.dispose();
		createCardHolder();
		while(sylladexcards.size()<modus_settings.get_initial_card_number())
		{ addCardWithoutRefresh(); }
		refreshDock();
		modus.prepare();
	}
	
	//Transfer Handler
	private class FileDropHandler extends TransferHandler
	{
		private static final long serialVersionUID = 1L;
		
		public boolean canImport(TransferSupport ts)
		{
			if (!ts.isDrop())
			{
				return false;
			}
			
			// accepting strings, images and file lists
			if(ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
					|| ts.isDataFlavorSupported(DataFlavor.stringFlavor)
					|| ts.isDataFlavorSupported(DataFlavor.imageFlavor))
			{
				//TODO
				showDock();
				return true;
			}
			return false;
		}
		
		@SuppressWarnings("unchecked")
		public boolean importData(TransferSupport ts)
		{
			if (!canImport(ts))
			{
				return false;
			}
			
			Transferable t = ts.getTransferable();
			
			try
			{
				if(ts.isDataFlavorSupported(DataFlavor.imageFlavor))
				{
					Image image = (Image)t.getTransferData(DataFlavor.imageFlavor);
					addItem(image);
				}
				else if(ts.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					String string = (String)t.getTransferData(DataFlavor.stringFlavor);
					System.out.println("Data: " + string);
					addItem(string);
				}
				else if(ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					java.util.List<File> fileList = (java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
					for (File file : fileList)
					{
						if(file.getName().endsWith(".class") || file.getName().endsWith(".sdw"))
						{
							Widget widget = loadWidget(file);
							addItem(widget);
						}
						else
						{
							addItem(file);
						}
					}
				}
			}
			catch (UnsupportedFlavorException e)
			{
				e.printStackTrace();
				return false;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
	}
	
	private static class ImageSelection implements Transferable
	{
		private Image image;
		
		public ImageSelection(Image image)
		{
			this.image = image;
		}
		
		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}
		
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return DataFlavor.imageFlavor.equals(flavor);
		}
		
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
		{
			if (!DataFlavor.imageFlavor.equals(flavor)) { throw new UnsupportedFlavorException(flavor); }
			return image;
		}
	}
	
	//Dock
	private void createDock()
	{
		createSystemTray();
		
		dock = new JWindow();
		pane = new JLayeredPane();
		dock.setLayeredPane(pane);
		//Window stuff
		dock.addWindowListener(this);
		//Components
		dock.setLayout(null);
		refreshDock();
		//Drag and drop support
		FileDropHandler h = new FileDropHandler();
		dock.setTransferHandler(h);
		//Show the frame
		dock.setVisible(true);
	}
	
	/** Updates the dock position and refreshes the dock icons. */
	public void refreshDock()
	{
		if(prefs.always_on_top_dock() || prefs.autohide_dock())
			dock.setAlwaysOnTop(true);
		else
			dock.setAlwaysOnTop(false);
		
		refreshSystemTray();
		
		dock.setIconImage(createImageIcon(modus_settings.get_card_image()).getImage());
		deckwidth = screensize.width;
		dock.setSize(new Dimension(deckwidth,100));
		hideDock();
		if(prefs.top() && !prefs.autohide_dock())
		{ dock.setLocation(new Point(0,prefs.offset())); }
		else if(!prefs.autohide_dock())
		{ dock.setLocation(new Point(0,screensize.height-100-prefs.offset())); }
		
		pane.removeAll();
		addComponentsToDock();
		pane.validate();
		
		if(modus_settings.draw_default_dock_icons())
		{
			drawDefaultDockIcons();
		}
		
		refreshDockIcons();
		dautohide_timer.stop();
		dautoshow_timer.stop();
		cautohide_timer.stop();
	}
	
	private void createSystemTray()
	{
		if(!SystemTray.isSupported()){ return; }
		popup = new PopupMenu();
		
		MenuItem preferences = new MenuItem("Preferences");
			preferences.addActionListener(popuplistener);
			preferences.setActionCommand("preferences");
		popup.add(preferences);
		
		MenuItem exit = new MenuItem("Exit");
			exit.addActionListener(popuplistener);
			exit.setActionCommand("exit");
		popup.add(exit);
		
		tray = SystemTray.getSystemTray();
		icon = new TrayIcon(createImageIcon("modi/global/trayicon.gif").getImage());
		
		icon.setPopupMenu(popup);
		try
		{
			tray.add(icon);
		}
		catch(Exception e){ e.printStackTrace(); }
	}
	
	private void refreshSystemTray()
	{
	}
	
	private void addComponentsToDock()
	{
		int textoffset;
		String bgpath;
		if(prefs.top()==true)
		{
			textoffset = 21;
			bgpath = modus_settings.get_top_dock_image();
		}
		else
		{
			textoffset = 94;
			bgpath = modus_settings.get_bottom_dock_image();
		}
		
		//Background image
		ImageIcon dockbgimage = createImageIcon(bgpath);
		ImageIcon docktextimage = createImageIcon(modus_settings.get_dock_text_image());
		int x = 0;
		while(x<deckwidth)
		{
			JLabel dockbg = new JLabel(dockbgimage);
			pane.setLayer(dockbg, 0);
			pane.add(dockbg);
			dockbg.setBounds(x,0,500,100);
			x+=500;
		}
		
		//Background
		pane.setLayer(modus.getBackground(), 1);
		modus.getBackground().setBounds(0,0,deckwidth,100);
		modus.getBackground().setOpaque(false);
		
		modus.getBackground().removeMouseListener(docklistener);
		modus.getBackground().removeMouseMotionListener(docklistener);
		modus.getBackground().addMouseListener(docklistener);
		modus.getBackground().addMouseMotionListener(docklistener);
		
		pane.add(modus.getBackground());
		
		//Text
		JLabel docktext = new JLabel(docktextimage);
		pane.setLayer(docktext, 100);
		docktext.setBounds(10,100-textoffset,211,16);
		pane.add(docktext);
		
		//Icons
		iconpane = new JLayeredPane();
		iconpane.setBounds(0,0,deckwidth,100);
		pane.setLayer(iconpane, 200);
		pane.add(iconpane);
		
		//Foreground
		pane.setLayer(modus.getForeground(), 199);
		modus.getForeground().setBounds(0,0,deckwidth,100);
		modus.getForeground().setOpaque(false);
		pane.add(modus.getForeground());
	}
	
	private void drawDefaultDockIcons()
	{
		int numcards = sylladexcards.size();
		ImageIcon cardbg = createImageIcon(modus_settings.get_dock_card_image());
		int x;
		int y;
		int i = 0;
		if(prefs.top()==true) { y=10; } else { y=35; }
		if(numcards>deckwidth/50)
		{
			int extracards = sylladexcards.size() - Math.round(deckwidth/50);
			x = 20;
			while(extracards>0)
			{
				JLabel card = new JLabel(cardbg);
				card.setBounds(x,y-5,43,60);
				pane.setLayer(card, 10);
				pane.add(card);
				extracards--;
				x+=50;
			}
			x = 15;
			while(x<deckwidth-50)
			{
				JLabel card = new JLabel(cardbg);
				card.setBounds(x,y,43,60);
				pane.setLayer(card, 20);
				pane.add(card);
				x+=50;
			}
		}
		else
		{
			x = deckwidth/2 - numcards*25;
			while(i<numcards)
			{
				JLabel card = new JLabel(cardbg);
				card.setBounds(x,y,43,60);
				pane.setLayer(card, 20);
				pane.add(card);
				x+=50;
				i++;
			}
		}
	}
	
	/** Refreshes the dock icons. Icons are separated by a distance of 50 pixels, minus the width of the icons (33 pixels).
	 * @see setIcons() */
	public void refreshDockIcons()
	{
		iconpane.removeAll();
		int x;
		int y;
		if(modus_settings.draw_default_dock_icons())
		{
			if(sylladexcards.size() < deckwidth/50)
			{ x = deckwidth/2 - sylladexcards.size()*25; }
			else { x = 16; }
		}
		else
		{ x = deckwidth/2 - icons.size()*25; }
		y = getDockIconYPosition();
		int i = 0;
		while(i<icons.size())
		{
			JLabel label = icons.get(i);
			label.setBounds(x+5,y+5,33,60);
			iconpane.add(label);
			x+=50;
			i++;
		}
		iconpane.validate();
		iconpane.repaint();
	}
	
	/** Sets the icons to be drawn on the dock, then calls {@link refreshDockIcons()}.
	 * @param newicons The new icons.*/
	public void setIcons(ArrayList<JLabel> newicons)
	{
		icons = newicons;
		refreshDockIcons();
	}
	
	/** Un-hides the dock. */
	public void showDock()
	{
		if(prefs.top())
		{
			dock.setLocation(0,prefs.offset());
		}
		else
		{
			dock.setLocation(0,screensize.height-100-prefs.offset());
		}
		d_hidden = false;
	}
	
	/** Hides the dock. */
	public void hideDock()
	{
		if(prefs.top())
			dock.setLocation(0,-99+prefs.offset());
		else
			dock.setLocation(0,screensize.height-1-prefs.offset());
		d_hidden = true;
	}
	
	//Card holder
	private void createCardHolder()
	{
		cardholder = new JWindow();
		cardpane = new JLayeredPane();
		cardholder.setLayeredPane(cardpane);
		cardholder.setLocation(modus_settings.get_origin());
		
		setTransparent(cardholder);
		
		cardholder.setSize(10, 10);
		cardholder.setLayout(null);
		
		cardholder.addMouseListener(cardlistener);
		cardholder.setTransferHandler(new FileDropHandler());
		
		cardholder.setVisible(true);
	}
	
	/** Updates the always-on-top status of the card window, and repaints the window.*/
	public void refreshCardHolder()
	{
		if(prefs.always_on_top_cards() || prefs.autohide_cards())
			cardholder.setAlwaysOnTop(true);
		else
			cardholder.setAlwaysOnTop(false);
		
		cardholder.repaint();
	}
	
	/** Resizes the card window.
	 * @param w - width of the window.
	 * @param h - height of the window.*/
	public void setCardHolderSize(int w, int h)
	{
		cardholder.setSize(w,h);
		cardholder.repaint();
	}
	
	/** Returns the card window. For advanced use only.*/
	public JWindow getCardHolder()
	{
		return cardholder;
	}
	
	//Card functions
	private void addCardWithoutRefresh()
	{
		sylladexcards.add(new SylladexCard(id, this));
		id++;
	}
	
	/** Adds a card to the deck, and refreshes the dock.*/
	public void addCard()
	{
		sylladexcards.add(new SylladexCard(id, this));
		refreshDock();
		id++;
	}
	
	protected Widget loadWidget(File file)
	{
		try
		{
			URL[] url = { new File("widgets/").toURI().toURL() };
			ClassLoader cl = new URLClassLoader(url);
			String name = file.getName().replaceAll("\\.class?", "");
			name = name.replaceAll("\\.sdw?", "");
			Class<?> wclass = cl.loadClass(name);
			Widget widget = (Widget) wclass.newInstance();
			widget.setMain(this);
			widget.prepare();
			return widget;
		}
		catch (Exception e) {e.printStackTrace();}
		return null;
	}
	
	private void addItem(String string)
	{
		string = string.replaceAll("http://", "");
		java.net.URL url = null;
		try
		{
			url = new java.net.URL(string);
		}
		catch (MalformedURLException e)
		{
			//It's a string.
			modus.addItem(string);
			return;
		}
		
		File file = null;
		try
		{
			file = new File(url.toURI().getPath());
			System.out.println("File created from URI.");
		}
		catch (URISyntaxException e)
		{
			file = new File(url.getPath());
			System.out.println("File created from URL.");
		}
		
		System.out.println("Adding to modus...");
		modus.addItem(file);
		System.out.println("Done!");
	}
	
	private void addItem(Image image)
	{
		modus.addItem(image);
	}
	
	private void addItem(File file)
	{
		modus.addItem(file);
	}
	
	private void addItem(Widget widget)
	{
		widget.add();
		modus.addItem(widget);
	}
	
	private void openCard(SylladexCard card)
	{
		SylladexItem item = card.getItem();
		Object o = item.getContents();
		
		if(o instanceof File)
		{
			try
			{
				if(Desktop.isDesktopSupported())
				{
					Desktop.getDesktop().open((File)o);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else if(o instanceof String)
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection((String)o), co);
		}
		else if(o instanceof Image)
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection((Image)o), co);
		}
		else if(o instanceof Widget)
		{
			((Widget)o).open();
		}
	}
	
	/** Opens the specified sylladex card.
	 * 
	 * @param card - The sylladex card to open.
	 * @see openWithoutRemoval(SylladexCard card)
	 * */
	public void open(SylladexCard card)
	{
		openCard(card);
		card.setItem(null);
	}
	
	/** Opens the specified sylladex card, without removing its item from the deck.
	 * 
	 * @param card - The sylladex card to open.
	 * @see open(SylladexCard card)
	 */
	public void openWithoutRemoval(SylladexCard card)
	{
		openCard(card);
	}
	
	/** Removes the specified card from the deck, without opening its item.
	 * 
	 * @param card - The sylladex card to remove.
	 */
	public void removeCard(SylladexCard card)
	{
		sylladexcards.remove(card);
		refreshDock();
	}
	
	/** Returns an empty sylladex card, if there are any left in the deck.
	 * 
	 * @return An empty sylladex card if one exists, otherwise null.
	 */
	public SylladexCard getNextEmptyCard()
	{
		for(SylladexCard card : sylladexcards)
		{
			if(card.isEmpty())
			{
				return card;
			}
		}
		return null;
	}
	
	//Utility functions
	/** Returns the sylladex card with the specified index. This is not a very useful function, in general.
	 * 
	 * @param index - the index of the required card
	 * @return The sylladex card with the specified index.
	 */
	public SylladexCard getCardWithIndex(int index)
	{
		return sylladexcards.get(index);
	}
	
	@Deprecated
	protected SylladexCard getCardWithId(int id)
	{
		int i = 0;
		while(i<sylladexcards.size())
		{
			SylladexCard card = sylladexcards.get(i);
			if(card.getId()==id)
			{
				return card;
			}
			i++;
		}
		return null;
	}
	
	/** Returns the sylladex card at the point specified. The point is measured relative to the card window.
	 * 
	 * @param position - The position of the required card's top left corner.
	 * @return The sylladex card at the specified position.
	 */
	public SylladexCard getCardAtPosition(Point position)
	{
		for(SylladexCard card : sylladexcards)
		{
			if(card.getPosition().x == position.x
					&& card.getPosition().y == position.y)
			{
				return card;
			}
		}
		return null;
	}
	
	/**
	 * @return An ArrayList of the cards currently in the deck.
	 */
	public ArrayList<SylladexCard> getCards()
	{
		return sylladexcards;
	}
	
	/**
	 * @return The current fetch modus.
	 */
	public FetchModus getModus()
	{
		return modus;
	}
	
	protected FetchModusSettings getModusSettings()
	{
		return modus_settings;
	}
	
	/**
	 * @return The size of the screen. On Windows, this is the primary monitor, as far as I can tell.
	 */
	public Dimension getScreenSize()
	{
		return screensize;
	}
	
	/**
	 * 
	 * @return An object containing the program preferences.
	 */
	public DeckPreferences getPreferences()
	{
		return prefs;
	}
	
	/**
	 * 
	 * @return The y-position at which cards are drawn on the dock. This is equal to 10 if the dock is at the top of the screen,
	 * and 35 if it is at the bottom.
	 */
	public int getDockIconYPosition()
	{
		if(prefs.top()==true) { return 10; } return 35;
	}
	
	/**
	 * Creates an ImageIcon from the file at the specified path.
	 * @param path - the path of the file, relative to SDECK.jar.
	 * @return An ImageIcon created from the file. If it fails to create an ImageIcon, null is returned.
	 */
	public static ImageIcon createImageIcon(String path)
	{
		java.net.URL url = null;
		ImageIcon icon = null;
		try
		{
			url = new File(path).toURI().toURL();
			Image image = Toolkit.getDefaultToolkit().getImage(url);
			icon = new ImageIcon(image);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		return icon;
	}
	
	/**
	 * Creates a JLabel based on the contents of the SylladexItem. This is what is displayed on the sylladex cards.
	 * @param item - The item to use.
	 * @return A JLabel for use on sylladex cards.
	 */
	public JLabel getIconLabelFromItem(SylladexItem item)
	{
		Object o = item.getContents();
		if(o instanceof File)
		{
			return new JLabel(getIconFromFile((File)o));
		}
		else if(o instanceof String)
		{
			return new JLabel((String)o);
		}
		else if(o instanceof Image)
		{
			return new JLabel(getDockIcon((Image)o));
		}
		else if(o instanceof Widget)
		{
			return ((Widget)o).getDockIcon();
		}
		return null;
	}
	
	/** Creates an Icon given a file.
	 * 
	 * @param file - the file to use.
	 * @return The default system icon for the file.
	 */
	public static Icon getIconFromFile(File file)
	{
		ShellFolder shellFolder;
		try
		{
			shellFolder = ShellFolder.getShellFolder(file);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			shellFolder = null;
		}
		if(shellFolder!=null && isWindows())
		{
			Icon icon = new ImageIcon(shellFolder.getIcon(true));
			return icon;
		}
		
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
		return icon;
	}
	
	/**
	 * Creates an icon for use in the dock, given an image.
	 * @param image - The image to use.
	 * @return An Icon suitable for use in the dock.
	 */
	public static Icon getDockIcon(Image image)
	{
		return new ImageIcon(image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	}
	
	/**
	 * Resizes an image, and returns the resulting image as an Icon. The image retains its original aspect ratio.
	 * @param image - The image to resize.
	 * @param width - The final width of the image.
	 * @param height - The final height of the image.
	 * @return The resized image as an Icon.
	 */
	public static Icon getSizedIcon(Image image, int width, int height)
	{
		ImageIcon icon = new ImageIcon(image);
		if(icon.getIconWidth()>icon.getIconHeight())
		{
			float ratio = new Float(width)/new Float(icon.getIconWidth());
			if(ratio==0){ ratio = 0.1f; }
			return new ImageIcon(image.getScaledInstance(width, Math.round(icon.getIconHeight()*ratio), Image.SCALE_SMOOTH));
		}
		float ratio = new Float(height)/new Float(icon.getIconHeight()+0.1f);
		if(ratio==0){ ratio = 0.1f; }
		return new ImageIcon(image.getScaledInstance(Math.round(icon.getIconWidth()*ratio), height, Image.SCALE_SMOOTH));
	}
	
	/** @deprecated use {@link #getItem(String)}. */
	@Deprecated
	public Object oldGetItem(String string)
	{
		string = string.replaceAll("http://", "");
		String p = ""; if(System.getProperty("file.separator").equals("\\")) { p="\\"; }
		string = string.replaceAll("\\\\", p + System.getProperty("file.separator"));
		string = string.replaceAll("/", p + System.getProperty("file.separator"));
		
		File file = new File(string);
		if(file.exists())
		{
			if(file.getName().matches("captchalogued_image_.*"))
			{
				try
				{
					Image image = ImageIO.read(file);
					file.delete();
					return image;
				}
				catch (IOException e){ return file.getPath(); }
			}
			return file;
		}
		return string.replaceAll("SYLLADEX_NL", System.getProperty("line.separator"));
	}
	
	// Transparency
	/**
	 * Attempts to make the specified window transparent.
	 * @param window - The window to make transparent.
	 */
	public static void setTransparent(JWindow window)
	{
		if (com.sun.awt.AWTUtilities.isTranslucencySupported(com.sun.awt.AWTUtilities.Translucency.PERPIXEL_TRANSLUCENT)
				&& isTransparencySupported())
		{
			try
			{
				com.sun.awt.AWTUtilities.setWindowOpaque(window, false);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @return Whether or not it is possible to make windows transparent on the current system.
	 */
	public static boolean isTransparencySupported()
	{
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = env.getScreenDevices();
		GraphicsConfiguration translucencyCapableGC = null;
		
		for (int i = 0; i<devices.length && translucencyCapableGC == null; i++)
		{
			GraphicsConfiguration[] configs = devices[i].getConfigurations();
			for(int j = 0; j<configs.length && translucencyCapableGC == null; j++)
			{
				if(AWTUtilities.isTranslucencyCapable(configs[j]))
				{
					//TODO find out how to set the transparency function to use this gc.
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @return True if the system is running Windows; false otherwise.
	 */
	public static boolean isWindows()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("win") >= 0;
	}
	
	/**
	 * @return True if the system is running Mac OS; false otherwise.
	 */
	public static boolean isMac()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("mac") >= 0;
	}
	
	/**
	 * @return True if the system is running Linux; false otherwise.
	 */
	public static boolean isLinux()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("nix") >= 0) || (os.indexOf("nux") >=0);
	}
	
	//Listeners-------------------------------------------------------------------------------------------------------
	private class PopupListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(e.getActionCommand().equals("preferences"))
			{
				prefs.showPreferencesFrame();
			}
			else if(e.getActionCommand().equals("exit"))
			{
				prefs.cleanUp();
				System.exit(0);
			}
		}	
	}
	
	private class DockListener implements MouseListener, MouseMotionListener
	{
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if(!d_hidden)
			{
				if(e.getButton()==MouseEvent.BUTTON1)
					modus.showSelectionWindow();
				else
					prefs.showPreferencesFrame();
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent arg0)
		{
			dautohide_timer.stop();
			cautohide_timer.stop();
			dautoshow_timer.restart();
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			showDock();
		}
		
		@Override
		public void mouseExited(MouseEvent arg0)
		{
			dautoshow_timer.stop();
			if((prefs.top() && arg0.getLocationOnScreen().getY()>100)
					|| (!prefs.top() && arg0.getLocationOnScreen().getY()<screensize.height-100))
			{
				if(prefs.autohide_dock())
				{
					dautohide_timer.restart();
				}
				if(prefs.autohide_cards())
					cautohide_timer.restart();
			}
		}
		
		@Override
		public void mousePressed(MouseEvent arg0){}
		
		@Override
		public void mouseReleased(MouseEvent arg0){}
		
		@Override
		public void mouseMoved(MouseEvent e){}
	}
	
	private class CardListener implements MouseListener
	{
		
		@Override
		public void mouseClicked(MouseEvent e){}
		
		@Override
		public void mouseEntered(MouseEvent e)
		{
			cautohide_timer.stop();
		}
		
		@Override
		public void mouseExited(MouseEvent e)
		{
			if(prefs.autohide_cards())
				cautohide_timer.restart();
		}
		
		@Override
		public void mousePressed(MouseEvent e){}
		
		@Override
		public void mouseReleased(MouseEvent e){}
		
	}
	
	private class MyClipboardOwner implements ClipboardOwner
	{
		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents){}
	}
	
	public static class DragListener implements MouseListener, MouseMotionListener
	{
		int startx = 0;
		int starty = 0;
		boolean dragging = false;
		JWindow window;
		
		public DragListener(JWindow window)
		{
			this.window = window;
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if(dragging)
			{
				int x = e.getXOnScreen();
				int y = e.getYOnScreen();
				window.setLocation(x-startx, y-starty);
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			dragging = true;
			startx = e.getXOnScreen()-window.getX();
			starty = e.getYOnScreen()-window.getY();
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			dragging = false;
		}
		
		@Override
		public void mouseMoved(MouseEvent e){}
		@Override
		public void mouseClicked(MouseEvent e){}
		@Override
		public void mouseEntered(MouseEvent e){}
		@Override
		public void mouseExited(MouseEvent e){}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(dautohide_timer))
		{
			hideDock();
		}
		
		if(e.getSource().equals(dautoshow_timer))
		{
			showDock();
			cardholder.setVisible(true);
		}
		
		if(e.getSource().equals(cautohide_timer))
		{
			cardholder.setVisible(false);
		}
	}
	
	@Override
	public void windowActivated(WindowEvent arg0){}
	
	@Override
	public void windowClosed(WindowEvent arg0){}
	
	@Override
	public void windowClosing(WindowEvent w){}
	
	@Override
	public void windowDeactivated(WindowEvent arg0){}
	
	@Override
	public void windowDeiconified(WindowEvent arg0){}
	
	@Override
	public void windowIconified(WindowEvent arg0){}
	
	@Override
	public void windowOpened(WindowEvent arg0){}
}