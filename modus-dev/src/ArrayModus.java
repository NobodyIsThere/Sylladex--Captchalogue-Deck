import java.awt.event.ActionEvent;

import sylladex.CaptchalogueCard;
import sylladex.FetchModus;
import sylladex.Main;
import sylladex.SylladexItem;
import util.Util.OpenReason;

public class ArrayModus extends FetchModus
{	
	public ArrayModus(Main m)
	{
		super(m);
	}
	
	@Override
	public void initialSettings()
	{
		settings.set_author("gumptiousCreator");
		settings.set_name("Array");
		settings.set_modus_image("modi/canon/array/modus.png");
		
		settings.set_background_color(6, 182, 255);
		settings.set_secondary_color(18, 148, 215);
		settings.set_text_color(254, 153, 171);
		
		settings.set_card_back_image("modi/canon/array/card_back.png");
		settings.set_card_image("modi/canon/array/card.png");
		settings.set_card_size(148, 188);
		settings.set_cards_draggable(true);
		settings.set_dock_text_image("modi/canon/array/docktext.png");
		settings.set_draw_empty_cards(true);
		
		settings.set_initial_card_number(10);
	}

	@Override
	public void actionPerformed(ActionEvent e) {}

	@Override
	public void prepare() {}
	
	@Override
	public void ready()
	{
		settings.set_bounce_captchalogued_items(true);
	}

	@Override
	public boolean captchalogue(SylladexItem item)
	{
		if (deck.isFull()) { return false; }
		deck.captchalogueItem(item);
		deck.setIcons(getCardOrder());
		return true;
	}
	
	@Override
	public Object[] getCardOrder()
	{
		return deck.getCards().toArray();
	}

	@Override
	public void open(CaptchalogueCard card, OpenReason reason)
	{
		deck.open(card, reason);
		deck.setIcons(getCardOrder());
	}

	@Override
	public void addCard()
	{
		deck.addCard();
	}
}
