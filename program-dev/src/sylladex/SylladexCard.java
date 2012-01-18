package sylladex;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.event.MouseInputListener;
import sylladex.Main.DragListener;

public class SylladexCard implements MouseInputListener, ActionListener
{
	//Referring to me
	private int id;
	private Main deck;
	private SylladexItem item;
	
	private JLabel icon; //dock icon
	
	private JPopupMenu popup;
	
	private boolean accessible = true; //Whether or not the card is click-able
	
	//Referring to my avatar
	private Point position = new Point(0,0);
	private JLayeredPane pane;	//mine
	private JPanel panel; //The card itself
	private JPanel itempanel = new JPanel(); //Whatever the SylladexItem decides to show
	private JPanel foreground; //Whatever the modus wants to show on top of that
	
	//Alchemy
	private JWindow reverse;
	private JLabel flip;
	
	private JLabel inaccessible;
	
	public SylladexCard(int id, Main deck)
	{
		this.id = id;
		this.deck = deck;
		populateAvatar();
		if(deck.getModus().draw_empty_cards)
		{
			addToCardHolder();
		}
	}
	//Class functions
	private void populateAvatar()
	{
		panel = new JPanel();
		panel.setLayout(null);
		panel.setOpaque(false);
		pane = new JLayeredPane();
		pane.setLayout(null);
		ImageIcon cardbgimage = Main.createImageIcon(deck.getModus().getCardBgUrl());
		JLabel cardbg = new JLabel(cardbgimage);
		cardbg.setBounds(0,0,getWidth(),getHeight());
		flip = new JLabel(Main.createImageIcon(deck.getModus().getFlipButtonBgUrl()));
		flip.setBounds(getWidth()-10,getHeight()-10,5,5);
		flip.setVisible(false);
		flip.addMouseListener(this);
		
		inaccessible = new JLabel(Main.createImageIcon("modi/global/inaccessible.png"));
		inaccessible.setBounds(0,0,getWidth(),getHeight());
		
		foreground = new JPanel();
		foreground.setBounds(0,0,getWidth(),getHeight());
		foreground.setOpaque(false);
		foreground.setLayout(null);
		
		if(deck.getModus().areCardsDraggable())
		{
			DragListener mouse = new DragListener(deck.getCardHolder());
			cardbg.addMouseListener(mouse);
			cardbg.addMouseMotionListener(mouse);
		}
		
		cardbg.addMouseListener(this);
		cardbg.addMouseMotionListener(this);
		
		pane.setLayer(cardbg, 0);
		pane.add(cardbg);
		
		pane.setLayer(flip, 413);
		pane.add(flip);
		
		pane.setLayer(inaccessible, 100);
		
		pane.setLayer(foreground, 50);
		pane.add(foreground);
		
		pane.setBounds(0,0,getWidth(), getHeight());
		panel.add(pane);
	}
	//Utility functions
	public int getId()
	{
		return id;
	}
	
	public SylladexItem getItem()
	{
		return item;
	}
	
	public Object getItemContents()
	{
		return item.getContents();
	}
	
	public String getCode()
	{
		return item.getCode();
	}
	
	public void setItem(SylladexItem item)
	{
		if(item!=null)
		{
			this.item = item;
			itempanel = item.getPanel();
			itempanel.setBounds(0,0,getWidth(),getHeight());
			pane.setLayer(itempanel, 1);
			
			pane.add(itempanel);
			if(!deck.getModus().draw_empty_cards) { addToCardHolder(); }
			
			if(item.getContents() instanceof Widget)
			{
				((Widget)item.getContents()).setCard(this);
			}
		}
		else
		{
			pane.remove(itempanel);
			if(!deck.getModus().draw_empty_cards) { removeFromCardHolder(); }
		}
		deck.refreshDockIcons();
	}
	
	public void addToCardHolder()
	{
		deck.getCardHolder().getLayeredPane().add(panel);
	}
	
	public JLabel getIcon()
	{
		return icon;
	}
	
	public void setIcon(JLabel newicon)
	{
		icon = newicon;
		if(icon != null)
		{
			icon.addMouseListener(this);
		}
	}
	
	public void setLayer(int layer)
	{
		deck.getCardHolder().getLayeredPane().setLayer(panel, layer);
	}
	
	public void removeFromCardHolder()
	{
		deck.getCardHolder().getLayeredPane().remove(panel);
		deck.refreshCardHolder();
	}
	
	public Point getPosition()
	{
		return position;
	}
	
	public void setPosition(Point position)
	{
		this.position = position;
		panel.setBounds(position.x, position.y, getWidth(), getHeight());
		deck.refreshCardHolder();
	}
	
	protected JPanel getPanel()
	{
		return panel;
	}
	
	public JPanel getForeground()
	{
		return foreground;
	}
	
	public int getWidth()
	{
		return deck.getModus().getCardWidth();
	}
	
	public int getHeight()
	{
		return deck.getModus().getCardHeight();
	}
	
	public void setAccessible(boolean accessible)
	{
		this.accessible = accessible;
		if(deck.getModus().shade_inaccessible_cards)
		{
			if(!accessible && pane.getComponentCountInLayer(100)==0)
			{ pane.add(inaccessible); }
			if(accessible && pane.getComponentCountInLayer(100)==1)
			{ pane.remove(inaccessible); }
		}
	}
	
	public boolean isAccessible()
	{
		return accessible;
	}
	
	public boolean isEmpty()
	{
		return item==null;
	}
	
	public void flip()
	{
		if(reverse!=null){ return; }
		reverse = new JWindow();
		reverse.setSize(148,188);
		reverse.setLocationRelativeTo(null);
		Main.setTransparent(reverse);
		JLayeredPane pane = new JLayeredPane();
		pane.setBounds(0,0,148,188);
		pane.setLayout(null);
		
		JLabel background = new JLabel(Main.createImageIcon(deck.getModus().getCardBackBgUrl()));
		background.setBounds(0,0,148,188);
		pane.setLayer(background, 0);
		pane.add(background);
		
		JLabel distaction = new JLabel(Main.createImageIcon("modi/global/captcha.gif"));
		distaction.setBounds(14,14,124,161);
		pane.setLayer(distaction, 1);
		pane.add(distaction);
		
		JLabel code = new JLabel("<html><font face=Courier size=5 color=ccccff>" + getCode() + "</font></html>");
		code.setHorizontalAlignment(JLabel.CENTER);
		code.setBounds(0,0,148,188);
		pane.setLayer(code, 2);
		pane.add(code);
		
		reverse.addMouseListener(this);
		DragListener rl = new DragListener(reverse);
		reverse.addMouseListener(rl);
		reverse.addMouseMotionListener(rl);
		reverse.add(pane);
		reverse.setVisible(true);
		reverse.setAlwaysOnTop(true);
	}
	
	private void createPopupMenu(MouseEvent e)
	{
		popup = new JPopupMenu();
		JMenuItem open = new JMenuItem("Open");
			open.addActionListener(this);
			open.setActionCommand("open");
		JMenuItem flip = new JMenuItem("Flip");
			flip.addActionListener(this);
			flip.setActionCommand("flip");
		
		popup.add(open);
		popup.add(flip);
		
		popup.show(icon, e.getX(), e.getY());
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("open"))
		{
			deck.openWithoutRemoval(this);
		}
		else if(e.getActionCommand().equals("flip"))
		{
			flip();
		}
	}
	
	// Mouse functions
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if(e.getSource().equals(flip))
		{
			flip();
		}
		else if(e.getSource().equals(reverse))
		{
			reverse.setVisible(false);
			reverse = null;
		}
		else if(e.getSource().equals(icon) && accessible)
		{
			if(e.getButton()==MouseEvent.BUTTON1)
			{ deck.getModus().open(this); }
			else
			{ createPopupMenu(e); }
		}
		else if(accessible)
		{
			if(!(item.getContents() instanceof Widget))
			{
				if(e.getButton()==MouseEvent.BUTTON1)
					deck.getModus().open(this);
				else
					deck.openWithoutRemoval(this);
			}
			else
			{
				((Widget)item.getContents()).mouseClicked(e);
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent e)
	{
		deck.getModus().actionPerformed(new ActionEvent(this, 612, "card mouse enter"));
		deck.getCardHolder().getMouseListeners()[0].mouseEntered(e);
		
		if(!isEmpty())
		{
			if((item.getContents() instanceof Widget))
			{
				((Widget)item.getContents()).mouseEntered(e);
			}
		}
		
		if(!isEmpty() && accessible){ flip.setVisible(true); }
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
		deck.getModus().actionPerformed(new ActionEvent(this, 613, "card mouse exit"));
		deck.getCardHolder().getMouseListeners()[0].mouseExited(e);
		
		if(!isEmpty())
		{
			if((item.getContents() instanceof Widget))
			{
				((Widget)item.getContents()).mouseExited(e);
			}
		}
		
		flip.setVisible(false);
	}
	@Override
	public void mousePressed(MouseEvent e)
	{
		if(!isEmpty())
		{
			if((item.getContents() instanceof Widget))
			{
				((Widget)item.getContents()).mousePressed(e);
			}
		}
	}
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if(!isEmpty())
		{
			if((item.getContents() instanceof Widget))
			{
				((Widget)item.getContents()).mouseReleased(e);
			}
		}
	}
	@Override
	public void mouseDragged(MouseEvent e){}
	@Override
	public void mouseMoved(MouseEvent e){}
}
