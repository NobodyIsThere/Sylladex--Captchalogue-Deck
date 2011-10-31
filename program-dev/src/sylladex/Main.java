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

public class Main implements ActionListener, WindowListener
{
    //Should be called "Sylladex", but has to be called "Main".
    //Controls everything.

    private static final long serialVersionUID = 1L;

    private DeckPreferences prefs;
    private FetchModus modus;
    private ArrayList<SylladexCard> sylladexcards = new ArrayList<SylladexCard>();
    private ArrayList<JLabel> icons = new ArrayList<JLabel>();

    private JFrame dock;
    private JWindow cardholder;
    private JLayeredPane pane;
    private JLayeredPane iconpane;
    private JLayeredPane cardpane;
    private int deckwidth;
    private int id;

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
        //catch (java.text.ParseException e)
        //{
        //  e.printStackTrace();
        //}
        //catch (UnsupportedLookAndFeelException e)
        //{
        //  e.printStackTrace();
        //}
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            //TODO: We can't continue without preferences.
        }
        modus = prefs.getModus();
        modus.setPreferences(prefs.getModusPreferences());
        modus.setItems(prefs.getModusItems());
        id = 0;
        createCardHolder();
        while(sylladexcards.size()<modus.startcards)
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
    }

    protected void changeModus(FetchModus m)
    {
        icons = new ArrayList<JLabel>();
        modus = m;
        modus.setPreferences(prefs.getModusPreferences());
        modus.setItems(prefs.getModusItems());
        id = 0;
        sylladexcards.clear();
        cardholder.setVisible(false);
        cardholder.dispose();
        createCardHolder();
        while(sylladexcards.size()<modus.startcards)
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
        dock = new JFrame();
        pane = new JLayeredPane();
        dock.setLayeredPane(pane);
        //Window stuff
        dock.setUndecorated(true);
        dock.setResizable(false);
        dock.setTitle("Sylladex::Captchalogue Deck");
        dock.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    public void refreshDock()
    {
        if(prefs.always_on_top_dock() || prefs.autohide_dock())
            dock.setAlwaysOnTop(true);
        else
            dock.setAlwaysOnTop(false);

        dock.setIconImage(createImageIcon(modus.image_card).getImage());
        deckwidth = screensize.width;
        dock.setSize(new Dimension(deckwidth,100));
        if(prefs.top()==true)
        { dock.setLocation(new Point(0,prefs.offset())); }
        else
        { dock.setLocation(new Point(0,screensize.height-100-prefs.offset())); }

        pane.removeAll();
        addComponentsToDock();
        pane.validate();

        if(modus.drawDefaultDockIcons())
        {
            drawDefaultDockIcons();
        }

        refreshDockIcons();
        dautohide_timer.stop();
        dautoshow_timer.stop();
        cautohide_timer.stop();
    }

    private void addComponentsToDock()
    {
        int textoffset;
        String bgpath;
        if(prefs.top()==true)
        {
            textoffset = 21;
            bgpath = getModus().getTopBgUrl();
        }
        else
        {
            textoffset = 94;
            bgpath = getModus().getBottomBgUrl();
        }

        //Background image
        ImageIcon dockbgimage = createImageIcon(bgpath);
        ImageIcon docktextimage = createImageIcon(getModus().getTextUrl());
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
        ImageIcon cardbg = createImageIcon(getModus().getDockCardBg());
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

    public void refreshDockIcons()
    {
        iconpane.removeAll();
        int x;
        int y;
        if(modus.drawDefaultDockIcons())
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

    public void setIcons(ArrayList<JLabel> newicons)
    {
        icons = newicons;
        refreshDockIcons();
    }

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
        cardholder.setLocation(modus.getOrigin());

        setTransparent(cardholder);

        cardholder.setSize(10, 10);
        cardholder.setLayout(null);

        cardholder.addMouseListener(cardlistener);
        cardholder.setTransferHandler(new FileDropHandler());

        cardholder.setVisible(true);
    }

    public void refreshCardHolder()
    {
        if(prefs.always_on_top_cards() || prefs.autohide_cards())
            cardholder.setAlwaysOnTop(true);
        else
            cardholder.setAlwaysOnTop(false);

        cardholder.repaint();
    }

    public void setCardHolderSize(int w, int h)
    {
        cardholder.setSize(w,h);
        cardholder.repaint();
    }

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

    public void addCard()
    {
        sylladexcards.add(new SylladexCard(id, this));
        refreshDock();
        id++;
    }

    private Widget loadWidget(File file)
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
    		return widget;
    	}
    	catch (Exception e) {e.printStackTrace();}
    	return null;
    }
    
    public void addItem(String string)
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

    public void addItem(Image image)
    {
        modus.addItem(image);
    }

    public void addItem(File file)
    {
        modus.addItem(file);
    }

    public void addItem(Widget widget)
    {
    	widget.prepare();
        modus.addItem(widget);
    }

    private void openCard(SylladexCard card)
    {
        File file = card.getFile();
        String string = card.getString();
        Image image = card.getImage();
        Widget widget = card.getWidget();

        if(file!=null)
        {
            try
            {
                if(Desktop.isDesktopSupported())
                {
                    Desktop.getDesktop().open(file);
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if(string!=null)
        {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(string), co);
        }
        else if(image!=null)
        {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(image), co);
        }
        else if(widget!=null)
        {
            widget.open();
        }
    }

    public void open(SylladexCard card)
    {
        openCard(card);
        if(card.getFile()!=null) { card.setFile(null); }
        if(card.getString()!=null) { card.setString(null); }
        if(card.getImage()!=null) { card.setImage(null); }
        if(card.getWidget()!=null) { card.setWidget(null); }
    }

    public void openWithoutRemoval(SylladexCard card)
    {
        openCard(card);
    }

    public void removeCard(SylladexCard card)
    {
        sylladexcards.remove(card);
        refreshDock();
    }

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

    public ArrayList<SylladexCard> getCards()
    {
        return sylladexcards;
    }

    public FetchModus getModus()
    {
        return modus;
    }

    public Dimension getScreenSize()
    {
        return screensize;
    }

    public DeckPreferences getPreferences()
    {
        return prefs;
    }

    public int getDockIconYPosition()
    {
        if(prefs.top()==true) { return 10; } return 35;
    }

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

    public JLabel getIconLabelFromObject(Object o)
    {
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

    public static Icon getDockIcon(Image image)
    {
        return new ImageIcon(image.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
    }

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

    public Object getItem(String string)
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
        else if(string.startsWith("[WIDGET]"))
        {
        	String cut = string.replaceAll("\\[WIDGET\\]", "");
        	String path = cut.substring(0, cut.indexOf("[")-1);
        	Widget widget = loadWidget(new File(path));
        	widget.load(cut.substring(cut.indexOf("]")+1));
        	return widget;
        }
        return string.replaceAll("SYLLADEX_NL", System.getProperty("line.separator"));
    }

    // Transparency
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

    public static boolean isWindows()
    {
        String os = System.getProperty("os.name").toLowerCase();
        return os.indexOf("win") >= 0;
    }

    public static boolean isMac()
    {
        String os = System.getProperty("os.name").toLowerCase();
        return os.indexOf("mac") >= 0;
    }

    public static boolean isLinux()
    {
        String os = System.getProperty("os.name").toLowerCase();
        return (os.indexOf("nix") >= 0) || (os.indexOf("nux") >=0);
    }

    //Listeners-------------------------------------------------------------------------------------------------------

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
    public void windowClosing(WindowEvent w)
    {
        prefs.cleanUp();
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
