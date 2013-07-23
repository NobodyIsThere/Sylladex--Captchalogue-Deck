import java.awt.event.ActionEvent;

import sylladex.FetchModus;
import sylladex.Main;
import sylladex.CaptchalogueCard;
import sylladex.SylladexItem;
import util.Util.OpenReason;

public class TestModus extends FetchModus
{
	public TestModus(Main m)
	{
		super(m);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		
	}

	@Override
	public void initialSettings()
	{
		System.out.println("Initial settings");
	}

	@Override
	public void prepare()
	{
		System.out.println("Prepare");
	}

	@Override
	public void ready()
	{
		System.out.println("Ready");
		new InnerClass();
	}
	
	private class InnerClass
	{
		public InnerClass()
		{
			
		}
	}

	@Override
	public boolean captchalogue(SylladexItem item)
	{
		return false;
	}

	@Override
	public void open(CaptchalogueCard card, OpenReason reason)
	{
		
	}

	@Override
	public void addCard()
	{
		
	}

	@Override
	public Object[] getCardOrder()
	{
		return deck.getCards().toArray();
	}
}
