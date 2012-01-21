import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.*;
import sylladex.*;
import sylladex.Animation.AnimationType;

public class EncryptionModus extends FetchModus implements KeyListener
{
	private FetchModusSettings s;
	
	private boolean enabled = true;
	private boolean openenabled = true;
	private ArrayList<SylladexCard> cards;
	private JWindow window;
	private JLayeredPane pane;
	
	private JFrame hacking = new JFrame();
	private Timer timer = new Timer(1500, this);
	private JLabel pbar;
	private int progress;
	private SylladexCard hackcard;
	
	public EncryptionModus(Main m)
	{
		this.m = m;
		createModusSettings();
		icons = new ArrayList<JLabel>();
	}
	
	private void createModusSettings()
	{
		s = new FetchModusSettings();
		
		s.set_bottom_dock_image("modi/encryption/dockbg.png");
		s.set_top_dock_image("modi/encryption/dockbg_top.png");
		s.set_dock_text_image("modi/encryption/docktext.png");
		s.set_card_image("modi/encryption/card.png");
		s.set_card_back_image("modi/encryption/back.png");
		
		s.set_modus_image("modi/encryption/modus.png");
		s.set_name("Encryption");
		s.set_author("gumptiousCreator");
		
		s.set_item_file("modi/items/queuestack.txt");
		s.set_preferences_file("modi/prefs/encryptionprefs.txt");
		
		s.set_background_color(75, 75, 75);
		
		s.set_initial_card_number(12);
		s.set_origin(20, 120);
		
		s.set_cards_draggable(false);
		
		s.set_card_size(94, 120);
	}
	
	public FetchModusSettings getModusSettings()
	{
		return s;
	}
	
	@Override
	public void prepare()
	{
		cards = new ArrayList<SylladexCard>();
		
		window = new JWindow();
		window.setBounds(getModusSettings().get_origin().x,getModusSettings().get_origin().y,132,m.getScreenSize().height);
		window.setLayout(null);
		window.setAlwaysOnTop(true);
		Main.setTransparent(window);
		
		pane = new JLayeredPane();
		pane.setBounds(0,0,132,m.getScreenSize().height);
		pane.setLayout(null);
		
		window.add(pane);
		
		loadItems();
	}
	
	private void loadItems()
	{
		for(String string : items)
		{
			if(!string.equals(""))
			{
				if(m.getNextEmptyCard()==null) { m.addCard(); }
				SylladexCard card = m.getNextEmptyCard();
				SylladexItem item = new SylladexItem(string, m);
				card.setItem(item);
				cards.add(card);
				JLabel icon = m.getIconLabelFromItem(item);
				icons.add(icon);
				m.setIcons(icons);
				card.setIcon(icon);
			}
		}
	}
	
	private void animate(SylladexCard card)
	{
		card.setPosition(new Point(5,5));
		card.setLayer(cards.indexOf(card));
		m.setCardHolderSize(132, 120);
		Animation a2 = new Animation(AnimationType.WAIT, 500, this, "card bounce");
		new Animation(card, new Point(0,0), AnimationType.MOVE, a2, "run").run();
	}
	
	@Override
	public void addGenericItem(Object o)
	{
		if(enabled==false){ return; }
		enabled = false;
		if(m.getNextEmptyCard()==null){ return; }
		SylladexCard card = m.getNextEmptyCard();
		SylladexItem item = new SylladexItem("ITEM", o, m);
		card.setItem(item);
		
		cards.add(card);
		JLabel icon = m.getIconLabelFromItem(item);
		icons.add(icon);
		m.setIcons(icons);
		card.setIcon(icon);
		
		animate(card);
	}
	
	@Override
	public void open(SylladexCard card)
	{
		if(openenabled==false){ return; }
		openenabled = false;
		
		progress = 0;

		hacking = new JFrame();
		hacking.setBounds(0, 0, 296, 376);
		hacking.setLocationRelativeTo(null);
		hacking.setLayout(null);
		//Main.setTransparent(hacking);
		
		JLayeredPane panel = new JLayeredPane();
		panel.setBounds(0,0,296,376);
		panel.setLayout(null);
		JLabel animation = new JLabel(Main.createImageIcon("modi/encryption/hacking.gif"));
		animation.setBounds(0,0,296,376);
		panel.setLayer(animation, 0);
		panel.add(animation);
		
		pbar = new JLabel();
		pbar.setBounds(275,262,1,10);
		pbar.setBackground(new Color(69,242,0));
		pbar.setOpaque(true);
		panel.setLayer(pbar, 1);
		panel.add(pbar);
		
		hacking.add(panel);
		hacking.setVisible(true);
		
		hackcard = card;
		timer = new Timer(100, this);
		timer.setActionCommand("progress");
		timer.restart();
		
		animation.setFocusable(true);
		animation.requestFocusInWindow();
		animation.addKeyListener(this);
		animation.setFocusTraversalKeysEnabled(false);
	}
	
	private void actuallyOpen(SylladexCard card)
	{
		icons.remove(card.getIcon());
		icons.trimToSize();
		m.setIcons(icons);
		cards.remove(card);
		m.open(card);
	}
	
	@Override
	public void addCard()
	{
		m.addCard();
	}
	
	@Override
	public void showSelectionWindow(){}
	
	@Override
	public ArrayList<String> getItems()
	{
		hacking.setVisible(false);
		hacking.removeAll();
		timer.stop();
		
		ArrayList<String> items = new ArrayList<String>();
		if(cards.size()>0)
		{
			for(SylladexCard card : cards)
			{
				items.add(card.getItem().getSaveString());
			}
		}
		else { items.add(""); }
		return items;
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("card bounce"))
		{
			pane.removeAll();
			pane.revalidate();
			pane.repaint();
			
			JLabel cardbg = new JLabel(Main.createImageIcon(getModusSettings().get_card_image()));
			cardbg.setBounds(0,0,94,120);
			pane.setLayer(cardbg, 0);
			pane.add(cardbg);
			
			JLabel animation = new JLabel(Main.createImageIcon("modi/encryption/animation.gif"));
			animation.setBounds(8,11,68,95);
			pane.setLayer(animation, 1);
			pane.add(animation);
			
			window.setVisible(true);
			m.setCardHolderSize(0, 0);
			
			new Animation(AnimationType.WAIT, 500, this, "card wait").run();
		}
		else if(e.getActionCommand().equals("card wait"))
		{
			pane.removeAll();
			pane.revalidate();
			pane.repaint();
			
			JLabel vault = new JLabel(Main.createImageIcon("modi/encryption/vault.gif"));
			vault.setBounds(0,0,132,116);
			pane.setLayer(vault, 0);
			pane.add(vault);
			
			int height = m.getScreenSize().height;
			Animation a3 = new Animation(vault, new Point(0,height), AnimationType.MOVE, this, "vault down");
			Animation a2 = new Animation(vault, new Point(0,3*height/4), AnimationType.MOVE, a3, "run");
			new Animation(vault, new Point(0, height/4), AnimationType.MOVE, a2, "run").run();
		}
		else if(e.getActionCommand().equals("vault down"))
		{
			enabled = true;
		}
		else if(e.getActionCommand().equals("progress"))
		{
			progress-=Math.random()*4;
			if(progress<0){ progress = 0; }
			if(progress>100){ progress = 100; }
			int x = 230*progress/100;
			pbar.setBounds(276-x,262,x,10);
			pbar.repaint();
			
			if(progress==100)
			{
				actuallyOpen(hackcard);
				openenabled=true;
				hacking.setVisible(false);
				progress = 0;
				timer.stop();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e){}

	@Override
	public void keyReleased(KeyEvent e)
	{
		progress+=Math.random()*5;
	}

	@Override
	public void keyTyped(KeyEvent e){}
	
}