package sylladex;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Animation implements ActionListener
{
	private JComponent comp;
	
	private Point startposition;
	private Point finalposition;
	private AnimationType type;
	private ActionListener listener;
	
	private String command = new String();
	private Timer timer = new Timer(50, this);
	
	public enum AnimationType { MOVE, BOUNCE, BOUNCE_SPOT, WAIT }
	
	public Animation(JComponent comp, Point finalposition, AnimationType type, ActionListener listener, String command)
	{
		this.comp = comp;
		this.startposition = comp.getLocation();
		this.finalposition = finalposition;
		this.type = type;
		this.listener = listener;
		
		this.command = command;
	}
	
	public Animation(SylladexCard card, Point finalposition, AnimationType type, ActionListener listener, String command)
	{
		this.comp = card.getPanel();
		this.startposition = card.getPosition();
		this.finalposition = finalposition;
		this.type = type;
		this.listener = listener;
		
		this.command = command;
	}
	
	//Only for WAIT
	public Animation(AnimationType type, int duration, ActionListener listener, String command)
	{
		this.type = AnimationType.WAIT;
		this.listener = listener;
		this.command = command;
		
		timer.setInitialDelay(duration);
	}
	
	public JComponent getComponent()
	{
		return comp;
	}
	
	public void run()
	{
		timer.restart();
	}
	
	public void stop()
	{
		timer.stop();
		comp.setLocation(finalposition);
	}
	
	private void fireEvent()
	{
		ActionEvent f = new ActionEvent(this, 413, command);
		if(listener!=null)
			listener.actionPerformed(f);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(timer))
		{
			switch (type)
			{
				case MOVE:
				{
					if(comp.getLocation().equals(finalposition)) { timer.stop(); fireEvent(); }
					else if(comp.getLocation().equals(startposition))
					{
						comp.setLocation( (finalposition.x+startposition.x)/2, (finalposition.y+startposition.y)/2);
						timer.restart();
					}
					else
					{
						comp.setLocation(finalposition);
						timer.stop();
						fireEvent();
					}
					break;
				}
				case BOUNCE:
				{
					if(comp.getLocation().equals(finalposition)) { timer.stop(); fireEvent(); }
					else if(comp.getLocation().equals(startposition))
					{
						int magnitude = 5;
						int xbounce = magnitude*getMultiplier(finalposition.x - startposition.x);
						int ybounce = magnitude*getMultiplier(finalposition.y - startposition.y);
						comp.setLocation(finalposition.x+xbounce, finalposition.y+ybounce);
						timer.restart();
					}
					else
					{
						comp.setLocation(finalposition);
						timer.stop();
						fireEvent();
					}
					break;
				}
				case BOUNCE_SPOT:
				{
					if(comp.getLocation().equals(startposition))
					{
						comp.setLocation(finalposition);
						timer.restart();
					}
					else if(comp.getLocation().equals(finalposition))
					{
						comp.setLocation(startposition);
						timer.stop();
						fireEvent();
					}
				}
				case WAIT:
				{
					fireEvent();
				}
			}
		}
		else if (e.getActionCommand().equals("run"))
		{
			run();
		}
	}
	
	private int getMultiplier(int n)
	{
		if(n>0) { return 1; }
		if(n<0) { return -1; }
		return 0;
	}
}
