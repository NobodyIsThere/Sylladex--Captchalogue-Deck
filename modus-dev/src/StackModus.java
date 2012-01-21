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
	private FetchModusSettings s;
	
	private JLabel arrow;
	private Timer timer = new Timer(1000, this);
	
	public StackModus(Main m)
	{
		this.m = m;
		createModusSettings();
		icons = new ArrayList<JLabel>();
	}
	
	private void createModusSettings()
	{
		s = new FetchModusSettings();
		
		s.set_name("Stack");
		s.set_author("gumptiousCreator");
		
		s.set_item_file("modi/items/queuestack.txt");
		s.set_preferences_file("modi/prefs/stackprefs.txt");
		
		s.set_background_color(255, 6, 124);
		
		s.set_initial_card_number(4);
		s.set_origin(20, 120);
	}
	
	public FetchModusSettings getModusSettings()
	{
		return s;
	}
	
	//Inherited methods
	public void showSelectionWindow(){}
	
	public void addGenericItem(Object o)
	{
		checkBottomCard();
		SylladexCard card = m.getNextEmptyCard();
		SylladexItem item = new SylladexItem("ITEM", o, m);
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
			card.setPosition(new Point(index*23, index*23));
			card.setLayer(100-index);
			
			card.setAccessible(false);
		}
		if(stack.size()!=0)
			stack.getFirst().setAccessible(true);
		m.setCardHolderSize(stack.size()*23 + s.get_card_width(), stack.size()*23 + s.get_card_height());
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