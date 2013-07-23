package ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import sylladex.FetchModusSettings;
import util.Util;

public class PopupMenu implements WindowFocusListener
{
	private ArrayList<MenuItem> items;
	private JFrame window;
	private JPanel panel;
	
	private Color default_color;
	private Color selected_color;
	private Color text_color;
	
	public PopupMenu(FetchModusSettings settings)
	{
		items = new ArrayList<MenuItem>();
		
		this.default_color = settings.get_background_color();
		this.selected_color = settings.get_secondary_color();
		this.text_color = settings.get_text_color();
		
		window = new JFrame();
		window.setUndecorated(true);
		window.setAlwaysOnTop(true);
		window.addWindowFocusListener(this);
		panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		window.add(panel);
	}
	
	private void arrangeItems()
	{
		panel.removeAll();
		for (MenuItem item : items)
		{
			panel.add(item);
		}
		window.revalidate();
		window.setSize(panel.getPreferredSize());
	}
	
	public void show(JComponent component)
	{
		if (!component.isShowing()) { return; }
		Point pos = new Point();
		pos.setLocation(component.getLocationOnScreen().x,
				component.getLocationOnScreen().y);
		int dx = component.getWidth()/2 - window.getWidth()/2;
		int dy = pos.y > Util.SCREEN_SIZE.height/2 ? -window.getHeight() : component.getHeight();
		pos.translate(dx, dy);
		window.setLocation(pos);
		window.setVisible(true);
	}
	
	public void hide()
	{
		window.setVisible(false);
	}
	
	public void add(MenuItem item)
	{
		items.add(item);
		item.setMenu(this);
		arrangeItems();
	}
	
	public void add(ArrayList<MenuItem> menu_items)
	{
		for (MenuItem item : menu_items)
		{
			add(item);
		}
	}
	
	public void remove(MenuItem item)
	{
		items.remove(item);
		arrangeItems();
	}
	
	public void clear()
	{
		items.clear();
		arrangeItems();
	}
	
	public Color getDefaultColor()
	{
		return default_color;
	}
	
	public Color getSelectedColor()
	{
		return selected_color;
	}
	
	public Color getTextColor()
	{
		return text_color;
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {}

	@Override
	public void windowLostFocus(WindowEvent e)
	{
		hide();
	}
}
