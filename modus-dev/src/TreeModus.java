import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import sylladex.CaptchalogueCard;
import sylladex.FetchModus;
import sylladex.Main;
import sylladex.SylladexItem;
import util.Animation;
import util.Animation.AnimationType;
import util.Util;
import util.Util.OpenReason;

public class TreeModus extends FetchModus
{	
	private Tree tree;
	
	private JToggleButton rootbutton;
	private JToggleButton leafbutton;
	
	private static final int PREF_ROOT_ACCESS = 0, PREF_AUTO_BALANCE = 1;
	
	public TreeModus(Main m)
	{
		super(m);
	}
	
	@Override
	public void initialSettings()
	{
		settings.set_dock_text_image("modi/canon/tree/text.png");
		settings.set_dock_card_image("modi/canon/tree/dockcard.png");
		settings.set_card_image("modi/canon/tree/card.png");
		settings.set_card_back_image("modi/canon/tree/back.png");
		
		settings.set_modus_image("modi/canon/tree/modus.png");
		settings.set_name("Tree");
		settings.set_author("gumptiousCreator");
		
		settings.set_preferences_file("modi/prefs/treeprefs.txt");
		
		settings.set_background_color(150, 255, 0);
		settings.set_secondary_color(124, 166, 25);
		settings.set_text_color(248, 102, 89);
		
		settings.set_initial_card_number(8);
		settings.set_origin(20, 120);
		
		settings.set_card_size(94, 119);
		
		settings.set_shade_inaccessible_cards(false);
	}
	
	@Override
	public void prepare()
	{
		if (preferences.size() == 0)
		{
			preferences.add("true");
			preferences.add("true");
		}
		populatePreferencesPanel();
		deck.getCardDisplayManager().setMargin(settings.get_card_height());
		tree = new Tree();
	}
	
	@Override
	public void ready()
	{
		arrangeCards(false);
	}
	
	private void populatePreferencesPanel()
	{
		// We need absolute positioning.
		preferences_panel.setLayout(null);
		preferences_panel.setPreferredSize(new Dimension(270,300));
		
		JButton ejectbutton = new JButton("EJECT");
			ejectbutton.setActionCommand("tree_eject");
			ejectbutton.addActionListener(this);
			ejectbutton.setBounds(77,7,162,68);
			preferences_panel.add(ejectbutton);
			
		rootbutton = new JToggleButton("ROOT");
			rootbutton.setActionCommand("tree_root");
			rootbutton.addActionListener(this);
			rootbutton.setBounds(58,181,84,36);
			rootbutton.setSelected(Boolean.parseBoolean(preferences.get(PREF_ROOT_ACCESS)));
			preferences_panel.add(rootbutton);

		leafbutton = new JToggleButton("LEAF");
			leafbutton.setActionCommand("tree_leaf");
			leafbutton.addActionListener(this);
			leafbutton.setBounds(142,181,84,36);
			leafbutton.setSelected(!Boolean.parseBoolean(preferences.get(PREF_ROOT_ACCESS)));
			preferences_panel.add(leafbutton);
			
		JCheckBox autobalance = new JCheckBox("auto-balance");
			autobalance.setActionCommand("tree_autobalance");
			autobalance.addActionListener(this);
			autobalance.setSelected(Boolean.parseBoolean(preferences.get(PREF_AUTO_BALANCE)));
			autobalance.setBounds(54,260,165,19);
			preferences_panel.add(autobalance);
			
		preferences_panel.validate();
	}
	
	@Override
	public boolean captchalogue(SylladexItem item)
	{
		if (deck.isFull()) { return false; }
		
		CaptchalogueCard card = deck.captchalogueItemAndReturnCard(item);
		tree.add(card);
		
		if (loading && preferences.get(PREF_AUTO_BALANCE).equals("true"))
		{
			tree.getNodeWithCard(card).balance();
		}
		else
		{
			tree.buildAnimation(card);
			Animation a = new Animation(AnimationType.WAIT, 10, this, "tree_animation_complete");
			a.setAnimationTarget(card);
			deck.addAnimation(a);
		}
		return true;
	}
	
	private void arrangeCards(boolean animate)
	{
		deck.setIcons(getCardOrder());
		foreground.removeAll();
		
		for (CaptchalogueCard card : deck.getCards())
		{
			if (tree == null || tree.getNodeWithCard(card) == null)
			{
				card.setVisible(false);
			}
		}
		
		if (deck.isEmpty()) { return; }
		
		for (Tree.Node node : tree)
		{
			CaptchalogueCard card = node.card;
			
			if (animate)
			{
				int x = (int) (card.getX()*1.1);
				int y = (int) (card.getY()*1.1);
				card.moveTo(new Point(x, y), AnimationType.JUMP);
				card.moveTo(new Point(x/2, y/2), AnimationType.JUMP);
				Animation a = new Animation(card, new Point(node.getX(), node.getY()),
											AnimationType.BOUNCE, this, "tree_balance_complete");
				card.addAnimation(a);
				a.setStartPosition(new Point(0, settings.get_card_height()/2));
			}
			else
			{
				card.setLocation(new Point(node.getX(), node.getY()));
			}
			
			card.setVisible(true);
			card.setAccessible((preferences.get(PREF_ROOT_ACCESS).equals("true") && node.isRoot()) || 
								(!preferences.get(PREF_ROOT_ACCESS).equals("true") && node.isLeaf()));
			
			if (preferences.get(PREF_ROOT_ACCESS).equals("true"))
			{
				card.setLayer(getLayer(node.getX(), node.getY()));
			}
			else
			{
				card.setLayer(getLayer(node.getX(), node.getY()));
			}
			
			if (node.left != null)
			{
				Brace b = new Brace();
				b.setAbove(true);
				int w = node.left.card.getDockIcon().getX() - card.getDockIcon().getX() - 3;
				b.setBounds(card.getDockIcon().getX()+16, deck.getDockIconYPosition()-2, w, 8);
				foreground.add(b);
			}
			
			if (node.right != null)
			{
				Brace b = new Brace();
				int w = node.right.card.getDockIcon().getX() - card.getDockIcon().getX() - 3;
				b.setBounds(card.getDockIcon().getX()+16, deck.getDockIconYPosition()+52, w, 8);
				foreground.add(b);
			}
		}
		
		if (!animate)
		{
			deck.getCardDisplayManager().unfreeze();
			deck.getCardDisplayManager().freeze();
		}
	}
	
	@Override
	public Object[] getCardOrder()
	{
		ArrayList<CaptchalogueCard> cardorder = new ArrayList<CaptchalogueCard>();
		if (tree != null && tree.treeroot != null)
		{
			for (Tree.Node n : tree)
			{
				cardorder.add(n.card);
			}
		}
		return cardorder.toArray();
	}
	
	@Override
	public void open(CaptchalogueCard card, OpenReason reason)
	{
		if (tree.getNodeWithCard(card).isRoot())
		{ eject(OpenReason.MODUS_DEFAULT); arrangeCards(false); }
		else
		{
			deck.open(card, reason);
			Tree.Node n = tree.getNodeWithCard(card).parent;
			tree.remove(card);
			if (preferences.get(PREF_AUTO_BALANCE).equals("true")
					&& !tree.isBalanced())
			{
					n.balance();
					arrangeCards(true);
			}
			else
			{
				arrangeCards(false);
			}
		}
	}
	
	private void eject(OpenReason reason)
	{
		for (Tree.Node node : tree)
		{
			if (!node.isRoot())
			{
				deck.open(node.card, reason);
			}
		}
		deck.open(tree.getRoot().card, reason);
		tree.clear();
		arrangeCards(false);
	}
	
	@Override
	public void addCard()
	{
		deck.addCard();
		arrangeCards(false);
	}

	private int getLayer(int x, int y)
	{
		if (preferences.get(PREF_ROOT_ACCESS).equals("true"))
		{
			return deck.getCardHolder().getHeight() - y - x;
		}
		return y - x;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("tree_animation_complete"))
		{
			if (preferences.get(PREF_AUTO_BALANCE).equals("true"))
			{
				Tree.Node node = tree.getNodeWithCard((CaptchalogueCard) ((Animation) e.getSource()).getAnimationTarget());
				if (!tree.isBalanced())
				{
					node.balance();
					arrangeCards(true);
					return;
				}
			}
			arrangeCards(false);
		}
		else if (e.getActionCommand().equals("tree_animation_continue"))
		{
			Object target = ((Animation) e.getSource()).getAnimationTarget();
			if (target instanceof CaptchalogueCard)
			{
				((CaptchalogueCard) target).setVisible(true);
				if (preferences.get(PREF_ROOT_ACCESS).equals("true"))
				{
					((CaptchalogueCard) target).setLayer(getLayer(((CaptchalogueCard) target).getX(),
							((CaptchalogueCard) target).getY() + settings.get_card_height()));
				}
				else
				{
					((CaptchalogueCard) target).setLayer(getLayer(((CaptchalogueCard) target).getX(),
							((CaptchalogueCard) target).getY() + settings.get_card_height()));
				}
			}
		}
		else if (e.getActionCommand().equals("tree_balance_complete"))
		{
			arrangeCards(false);
		}
		else if(e.getActionCommand().equals("tree_eject"))
		{
			int n = JOptionPane.showOptionDialog(preferences_panel, "EJECT ALL ITEMS FROM SYLLADEX?", "",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new Object[] {"Y", "N"}, "N");
			if(n==0)
				eject(OpenReason.USER_EJECT);
		}
		else if(e.getActionCommand().equals("tree_root"))
		{
			preferences.set(PREF_ROOT_ACCESS, "true");
			leafbutton.setSelected(false);
			arrangeCards(false);
		}
		else if(e.getActionCommand().equals("tree_leaf"))
		{
			preferences.set(PREF_ROOT_ACCESS, "false");
			rootbutton.setSelected(false);
			arrangeCards(false);
		}
		else if(e.getActionCommand().equals("tree_autobalance"))
		{
			if(preferences.get(PREF_AUTO_BALANCE).equals("true"))
			{
				preferences.set(PREF_AUTO_BALANCE, "false");
			}
			else
			{
				int n = JOptionPane.showOptionDialog(preferences_panel,
						"ENABLING AUTO-BALANCE WILL EJECT SYLLADEX. ARE YOU SURE?", "", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, new Object[] {"Y", "N"}, "N");
				if(n==0)
				{
					eject(OpenReason.USER_EJECT);
					preferences.set(PREF_AUTO_BALANCE, "true");
				}
				else
				{
					((JCheckBox)e.getSource()).setSelected(false);
				}
			}
		}
		else if (e.getActionCommand().equals(Util.ACTION_CARD_MOUSE_ENTER))
		{
			((CaptchalogueCard) e.getSource()).setLayer(deck.getCardHolder().getHeight());
		}
		else if (e.getActionCommand().equals(Util.ACTION_CARD_MOUSE_EXIT))
		{
			((CaptchalogueCard) e.getSource()).setLayer(getLayer(((CaptchalogueCard) e.getSource()).getX(),
															((CaptchalogueCard) e.getSource()).getY()));
		}
	}

	private class Tree implements Iterable<Tree.Node>, Iterator<Tree.Node>
	{
		private Node treeroot;
		
		public void clear()
		{
			treeroot = null;
		}

		public Node getRoot()
		{ return treeroot; }
		
		public void add(CaptchalogueCard card)
		{
			Node node = new Node(card);
			
			if (getRoot() == null)
			{
				treeroot = node;
				return;
			}
			
			Node current = treeroot;
			while (node.parent == null)
			{
				if (node.card.getItem().getName().toLowerCase().compareTo(current.card.getItem().getName().toLowerCase())<0)
				{
					if (current.left==null)
					{
						current.left = node;
						node.parent = current;
					}
					else
					{
						current = current.left;
					}
				}
				else if (node.card.getItem().getName().toLowerCase().compareTo(current.card.getItem().getName().toLowerCase())>=0)
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
		
		public void remove(CaptchalogueCard card)
		{
			Node node = getNodeWithCard(card);
			
			// We can only remove leaves
			if (!node.isLeaf())
			{ return; }
			
			if (node.parent != null)
			{
				if (node.parent.left == node)
				{ node.parent.left = null; }
				else
				{ node.parent.right = null; }
			}
		}
		
		public Node getNodeWithCard(CaptchalogueCard card)
		{
			if (treeroot != null)
				return treeroot.getNodeWithCard(card);
			return null;
		}
		
		public void buildAnimation(CaptchalogueCard c)
		{
			c.setLocation(new Point(0, 0));
			treeroot.buildAnimation(c);
		}
		
		public boolean isBalanced()
		{
			boolean result = true;
			for (Node n : this)
			{
				if (!n.isBalanced())
					result = false;
			}
			return result;
		}
		
		public class Node
		{
			private CaptchalogueCard card;
			private Node left = null;
			private Node right = null;
			private Node parent = null;
			
			private int card_height = settings.get_card_height();
			
			public Node(CaptchalogueCard card)
			{
				this.card = card;
			}
			
			public void balance()
			{
				int balance = leftHeight() - rightHeight();
				if (balance==2)
				{
					// Left outweighs right. Check left child
					if (left.leftHeight() - left.rightHeight() == 1)
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
				else if (balance==-2)
				{
					// Right outweighs left. Check right child
					if (right.leftHeight() - right.rightHeight() == -1)
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
				if (parent!=null)
				{ parent.balance(); }
			}
			
			private void rightRotate()
			{
				Node rroot = this;
				Node rootparent = parent;
				boolean isleftchild = false;
				if (rootparent!=null)
				{ isleftchild = rootparent.left==rroot; }
				Node rpivot = left;
				
				rroot.left = rpivot.right;
					if (rpivot.right!=null)
					{ rpivot.right.parent = rroot; }
				rpivot.right = rroot;
					rroot.parent = rpivot;
				if (rootparent!=null)
				{
					if (isleftchild) { rootparent.left = rpivot; }
					else { rootparent.right = rpivot; }
				}
				rpivot.parent = rootparent;
				if (rootparent==null)
				{ treeroot = rpivot; }
			}
			
			private void leftRotate()
			{
				Node rroot = this;
				Node rootparent = parent;
				boolean isleftchild = false;
				if (rootparent!=null)
				{ isleftchild = rootparent.left==rroot; }
				Node rpivot = right;
				
				rroot.right = rpivot.left;
					if (rpivot.left!=null)
					{ rpivot.left.parent = rroot; }
				rpivot.left = rroot;
					rroot.parent = rpivot;
				if (rootparent!=null)
				{
					if (isleftchild) { rootparent.left = rpivot; }
					else { rootparent.right = rpivot; }
				}
				rpivot.parent = rootparent;
				if (rootparent==null)
				{ treeroot = rpivot; }
			}
			
			public int leftHeight()
			{
				if (left==null)
				{ return 0; }
				else if (left.leftHeight() > left.rightHeight())
				{ return 1 + left.leftHeight(); }
				else
				{ return 1 + left.rightHeight(); }
			}
			public int rightHeight()
			{
				if (right==null)
				{ return 0; }
				else if (right.leftHeight() > right.rightHeight())
				{ return 1 + right.leftHeight(); }
				else
				{ return 1 + right.rightHeight(); }
			}
			
			public int numLeftChildren()
			{
				if (left==null)
				{ return 0; }
				return 1 + left.numLeftChildren() + left.numRightChildren();
			}
			public int numRightChildren()
			{
				if (right==null)
				{ return 0; }
				return 1 + right.numLeftChildren() + right.numRightChildren();
			}
			
			public int getX()
			{
				if (parent==null)
				{
					return 0;
				}
				if (this == parent.left)
				{
					return parent.getLeftChildX();
				}
				return parent.getRightChildX();
			}	
			public int getY()
			{
				if (parent==null)
				{ return 0; }
				return parent.getChildY();
			}
			
			public int getChildY()
			{
				return getY() + 3*card_height/4;
			}
			
			public int getLeftChildX()
			{
				return getX() - 75/(2*getY()/card_height + 1);
			}
			public int getRightChildX()
			{
				return getX() + 75/(2*getY()/card_height + 1);
			}
			
			public boolean isRoot()
			{
				return this==treeroot;
			}
			
			public boolean isLeaf()
			{
				return left==null && right==null;
			}
			
			public boolean isBalanced()
			{
				return Math.abs(leftHeight() - rightHeight()) < 2;
			}
			
			public Node getNodeWithCard(CaptchalogueCard card)
			{
				if (this.card == card)
				{ return this; }
				if (left!=null)
				{
					if (left.getNodeWithCard(card)!=null)
					{ return left.getNodeWithCard(card); }
				}
				if (right!=null)
				{
					if (right.getNodeWithCard(card)!=null)
					{ return right.getNodeWithCard(card); }
				}
				return null;
			}
			
			public void buildAnimation(CaptchalogueCard c)
			{
				Animation waitanim = new Animation(AnimationType.WAIT, 200, null, "tree_animation_continue");
				waitanim.setAnimationTarget(c);
				deck.addAnimation(waitanim);
				
				Point p = null;
				int offset = 10;
				if (c.getItem().getName().compareToIgnoreCase(card.getItem().getName()) < 0)
				{
					if (left != null && left.card == c) { offset = 0; }
					p = new Point(getLeftChildX() + offset, getChildY() - offset);
					deck.moveCard(c, p, AnimationType.BOUNCE);
					if (left != null)
					{
						left.buildAnimation(c);
					}
				}
				else if (c.getItem().getName().compareToIgnoreCase(card.getItem().getName()) > 0)
				{
					if (right != null && right.card == c) { offset = 0; }
					p = new Point(getRightChildX() - offset, getChildY() - offset);
					deck.moveCard(c, p, AnimationType.BOUNCE);
					if (right != null)
					{
						right.buildAnimation(c);
					}
				}
			}
		}
		
		private ArrayList<Node> visited = new ArrayList<Node>();
		
		@Override
		public boolean hasNext()
		{
			for (CaptchalogueCard card : deck.getCards())
			{
				Node node = getNodeWithCard(card);
				if (node == null) { break; }
				else if(!visited.contains(node))
					return true;
			}
			visited.clear();
			return false;
		}
	
		@Override
		public Node next()
		{
			boolean success = false;
			
			if (visited.size()==0)
			{
				visited.add(treeroot);
				return treeroot;
			}
			while (success==false)
			{
				Node lastvisited = visited.get(visited.size()-1);
				
				//Have we been to the left of here?
				if (!visited.contains(lastvisited.left) && lastvisited.left!=null)
				{
					visited.add(lastvisited.left);
					success=true;
					return lastvisited.left;
				}
				else if (!visited.contains(lastvisited.right) && lastvisited.right!=null)
				{
					visited.add(lastvisited.right);
					success=true;
					return lastvisited.right;
				}
				else
				{
					visited.add(lastvisited.parent);
				}
			}
			return null;
		}
	
		@Override
		public Iterator<Node> iterator()
		{
			return this;
		}
	
		@Override
		public void remove(){}
	
	}

	@SuppressWarnings("serial")
	private class Brace extends JLabel
	{
		boolean above = false;
		public void setAbove(boolean above)
		{
			this.above = above;
		}
		
		public void paintComponent(Graphics g)
		{
			g.setColor(new Color(124,166,25));
			if (above)
			{
				// |
				g.drawLine(0, getHeight(), 0, 0);
				// -
				g.drawLine(0, 0, getWidth(), 0);
				// |
				g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
			}
			else
			{
				// |
				g.drawLine(0, 0, 0, getHeight()-1);
				// -
				g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
				// |
				g.drawLine(getWidth()-1, getHeight()-1, getWidth()-1, 0);
			}
		}
	}
	
	@Override
	public void refreshDock()
	{
		arrangeCards(false);
	}
}