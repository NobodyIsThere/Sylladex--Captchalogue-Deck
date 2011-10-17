import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
						rss = new URI(/*"http"*/"file", /*"//www.mspaintadventures.com/rss/*/"///C:/Users/Alexander/Sylladex--Captchalogue-Deck/program-dev/"+"rss.xml", null);
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
		public enum State
			{
				UPDATE, NO_UPDATE, NO_CONNECTION, ERROR
			}

		/**
		 * This performs the actual check, and notifies the <code>UpdateChecker</code> if
		 * something's changed.
		 */
		private class Check extends TimerTask
			{
				// see run()
				private boolean justRead = false;

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
										if(latestAdventure > adventure) //new adventure!
											{
												adventure = latestAdventure;
												return new Status(State.UPDATE);
											}
										int latestPage = scanner.nextInt();
										if(latestPage > lastPage) //update!
											{
												lastPage = latestPage;
												return new Status(State.UPDATE);
											}
										return new Status(State.NO_UPDATE); // :(
									}
								//I think this indicates there isn't an internet connection?
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
						 */
						/*
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

		//below variables should all be set in load(), but are initialized just in case
		/** number of seconds between checks */
		private int checkInterval = DEFAULT_CHECK_INTERVAL;
		/** current state of the <code>UpdateChecker</code> */
		private State state = null;
		/** If an exception occurs, goes into ERROR state and stores the exception here */
		private Throwable error = null;
		/** the most recent Check to check the site */
		private Check check = null;

		public void setState(State state)
			{
				this.state = state;
				dock_icon.setText(state.toString());
				panel.add(dock_icon);
				System.out.println("UpdateChecker: state = " + state.toString());
				m.refreshDock();
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

		private Check scheduleCheck(int adventure, int lastPage, boolean justRead)
			{
				Check check = new Check(adventure, lastPage, justRead);
				timer.schedule(check, 0, TimeUnit.SECONDS.toMillis(checkInterval));
				return check;
			}

		@Override
		public void prepare()
			{
				System.out.println("prepare");
				error(new IllegalStateException("UpdateChecker was not initialized."));
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
					setState(State.UPDATE); //don't need to check if I already know there's an unread update
				else
					check = scheduleCheck(adventure, lastPage, false);
			}

		/**
		 * If the state is UPDATE or NO_UPDATE, opens MSPA in the default browser. If the
		 * state, is NO_CONNECTION or ERROR, outputs the error.
		 */
		@Override
		public void open()
			{
				switch(state)
					{
						case UPDATE:
							setState(State.NO_UPDATE);
							check = scheduleCheck(check.getAdventure(), check.getLastPage(), true);
						case NO_UPDATE: {
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
