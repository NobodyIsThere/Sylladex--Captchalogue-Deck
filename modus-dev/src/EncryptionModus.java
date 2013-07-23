import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JWindow;
import javax.swing.Timer;

import sylladex.CaptchalogueCard;
import sylladex.FetchModus;
import sylladex.Main;
import sylladex.SylladexItem;
import util.Animation;
import util.Animation.AnimationType;
import util.DragListener;
import util.Util;
import util.Util.OpenReason;

public class EncryptionModus extends FetchModus implements KeyListener, MouseListener
{	
	private boolean enabled = true;
	private boolean openenabled = true;
	private JWindow window = new JWindow();
	private JLayeredPane pane = new JLayeredPane();
	
	private JWindow hacking;
	private Timer timer = new Timer(1500, this);
	private JLabel pbar;
	private int progress = 0;
	private CaptchalogueCard hackcard;
	private OpenReason current_reason;
	
	public EncryptionModus(Main m)
	{
		super(m);
	}
	
	@Override
	public void initialSettings()
	{
		settings.set_dock_text_image("modi/canon/encryption/docktext.png");
		settings.set_card_image("modi/canon/encryption/card.png");
		settings.set_card_back_image("modi/canon/encryption/back.png");
		
		settings.set_modus_image("modi/canon/encryption/modus.png");
		settings.set_name("Encryption");
		settings.set_author("gumptiousCreator");
		
		settings.set_preferences_file("modi/prefs/encryptionprefs.txt");
		
		settings.set_background_color(0, 0, 0);
		settings.set_secondary_color(85, 85, 85);
		settings.set_text_color(255, 255, 255);
		
		settings.set_initial_card_number(12);
		settings.set_origin(20, 120);
		
		settings.set_cards_draggable(false);
		
		settings.set_card_size(94, 120);
	}
	
	@Override
	public void prepare()
	{
		window.setBounds(settings.get_origin().x, settings.get_origin().y,
						132,Util.SCREEN_SIZE.height);
		window.setLayout(null);
		window.setAlwaysOnTop(true);
		window.setBackground(Util.COLOR_TRANSPARENT);
		
		pane.setBounds(0, 0, 132, Util.SCREEN_SIZE.height);
		pane.setLayout(null);
		
		window.add(pane);
	}
	
	@Override
	public void ready()
	{
		deck.setIcons(deck.getCards().toArray());
	}
	
	private void animate(CaptchalogueCard card)
	{
		card.setLocation(new Point(5,5));
		deck.bounceCard(card);
		deck.addAnimation(new Animation(AnimationType.WAIT, 500, this, "encryption_card_bounce"));
	}
	
	@Override
	public boolean captchalogue(SylladexItem item)
	{
		if (deck.isFull()) { return false; }
		
		if (loading)
		{
			deck.captchalogueItem(item);
			return true;
		}
		
		if (enabled==false){ return false; }
		
		enabled = false;
		CaptchalogueCard card = deck.captchalogueItemAndReturnCard(item);
		animate(card);
		return true;
	}
	
	@Override
	public void open(CaptchalogueCard card, OpenReason reason)
	{
		if(openenabled==false){ return; }
		openenabled = false;
		
		progress = 0;

		hacking = new JWindow();
		hacking.setBounds(0, 0, 296, 376);
		hacking.setLocationRelativeTo(null);
		hacking.setLayout(null);
		hacking.setBackground(Util.COLOR_TRANSPARENT);
		
		JLayeredPane panel = new JLayeredPane();
		panel.setBounds(0,0,296,376);
		panel.setLayout(null);
		JLabel animation = new JLabel(Util.createImageIcon("modi/canon/encryption/hacking.gif"));
		DragListener l = new DragListener(hacking);
		animation.addMouseListener(l);
		animation.addMouseListener(this);
		animation.addMouseMotionListener(l);
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
		current_reason = reason;
		timer = new Timer(100, this);
		timer.setActionCommand("encryption_progress");
		timer.restart();
		
		panel.setFocusable(true);
		panel.requestFocusInWindow();
		panel.addKeyListener(this);
		panel.setFocusTraversalKeysEnabled(false);
	}
	
	private void actuallyOpen(CaptchalogueCard card, OpenReason reason)
	{
		deck.open(card, reason);
		deck.setIcons(deck.getCards().toArray());
	}
	
	@Override
	public void addCard()
	{
		deck.addCard();
	}
	
	@Override
	public Object[] getCardOrder()
	{
		return deck.getCards().toArray();
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("encryption_card_bounce"))
		{
			pane.removeAll();
			pane.revalidate();
			pane.repaint();
			
			JLabel cardbg = new JLabel(Util.createImageIcon(settings.get_card_image()));
			cardbg.setBounds(0,0,94,120);
			pane.setLayer(cardbg, 0);
			pane.add(cardbg);
			
			JLabel animation = new JLabel(Util.createImageIcon("modi/canon/encryption/animation.gif"));
			animation.setBounds(8,11,68,95);
			pane.setLayer(animation, 1);
			pane.add(animation);
			
			window.setVisible(true);
			deck.setCardHolderSize(0, 0);
			
			new Animation(AnimationType.WAIT, 500, this, "encryption_card_wait").run();
		}
		else if (e.getActionCommand().equals("encryption_card_wait"))
		{
			pane.removeAll();
			pane.revalidate();
			pane.repaint();
			
			JLabel vault = new JLabel(Util.createImageIcon("modi/canon/encryption/vault.gif"));
			vault.setBounds(0,0,132,116);
			pane.setLayer(vault, 0);
			pane.add(vault);
			
			int height = Util.SCREEN_SIZE.height;
			deck.addAnimation(new Animation(vault, new Point(0, height/4), AnimationType.MOVE, null, null));
			deck.addAnimation(new Animation(vault, new Point(0, 3*height/4), AnimationType.MOVE, null, null));
			deck.addAnimation(new Animation(vault, new Point(0, height), AnimationType.MOVE, this, "encryption_vault_down"));
		}
		else if (e.getActionCommand().equals("encryption_vault_down"))
		{
			enabled = true;
		}
		else if (e.getActionCommand().equals("encryption_progress"))
		{
			progress-=Math.random()*4;
			if(progress<0){ progress = 0; }
			if(progress>100){ progress = 100; }
			int x = 230*progress/100;
			pbar.setBounds(276-x,262,x,10);
			pbar.repaint();
			
			if(progress==100)
			{
				actuallyOpen(hackcard, current_reason);
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

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON2)
		{
			openenabled = true;
			hacking.setVisible(false);
			progress = 0;
			timer.stop();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
	
}