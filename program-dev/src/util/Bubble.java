package util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;

import sylladex.Main;

public class Bubble implements ActionListener, MouseListener
{
	private Main deck;
	
	private JWindow w;
	private JLayeredPane panel;
	private JLabel bubble;
	private JPanel contents;
	
	private JComponent target;
	private int offset = 0;
	private int x_offset;
	
	private Timer t;
	private int counter = 0;
	
	private final String PATH = "modi/global/bubble.png";
	private final String PATH_TOP = "modi/global/bubble_top.png";
	
	private ActionListener listener;
	private String command = "bubble_burst";
	
	public Bubble(Main deck, JComponent target, int x_offset)
	{
		this.deck = deck;
		this.target = target;
		
		this.x_offset = x_offset;
		
		w = new JWindow();
		w.setLayout(null);
		w.setSize(80, 101);
		w.setAlwaysOnTop(true);
		w.addMouseListener(this);
		w.setBackground(Util.COLOR_TRANSPARENT);
		
		panel = new JLayeredPane();
		panel.setBounds(0, 0, 80, 99);
		panel.setLayout(null);
		panel.setOpaque(false);
		w.add(panel);
		
		String path = PATH_TOP;
		int contents_y = 40;
		if(!deck.getPreferences().top())
		{
			path = PATH;
			contents_y = 30;
		}
		bubble = new JLabel(Util.createImageIcon(path));
		contents = new JPanel();
		contents.setOpaque(false);
		
		bubble.setBounds(0, 0, 80, 99);
		bubble.setOpaque(false);
		panel.setLayer(bubble, 0);
		panel.add(bubble);
		
		contents.setBounds(0, contents_y, 80, 20);
		contents.setLayout(null);
		panel.setLayer(contents, 1);
		panel.add(contents);
		
		w.setLocation(calculateX(), calculateY());
		w.setVisible(true);
		
		t = new Timer(100, this);
		t.start();
	}
	
	public JPanel getContents()
	{
		return contents;
	}
	
	public void setContents(JPanel contents)
	{
		this.contents = contents;
	}
	
	public void setActionListner(ActionListener listener)
	{
		this.listener = listener;
	}
	
	public ActionListener getActionListener()
	{
		return listener;
	}
	
	public void setActionCommand(String command)
	{
		this.command = command;
	}
	
	public String getActionCommand()
	{
		return command;
	}
	
	public void show()
	{
		w.setVisible(true);
	}
	public void hide()
	{
		w.setVisible(false);
	}
	public boolean isShowing()
	{
		return w.isVisible();
	}
	
	private int calculateX()
	{
		if (target.isShowing())
		{
			return target.getLocationOnScreen().x + target.getWidth()/2 - bubble.getWidth()/2 - 10 + x_offset;
		}
		return x_offset;
	}
	
	private int calculateY()
	{
		if (target.isShowing())
		{
			if (deck.getPreferences().top())
			{
				return target.getLocationOnScreen().y + target.getHeight();
			}
			
			return target.getLocationOnScreen().y - bubble.getHeight();
		}
		return 0;
	}
	
	public void remove()
	{
		t.stop();
		hide();
	}
	
	public void updatePosition()
	{
		String path = PATH_TOP;
		if(!deck.getPreferences().top())
			path = PATH;
		
		bubble.setIcon(Util.createImageIcon(path));
		
		w.setLocation(calculateX(), calculateY());
	}
	
	public int getOffset()
	{
		return x_offset;
	}
	
	public void setOffset(int offset)
	{
		x_offset = offset;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(t))
		{
			if(counter<2)
			{
				offset++;
				counter++;
			}
			else if(counter>=2)
			{
				offset--;
				counter++;
			}
			if(counter>3)
			{
				counter = 0;
				updatePosition();
			}
		}
		panel.setLocation(0, offset);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (listener != null)
		{
			ActionEvent ev = new ActionEvent(this, 413, command);
			listener.actionPerformed(ev);
		}
		remove();
	}

	@Override
	public void mouseEntered(MouseEvent arg0){}
	@Override
	public void mouseExited(MouseEvent arg0){}
	@Override
	public void mousePressed(MouseEvent arg0){}
	@Override
	public void mouseReleased(MouseEvent arg0){}
}