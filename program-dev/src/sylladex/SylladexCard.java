package sylladex;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

public class SylladexCard implements MouseInputListener, ActionListener
{
	//Referring to me
	private int id;
	private Main deck;
	private File file = null;
	private String string = null;
	private String imagestring = "IMAGE";
	private Image image = null;
	private Widget widget = null;
	private JLabel icon; //dock icon
	
	private JPopupMenu popup;
	
	private boolean accessible = true; //Whether or not the card is click-able
	
	//Referring to my avatar
	private Point position = new Point(0,0);
	private JLayeredPane pane;	//mine
	private JPanel panel; //The card itself
	private JLabel cardicon; //mine - shows images and icons
	private JTextArea cardtext; //mine - shows strings
	private JPanel widgetpanel; //controlled by the widget.
	private JPanel foreground;
	
	//Alchemy
	private JWindow reverse;
	private JLabel flip;
	private JPanel holes;
	
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
		
		holes = new JPanel();
		holes.setBounds(0,0,getWidth(),getHeight());
		holes.setOpaque(false);
		holes.setLayout(null);
		
		cardbg.addMouseListener(this);
		cardbg.addMouseMotionListener(this);
		
		DragListener mouse = new DragListener();
		cardbg.addMouseListener(mouse);
		cardbg.addMouseMotionListener(mouse);
		
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
	
	public File getFile()
	{
		return file;
	}
	
	public String getString()
	{
		return string;
	}
	
	public String getCode()
	{
		return Alchemy.generateCode(getItemString());
	}
	
	public Image getImage()
	{
		return image;
	}
	
	public Widget getWidget()
	{
		return widget;
	}
	
	public String getSaveString()
	{
		if(file!=null)
		{
			String path;
			if(!deck.getPreferences().usb_mode())
				path = file.getPath();
			else
				path = "files" + System.getProperty("file.separator") + file.getName();
			return Main.FILE_PREFIX + path;
		}
		else if(string!=null)
		{
			return Main.STRING_PREFIX + string.replaceAll(System.getProperty("line.separator"), "SYLLADEX_NL");
		}
		else if(image!=null)
		{
			if(image instanceof BufferedImage)
			{
				File f = null;
				while(f == null ? true : f.exists()) //don't want to overwrite a previous image
					f = new File("files" + System.getProperty("file.separator") + "captchalogued_image_" + new Double(Math.random()).toString().replaceAll("\\.", "") + ".png");
				BufferedImage b = (BufferedImage)image;
				try
				{
					ImageIO.write(b, "png", f);
				}
				catch (IOException e){ e.printStackTrace(); }
				return Main.IMAGE_PREFIX + f.getPath().substring(f.getPath().indexOf("files" + System.getProperty("file.separator") + "captchalogued_image_"));
			}
		}
		else if(widget!=null)
		{
			return Main.WIDGET_PREFIX + "widgets/" + widget.getClass().getName() + ".class[/WIDGET]" + widget.getSaveString();
		}
		return null;
	}
	
	public String getItemString()
	{
		if(file!=null){ return file.getName(); }
		else if(image!=null){ return imagestring; }
		else if(string!=null){ return string; }
		else if(widget!=null){ return widget.getString(); }
		return null;
	}
	
	public void setImageString(String s)
	{
		imagestring = s;
	}
	
	public void setItem(Object o)
	{
		if(o instanceof File)
		{
			setFile((File)o);
		}
		else if(o instanceof Image)
		{
			setImage((Image)o);
		}
		else if(o instanceof String)
		{
			setString((String)o);
		}
		else if(o instanceof Widget)
		{
			setWidget((Widget)o);
		}
	}
	
	public void setFile(File file)
	{
		this.file = file;
		if(file!=null)
		{
			string = null;
			image = null;
			widget = null;
			
			Icon icon = Main.getIconFromFile(file);
			String filename = file.getName();
			cardicon = new JLabel(filename);
			cardicon.setBounds(15*getWidth()/148,35*getHeight()/94,24*getWidth()/37,82*getHeight()/188);
			cardicon.setIcon(icon);
			cardicon.setHorizontalAlignment(JLabel.CENTER);
			cardicon.setVerticalAlignment(JLabel.TOP);
			cardicon.setVerticalTextPosition(JLabel.BOTTOM);
			cardicon.setHorizontalTextPosition(JLabel.CENTER);
			
			cardicon.addMouseListener(this);
			pane.setLayer(cardicon, 1);
			pane.add(cardicon);
			
			if(!deck.getModus().draw_empty_cards)
				addToCardHolder();
		}
		else
		{
			if(cardicon!=null)
				pane.remove(cardicon);
			icon = null;
			if(!deck.getModus().draw_empty_cards)
				removeFromCardHolder();
		}
		deck.refreshDockIcons();
	}
	
	public void setImage(Image image)
	{
		this.image = image;
		if(image!=null)
		{
			file = null;
			string = null;
			widget = null;
			
			cardicon = new JLabel();
			cardicon.setBounds(15*getWidth()/148,60*getHeight()/188,24*getWidth()/37,100*getHeight()/188);
			Icon icon = Main.getSizedIcon(image, cardicon.getWidth(), cardicon.getHeight());
			cardicon.setIcon(icon);
			cardicon.setHorizontalAlignment(JLabel.CENTER);
			cardicon.setVerticalAlignment(JLabel.CENTER);
			
			cardicon.addMouseListener(this);
			pane.setLayer(cardicon, 1);
			pane.add(cardicon);
			
			if(!deck.getModus().draw_empty_cards)
				addToCardHolder();
		}
		else
		{
			pane.remove(cardicon);
			icon = null;
			if(!deck.getModus().draw_empty_cards)
				removeFromCardHolder();
		}
		deck.refreshDockIcons();
	}
	
	public void setString(String string)
	{
		this.string = string;
		if(string!=null)
		{
			file = null;
			image = null;
			widget = null;
			
			cardtext = new JTextArea(string);
			cardtext.setBounds(15*getWidth()/148,60*getHeight()/188,24*getWidth()/37,100*getHeight()/188);
			cardtext.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
			cardtext.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
			cardtext.setEditable(false);
			cardtext.setLineWrap(true);
			cardtext.setWrapStyleWord(true);
			
			cardtext.addMouseListener(this);
			pane.setLayer(cardtext, 1);
			pane.add(cardtext);
			
			if(!deck.getModus().draw_empty_cards)
				addToCardHolder();
		}
		else
		{
			pane.remove(cardtext);
			icon = null;
			if(!deck.getModus().draw_empty_cards)
				removeFromCardHolder();
		}
		deck.refreshDockIcons();
	}
	
	public void setWidget(Widget widget)
	{
		this.widget = widget;
		if(widget!=null)
		{
			widget.card = this;
			
			file = null;
			image = null;
			string = null;
			
			widgetpanel = widget.getPanel();
			widgetpanel.setBounds(15*getWidth()/148,60*getHeight()/188,24*getWidth()/37,100*getHeight()/188);
			pane.setLayer(widgetpanel, 1);
			pane.add(widgetpanel);
			widgetpanel.addMouseListener(this);
			
			if(!deck.getModus().draw_empty_cards)
				addToCardHolder();
		}
		else
		{
			pane.remove(widgetpanel);
			icon = null;
			if(!deck.getModus().draw_empty_cards)
				removeFromCardHolder();
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
		return file==null && string==null && image==null && widget==null;
	}
	
	private void flip()
	{
		reverse = new JWindow();
		reverse.setSize(323,410);
		reverse.setLocationRelativeTo(null);
		Main.setTransparent(reverse);
		JLayeredPane pane = new JLayeredPane();
		pane.setBounds(0,0,296,376);
		pane.setLayout(null);
		
		JLabel background = new JLabel(Main.createImageIcon(deck.getModus().getCardBackBgUrl()));
		background.setBounds(0,0,323,410);
		pane.setLayer(background, 0);
		pane.add(background);
		
		JLabel distaction = new JLabel(Main.createImageIcon("modi/global/captcha.gif"));
		distaction.setBounds(31,30,271,352);
		pane.setLayer(distaction, 1);
		pane.add(distaction);
		
		JLabel code = new JLabel("<html><font face=Courier size=20 color=ccccff>" + getCode() + "</font></html>");
		code.setHorizontalAlignment(JLabel.CENTER);
		code.setBounds(0,0,323,410);
		pane.setLayer(code, 2);
		pane.add(code);
		
		reverse.addMouseListener(this);
		reverse.add(pane);
		reverse.setVisible(true);
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
		if(e.getSource().equals(cardicon)
				|| e.getSource().equals(cardtext)
				|| e.getSource().equals(widgetpanel))
		{
			if(accessible)
			{
				if(widget==null)
				{
					if(e.getButton()==MouseEvent.BUTTON1)
						deck.getModus().open(this);
					else
						deck.openWithoutRemoval(this);
				}
				else
				{
					widget.mouseClicked(e);
				}
			}
		}
		
		if(e.getSource().equals(icon) && accessible)
		{
			if(e.getButton()==MouseEvent.BUTTON1)
			{ deck.getModus().open(this); }
			else
			{ createPopupMenu(e); }
		}
		
		if(e.getSource().equals(flip))
		{
			if(reverse==null){ flip(); }
		}
		else if(e.getSource().equals(reverse))
		{
			reverse.setVisible(false);
			reverse = null;
		}
	}
	@Override
	public void mouseEntered(MouseEvent e)
	{
		deck.getModus().actionPerformed(new ActionEvent(this, 612, "card mouse enter"));
		deck.getCardHolder().getMouseListeners()[0].mouseEntered(e);
		
		if(widget!=null && (e.getSource().equals(widgetpanel)||e.getSource().equals(icon)))
		{
			widget.mouseEntered(e);
		}
		
		if(!isEmpty()){ flip.setVisible(true); }
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
		deck.getModus().actionPerformed(new ActionEvent(this, 613, "card mouse exit"));
		deck.getCardHolder().getMouseListeners()[0].mouseExited(e);
		
		if(widget!=null && (e.getSource().equals(widgetpanel)||e.getSource().equals(icon)))
		{
			widget.mouseExited(e);
		}
		
		flip.setVisible(false);
	}
	@Override
	public void mousePressed(MouseEvent e)
	{
		if(widget!=null && (e.getSource().equals(widgetpanel)||e.getSource().equals(icon)))
		{
			widget.mousePressed(e);
		}
	}
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if(widget!=null && (e.getSource().equals(widgetpanel)||e.getSource().equals(icon)))
		{
			widget.mouseReleased(e);
		}	
	}
	@Override
	public void mouseDragged(MouseEvent e)
	{
		if(widget!=null && (e.getSource().equals(widgetpanel)||e.getSource().equals(icon)))
		{
			widget.mouseEntered(e);
		}
	}
	@Override
	public void mouseMoved(MouseEvent e)
	{
		if(widget!=null && (e.getSource().equals(widgetpanel)||e.getSource().equals(icon)))
		{
			widget.mouseEntered(e);
		}
	}
	
	private class DragListener implements MouseListener, MouseMotionListener
	{
		int startx = 0;
		int starty = 0;
		boolean dragging = false;
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if(dragging && deck.getModus().areCardsDraggable())
			{
				int x = e.getXOnScreen();
				int y = e.getYOnScreen();
				deck.getCardHolder().setLocation(x-startx, y-starty);
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			dragging = true;
			startx = e.getX()+position.x;
			starty = e.getY()+position.y;
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			dragging = false;
		}
		
		@Override
		public void mouseMoved(MouseEvent e){}
		@Override
		public void mouseClicked(MouseEvent e){}
		@Override
		public void mouseEntered(MouseEvent e){}
		@Override
		public void mouseExited(MouseEvent e){}
		
	}

	
}
