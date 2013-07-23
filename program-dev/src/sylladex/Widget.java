package sylladex;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ui.MenuItem;
import util.Util.OpenReason;

public abstract class Widget implements MouseListener
{
	protected CaptchalogueCard card;
	protected JLabel dock_icon = new JLabel("");
	protected Main deck;
	
	public abstract void prepare();
	public abstract void add();
	public abstract void load(String string);
	public abstract void open(OpenReason reason);
	public abstract String getName();
	public abstract String getSaveString();
	
	public String canonCaptchaCodeOverride()
	{
		return null;
	}
	
	public void setCard(CaptchalogueCard newcard)
	{
		card = newcard;
	}
	
	public void setMain(Main m)
	{
		this.deck = m;
	}
	
	public CaptchalogueCard getCard()
	{
		return card;
	}
	
	public abstract JPanel getPanel();
	
	public JLabel getDockIcon()
	{
		return dock_icon;
	}
	
	public ArrayList<MenuItem> getExtraMenuItems()
	{
		return new ArrayList<MenuItem>();
	}
	
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}
