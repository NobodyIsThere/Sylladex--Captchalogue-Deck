import sylladex.*;
import sylladex.Animation.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class HashMapModus extends FetchModus implements ActionListener, ListSelectionListener
{
	private HashMap<String,Integer> map;
	
	private boolean accessible = true;
	
	private SylladexItem item;
	
	private JWindow window;
	private JLayeredPane box;
	private SylladexCard card;
	
	private JLabel collisionicon;
	
	private String string;
	private JLabel stringline;
	private String calculation;
	private String numbers;
	private int total;
	private int answer;
	
	private JTextField text;
	private DefaultListModel model;
	
	public HashMapModus(Main m)
	{
		this.m = m;
		image_background_top = "modi/hashmap/dockbg_top.png";
		image_background_bottom = "modi/hashmap/dockbg.png";
		image_text = "modi/hashmap/docktext.png";
		image_card = "modi/hashmap/card.png";
		image_card_back = "modi/hashmap/back.png";
		image_dock_card = "modi/global/dockcard.png";
		
		info_image = "modi/hashmap/modus.png";
		info_name = "Hashmap";
		info_author = "gumptiousCreator";
		
		item_file = "modi/items/hashmap.txt";
		prefs_file = "modi/prefs/hashmapprefs.txt";
		
		color_background = new Color(255, 255, 0);
		
		startcards = 10;
		origin = new Point(-123,120);
		draw_default_dock_icons = true;
		draw_empty_cards = true;
		shade_inaccessible_cards = false;
		
		draggable_cards = false;
		
		card_width = 148;
		card_height = 188;
		
		icons = new ArrayList<JLabel>();
	}
	
	public void prepare()
	{
		// Exclamation mark icon
		collisionicon = new JLabel(Main.createImageIcon("modi/hashmap/collision.gif"));
		collisionicon.setBounds(0,0,card_width,card_height);
		
		// Fill icons with blank labels
		if(icons.size()<m.getCards().size())
		{
			for(int i=0; i<m.getCards().size(); i++)
			{
				icons.add(new JLabel(""));
			}
		}
		
		// Set up the screen
		arrangeCards();
		JLabel numbers = new JLabel(Main.createImageIcon("modi/hashmap/numbers.png"));
		numbers.setBounds((m.getScreenSize().width-466)/2,m.getDockIconYPosition()-8,453,6);
		foreground.add(numbers);
		foreground.repaint();
		
		// Load items and create prefs panel
		addLoadedItems();
		populatePreferencesPanel();
	}
	
	private void addLoadedItems()
	{
		int i = 0;
		for(String string : items)
		{
			if(!string.equals(""))
			{
				SylladexCard card = m.getCards().get(i);
				SylladexItem item = new SylladexItem(string, m);
				card.setItem(item);
				card.setAccessible(true);
				JLabel icon = m.getIconLabelFromItem(item);
				icons.set(i, icon);
				card.setIcon(icon);
				m.setIcons(icons);
			}
			i++;
		}
	}

	public ArrayList<String> getItems()
	{
		ArrayList<String> items = new ArrayList<String>();
		for(SylladexCard card : m.getCards())
		{
			if(card.getItem()!=null)
				items.add(m.getCards().indexOf(card), card.getItem().getSaveString());
			else
				items.add("");
		}
		return items;
	}
	
	enum PrefLabels
	{
		SELECTED_MAPPING (0),
		DETECT_COLLISIONS (1);
		
		public int index;
		PrefLabels(int i)
		{ index = i; }
	}

	private void populatePreferencesPanel()
	{
		// We need absolute positioning.
		preferences_panel.setLayout(null);
		preferences_panel.setPreferredSize(new Dimension(270,300));
		
		JButton ejectbutton = new JButton("EJECT");
			ejectbutton.setActionCommand("eject");
			ejectbutton.addActionListener(this);
			ejectbutton.setBounds(77,7,162,68);
			preferences_panel.add(ejectbutton);
		
		JLabel mappingslabel = new JLabel("hash functions");
			mappingslabel.setHorizontalAlignment(JLabel.CENTER);
			mappingslabel.setBounds(20,108,234,16);
			preferences_panel.add(mappingslabel);
			
		model = new DefaultListModel();
			loadMappings(model);
			JList mappinglist = new JList(model);
			mappinglist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			mappinglist.setSelectedValue(preferences.get(PrefLabels.SELECTED_MAPPING.index).replaceAll("\\.txt", ""), true);
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
			addbutton.setActionCommand("add mapping");
			addbutton.addActionListener(this);
			preferences_panel.add(addbutton);
			
		JCheckBox detectcollisions = new JCheckBox("detect collisions");
			detectcollisions.setSelected(preferences.get(PrefLabels.DETECT_COLLISIONS.index).equals("true"));
			detectcollisions.setActionCommand("detect collisions");
			detectcollisions.addActionListener(this);
			detectcollisions.setBounds(41,242,196,17);
			preferences_panel.add(detectcollisions);
			
		preferences_panel.validate();
	}
	
	private void loadMappings(DefaultListModel model)
	{
		File dir = new File("modi/hashmap/");
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
		window.setAlwaysOnTop(true);
		Main.setTransparent(window);
		
		box = new JLayeredPane();
		box.setLayout(null);
		box.setBounds(0,0,239,124);
		
		JLabel background = new JLabel(Main.createImageIcon("modi/hashmap/selectionwindow.png"));
		background.setBounds(0,0,239,124);
		box.setLayer(background, 0);
		box.add(background);
		
		stringline = new JLabel(colour(string));
		stringline.setBounds(14,18,202,14);
		stringline.setHorizontalAlignment(JLabel.CENTER);
		stringline.setVerticalAlignment(JLabel.TOP);
		box.setLayer(stringline, 1);
		box.add(stringline);
		
		JLabel numberline = new JLabel(colour(numbers));
		numberline.setBounds(14,35,202,14);
		numberline.setHorizontalAlignment(JLabel.CENTER);
		numberline.setVerticalAlignment(JLabel.TOP);
		box.setLayer(numberline, 1);
		box.add(numberline);
		
		JLabel calculationline = new JLabel(calculation + " = " + total);
		calculationline.setBounds(14,62,202,14);
		calculationline.setHorizontalAlignment(JLabel.CENTER);
		box.setLayer(calculationline, 1);
		box.add(calculationline);
		
		JLabel finalline = new JLabel("%" + m.getCards().size() + " =");
		finalline.setBounds(14,85,202,14);
		finalline.setHorizontalAlignment(JLabel.CENTER);
		box.setLayer(finalline, 1);
		box.add(finalline);
		
		JLabel answerline = new JLabel(new Integer(answer).toString());
		answerline.setBackground(new Color(0,0,0));
		answerline.setForeground(new Color(255,255,255));
		answerline.setOpaque(true);
		answerline.setBounds(190,79,30,30);
		answerline.setHorizontalAlignment(JLabel.CENTER);
		box.setLayer(answerline, 1);
		box.add(answerline);
		
		JLabel animation = new JLabel(Main.createImageIcon("modi/hashmap/animation.gif"));
		animation.setBounds(0,0,239,124);
		box.setLayer(animation, 2);
		box.add(animation);
		
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
			s = new Scanner(new FileReader(new File("modi/hashmap/" + preferences.get(PrefLabels.SELECTED_MAPPING.index))));
			while(s.hasNextLine())
			{
				Scanner t = new Scanner(s.nextLine());
				t.useDelimiter(":");
				map.put(t.next(), Integer.parseInt(t.next()));
			}
		}
		catch (FileNotFoundException e){ e.printStackTrace(); }
		finally { s.close(); }
	}
	
	private String colour(String s)
	{
		String t = "<html>";
		for(int i=0; i<s.length(); i++)
		{
			// Can't create a String from a char, directly, so concatenate with empty string.
			String character = s.charAt(i) + "";
			String value = "";
			if(map.get(character)!=null) { value = map.get(character).toString(); }
			// If no mapping for the character, perhaps we're looking at a number
			else if(map.get("c" + character)!=null) { value = map.get("c" + character).toString(); }
			// Default colour
			String col = "000000";
			if(value!=null)
			{
				if(map.get("c" + value)!=null)
				{
					// In this case, c+value = e.g. c3 so the colour is the mapping for this
					col = Integer.toHexString(map.get("c" + value));
				}
				else if(value.length()>2)
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
	public void addGenericItem(Object o)
	{
		// The "accessible" thing is because this modus only supports adding one file
		// at a time (to allow time for the animation to complete).
		try
		{
			if(accessible)
			{
				accessible = false;
				
				if(o instanceof Image)
				{
					String name = JOptionPane.showInputDialog("Enter a name for the item:");
					item = new SylladexItem(name, o, m);
				}
				else
				{
					item = new SylladexItem("ITEM", o, m);
				}
				
				string = item.getName().toUpperCase();
				
				doWork();
				
				card = m.getCards().get(answer);
				Animation a3 = new Animation(card, new Point(card.getWidth(),card.getPosition().y), AnimationType.BOUNCE, this, "add animation");
				Animation a2 = new Animation(AnimationType.WAIT, 2000, a3, "run");
				new Animation(box, new Point(0,244), AnimationType.MOVE, a2, "run").run();
			}
		}catch(Exception e){e.printStackTrace();}
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
		for(int i=0; i<string.length(); i++)
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
		answer = total%m.getCards().size();
		
		createBox();
	}

	private void eject()
	{
		for(SylladexCard card : m.getCards())
		{
			if(!card.isEmpty())
				open(card);
		}
	}
	
	@Override
	public void open(SylladexCard card)
	{
		icons.set(icons.indexOf(card.getIcon()), new JLabel(""));
		m.setIcons(icons);
		card.setAccessible(false);
		m.open(card);
		arrangeCards();
	}

	// Don't support adding cards
	public void addCard(){}
	
	@Override
	public void showSelectionWindow()
	{
		if(accessible==false){ return; }
		accessible = false;
		
		string = JOptionPane.showInputDialog("");
		if(string==null){ accessible=true; return; }
		string = string.toUpperCase();
		
		doWork();
		
		SylladexCard card = m.getCards().get(answer);
		Animation a3 = new Animation(card, new Point(card.getWidth(),card.getPosition().y), AnimationType.BOUNCE, this, "open animation");
		Animation a2 = new Animation(AnimationType.WAIT, 2000, a3, "run");
		new Animation(box, new Point(0,244), AnimationType.MOVE, a2, "run").run();
	}

	private void arrangeCards()
	{
		for(int i=0; i<m.getCards().size(); i++)
		{
			SylladexCard card = m.getCards().get(i);
			card.setPosition(new Point(0,41*i));
			card.setLayer(i);
		}
		m.setCardHolderSize(card_width*2 + 5, m.getScreenSize().height);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("box up"))
		{
			window.removeAll();
			window.setVisible(false);
			accessible = true;
			
			for(SylladexCard card : m.getCards())
			{
				if(!card.isEmpty())
				{ card.getForeground().removeAll(); }
			}
		}
		else if(e.getActionCommand().equals("add animation"))
		{
			SylladexCard c = ((Animation)e.getSource()).getCard();
			
			if(preferences.get(PrefLabels.DETECT_COLLISIONS.index).equals("false"))
			{
				if(!c.isEmpty()) { open(c); }
			}
			if(c.isEmpty())
			{
				c.setItem(item);
				c.setAccessible(true);
				JLabel icon = m.getIconLabelFromItem(item);
				icons.set(answer, icon);
				c.setIcon(icon);
				m.setIcons(icons);
			}
			else
			{
				// Collision detected!
				c.getForeground().add(collisionicon);
			}

			Animation a3 = new Animation(box, new Point(0,0), AnimationType.MOVE, this, "box up");
			Animation a2 = new Animation(c, new Point(0,c.getPosition().y), AnimationType.MOVE, a3, "run");
			new Animation(AnimationType.WAIT, 1000, a2, "run").run();
		}
		else if(e.getActionCommand().equals("open animation"))
		{
			
			SylladexCard c = ((Animation)e.getSource()).getCard();
			if(!c.isEmpty())
				open(c);
			
			Animation a3 = new Animation(box, new Point(0,0), AnimationType.MOVE, this, "box up");
			Animation a2 = new Animation(c, new Point(0,c.getPosition().y), AnimationType.MOVE, a3, "run");
			new Animation(AnimationType.WAIT, 1000, a2, "run").run();
		}
		else if(e.getActionCommand().equals("card mouse enter"))
		{
			SylladexCard card = (SylladexCard)e.getSource();
			card.setPosition(new Point(card_width-25, card.getPosition().y));
		}
		else if(e.getActionCommand().equals("card mouse exit"))
		{
			SylladexCard card = (SylladexCard)e.getSource();
			card.setPosition(new Point(0, card.getPosition().y));
		}
		else if(e.getActionCommand().equals("detect collisions"))
		{
			// So convoluted! We have to use the wrapper class to convert it to a String.
			String value = new Boolean(((JCheckBox)e.getSource()).isSelected()).toString();
			preferences.set(PrefLabels.DETECT_COLLISIONS.index, value);
		}
		else if(e.getActionCommand().equals("eject"))
		{
			int n = JOptionPane.showOptionDialog(preferences_panel, "EJECT ALL ITEMS FROM SYLLADEX?", "", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"Y", "N"}, "N");
			if(n==0)
				eject();
		}
		else if(e.getActionCommand().equals("add mapping"))
		{
			String name = "new";
			if(text.getText().length()>15)
			{
				name = text.getText().substring(0, 15) + "...";
			}
			else { name = text.getText(); }
			
			try
			{
				FileWriter writer = new FileWriter(new File("modi/hashmap/" + name + ".txt"));
				new File("modi/hashmap/" + name + ".txt").createNewFile();
				BufferedWriter b = new BufferedWriter(writer);
				
				String s = text.getText().replaceAll("=", ":").replaceAll(";", System.getProperty("line.separator")).replaceAll(" ", "");
				b.write(s);
				b.close();
				model.add(0, name);
			}
			catch (IOException e1){ e1.printStackTrace(); }
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		String value = (String)((JList)e.getSource()).getSelectedValue();
		int n = JOptionPane.showOptionDialog(preferences_panel, "CHANGING HASH FUNCTION WILL EJECT SYLLADEX. ARE YOU SURE?", "", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"Y", "N"}, "N");
		if(n==0)
		{
			eject();
			preferences.set(PrefLabels.SELECTED_MAPPING.index, value + ".txt");
		}
	}
}
