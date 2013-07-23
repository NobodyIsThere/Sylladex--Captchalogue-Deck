import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sylladex.Widget;
import util.RW;
import util.Util;
import util.Util.OpenReason;

public class FetchModusWidget extends Widget
{
	private ImageIcon image;
	private ArrayList<String> info;
	private String savestring;
	
	@Override
	public void prepare() {}

	@Override
	public void add() {}

	@Override
	public void load(String string)
	{
		savestring = string;
		File file = new File(string);
		info = RW.readFile(file);
		image = Util.createImageIcon("modi/" + info.get(2));
		dock_icon = new JLabel(Util.getDockIcon(image.getImage()));
	}

	@Override
	public void open(OpenReason reason) {}

	@Override
	public String getName()
	{
		if (info != null)
		{
			return info.get(0);
		}
		return "Fetch modus";
	}

	@Override
	public String getSaveString()
	{
		return savestring;
	}

	@Override
	public JPanel getPanel()
	{
		int card_width = deck.getModus().getSettings().get_card_width();
		int card_height = deck.getModus().getSettings().get_card_height();
		return Util.getCardPanelFromImage(image.getImage(), card_width, card_height);
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
