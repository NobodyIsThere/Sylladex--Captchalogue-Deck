package sylladex;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

public class SylladexCard implements MouseInputListener
{
	//Referring to me
	private int id;
	private Main deck;
	private File file = null;
	private String string = null;
	private Image image = null;
	private Widget widget = null;
	private JLabel icon; //dock icon
	
	private boolean accessible = false; //Whether or not the card is click-able
	
	//Referring to my avatar
	private Point position = new Point(0,0);
	private JLayeredPane pane;	//mine
	private JPanel panel; //The card itself
	private JLabel cardicon; //mine - shows images and icons
	private JTextArea cardtext; //mine - shows strings
	private JPanel widgetpanel; //controlled by the widget.
	private JPanel foreground;
	
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
		ImageIcon cardbgimage = deck.createImageIcon(deck.getModus().getCardBgUrl());
		JLabel cardbg = new JLabel(cardbgimage);
		cardbg.setBounds(0,0,getWidth(),getHeight());
		
		inaccessible = new JLabel(deck.createImageIcon("modi/global/inaccessible.png"));
		inaccessible.setBounds(0,0,getWidth(),getHeight());
		
		foreground = new JPanel();
		foreground.setBounds(0,0,getWidth(),getHeight());
		foreground.setOpaque(false);
		foreground.setLayout(null);
		
		cardbg.addMouseListener(this);
		cardbg.addMouseMotionListener(this);
		
		DragListener mouse = new DragListener();
		cardbg.addMouseListener(mouse);
		cardbg.addMouseMotionListener(mouse);
		
		pane.setLayer(cardbg, 0);
		pane.add(cardbg);
		
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
			if(!deck.getPreferences().usb_mode())
				return file.getPath();
			return "files" + System.getProperty("file.separator") + file.getName();
		}
		else if(string!=null)
		{
			return string.replaceAll(System.getProperty("line.separator"), "SYLLADEX_NL");
		}
		else if(image!=null)
		{
			if(image instanceof BufferedImage)
			{
				File f = new File("files" + System.getProperty("file.separator") + "captchalogued_image_" + new Double(Math.random()).toString().replaceAll("\\.", "") + ".png");
				BufferedImage b = (BufferedImage)image;
				try
				{
					ImageIO.write(b, "png", f);
				}
				catch (IOException e){ e.printStackTrace(); }
				return f.getPath().substring(f.getPath().indexOf("files" + System.getProperty("file.separator") + "captchalogued_image_"));
			}
		}
		else if(widget!=null)
		{
			return widget.getSaveString();
		}
		return null;
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
			
			Icon icon = deck.getIconFromFile(file);
			String filename = file.getName();
			cardicon = new JLabel(filename);
			cardicon.setBounds(15*getWidth()/148,35*getHeight()/94,24*getWidth()/37,25*getHeight()/94);
			cardicon.setIcon(icon);
			cardicon.setHorizontalAlignment(JLabel.CENTER);
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
			Icon icon = deck.getSizedIcon(image, cardicon.getWidth(), cardicon.getHeight());
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
			file = null;
			image = null;
			string = null;
			
			widgetpanel = widget.getPanel();
			pane.setLayer(widgetpanel, 1);
			pane.add(widgetpanel);
			
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
	
	// Mouse functions
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if(e.getSource().equals(cardicon) || e.getSource().equals(cardtext) || e.getSource().equals(icon))
		{
			if(accessible)
			{
				if(e.getButton()==MouseEvent.BUTTON1)
					deck.getModus().open(id);
				else
					deck.openWithoutRemoval(this);
			}
		}
	}
	@Override
	public void mouseEntered(MouseEvent e)
	{
		deck.getModus().actionPerformed(new ActionEvent(this, 612, "card mouse enter"));
		deck.getCardHolder().getMouseListeners()[0].mouseEntered(e);
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
		deck.getModus().actionPerformed(new ActionEvent(this, 613, "card mouse exit"));
		deck.getCardHolder().getMouseListeners()[0].mouseExited(e);
	}
	@Override
	public void mousePressed(MouseEvent e){}
	@Override
	public void mouseReleased(MouseEvent e){}
	@Override
	public void mouseDragged(MouseEvent arg0){}
	@Override
	public void mouseMoved(MouseEvent arg0){}
	
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
