import sylladex.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class SbahjModus extends FetchModus implements ActionListener
{
	//Stack stuff
	private LinkedList<SylladexCard> stack = new LinkedList<SylladexCard>();
	private JLabel arrow;
	private Timer timer = new Timer(1000, this);
	
	public SbahjModus(Main m)
	{
		this.m = m;
		
		image_background_top = "modi/sbahj/dock_top.png";
		image_background_bottom = "modi/sbahj/dock.png";
		image_text = "modi/sbahj/text.png";
		image_card = "modi/sbahj/card.png";
		image_dock_card = "modi/sbahj/dockcard.png";
		
		info_image = "modi/sbahj/modus.png";
		info_name = "SWEET BRO AND HELLA JEFF";
		info_author = "gumtpiousCreator";
		
		item_file = "modi/items/queuestack.txt";
		prefs_file = "modi/prefs/stackprefs.txt";
		
		color_background = new Color(255, 0, 255);
		
		startcards = 8;
		origin = new Point(50,200);
		draw_default_dock_icons = true;
		draw_empty_cards = true;
		shade_inaccessible_cards = true;
		
		icons = new ArrayList<JLabel>();
	}
	
	//Inherited methods
	public void showSelectionWindow()
	{
		JOptionPane.showConfirmDialog(m.getCardHolder(), "JESUS DUFE WHAT YOU CLICK FOR");
	}
	
	public void addGenericItem(Object o)
	{
		checkBottomCard();
		SylladexCard card = m.getNextEmptyCard();
		SylladexItem item = new SylladexItem("ITEMP", o, m);
		card.setItem(item);
		
		stack.addFirst(card);
		JLabel icon = m.getIconLabelFromItem(item);
		icons.add(0, icon);
		m.setIcons(icons);
		card.setIcon(icon);
		arrangeCards();
	}
	
	public void open(SylladexCard card)
	{
		icons.remove(card.getIcon());
		icons.trimToSize();
		m.setIcons(icons);
		stack.remove(card);
		arrangeCards();
		m.open(card);
	}
	
	public void addCard()
	{
		m.addCard();
	}

	@Override
	public void prepare()
	{
		for(String string : items)
		{
			if(!string.equals(""))
			{
				if(m.getNextEmptyCard()==null) { m.addCard(); }
				SylladexCard card = m.getNextEmptyCard();
				SylladexItem item = new SylladexItem(string, m);
				card.setItem(item);
				stack.addLast(card);
				JLabel icon = m.getIconLabelFromItem(item);
				icons.add(icon);
				m.setIcons(icons);
				card.setIcon(icon);
				arrangeCards();
			}
		}
	}

	@Override
	public ArrayList<String> getItems()
	{
		ArrayList<String> items = new ArrayList<String>();
		if(stack.size()>0)
		{
			for(SylladexCard card : stack)
			{
				items.add(card.getItem().getSaveString());
			}
		}
		else { items.add(""); }
		return items;
	}

	//Unique methods
	public void checkBottomCard()
	{
		if(m.getNextEmptyCard()==null)
		{
			SylladexCard bottomcard = stack.getLast();
			icons.trimToSize();
			JLabel icon = new JLabel(bottomcard.getIcon().getIcon());

			int xpos = m.getScreenSize().width/2 + (25*m.getCards().size());
			arrow = new JLabel(Main.createImageIcon("modi/stack/arrow.gif"));
			arrow.setBounds(xpos,m.getDockIconYPosition(),43,60);
			icon.setBounds(xpos+50,m.getDockIconYPosition(),43,60);
			
			m.showDock();
			
			foreground.add(arrow);
			foreground.add(icon);
			foreground.repaint();
			
			timer.restart();
			open(bottomcard);
		}
	}
	
	public void arrangeCards()
	{
		ArrayList<SylladexCard> cards = m.getCards();
		for (SylladexCard card : cards)
		{
			int index = stack.indexOf(card);
			card.setPosition(new Point(index*25, index*26));
			card.setLayer(100-index);
			
			card.setAccessible(false);
		}
		if(stack.size()!=0)
			stack.getFirst().setAccessible(true);
		m.setCardHolderSize(stack.size()*20 + card_width, stack.size()*20 + card_height);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(timer))
		{
			foreground.removeAll();
			foreground.repaint();
		}
	}
}