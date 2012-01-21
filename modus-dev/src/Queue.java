import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.JLabel;
import javax.swing.Timer;
import sylladex.*;

public class Queue extends FetchModus implements ActionListener
{
	//Queue stuff
	private LinkedList<SylladexCard> queue = new LinkedList<SylladexCard>();
	private FetchModusSettings s;
	
	private JLabel arrow;
	private Timer timer = new Timer(1000, this);
	
	public Queue(Main m)
	{
		this.m = m;
		createModusSettings();
		icons = new ArrayList<JLabel>();
	}
	
	private void createModusSettings()
	{
		s = new FetchModusSettings();
		
		s.set_bottom_dock_image("modi/queue/dockbg.png");
		s.set_top_dock_image("modi/queue/dockbg_top.png");
		s.set_dock_text_image("modi/queue/docktext.png");
		s.set_card_image("modi/queue/card.png");
		s.set_card_back_image("modi/queue/back.png");
		
		s.set_modus_image("modi/queue/modus.png");
		s.set_name("Queue");
		s.set_author("gumptiousCreator");
		
		s.set_item_file("modi/items/queuestack.txt");
		s.set_preferences_file("modi/prefs/queueprefs.txt");
		
		s.set_background_color(255, 96, 0);
		
		s.set_initial_card_number(4);
		s.set_origin(20, 120);
	}
	
	public FetchModusSettings getModusSettings()
	{
		return s;
	}
	
	@Override
	public void prepare()
	{
		for(String string : items)
		{
			if(!string.equals(""))
			{
				if(m.getNextEmptyCard()==null){ m.addCard(); }
				SylladexCard card = m.getNextEmptyCard();
				SylladexItem item = new SylladexItem(string, m);
				card.setItem(item);
				queue.addLast(card);
				JLabel icon = m.getIconLabelFromItem(item);
				icons.add(icon);
				m.setIcons(fillIcons());
				card.setIcon(icon);
				arrangeCards();
			}
		}
	}

	public void addGenericItem(Object o)
	{
		checkBottomCard();
		SylladexCard card = m.getNextEmptyCard();
		SylladexItem item = new SylladexItem("ITEM", o, m);
		card.setItem(item);
		
		queue.addFirst(card);
		JLabel icon = m.getIconLabelFromItem(item);
		icons.add(queue.indexOf(card), icon);
		m.setIcons(fillIcons());
		card.setIcon(icon);
		arrangeCards();
	}

	public void showSelectionWindow(){}

	public void open(SylladexCard card)
	{
		icons.remove(card.getIcon());
		icons.trimToSize();
		m.setIcons(fillIcons());
		queue.remove(card);
		arrangeCards();
		m.open(card);
	}
	
	public void addCard()
	{
		m.addCard();
	}

	@Override
	public ArrayList<String> getItems()
	{
		ArrayList<String> items = new ArrayList<String>();
		if(queue.size()>0)
		{
			for(SylladexCard card : queue)
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
			SylladexCard bottomcard = queue.getLast();
			JLabel icon = new JLabel(bottomcard.getIcon().getIcon());

			int xpos = m.getScreenSize().width/2 + (25*m.getCards().size());
			arrow = new JLabel(Main.createImageIcon("modi/stack/arrow.gif"));
			arrow.setBounds(xpos,m.getDockIconYPosition(),43,60);
			icon.setBounds(xpos+50,m.getDockIconYPosition(),43,60);
			
			m.showDock();
			
			foreground.setLayout(null);
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
			int index = queue.indexOf(card);
			card.setPosition(new Point(index*19, index*5));
			card.setLayer(100-index);
			
			card.setAccessible(false);
		}
		if(queue.size()!=0)
			queue.getLast().setAccessible(true);
		m.setCardHolderSize(queue.size()*23 + 2*s.get_card_width(), queue.size()*10 + s.get_card_height());
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<JLabel> fillIcons()
	{
		ArrayList<JLabel> newicons = (ArrayList<JLabel>) icons.clone();
		newicons.trimToSize();
		while(newicons.size()<m.getCards().size())
		{
			newicons.add(0, new JLabel(""));
		}
		return newicons;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(timer))
		{
			foreground.removeAll();
			foreground.revalidate();
			foreground.repaint();
		}
		else if(e.getActionCommand().equals("card mouse enter"))
		{
			if(queue.size()>1)
			{
				SylladexCard card = queue.getLast();
				Point destination = new Point((queue.indexOf(card)-1)*19 + s.get_card_width(), card.getPosition().y);
				if(!card.getPosition().equals(destination))
				{ card.setPosition(new Point((queue.indexOf(card)-1)*19 + s.get_card_width(), card.getPosition().y)); }
			}
		}
		else if(e.getActionCommand().equals("card mouse exit"))
		{
			arrangeCards();
		}
	}
}
