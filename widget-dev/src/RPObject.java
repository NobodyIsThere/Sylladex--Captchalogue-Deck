//import javax.swing.JLabel;

import sylladex.*;

public class RPObject extends Widget
{
	private String string = "RP";
	
	public void open()
	{
		
	}

	@Override
	public void prepare()
	{
		//dock_icon = new JLabel(Main.createImageIcon("modi/hashmap/collision.gif"));
	}

	@Override
	public void load(String string)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getString()
	{
		return string;
	}

	@Override
	public String getSaveString()
	{
		return "Temporary save string for RP widget";
	}
}
