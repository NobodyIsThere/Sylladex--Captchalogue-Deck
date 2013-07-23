package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class MenuItem extends JLabel implements MouseListener
{
	private PopupMenu menu;
	private ActionListener listener;
	private String command;
	
	public MenuItem(String text)
	{
		super();
		this.setText("<HTML><FONT FACE=Courier>" + text + "</FONT></HTML>");
		this.setHorizontalAlignment(CENTER);
		this.addMouseListener(this);
		this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		this.setOpaque(true);
	}
	
	protected void setMenu(PopupMenu menu)
	{
		this.menu = menu;
		this.setBackground(menu.getDefaultColor());
		this.setForeground(menu.getTextColor());
	}
	
	public void setActionListener(ActionListener l)
	{
		listener = l;
	}
	
	public void setActionCommand(String command)
	{
		this.command = command;
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		this.setBackground(menu.getSelectedColor());
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		this.setBackground(menu.getDefaultColor());
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		ActionEvent a = new ActionEvent(this, 612, command);
		if (listener != null)
			listener.actionPerformed(a);
		menu.hide();
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
}
