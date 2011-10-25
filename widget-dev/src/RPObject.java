import sylladex.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RPObject extends Widget implements MouseListener
{
	private String string = "RP";
	private String code = "alternia";
	private File img;
	private final JFileChooser image_chooser = new JFileChooser();
	
	public void open()
	{
		
	}

	@Override
	public void prepare()
	{
		//Ask for a name
		string = JOptionPane.showInputDialog("Enter a name for the item.");
		generateCode();
		//Accept an image
		ImageFileFilter filter = new ImageFileFilter();
		image_chooser.setFileFilter(filter);
		int decision = image_chooser.showOpenDialog(dock_icon);
		if(decision==JFileChooser.APPROVE_OPTION)
		{
			img = image_chooser.getSelectedFile();
			setImages(img);
		}
		else
		{
			img = new File("widgets/RPObject/missing.gif");
			setImages(img);
		}
		panel.addMouseListener(this);
	}
	
	private void setImages(File img)
	{
		ImageIcon icon = Main.createImageIcon(img.getPath());
		int cardwidth = m.getModus().getCardWidth();
		int cardheight = m.getModus().getCardHeight();
		Icon image = Main.getSizedIcon(icon.getImage(), 24*cardwidth/37, 100*cardheight/188);
		JLabel image_label = new JLabel(image);
		image_label.setBounds(0, 0, 24*cardwidth/37, 100*cardheight/188);
		panel.setLayout(null);
		panel.add(image_label);
		dock_icon = new JLabel(Main.getDockIcon(icon.getImage()));
	}

	@Override
	public void load(String savestring)
	{
		string = savestring.substring(0, savestring.indexOf(";"));
		img = new File(savestring.substring(savestring.indexOf(";")+1));
		if(img.exists())
			setImages(img);
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
	
	private void generateCode()
	{
		byte[] bytes = null;
		MessageDigest d = null;
		try
		{
			bytes = string.getBytes("UTF-8");
			d = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e){ e.printStackTrace(); }
		catch (UnsupportedEncodingException e){ e.printStackTrace(); }
		
		byte[] digest = d.digest(bytes);
		BigInteger big = new BigInteger(1,digest);
		code = big.toString(36);
		Pattern p = Pattern.compile("[0-9][a-z]([a-z])");
		Matcher m = p.matcher(code);
		ArrayList<String> groups = new ArrayList<String>();
		while(m.find())
		{
			groups.add(m.group());
		}
		for(String s : groups)
		{
			code = code.replaceAll(s, s.toUpperCase());
		}
		code = code.replaceAll(groups.get(1).toUpperCase(), " ");
		code = code.substring(0, 8);
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
		if(e.getButton()==MouseEvent.BUTTON2)
		{
			
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
}
