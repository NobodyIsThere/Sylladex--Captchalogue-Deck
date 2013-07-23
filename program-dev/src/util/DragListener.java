package util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JWindow;

public class DragListener implements MouseListener, MouseMotionListener
{
	int startx = 0;
	int starty = 0;
	boolean dragging = false;
	JWindow window;
	
	public DragListener(JWindow window)
	{
		this.window = window;
	}
	
	@Override
	public void mouseDragged(MouseEvent e)
	{
		if(dragging)
		{
			int x = e.getXOnScreen();
			int y = e.getYOnScreen();
			window.setLocation(x-startx, y-starty);
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		dragging = true;
		startx = e.getXOnScreen()-window.getX();
		starty = e.getYOnScreen()-window.getY();
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