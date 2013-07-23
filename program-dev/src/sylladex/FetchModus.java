package sylladex;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import util.Util.OpenReason;

public abstract class FetchModus implements ActionListener
{
	//Declarations
	protected Main deck;
	
	protected FetchModusSettings settings;
	
	protected JPanel preferences_panel = new JPanel();
	
	protected ArrayList<String> preferences = new ArrayList<String>();
	
	protected ArrayList<String> items = new ArrayList<String>();
	
	protected JPanel background = new JPanel();
	
	protected JPanel foreground = new JPanel();
	
	protected boolean loading = true;
	
	public FetchModus(Main m)
	{
		deck = m;
		settings = new FetchModusSettings();
	}
	
	/**
	 * Called before prepare(). Images etc. should be set here.
	 */
	public abstract void initialSettings();
	
	/**
	 * Called before prepare(), after initialSettings().
	 */
	public FetchModusSettings getSettings()
	{
		return settings;
	}
	
	/**
	 * Called when the modus has been selected. Preferences and items will be available at this point.
	 */
	public abstract void prepare();
	
	/**
	 * Called when all items have been loaded
	 */
	public abstract void ready();
	
	/**
	 * Called when the user drags an item to the sylladex.
	 * @param item - The item that the user is attempting to captchalogue.
	 */
	public abstract boolean captchalogue(SylladexItem item);

	/**
	 * Called when the user removes the item from the card.
	 * @param card
	 */
	public abstract void open(CaptchalogueCard card, OpenReason reason);
	
	/**
	 * Called when the user attempts to add a card to the deck. Common responses are either to leave this function
	 * empty (if you don't want to add a card), or to type m.addCard().
	 */
	public abstract void addCard();
	
	public abstract Object[] getCardOrder();
	
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
	public JPanel getBackground()
	{
		return background;
	}
	public JPanel getForeground()
	{
		foreground.setLayout(null);
		return foreground;
	}

	
	public void refreshDock() {}
}
