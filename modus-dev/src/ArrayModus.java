import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import sylladex.*;
import sylladex.Animation.AnimationType;
import javax.swing.JLabel;

public class ArrayModus extends FetchModus
{
	private FetchModusSettings s;
	private ArrayList<SylladexCard> array;
	
	public ArrayModus(Main m)
	{
		this.m = m;
		createModusSettings();
	}
	
	private void createModusSettings()
	{
		s = new FetchModusSettings();
		
		s.set_author("gumptiousCreator");
		s.set_name("Array");
		s.set_modus_image("modi/array/modus.png");
		
		s.set_background_color(100, 100, 255);
		s.set_bottom_dock_image("modi/array/dockbg.png");
		s.set_top_dock_image("modi/array/dockbg_top.png");
		s.set_card_back_image("modi/array/card_back.png");
		s.set_card_image("modi/array/card.png");
		s.set_card_size(148, 188);
		s.set_cards_draggable(true);
		s.set_dock_text_image("modi/array/docktext.png");
		s.set_draw_empty_cards(true);
		
		s.set_initial_card_number(10);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("card mouse enter"))
		{
			SylladexCard card = (SylladexCard)e.getSource();
			
			Animation a = new Animation(card,
										new Point(card.getPosition().x + 10, card.getPosition().y),
										AnimationType.BOUNCE,
										this, "bounce complete");
			a.run();
		}
		
		if (e.getActionCommand().equals("card mouse exit"))
		{
			SylladexCard card = (SylladexCard)e.getSource();
			card.setPosition(new Point(0, card.getPosition().y));
		}
		
		if (e.getActionCommand().equals("bounce complete"))
		{
			//blah
		}
	}

	@Override
	public FetchModusSettings getModusSettings()
	{
		return s;
	}

	@Override
	public void prepare()
	{
		icons = new ArrayList<JLabel>();
		array = new ArrayList<SylladexCard>();
		loadItems();
		arrangeCards();
	}
	
	private void loadItems()
	{
		for(String string : items)
		{
			if(m.getNextEmptyCard()==null){ m.addCard(); }
			SylladexCard card = m.getNextEmptyCard();
			
			SylladexItem item = new SylladexItem(string, m);
			card.setItem(item);
			JLabel icon = m.getIconLabelFromItem(item);
			card.setIcon(icon);
			icons.add(icon);
			m.setIcons(icons);
			array.add(card);
		}
	}

	@Override
	public void addGenericItem(Object o)
	{
		if(m.getNextEmptyCard()==null){ return; }
		
		SylladexCard card = m.getNextEmptyCard();
		SylladexItem item = new SylladexItem("ITEM413", o, m);
		
		card.setItem(item);
		array.add(card);
		
		JLabel icon = m.getIconLabelFromItem(item);
		icons.add(icon);
		m.setIcons(icons);
		card.setIcon(icon);
		arrangeCards();
	}

	@Override
	public void open(SylladexCard card)
	{
		icons.remove(card.getIcon());
		m.setIcons(icons);
		array.remove(card);
		m.open(card);
		arrangeCards();
	}

	@Override
	public void addCard()
	{
		arrangeCards();
	}

	@Override
	public void showSelectionWindow()
	{
		open(array.get((int)Math.random()*array.size()));
	}

	private void arrangeCards()
	{
		m.setCardHolderSize(s.get_card_width() + 15,
							m.getCards().size()*(2 + s.get_card_height()));
		for (SylladexCard card : m.getCards())
		{
			card.setPosition(new Point(0,m.getCards().indexOf(card)*s.get_card_height()));
			
		}
	}
	
	@Override
	public ArrayList<String> getItems()
	{
		ArrayList<String> i = new ArrayList<String>();
		
		for(SylladexCard card : array)
		{
			i.add(card.getItem().getSaveString());
		}
		
		return i;
	}
	
}
