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
	private JLabel arrow;
	private Timer timer = new Timer(1000, this);
	
	public Queue(Main m)
	{
		this.m = m;
		image_background_top = "modi/queue/dockbg_top.png";
		image_background_bottom = "modi/queue/dockbg.png";
		image_text = "modi/queue/docktext.png";
		image_card = "modi/queue/card.png";
		
		info_image = "modi/queue/modus.png";
		info_name = "Queue";
		info_author = "gumptiousCreator";
		prefs_file = "modi/prefs/queueprefs.txt";
		
		color_background = new Color(255, 96, 0);
		
		startcards = 4;
		origin = new Point(21,120);
		draw_default_dock_icons = true;
		card_width = 148;
		card_height = 188;
		
		icons = new ArrayList<JLabel>();
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
				Object o = m.getItem(string);
				card.setItem(o);
				queue.addLast(card);
				JLabel icon = m.getIconLabelFromObject(o);
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
		card.setItem(o);
		
		queue.addFirst(card);
		JLabel icon = m.getIconLabelFromObject(o);
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
		m.setCardHolderSize(queue.size()*23 + 2*card_width, queue.size()*10 + card_height);
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
				Point destination = new Point((queue.indexOf(card)-1)*19 + card_width, card.getPosition().y);
				if(!card.getPosition().equals(destination))
				{ card.setPosition(new Point((queue.indexOf(card)-1)*19 + card_width, card.getPosition().y)); }
			}
		}
		else if(e.getActionCommand().equals("card mouse exit"))
		{
			arrangeCards();
		}
	}
}
