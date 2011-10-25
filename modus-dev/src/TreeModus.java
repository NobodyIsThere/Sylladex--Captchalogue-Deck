import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.*;
import sylladex.*;
import sylladex.Animation.AnimationType;

public class TreeModus extends FetchModus
{
	private ArrayList<SylladexCard> cards = new ArrayList<SylladexCard>();
	private Tree tree;
	private Tree.Node last;
	
	public TreeModus(Main m)
	{
		this.m = m;
		image_background_top = "modi/tree/dock_top.png";
		image_background_bottom = "modi/tree/dock.png";
		image_text = "modi/tree/text.png";
		image_card = "modi/tree/card.png";
		image_dock_card = "modi/global/dockcard.png";
		
		info_image = "modi/tree/modus.png";
		info_name = "Tree";
		info_author = "gumptiousCreator";
		
		item_file = "modi/items/tree.txt";
		prefs_file = "modi/prefs/treeprefs.txt";
		
		color_background = new Color(150, 255, 0);
		
		startcards = 8;
		origin = new Point(21,120);
		draw_default_dock_icons = true;
		draw_empty_cards = false;
		shade_inaccessible_cards = false;
		
		card_width = 94;
		card_height = 119;
		
		icons = new ArrayList<JLabel>();
	}
	
	enum PrefLabels
	{
		ROOT_ACCESS (0),
		AUTO_BALANCE (1);
		
		public int index;
		PrefLabels(int i)
		{ index = i; }
	}
	
	private class Tree
	{
		private Node treeroot;
		
		public Tree(SylladexCard card)
		{
			treeroot = new Node(card);
		}
		
		public Node getRoot()
		{ return treeroot; }
		
		public void add(SylladexCard card)
		{
			Node node = new Node(card);
			Node current = getRoot();
			while(node.parent==null)
			{
				if(node.card.getItemString().toLowerCase().compareTo(current.card.getItemString().toLowerCase())<0)
				{
					if(current.left==null)
					{
						current.left = node;
						node.parent = current;
					}
					else
					{
						current = current.left;
					}
				}
				else if(node.card.getItemString().toLowerCase().compareTo(current.card.getItemString().toLowerCase())>=0)
				{
					if(current.right==null)
					{
						current.right = node;
						node.parent = current;
					}
					else
					{
						current = current.right;
					}
				}
			}
		}
		
		public void remove(SylladexCard card)
		{
			Node node = getNodeWithCard(card);
			
			// We can only remove leaves
			if(node.left!=null || node.right!=null)
			{ return; }
			
			if(node.parent!=null)
			{
				if(node.parent.left==node)
				{ node.parent.left = null; }
				else
				{ node.parent.right = null; }
				
				if(preferences.get(PrefLabels.AUTO_BALANCE.index).equals("true"))
					node.parent.balance();
			}
		}
		
		public Node getNodeWithCard(SylladexCard card)
		{
			return treeroot.getNodeWithCard(card);
		}
		
		public ArrayList<Animation> buildAnimation(SylladexCard c)
		{
			ArrayList<Animation> anims = new ArrayList<Animation>();
			c.setPosition(new Point(treeroot.x,treeroot.y));
			if(preferences.get(PrefLabels.ROOT_ACCESS.index).equals("true"))
			{ c.setLayer(0); }
			else
			{ c.setLayer(10000); }
			return treeroot.buildAnimation(anims, c);
		}
		
		public class Node
		{
			private SylladexCard card;
			private Node left = null;
			private Node right = null;
			private Node parent = null;
			public int x;
			public int y;
			
			public Node(SylladexCard card)
			{
				this.card = card;
			}
			
			public void balance()
			{
				int balance = leftHeight() - rightHeight();
				if(balance==2)
				{
					// Left outweighs right. Check left child
					if(left.leftHeight() - left.rightHeight()==1)
					{
						// Right rotation.
						rightRotate();
					}
					else
					{
						// Left-rotate left child, then right-rotate.
						left.leftRotate();
						rightRotate();
					}
				}
				else if(balance==-2)
				{
					// Right outweighs left. Check right child
					if(right.leftHeight() - right.rightHeight()==-1)
					{
						// Left rotation
						leftRotate();
					}
					else
					{
						// Right-rotate right child, then left-rotate.
						right.rightRotate();
						leftRotate();
					}
				}
				if(parent!=null)
				{ parent.balance(); }
			}
			
			private void rightRotate()
			{
				Node rroot = this;
				Node rootparent = parent;
				boolean isleftchild = false;
				if(rootparent!=null)
				{isleftchild = rootparent.left==rroot;}
				Node rpivot = left;
				
				rroot.left = rpivot.right;
					if(rpivot.right!=null)
					{rpivot.right.parent = rroot;}
				rpivot.right = rroot;
					rroot.parent = rpivot;
				if(rootparent!=null)
				{
					if(isleftchild) { rootparent.left = rpivot; }
					else { rootparent.right = rpivot; }
				}
				rpivot.parent = rootparent;
				if(rootparent==null)
				{ treeroot = rpivot; }
			}
			
			private void leftRotate()
			{
				Node rroot = this;
				Node rootparent = parent;
				boolean isleftchild = false;
				if(rootparent!=null)
				{isleftchild = rootparent.left==rroot;}
				Node rpivot = right;
				
				rroot.right = rpivot.left;
					if(rpivot.left!=null)
					{rpivot.left.parent = rroot;}
				rpivot.left = rroot;
					rroot.parent = rpivot;
				if(rootparent!=null)
				{
					if(isleftchild) { rootparent.left = rpivot; }
					else { rootparent.right = rpivot; }
				}
				rpivot.parent = rootparent;
				if(rootparent==null)
				{ treeroot = rpivot; }
			}
			
			public int leftHeight()
			{
				if(left==null)
				{ return 0; }
				else if(left.leftHeight() > left.rightHeight())
				{ return 1 + left.leftHeight(); }
				else
				{ return 1 + left.rightHeight(); }
			}
			public int rightHeight()
			{
				if(right==null)
				{ return 0; }
				else if(right.leftHeight() > right.rightHeight())
				{ return 1 + right.leftHeight(); }
				else
				{ return 1 + right.rightHeight(); }
			}
			
			public int numLeftChildren()
			{
				if(left==null)
				{ return 0; }
				else
				{ return 1 + left.numLeftChildren() + left.numRightChildren(); }
			}
			public int numRightChildren()
			{
				if(right==null)
				{ return 0; }
				else
				{ return 1 + right.numLeftChildren() + right.numRightChildren(); }
			}
			
			public int leftWidth()
			{
				if(left!=null)
					return card_width/2 + 1 + left.leftWidth();
				return card_width/2 + 1;
			}
			
			public int rightWidth()
			{
				if(right!=null)
					return card_width/2 + 1 + right.rightWidth();
				return card_width/2 + 1;
			}
			
			public int getX()
			{
				if(parent==null)
				{
					if(left!=null)
					{ return left.leftWidth() + left.rightWidth(); }
					return 0;
				}
				else
				{
					if(this == parent.left)
					{
						if(parent.getX()-rightWidth()<parent.getX()-card_width+10)
						{ return parent.getX()-card_width+10; }
						return parent.getX() - rightWidth();
					}
					else
					{
						if(parent.getX()+leftWidth()>parent.getX()+card_width-10)
						{ return parent.getX()+card_width-10; }
						return parent.getX() + leftWidth();
					}
				}
			}
			
			public int getY()
			{
				if(parent==null)
				{ return 0; }
				else
				{ return parent.getY() + 3*card_height/4; }
			}
			
			public boolean isRoot()
			{
				return this==treeroot;
			}
			
			public boolean isLeaf()
			{
				return left==null && right==null;
			}
			
			public Node getNodeWithCard(SylladexCard card)
			{
				if(this.card == card)
				{ return this; }
				if(left!=null)
				{
					if(left.getNodeWithCard(card)!=null)
					{ return left.getNodeWithCard(card); }
				}
				if(right!=null)
				{
					if(right.getNodeWithCard(card)!=null)
					{ return right.getNodeWithCard(card); }
				}
				return null;
			}
			
			public ArrayList<Animation> buildAnimation(ArrayList<Animation> anims, SylladexCard c)
			{
				//Don't animate the root
				if(c.getItemString().equals(card.getItemString()))
				{
					Animation a = new Animation(AnimationType.WAIT,100,null,"run");
					if(anims.size()>0)
					{
						anims.get(anims.size()-1).setListener(a);
					}
					anims.add(a);
					return anims;
				}
				
				Point p = null;
				boolean addleft = false;
				if(c.getItemString().compareToIgnoreCase(card.getItemString())<0)
				{
					//Left
					p = new Point(x-20,y+20);
					addleft = true;
				}
				else
				{
					//Right
					p = new Point(x+20,y+20);
				}
				
				Animation b = new Animation(AnimationType.WAIT, 500, null, "run");
				Animation a = new Animation(c, p, AnimationType.BOUNCE, b, "run");
				b.setFinalPosition(p);
				if(anims.size()>0)
				{
					anims.get(anims.size()-1).setListener(a);
					a.setStartPosition(anims.get(anims.size()-1).getFinalPosition());
				}
				anims.add(a);
				anims.add(b);
				
				if(addleft && !isLeaf())
				{
					return left.buildAnimation(anims, c);
				}
				if(!addleft && !isLeaf())
				{
					return right.buildAnimation(anims, c);
				}
				return anims;
			}
		}
	}
	
	@Override
	public void prepare()
	{
		populatePreferencesPanel();
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
			
		JToggleButton rootbutton = new JToggleButton("ROOT");
			rootbutton.setActionCommand("root");
			rootbutton.addActionListener(this);
			rootbutton.setBounds(78,231,84,36);
			preferences_panel.add(rootbutton);
			
		preferences_panel.validate();
	}
	
	@Override
	public void addGenericItem(Object o)
	{
		ArrayList<Animation> anims;
		
		if(m.getNextEmptyCard()==null)
			return;
		SylladexCard card = m.getNextEmptyCard();
		if(cards.size()==0)
		{
			card.setItem(o);
			tree = new Tree(card);
		}
		else
		{
			card.setItem(o);
			tree.add(card);
		}
		
		last = tree.getNodeWithCard(card);
		
		JLabel icon = m.getIconLabelFromObject(o);
		icons.add(icon);
		card.setIcon(icon);
		cards.add(card);
		m.setIcons(icons);

		anims = tree.buildAnimation(card);
		anims.get(anims.size()-1).setListener(this);
		anims.get(anims.size()-1).setActionCommand("end animation");
		anims.get(0).run();
	}
	
	private void arrangeCards()
	{
		for(SylladexCard card : cards)
		{
			Tree.Node node = tree.getNodeWithCard(card);
			Point p = card.getPosition();
			Animation b = new Animation(card, new Point(node.getX(),node.getY()), AnimationType.BOUNCE, null, "");
			Animation a = new Animation(card, p, AnimationType.BOUNCE, b, "run");
			a.run();
			node.x = node.getX();
			node.y = node.getY();
			if(node.isLeaf())
			{ card.setAccessible(true); }
			else
			{ card.setAccessible(false); }

			if(preferences.get(PrefLabels.ROOT_ACCESS.index).equals("true"))
				card.setLayer(100-node.getY()+node.getX());
			else
				card.setLayer(node.getY()+node.getX());
		}
		if(tree!=null)
		{ m.setCardHolderSize(tree.treeroot.leftWidth()*2 + tree.treeroot.rightWidth()*2 + card_width*2,500); }
		m.refreshCardHolder();
	}
	
	@Override
	public void open(SylladexCard card)
	{
		cards.remove(card);
		icons.remove(card.getIcon());
		m.setIcons(icons);
		m.open(card);
		if(tree.getNodeWithCard(card).isRoot())
		{ tree = null; }
		else
		{ tree.remove(card); }
		arrangeCards();
	}
	
	@Override
	public void addCard()
	{
		m.addCard();
	}
	
	@Override
	public void showSelectionWindow(){}
	
	@Override
	public ArrayList<String> getItems()
	{
		// TODO Auto-generated method stub
		return new ArrayList<String>();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("end animation"))
		{
			if(preferences.get(PrefLabels.AUTO_BALANCE.index).equals("true"))
				last.balance();
			arrangeCards();
		}
	}
	
}