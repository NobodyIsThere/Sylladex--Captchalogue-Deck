package sylladex;

import javax.swing.JPanel;
import javax.swing.JLabel;

public abstract class Widget
{
	private SylladexCard card;
	private JPanel panel = new JPanel();
	private JLabel dock_icon = new JLabel("");
	
	public abstract void prepare();
	public abstract void load(String string);
	public abstract void open();
	public abstract String getSaveString();
	
	public void setCard(SylladexCard newcard)
	{
		card = newcard;
	}
	
	public SylladexCard getCard()
	{
		return card;
	}
	
	public JPanel getPanel()
	{
		return panel;
	}
	
	public JLabel getDockIcon()
	{
		return dock_icon;
	}
}
