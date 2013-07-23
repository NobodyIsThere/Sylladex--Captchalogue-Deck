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

public class StackModus extends FetchModus implements ActionListener
{
	public StackModus(Main m)
	{
		super(m);
	}

	private LinkedList<CaptchalogueCard> stack = new LinkedList<CaptchalogueCard>();
	
	@Override
	public void initialSettings()
	{
		settings.set_name("Stack");
		
		settings.set_preferences_file("modi/prefs/stackprefs.txt");
		
		settings.set_background_color(255, 6, 124);
		
		settings.set_secondary_color(148, 36, 70);
		
		settings.set_text_color(191, 255, 253);
		
		settings.set_initial_card_number(4);
		
		settings.set_origin(20, 120);
		
		settings.set_shade_inaccessible_cards(false);
	}
	
	@Override
	public void prepare() {}
	
	@Override
	public void ready() {}

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
			CaptchalogueCard card = stack.getLast();
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
		stack.addFirst(card);
		card.setLayer(100);
		card.setLocation(new Point(0, 0));
		deck.setIcons(stack.toArray());
	}
	
	@Override
	public void open(CaptchalogueCard card, OpenReason reason)
	{
		stack.remove(card);
		deck.setIcons(stack.toArray());
		deck.open(card, reason);
		arrangeCards(true);
	}
	
	@Override
	public void addCard()
	{
		deck.addCard();
	}

	@Override	
	public Object[] getCardOrder()
	{
		return stack.toArray();
	}

	private void animatePushOut(SylladexItem item)
	{
		arrangeCards(false);
		
		if (loading) { return; }
		
		for (CaptchalogueCard c : stack)
		{
			if (c != stack.getFirst())
				deck.bounceCard(c);
		}
		
		JLabel icon = item.getIcon();
		
		int xpos = Util.SCREEN_SIZE.width/2 + (25*deck.getCards().size());
		JLabel arrow = new JLabel(Util.createImageIcon("modi/canon/stack/arrow.gif"));
		arrow.setBounds(xpos,deck.getDockIconYPosition(),43,60);
		icon.setBounds(xpos+50,deck.getDockIconYPosition(),43,60);
		
		deck.showDock();
		
		foreground.add(arrow);
		foreground.add(icon);
		foreground.repaint();
		
		Animation.waitFor(1000, this, "stack_remove_arrow");
	}

	//Unique methods
	private void arrangeCards(boolean animate)
	{
		if (loading) { animate = false; }
		
		for (CaptchalogueCard c : deck.getCards())
		{
			int index = stack.indexOf(c);
			if (index > -1)
			{
				c.setVisible(true);
				c.setLayer(100 - index);
				c.setAccessible(false);
				Point p = new Point(index*23, index*23);
				if (animate)
				{
					c.moveTo(p, AnimationType.BOUNCE);
				}
				else
				{
					c.setLocation(p);
				}
			}
			else
			{
				c.setVisible(false);
			}
		}
		if (stack.size() > 0)
			stack.getFirst().setAccessible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("stack_remove_arrow"))
		{
			foreground.removeAll();
			foreground.repaint();
		}
	}
}