package sylladex;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

public class CardDisplayManager
{
	private Main deck;
	
	private Point origin = new Point(0, 0);
	private Dimension size = new Dimension(0, 0);
	
	private int margin = 0;
	
	private boolean frozen = false;
	
	private static final int RIGHT = 0, UP = 1, LEFT = 2, DOWN = 3;
	
	public CardDisplayManager(Main deck)
	{
		this.deck = deck;
	}
	
	public Point getAbsolutePosition(Point relative_position)
	{
		return new Point(origin.x + relative_position.x,
						origin.y + relative_position.y);
	}
	
	public Point getRelativePosition(Point absolute_position)
	{
		return new Point(absolute_position.x - origin.x,
						absolute_position.y - origin.y);
	}
	
	public void adjustSize()
	{
		if (frozen) return;
		ArrayList<CaptchalogueCard> cards = deck.getCards();
		
		int minimum_x = size.width - margin;
		int maximum_x = margin;
		int minimum_y = size.height - margin;
		int maximum_y = margin;
		
		int card_width = cards.get(0).getWidth();
		int card_height = cards.get(0).getHeight();
		
		for (CaptchalogueCard card : cards)
		{
			int x = card.getAbsoluteLocation().x;
			int y = card.getAbsoluteLocation().y;
			
			if (x + card_width > maximum_x) { maximum_x = x + card_width; }
			if (x < minimum_x) { minimum_x = x; }
			if (y + card_height > maximum_y) { maximum_y = y + card_height; }
			if (y < minimum_y) { minimum_y = y; }
		}
		
		if (minimum_x < margin)
		{
			expandLeft(Math.abs(minimum_x - margin));
		}
		if (minimum_y < margin)
		{
			expandUp(Math.abs(minimum_y - margin));
		}
		if (minimum_x > margin)
		{
			minimum_x -= margin;
			moveAllCards(LEFT, minimum_x);
			size.setSize(size.width - minimum_x, size.height);
			maximum_x -= minimum_x;
		}
		if (minimum_y > margin)
		{
			minimum_y -= margin;
			moveAllCards(UP, minimum_y);
			size.setSize(size.width, size.height - minimum_y);
			maximum_y -= minimum_y;
		}
		
		size.setSize(maximum_x + margin, maximum_y + margin);
		deck.getCardHolder().setSize(size);
	}
	
	private void moveAllCards(int direction, int distance)
	{	
		switch (direction)
		{
			case (RIGHT):
			{
				for (CaptchalogueCard card : deck.getCards())
				{
					Point currentpos = card.getAbsoluteLocation();
					card.setAbsoluteLocation(new Point(currentpos.x + distance, currentpos.y));
				}
				origin.x += distance;
				Point currentpos = deck.getCardHolder().getLocation();
				deck.getCardHolder().setLocation(new Point(currentpos.x - distance,
															currentpos.y));
				break;
			}
			case (UP):
			{
				for (CaptchalogueCard card : deck.getCards())
				{
					Point currentpos = card.getAbsoluteLocation();
					card.setAbsoluteLocation(new Point(currentpos.x, currentpos.y - distance));
				}
				origin.y -= distance;
				Point currentpos = deck.getCardHolder().getLocation();
				deck.getCardHolder().setLocation(new Point(currentpos.x,
															currentpos.y + distance));
				break;
			}
			case (LEFT):
			{
				for (CaptchalogueCard card : deck.getCards())
				{
					Point currentpos = card.getAbsoluteLocation();
					card.setAbsoluteLocation(new Point(currentpos.x - distance, currentpos.y));
				}
				origin.x -= distance;
				Point currentpos = deck.getCardHolder().getLocation();
				deck.getCardHolder().setLocation(new Point(currentpos.x + distance,
															currentpos.y));
				break;
			}
			case (DOWN):
			{
				for (CaptchalogueCard card : deck.getCards())
				{
					Point currentpos = card.getAbsoluteLocation();
					card.setAbsoluteLocation(new Point(currentpos.x, currentpos.y + distance));
				}
				origin.y += distance;
				Point currentpos = deck.getCardHolder().getLocation();
				deck.getCardHolder().setLocation(new Point(currentpos.x,
															currentpos.y - distance));
				break;
			}
		}
	}

	public void freeze()
	{
		frozen = true;
	}
	
	public void unfreeze()
	{
		frozen = false;
		adjustSize();
	}
	
	public void setMargin(int margin)
	{
		this.margin = margin;
	}
	
	public int getMargin()
	{
		return margin;
	}
	
	public void expandLeft(int distance)
	{
		size.setSize(size.width + distance, size.height);
		moveAllCards(RIGHT, distance);
	}
	
	public void expandUp(int distance)
	{
		size.setSize(size.width, size.height + distance);
		moveAllCards(DOWN, distance);
	}
	
	public void expandRight(int distance)
	{
		size.setSize(size.width + distance, size.height);
	}
	
	public void expandDown(int distance)
	{
		size.setSize(size.width, size.height + distance);
	}
	
	public void updateCardHolderSize()
	{
		deck.getCardHolder().setSize(size);
	}
}
