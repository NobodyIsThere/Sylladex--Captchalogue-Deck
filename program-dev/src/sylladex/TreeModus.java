package sylladex;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JLabel;
import sylladex.*;
import sylladex.Animation.AnimationType;

public class TreeModus extends FetchModus
{
	private ArrayList<SylladexCard> cards = new ArrayList<SylladexCard>();
	private Tree tree;
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
			node.balance();
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
				node.parent.balance();
			}
		}
		
		public Node getNodeWithCard(SylladexCard card)
		{
			return treeroot.getNodeWithCard(card);
		}
		
		public class Node
		{
			private SylladexCard card;
			private Node left = null;
			private Node right = null;
			private Node parent = null;
			
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
			
			private int offset()
			{
				double y = getY()/(3*card_height/4);
				return new Double(10*Math.pow(Math.E,-y)*card_width/4).intValue();
			}
			
			public int leftWidth()
			{
				return offset();
				//if(left!=null)
				//	return card_width/2 + 1 + left.leftWidth();
				//return card_width/2 + 1;
			}
			
			public int rightWidth()
			{
				return offset();
				//if(right!=null)
				//	return card_width/2 + 1 + right.rightWidth();
				//return card_width/2 + 1;
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
						return parent.getX() - rightWidth();
					}
					else
					{
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
		}
	}
	
	@Override
	public void prepare()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void addGenericItem(Object o)
	{
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
		card.setAccessible(true);
		JLabel icon = m.getIconLabelFromObject(o);
		icons.add(icon);
		card.setIcon(icon);
		cards.add(card);
		m.setIcons(icons);
		
		//TODO
		arrangeCards();
	}
	
	private void arrangeCards()
	{
		for(SylladexCard card : cards)
		{
			Tree.Node node = tree.getNodeWithCard(card);
			card.setPosition(new Point(node.getX(),node.getY()));
			if(node.isLeaf())
			{ card.setAccessible(true); }
			else
			{ card.setAccessible(false); }
		}
		if(tree!=null)
		{ m.setCardHolderSize(500,500); }
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
	public void actionPerformed(ActionEvent e){}
	
}