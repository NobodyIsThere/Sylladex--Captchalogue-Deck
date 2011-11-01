package sylladex;

import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.JLabel;

public abstract class Widget implements MouseListener
{
	protected SylladexCard card;
	protected JPanel panel = new JPanel();
	protected JLabel dock_icon = new JLabel("");
	protected Main m;
	
	public abstract void prepare();
	public abstract void add();
	public abstract void load(String string);
	public abstract void open();
	public abstract String getString();
	public abstract String getSaveString();
	
	public void setCard(SylladexCard newcard)
	{
		card = newcard;
	}
	
	public void setMain(Main m)
	{
		this.m = m;
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
