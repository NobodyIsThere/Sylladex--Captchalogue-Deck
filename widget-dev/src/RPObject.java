import sylladex.*;
import javax.swing.*;

public class RPObject extends Widget
{
	private String string = "RP";
	
	public void open()
	{
		
	}

	@Override
	public void prepare()
	{
		string = JOptionPane.showInputDialog("Enter a name for the item.");
		//Accept an image
	}

	@Override
	public void load(String string)
	{
		this.string = string;
	}
	
	@Override
	public String getString()
	{
		return string;
	}

	@Override
	public String getSaveString()
	{
		return string;
	}
}
