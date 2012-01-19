package sylladex;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SylladexItem
{
	public static enum ItemType { FILE, IMAGE, STRING, WIDGET, CARD }
	/** Prefixes for save strings, to indicate the type of data that is stored. */
	public static final String FILE_PREFIX = "[FILE]", STRING_PREFIX = "[STRING]", IMAGE_PREFIX = "[IMAGE]", WIDGET_PREFIX = "[WIDGET]", CARD_PREFIX = "[CARD]", NAME = "[NAME]";
	
	private Main m;
	
	private ItemType type;
	private Object contents;
	private String name = "ITEM";
	private String code;
	
	private JPanel panel;
	
	public SylladexItem(String string, Main m)
	{
		this.m = m;
		
		if(string.indexOf(NAME)!=-1)
		{
			name = string.substring(string.indexOf(NAME)+6);
			string = string.substring(0,string.indexOf(NAME));
		}
		
		if(string.startsWith(FILE_PREFIX))
		{
			string = string.substring(FILE_PREFIX.length());
			string = string.replaceAll("http://", "");
			String p = ""; if(System.getProperty("file.separator").equals("\\")) { p="\\"; }
			string = string.replaceAll("\\\\", p + System.getProperty("file.separator"));
			string = string.replaceAll("/", p + System.getProperty("file.separator"));
				
			File file = new File(string);
			if(file.exists())
			{
				type = ItemType.FILE;
				contents = file;
			}
			else
			{
				//file doesn't exist: interpret as string
				type = ItemType.STRING;
				string = STRING_PREFIX + string;
			}
		}
		if(string.startsWith(IMAGE_PREFIX))
		{
			string = string.substring(IMAGE_PREFIX.length());
			string = string.replaceAll("http://", "");
			String p = ""; if(System.getProperty("file.separator").equals("\\")) { p="\\"; }
			string = string.replaceAll("\\\\", p + System.getProperty("file.separator"));
			string = string.replaceAll("/", p + System.getProperty("file.separator"));
			
			File file = new File(string);
			if(file.exists())
			{
				try
				{
					Image image = ImageIO.read(file);
					file.delete();
					type = ItemType.IMAGE;
					contents = image;
				}
				catch (IOException e){ type = ItemType.STRING; contents = file.getPath(); }
			}
			else
			{
				//file doesn't exist: interpret as string
				string = STRING_PREFIX + string;
			}
		}
		if(string.startsWith(STRING_PREFIX))
		{
			type = ItemType.STRING;
			contents = string.substring(STRING_PREFIX.length()).replaceAll("SYLLADEX_NL", System.getProperty("line.separator"));
		}
		if(string.startsWith(WIDGET_PREFIX))
		{
			String cut = string.substring(WIDGET_PREFIX.length());
			String path = cut.substring(0, cut.indexOf("[")-1);
			Widget widget = m.loadWidget(new File(path));
			widget.load(cut.substring(cut.indexOf("]")+1));
			type = ItemType.WIDGET;
			contents = widget;
		}
		determineNameAndCode();
	}
	
	public SylladexItem(String name, Object contents, Main m)
	{
		this.m = m;
		
		if(contents instanceof File)
		{
			type = ItemType.FILE;
		}
		else if(contents instanceof Image)
		{
			type = ItemType.IMAGE;
			if(m.getPreferences().name_items())
			{
				name = JOptionPane.showInputDialog("Item name:");
			}
		}
		else if(contents instanceof String)
		{
			type = ItemType.STRING;
		}
		else if(contents instanceof Widget)
		{
			type = ItemType.WIDGET;
		}
		this.contents = contents;
		this.name = name;
		determineNameAndCode();
	}
	
	public JPanel getPanel()
	{
		panel = new JPanel();
		panel.setLayout(null);
		panel.setOpaque(false);
		
		JLabel cardicon = new JLabel();
		if(contents instanceof File)
		{
			Icon icon = Main.getIconFromFile((File)contents);
			cardicon = new JLabel(name);
			cardicon.setBounds(15*m.getModus().getCardWidth()/148,35*m.getModus().getCardHeight()/94,
								24*m.getModus().getCardWidth()/37,82*m.getModus().getCardHeight()/188);
			cardicon.setIcon(icon);
			cardicon.setHorizontalAlignment(JLabel.CENTER);
			cardicon.setVerticalAlignment(JLabel.TOP);
			cardicon.setVerticalTextPosition(JLabel.BOTTOM);
			cardicon.setHorizontalTextPosition(JLabel.CENTER);
		}
		else if(contents instanceof Image)
		{
			cardicon = new JLabel();
			cardicon.setBounds(15*m.getModus().getCardWidth()/148,60*m.getModus().getCardHeight()/188,
								24*m.getModus().getCardWidth()/37,100*m.getModus().getCardHeight()/188);
			Icon icon = Main.getSizedIcon((Image)contents, cardicon.getWidth(), cardicon.getHeight());
			cardicon.setIcon(icon);
			cardicon.setHorizontalAlignment(JLabel.CENTER);
			cardicon.setVerticalAlignment(JLabel.CENTER);
		}
		else if(contents instanceof String)
		{
			cardicon = new JLabel("<HTML>" + (String)contents + "</HTML>");
			cardicon.setBounds(15*m.getModus().getCardWidth()/148,60*m.getModus().getCardHeight()/188,
								24*m.getModus().getCardWidth()/37,100*m.getModus().getCardHeight()/188);
		}
		else if(contents instanceof Widget)
		{
			JPanel widgetpanel = ((Widget)contents).getPanel();
			widgetpanel.setBounds(15*m.getModus().getCardWidth()/148,60*m.getModus().getCardHeight()/188,
									24*m.getModus().getCardWidth()/37,100*m.getModus().getCardHeight()/188);
			panel.add(widgetpanel);
			return panel;
		}
		cardicon.setOpaque(false);
		panel.add(cardicon);
		return panel;
	}

	public String getSaveString()
	{
		if(contents instanceof File)
		{
			File file = (File)contents;
			String path;
			if(!m.getPreferences().usb_mode())
				path = file.getPath();
			else
				path = "files" + System.getProperty("file.separator") + file.getName();
			return FILE_PREFIX + path + NAME + name;
		}
		else if(contents instanceof String)
		{
			return STRING_PREFIX + ((String)contents).replaceAll(System.getProperty("line.separator"), "SYLLADEX_NL") + NAME + name;
		}
		else if(contents instanceof Image)
		{
			Image image = (Image)contents;
			if(image instanceof BufferedImage)
			{
				File f = null;
				while(f == null ? true : f.exists()) //don't want to overwrite a previous image
					f = new File("files" + System.getProperty("file.separator") + "captchalogued_image_" + new Double(Math.random()).toString().replaceAll("\\.", "") + ".png");
				BufferedImage b = (BufferedImage)image;
				try
				{
					ImageIO.write(b, "png", f);
				}
				catch (IOException e){ e.printStackTrace(); }
				return IMAGE_PREFIX + f.getPath().substring(f.getPath().indexOf("files" + System.getProperty("file.separator") + "captchalogued_image_")) + NAME + name;
			}
		}
		else if(contents instanceof Widget)
		{
			Widget widget = (Widget)contents;
			return WIDGET_PREFIX + "widgets/" + widget.getClass().getName() + ".class[/WIDGET]" + widget.getSaveString() + NAME + name;
		}
		return null;
	}
	
	private void determineNameAndCode()
	{
		if(contents instanceof File){ name = ((File)contents).getName(); }
		if(contents instanceof String){ name = ((String)contents); }
		if(contents instanceof Widget){ name = ((Widget)contents).getString(); }
		code = Alchemy.generateCode(name);
	}
	
	public ItemType getType()
	{
		return type;
	}
	
	public Object getContents()
	{
		return contents;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getCode()
	{
		return code;
	}
}
