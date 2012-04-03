import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import sylladex.*;

public class Pesterchum extends Widget implements ActionListener
{
	private String path;
	private String username;
	private HashMap<File, Integer> conversations;
	private ArrayList<File> files;
	private ArrayList<Bubble> bubbles;
	
	private String fs = System.getProperty("file.separator");
	
	private Timer timer;
	
	@Override
	public void prepare()
	{
		ImageIcon imageicon = Main.createImageIcon("widgets/Pesterchum/image.png");
		dock_icon = new JLabel(Main.getDockIcon(imageicon.getImage()));
		
		if(Main.isWindows())
		{
			path = System.getenv("LOCALAPPDATA") + "\\pesterchum\\logs";
		}
		else if(Main.isMac())
		{
			path = System.getProperty("user.home") + "/Library/Application Support/Pesterchum/logs";
		}
		else if(Main.isLinux())
		{
			path = System.getProperty("user.home") + "/.pesterchum/logs";
		}
		System.out.println(path);
		conversations = new HashMap<File, Integer>();
		files = new ArrayList<File>();
		bubbles = new ArrayList<Bubble>();
		
		timer = new Timer(5000, this);
	}

	@Override
	public void add()
	{
		username = JOptionPane.showInputDialog("Pesterchum username:");
		getFiles();
	}

	@Override
	public void load(String string)
	{
		username = string;
		getFiles();
	}
	
	private void getFiles()
	{
		path = path + fs + username;
		File root = new File(path);
		System.out.println(root.getPath());
		
		for(String s : root.list())
		{
			File f = new File(path + fs + s + fs + "bbcode");
			System.out.println(f.getPath());
			files.add(f);
			setHashMap(f);
		}
		
		timer.start();
	}
	
	private void checkFiles()
	{
		File root = new File(path);
		if(root.list().length > files.size())
		{
			//TODO This is horrible, but it works.
			files.add(root);
			bubbles.add(new Bubble("...", "#000000"));
			arrangeBubbles();
		}
	}
	
	private void setHashMap(File f)
	{
		conversations.put(f, f.list().length);
	}
	
	private void check()
	{
		checkFiles();
		for(File f : files)
		{
			if(f.list().length > conversations.get(f))
			{
				//New log! Process at once.
				processFile(f);
				conversations.put(f, f.list().length);
			}
		}
	}
	
	private void processFile(File f)
	{
		String latest = "0";
		for(String s : f.list())
		{
			if(s.compareTo(latest)>0)
			{
				latest = s;
			}
		}
		File file = new File(f.getPath() + fs + latest);
		
		try
		{
			Scanner scanner = new Scanner(new FileReader(file));
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
				addBubble(contract(from), colour);
				m.showDock();
			}
		}
		catch(Exception e){ e.printStackTrace(); }
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
	
	private void addBubble(String from, String colour)
	{
		bubbles.add(new Bubble(from, colour));
		arrangeBubbles();
	}
	
	private void arrangeBubbles()
	{
		int n = bubbles.size() + 1;
		int offset = dock_icon.getLocation().x - 60;
		int i = 1;
		for(Bubble b : bubbles)
		{
			b.x = offset + (50*i/n);
			b.updatePosition();
			b.show();
			i++;
		}
	}
	
	private void removeBubble(Bubble b)
	{
		bubbles.remove(b);
		b.hide();
	}

	@Override
	public void open()
	{
		cleanUp();
	}

	@Override
	public String getString()
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
		timer = null;
	}
	
	public JPanel getPanel()
	{
		ImageIcon imageicon = Main.createImageIcon("widgets/Pesterchum/image.png");
		JPanel panel = new JPanel();
		panel.add(new JLabel(imageicon));
		panel.setOpaque(false);
		return panel;
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		m.getModus().open(card);
	}

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
	
	private class Bubble implements ActionListener, MouseListener
	{
		private JWindow w;
		private JLayeredPane panel;
		private JLabel bubble;
		public int x = 0;
		private int y = 0;
		private int offset = 0;
		private Timer t;
		private int counter = 0;
		
		public Bubble(String from, String colour)
		{
			w = new JWindow();
			w.setLayout(null);
			w.setSize(80, 101);
			w.setAlwaysOnTop(true);
			w.addMouseListener(this);
			Main.setTransparent(w);
			
			panel = new JLayeredPane();
			panel.setBounds(0,0,80,99);
			panel.setLayout(null);
			panel.setOpaque(false);
			w.add(panel);
			
			String path = "widgets/Pesterchum/bubble_top.png"; y = 70; int texty = 47;
			if(!m.getPreferences().top()){ path = "widgets/Pesterchum/bubble.png"; y = m.getScreenSize().height - 99 - 70; texty = 30; }
			bubble = new JLabel(Main.createImageIcon(path));
			JLabel name = new JLabel("<HTML><font color=\"" + colour + "\">" + from + "</font></HTML>");
			
			bubble.setBounds(0,0,80,99);
			bubble.setOpaque(false);
			panel.setLayer(bubble, 0);
			panel.add(bubble);
			
			name.setBounds(0,texty,80,20);
			name.setHorizontalAlignment(JLabel.CENTER);
			panel.setLayer(name, 1);
			panel.add(name);
			
			w.setLocation(x, y);
			
			t = new Timer(100, this);
			t.start();
		}
		
		public void show()
		{
			w.setVisible(true);
		}
		public void hide()
		{
			w.setVisible(false);
		}
		
		public void updatePosition()
		{
			String path = "widgets/Pesterchum/bubble_top.png"; y = 70;
			if(!m.getPreferences().top()){ path = "widgets/Pesterchum/bubble.png"; y = m.getScreenSize().height - 99 - 70; }
			bubble.setIcon(Main.createImageIcon(path));
			
			w.setLocation(x, y);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource().equals(t))
			{
				if(counter<2)
				{
					offset++;
					counter++;
				}
				else if(counter>=2)
				{
					offset--;
					counter++;
				}
				if(counter>3)
				{
					counter = 0;
				}
			}
			panel.setBounds(0, offset, 80, 99);
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			removeBubble(this);
		}

		@Override
		public void mouseEntered(MouseEvent arg0){}
		@Override
		public void mouseExited(MouseEvent arg0){}
		@Override
		public void mousePressed(MouseEvent arg0){}
		@Override
		public void mouseReleased(MouseEvent arg0){}
	}
}
