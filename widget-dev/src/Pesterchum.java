import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import sylladex.Widget;
import util.Bubble;
import util.Util;
import util.Util.OpenReason;

public class Pesterchum extends Widget implements ActionListener
{
	private File logs_folder;
	private String username;
	private ArrayList<String> logs;
	private ArrayList<Bubble> bubbles;
	
	private String fs = System.getProperty("file.separator");
	
	private Timer timer;
	
	@Override
	public void prepare()
	{
		setImages();
		
		if(Util.isWindows())
		{
			logs_folder = new File(System.getenv("LOCALAPPDATA") + "\\pesterchum\\logs");
		}
		else if(Util.isMac())
		{
			logs_folder = new File(System.getProperty("user.home") + "/Library/Application Support/Pesterchum/logs");
		}
		else if(Util.isLinux())
		{
			logs_folder = new File(System.getProperty("user.home") + "/.pesterchum/logs");
		}
		System.out.println(logs_folder);
		logs = new ArrayList<String>();
		bubbles = new ArrayList<Bubble>();
		
		timer = new Timer(5000, this);
	}
	
	private void setImages()
	{
		ImageIcon imageicon = Util.createImageIcon("widgets/Pesterchum/image.png");
		dock_icon = new JLabel(Util.getDockIcon(imageicon.getImage()));
	}

	@Override
	public void add()
	{
		username = JOptionPane.showInputDialog("Pesterchum username:");
		logs_folder = new File(logs_folder, username);
		//Check whether directory exists; ask for (relative) path if not.
		if (!logs_folder.exists())
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int decision = JFileChooser.CANCEL_OPTION;
			while (decision != JFileChooser.APPROVE_OPTION)
				decision = chooser.showDialog(null, "Select");
			logs_folder = chooser.getSelectedFile();
		}
		
		getFiles();
		timer.start();
	}

	@Override
	public void load(String string)
	{
		username = string;
		setImages();
		logs_folder = new File(logs_folder, username);
		if (!logs_folder.exists())
			logs_folder = new File(new File("."), username);
		getFiles();
		timer.start();
	}
	
	private void getFiles()
	{		
		for(String s : logs_folder.list())
		{
			File f = new File(logs_folder + fs + s + fs + "bbcode");
			
			for (String t : f.list())
			{
				String g = f.getPath() + fs + t;
				logs.add(g);
			}
		}
	}
	
	private void checkFiles()
	{
		for (String s : logs_folder.list())
		{
			File f = new File(logs_folder.getPath() + fs + s + fs + "bbcode");
			
			for (String t : f.list())
			{
				String g = f.getPath() + fs + t;
				
				boolean already_existed = false;
				for (String log : logs)
				{
					if (log.equals(g))
					{
						already_existed = true;
					}
				}
				if(!already_existed)
				{
					createBubbleForFile(new File(g));
					logs.add(g);
				}
			}
		}
	}
	
	private void check()
	{
		checkFiles();
	}
	
	private void createBubbleForFile(File f)
	{
		try
		{
			Scanner scanner = new Scanner(new FileReader(f));
			String line = scanner.nextLine();
			String from = line.substring(line.indexOf("--") + 3);
			from = from.substring(0, from.indexOf(" "));
			System.out.println(from);
			
			if(from!=username)
			{
				//New message!
				int begin = line.indexOf(from) + from.length() + 8;
				String colour = line.substring(begin, begin + 7);
				System.out.println(colour);
				Bubble b = new Bubble(deck, getDockIcon(), 0);
				JLabel text = new JLabel("<HTML><FONT COLOR=" + colour + ">" +
										contract(from) + "</FONT></HTML>");
				text.setBounds(0, 0, 80, 20);
				text.setHorizontalAlignment(JLabel.CENTER);
				text.setVerticalAlignment(JLabel.CENTER);
				b.getContents().add(text);
				bubbles.add(b);
				deck.showDock();
				arrangeBubbles();
			}
			scanner.close();
		}
		catch(Exception e){ e.printStackTrace(); }
	}
	
	private void arrangeBubbles()
	{
		for (Bubble b : bubbles)
		{
			if (!b.isShowing())
				bubbles.remove(b);
		}
		
		int length = bubbles.size();
		if (length == 0) { return; }
		
		for (int i = 0; i<length; i++)
		{
			Bubble b = bubbles.get(i);
			b.setOffset((int) (getDockIcon().getWidth() *
					((i+1)/(length+1) - 0.5)));
		}
	}
	
	private String contract(String s)
	{
		Pattern p = Pattern.compile("([A-Z])");
		Matcher m = p.matcher(s);
		String result = s.substring(0, 1);
		while(m.find() && result.length()<2)
		{
			result = result + m.group(1);
		}
		return result.toUpperCase();
	}

	@Override
	public void open(OpenReason reason)
	{
		cleanUp();
	}

	@Override
	public String getName()
	{
		return "Pesterchum";
	}

	@Override
	public String getSaveString()
	{
		cleanUp();
		return username;
	}
	
	private void cleanUp()
	{
		for(Bubble b : bubbles)
		{
			b.hide();
		}
		bubbles.clear();
		timer.stop();
	}
	
	public JPanel getPanel()
	{
		ImageIcon imageicon = Util.createImageIcon("widgets/Pesterchum/image.png");
		JPanel panel = new JPanel();
		panel.add(new JLabel(imageicon));
		panel.setOpaque(false);
		return panel;
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0){}
	@Override
	public void mouseExited(MouseEvent arg0){}
	@Override
	public void mousePressed(MouseEvent arg0){}
	@Override
	public void mouseReleased(MouseEvent arg0){}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource().equals(timer))
		{
			check();
		}
	}
}
