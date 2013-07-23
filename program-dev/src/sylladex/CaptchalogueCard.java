package sylladex;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.event.MouseInputListener;

import ui.MenuItem;
import ui.PopupMenu;
import util.AnimatedObject;
import util.Animation;
import util.Animation.AnimationType;
import util.DragListener;
import util.Util;
import util.Util.OpenReason;

@SuppressWarnings("serial")
public class CaptchalogueCard extends AnimatedObject implements MouseInputListener, ActionListener
{
	//Referring to me
	private Main deck;
	private SylladexItem item;
	
	private PopupMenu popup;
	
	private boolean accessible = true; //Whether or not the card is click-able
	
	//Referring to my avatar
	private JLayeredPane pane;	//mine
	private JPanel panel; //The card itself
	private JPanel itempanel = new JPanel(); //Whatever the SylladexItem decides to show
	private JPanel foreground; //Whatever the modus wants to show on top of that
	private JLabel number_label; //e.g. x1
	
	//Animation
	private ArrayList<Animation> animations = new ArrayList<Animation>();
	
	//Alchemy
	private JWindow reverse;
	
	private JLabel inaccessible;
	
	public CaptchalogueCard(Main deck)
	{
		this.deck = deck;
		populateAvatar();
	}
	//Class functions
	private void populateAvatar()
	{
		panel = new JPanel();
		panel.setLayout(null);
		panel.setOpaque(false);
		panel.setSize(getWidth(), getHeight());
		panel.setVisible(false);
		
		pane = new JLayeredPane();
		pane.setLayout(null);
		ImageIcon cardbgimage = Util.createImageIcon(deck.getModusSettings().get_card_image());
		JLabel cardbg = new JLabel(cardbgimage);
		cardbg.setBounds(0,0,getWidth(),getHeight());
		
		number_label = new JLabel();
		number_label.setBounds(15, getHeight() - 24, 20, 20);
		
		inaccessible = new JLabel(Util.createImageIcon("modi/global/inaccessible.png"));
		inaccessible.setBounds(0,0,getWidth(),getHeight());
		
		foreground = new JPanel();
		foreground.setBounds(0,0,getWidth(),getHeight());
		foreground.setOpaque(false);
		foreground.setLayout(null);
		
		if(deck.getModusSettings().are_cards_draggable())
		{
			DragListener mouse = new DragListener(deck.getCardHolder());
			cardbg.addMouseListener(mouse);
			cardbg.addMouseMotionListener(mouse);
		}
		
		cardbg.addMouseListener(this);
		cardbg.addMouseMotionListener(this);
		
		pane.setLayer(cardbg, 0);
		pane.add(cardbg);
		
		pane.setLayer(number_label, 1);
		//pane.add(number_label);
		
		pane.setLayer(inaccessible, 100);
		
		pane.setLayer(foreground, 50);
		pane.add(foreground);
		
		pane.setBounds(0,0,getWidth(), getHeight());
		panel.add(pane);
	}
	
	//Utility functions
	
	public SylladexItem getItem()
	{
		return item;
	}
	
	public Object getItemContents()
	{
		return item.getContents();
	}
	
	public JLabel getDockIcon()
	{
		return item.getIcon();
	}
	
	public String getCode()
	{
		return item.getCode();
	}
	
	public void setItem(SylladexItem item)
	{
		this.item = item;
		if(item!=null)
		{
			item.getIcon().removeMouseListener(this);
			item.getIcon().addMouseListener(this);
			itempanel = item.getPanel();
			itempanel.setBounds(0,0,getWidth(),getHeight());
			pane.setLayer(itempanel, 1);
			
			pane.add(itempanel);
			
			if(item.getContents() instanceof Widget)
			{
				((Widget)item.getContents()).setCard(this);
			}
			
			number_label.setText("x" + item.getNumber());
		}
		else
		{
			pane.remove(itempanel);
			number_label.setText("");
			pane.revalidate();
		}
		deck.refreshDockIcons();
	}
	
	public void refresh()
	{
		SylladexItem i = item;
		setItem(null);
		setItem(i);
	}
	
	public void setLayer(int layer)
	{
		deck.getCardHolder().getLayeredPane().setLayer(panel, layer);
	}
	
	@Override
	public Point getLocation()
	{
		return deck.getCardDisplayManager().getRelativePosition(getAbsoluteLocation());
	}
	
	public Point getFinalLocation()
	{
		if (animations.size() == 0)
		{
			return deck.getCardDisplayManager().getRelativePosition(getAbsoluteLocation());
		}
		
		return deck.getCardDisplayManager().getRelativePosition(animations.get(animations.size()-1).getFinalPosition());
	}
	
	@Override
	public void setLocation(Point location)
	{
		Point absolute_location = deck.getCardDisplayManager().getAbsolutePosition(location);
		setAbsoluteLocation(absolute_location);
		deck.getCardDisplayManager().adjustSize();
	}
	
	@Override
	public void setLocation(int x, int y)
	{
		setLocation(new Point(x, y));
	}
	
	@Override
	public int getX()
	{
		return getLocation().x;
	}
	
	@Override
	public int getY()
	{
		return getLocation().y;
	}
	
	protected Point getAbsoluteLocation()
	{
		return panel.getLocation();
	}
	
	protected void setAbsoluteLocation(Point location)
	{
		panel.setLocation(location);
	}
	
	public void setVisible(boolean visible)
	{
		panel.setVisible(visible);
	}
	
	public void moveTo(Point position, AnimationType type)
	{
		Animation a = new Animation(this, position, type, this, Util.ACTION_ANIMATION_COMPLETE);
		animations.add(a);
		if(animations.size() == 1) a.run();
	}
	
	public void addAnimation(Animation a)
	{
		a.setListener(this);
		animations.add(a);
		if (animations.size() == 1) a.run();
	}
	
	public void stopAnimation()
	{
		if (animations.size() == 0) { return; }
		Animation a = animations.get(0);
		animations.clear();
		a.stop();
	}
	
	protected JPanel getPanel()
	{
		return panel;
	}
	
	public JPanel getForegroundPanel()
	{
		return foreground;
	}
	
	public int getWidth()
	{
		return deck.getModusSettings().get_card_width();
	}
	
	public int getHeight()
	{
		return deck.getModusSettings().get_card_height();
	}
	
	public void setAccessible(boolean accessible)
	{
		this.accessible = accessible;
		if(deck.getModusSettings().shade_inaccessible_cards())
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
		reverse.setBackground(Util.COLOR_TRANSPARENT);
		JLayeredPane pane = new JLayeredPane();
		pane.setBounds(0,0,148,188);
		pane.setLayout(null);
		
		JLabel background = new JLabel(Util.createImageIcon(deck.getModusSettings().get_card_back_image()));
		background.setBounds(0,0,148,188);
		pane.setLayer(background, 0);
		pane.add(background);
		
		JLabel distaction = new JLabel(Util.createImageIcon("modi/global/captcha.gif"));
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
	
	private void createPopupMenu(MouseEvent e, JComponent trigger)
	{
		if (item == null) { return; }
		popup = new PopupMenu(deck.getModusSettings());
		MenuItem open = new MenuItem("Open");
			open.setActionListener(this);
			open.setActionCommand("open");
		MenuItem open_keep = new MenuItem("Open and keep");
			open_keep.setActionListener(this);
			open_keep.setActionCommand("open_keep");
		MenuItem open_eject = new MenuItem("Open and eject");
			open_eject.setActionListener(this);
			open_eject.setActionCommand("open_eject");
		MenuItem flip = new MenuItem("Flip");
			flip.setActionListener(this);
			flip.setActionCommand("flip");
		
		popup.add(open);
		popup.add(open_keep);
		popup.add(open_eject);
		popup.add(flip);
		if (item.getContents() instanceof Widget)
		{
			popup.add(((Widget) item.getContents()).getExtraMenuItems());
		}
		popup.add(new MenuItem("Cancel"));
		
		popup.show(trigger);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() instanceof Animation)
		{
			animations.remove(0);
			if (animations.size() > 0)
			{
				animations.get(0).run();
			}
			deck.getModus().actionPerformed(e);
		}
		else if(e.getActionCommand().equals("open"))
		{
			deck.getModus().open(this, OpenReason.USER_DEFAULT);
		}
		else if(e.getActionCommand().equals("open_keep"))
		{
			deck.open(this, OpenReason.USER_KEEP);
		}
		else if(e.getActionCommand().equals("open_eject"))
		{
			SylladexItem item = this.item;
			deck.getModus().open(this, OpenReason.USER_EJECT);
			deck.eject(item);
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
		if(e.getSource().equals(reverse))
		{
			if (e.getButton() != MouseEvent.BUTTON1)
			{
				reverse.setVisible(false);
				reverse = null;
			}
		}
		else if(accessible)
		{
			int action = 0;
			if (!e.isAltDown()
					&& !e.isAltGraphDown()
					&& !e.isControlDown()
					&& !e.isShiftDown())
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{ action = deck.getPreferences().leftClickAction(); }
				else
				{ action = deck.getPreferences().rightClickAction(); }
			}
			else if (e.getButton() == MouseEvent.BUTTON1)
			{ action = deck.getPreferences().leftModClickAction(); }
			else
			{ action = deck.getPreferences().rightModClickAction(); }
			
			switch (action)
			{
				case DeckPreferences.OPEN:
				{
					deck.getModus().open(this, OpenReason.USER_DEFAULT);
					break;
				}
				case DeckPreferences.OPEN_AND_KEEP:
				{
					deck.open(this, OpenReason.USER_KEEP);
					break;
				}
				case DeckPreferences.OPEN_AND_EJECT:
				{
					SylladexItem item = this.item;
					deck.getModus().open(this, OpenReason.USER_EJECT);
					deck.eject(item);
					break;
				}
				case DeckPreferences.FLIP:
				{
					flip();
					break;
				}
				case DeckPreferences.POPUP_MENU:
				{
					createPopupMenu(e, (JComponent)e.getSource());
				}
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent e)
	{
		deck.getModus().actionPerformed(new ActionEvent(this, 612, Util.ACTION_CARD_MOUSE_ENTER));
		deck.getCardHolder().getMouseListeners()[0].mouseEntered(e);
		
		if(!isEmpty())
		{
			if((item.getContents() instanceof Widget))
			{
				((Widget)item.getContents()).mouseEntered(e);
			}
		}
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
		deck.getModus().actionPerformed(new ActionEvent(this, 613, Util.ACTION_CARD_MOUSE_EXIT));
		deck.getCardHolder().getMouseListeners()[0].mouseExited(e);
		
		if(!isEmpty())
		{
			if((item.getContents() instanceof Widget))
			{
				((Widget)item.getContents()).mouseExited(e);
			}
		}
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
