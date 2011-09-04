import sylladex.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class StackModus extends FetchModus implements ActionListener
{
	//Stack stuff
	private LinkedList<SylladexCard> stack = new LinkedList<SylladexCard>();
	private JLabel arrow;
	private Timer timer = new Timer(1000, this);
	
	public StackModus(Main m)
	{
		this.m = m;
		
		info_image = "modi/stack/modus.png";
		info_name = "Stack";
		info_author = "gumptiousCreator";
		color_background = new Color(255, 6, 124);
		prefs_file = "modi/prefs/stackprefs.txt";
		
		startcards = 4;
		origin = new Point(21,120);
		
		icons = new ArrayList<JLabel>();
	}
	
	//Inherited methods
	public void showSelectionWindow(){}
	
	public void addGenericItem(Object o)
	{
		checkBottomCard();
		SylladexCard card = m.getNextEmptyCard();
		card.setItem(o);
		
		stack.addFirst(card);
		JLabel icon = m.getIconLabelFromObject(o);
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
				Object o = m.getItem(string);
				card.setItem(o);
				stack.addLast(card);
				JLabel icon = m.getIconLabelFromObject(o);
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
				items.add(card.getSaveString());
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
			arrow = new JLabel(m.createImageIcon("modi/stack/arrow.gif"));
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
			card.setPosition(new Point(index*23, index*23));
			card.setLayer(100-index);
			
			card.setAccessible(false);
		}
		if(stack.size()!=0)
			stack.getFirst().setAccessible(true);
		m.setCardHolderSize(stack.size()*23 + card_width, stack.size()*23 + card_height);
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