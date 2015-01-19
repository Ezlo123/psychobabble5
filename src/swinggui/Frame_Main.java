package swinggui;

import game.*;
import gameLogic.TransferLogic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.synth.SynthLookAndFeel;

import libraryClasses.Team;
import xmlIO.XMLParser;

//import aurelienribon.tweenengine.Tween;
//import aurelienribon.tweenengine.TweenManager;

@SuppressWarnings("serial")
public class Frame_Main extends JFrame implements ActionListener{
	
	private JPanel curPanel;
	private String current;
	private Competition curComp;
	private Team curTeam;
	private int roundNum;
	private String playerName;
	private GameList games;
	private Game currentGame;
	
	public Dimension minSize = new Dimension(20,20);
	public Dimension prefSize = new Dimension(40,20);
	
	/**Function to create ImageIcons, just in case I have trouble finding them again
	 * @param path			- ImageIcon file location
	 * @return				- new ImageIcon object
	 */
	public ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, null);
		}
		else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
	public Frame_Main() {
		current = "nada";
		
		roundNum = 0;
		
		// Currently only supports one season
		games = XMLParser.readGameList("files/saves_v6.xml");
		
		
		initUI();
	}
	
	public final void initUI(){
		//set Look And Feelsv
		SynthLookAndFeel synth = new SynthLookAndFeel();
		try {
			synth.load(Frame_Main.class.getResourceAsStream("lookandfeel.xml"), Frame_Main.class);
		} catch (ParseException e) {
			System.out.println("lookandfeel.xml not found");
		}
		try {
			UIManager.setLookAndFeel(synth);
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		//initialize some stuff
		setTitle("Football Manager 2015");
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		setMinimumSize(new Dimension(1024, 720));
		setSize(1280, 800);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
//		TeamChoicePanel teamChoose = new TeamChoicePanel(curComp, this);
//		curPanel = teamChoose;
//		add(curPanel, BorderLayout.CENTER);

		boolean showLoadedGames = games.getGames().size() > 0;
		
		SplashPanel splashChoice = new SplashPanel(this, showLoadedGames);
		curPanel = splashChoice;
		add(curPanel, BorderLayout.CENTER);
			
	}
	
	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable() {
			@Override 
			public void run() {
				Frame_Main Mainwindow = new Frame_Main();
				Mainwindow.setVisible(true);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton possibleMenuB = (JButton) e.getSource();
			
			switch (possibleMenuB.getText()) {
				case "Load Game":

					loadLoadScreen();
					
					break;
				case "New Game":
					
					loadNameChoiceScreen();
					
					break;
				case "Pick your team":
					
					// grab name from NamePanel, then on to choosing team
					playerName = (String)possibleMenuB.getClientProperty("playerName");
					
					loadNewGameScreen();
					
					break;
				case "Play as this team":					
					String teamName = (String)possibleMenuB.getClientProperty("teamName");
										
					loadMainScreenNewGame(teamName);
					
					break;
				case "Load This Game":
					
					int gameIndex = (int)possibleMenuB.getClientProperty("gameIndex");
					
					loadMainScreenLoadedGame(gameIndex);
					
					break;
				case "overview ":					
					loadOverView();
					
					break;
				case "statistics":
					loadStatisticsView();
					
					break;
				case "":
					// Should this be encapsulated by a game logic class method?
					// For the demo this runs through the entire season (round by round)
					
					if (roundNum < 38) {
						curComp.playRound();
						TransferLogic.AutoTransfer(curTeam, curComp.getLibrary());
						roundNum++;
					} else {
						// trigger an event signalling the start of the
						// next season?
					}
										
					// Then display statistics page showcasing the results of the season
					current = "match";
					
					currentGame.save();
					XMLParser.writeGameList("files/saves_v6.xml", games);
					
					// Initialize new JPanel and remove current pane
					MatchPanel replPlayView = new MatchPanel(curComp, curTeam);
					remove(curPanel);
					
					// Refresh the view
					add(replPlayView, BorderLayout.CENTER, 1);
					curPanel = replPlayView;
					revalidate();
					repaint();
					
					break;
				case "positions ":
					loadPositionsView();
					
					break;
				case "transfers":
					loadTransfersView();
					
					break;
				default:
					System.out.println("This might not be a menu bar item!");
					break;
			}
		}
		
	}
	
	public void loadLoadScreen() {
		LoadPanel loadYaGame = new LoadPanel(games, this);
		
		remove(curPanel);
		
		curPanel = loadYaGame;
		add(curPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}
	
	public void loadNameChoiceScreen() {
		NamePanel nameSelection = new NamePanel(this);
		
		remove(curPanel);
		
		curPanel = nameSelection;
		add(curPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}
	
	public void loadNewGameScreen() {
		
		// grab name from current 
		
		TeamChoicePanel teamChoose = new TeamChoicePanel(curComp, this);
		
		remove(curPanel);
		
		curPanel = teamChoose;
		add(curPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}
	
	public void loadMainScreenLoadedGame(int gameIndex) {
		// Create the new game
		currentGame = games.get(gameIndex);
		
		curComp = currentGame.getCompetition();
		curTeam = currentGame.getTeam();
		
		currentGame.save();
		XMLParser.writeGameList("files/saves_v6.xml", games);
		roundNum = curComp.getRoundsPlayed();
		
		remove(curPanel);
		// Start playing!
		// Get screen sizes (for fullscreen)
		Toolkit tk = Toolkit.getDefaultToolkit();
		int boxwidth = (int) tk.getScreenSize().getWidth();
		int boxheight = (int) tk.getScreenSize().getHeight();
		
		//Header panel
		Header header = new Header(this);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		add(header);
		
		//Center panel begins here
		OverviewPanel overviewPanel = new OverviewPanel(curComp);
		
		// set current screen string
		current = "overview";
		
		//Center panel ends here
		curPanel = overviewPanel;
		add(curPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}
	
	public void loadMainScreenNewGame(String chosenTeam) {
		curTeam = curComp.getLibrary().getTeamForName(chosenTeam);
		
		// Create the new game
		currentGame = games.newgame(playerName, chosenTeam);
		currentGame.save();
		XMLParser.writeGameList("files/saves_v6.xml", games);
		
		remove(curPanel);
		// Start playing!
		// Get screen sizes (for fullscreen)
		Toolkit tk = Toolkit.getDefaultToolkit();
		int boxwidth = (int) tk.getScreenSize().getWidth();
		int boxheight = (int) tk.getScreenSize().getHeight();
		
		//Header panel
		Header header = new Header(this);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		add(header);
		
		//Center panel begins here
		OverviewPanel overviewPanel = new OverviewPanel(curComp);
		
		// set current screen string
		current = "overview";
		
		//Center panel ends here
		curPanel = overviewPanel;
		add(curPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}
	
	public void loadOverView() {
		if (!current.equals("overview")) {
			current = "overview";
			
			currentGame.save();
			XMLParser.writeGameList("files/saves_v6.xml", games);
			
			// Initialize new JPanel and remove current pane
			OverviewPanel replOverview = new OverviewPanel(curComp);
			remove(curPanel);
			
			// Refresh the view
			add(replOverview, BorderLayout.CENTER, 1);
			curPanel = replOverview;
			revalidate();
			repaint();
		}
	}
	
	public void loadStatisticsView() {
		// Switch to statistics panel if not current
		if (!current.equals("statistics")) {
			current = "statistics";
			
			currentGame.save();
			XMLParser.writeGameList("files/saves_v6.xml", games);
			
			// Initialize new JPanel and remove current pane
			StatisticsPanel replStatview = new StatisticsPanel(curComp);
			remove(curPanel);
			
			// Refresh the view
			add(replStatview, BorderLayout.CENTER, 1);
			curPanel = replStatview;
			revalidate();
			repaint();
		}
	}
	
	public void loadPositionsView() {
		// Switch to positions panel if not current
		if (!current.equals("positions")) {
			current = "positions";
			
			currentGame.save();
			XMLParser.writeGameList("files/saves_v6.xml", games);
			
			// Initialize new JPanel and remove current pane
			PositionsPanel replPositsview = new PositionsPanel(curTeam);
			remove(curPanel);
			
			// Refresh the view
			add(replPositsview, BorderLayout.CENTER, 1);
			curPanel = replPositsview;
			revalidate();
			repaint();
		}
	}
	
	public void loadTransfersView() {
		// Switch to transfers panel if not current
		if (!current.equals("transfers")) {
			current = "transfers";
			
			currentGame.save();
			XMLParser.writeGameList("files/saves_v6.xml", games);
			
			// Initialize new JPanel and remove current pane
			TransfersPanel replTransfview = new TransfersPanel(curTeam, curComp);
			remove(curPanel);
			
			// Refresh the view
			add(replTransfview, BorderLayout.CENTER, 1);
			curPanel = replTransfview;
			
//			//temporary, for the help text or something, I'll fix it later
//			JPanel Helper = new JPanel();
//			Helper.setLayout(new BoxLayout(Helper, BoxLayout.X_AXIS));
//			Helper.add(new Box.Filler(minSize, prefSize, null));
//			JPanel HelperBox = new JPanel();
//			HelperBox.setName("Panel"); HelperBox.setOpaque(false);
//			HelperBox.setMinimumSize(new Dimension(200,130));
//			HelperBox.setPreferredSize(new Dimension(1200,130));
//			HelperBox.setMaximumSize(new Dimension(2000,130));
//			Helper.add(HelperBox);
//			Helper.add(new Box.Filler(minSize, prefSize, null));
//			add(Helper);
			
//			//southern space
//			Component test = Box.createRigidArea(new Dimension(this.getWidth(), 108));
//			test.setBackground(new Color(0,0,0,200));
//			add(test);
			
			
			
			revalidate();
			repaint();
		}
	}

}
