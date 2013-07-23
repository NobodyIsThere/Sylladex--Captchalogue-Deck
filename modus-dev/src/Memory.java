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

import sylladex.CaptchalogueCard;
import sylladex.FetchModus;
import sylladex.Main;
import sylladex.SylladexItem;
import util.Animation;
import util.Animation.AnimationType;
import util.Util;
import util.Util.OpenReason;

public class Memory extends FetchModus
{	
	public Memory(Main m)
	{
		super(m);
	}
	
	public void initialSettings()
	{
		settings.set_dock_text_image("modi/canon/memory/docktext.png");
		settings.set_card_image("modi/canon/memory/card.png");
		settings.set_card_back_image("modi/canon/memory/back.png");
		
		settings.set_modus_image("modi/canon/memory/modus.png");
		settings.set_name("Memory");
		settings.set_author("gumptiousCreator");
		
		settings.set_preferences_file("modi/prefs/memoryprefs.txt");
		
		settings.set_background_color(185, 73, 255);
		settings.set_secondary_color(156, 41, 228);
		settings.set_text_color(255, 255, 255);
		
		settings.set_initial_card_number(10);
		settings.set_origin(20, 120);
		
		settings.set_card_size(105, 133);
		
		settings.set_cards_draggable(false);
		settings.set_draw_empty_cards(true);
		settings.set_shade_inaccessible_cards(false);
		settings.set_bounce_captchalogued_items(false);
	}

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
		CaptchalogueCard card = deck.captchalogueItemAndReturnCard(item);
		card.setAccessible(false);
		deck.setIcons(getCardOrder());
		return true;
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

	private void startGame()
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
	public Object[] getCardOrder()
	{
		return deck.getCards().toArray();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals(Util.ACTION_USER_DOCK_CLICK))
		{
			startGame();
		}
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
			int height = settings.get_card_height()*(deck.getCards().size()/3 + 1) + 20;
			cardpanel.setBounds(0, 0, 650, height);
			cardpanel.setPreferredSize(new Dimension(650, height));
			JScrollPane pane = new JScrollPane(cardpanel);
			pane.setBounds(0, 0, 670, 450);
			pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			
			ArrayList<MemoryCard> mcards = new ArrayList<MemoryCard>();
			for(CaptchalogueCard card : deck.getCards())
			{
				if (!card.isEmpty())
				{
					mcards.add(new MemoryCard(card));
					mcards.add(new MemoryCard(card));
				}
			}
			
			int x = 4;
			int y = 10;
			
			while(mcards.size()>0)
			{
				int index = (int)Math.floor(Math.random()*mcards.size());
				MemoryCard card = mcards.get(index);
				card.setBounds(x, y, settings.get_card_width(), settings.get_card_height());
				cardpanel.add(card);
				mcards.remove(index);
				
				x += settings.get_card_width() + 2;
				if(x>650-settings.get_card_width())
				{
					y += settings.get_card_height() + 2;
					x = 4;
				}
			}
			this.add(pane);
		}
		
		private class MemoryCard extends JLabel implements MouseListener, ActionListener
		{
			private CaptchalogueCard card;
			private boolean uncovered;
			
			private JPanel panel;
			
			public MemoryCard(CaptchalogueCard card)
			{
				this.card = card;
				setIcon(Util.createImageIcon("modi/canon/memory/card_hidden.png"));
				if (card.getItem() != null)
				{
					panel = card.getItem().getPanel();
				}
				else
				{
					panel = new JPanel();
				}
				panel.setBounds(0, 0, settings.get_card_width(), settings.get_card_height());
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
					setIcon(Util.createImageIcon(settings.get_card_image()));
				}
				else
				{
					setIcon(Util.createImageIcon("modi/canon/memory/card_hidden.png"));
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
							open(card, OpenReason.MODUS_DEFAULT);
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
