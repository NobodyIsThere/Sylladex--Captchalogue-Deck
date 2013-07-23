package util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

import sun.awt.shell.ShellFolder;
import sylladex.SylladexItem;
import sylladex.Widget;

public final class Util
{
	//Constants
	public static final Color COLOR_TRANSPARENT = new Color(255, 255, 255, 0);
	public static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
	
	public static enum OpenReason
	{
		USER_DEFAULT, USER_KEEP, USER_EJECT, MODUS_DEFAULT, MODUS_PUSH
	}
	public static final String ACTION_CARD_MOUSE_ENTER = "card_mouse_enter",
			ACTION_CARD_MOUSE_EXIT = "card_mouse_exit",
			ACTION_ANIMATION_COMPLETE = "animation_complete",
			ACTION_USER_DOCK_CLICK = "user_dock_click";
	
	/**
	 * Creates an ImageIcon from the file at the specified path.
	 * @param path - the path of the file, relative to SDECK.jar.
	 * @return An ImageIcon created from the file. If it fails to create an ImageIcon, null is returned.
	 */
	public static ImageIcon createImageIcon(String path)
	{
		java.net.URL url = null;
		ImageIcon icon = null;
		try
		{
			url = new File(path).toURI().toURL();
			Image image = Toolkit.getDefaultToolkit().getImage(url);
			icon = new ImageIcon(image);
		}
		catch (MalformedURLException e)
		{
			JOptionPane.showMessageDialog(null, "Error!\n" + e.getLocalizedMessage());
		}
		return icon;
	}
	
	/**
	 * Creates a JLabel based on the contents of the SylladexItem. This can be used to create a dock icon from a SylladexItem.
	 * @param item - The item to use.
	 * @return A JLabel for use on the dock.
	 */
	public static JLabel getIconLabelFromItem(SylladexItem item)
	{
		Object o = item.getContents();
		if(o instanceof File)
		{
			return new JLabel(getIconFromFile((File)o));
		}
		else if(o instanceof String)
		{
			return new JLabel((String)o);
		}
		else if(o instanceof Image)
		{
			return new JLabel(getDockIcon((Image)o));
		}
		else if(o instanceof Widget)
		{
			return ((Widget)o).getDockIcon();
		}
		return null;
	}
	
	/** Creates an Icon given a file.
	 * 
	 * @param file - the file to use.
	 * @return The default system icon for the file.
	 */
	public static Icon getIconFromFile(File file)
	{
		ShellFolder shellFolder;
		try
		{
			shellFolder = ShellFolder.getShellFolder(file);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			shellFolder = null;
		}
		if(shellFolder!=null && isWindows())
		{
			Icon icon = new ImageIcon(shellFolder.getIcon(true));
			return icon;
		}
		
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
		return icon;
	}
	
	/**
	 * Creates an icon for use in the dock, given an image.
	 * @param image - The image to use.
	 * @return An Icon suitable for use in the dock.
	 */
	public static Icon getDockIcon(Image image)
	{
		return getSizedIcon(image, 32, 32);
	}
	
	/**
	 * Returns a JPanel containing the specified image in a JLabel.
	 * The panel is the size of the card, while the label is sized and positioned to fit the sylladex card.
	 * @param image - The image to use.
	 * @param card_width - Width of a sylladex card.
	 * @param card_height - Height of a sylladex card.
	 * @return An Icon suitable for use on a card.
	 */
	public static JPanel getCardPanelFromImage(Image image, int card_width, int card_height)
	{
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(null);
		JLabel image_label = new JLabel(getSizedIcon(image, 24*card_width/37, 100*card_height/188));
		image_label.setBounds(0, 0, 24*card_width/37, 100*card_height/188);
		image_label.setOpaque(false);
		panel.add(image_label);
		return panel;
	}
	
	/**
	 * Resizes an image, and returns the resulting image as an Icon. The image retains its original aspect ratio.
	 * @param image - The image to resize.
	 * @param width - The final width of the image.
	 * @param height - The final height of the image.
	 * @return The resized image as an Icon.
	 */
	public static Icon getSizedIcon(Image image, int width, int height)
	{
		ImageIcon icon = new ImageIcon(image);
		if(icon.getIconWidth()>icon.getIconHeight())
		{
			float ratio = new Float(width)/new Float(icon.getIconWidth());
			if(ratio==0){ ratio = 0.1f; }
			return new ImageIcon(image.getScaledInstance(width, Math.round(icon.getIconHeight()*ratio), Image.SCALE_SMOOTH));
		}
		float ratio = new Float(height)/new Float(icon.getIconHeight()+0.1f);
		if(ratio==0){ ratio = 0.1f; }
		return new ImageIcon(image.getScaledInstance(Math.round(icon.getIconWidth()*ratio), height, Image.SCALE_SMOOTH));
	}
	
	public static void error(String string)
	{
		JOptionPane.showMessageDialog(null, string, "Sylladex", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Installs the specified .fetchmodus file.
	 */
	public static void installFetchModus(File fetchmodus)
	{
		Zip.unzipFile(fetchmodus, new File("modi"));
	}
	
	/**
	 * Installs the specified .widget file.
	 */
	public static void installWidget(File widget)
	{
		Zip.unzipFile(widget, new File("widgets"));
	}
	
	/**
	 * @return True if the system is running Windows; false otherwise.
	 */
	public static boolean isWindows()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("win") >= 0;
	}
	
	/**
	 * @return True if the system is running Mac OS; false otherwise.
	 */
	public static boolean isMac()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return os.indexOf("mac") >= 0;
	}
	
	/**
	 * @return True if the system is running Linux; false otherwise.
	 */
	public static boolean isLinux()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("nix") >= 0) || (os.indexOf("nux") >=0);
	}
}
