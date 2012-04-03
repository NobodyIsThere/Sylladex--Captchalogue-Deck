import sylladex.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class RPObject extends Widget implements MouseListener
{
	private String string = "RP";
	private File img;
	private final JFileChooser image_chooser = new JFileChooser();
	
	public void open(){}
	
	public void prepare(){}
	
	private void setImages()
	{
		ImageIcon icon = Main.createImageIcon(img.getPath());
		dock_icon = new JLabel(Main.getDockIcon(icon.getImage()));
	}
	
	public JPanel getPanel()
	{
		JPanel panel = new JPanel();
		ImageIcon icon = Main.createImageIcon(img.getPath());
		int cardwidth = m.getModus().getModusSettings().get_card_width();
		int cardheight = m.getModus().getModusSettings().get_card_height();
		Icon image = Main.getSizedIcon(icon.getImage(), 24*cardwidth/37, 100*cardheight/188);
		JLabel image_label = new JLabel(image);
		image_label.setBounds(0, 0, 24*cardwidth/37, 100*cardheight/188);
		panel.setLayout(null);
		panel.setOpaque(false);
		panel.add(image_label);
		return panel;
	}

	@Override
	public void load(String savestring)
	{
		string = savestring.substring(0, savestring.indexOf(";"));
		img = new File(savestring.substring(savestring.indexOf(";")+1));
		if(img.exists())
			setImages();
	}
	
	@Override
	public String getString()
	{
		return string;
	}

	@Override
	public String getSaveString()
	{
		return string + ";" + img.getPath();
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
	

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if(card.isAccessible())
		{
			if(e.getButton()==MouseEvent.BUTTON1)
			{
				m.getModus().open(card);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e){}

	@Override
	public void mouseExited(MouseEvent e){}

	@Override
	public void mousePressed(MouseEvent e){}

	@Override
	public void mouseReleased(MouseEvent e){}

	@Override
	public void add()
	{
		//Ask for a name
		string = JOptionPane.showInputDialog("Enter a name for the item.");
		//Accept an image
		ImageFileFilter filter = new ImageFileFilter();
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
}
