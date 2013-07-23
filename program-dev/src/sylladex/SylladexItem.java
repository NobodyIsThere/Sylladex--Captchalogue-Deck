package sylladex;

import java.awt.Image;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.Util;

public class SylladexItem
{
	public static enum ItemType { FILE, IMAGE, STRING, WIDGET, CARD }
	/** Prefixes for save strings, to indicate the type of data that is stored. */
	public static final String FILE_PREFIX = "[FILE]", STRING_PREFIX = "[STRING]", IMAGE_PREFIX = "[IMAGE]", WIDGET_PREFIX = "[WIDGET]", CARD_PREFIX = "[CARD]";
	
	private Main m;
	
	private ItemType type;
	private Object contents;
	private int number = 1;
	private JLabel icon;
	private String name = "ITEM";
	
	private String save_string;
	
//	/**
//	 * Creates a new sylladex item from the given save string.
//	 * @param string - The string to load the item from.
//	 * @param m - The instance of Main to use.
//	 */
//	public SylladexItem(String string, Main m)
//	{
//		this.m = m;
//		
//		if(string.indexOf(NAME)!=-1)
//		{
//			name = string.substring(string.indexOf(NAME)+6);
//			string = string.substring(0,string.indexOf(NAME));
//		}
//		
//		if(string.startsWith(FILE_PREFIX))
//		{
//			string = string.substring(FILE_PREFIX.length());
//			string = string.replaceAll("http://", "");
//			String p = ""; if(System.getProperty("file.separator").equals("\\")) { p="\\"; }
//			string = string.replaceAll("\\\\", p + System.getProperty("file.separator"));
//			string = string.replaceAll("/", p + System.getProperty("file.separator"));
//				
//			File file = new File(string);
//			if(file.exists())
//			{
//				type = ItemType.FILE;
//				contents = file;
//			}
//			else
//			{
//				//file doesn't exist: interpret as string
//				type = ItemType.STRING;
//				string = STRING_PREFIX + string;
//			}
//		}
//		if(string.startsWith(IMAGE_PREFIX))
//		{
//			string = string.substring(IMAGE_PREFIX.length());
//			string = string.replaceAll("http://", "");
//			String p = ""; if(System.getProperty("file.separator").equals("\\")) { p="\\"; }
//			string = string.replaceAll("\\\\", p + System.getProperty("file.separator"));
//			string = string.replaceAll("/", p + System.getProperty("file.separator"));
//			
//			File file = new File(string);
//			if(file.exists())
//			{
//				try
//				{
//					Image image = ImageIO.read(file);
//					file.delete();
//					type = ItemType.IMAGE;
//					contents = image;
//				}
//				catch (IOException e){ type = ItemType.STRING; contents = file.getPath(); }
//			}
//			else
//			{
//				//file doesn't exist: interpret as string
//				string = STRING_PREFIX + string;
//			}
//		}
//		if(string.startsWith(STRING_PREFIX))
//		{
//			type = ItemType.STRING;
//			contents = string.substring(STRING_PREFIX.length()).replaceAll("SYLLADEX_NL", System.getProperty("line.separator"));
//		}
//		if(string.startsWith(WIDGET_PREFIX))
//		{
//			String cut = string.substring(WIDGET_PREFIX.length());
//			String path = cut.substring(0, cut.indexOf("[")-1);
//			Widget widget = m.loadWidget(new File(path));
//			widget.load(cut.substring(cut.indexOf("]")+1));
//			type = ItemType.WIDGET;
//			contents = widget;
//		}
//		
//		determineNameAndCode();
//		createIcon();
//	}
	
	/**
	 * Creates a new sylladex item containin the given object.
	 * @param name - The name of the item (not normally used).
	 * @param contents - The object to be captchalogued.
	 * @param m - The instance of Main to use.
	 */
	public SylladexItem(String name, Object contents, Main m)
	{
		this.m = m;
		
		if (contents instanceof File)
		{
			type = ItemType.FILE;
		}
		else if (contents instanceof Image)
		{
			type = ItemType.IMAGE;
		}
		else if (contents instanceof String)
		{
			type = ItemType.STRING;
		}
		else if (contents instanceof Widget)
		{
			type = ItemType.WIDGET;
		}
		this.contents = contents;
		this.name = name;
		
		createIcon();
	}
	
	/**
	 * Warning: this does not update the name, code or icon.
	 * @param contents - New contents.
	 */
	protected void setContents(Object contents)
	{
		this.contents = contents;
	}
	
	/**
	 * Sets the number of items contained in this card.
	 * @param number - New number.
	 */
	public void setNumber(int number)
	{
		this.number = number;
	}
	
	/**
	 * @return The number of items in this card.
	 */
	public int getNumber()
	{
		return number;
	}
	
	/**
	 * @return A panel for use on the sylladex card.
	 */
	public JPanel getPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setOpaque(false);
		
		JLabel cardicon = new JLabel();
		if(contents instanceof File)
		{
			Icon icon = Util.getIconFromFile((File)contents);
			cardicon = new JLabel(name);
			cardicon.setBounds(15*m.getModusSettings().get_card_width()/148,35*m.getModusSettings().get_card_height()/94,
								24*m.getModusSettings().get_card_width()/37,82*m.getModusSettings().get_card_height()/188);
			cardicon.setIcon(icon);
			cardicon.setHorizontalAlignment(JLabel.CENTER);
			cardicon.setVerticalAlignment(JLabel.TOP);
			cardicon.setVerticalTextPosition(JLabel.BOTTOM);
			cardicon.setHorizontalTextPosition(JLabel.CENTER);
		}
		else if(contents instanceof Image)
		{
			cardicon = new JLabel();
			cardicon.setBounds(15*m.getModusSettings().get_card_width()/148,60*m.getModusSettings().get_card_height()/188,
								24*m.getModusSettings().get_card_width()/37,100*m.getModusSettings().get_card_height()/188);
			Icon icon = Util.getSizedIcon((Image)contents, cardicon.getWidth(), cardicon.getHeight());
			cardicon.setIcon(icon);
			cardicon.setHorizontalAlignment(JLabel.CENTER);
			cardicon.setVerticalAlignment(JLabel.CENTER);
		}
		else if(contents instanceof String)
		{
			cardicon = new JLabel("<HTML>" + (String)contents + "</HTML>");
			cardicon.setBounds(15*m.getModusSettings().get_card_width()/148,60*m.getModusSettings().get_card_height()/188,
								24*m.getModusSettings().get_card_width()/37,100*m.getModusSettings().get_card_height()/188);
			cardicon.setVerticalTextPosition(JLabel.TOP);
		}
		else if(contents instanceof Widget)
		{
			JPanel widgetpanel = ((Widget)contents).getPanel();
			widgetpanel.setBounds(15*m.getModusSettings().get_card_width()/148,60*m.getModusSettings().get_card_height()/188,
									24*m.getModusSettings().get_card_width()/37,100*m.getModusSettings().get_card_height()/188);
			panel.add(widgetpanel);
			return panel;
		}
		cardicon.setOpaque(false);
		panel.add(cardicon);
		return panel;
	}

	/**
	 * @return A string from which the item may be loaded.
	 */
//	public String getSaveString()
//	{
//		if(contents instanceof File)
//		{
//			File file = (File)contents;
//			String path;
//			if(!m.getPreferences().usb_mode())
//				path = file.getPath();
//			else
//				path = "files" + System.getProperty("file.separator") + file.getName();
//			return FILE_PREFIX + path + NAME + name;
//		}
//		else if(contents instanceof String)
//		{
//			String savestring = ((String)contents).replace(System.getProperty("line.separator"), "SYLLADEX_NL");
//			System.out.println(savestring);
//			return STRING_PREFIX + savestring + NAME + name;
//		}
//		else if(contents instanceof Image)
//		{
//			Image image = (Image)contents;
//			if(image instanceof BufferedImage)
//			{
//				File f = null;
//				while(f == null ? true : f.exists()) //don't want to overwrite a previous image
//					f = new File("files" + System.getProperty("file.separator") + "captchalogued_image_" + new Double(Math.random()).toString().replaceAll("\\.", "") + ".png");
//				BufferedImage b = (BufferedImage)image;
//				try
//				{
//					ImageIO.write(b, "png", f);
//				}
//				catch (IOException e){ e.printStackTrace(); }
//				return IMAGE_PREFIX + f.getPath().substring(f.getPath().indexOf("files" + System.getProperty("file.separator") + "captchalogued_image_")) + NAME + name;
//			}
//		}
//		else if(contents instanceof Widget)
//		{
//			Widget widget = (Widget)contents;
//			return WIDGET_PREFIX + "widgets/" + widget.getClass().getName() + ".class[/WIDGET]" + widget.getSaveString() + NAME + name;
//		}
//		return null;
//	}
	
	private void createIcon()
	{
		if (contents instanceof File)
		{
			icon = new JLabel(Util.getIconFromFile((File)contents));
		}
		else if (contents instanceof Image)
		{
			icon = new JLabel(Util.getDockIcon((Image)contents));
		}
		else if (contents instanceof String)
		{
			icon = new JLabel((String)contents);
		}
		else if (contents instanceof Widget)
		{
			icon = ((Widget)contents).getDockIcon();
		}
		else
		{
			icon = new JLabel();
		}
	}
	
	/**
	 * @return The item type.
	 */
	public ItemType getType()
	{
		return type;
	}
	
	/**
	 * @return The File, Image, String etc. that this SylladexItem represents.
	 */
	public Object getContents()
	{
		return contents;
	}
	
	/**
	 * @return The name of the item.
	 */
	public String getName()
	{
		if (contents instanceof Widget) { return ((Widget) contents).getName(); }
		return name;
	}
	
	/**
	 * @return This item's CAPTCHA code.
	 */
	public String getCode()
	{
		if (contents instanceof Widget &&
				((Widget) contents).canonCaptchaCodeOverride() != null)
		{
			return ((Widget) contents).canonCaptchaCodeOverride();
		}
		return Alchemy.generateCode(getName());
	}

	public String getSaveString()
	{
		if (contents instanceof Widget)
		{
			return "captchalogued_widget_" + contents.getClass().getName() +
				Main.NAME + ((Widget) contents).getName() + Main.STATE +
				((Widget) contents).getSaveString();
		}
		return save_string;
	}
	
	public void setSaveString(String s)
	{
		save_string = s;
	}
	
	public JLabel getIcon()
	{
		if (contents instanceof Widget) { return ((Widget) contents).getDockIcon(); }
		return icon;
	}
}
