package sylladex;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import util.DragListener;
import util.Util;

public class StrifeSpecibus implements MouseListener
{
	private ArrayList<CaptchalogueCard> cards;
	private String kind;
	
	private JWindow window;
	
	public StrifeSpecibus(Main deck)
	{
		cards = new ArrayList<CaptchalogueCard>();
	}
	
	public void setKind(String k)
	{
		kind = k;
		createWindow();
	}
	
	public boolean matches(String s)
	{
		if (s.contains(kind))
			return true;
		return false;
	}
	
	public void add(CaptchalogueCard c)
	{
		cards.add(c);
		update();
	}
	
	public void createWindow()
	{
		window = new JWindow();
		window.setSize(296, 376);
		window.setLocationRelativeTo(null);
		window.setBackground(Util.COLOR_TRANSPARENT);
		
		window.add(getPanel(0));
	}
	
	public JPanel getPortfolioPanel()
	{
		return getPanel(1);
	}
	
	private JPanel getPanel(int type)
	{
		// 0: full size; 1: small
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(null);
		JLabel background = null;
		switch (type)
		{
			case 0:
			{
				background = new JLabel(Util.createImageIcon("modi/global/strife/specibus.png"));
				background.setBounds(0, 0, 296, 376);
				DragListener dl = new DragListener(window);
				panel.addMouseListener(this);
				panel.addMouseListener(dl);
				panel.addMouseMotionListener(dl);
				break;
			}
			case 1:
			{
				background = new JLabel(Util.getSizedIcon(
										Util.createImageIcon("modi/global/strife/specibus.png").getImage(),
										150, 190));
				background.setBounds(0, 0, 150, 190);
				panel.setSize(150, 190);
				panel.addMouseListener(this);
				break;
			}
		}
		if (background != null)
		{
			panel.add(background);
		}
		else
		{
			Util.error("Unable to create strife specibus background image.");
		}
		
		return panel;
	}
	
	public void update()
	{
		
	}
	
	public void show()
	{
		update();
		window.setVisible(true);
	}
	
	public void hide()
	{
		window.setVisible(false);
	}
	
	public boolean isShowing()
	{
		return window.isVisible();
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (e.getButton() != MouseEvent.BUTTON1)
			hide();
		else
			show();
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}
