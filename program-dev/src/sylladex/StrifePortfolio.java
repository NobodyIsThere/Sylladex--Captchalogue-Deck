package sylladex;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JWindow;

import util.DragListener;
import util.Util;

public class StrifePortfolio implements MouseListener
{
	private JWindow window;
	private JLayeredPane pane;
	
	private ArrayList<StrifeSpecibus> specibi;
	
	public StrifePortfolio()
	{
		window = new JWindow();
		window.setSize(618, 432);
		window.setLayout(null);
		window.setLocationRelativeTo(null);
		window.setBackground(Util.COLOR_TRANSPARENT);
		
		pane = new JLayeredPane();
		pane.setLayout(null);
		pane.setBounds(0, 0, 618, 422);
		pane.setOpaque(false);
		
		window.setLayeredPane(pane);
	}
	
	public void update()
	{
		pane.removeAll();
		
		JLabel background = new JLabel(Util.createImageIcon("modi/global/strife/portfolio.png"));
		background.setBounds(0, 0, 618, 432);
		
		DragListener d = new DragListener(window);
		background.addMouseListener(this);
		background.addMouseListener(d);
		background.addMouseMotionListener(d);
		
		JLabel layer1 = new JLabel(Util.createImageIcon("modi/global/strife/portfolio_1.png"));
		JLabel layer2 = new JLabel(Util.createImageIcon("modi/global/strife/portfolio_2.png"));
		JLabel layer3 = new JLabel(Util.createImageIcon("modi/global/strife/portfolio_3.png"));
		JLabel layer4 = new JLabel(Util.createImageIcon("modi/global/strife/portfolio_4.png"));
		JLabel layer5 = new JLabel(Util.createImageIcon("modi/global/strife/portfolio_5.png"));
		JLabel layer6 = new JLabel(Util.createImageIcon("modi/global/strife/portfolio_6.png"));
		JLabel layer7 = new JLabel(Util.createImageIcon("modi/global/strife/portfolio_7.png"));
		
		layer1.setBounds(16, 5, 187, 341);
		layer2.setBounds(45, 5, 257, 341);
		layer3.setBounds(94, 5, 307, 341);
		layer4.setBounds(173, 5, 327, 341);
		layer5.setBounds(273, 5, 277, 341);
		layer6.setBounds(371, 5, 228, 341);
		layer7.setBounds(470, 5, 129, 341);
		
		pane.setLayer(background, 0);
		pane.setLayer(layer1, 2);
		pane.setLayer(layer2, 4);
		pane.setLayer(layer3, 6);
		pane.setLayer(layer4, 8);
		pane.setLayer(layer5, 10);
		pane.setLayer(layer6, 12);
		pane.setLayer(layer7, 14);
		
		pane.add(background);
		pane.add(layer1);
		pane.add(layer2);
		pane.add(layer3);
		pane.add(layer4);
		pane.add(layer5);
		pane.add(layer6);
		pane.add(layer7);
		
		int i = 1;
		for (StrifeSpecibus s : specibi)
		{
			int layer = i*2 - 1;
			
			JPanel panel = s.getPortfolioPanel();
			panel.setLocation(layer*35 - 10, 20 + layer*10);
			pane.setLayer(panel, layer);
			pane.add(panel);
			// Can only display 7
			if (i<7) i++; else break;
		}
	}
	
	public void setSpecibi(ArrayList<StrifeSpecibus> specibi)
	{
		this.specibi = specibi;
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
		if(e.getButton() != MouseEvent.BUTTON1)
		hide();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}
}
