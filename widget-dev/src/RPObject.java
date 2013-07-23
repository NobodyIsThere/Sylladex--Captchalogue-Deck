import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import sylladex.Widget;
import util.Util;
import util.Util.OpenReason;

public class RPObject extends Widget
{
	private File img;
	private String name;
	
	@Override
	public void prepare() {}

	@Override
	public void add() {}

	@Override
	public void load(String string)
	{
		name = string.substring(0, string.indexOf(";"));
		String path = string.substring(string.indexOf(";") + 1);
		img = new File(path);
		if(img.exists())
			setImages();
	}
	
	private void setImages()
	{
		dock_icon.setIcon(Util.getDockIcon(Util.createImageIcon(img.getAbsolutePath()).getImage()));
	}

	@Override
	public void open(OpenReason reason) {}

	@Override
	public String getName()
	{
		if (name == null)
		{
			name = JOptionPane.showInputDialog("Enter a name for the item.");
		}
		return name;
	}

	@Override
	public String getSaveString()
	{
		try
		{
			if (img.getCanonicalPath().startsWith(new File(".").getAbsolutePath().replace(".", "")))
			{
				return getName() + ";" +
						new File(".").toPath().toAbsolutePath().relativize(img.toPath().toAbsolutePath()).toString().replace("../", "");
			}
			return getName() + ";" + img.getCanonicalPath();
		}
		catch (IOException x) { x.printStackTrace(); }
		return getName() + ";widgets/RPObject/missing.gif";
	}

	@Override
	public JPanel getPanel()
	{
		if (img == null)
		{
			ImageFileFilter filter = new ImageFileFilter();
			JFileChooser image_chooser = new JFileChooser();
			image_chooser.setFileFilter(filter);
			int decision = image_chooser.showOpenDialog(dock_icon);
			if(decision==JFileChooser.APPROVE_OPTION)
			{
				img = image_chooser.getSelectedFile();
				setImages();
			}
			else
			{
				img = new File("widgets/RPObject/missing.gif");
				setImages();
			}
		}
		int card_width = deck.getModus().getSettings().get_card_width();
		int card_height = deck.getModus().getSettings().get_card_height();
		return Util.getCardPanelFromImage(Util.createImageIcon(img.getAbsolutePath()).getImage(),
				card_width, card_height);
	}
	
	private class ImageFileFilter extends FileFilter
	{

		@Override
		public boolean accept(File f)
		{
			if(f.isDirectory()) { return true; }
			
			String extension = f.getName();
			if(extension.lastIndexOf(".")!=-1)
				extension = extension.substring(extension.lastIndexOf("."));
			
			if(extension.equals(".gif")
					|| extension.equals(".jpeg")
					|| extension.equals(".jpg")
					|| extension.equals(".png"))
			{
				return true;
			}
			return false;
		}

		@Override
		public String getDescription()
		{
			return "Images (.gif, .jpeg, .jpg, .png)";
		}
		
	}
}
