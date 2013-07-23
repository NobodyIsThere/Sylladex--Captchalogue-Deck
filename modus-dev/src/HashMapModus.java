import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sylladex.CaptchalogueCard;
import sylladex.FetchModus;
import sylladex.Main;
import sylladex.SylladexItem;
import util.Animation;
import util.Animation.AnimationType;
import util.Util;
import util.Util.OpenReason;

public class HashMapModus extends FetchModus implements ActionListener, ListSelectionListener
{
	private HashMap<String,Integer> map;
	
	private SylladexItem item;
	
	private JWindow window;
	private JPanel box;
	private CaptchalogueCard card;
	
	private JLabel collisionicon;
	
	private String string;
	private JLabel stringline;
	private String calculation;
	private String numbers;
	private int total;
	private int answer;
	
	private JTextField text;
	private DefaultListModel<String> model;
	
	private static final int LOADING = -1, DEFAULT = 0, CAPTCHALOGUING = 1, OPENING = 2;
	private int mode = LOADING;
	
	private static final int PREF_SELECTED_MAPPING = 0, PREF_DETECT_COLLISIONS = 1;
	
	public HashMapModus(Main m)
	{
		super(m);
	}
	
	@Override
	public void initialSettings()
	{
		settings.set_dock_text_image("modi/canon/hashmap/docktext.png");
		settings.set_card_image("modi/canon/hashmap/card.png");
		settings.set_card_back_image("modi/canon/hashmap/back.png");
		
		settings.set_modus_image("modi/canon/hashmap/modus.png");
		settings.set_name("Hashmap");
		settings.set_author("gumptiousCreator");
		
		settings.set_preferences_file("modi/prefs/hashmapprefs.txt");
		
		settings.set_background_color(255, 240, 0);
		settings.set_secondary_color(227, 203, 0);
		settings.set_text_color(149, 83, 255);
		
		settings.set_initial_card_number(10);
		settings.set_origin(-123, 120);
		
		settings.set_cards_draggable(false);
		settings.set_draw_empty_cards(true);
		settings.set_shade_inaccessible_cards(false);
	}
	
	@Override
	public void prepare()
	{
		// Exclamation mark icon
		collisionicon = new JLabel(Util.createImageIcon("modi/canon/hashmap/collision.gif"));
		collisionicon.setBounds(0,0,settings.get_card_width(),settings.get_card_height());
		
		// Set up the screen
		arrangeCards();
		refreshDock();
		
		// Preferences
		if (preferences.size() == 0)
		{
			preferences.add("C=2; V=1;.txt");
			preferences.add("false");
		}
		populatePreferencesPanel();
	}
	
	@Override
	public void ready()
	{
		arrangeCards();
		mode = DEFAULT;
	}
	
	@Override
	public Object[] getCardOrder()
	{
		return deck.getCards().toArray();
	}

	private void populatePreferencesPanel()
	{
		// We need absolute positioning.
		preferences_panel.setLayout(null);
		preferences_panel.setPreferredSize(new Dimension(270,300));
		
		JButton ejectbutton = new JButton("EJECT");
			ejectbutton.setActionCommand("hashmap_eject");
			ejectbutton.addActionListener(this);
			ejectbutton.setBounds(77,7,162,68);
			preferences_panel.add(ejectbutton);
		
		JLabel mappingslabel = new JLabel("hash functions");
			mappingslabel.setHorizontalAlignment(JLabel.CENTER);
			mappingslabel.setBounds(20,108,234,16);
			preferences_panel.add(mappingslabel);
			
		model = new DefaultListModel<String>();
			loadMappings(model);
			JList<String> mappinglist = new JList<String>(model);
			mappinglist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mappinglist.setSelectedValue(preferences.get(PREF_SELECTED_MAPPING).replaceAll("\\.txt", ""), true);
			mappinglist.addListSelectionListener(this);
			mappinglist.setVisibleRowCount(-1);
			JScrollPane scrollpane = new JScrollPane(mappinglist);
			scrollpane.setBounds(20,124,234,59);
			preferences_panel.add(scrollpane);
			
		text = new JTextField();
			text.setBounds(20,186,179,24);
			preferences_panel.add(text);
			
		JButton addbutton = new JButton("<html><font size=\"8px\">ADD</font>");
			addbutton.setBounds(201,185,57,25);
			addbutton.setActionCommand("hashmap_add_mapping");
			addbutton.addActionListener(this);
			preferences_panel.add(addbutton);
			
		JCheckBox detectcollisions = new JCheckBox("detect collisions");
			detectcollisions.setSelected(preferences.get(PREF_DETECT_COLLISIONS).equals("true"));
			detectcollisions.setActionCommand("hashmap_detect_collisions");
			detectcollisions.addActionListener(this);
			detectcollisions.setBounds(41,242,196,17);
			preferences_panel.add(detectcollisions);
			
		preferences_panel.validate();
	}
	
	private void loadMappings(DefaultListModel<String> model)
	{
		File dir = new File("modi/canon/hashmap/");
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				if(name.endsWith(".txt"))
				{
					return true;
				}
				return false;
			}
		};
		// Strip extension for list
		for(String name : dir.list(filter))
		{
			model.add(0, name.replaceAll(".txt", ""));
		}
	}

	private void createBox()
	{
		window = new JWindow();
		window.setLayout(null);
		window.setBounds(200,-124,255,410);
		window.setBackground(Util.COLOR_TRANSPARENT);
		
		box = new JPanel();
		box.setLayout(null);
		box.setBounds(0,0,239,124);
		
		JLayeredPane pane = new JLayeredPane();
		pane.setLayout(null);
		pane.setBounds(0, 0, 239, 124);
		box.add(pane);
		
		JLabel background = new JLabel(Util.createImageIcon("modi/canon/hashmap/selectionwindow.png"));
		background.setBounds(0,0,239,124);
		pane.setLayer(background, 0);
		pane.add(background);
		
		stringline = new JLabel(colour(string));
		stringline.setBounds(14,18,202,14);
		stringline.setHorizontalAlignment(JLabel.CENTER);
		stringline.setVerticalAlignment(JLabel.TOP);
		pane.setLayer(stringline, 1);
		pane.add(stringline);
		
		JLabel numberline = new JLabel(colour(numbers));
		numberline.setBounds(14,35,202,14);
		numberline.setHorizontalAlignment(JLabel.CENTER);
		numberline.setVerticalAlignment(JLabel.TOP);
		pane.setLayer(numberline, 1);
		pane.add(numberline);
		
		JLabel calculationline = new JLabel(calculation + " = " + total);
		calculationline.setBounds(14,62,202,14);
		calculationline.setHorizontalAlignment(JLabel.CENTER);
		pane.setLayer(calculationline, 1);
		pane.add(calculationline);
		
		JLabel finalline = new JLabel("%" + deck.getCards().size() + " =");
		finalline.setBounds(14,85,202,14);
		finalline.setHorizontalAlignment(JLabel.CENTER);
		pane.setLayer(finalline, 1);
		pane.add(finalline);
		
		JLabel answerline = new JLabel(new Integer(answer).toString());
		answerline.setBackground(new Color(0,0,0));
		answerline.setForeground(new Color(255,255,255));
		answerline.setOpaque(true);
		answerline.setBounds(190,79,30,30);
		answerline.setHorizontalAlignment(JLabel.CENTER);
		pane.setLayer(answerline, 1);
		pane.add(answerline);
		
		JLabel animation = new JLabel(Util.createImageIcon("modi/canon/hashmap/animation.gif"));
		animation.setBounds(0,0,239,124);
		pane.setLayer(animation, 2);
		pane.add(animation);
		
		window.add(box);
		window.setVisible(true);
	}
	
	private void loadMapping()
	{
		map = new HashMap<String, Integer>();
		map.clear();
		Scanner s = new Scanner("");
		try
		{
			s = new Scanner(new FileReader(new File("modi/canon/hashmap/" + preferences.get(PREF_SELECTED_MAPPING))));
			while(s.hasNextLine())
			{
				Scanner t = new Scanner(s.nextLine());
				t.useDelimiter(":");
				map.put(t.next(), Integer.parseInt(t.next()));
				t.close();
			}
		}
		catch (FileNotFoundException e){ e.printStackTrace(); }
		finally { s.close(); }
	}
	
	private String colour(String s)
	{
		String t = "<html>";
		for (int i=0; i<s.length(); i++)
		{
			// Can't create a String from a char directly, so concatenate with empty string.
			String character = s.charAt(i) + "";
			String value = "";
			if (map.get(character)!=null) { value = map.get(character).toString(); }
			// If no mapping for the character, perhaps we're looking at a number
			else if (map.get("c" + character) != null) { value = map.get("c" + character).toString(); }
			// Default colour
			String col = "000000";
			if (value!=null)
			{
				if (map.get("c" + value) != null)
				{
					// In this case, c + value = e.g. c3 so the colour is the mapping for this
					col = Integer.toHexString(map.get("c" + value));
				}
				else if (value.length() > 2)
				{
					// In this case, we already looked up the colour.
					col = Integer.toHexString(new Integer(value));
				}
			}
			if(character.equals("0")) { col="ffffff"; } // Colour unmapped characters white (invisible)
			// Change to HTML - note that we have to escape spaces.
			t = t.concat("<font face=\"Courier New\" color=\"#" + col + "\">" + character.replaceAll(" ", "&nbsp;") + "</font>");
		}
		t = t.concat("</html>");
		return t;
	}
	
	@Override
	public boolean captchalogue(SylladexItem item)
	{
		// The "accessible" thing is because this modus only supports adding one file
		// at a time (to allow time for the animation to complete).
		if (loading)
		{
			string = item.getName().toUpperCase();
			this.item = item;
			doWork();
			if (!card.isEmpty())
			{
				if (preferences.get(PREF_DETECT_COLLISIONS).equals("false"))
				{
					deck.open(card, OpenReason.MODUS_PUSH);
				}
				else
				{
					return false;
				}
			}
			deck.getCards().get(answer).setItem(item);
			return true;
		}
		
		try
		{
			if (mode == DEFAULT)
			{			
				mode = CAPTCHALOGUING;
				
				string = item.getName().toUpperCase();
				this.item = item;
				
				firstAnimation();
				
				if (preferences.get(PREF_DETECT_COLLISIONS).equals("true")
						&& !card.isEmpty())
				{
					return false;
				}
				
				return true;
			}
		}
		catch (Exception e) { e.printStackTrace(); }
		return false;
	}
	
	private void showSelectionWindow()
	{
		if (mode != DEFAULT) { return; }
		mode = OPENING;
		
		string = JOptionPane.showInputDialog("");
		if (string == null) { mode = DEFAULT; return; }
		string = string.toUpperCase();
		
		firstAnimation();
	}
	
	private void firstAnimation()
	{
		doWork();
		createBox();
		
		window.setAlwaysOnTop(true);
		deck.addAnimation(new Animation(box, new Point(0,244), AnimationType.MOVE, null, null));
		deck.addAnimation(new Animation(AnimationType.WAIT, 2000, null, null));
		deck.addAnimation(new Animation(card, new Point(card.getWidth(),card.getLocation().y),
												AnimationType.BOUNCE, null, "hashmap_first_animation"));
	}

	private void doWork()
	{
		// Sets the hashmap
		loadMapping();
		
		//Loop through string
		calculation = new String("");
		numbers = new String("");
		String longstring = new String("");
		total = 0;
		for (int i=0; i<string.length(); i++)
		{
			String s = string.charAt(i) + "";
			Integer value = map.get(s);
			if(value == null){ value = 0; }
			
			longstring = longstring.concat(" " + s);
			numbers = numbers.concat(" " + value.toString());
			calculation = calculation.concat(" + " + value.toString());
			total += value;
		}
		//Trim off the first characters
		string = longstring.substring(1);
		numbers = numbers.substring(1);
		calculation = calculation.substring(3);
		answer = total%deck.getCards().size();

		card = deck.getCards().get(answer);
	}

	private void eject()
	{
		for (CaptchalogueCard card : deck.getCards())
		{
			if (!card.isEmpty())
			{
				SylladexItem item = card.getItem();
				open(card, OpenReason.MODUS_PUSH);
				deck.eject(item);
			}
		}
	}
	
	@Override
	public void open(CaptchalogueCard card, OpenReason reason)
	{
		deck.setIcons(deck.getCards().toArray());
		card.setAccessible(false);
		deck.open(card, reason);
		arrangeCards();
	}

	public void addCard()
	{
		if (!deck.isEmpty())
		{
			int n = JOptionPane.showOptionDialog(preferences_panel,
					"ADDING CARDS WILL EJECT SYLLADEX. ARE YOU SURE?", "",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
					new Object[] {"Y", "N"}, "N");
			if(n==1)
			{
				return;
			}
			eject();
		}
		deck.addCard();
		refreshDock();
		arrangeCards();
	}
	
	private void arrangeCards()
	{
		for (int i=0; i<deck.getCards().size(); i++)
		{
			CaptchalogueCard card = deck.getCards().get(i);
			card.setLocation(new Point(0,41*i));
			card.setLayer(i);
			card.setVisible(true);
		}
	}
	
	@Override
	public void refreshDock()
	{
		foreground.removeAll();
		int startx = Util.SCREEN_SIZE.width/2 - deck.getCards().size()*25 - 10;
		
		int max = deck.getCards().size();
		if (max > Util.SCREEN_SIZE.width/50) { max = Util.SCREEN_SIZE.width/50; }
		
		for (int i=0; i<max; i++)
		{
			JLabel label = new JLabel("<HTML><FONT SIZE=1>" + i + "</FONT></HTML>");
			label.setBounds(startx + i*50, deck.getDockIconYPosition() - 10, 50, 10);
			label.setHorizontalAlignment(JLabel.CENTER);
			foreground.add(label);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("hashmap_box_up"))
		{
			window.removeAll();
			window.setVisible(false);
			window.setAlwaysOnTop(false);
			mode = DEFAULT;
			
			for (CaptchalogueCard card : deck.getCards())
			{
				if (!card.isEmpty())
				{ card.getForegroundPanel().removeAll(); }
			}
		}
		else if (e.getActionCommand().equals("hashmap_first_animation"))
		{
			CaptchalogueCard c = (CaptchalogueCard) ((Animation)e.getSource()).getAnimationTarget();
			
			if (mode == OPENING || preferences.get(PREF_DETECT_COLLISIONS).equals("false"))
			{
				if(!c.isEmpty()) { open(c, OpenReason.MODUS_PUSH); }
			}
			if (mode != OPENING)
			{
				if (c.isEmpty())
				{
					c.setItem(item);
					c.setAccessible(true);
					deck.setIcons(deck.getCards().toArray());
				}
				else
				{
					// Collision detected!
					c.getForegroundPanel().add(collisionicon);
				}
			}

			deck.addAnimation(new Animation(AnimationType.WAIT, 1000, null, null));
			deck.addAnimation(new Animation(c, new Point(0,c.getLocation().y), AnimationType.MOVE, null, null));
			deck.addAnimation(new Animation(box, new Point(0,0), AnimationType.MOVE, this, "hashmap_box_up"));
		}
		else if (e.getActionCommand().equals(Util.ACTION_CARD_MOUSE_ENTER))
		{
			CaptchalogueCard card = (CaptchalogueCard)e.getSource();
			Point finalposition = new Point(settings.get_card_width()-25, card.getLocation().y);
			card.setLocation(finalposition);
		}
		else if (e.getActionCommand().equals(Util.ACTION_CARD_MOUSE_EXIT))
		{
			CaptchalogueCard card = (CaptchalogueCard)e.getSource();
			Point finalposition = new Point(0, card.getLocation().y);
			card.setLocation(finalposition);
		}
		else if (e.getActionCommand().equals("hashmap_detect_collisions"))
		{
			// So convoluted! We have to use the Boolean class to convert it to a String.
			String value = new Boolean(((JCheckBox)e.getSource()).isSelected()).toString();
			preferences.set(PREF_DETECT_COLLISIONS, value);
		}
		else if (e.getActionCommand().equals("hashmap_eject"))
		{
			int n = JOptionPane.showOptionDialog(preferences_panel, "EJECT ALL ITEMS FROM SYLLADEX?", "", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"Y", "N"}, "N");
			if (n==0)
				eject();
		}
		else if (e.getActionCommand().equals("hashmap_add_mapping"))
		{
			String name = "new";
			if (text.getText().length()>15)
			{
				name = text.getText().substring(0, 15) + "...";
			}
			else { name = text.getText(); }
			
			try
			{
				FileWriter writer = new FileWriter(new File("modi/canon/hashmap/" + name + ".txt"));
				new File("modi/canon/hashmap/" + name + ".txt").createNewFile();
				BufferedWriter b = new BufferedWriter(writer);
				String s = text.getText().replaceAll("=", ":").replaceAll(";", System.getProperty("line.separator")).replaceAll(" ", "");
				b.write(s);
				b.close();
				model.add(0, name);
			}
			catch (IOException x){ x.printStackTrace(); }
		}
		else if (e.getActionCommand().equals(Util.ACTION_USER_DOCK_CLICK))
		{
			showSelectionWindow();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		String value = (String)((JList<String>)e.getSource()).getSelectedValue();
		int n = JOptionPane.showOptionDialog(preferences_panel, "CHANGING HASH FUNCTION WILL EJECT SYLLADEX. ARE YOU SURE?", "", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"Y", "N"}, "N");
		if(n==0)
		{
			eject();
			preferences.set(PREF_SELECTED_MAPPING, value + ".txt");
		}
	}
}
