import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import sylladex.Widget;
import ui.MenuItem;
import util.Bubble;
import util.Util;
import util.Util.OpenReason;

public class UpdateChecker extends Widget implements ActionListener
{
	/** Alert bubble */
	private Bubble bubble;
	
	/** MSPA RSS feed */
	private static final URI RSS;
	static
	{
		URI rss = null; // temp variable that isn't final for the try block
		try
		{
			rss = new URI("http", "//www.mspaintadventures.com/rss/rss.xml", null);
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
			Util.error("Update Checker was unable to create URI.");
		}
		RSS = rss;
	}
	public static final String FOLDER = "widgets/UpdateChecker/";
	private static final File HELP_FILE = new File(FOLDER, "Instructions.txt");
	/** The default interval at which to check for updates (5 minutes). */
	private static final int DEFAULT_CHECK_INTERVAL = 300;

	public static enum State
	{
		UPDATE, NO_UPDATE, NO_CONNECTION, ERROR;
		private Icon dockIcon = Util.createImageIcon(FOLDER + "doc_"
				+ toString() + ".gif");
		private ImageIcon cardIcon = Util.createImageIcon(FOLDER + "card_"
				+ toString() + ".gif");

		public Icon getDockIcon()
		{
			return dockIcon;
		}

		public ImageIcon getCardIcon()
		{
			return cardIcon;
		}
	}

	private static enum Website
	{
		MSPA_HOME(new JRadioButton("MSPA home page"))
		{
			@Override
			protected String getAddress(UpdateChecker checker)
			{
				return "http://www.mspaintadventures.com/";
			}
		},
		MSPA_LOAD(new JRadioButton("Load your saved game"))
		{
			@Override
			protected String getAddress(UpdateChecker checker)
			{
				return "http://www.mspaintadventures.com/?game=load&s=6&p=1901";
			}
		},
		LATEST_PAGE(new JRadioButton("Latest page"))
		{
			@Override
			protected String getAddress(UpdateChecker checker)
			{
				StringBuffer page = new StringBuffer("" + checker.getLastReadPage());
				while (page.length() < 6)
					page.insert(0, "0");
				return "http://www.mspaintadventures.com/?s="
						+ checker.getLastReadAdventure() + "&p=" + page;
			}
		};
		// initial capacity > maximum # of entries / load factor: no rehashes
		public static Map<ButtonModel, Website> modelMap = new HashMap<ButtonModel,
				UpdateChecker.Website>(Website.values().length + 1, 1);
		static
		{
			for (Website website : Website.values())
				modelMap.put(website.getButton().getModel(), website);
		}
		private JRadioButton button;

		private Website(JRadioButton button)
		{
			this.button = button;
		}

		public JRadioButton getButton()
		{
			return button;
		}

		protected abstract String getAddress(UpdateChecker checker);

		public URI getURI(UpdateChecker checker)
		{
			URI uri = null;
			try
			{
				uri = new URI(getAddress(checker));
			}
			catch (URISyntaxException e)
			{
				e.printStackTrace();
			}
			return uri;
		}
	}

	/**
	 * A class representing something that must be saved by the widget.
	 * 
	 * @param <C> - The type of input component.
	 */
	private static abstract class Preference<C extends Component>
	{
		private Box input = null;

		/**
		 * Creates a new <code>Preference</code> with the given name and input
		 * component. The name and input component are used in the preferences
		 * dialog. Each preference has a horizontally-aligned <code>Box</code>
		 * containing both a <code>JLabel</code> with the given name as text and
		 * the given input component.
		 * 
		 * @param name
		 *            the name of the new preference. If <code>null</code>,
		 *            doesn't add it.
		 * @param inputComp
		 *            a <code>Component</code> that allows the user to modify
		 *            the preference. If <code>null</code>, the preference is
		 *            not modifiable by the user.
		 */
		public Preference(String name, C inputComp)
		{
			if (inputComp == null) return;
			input = new Box(BoxLayout.X_AXIS);
			if (name != null) input.add(new JLabel(name));
			input.add(inputComp);
		}

		public Preference(C inputComp)
		{
			this(null, inputComp);
		}

		public Preference()
		{
			this(null);
		}

		public Box inputBox()
		{
			if (input != null) updateComponent();
			return input;
		}

		/**
		 * @return a <code>String</code> representing the user's current
		 *         preference for this <code>Preference</code>.
		 */
		public abstract String getSaveString();

		/**
		 * Sets up the <code>UpdateChecker</code> based on the given save
		 * string.
		 */
		public abstract void load(String loadString);

		/**
		 * Called before the preferences dialog is shown. Updates the input
		 * component to match the <code>UpdateChecker</code>'s current state.
		 * (Only necessary if the <code>Preference</code> is modifiable by the
		 * user.)
		 */
		public void updateComponent()
		{
		}

		/**
		 * Called after the preferences dialog is shown. Updates the
		 * <code>UpdateChecker</code>'s properties based on the user's
		 * interaction with the input component associated with the
		 * <code>Preference</code>. (Only necessary if the
		 * <code>Preference</code> is modifiable by the user.)
		 */
		public void applyChanges()
		{
		}
	}

	/**
	 * This performs the actual check, and notifies the
	 * <code>UpdateChecker</code> if something's changed.
	 */
	private class Check extends TimerTask
	{
		// indicates that the check is just to get up-to-date; see run()
		private boolean justGetInfo = false;

		/** Icon used when a check is done. */
		public Icon dockCheckIcon = Util.createImageIcon(FOLDER + "doc_check.gif");
		public ImageIcon cardCheckIcon = Util.createImageIcon(FOLDER + "card_check.gif");
		/** Time in milliseconds that the check icon takes to play. */
		public final long CHECK_ICON_TIME = 600;

		/*
		 * Note: card icons are sized to fit the default card size, since
		 * they're animations, and resizing doesn't seem to work on them.
		 */

		public Check(boolean justGetInfo)
		{
			this.justGetInfo = justGetInfo;
		}

		/**
		 * The result of a check: the current state, and, if applicable, what
		 * went wrong.
		 */
		public class Status
		{
			private int newAdventure;
			private int newLastPage;
			private Throwable error = null;
			private boolean noConnection;
			private boolean justGetInfo;

			public Status(int newAdventure, int newLastPage, boolean justGetInfo)
			{
				this.newAdventure = newAdventure;
				this.newLastPage = newLastPage;
				this.justGetInfo = justGetInfo;
			}

			public Status(Throwable error, boolean noConnection)
			{
				this.error = error;
				this.noConnection = noConnection;
			}

			public Status(Throwable e)
			{
				this(e, false);
			}

			public boolean wasSuccessful()
			{
				return error == null;
			}

			public int getNewAdventure()
			{
				return newAdventure;
			}

			public int getNewLastPage()
			{
				return newLastPage;
			}

			public Throwable getError()
			{
				return error;
			}

			public boolean isNoConnection()
			{
				return noConnection;
			}

			public boolean justGetInfo()
			{
				return justGetInfo;
			}
		}

		private Status check()
		{
			if (!justGetInfo) // if justGetInfo, not really a check
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						changeIcon(dockCheckIcon, cardCheckIcon,
								CHECK_ICON_TIME);
					}
				});
			try
			{
				InputStream rssStream = null;
				try
				{
					rssStream = RSS.toURL().openStream();
					// only need digits; ignore non-digits for next()
					Scanner scanner = new Scanner(rssStream).useDelimiter("\\D+");
					// exception used to indicate that the RSS isn't what I
					// expected
					Exception rssProblem = new Exception("Problem reading RSS feed");
					if (scanner.findWithinHorizon(
							"\\Q<link>http://www.mspaintadventures.com/?s=\\E",
							1000) == null) return new Status(rssProblem);
					if (!scanner.hasNextInt()) return new Status(rssProblem);
					int newAdventure = scanner.nextInt();
					if (!scanner.hasNextInt()) return new Status(rssProblem);
					return new Status(newAdventure, scanner.nextInt(), justGetInfo);
				}
				// I think this indicates there isn't an internet connection?
				// I found it from a Google search
				// It works for me, at least
				catch (UnknownHostException e)
				{
					return new Status(e, true);
				}
				catch (MalformedURLException e)
				{
					return new Status(e);
				}
				finally
				{
					if (rssStream != null) rssStream.close();
				}
			}
			catch (IOException e)
			{
				return new Status(e);
			}
		}

		@Override
		public void run()
		{
			System.out.println("UpdateChecker: Check run");
			final Status status = check(); // check for update
			justGetInfo = false; // only first time
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					reactToCheck(status);
				}
			});
		}
	}

	// timer thread is daemon: want to run as long as the sylladex is open but
	// no longer
	private static Timer timer = new Timer(true);
	/** exception for if the load doesn't work */
	private static Exception badLoad = new Exception("Error reading saved data.");

	/**
	 * Icon can't be changed yet, because the check icon is in the process of
	 * running.
	 */
	private boolean iconBlocked = false;
	/** JPanel holding the icon for the card. */
	private JPanel panel = new JPanel();
	private JLabel card_icon = new JLabel();
	/**
	 * An <code>Action</code> representing opening the
	 * <code>UpdateChecker</code>, without removing it from the sylladex.
	 */
	private Action open = new AbstractAction("Open")
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			deck.open(card, OpenReason.USER_KEEP);
		}
	};
	/** Whether or not the dock will be shown when an update occurs. */
	private boolean showDock = true;
	/** List of preferences */
	private List<Preference<?>> preferences = new ArrayList<Preference<?>>();
	private Website website = Website.MSPA_HOME;

	// below variables should all be set in prepare()/load()/add()
	/** number of seconds between checks. */
	private int checkInterval;
	/** current state of the <code>UpdateChecker</code> */
	private State state;
	/**
	 * If an exception occurs, goes into ERROR state and stores the exception
	 * here
	 */
	private Throwable error;
	/** the most recent Check to check the site */
	private Check check = new Check(true);
	// adventure, lastPage: most recent adventure & page that the UpdateChecker
	// knows about
	// lastReadX: most recent adventure & page that the user has read
	private int adventure, lastPage, lastReadAdventure, lastReadPage;

	private int getAdventure()
	{
		return adventure;
	}

	private int getLastPage()
	{
		return lastPage;
	}

	private int getLastReadAdventure()
	{
		return lastReadAdventure;
	}

	private int getLastReadPage()
	{
		return lastReadPage;
	}

	private void setAdventure(int adventure)
	{
		this.adventure = adventure;
	}

	private void setLastPage(int lastPage)
	{
		this.lastPage = lastPage;
	}

	private void setLastReadAdventure(int lastReadAdventure)
	{
		this.lastReadAdventure = lastReadAdventure;
	}

	private void setLastReadPage(int lastReadPage)
	{
		this.lastReadPage = lastReadPage;
	}

	/** Initialization; called by both prepare() and load(). */
	@Override
	public void prepare()
	{
		panel.setOpaque(false);
		panel.add(card_icon);

		// set up preferences

		/*
		 * Check Interval: min: 0 (each check comes immediately after the last)
		 * max: 2^31-1 (don't want to overflow) step: 1 second (don't think you
		 * really need to be more precise)
		 */
		final SpinnerNumberModel intervalSpinner = new SpinnerNumberModel(0, 0,
				Integer.MAX_VALUE, 1);
		preferences.add(new Preference<JSpinner>("Check Interval (seconds): ",
				new JSpinner(intervalSpinner))
		{
			@Override
			public void load(String loadString)
			{
				try
				{
					checkInterval = Integer.parseInt(loadString);
				}
				catch (NumberFormatException e)
				{
					error(badLoad);
				}
			}

			@Override
			public String getSaveString()
			{
				return "" + checkInterval;
			}

			@Override
			public void updateComponent()
			{
				intervalSpinner.setValue(checkInterval);
			}

			@Override
			public void applyChanges()
			{
				setCheckInterval(intervalSpinner.getNumber().intValue());
			}
		});

		// show dock when update?
		final JCheckBox showDockCheckBox = new JCheckBox(
				"Show Dock when Update Detected");
		preferences.add(new Preference<JCheckBox>(showDockCheckBox)
		{
			@Override
			public String getSaveString()
			{
				return "" + showDock;
			}

			@Override
			public void load(String loadString)
			{
				showDock = Boolean.parseBoolean(loadString);
			}

			@Override
			public void updateComponent()
			{
				showDockCheckBox.setSelected(showDock);
			}

			@Override
			public void applyChanges()
			{
				showDock = showDockCheckBox.isSelected();
			}
		});

		// website to go to
		final ButtonGroup websiteGroup = new ButtonGroup();
		Box websiteContainer = new Box(BoxLayout.Y_AXIS);
		websiteContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		for (Website option : Website.values())
		{
			websiteGroup.add(option.getButton());
			websiteContainer.add(option.getButton());
		}
		preferences.add(new Preference<Component>("Browser Destination: ",
				websiteContainer)
		{
			@Override
			public String getSaveString()
			{
				return website.toString();
			}

			@Override
			public void load(String loadString)
			{
				try
				{
					website = Website.valueOf(loadString);
				}
				catch (IllegalArgumentException e)
				{
					e.printStackTrace();
				}
			}

			@Override
			public void updateComponent()
			{
				websiteGroup.setSelected(website.getButton().getModel(), true);
			}

			@Override
			public void applyChanges()
			{
				website = Website.modelMap.get(websiteGroup.getSelection());
			}
		});

		// current adventure
		preferences.add(new Preference<Component>()
		{
			@Override
			public String getSaveString()
			{
				return "" + getAdventure();
			}

			@Override
			public void load(String loadString)
			{
				try
				{
					setAdventure(Integer.parseInt(loadString));
				}
				catch (NumberFormatException e)
				{
					error(badLoad);
				}
			}
		});

		// latest page
		preferences.add(new Preference<Component>()
		{
			@Override
			public String getSaveString()
			{
				return "" + getLastPage();
			}

			@Override
			public void load(String loadString)
			{
				try
				{
					setLastPage(Integer.parseInt(loadString));
				}
				catch (NumberFormatException e)
				{
					error(badLoad);
				}

			}
		});

		// last read adventure
		preferences.add(new Preference<Component>()
		{
			@Override
			public String getSaveString()
			{
				return "" + getLastReadAdventure();
			}

			@Override
			public void load(String loadString)
			{
				try
				{
					setLastReadAdventure(Integer.parseInt(loadString));
				}
				catch (NumberFormatException e)
				{
					error(badLoad);
				}
			}
		});

		// last read page
		preferences.add(new Preference<Component>()
		{
			@Override
			public String getSaveString()
			{
				return "" + getLastReadPage();
			}

			@Override
			public void load(String loadString)
			{
				try
				{
					setLastReadPage(Integer.parseInt(loadString));
				}
				catch (NumberFormatException e)
				{
					error(badLoad);
				}
			}
		});

		// in update state? (this one must be after adventure and last page)
		preferences.add(new Preference<Component>()
		{
			@Override
			public void load(String loadString)
			{
				if ("u".equals(loadString))
					setState(State.UPDATE); // don't need to check if I already
											// know there's an unread update
				else
				{
					setState(State.NO_UPDATE);
					scheduleCheck(false);
				}
			}

			@Override
			public String getSaveString()
			{
				return state == State.UPDATE ? "u" : "n";
			}
		});
	}

	@Override
	public void add()
	{
		checkInterval = DEFAULT_CHECK_INTERVAL;
		setState(State.NO_UPDATE);
		scheduleCheck(true);
	}

	@Override
	public void load(String string)
	{
		String[] vars = string.split(";");
		if (vars.length != preferences.size())
		{
			error(badLoad);
			return;
		}
		for (int i = 0; i < preferences.size(); i++)
		{
			preferences.get(i).load(vars[i]);
		}
	}

	/**
	 * Sets the dock icon to <code>dockIcon</code>. Waits
	 * <code>animTimeMillis</code> milliseconds, blocking the icon from being
	 * changed again, then updates the icon to match the state, if
	 * <code>animTimeMillis > 0</code>. Otherwise, just leaves the icon as the
	 * given icon.
	 */
	private void changeIcon(Icon dockIcon, ImageIcon cardIcon,
			long animTimeMillis)
	{
		if (iconBlocked) return; // icon blocked; don't change it

		dock_icon.setIcon(dockIcon);
		card_icon.setIcon(cardIcon);

		// it seems to work without refreshing/repainting the dock
		// so i'll leave this commented out.
		// refreshDoc() also seems to show the dock, and I don't want it to be
		// popping up every time a check is run
		// (^ also, I just noticed that I forgot the k in refreshDock(). I think
		// I'll leave it. :P)
		// m.refreshDock();
		// m.refreshCardHolder();
		if (animTimeMillis <= 0) return;
		iconBlocked = true;
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				// not in event thread
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						iconBlocked = false;
						updateIcon();
					}
				});
			}
		}, animTimeMillis);
	}

	/** Updates the icon to that of the current state. */
	private void updateIcon()
	{
		changeIcon(state.getDockIcon(), state.getCardIcon(), 0);
	}

	public void setState(State state)
	{
		this.state = state;
		updateIcon();
		System.out.println("UpdateChecker: state = " + state.toString());
		if (state == State.UPDATE && showDock) deck.refreshDock(); // show dock;
																	// showDock()
																	// doesn't
																	// seem to
																	// keep it
																	// visible
																	// (if
																	// auto-hide
																	// is on)
		if (state == State.UPDATE)
		{
			System.out.println("creating bubble");
			bubble = new Bubble(deck, dock_icon, 0);
			JLabel label = new JLabel("<HTML><FONT COLOR=#FF0000>!</FONT></HTML>");
			label.setBounds(0, 0, 80, 20);
			label.setHorizontalAlignment(JLabel.CENTER);
			bubble.getContents().add(label);
			bubble.setActionCommand("updatechecker_bubble");
			bubble.setActionListner(this);
		}
	}

	public void setError(Throwable e)
	{
		error = e;
	}

	public void error(Throwable e)
	{
		setState(State.ERROR);
		setError(e);
	}

	/** Sets the check interval and reschedules the check. */
	public void setCheckInterval(int interval)
	{
		if (interval != checkInterval)
		{
			checkInterval = interval;
			scheduleCheck(false);
		}
	}

	/**
	 * Cancels the current <code>Check</code>, schedules a new one based on the
	 * parameters, and sets the new one to <code>check</code>.
	 * 
	 * @param adventure
	 *            the number of the current adventure
	 * @param lastPage
	 *            the number of the last page read
	 * @param justGetInfo
	 *            if <code>true</code>, uses the first check just to get
	 *            up-to-date (see {@link Check#run()})
	 */
	private void scheduleCheck(boolean justGetInfo)
	{
		if (check != null) check.cancel();
		check = new Check(justGetInfo);
		timer.schedule(check, 0, TimeUnit.SECONDS.toMillis(checkInterval));
	}

	private void reactToCheck(Check.Status status)
	{
		State newState;
		if (status.wasSuccessful())
		{
			boolean update = false;
			if (status.getNewAdventure() > adventure) // new adventure!
			{
				setAdventure(status.getNewAdventure());
				update = true;
			}
			if (status.getNewLastPage() > lastPage) // update!
			{
				setLastPage(status.getNewLastPage());
				update = true;
			}
			newState = update ? State.UPDATE : State.NO_UPDATE;
			/*
			 * If there's an update, I want to cancel the TimerTask. I don't
			 * need to check for another update until they've looked at this
			 * one. TODO: Perhaps I should add a time limit on this, if they
			 * check independent of the widget. Also, it would be nice to be
			 * able to just set the update as read without opening the browser,
			 * for the same reason. When the user goes to MSPA after an update
			 * is detected, I want to resume checking for further updates.
			 * However, it is possible that there's been an update between when
			 * the update was detected and when the user went to the site. So if
			 * I just start checking again, I could detect an update that the
			 * user has already seen. So when a new Check is created after the
			 * user goes to read an update, I want to use the first check to
			 * just get the Check up to date and not to actually sound an alert.
			 */
			if (status.justGetInfo())
			{
				setLastReadAdventure(getAdventure());
				setLastReadPage(getLastPage());
				newState = State.NO_UPDATE;
			}
			else if (newState == State.UPDATE)
			{
				check.cancel();
			}
		}
		else
		{
			newState = status.isNoConnection() ? State.NO_CONNECTION
					: State.ERROR;
			setError(status.getError());
		}
		setState(newState);
	}

	/**
	 * Open a dialog allow user to set preferences. Current preferences: <list>
	 * <item>Check interval: time between successive checks.</item> </list>
	 */
	private void preferencesDialog()
	{
		// components to update each preference
		List<Component> preferenceComponents = new ArrayList<Component>();
		for (Preference<?> preference : preferences)
			if (preference.inputBox() != null)
				preferenceComponents.add(preference.inputBox());
		if (JOptionPane.showConfirmDialog(null, preferenceComponents.toArray(),
				"Set Update Checker Preferences", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION)
			for (Preference<?> preference : preferences)
				preference.applyChanges();
	}

	/**
	 * If the state is UPDATE opens MSPA in the default browser. If the state is
	 * NO_UPDATE, opens the preferences dialog. If the state, is NO_CONNECTION
	 * or ERROR, outputs the error.
	 */
	@Override
	public void open(OpenReason reason)
	{
		switch (state)
		{
			case UPDATE:
				scheduleCheck(true);
			case NO_UPDATE:
			{
				if (Desktop.isDesktopSupported())
				{
					Desktop desktop = Desktop.getDesktop();
					if (desktop.isSupported(Desktop.Action.BROWSE)) try
					{
						// User: Open browser and go to mspaintadventures.com
						desktop.browse(website.getURI(this));
						return;
					}
					catch (IOException e)
					{
						error(e);
						return;
					}
				}
				JOptionPane.showMessageDialog(null, "Unable to open browser.",
						"Browse Not Supported", JOptionPane.ERROR_MESSAGE);
			}
				break;
			case NO_CONNECTION:
			case ERROR:
				error.printStackTrace();
				break;
		}
	}

	@Override
	public String getName()
	{
		return "Update Checker";
	}

	@Override
	public String getSaveString()
	{
		StringBuffer saveString = new StringBuffer();
		for (Preference<?> preference : preferences)
			saveString.append(preference.getSaveString() + ";");
		return saveString.toString();
	}

	public JPanel getPanel()
	{
		return panel;
	}
	
	public ArrayList<MenuItem> getExtraMenuItems()
	{
		ArrayList<MenuItem> items = new ArrayList<MenuItem>();
		
		MenuItem checkNow = new MenuItem("Check for Updates Now");
		checkNow.setActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				timer.schedule(new Check(false), 0);
			}
		});
		items.add(checkNow);
		MenuItem markAsRead = new MenuItem("Mark as Read");
		markAsRead.setActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				scheduleCheck(true);
			}
		});
		items.add(markAsRead);
		MenuItem preferencesItem = new MenuItem("Preferences");
		preferencesItem.setActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				preferencesDialog();
			}
		});
		items.add(preferencesItem);
		MenuItem help = new MenuItem("Help");
		help.setActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (Desktop.isDesktopSupported())
				{
					try
					{
						Desktop.getDesktop().open(HELP_FILE);
					}
					catch (IOException ex)
					{
						ex.printStackTrace();
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null,
							"See Instructions.txt in the widgets/UpdateChecker folder.",
							"Help", JOptionPane.PLAIN_MESSAGE);
				}
			}
		});
		items.add(help);
		return items;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals("updatechecker_bubble"))
		{
			bubble = null;
			scheduleCheck(true);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
}
