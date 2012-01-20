package sylladex;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;

public abstract class FetchModus implements ActionListener
{
	//Declarations
	protected Main m;
	
	protected String image_background_bottom = "modi/stack/dockbg.png";
	protected String image_background_top = "modi/stack/dockbg_top.png";
	protected String image_text = "modi/stack/docktext.png";
	protected String image_card = "modi/stack/card.png";
	protected String image_card_back = "modi/stack/back.png";
	protected String image_dock_card = "modi/global/dockcard.png";
	protected String image_flip_button = "modi/global/flip.png";
	
	protected String info_image = "modi/stack/modus.png";
	protected String info_name = "Untitled";
	protected String info_author = "anonymous";
	
	protected String item_file = "modi/items/queuestack.txt";
	protected String prefs_file = "modi/prefs/" + info_name + "prefs.txt";
	
	protected Color color_background = new Color(255,255,255);
	
	protected int startcards = 1;
	protected Point origin = new Point(0,0);
	
	protected int card_width = 148;
	protected int card_height = 188;
	
	protected ArrayList<JLabel> icons;
	
	protected JPanel preferences_panel = new JPanel();
	
	protected ArrayList<String> preferences = new ArrayList<String>();
	
	protected ArrayList<String> items = new ArrayList<String>();
	
	protected JPanel background = new JPanel();
	
	protected JPanel foreground = new JPanel();
	
	protected boolean draw_default_dock_icons = true;
	protected boolean shade_inaccessible_cards = true;
	protected boolean draw_empty_cards = false;
	protected boolean draggable_cards = true;
	
	//Class functions
	/**
	 * Called when the modus has been selected. Preferences and items will be available at this point.
	 */
	public abstract void prepare();
	
	public abstract void addGenericItem(Object o);
	
	public void addItem(String string)
	{
		addGenericItem(string);
	}
	public void addItem(Image image)
	{
		addGenericItem(image);
	}
	public void addItem(File file)
	{
		addGenericItem(file);
	}
	public void addItem(Widget widget)
	{
		addGenericItem(widget);
	}

	public abstract void open(SylladexCard card);
	
	public abstract void addCard();
	
	public abstract void showSelectionWindow();
	
	public abstract ArrayList<String> getItems();
	
	public ArrayList<JLabel> getDockIcons()
	{
		return icons;
	}
	//Utility functions
	public String getTopBgUrl()
	{
		return image_background_top;
	}
	public String getBottomBgUrl()
	{
		return image_background_bottom;
	}
	public String getTextUrl()
	{
		return image_text;
	}
	public String getCardBgUrl()
	{
		return image_card;
	}
	public String getCardBackBgUrl()
	{
		return image_card_back;
	}
	public String getDockCardBg()
	{
		return image_dock_card;
	}
	public String getFlipButtonBgUrl()
	{
		return image_flip_button;
	}
	public Color getBackgroundColour()
	{
		return color_background;
	}
	public boolean drawDefaultDockIcons()
	{
		return draw_default_dock_icons;
	}
	public ArrayList<JLabel> getIcons()
	{
		return icons;
	}
	public JPanel getPreferencesPanel()
	{
		return preferences_panel;
	}
	public void setPreferences(ArrayList<String> prefs)
	{
		preferences = prefs;
	}
	public ArrayList<String> getPreferences()
	{
		return preferences;
	}
	public void setItems(ArrayList<String> items)
	{
		this.items = items;
	}
	public JPanel getBackground()
	{
		return background;
	}
	public JPanel getForeground()
	{
		foreground.setLayout(null);
		return foreground;
	}
	public Point getOrigin()
	{
		return origin;
	}
	public int getCardWidth()
	{
		return card_width;
	}
	public int getCardHeight()
	{
		return card_height;
	}
	public boolean areCardsDraggable()
	{
		return draggable_cards;
	}
}
