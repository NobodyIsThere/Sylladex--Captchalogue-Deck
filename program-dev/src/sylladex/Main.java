package sylladex;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.TransferHandler;

import util.Animation;
import util.Animation.AnimationType;
import util.CMD;
import util.RW;
import util.Util;
import util.Util.OpenReason;

/** Holds all the cards. Also provides utility functions for the other classes. */
public class Main implements ActionListener, WindowListener
{
	//Referred to as "deck" by other classes.
	
	public static final String NAME = "[NAME]", STATE = "[STATE]";
	
	private DeckPreferences prefs;
	private FetchModus modus;
	private FetchModusSettings modus_settings;
	private ArrayList<CaptchalogueCard> captchalogue_cards;
	private ArrayList<StrifeSpecibus> strife_specibi;
	private StrifePortfolio strife_portfolio;
	
	private Object[] icons;
	private ArrayList<SylladexItem> item_list;
	
	private JWindow dock;
	private JWindow card_holder;
	private CardDisplayManager card_display_manager;
	private JWindow bouncewindow;
	private JLayeredPane pane;
	private JLayeredPane iconpane;
	private JLayeredPane cardpane;
	
	private SystemTray tray;
	private PopupMenu popup;
	private PopupListener popuplistener = new PopupListener();
	
	private JLabel captchalogue_button;
	private JLabel strife_button;
	
	private Thread directory_watch_thread;
	
	private DockListener docklistener = new DockListener();
	private CardListener cardlistener = new CardListener();
	private Timer dautohide_timer = new Timer(500, this);
	private Timer dautoshow_timer = new Timer(500, this);
	private Timer cautohide_timer = new Timer(2000, this);
	private boolean d_hidden = false;
	
	private SylladexClipboardOwner co;
	
	private ArrayList<Animation> animations = new ArrayList<Animation>();
	
	public static void main(String[] args)
	{
		//Okay. Start everything going.
		new Main();
	}
	
	public Main()
	{
//		SynthLookAndFeel lf = new SynthLookAndFeel();
//		try
//		{
//			lf.load(new File("ui/ui.xml").toURI().toURL());
//			UIManager.setLookAndFeel(lf);
//		}
//		catch (Exception x)
//		{
//			error("Could not set look and feel.");
//			x.printStackTrace();
//		}
		prefs = new DeckPreferences(this);
		createDock();
		
		co = new SylladexClipboardOwner();
		
		changeModus(prefs.getModus());
		
		if (prefs.autohide_cards()){ card_holder.setVisible(false); }
		
		//Make sure contents are saved on Mac Cmd+Q
		if(Util.isMac())
		{
			Runnable exithook = new Runnable()
			{
				public void run()
				{
					prefs.cleanUp(item_list);
				}
			};
			Runtime.getRuntime().addShutdownHook(new Thread(exithook,"Contents save hook (OSX)"));
		}
		
		strife_specibi = new ArrayList<StrifeSpecibus>();
		strife_portfolio = new StrifePortfolio();
		strife_portfolio.setSpecibi(strife_specibi);
		StrifeSpecibus s = new StrifeSpecibus(this);
		s.setKind(".gif");
		StrifeSpecibus t = new StrifeSpecibus(this);
		t.setKind(".png");
		StrifeSpecibus u = new StrifeSpecibus(this);
		u.setKind(".gif");
		StrifeSpecibus v = new StrifeSpecibus(this);
		v.setKind(".png");
		StrifeSpecibus w = new StrifeSpecibus(this);
		w.setKind(".gif");
		StrifeSpecibus x = new StrifeSpecibus(this);
		x.setKind(".png");
		StrifeSpecibus y = new StrifeSpecibus(this);
		y.setKind(".gif");
		StrifeSpecibus z = new StrifeSpecibus(this);
		z.setKind("hammer");
		strife_specibi.add(s);
		strife_specibi.add(t);
		strife_specibi.add(u);
		strife_specibi.add(v);
		strife_specibi.add(w);
		strife_specibi.add(x);
		strife_specibi.add(y);
		strife_specibi.add(z);
		
		directory_watch_thread = new Thread(new DirectoryWatchLoop());
		directory_watch_thread.start();
	}
	
	protected void changeModus(FetchModus m)
	{
		modus = m;
		modus_settings = m.getSettings();
		modus.setPreferences(prefs.getModusPreferences());
		captchalogue_cards = new ArrayList<CaptchalogueCard>();
		if (card_holder != null) { card_holder.setVisible(false); card_holder.dispose(); }
		createCardHolder();
		card_display_manager = new CardDisplayManager(this);
		while(captchalogue_cards.size()<prefs.number_of_cards())
		{ addCardWithoutRefresh(); }
		icons = captchalogue_cards.toArray();
		refreshDock();
		modus.prepare();
		
		item_list = new ArrayList<SylladexItem>();
		
		for (String s : prefs.getModusItems())
		{	
			String name = s.substring(s.indexOf(NAME) + 6);
			s = s.substring(0, s.indexOf(NAME));
			
			String state = "";
			if (name.contains(STATE))
			{
				state = name.substring(name.indexOf(STATE) + 7);
			}
			
			Object o = getObjectFromSaveString(s);
			
			if (o instanceof Widget)
			{
				((Widget)o).load(state);
			}
			
			addLoadedItem(o, name, s);
		}
		
		modus.loading = false;
		modus.ready();
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
				showDock();
				return true;
			}
			return false;
		}
		
		public boolean importData(TransferSupport ts)
		{
			if (!canImport(ts))
			{
				return false;
			}
			
			Transferable t = ts.getTransferable();
			
			try
			{
				captchalogueTransferable(t);
			}
			catch (UnsupportedFlavorException e)
			{
				return false;
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(null, "IO error!\n" + e.getLocalizedMessage());
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
		
		dock.setIconImage(Util.createImageIcon(modus_settings.get_card_image()).getImage());
		dock.setSize(new Dimension(Util.SCREEN_SIZE.width,100));
		hideDock();
		if(prefs.top() && !prefs.autohide_dock())
		{ dock.setLocation(new Point(0,prefs.offset())); }
		else if(!prefs.autohide_dock())
		{ dock.setLocation(new Point(0,Util.SCREEN_SIZE.height - 100 - prefs.offset())); }
		
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
		
		modus.refreshDock();
	}
	
	private void createSystemTray()
	{
		if(!SystemTray.isSupported()){ return; }
		popup = new PopupMenu();
		
		//Note that these are java.awt.MenuItems, not ui.MenuItems.
		Menu captchalogue = new Menu("Captchalogue");
		for (MenuItem item : getWidgetMenuItems())
		{
			captchalogue.add(item);
		}
		popup.add(captchalogue);
		
		MenuItem preferences = new MenuItem("Preferences");
			preferences.addActionListener(popuplistener);
			preferences.setActionCommand("preferences");
		popup.add(preferences);
		
		MenuItem exit = new MenuItem("Exit");
			exit.addActionListener(popuplistener);
			exit.setActionCommand("exit");
		popup.add(exit);
		
		tray = SystemTray.getSystemTray();
		TrayIcon icon = new TrayIcon(Util.createImageIcon("modi/global/trayicon.gif").getImage());
		
		icon.setPopupMenu(popup);
		
		try { tray.add(icon); }
		catch (AWTException e)
		{
			JOptionPane.showMessageDialog(null, "Could not create tray icon!\n" + e.getLocalizedMessage());
		}
	}
	
	private void refreshSystemTray()
	{
	}
	
	private ArrayList<MenuItem> getWidgetMenuItems()
	{
		ArrayList<MenuItem> items = new ArrayList<MenuItem>();
		for (String s : new File("widgets/").list())
		{
			if (s.endsWith(".sdw"))
			{
				ArrayList<String> info = RW.readFile(new File("widgets", s));
				MenuItem item = new MenuItem(info.get(0));
				item.addActionListener(popuplistener);
				item.setActionCommand("add_widget_" + info.get(1));
				items.add(item);
			}
		}
		return items;
	}
	
	private void addComponentsToDock()
	{
		int textoffset = 94;
		if(prefs.top())
		{
			textoffset = 21;
		}
		
		//Background image
		Color dockbg = modus_settings.get_background_color();
		Color secondary = modus_settings.get_secondary_color();
		ImageIcon docktextimage = Util.createImageIcon(modus_settings.get_dock_text_image());
		
		DockBackground background = new DockBackground(dockbg, secondary, prefs.top());
		background.setBounds(0, 0, Util.SCREEN_SIZE.width, 100);
		pane.setLayer(background, 0);
		pane.add(background);
		
		//Background
		pane.setLayer(modus.getBackground(), 1);
		modus.getBackground().setBounds(0, 0, Util.SCREEN_SIZE.width, 100);
		modus.getBackground().setOpaque(false);
		
		modus.getBackground().removeMouseListener(docklistener);
		modus.getBackground().removeMouseMotionListener(docklistener);
		modus.getBackground().addMouseListener(docklistener);
		modus.getBackground().addMouseMotionListener(docklistener);
		
		pane.add(modus.getBackground());
		
		//Text
		JLabel docktext = new JLabel(docktextimage);
		pane.setLayer(docktext, 100);
		docktext.setBounds(10, 100 - textoffset, 211, 16);
		pane.add(docktext);
		
		//Buttons
		captchalogue_button = new JLabel(Util.createImageIcon("modi/global/captchalogue.png"));
			captchalogue_button.addMouseListener(docklistener);
			captchalogue_button.setBounds(Util.SCREEN_SIZE.width - 32, 100 - textoffset, 16, 16);
			pane.setLayer(captchalogue_button, 100);
			pane.add(captchalogue_button);
		
		strife_button = new JLabel(Util.createImageIcon("modi/global/strife_icon.png"));
			strife_button.addMouseListener(docklistener);
			strife_button.setBounds(Util.SCREEN_SIZE.width - 56, 100 - textoffset, 16, 16);
			pane.setLayer(strife_button, 100);
			pane.add(strife_button);
			
		
		//Icons
		iconpane = new JLayeredPane();
		iconpane.setBounds(0,0,Util.SCREEN_SIZE.width,100);
		pane.setLayer(iconpane, 200);
		pane.add(iconpane);
		
		//Foreground
		pane.setLayer(modus.getForeground(), 199);
		modus.getForeground().setBounds(0,0,Util.SCREEN_SIZE.width,100);
		modus.getForeground().setOpaque(false);
		pane.add(modus.getForeground());
	}
	
	private void drawDefaultDockIcons()
	{
		int numcards = captchalogue_cards.size();
		ImageIcon cardbg = Util.createImageIcon(modus_settings.get_dock_card_image());
		int x;
		int y;
		int i = 0;
		if(prefs.top()==true) { y=10; } else { y=35; }
		if(numcards>Util.SCREEN_SIZE.width/50)
		{
			int extracards = captchalogue_cards.size() - Math.round(Util.SCREEN_SIZE.width/50);
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
			while(x<Util.SCREEN_SIZE.width-50)
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
			x = Util.SCREEN_SIZE.width/2 - numcards*25;
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
			if(captchalogue_cards.size() < Util.SCREEN_SIZE.width/50)
			{ x = Util.SCREEN_SIZE.width/2 - captchalogue_cards.size()*25; }
			else { x = 16; }
		}
		else
		{ x = Util.SCREEN_SIZE.width/2 - icons.length*25; }
		y = getDockIconYPosition();

		for (int i=0; i<icons.length; i++)
		{
			if (icons[i] != null)
			{
				SylladexItem item = ((CaptchalogueCard) icons[i]).getItem();
				if (item != null)
				{
					JLabel label = item.getIcon();
					label.setBounds(x+5,y+5,33,60);
					iconpane.add(label);
				}
			}
			x+=50;
		}
		iconpane.validate();
		iconpane.repaint();
	}
	
	/** Sets the icons to be drawn on the dock, then calls {@link refreshDockIcons()}.
	 * @param cards The new icons.*/
	public void setIcons(Object[] cards)
	{
		icons = cards;
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
			dock.setLocation(0, Util.SCREEN_SIZE.height-100-prefs.offset());
		}
		d_hidden = false;
	}
	
	/** Hides the dock. */
	public void hideDock()
	{
		if(prefs.top())
			dock.setLocation(0,-99+prefs.offset());
		else
			dock.setLocation(0, Util.SCREEN_SIZE.height-1-prefs.offset());
		d_hidden = true;
	}
	
	//Strife specibus
	public void showStrifePortfolio()
	{
		if (strife_specibi.size() == 1)
		{
			strife_specibi.get(0).show();
		}
		else
		{
			strife_portfolio.show();
		}
	}
	
	//Card holder
	private void createCardHolder()
	{
		card_holder = new JWindow();
		cardpane = new JLayeredPane();
		card_holder.setLayeredPane(cardpane);
		card_holder.setLocation(modus_settings.get_origin());
		
		card_holder.setBackground(Util.COLOR_TRANSPARENT);
		
		card_holder.setSize(10, 10);
		card_holder.setLayout(null);
		
		card_holder.addMouseListener(cardlistener);
		card_holder.setTransferHandler(new FileDropHandler());
		
		card_holder.setVisible(true);
	}
	
	/** Updates the always-on-top status of the card window, and repaints the window.*/
	public void refreshCardHolder()
	{
		if(prefs.always_on_top_cards() || prefs.autohide_cards())
			card_holder.setAlwaysOnTop(true);
		else
			card_holder.setAlwaysOnTop(false);
		
		card_holder.repaint();
	}
	
	/** Resizes the card window.
	 * @param w - width of the window.
	 * @param h - height of the window.*/
	public void setCardHolderSize(int w, int h)
	{
		card_holder.setSize(w,h);
		card_holder.repaint();
	}
	
	/** Returns the card window. For advanced use only.*/
	public JWindow getCardHolder()
	{
		return card_holder;
	}
	
	/** Returns the card window manager, which provides functions for shifting the contents
	 * of the cardholder window.
	 */
	public CardDisplayManager getCardDisplayManager()
	{
		return card_display_manager;
	}
	
	//Card functions
	private void addCardWithoutRefresh()
	{
		CaptchalogueCard card = new CaptchalogueCard(this);
		captchalogue_cards.add(card);
		card_holder.getLayeredPane().add(card.getPanel());
	}
	
	/** Adds a card to the deck, and refreshes the dock.*/
	public void addCard()
	{
		CaptchalogueCard card = new CaptchalogueCard(this);
		captchalogue_cards.add(card);
		card_holder.getLayeredPane().add(card.getPanel());
		refreshDock();
	}
	
	private void captchalogueClipboard()
	{
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		try
		{
			captchalogueTransferable(t);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "IO error!\n" + e.getLocalizedMessage());
		}
		catch (UnsupportedFlavorException e){}
	}
	
	private void captchalogueTransferable(Transferable t) throws UnsupportedFlavorException, IOException
	{
		if(t.isDataFlavorSupported(DataFlavor.imageFlavor))
		{
			Image image = (Image) t.getTransferData(DataFlavor.imageFlavor);
			addItem(image);
		}
		else if(t.isDataFlavorSupported(DataFlavor.stringFlavor))
		{
			String string = (String) t.getTransferData(DataFlavor.stringFlavor);
			addItem(string);
		}
		else if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			@SuppressWarnings("unchecked")
			java.util.List<File> fileList = (java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
			for (File file : fileList)
			{
				if(file.getName().endsWith(".sdw"))
				{
					Widget widget = loadWidget(RW.readFile(file).get(1));
					addItem(widget);
				}
				else if (file.getName().endsWith(".fetchmodus"))
				{
					Util.installFetchModus(file);
					String fmi_file = "modi/" + file.getName().replace(".fetchmodus", ".fmi");
					Object o = getObjectFromSaveString("captchalogued_widget_FetchModusWidget");
					((Widget) o).load(fmi_file);
					addLoadedItem(o, "Fetch Modus", "");
					Files.delete(file.toPath());
				}
				else if (file.getName().endsWith(".widget"))
				{
					Util.installWidget(file);
					String name = file.getName().replace(".widget", "");
					Widget widget = loadWidget(name);
					addItem(widget);
					Files.delete(file.toPath());
				}
				else
				{
					addItem(file);
				}
			}
		}
	}
	
	protected Widget loadWidget(String name)
	{
		try
		{
			File classes = new File("widgets/");
			URL[] url = { classes.toURI().toURL() };
			ClassLoader cl = new URLClassLoader(url);
			name = name.replaceAll("\\.class?", "");
			name = name.replaceAll("\\.sdw?", "");
			Class<?> wclass = cl.loadClass(name);
			
			//Load inner classes too
			for (String filename : classes.list())
			{
				if (filename.startsWith(name) && filename.contains("$"))
				{
					cl.loadClass(filename.replace(".class", ""));
				}
			}
			
			Widget widget = (Widget) wclass.newInstance();
			widget.setMain(this);
			widget.prepare();
			((URLClassLoader) cl).close();
			return widget;
		}
		catch (ClassNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "Class not found: " + name);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Unable to load widget!\n" + e.getLocalizedMessage());
		}
		return null;
	}
	
	private Object getObjectFromSaveString(String s)
	{
		Object o = null;
		File file = new File("files" + File.separator + s);
		
		if (s.contains("captchalogued_string_"))
		{
			o = "";
			ArrayList<String> lines = RW.readFile(file);
			for (String line : lines)
			{
				o = ((String) o).concat(line + System.getProperty("line.separator"));
			}
			return o;
		}
		else if (s.contains("captchalogued_image_"))
		{
			try
			{
				o = ImageIO.read(file);
				return o;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return file.getPath();
			}
		}
		else if (s.contains("captchalogued_widget_"))
		{
			s = s.substring(21);
			System.out.println(s);
			return loadWidget(s);
		}
		else if (new File(s).exists())
		{
			return new File(s);
		}

		return file;
	}
	
	private void addLoadedItem(Object o, String name, String path)
	{
		SylladexItem item = new SylladexItem(name, o, this);
		item.setSaveString(path + NAME + name);
		item_list.add(item);
		modus.captchalogue(item);
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
			SylladexItem item = new SylladexItem(string, string, this);
			if (modus.captchalogue(item))
			{
				addItem(item);
				
				File f = null;
				while (f==null ? true : f.exists()) // avoid overwriting another file
					f = new File("files" + File.separator + "captchalogued_string_" + new Double(Math.random()).toString().replaceAll("\\.", "") + ".txt");
				
				ArrayList<String> contents = new ArrayList<String>();
				contents.add(string);
				RW.writeFile(contents, f);
				if (string.contains(System.getProperty("line.separator")))
				{ 
					item.setSaveString(f.getName() + NAME + string.substring(0, string.indexOf(System.getProperty("line.separator"))));
				}
				else
				{
					item.setSaveString(f.getName() + NAME + string);
				}
				item_list.add(item);
			}
			return;
		}
		
		File file = null;
		try
		{
			file = new File(url.toURI().getPath());
		}
		catch (URISyntaxException e)
		{
			file = new File(url.getPath());
		}
		
		addItem(file);
	}
	
	private void addItem(Image image)
	{
		String name = "ITEM";
		if (modus.getSettings().require_names())
		{
			name = requestName();
		}
		
		SylladexItem item = new SylladexItem(name, image, this);
		if (modus.captchalogue(item))
		{
			addItem(item);
			
			File f = null;
			while(f == null ? true : f.exists()) //don't want to overwrite a previous image
				f = new File("files" + File.separator + "captchalogued_image_" + new Double(Math.random()).toString().replaceAll("\\.", "") + ".png");
			BufferedImage b = (BufferedImage)image;
			try
			{
				ImageIO.write(b, "png", f);
				item.setSaveString(f.getName() + NAME + name);
				item_list.add(item);
			}
			catch (IOException x)
			{
				Util.error("Could not write image file. Check that the files/ directory is writable");
				x.printStackTrace();
			}
		}
	}
	
	private void addItem(File file)
	{
		SylladexItem item = new SylladexItem(file.getName(), file, this);
		if (modus.captchalogue(item))
		{	
			addItem(item);
			
			Path path = Paths.get(file.toURI());
			Path destination = Paths.get("files" + File.separator + file.getName());
			
			try
			{
				if (prefs.captchalogue_mode() == DeckPreferences.MOVE)
				{
					Files.walkFileTree(path, new CMD(path, destination, CMD.MOVE));
				}
				else if (prefs.captchalogue_mode() == DeckPreferences.COPY)
				{
					Files.walkFileTree(path, new CMD(path, destination, CMD.COPY));
				}
				item.setSaveString(file.getName() + NAME + file.getName());
				item.setContents(destination.toFile());
			}
			catch (FileAlreadyExistsException x) {}
			catch (IOException x) { Util.error("Unable to copy/move file."); }
			
			if (prefs.captchalogue_mode() == DeckPreferences.LINK)
			{
				item.setSaveString(file.getAbsolutePath() + NAME + file.getName());
			}
			
			item_list.add(item);
		}
	}
	
	private void addItem(Widget widget)
	{
		SylladexItem item = new SylladexItem(widget.getName(), widget, this);
		if (modus.captchalogue(item))
		{
			widget.add();
			addItem(item);
			
			item_list.add(item);
		}
	}
	
	private void addItem(SylladexItem item)
	{
		if (modus.getSettings().bounce_captchalogued_items())
		{
			bounceItem(item);
		}
	}
	
	private void fileAdded(Path file)
	{
		if (!file.getFileName().equals("index.txt") &&
				getCardWithFile(file.toFile()) == null &&
				!file.toString().contains("captchalogued_") &&
				!file.toString().endsWith(".download"))
		{
			addItem(new File("files" + File.separator + file.getFileName()));
		}
	}
	
	private void fileDeleted(Path file)
	{
		CaptchalogueCard card = getCardWithFile(new File("files" + File.separator + file.getFileName()));
		String name = file.getFileName().toString();
		if (card != null)
		{
			item_list.remove(card.getItem());
			card.setItem(null);
			card.setItem(new SylladexItem(name, name, this));
		}
		else
		{ System.out.println("Item removed from files/ directory but corresponding card " +
							"not found: " + name); }
	}
	
	private void bounceItem(SylladexItem item)
	{
		if (bouncewindow == null)
		{
			bouncewindow = new JWindow();
			bouncewindow.setBackground(Util.COLOR_TRANSPARENT);
			bouncewindow.setLayout(null);
			bouncewindow.setAlwaysOnTop(true);
			int width = modus.getSettings().get_card_width() + 2;
			int height = modus.getSettings().get_card_height() + 2;
			bouncewindow.setSize(width, height);
			bouncewindow.setLocation(Util.SCREEN_SIZE.width - width - 20, 30);
			CaptchalogueCard bouncecard = new CaptchalogueCard(this);
			bouncecard.setItem(item);
			bouncecard.getPanel().setBounds(0, 0, width-2, height-2);
			bouncecard.setVisible(true);
			bouncewindow.add(bouncecard.getPanel());
			bouncewindow.setVisible(true);
			bounceCard(bouncecard);
			Animation.waitFor(1000, this, "deck_remove_bounce_window");
		}
	}
	
	private void openCard(CaptchalogueCard card, OpenReason reason)
	{
		SylladexItem item = card.getItem();
		Object o = item.getContents();
		if (o instanceof File)
		{
			try
			{
				if(Desktop.isDesktopSupported())
				{
					Desktop.getDesktop().open((File) o);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else if (o instanceof String)
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection((String) o), co);
			co.updateContents();
		}
		else if (o instanceof Image)
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection((Image) o), co);
			co.updateContents();
		}
		else if (o instanceof Widget)
		{
			((Widget) o).open(reason);
		}
	}
	
	/** Opens the specified captchalogue card.
	 * 
	 * @param card - The captchalogue card to open.
	 * @see openWithoutRemoval(SylladexCard card)
	 * */
	public void open(CaptchalogueCard card, OpenReason reason)
	{
		openCard(card, reason);
		if (reason == OpenReason.USER_EJECT)
			eject(card.getItem());
		if (reason != OpenReason.USER_KEEP)
			card.setItem(null);
	}
	
	/** Removes the item from the files/
	 * directory and the index.
	 * 
	 * @param card - The captchalogue card to open.
	 */
	public void eject(SylladexItem item)
	{
		item_list.remove(item);
		String save_string = item.getSaveString();
		String path = "files/" + save_string.substring(0, save_string.indexOf(NAME));
		if (prefs.captchalogue_mode() == DeckPreferences.COPY
				|| item.getContents() instanceof String
				|| item.getContents() instanceof Image)
		{
			try
			{
				Files.walkFileTree(Paths.get(path), new CMD(Paths.get(path), Paths.get(path), CMD.DELETE));
			}
			catch (IOException x) { x.printStackTrace(); }
		}
	}
	
	/** Removes the specified card from the deck, without opening its item or removing its item from the index.
	 * 
	 * @param card - The captchalogue card to remove.
	 */
	public void removeCard(CaptchalogueCard card)
	{
		captchalogue_cards.remove(card);
		refreshDock();
	}
	
	/**
	 * Removes the next available empty card.
	 */
	public void removeCard()
	{
		for(CaptchalogueCard card : captchalogue_cards)
		{
			if (card.isEmpty())
			{
				captchalogue_cards.remove(card);
				refreshDockIcons();
				return;
			}
		}
	}
	
	/**
	 * @return true if every card in the deck has been assigned an item.
	 */
	public boolean isFull()
	{
		for (CaptchalogueCard card : captchalogue_cards)
		{
			if (card.isEmpty()) return false;
		}
		return true;
	}
	
	/**
	 * @return true if no cards in the deck have been assigned items.
	 */
	public boolean isEmpty()
	{
		for (CaptchalogueCard card : captchalogue_cards)
		{
			if (!card.isEmpty()) return false;
		}
		return true;
	}
	
	/**
	 * Captchalogues the item in the next available captchalogue card.
	 * 
	 * @param item - The item to captchalogue.
	 */
	public void captchalogueItem(SylladexItem item)
	{
		captchalogueItemAndReturnCard(item);
	}
	
	/**
	 * Captchalogues the item in the next available captchalogue card and returns it.
	 * 
	 * @param item - The item to captchalogue.
	 * @return The captchalogue card in which the item is captchalogued. If the deck is full, returns null.
	 */
	public CaptchalogueCard captchalogueItemAndReturnCard(SylladexItem item)
	{
		for (CaptchalogueCard card : captchalogue_cards)
		{
			if (card.isEmpty())
			{
				card.setItem(item);
				if (item.getContents() instanceof Widget)
				{
					((Widget) item.getContents()).setCard(card);
				}
				return card;
			}
		}
		return null;
	}
	
	public void captchalogueCaptchalogueCard()
	{
		Widget ccc = loadWidget("CaptchaloguedCaptchalogueCard");
		addItem(ccc);
	}
	
	/**
	 * Adds a card movement animation to the animation queue.
	 * 
	 * @param card - The card to move.
	 * @param position - The final position of the card.
	 * @param type - The animation type to use.
	 */
	public void moveCard(CaptchalogueCard card, Point position, AnimationType type)
	{
		Animation a = new Animation(card, position, type, this, "animation_complete");
		addAnimation(a);
	}
	
	/**
	 * Adds a card bounce animation to the animation queue.
	 * 
	 * @param card - The card to bounce.
	 */
	public void bounceCard(CaptchalogueCard card)
	{
		Animation a = new Animation(card, new Point(card.getFinalLocation().x + 2, card.getFinalLocation().y + 2),
				AnimationType.BOUNCE_SPOT, this, "animation_complete");
		addAnimation(a);
	}
	
	/**
	 * Adds a card bounce animation to the animation queue.
	 * 
	 * @param card - The card to bounce.
	 * @param size - The size of the bounce.
	 */
	public void bounceCard(CaptchalogueCard card, int size)
	{
		Animation a = new Animation(card, new Point(card.getFinalLocation().x + 5, card.getFinalLocation().y + 5),
				AnimationType.BOUNCE_SPOT, this, "animation_complete");
		addAnimation(a);
	}
	
	/**
	 * Adds an animation to the animation queue.
	 * 
	 * @param a - The animation to be added.
	 */
	public void addAnimation(Animation a)
	{
		a.setListener(this);
		animations.add(a);
		if (animations.size() == 1) a.run();
	}
	
	/**
	 * Stops all running animations and clears the queue.
	 */
	public void stopAnimation()
	{
		if (animations.size() == 0) { return; }
		Animation a = animations.get(0);
		animations.clear();
		a.stop();
		for (CaptchalogueCard card : captchalogue_cards)
		{
			card.stopAnimation();
		}
	}
	
	//Utility functions
	
	public CaptchalogueCard getCardWithFile(File f)
	{
		for (CaptchalogueCard card : captchalogue_cards)
		{
			if (card.getItem() != null && card.getItemContents() instanceof File)
			{
				if (((File)card.getItemContents()).getName().equals(f.getName()))
				{
					return card;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * @return An ArrayList of the cards currently in the deck.
	 */
	public ArrayList<CaptchalogueCard> getCards()
	{
		return captchalogue_cards;
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
	
	protected ArrayList<SylladexItem> getModusItems()
	{
		return item_list;
	}
	
	/**
	 * Asks the user for a string.
	 * @return The string the user gave.
	 */
	public String requestName()
	{
		return JOptionPane.showInputDialog("Requesting item name.");
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
	
	public static DataFlavor selectBestDataFlavor(DataFlavor[] flavors)
	{
		java.util.List<DataFlavor> fl = Arrays.asList(flavors);
		if (fl.contains(DataFlavor.imageFlavor))
		{
			return DataFlavor.imageFlavor;
		}
		else if (fl.contains(DataFlavor.stringFlavor))
		{
			return DataFlavor.stringFlavor;
		}
		else if (fl.contains(DataFlavor.javaFileListFlavor))
		{
			return DataFlavor.javaFileListFlavor;
		}
		return null;
	}
	
	//Listeners and classes -------------------------------------------------------------------------------------------
	private class PopupListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand().startsWith("add_widget_"))
			{
				String name = e.getActionCommand().replace("add_widget_", "");
				Widget widget = loadWidget(name);
				addItem(widget);
			}
			else if(e.getActionCommand().equals("preferences"))
			{
				prefs.showPreferencesFrame();
			}
			else if(e.getActionCommand().equals("exit"))
			{
				prefs.cleanUp(item_list);
				System.exit(0);
			}
		}	
	}
	
	private class DockListener implements MouseListener, MouseMotionListener
	{
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if(e.getSource().equals(captchalogue_button))
			{
				captchalogueClipboard();
			}
			else if (e.getSource() == strife_button)
			{
				showStrifePortfolio();
			}
			else
			{
				if(!d_hidden)
				{
					if(e.getButton()==MouseEvent.BUTTON1)
					{
						ActionEvent event = new ActionEvent(this, 612, Util.ACTION_USER_DOCK_CLICK);
						modus.actionPerformed(event);
					}
					else
						prefs.showPreferencesFrame();
				}
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
					|| (!prefs.top() && arg0.getLocationOnScreen().getY()<Util.SCREEN_SIZE.height-100))
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
	
	private class SylladexClipboardOwner implements ClipboardOwner, ActionListener
	{
		private Timer timer;
		private Object last_contents;
		
		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents){}
		
		public SylladexClipboardOwner()
		{
			timer = new Timer(100, this);
			updateContents();
			timer.start();
		}
		
		public void updateContents()
		{
			try
			{
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				DataFlavor f = Main.selectBestDataFlavor(c.getAvailableDataFlavors());
				if (f != null && !f.equals(DataFlavor.imageFlavor))
				{
					last_contents = c.getData(f);
				}
			}
			catch (Exception e){ e.printStackTrace(); }
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == timer && prefs.auto_captcha())
			{
				try
				{
					Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
					DataFlavor f = Main.selectBestDataFlavor(c.getAvailableDataFlavors());
					if (f == null || f == DataFlavor.imageFlavor){ return; }
					Object new_contents = c.getData(f);
					if (!new_contents.equals(last_contents))
					{
						captchalogueClipboard();
						last_contents = new_contents;
					}
				}
				catch (Exception f){ f.printStackTrace(); }
			}
		}
	}
	
	private class DirectoryWatchLoop implements Runnable
	{

		@Override
		public void run()
		{
			WatchService watcher = null;
			Path directory = Paths.get("files");
			try
			{
				watcher = FileSystems.getDefault().newWatchService();
				directory.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
											StandardWatchEventKinds.ENTRY_DELETE);
			}
			catch (IOException x) { Util.error("Could not watch files/ directory."); }
			
			while (true)
			{
				WatchKey key;
				try { key = watcher.take(); }
				catch (InterruptedException x) { System.out.println("Directory watch thread interrupted."); return; }
				
				for (WatchEvent<?> event : key.pollEvents())
				{
					Kind<?> kind = event.kind();
					if (kind == StandardWatchEventKinds.OVERFLOW) { continue; }
					
					Path file = (Path) event.context();
					
					if (kind == StandardWatchEventKinds.ENTRY_CREATE)
					{
						System.out.println("File added: " + file);
						fileAdded(file);
					}
					else if (kind == StandardWatchEventKinds.ENTRY_DELETE)
					{
						System.out.println("File removed: " + file);
						fileDeleted(file);
					}
					
					boolean isKeyValid = key.reset();
					if (!isKeyValid)
					{
						Util.error("Could not watch files/ directory.");
						break;
					}
				}
			}
		}
		
	}
	
	@SuppressWarnings("serial")
	private class DockBackground extends JPanel
	{
		private Color primary;
		private Color secondary;
		private boolean top;
		
		public DockBackground(Color background, Color highlight, boolean top)
		{
			primary = background;
			secondary = highlight;
			this.top = top;
		}
		
		@Override
		public void paintComponent(Graphics g)
		{
			if (top)
			{
				g.setColor(primary);
				g.fillRect(0, 0, getWidth(), getHeight() - 25);
				g.setColor(secondary);
				g.fillRect(0, getHeight() - 25, getWidth(), 21);
				g.setColor(Color.BLACK);
				g.fillRect(0, getHeight() - 4, getWidth(), 4);
			}
			else
			{
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
				g.setColor(secondary);
				g.fillRect(0, 4, getWidth(), 21);
				g.setColor(primary);
				g.fillRect(0, 25, getWidth(), getHeight() - 25);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand() != null)
		{
			if (e.getSource() instanceof Animation)
			{
				animations.remove(0);
				if (animations.size() > 0)
				{
					animations.get(0).run();
				}
				modus.actionPerformed(e);
			}
			else if (e.getActionCommand().equals("deck_remove_bounce_window"))
			{
				bouncewindow.setVisible(false);
				bouncewindow = null;
			}
		}
		
		if(e.getSource().equals(dautohide_timer))
		{
			hideDock();
		}
		
		if(e.getSource().equals(dautoshow_timer))
		{
			showDock();
			card_holder.setVisible(true);
		}
		
		if(e.getSource().equals(cautohide_timer))
		{
			card_holder.setVisible(false);
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