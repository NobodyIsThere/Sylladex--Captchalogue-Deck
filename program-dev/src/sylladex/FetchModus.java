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
	
	protected ArrayList<JLabel> icons;
	
	protected JPanel preferences_panel = new JPanel();
	
	protected ArrayList<String> preferences = new ArrayList<String>();
	
	protected ArrayList<String> items = new ArrayList<String>();
	
	protected JPanel background = new JPanel();
	
	protected JPanel foreground = new JPanel();
	
	/**
	 * @return An instance of FetchModusSettings containing vital information about the modus.
	 */
	public abstract FetchModusSettings getModusSettings();
	
	/**
	 * Called when the modus has been selected. Preferences and items will be available at this point.
	 */
	public abstract void prepare();
	
	/**
	 * Called when the user drags an item to the sylladex.
	 * @param o - The File, Image, String etc. that the user is attempting to captchalogue.
	 */
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

	/**
	 * Called when the user removes the item from the card.
	 * @param card
	 */
	public abstract void open(SylladexCard card);
	
	/**
	 * Called when the user attempts to add a card to the deck. Common responses are either to leave this function
	 * empty (if you don't want to add a card), or to type m.addCard().
	 */
	public abstract void addCard();
	
	/**
	 * Called when the user left-clicks on the dock.
	 */
	public abstract void showSelectionWindow();
	
	/**
	 * Called when the state of the modus is required. Each element of the ArrayList corresponds to one line of the
	 * item file.
	 */
	public abstract ArrayList<String> getItems();
	
	public ArrayList<JLabel> getDockIcons()
	{
		return icons;
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
}
