
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import sylladex.FetchModus;
import sylladex.FetchModusSettings;
import sylladex.Main;
import sylladex.CaptchalogueCard;
import sylladex.SylladexItem;


public class BoggleModus extends FetchModus
{
	private FetchModusSettings s;
	
	public BoggleModus(Main m)
	{
		this.deck = m;
		createModusSettings();
	}
	
	private void createModusSettings()
	{
		s = new FetchModusSettings();
		
		s.set_bottom_dock_image("modi/boggle/dockbg.png");
		s.set_top_dock_image("modi/boggle/dockbg_top.png");
		s.set_dock_text_image("modi/boggle/docktext.png");
		s.set_card_image("modi/boggle/card.png");
		s.set_card_back_image("modi/boggle/back.png");
		
		s.set_modus_image("modi/boggle/modus.png");
		s.set_name("Boggle");
		s.set_author("gumptiousCreator");
		
		s.set_item_file("modi/items/queuestack.txt");
		s.set_preferences_file("modi/prefs/boggleprefs.txt");
		
		s.set_background_color(255, 255, 0);
		
		s.set_initial_card_number(10);
		s.set_origin(20, 120);
		
		s.set_card_size(105, 133);
		
		s.set_cards_draggable(false);
		s.set_draw_empty_cards(true);
		s.set_shade_inaccessible_cards(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		// TODO Auto-generated method stub
		
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
		addLoadedItems();
	}
	
	private void addLoadedItems()
	{
		for (String s : items)
		{
			System.out.println(s);
			if(deck.getNextEmptyCard()==null) { deck.addCard(); }
			SylladexItem item = new SylladexItem(s, deck);
			CaptchalogueCard card = deck.getNextEmptyCard();
			JLabel icon = deck.getIconLabelFromItem(item);
			card.setIcon(icon);
			card.setItem(item);
			icons.add(icon);
			deck.setIcons(icons);
		}
	}
	
	@Override
	public void addGenericItem(Object o)
	{
		if(deck.getNextEmptyCard()==null){ return; }
		CaptchalogueCard card = deck.getNextEmptyCard();
		SylladexItem item = new SylladexItem("ITEM", o, deck);
		JLabel icon = deck.getIconLabelFromItem(item);
		card.setIcon(icon);
		icons.add(icon);
		card.setItem(item);
		
		deck.setIcons(icons);
	}
	
	@Override
	public void open(CaptchalogueCard card)
	{
		icons.remove(card.getIcon());
		deck.open(card);
		deck.setIcons(icons);
	}
	
	@Override
	public void addCard()
	{
		deck.addCard();
	}
	
	@Override
	public void showSelectionWindow()
	{
		Game game = new Game();
		game.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		game.setTitle("Boggle");
		game.setLayout(null);
		game.setSize(675,476);
		game.setResizable(false);
		game.setLocationRelativeTo(null);
		game.setVisible(true);
		
		boolean success = false;
		while (!success)
		{
			System.out.println("Setting letters...");
			game.newGame();
			success = game.setLetters();
		}
	}
	
	@Override
	public ArrayList<String> getItems()
	{
		items = new ArrayList<String>();
		for(CaptchalogueCard card : deck.getCards())
		{
			if(!card.isEmpty())
			{
				items.add(card.getItem().getSaveString());
			}
		}
		return items;
	}
	
	@SuppressWarnings("serial")
	private class Game extends JFrame
	{
		static final int cube_width = 70;
		
		private Cube[][] cubes;
		private int dimension;
		
		private String currentstring = "";
		private Cube lastselected;
		
		public Game()
		{
			JPanel gamepanel = new JPanel();
			gamepanel.setLayout(null);
			dimension = (int) Math.ceil(Math.sqrt(numberOfLetters()));
			System.out.println(dimension);
			gamepanel.setBounds(0, 0, dimension*cube_width, dimension*cube_width);
			gamepanel.setPreferredSize(new Dimension(dimension*cube_width, dimension*cube_width));
			gamepanel.setBackground(new Color(38, 222, 255));
			JScrollPane pane = new JScrollPane(gamepanel);
			pane.setBounds(0, 0, 670, 450);
			pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			
			cubes = new Cube[dimension][dimension];
			
			for (int i=0; i<dimension; i++)
			{
				for (int j=0; j<dimension; j++)
				{
					cubes[i][j] = new Cube();
					cubes[i][j].setBounds(i*cube_width, j*cube_width, cube_width, cube_width);
					cubes[i][j].x = i;
					cubes[i][j].y = j;
					gamepanel.add(cubes[i][j]);
				}
			}
			
			this.add(pane);
		}
		
		private boolean setLetters()
		{
			for (CaptchalogueCard card : deck.getCards())
			{
				if (!card.isEmpty())
				{
					String target = card.getItem().getName().replaceAll("[^A-Za-z]", "").toLowerCase();
					System.out.println("Seeding " + target);
					Point currentpoint = getRandomPosition();
					for (char letter : target.toCharArray())
					{
						String l = new String("" + letter);
						currentpoint = getNextPosition(currentpoint, l);
						if (currentpoint == null) { return false; }
						
						Cube cube = cubes[currentpoint.x][currentpoint.y];
						cube.setLetter(l);
						cube.set = true;
						cube.usedinthisword = true;
					}
					newWord();
				}
			}
			return true;
		}
		
		private Point getRandomPosition()
		{
			while (true)
			{
				int x = (int) Math.floor(Math.random()*dimension);
				int y = (int) Math.floor(Math.random()*dimension);
				
				if (!cubes[x][y].set) { return new Point(x,y); }
			}
		}
		
		private Point getNextPosition(Point point, String l)
		{
			Point result = null;
			int counter = 0;
			while (counter < 50)
			{
				int dx = (int) Math.floor(Math.random()*3)-1;
				int dy = (int) Math.floor(Math.random()*3)-1;
				
				if (point.x + dx > dimension-1 || point.x + dx < 0)
				{ dx = 0; }
				if (point.y + dy > dimension-1 || point.y + dy < 0)
				{ dy = 0; }
				
				Cube cube = cubes[point.x + dx][point.y + dy];
				if (!cube.set || (!cube.usedinthisword && cube.getLetter().equals(l)))
				{ result = new Point(point.x + dx, point.y + dy); return result; }
				
				counter++;
			}
			return null;
		}
		
		private void newWord()
		{
			for (Cube[] cubes1 : cubes)
			{
				for (Cube cube : cubes1)
				{
					cube.usedinthisword = false;
				}
			}
		}
		
		public void newGame()
		{
			for (Cube[] cubes1 : cubes)
			{
				for (Cube cube : cubes1)
				{
					cube.usedinthisword = false;
					cube.set = false;
				}
			}
		}
		
		private void clearSelection()
		{
			for (Cube[] cubes1 : cubes)
			{
				for (Cube cube : cubes1)
				{
					cube.setSelected(false);
				}
			}
			currentstring = "";
			lastselected = null;
		}
		
		private void addLetter(String letter)
		{
			currentstring = currentstring + letter;
			for (CaptchalogueCard card : deck.getCards())
			{
				if (!card.isEmpty())
				{
					if (card.getItem().getName().replaceAll("[^A-Za-z]", "").toLowerCase().equals(currentstring))
					{
						deck.openWithoutRemoval(card);
						clearSelection();
					}
				}
			}
		}
		
		public boolean isConnected(Cube cube)
		{
			if (lastselected == null)
			{ return true; }
			
			if (cube.x > lastselected.x-2 && cube.x < lastselected.x+2
					&& cube.y > lastselected.y-2 && cube.y < lastselected.y+2)
			{
				return true;
			}
			return false;
		}
		
		private int numberOfLetters()
		{
			int num = 0;
			for (CaptchalogueCard card : deck.getCards())
			{
				if (!card.isEmpty())
				{
					num += card.getItem().getName().replaceAll("[^A-Za-z]", "").length();
				}
			}
			return num;
		}
		
		private class Cube extends JLabel implements MouseListener
		{
			public boolean set = false;
			public boolean usedinthisword = false;
			private String letter;
			private JLabel text;
			
			public int x;
			public int y;
			
			public Cube()
			{
				setIcon(Main.createImageIcon("modi/boggle/cube.png"));
				setOpaque(false);
				text = new JLabel();
				text.setBounds(0, 0, cube_width, cube_width);
				text.setHorizontalAlignment(JLabel.CENTER);
				add(text);
				setLetter("a");
				addMouseListener(this);
			}
			
			public void setLetter(String letter)
			{
				this.letter = letter;
				text.setText("<html><font face='Courier New' size=12 color=#26deff>" + letter + "</font></html>");
			}
			
			public void setSelected(boolean s)
			{
				if (s)
				{
					text.setText("<html><font face='Courier New' size=12 color=#ffde26>" + letter + "</font></html>");
				}
				else { setLetter(letter); }
			}
			
			public String getLetter()
			{
				return letter;
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					if (isConnected(this))
					{
						addLetter(letter);
						setSelected(true);
						lastselected = this;
					}
				}
				else
				{
					clearSelection();
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
		}
	}
}
