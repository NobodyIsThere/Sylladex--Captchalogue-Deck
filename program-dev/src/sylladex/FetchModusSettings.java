package sylladex;

import java.awt.Color;
import java.awt.Point;

public class FetchModusSettings
{
	private int card_height = 188;
	private int card_width = 148;
	
	private Color color_background = new Color(255,255,255);
	
	private boolean draggable_cards = true;
	private boolean draw_default_dock_icons = true;
	private boolean draw_empty_cards = false;
	
	private String image_background_bottom = "modi/stack/dockbg.png";
	private String image_background_top = "modi/stack/dockbg_top.png";
	private String image_card = "modi/stack/card.png";
	private String image_card_back = "modi/stack/back.png";
	private String image_dock_card = "modi/global/dockcard.png";
	private String image_flip_button = "modi/global/flip.png";
	private String image_text = "modi/stack/docktext.png";
	
	private String info_author = "anonymous";
	private String info_image = "modi/stack/modus.png";
	private String info_name = "Untitled";
	
	private String item_file = "modi/items/queuestack.txt";
	
	private Point origin = new Point(0,0);
	
	private String prefs_file = "modi/prefs/" + info_name + "prefs.txt";
	
	private boolean shade_inaccessible_cards = true;
	
	private int startcards = 1;
	
	public boolean are_cards_draggable()
	{
		return draggable_cards;
	}
	public void set_cards_draggable(boolean draggable)
	{
		this.draggable_cards = draggable;
	}
	
	public boolean draw_default_dock_icons()
	{
		return draw_default_dock_icons;
	}
	public void set_draw_default_dock_icons(boolean draw_default_dock_icons)
	{
		this.draw_default_dock_icons = draw_default_dock_icons;
	}
	
	public boolean draw_empty_cards()
	{
		return draw_empty_cards;
	}
	public void set_draw_empty_cards(boolean draw_empty_cards)
	{
		this.draw_empty_cards = draw_empty_cards;
	}
	
	public String get_author()
	{
		return info_author;
	}
	public void set_author(String author)
	{
		info_author = author;
	}
	
	public Color get_background_color()
	{
		return color_background;
	}
	public void set_background_color(int red, int green, int blue)
	{
		color_background = new Color(red, green, blue);
	}
	
	public String get_bottom_dock_image()
	{
		return image_background_bottom;
	}
	public void set_bottom_dock_image(String path)
	{
		image_background_bottom = path;
	}
	
	public String get_card_back_image()
	{
		return image_card_back;
	}
	public void set_card_back_image(String path)
	{
		image_card_back = path;
	}
	
	public int get_card_width()
	{
		return card_width;
	}
	public int get_card_height()
	{
		return card_height;
	}
	public void set_card_size(int width, int height)
	{
		card_width = width;
		card_height = height;
	}
	
	public String get_card_image()
	{
		return image_card;
	}
	public void set_card_image(String path)
	{
		image_card = path;
	}
	
	public String get_dock_card_image()
	{
		return image_dock_card;
	}
	public void set_dock_card_image(String path)
	{
		image_dock_card = path;
	}
	
	public String get_dock_text_image()
	{
		return image_text;
	}
	public void set_dock_text_image(String path)
	{
		image_text = path;
	}
	
	public String get_flip_button_image()
	{
		return image_flip_button;
	}
	public void set_flip_button_image(String path)
	{
		image_flip_button = path;
	}
	
	public int get_initial_card_number()
	{
		return startcards;
	}
	public void set_initial_card_number(int cards)
	{
		startcards = cards;
	}
	
	public String get_item_file()
	{
		return item_file;
	}
	public void set_item_file(String path)
	{
		item_file = path;
	}
	
	public String get_modus_image()
	{
		return info_image;
	}
	public void set_modus_image(String path)
	{
		info_image = path;
	}
	
	public String get_name()
	{
		return info_name;
	}
	public void set_name(String name)
	{
		info_name = name;
	}
	
	public Point get_origin()
	{
		return origin;
	}
	public void set_origin(int x, int y)
	{
		origin = new Point(x,y);
	}
	
	public String get_preferences_file()
	{
		return prefs_file;
	}
	public void set_preferences_file(String path)
	{
		prefs_file = path;
	}
	
	public String get_top_dock_image()
	{
		return image_background_top;
	}
	public void set_top_dock_image(String path)
	{
		image_background_top = path;
	}
	
	public boolean shade_inaccessible_cards()
	{
		return shade_inaccessible_cards;
	}
	public void set_shade_inaccessible_cards(boolean shade_inaccessible_cards)
	{
		this.shade_inaccessible_cards = shade_inaccessible_cards;
	}
}
