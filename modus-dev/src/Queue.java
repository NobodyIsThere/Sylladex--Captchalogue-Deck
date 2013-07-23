import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JLabel;

import sylladex.CaptchalogueCard;
import sylladex.FetchModus;
import sylladex.Main;
import sylladex.SylladexItem;
import util.Animation;
import util.Animation.AnimationType;
import util.Util;
import util.Util.OpenReason;

public class Queue extends FetchModus implements ActionListener
{
	private LinkedList<CaptchalogueCard> queue = new LinkedList<CaptchalogueCard>();
	
	private boolean cursor_within_cards = false;
	
	public Queue(Main m)
	{
		super(m);
	}
	
	@Override
	public void initialSettings()
	{
		settings.set_dock_text_image("modi/canon/queue/docktext.png");
		settings.set_card_image("modi/canon/queue/card.png");
		settings.set_card_back_image("modi/canon/queue/back.png");
		
		settings.set_modus_image("modi/canon/queue/modus.png");
		settings.set_name("Queue");
		settings.set_author("gumptiousCreator");
		
		settings.set_preferences_file("modi/prefs/queueprefs.txt");
		
		settings.set_background_color(255, 96, 0);
		settings.set_secondary_color(207, 86, 12);
		settings.set_text_color(183, 255, 253);
		
		settings.set_initial_card_number(4);
		settings.set_origin(20, 120);
	}
	
	@Override
	public void prepare() {}
	
	@Override
	public void ready()
	{
		deck.setIcons(getCardOrder());
	}

	@Override
	public boolean captchalogue(SylladexItem item)
	{
		if (!deck.isFull())
		{
			finishCaptchalogue(item);
			arrangeCards(true);
		}
		else
		{
			CaptchalogueCard card = queue.getLast();
			SylladexItem last_item = card.getItem();
			open(card, OpenReason.MODUS_PUSH);
			finishCaptchalogue(item);
			animatePushOut(last_item);
		}
		return true;
	}
	
	private void finishCaptchalogue(SylladexItem item)
	{
		CaptchalogueCard card = deck.captchalogueItemAndReturnCard(item);
		queue.addFirst(card);
		card.setLayer(100);
		card.setLocation(new Point(0, 0));
		deck.setIcons(getCardOrder());
	}

	@Override
	public void open(CaptchalogueCard card, OpenReason reason)
	{
		deck.setIcons(getCardOrder());
		queue.remove(card);
		deck.open(card, reason);
		arrangeCards(true);
		deck.setIcons(getCardOrder());
	}
	
	public void addCard()
	{
		deck.addCard();
	}

	@Override
	public Object[] getCardOrder()
	{
		Object[] icons = queue.toArray();
		Object[] newicons = new Object[deck.getCards().size()];
		if (!deck.isEmpty())
			System.arraycopy(icons, 0, newicons, deck.getCards().size() - queue.size(), icons.length);
		return newicons;
	}

	//Unique methods
	public void animatePushOut(SylladexItem item)
	{
		arrangeCards(false);
		
		if (loading) { return; }
		
		deck.bounceCard(queue.getLast());
		
		JLabel icon = item.getIcon();
		
		int xpos = Util.SCREEN_SIZE.width/2 + (25*deck.getCards().size());
		JLabel arrow = new JLabel(Util.createImageIcon("modi/canon/stack/arrow.gif"));
		arrow.setBounds(xpos,deck.getDockIconYPosition(),43,60);
		icon.setBounds(xpos+50,deck.getDockIconYPosition(),43,60);
		
		deck.showDock();
		
		foreground.add(arrow);
		foreground.add(icon);
		foreground.repaint();
		
		Animation.waitFor(1000, this, "queue_remove_arrow");
	}
	
	public void arrangeCards(boolean animate)
	{
		if (loading) { animate = false; }
		
		for (CaptchalogueCard card : deck.getCards())
		{
			int index = queue.indexOf(card);
			if (index > -1)
			{
				Point p = new Point(index*19, index*5);
				if (animate)
				{
					card.moveTo(p, AnimationType.BOUNCE);
				}
				else
				{
					card.setLocation(p);
				}
				card.setLayer(100-index);
				card.setAccessible(false);
				card.setVisible(true);
			}
			else
			{
				card.setVisible(false);
			}
		}
		if (queue.size() > 0)
			queue.getLast().setAccessible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("queue_remove_arrow"))
		{
			foreground.removeAll();
			foreground.revalidate();
			foreground.repaint();
		}
		else if (e.getActionCommand().equals(Util.ACTION_CARD_MOUSE_ENTER))
		{
			if(queue.size()>1)
			{
				cursor_within_cards = true;
				CaptchalogueCard card = queue.getLast();
				Point destination = new Point((queue.indexOf(card)-1)*19 + settings.get_card_width(), card.getLocation().y);
				if(!card.getLocation().equals(destination))
				{ card.moveTo(new Point((queue.indexOf(card)-1)*19 + settings.get_card_width(), card.getLocation().y), AnimationType.MOVE); }
			}
		}
		else if(e.getActionCommand().equals(Util.ACTION_CARD_MOUSE_EXIT))
		{
			cursor_within_cards = false;
			Animation.waitFor(10, this, "queue_check_mouse");
		}
		else if (e.getActionCommand().equals("queue_check_mouse"))
		{
			if (!cursor_within_cards)
				arrangeCards(true);
		}
	}
}
