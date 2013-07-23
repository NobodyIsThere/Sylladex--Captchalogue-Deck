import java.awt.Image;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import sylladex.Widget;
import util.Util;
import util.Util.OpenReason;

public class CaptchaloguedCaptchalogueCard extends Widget
{

	@Override
	public void prepare()
	{
		Image image = Util.createImageIcon(deck.getModus().getSettings().get_card_image()).getImage();
		dock_icon.setIcon(Util.getDockIcon(image));
	}

	@Override
	public void add() {}

	@Override
	public void load(String string) {}

	@Override
	public void open(OpenReason reason)
	{
		if (reason == OpenReason.USER_KEEP)
			deck.getModus().open(card, OpenReason.MODUS_PUSH);
		else if (reason != OpenReason.USER_EJECT)
			deck.eject(card.getItem());
		deck.getModus().addCard();
	}

	@Override
	public String getName()
	{
		return "Captchalogue card";
	}
	
	@Override
	public String canonCaptchaCodeOverride()
	{
		return "00000000";
	}

	@Override
	public String getSaveString()
	{
		return "";
	}

	@Override
	public JPanel getPanel()
	{
		int width = deck.getModus().getSettings().get_card_width();
		int height = deck.getModus().getSettings().get_card_height();
		Image image = Util.createImageIcon(deck.getModus().getSettings().get_card_image()).getImage();
		return Util.getCardPanelFromImage(image, width, height);
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
