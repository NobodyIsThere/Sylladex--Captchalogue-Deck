package ui;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class SButton extends JButton implements MouseListener
{
	private boolean depressed = false;
	private JLabel textlabel = new JLabel();
	
	public SButton()
	{
		addMouseListener(this);
		add(textlabel);
	}
	
	public SButton(String text)
	{
		addMouseListener(this);
		setText(text);
		add(textlabel);
	}
	
	@Override
	public void setText(String text)
	{
		textlabel.setText(text);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		//super.processMouseEvent(e);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		depressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		depressed = false;
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		//super.processMouseEvent(e);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		//super.processMouseEvent(e);
	}
}
