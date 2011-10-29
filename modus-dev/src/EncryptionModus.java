import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import sylladex.*;
import sylladex.Animation.AnimationType;


public class EncryptionModus extends FetchModus
{
	private boolean enabled = true;
	private ArrayList<SylladexCard> cards;
	private JWindow window;
	private JLayeredPane pane;
	
	public EncryptionModus(Main m)
	{
		this.m = m;
		image_background_top = "modi/encryption/dockbg_top.png";
		image_background_bottom = "modi/encryption/dockbg.png";
		image_text = "modi/encryption/docktext.png";
		image_card = "modi/encryption/card.png";
		image_dock_card = "modi/global/dockcard.png";
		
		info_image = "modi/encryption/modus.png";
		info_name = "Encryption";
		info_author = "gumptiousCreator";
		
		item_file = "modi/items/queuestack.txt";
		prefs_file = "modi/prefs/encryptionprefs.txt";
		
		color_background = new Color(75, 75, 75);
		
		startcards = 12;
		origin = new Point(20,120);
		draw_default_dock_icons = true;
		
		draggable_cards = false;
		
		card_width = 94;
		card_height = 120;
		
		icons = new ArrayList<JLabel>();
	}
	
	@Override
	public void prepare()
	{
		cards = new ArrayList<SylladexCard>();
		
		window = new JWindow();
		window.setBounds(origin.x,origin.y,132,m.getScreenSize().height);
		window.setLayout(null);
		window.setAlwaysOnTop(true);
		Main.setTransparent(window);
		
		pane = new JLayeredPane();
		pane.setBounds(0,0,132,m.getScreenSize().height);
		pane.setLayout(null);
		
		window.add(pane);
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
		card.setItem(o);
		
		cards.add(card);
		JLabel icon = m.getIconLabelFromObject(o);
		icons.add(icon);
		m.setIcons(icons);
		card.setIcon(icon);
		
		animate(card);
	}
	
	@Override
	public void open(SylladexCard card)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void addCard()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void showSelectionWindow()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ArrayList<String> getItems()
	{
		return new ArrayList<String>();
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("card bounce"))
		{
			pane.removeAll();
			pane.revalidate();
			pane.repaint();
			
			JLabel cardbg = new JLabel(Main.createImageIcon(image_card));
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
	}
	
}