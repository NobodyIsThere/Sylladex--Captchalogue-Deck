package util;

import java.awt.Point;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public abstract class AnimatedObject extends JComponent
{
	public abstract Point getLocation();
	public abstract void setLocation(Point finalposition);
}
