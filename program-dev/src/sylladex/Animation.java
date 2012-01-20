package sylladex;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Animation implements ActionListener
{
	private JComponent comp;
	private SylladexCard card;
	
	private Point startposition;
	private Point finalposition;
	private AnimationType type;
	private ActionListener listener;
	
	private String command = new String();
	private Timer timer = new Timer(50, this);
	
	public enum AnimationType { MOVE, BOUNCE, BOUNCE_SPOT, WAIT }
	
	/**
	 * Sets up a new animation.
	 * @param comp - The component to animate.
	 * @param finalposition - The final position of the component.
	 * @param type - The animation type.
	 * @param listener - An ActionListener to respond when the animation ends.
	 * @param command - The action command for the listener.
	 */
	public Animation(JComponent comp, Point finalposition, AnimationType type, ActionListener listener, String command)
	{
		this.comp = comp;
		this.startposition = comp.getLocation();
		this.finalposition = finalposition;
		this.type = type;
		this.listener = listener;
		
		this.command = command;
	}
	
	/**
	 * Sets up a new animation.
	 * @param card - The sylladex card to animate.
	 * @param finalposition - The final position of the card.
	 * @param type - The animation type.
	 * @param listener - An ActionListener to respond when the animation ends.
	 * @param command - The action command for the listener.
	 */
	public Animation(SylladexCard card, Point finalposition, AnimationType type, ActionListener listener, String command)
	{
		this.card = card;
		this.comp = card.getPanel();
		this.startposition = card.getPosition();
		this.finalposition = finalposition;
		this.type = type;
		this.listener = listener;
		
		this.command = command;
	}
	
	//Only for WAIT
	/**
	 * Sets up a new WAIT animation.
	 * @param type - AnimationType.WAIT. This is accepted as a parameter for code readability.
	 * @param duration - The amount of time to wait, in milliseconds.
	 * @param listener - An ActionListener to call after the timer expires.
	 * @param command - The action command for the listener.
	 */
	public Animation(AnimationType type, int duration, ActionListener listener, String command)
	{
		this.type = AnimationType.WAIT;
		this.listener = listener;
		this.command = command;
		
		timer.setInitialDelay(duration);
		timer.setDelay(duration);
	}
	
	/**
	 * @return The component which is animated by this Animation.
	 */
	public JComponent getComponent()
	{
		return comp;
	}
	
	/**
	 * @return The card which is animated by this Animation.
	 */
	public SylladexCard getCard()
	{
		return card;
	}
	
	/**
	 * Runs the animation.
	 */
	public void run()
	{
		timer.restart();
	}
	
	/**
	 * Aborts the animation and sets the position of the animated component to the final position specified in the constructor.
	 */
	public void stop()
	{
		timer.stop();
		comp.setLocation(finalposition);
	}
	
	private void fireEvent()
	{
		if(card!=null)
			card.setPosition(finalposition);
		ActionEvent f = new ActionEvent(this, 413, command);
		if(listener!=null)
			listener.actionPerformed(f);
	}
	
	/**
	 * Sets the ActionListener to be called when the animation completes.
	 * @param listener - The listener to call.
	 */
	public void setListener(ActionListener listener)
	{this.listener = listener;}
	
	/**
	 * Sets the action command which is passed to the listener when the animation completes.
	 * @param command - The action command.
	 */
	public void setActionCommand(String command)
	{this.command = command;}
	
	/**
	 * Sets the start position for the animation.
	 * @param startposition
	 */
	public void setStartPosition(Point startposition)
	{this.startposition = startposition;}
	
	/**
	 * @return This animation's start position.
	 */
	public Point getStartPosition()
	{return startposition;}
	
	/**
	 * Sets the end position for the animation.
	 * @param finalposition
	 */
	public void setFinalPosition(Point finalposition)
	{this.finalposition = finalposition;}
	
	/**
	 * @return - This animation's end position.
	 */
	public Point getFinalPosition()
	{return finalposition;}

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
					timer.stop();
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
