import java.awt.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import sylladex.Main;
import sylladex.Widget;

public class UpdateChecker extends Widget
	{
		/** MSPA rss feed */
		private static final URI RSS;
		/** MSPA website */
		private static final URI WEBSITE;
		static
			{
				URI rss = null, website = null; //temp variables that aren't final for the try block
				try
					{
						rss = new URI("http", "//www.mspaintadventures.com/rss/rss.xml", null);
						website = new URI("http", "//www.mspaintadventures.com/", null);
					}
				catch(URISyntaxException e)
					{
						e.printStackTrace();
					}
				RSS = rss;
				WEBSITE = website;
			}
		/** The default interval at which to check for updates (5 minutes). */
		private static final int DEFAULT_CHECK_INTERVAL = 300;
		public static enum State
			{
				UPDATE, NO_UPDATE, NO_CONNECTION, ERROR;
				public static final String folder = "widgets/UpdateChecker/";
				private Icon dockIcon = Main.createImageIcon(folder + "doc_" + toString() + ".gif");
				private ImageIcon cardIcon = Main.createImageIcon(folder + "card_" + toString() + ".gif");
				public Icon getDockIcon()
					{
						return dockIcon;
					}
				public ImageIcon getCardIcon()
					{
						return cardIcon;
					}
			}

		/**
		 * This performs the actual check, and notifies the <code>UpdateChecker</code> if
		 * something's changed.
		 */
		private class Check extends TimerTask
			{
				// see run()
				private boolean justRead = false;

				/** Icon used when a check is done. */
				public Icon dockCheckIcon = Main.createImageIcon(State.folder + "doc_check.gif");
				public ImageIcon cardCheckIcon = Main.createImageIcon(State.folder + "card_check.gif");

				/*
				 * Note: card icons are sized to fit the default card size, since they're
				 * animations, and resizing doesn't seem to work on them.
				 */

				/** number after "s=" for the latest known page */
				private int adventure;
				/** number after "p=" for the latest known page */
				private int lastPage;

				public Check(int adventure, int lastPage, boolean justRead)
					{
						this.adventure = adventure;
						this.lastPage = lastPage;
						this.justRead = justRead;
					}

				public int getAdventure()
					{
						return adventure;
					}
				public int getLastPage()
					{
						return lastPage;
					}

				/**
				 * The result of a check: the current state, and, if applicable, what went
				 * wrong.
				 */
				private class Status
					{
						private State state;
						private Throwable error;
						public Status(State state, Throwable error)
							{
								this.state = state;
								this.error = error;
							}
						public Status(State newState)
							{
								this(newState, null);
							}
						public Status(Throwable e)
							{
								this(State.ERROR, e);
							}
					}

				private Status check()
					{
						if(!justRead) //if justRead, not really a check
							SwingUtilities.invokeLater(new Runnable(){
								@Override
								public void run()
									{
										//check icon takes 600 ms to play
										changeIcon(dockCheckIcon, cardCheckIcon, 600);
									}
							});
						try
							{
								InputStream rssStream = null;
								try
									{
										rssStream = RSS.toURL().openStream();
										//only need digits; ignore non-digits for next()
										Scanner scanner = new Scanner(rssStream).useDelimiter("\\D+");
										//exception used to indicate that the RSS isn't what I expected
										Exception rssProblem = new Exception("Problem reading RSS feed");

										if(scanner.findWithinHorizon("\\Q<link>http://www.mspaintadventures.com/?s=\\E", 1000) == null)
											return new Status(rssProblem);
										if(!scanner.hasNextInt())
											return new Status(rssProblem);
										int latestAdventure = scanner.nextInt();
										boolean update = false;
										if(latestAdventure > adventure) //new adventure!
											{
												adventure = latestAdventure;
												update = true;
											}
										int latestPage = scanner.nextInt();
										if(latestPage > lastPage) //update!
											{
												lastPage = latestPage;
												update = true;
											}
										return new Status(update ? State.UPDATE : State.NO_UPDATE);
									}
								//I think this indicates there isn't an internet connection?
								//I found it from a Google search
								//It works for me, at least
								catch(UnknownHostException e)
									{
										return new Status(State.NO_CONNECTION, e);
									}
								catch(MalformedURLException e)
									{
										return new Status(e);
									}
								finally
									{
										if(rssStream != null)
											rssStream.close();
									}
							}
						catch(IOException e)
							{
								return new Status(e);
							}
					}

				@Override
				public void run()
					{
						System.out.println("UpdateChecker: Check run");
						final Status status = check(); //check for update
						/*
						 * If there's an update, I want to cancel the TimerTask. I don't
						 * need to check for another update until they've looked at this
						 * one. TODO: Perhaps I should add a time limit on this, if they
						 * check independent of the widget. Also, it would be nice to be
						 * able to just set the update as read without opening the
						 * browser, for the same reason.
						 * When the user goes to MSPA after an update is detected, I want
						 * to resume checking for further updates. However, it is possible
						 * that there's been an update between when the update was
						 * detected and when the user went to the site. So if I just start
						 * checking again, I could detect an update that the user has
						 * already seen. So when a new Check is created after the user
						 * goes to read an update, I want to use the first check to just
						 * get the Check up to date and not to actually sound an alert.
						 */
						if(status.state == State.UPDATE)
							if(justRead)
								{
									justRead = false;
									return;
								}
							else
								cancel();
						SwingUtilities.invokeLater(new Runnable(){
							@Override
							public void run() //update UpdateChecker
								{
									setState(status.state);
									setError(status.error);
								}
						});
					}
			}

		// timer thread is daemon: want to run as long as the sylladex is open but no longer
		private Timer timer = new Timer(true);
		/**
		 * Icon can't be changed yet, because the check icon is in the process of running.
		 */
		private boolean iconBlocked = false;
		/** JPanel holding the icon for the card. */
		private JLabel card_icon = new JLabel();

		//below variables should all be set in load()/prepare()
		/** number of seconds between checks. */
		private int checkInterval;
		/** current state of the <code>UpdateChecker</code> */
		private State state;
		/** If an exception occurs, goes into ERROR state and stores the exception here */
		private Throwable error;
		/** the most recent Check to check the site */
		private Check check;

		/**
		 * Sets the dock icon to <code>dockIcon</code>. Waits <code>animTimeMillis</code>
		 * milliseconds, blocking the icon from being changed again, then updates the icon
		 * to match the state, if <code>animTimeMillis > 0</code>. Otherwise, just leaves
		 * the icon as the given icon.
		 */
		private void changeIcon(Icon dockIcon, ImageIcon cardIcon, long animTimeMillis)
			{
				if(iconBlocked)
					return; //icon blocked; don't change it

				dock_icon.setIcon(dockIcon);
				card_icon.setIcon(cardIcon);

				//it seems to work without refreshing/repainting the dock
				//so i'll leave this commented out.
				//refreshDoc() also seems to show the dock, and I don't want it to be popping up every time a check is run
				//(^ also, I just noticed that I forgot the k in refreshDock(). I think I'll leave it. :P)
				//m.refreshDock();
				//m.refreshCardHolder();
				if(animTimeMillis <= 0)
					return;
				iconBlocked = true;
				timer.schedule(new TimerTask(){
					@Override
					public void run()
						{
							//not in event thread
							SwingUtilities.invokeLater(new Runnable(){
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
				checkInterval = interval;
				check.cancel();
				scheduleCheck(check.getAdventure(), check.getLastPage(), false);
			}

		/**
		 * Schedules a check based on the parameters and sets it to <code>check</code>.
		 * @param adventure the number of the current adventure
		 * @param lastPage the number of the last page read
		 * @param justRead if <code>true</code>, uses the first check just to get
		 *        up-to-date (see {@link Check#run()})
		 */
		private void scheduleCheck(int adventure, int lastPage, boolean justRead)
			{
				check = new Check(adventure, lastPage, justRead);
				timer.schedule(check, 0, TimeUnit.SECONDS.toMillis(checkInterval));
			}

		/** Initialization; called by both prepare() and load(). */
		private void init()
			{
				panel.setOpaque(false);
				panel.add(card_icon);
			}

		@Override
		public void prepare()
			{
				checkInterval = DEFAULT_CHECK_INTERVAL;
				setState(State.NO_UPDATE);
				scheduleCheck(0, 0, true); //use first check to get up-to-date
				init();
			}
		@Override
		public void load(String string)
			{
				String[] vars = string.split(";");
				//exception for if the load doesn't work
				Exception badLoad = new Exception("Error reading saved data.");
				if(vars.length != 4)
					{
						error(badLoad);
						return;
					}
				int adventure, lastPage;
				try
					{
						checkInterval = Integer.parseInt(vars[1]);
						adventure = Integer.parseInt(vars[2]);
						lastPage = Integer.parseInt(vars[3]);
					}
				catch(NumberFormatException e)
					{
						error(badLoad);
						return;
					}
				if("u".equals(vars[0]))
					{
						setState(State.UPDATE); //don't need to check if I already know there's an unread update
						//make a Check but don't run it; just to keep track of adventure/last page
						check = new Check(adventure, lastPage, true);
					}
				else
					{
						setState(State.NO_UPDATE);
						scheduleCheck(adventure, lastPage, false);
					}
				init();
			}

		/**
		 * Open a dialog allow user to set preferences. Current preferences: <list>
		 * <item>Check interval: time between successive checks.</item> </list>
		 */
		//TODO: atm, only opens if NO_UPDATE. would be nice to be able to open whenever.
		private void preferencesDialog()
			{
				//components to update each preference
				List<Component> preferences = new ArrayList<Component>();
				//component for interval; holds label & spinner
				Box interval = new Box(BoxLayout.X_AXIS);
				preferences.add(interval);
				/*
				 * min: 0 (each check comes immediately after the last)
				 * max: 2^31-1 (don't want to overflow)
				 * step: 1 second (don't think you really need to be more precise)
				 */
				SpinnerNumberModel intervalSpinner = new SpinnerNumberModel(checkInterval, 0, Integer.MAX_VALUE, 1);
				interval.add(new JLabel("Check Interval (seconds):"));
				interval.add(new JSpinner(intervalSpinner));
				JOptionPane.showMessageDialog(null, preferences.toArray(), "Set Update Checker Preferences", JOptionPane.PLAIN_MESSAGE);
				setCheckInterval(intervalSpinner.getNumber().intValue());
			}

		/**
		 * If the state is UPDATE opens MSPA in the default browser. If the state is
		 * NO_UPDATE, opens the preferences dialog. If the state, is NO_CONNECTION or
		 * ERROR, outputs the error.
		 */
		@Override
		public void open()
			{
				switch(state)
					{
						case UPDATE: {
							setState(State.NO_UPDATE);
							scheduleCheck(check.getAdventure(), check.getLastPage(), true);
							if(Desktop.isDesktopSupported())
								{
									Desktop desktop = Desktop.getDesktop();
									if(desktop.isSupported(Desktop.Action.BROWSE))
										try
											{
												//User: Open browser and go to mspaintadventures.com
												desktop.browse(WEBSITE);
												return;
											}
										catch(IOException e)
											{
												error(e);
												return;
											}
								}
							JOptionPane.showMessageDialog(null, "Unable to open browser.", "Browse Not Supported", JOptionPane.ERROR_MESSAGE);
						}
						break;
						case NO_UPDATE:
							preferencesDialog();
						break;
						case NO_CONNECTION:
						case ERROR:
							error.printStackTrace();
						break;
					}
			}

		@Override
		public String getString()
			{
				return "Update Checker";
			}
		@Override
		public String getSaveString()
			{
				return (state == State.UPDATE ? 'u' : 'n') + ";" + checkInterval + ";" + check.getAdventure() + ";" + check.getLastPage();
			}
	}
