import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import sylladex.*;
import sylladex.Animation.AnimationType;

public class Memory extends FetchModus
{
	private FetchModusSettings s;
	private ArrayList<SylladexCard> array;
	
	public Memory(Main m)
	{
		this.m = m;
		createModusSettings();
	}
	
	private void createModusSettings()
	{
		s = new FetchModusSettings();
		
		s.set_bottom_dock_image("modi/memory/dockbg.png");
		s.set_top_dock_image("modi/memory/dockbg_top.png");
		s.set_dock_text_image("modi/memory/docktext.png");
		s.set_card_image("modi/memory/card.png");
		s.set_card_back_image("modi/memory/back.png");
		
		s.set_modus_image("modi/memory/modus.png");
		s.set_name("Memory");
		s.set_author("gumptiousCreator");
		
		s.set_item_file("modi/items/queuestack.txt");
		s.set_preferences_file("modi/prefs/memoryprefs.txt");
		
		s.set_background_color(255, 255, 0);
		
		s.set_initial_card_number(10);
		s.set_origin(20, 120);
		
		s.set_card_size(105, 133);
		
		s.set_cards_draggable(false);
		s.set_draw_empty_cards(true);
		s.set_shade_inaccessible_cards(false);
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
		addLoadedItems();
	}
	
	private void addLoadedItems()
	{
		for(String s : items)
		{
			if(m.getNextEmptyCard()==null) { m.addCard(); }
			SylladexItem item = new SylladexItem(s, m);
			SylladexCard card = m.getNextEmptyCard();
			JLabel icon = m.getIconLabelFromItem(item);
			card.setIcon(icon);
			card.setItem(item);
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
		SylladexItem item = new SylladexItem("ITEM", o, m);
		JLabel icon = m.getIconLabelFromItem(item);
		card.setIcon(icon);
		icons.add(icon);
		card.setItem(item);
		array.add(card);
		
		m.setIcons(icons);
	}

	@Override
	public void open(SylladexCard card)
	{
		array.remove(card);
		icons.remove(card.getIcon());
		m.open(card);
		m.setIcons(icons);
	}

	@Override
	public void addCard()
	{
		m.addCard();
	}

	@Override
	public void showSelectionWindow()
	{
		Game game = new Game();
		game.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		game.setTitle("Memory");
		game.setLayout(null);
		game.setSize(675,476);
		game.setResizable(false);
		game.setLocationRelativeTo(null);
		
		System.out.println("Showing window.");
		game.setVisible(true);
	}

	@Override
	public ArrayList<String> getItems()
	{
		items = new ArrayList<String>();
		for(SylladexCard card : array)
		{
			items.add(card.getItem().getSaveString());
		}
		return items;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("serial")
	private class Game extends JFrame
	{	
		private MemoryCard currentcard = null;
		private MemoryCard card2 = null;
		private boolean resetting = false;
		
		public Game()
		{
			createGame();
		}
		
		private void createGame()
		{
			JPanel cardpanel = new JPanel();
			cardpanel.setLayout(null);
			int height = s.get_card_height()*(array.size()/3 + 1) + 20;
			cardpanel.setBounds(0, 0, 650, height);
			cardpanel.setPreferredSize(new Dimension(650, height));
			JScrollPane pane = new JScrollPane(cardpanel);
			pane.setBounds(0, 0, 670, 450);
			pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			
			ArrayList<MemoryCard> mcards = new ArrayList<MemoryCard>();
			for(SylladexCard card : array)
			{
				mcards.add(new MemoryCard(card));
				mcards.add(new MemoryCard(card));
			}
			
			int x = 4;
			int y = 10;
			
			while(mcards.size()>0)
			{
				int index = (int)Math.floor(Math.random()*mcards.size());
				MemoryCard card = mcards.get(index);
				card.setBounds(x, y, s.get_card_width(), s.get_card_height());
				cardpanel.add(card);
				mcards.remove(index);
				
				x += s.get_card_width() + 2;
				if(x>650-s.get_card_width())
				{
					y += s.get_card_height() + 2;
					x = 4;
				}
			}
			this.add(pane);
		}
		
		private class MemoryCard extends JLabel implements MouseListener, ActionListener
		{
			private SylladexCard card;
			private boolean uncovered;
			
			private JPanel panel;
			
			public MemoryCard(SylladexCard card)
			{
				this.card = card;
				setIcon(Main.createImageIcon("modi/memory/card_hidden.png"));
				panel = card.getItem().getPanel();
				panel.setBounds(0, 0, s.get_card_width(), s.get_card_height());
				panel.setVisible(false);
				add(panel);
				addMouseListener(this);
			}
			
			public void setUncovered(boolean uncovered)
			{
				this.uncovered = uncovered;
				panel.setVisible(uncovered);
				
				if(uncovered)
				{
					setIcon(Main.createImageIcon(s.get_card_image()));
				}
				else
				{
					setIcon(Main.createImageIcon("modi/memory/card_hidden.png"));
				}
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if(!resetting && !uncovered)
				{
					setUncovered(true);
					if(currentcard == null)
					{
						currentcard = this;
					}
					else
					{
						if(currentcard.card == this.card)
						{
							// Win the game
							open(card);
							currentcard = null;
						}
						else
						{
							// Lose this time
							resetting = true;
							card2 = this;
							new Animation(AnimationType.WAIT, 1000, this, "Reset").run();
						}
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0){}
			@Override
			public void mouseExited(MouseEvent arg0){}
			@Override
			public void mousePressed(MouseEvent arg0){}
			@Override
			public void mouseReleased(MouseEvent arg0){}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(e.getActionCommand().equals("Reset"))
				{
					currentcard.setUncovered(false);
					card2.setUncovered(false);
					currentcard = null;
					card2 = null;
					resetting = false;
				}
			}
		}
	}
}
