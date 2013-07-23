package util;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Timer;

import sylladex.CaptchalogueCard;

public class Animation implements ActionListener
{
	private JComponent comp;
	
	private Point startposition;
	private Point finalposition;
	private AnimationType type;
	private ActionListener listener;
	
	private String command = new String();
	private Timer timer = new Timer(50, this);
	private int step = 0;
	
	public enum AnimationType { JUMP, MOVE, BOUNCE, BOUNCE_SPOT, WAIT }
	
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
		if (comp instanceof CaptchalogueCard) { this.startposition = ((CaptchalogueCard) comp).getFinalLocation(); }
		this.finalposition = finalposition;
		this.type = type;
		this.listener = listener;
		
		this.command = command;
	}
	
	public Animation(AnimationType wait, int length, ActionListener listener, String command)
	{
		this.type = AnimationType.WAIT;
		this.listener = listener;
		this.command = command;
		timer.setInitialDelay(length);
	}
	
	public static void waitFor(int length, ActionListener listener, String command)
	{
		Timer timer = new Timer(length, listener);
		timer.setActionCommand(command);
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * @return The component which is animated by this Animation.
	 */
	public JComponent getAnimationTarget()
	{
		return comp;
	}
	
	/**
	 * Set the animation target
	 */
	public void setAnimationTarget(JComponent comp)
	{
		this.comp = comp;
	}
	
	/**
	 * Runs the animation.
	 */
	public void run()
	{
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * Aborts the animation and sets the position of the animated component to the final position specified in the constructor.
	 */
	public void stop()
	{
		timer.stop();
		if (comp != null)
			comp.setLocation(finalposition);
	}
	
	private void fireEvent()
	{
		if (listener != null)
		{
			if (command == null) { command = Util.ACTION_ANIMATION_COMPLETE; }
			ActionEvent f = new ActionEvent(this, 413, command);
			listener.actionPerformed(f);
		}
	}
	
	/**
	 * Sets the ActionListener to be called when the animation completes.
	 * @param listener - The listener to call.
	 */
	public void setListener(ActionListener listener)
	{ this.listener = listener; }
	
	/**
	 * Sets the action command which is passed to the listener when the animation completes.
	 * @param command - The action command.
	 */
	public void setActionCommand(String command)
	{ this.command = command; }
	
	/**
	 * Sets the start position for the animation.
	 * @param startposition
	 */
	public void setStartPosition(Point startposition)
	{ this.startposition = startposition; }
	
	/**
	 * @return This animation's start position.
	 */
	public Point getStartPosition()
	{ return startposition; }
	
	/**
	 * Sets the end position for the animation.
	 * @param finalposition
	 */
	public void setFinalPosition(Point finalposition)
	{ this.finalposition = finalposition; }
	
	/**
	 * @return - This animation's end position.
	 */
	public Point getFinalPosition()
	{ return finalposition; }

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(timer))
		{
			switch (type)
			{
				case JUMP:
				{
					if (step == 0)
					{
						comp.setLocation(finalposition);
						timer.stop();
						fireEvent();
					}
					break;
				}
				case MOVE:
				{
					if(step == 0)
					{
						comp.setLocation(new Point((finalposition.x+startposition.x)/2, (finalposition.y+startposition.y)/2));
						step++;
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
					if (step == 0)
					{
						int magnitude = 2;
						int xbounce = magnitude*getMultiplier(finalposition.x - startposition.x);
						int ybounce = magnitude*getMultiplier(finalposition.y - startposition.y);
						comp.setLocation(new Point(finalposition.x+xbounce, finalposition.y+ybounce));
						step++;
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
					if (step == 0)
					{
						comp.setLocation(finalposition);
						step++;
						timer.restart();
					}
					else
					{
						comp.setLocation(startposition);
						timer.stop();
						fireEvent();
					}
					break;
				}
				case WAIT:
				{
					timer.stop();
					fireEvent();
					break;
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
