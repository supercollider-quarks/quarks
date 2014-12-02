// Main Class of the Chordtris Game
Chordtris
{
	// game instance
	classvar instance;
	
	// menu item to pause the game
	classvar <pauseItem;
	
	// menu item to resume the game
	classvar <resumeItem;
	
	// The Window Chordtris is displayed in
	var <window;
	
	// Composite View which contains the other views
	var <compositeView;
	
	// The tetris view
	var view;
	
	// Next brick view
	var <nextBrickView;
	
	// NumberBox displaying the level
	var <levelBox;
	
	// NumberBox displaying the number of lines
	var <lineBox;
	
	// NumberBox displaying the score
	var <scoreBox;
	
	// the current game screen (a subclass of ChordGameScreen)
	var currentScreen;
	
	// the main game screen
	var <gameScreen;
	
	// number of matrix columns
	var numColumns = 10;
	
	// number of matrix rows
	var numRows = 20;
	
	// size of a matrix square
	var squareSize = 30;
	
	// the index of the currently selected menu item
	var selectedMenuItem = 0;
	
	// width of the side panel next to the game matrix
	var sidePanelWidth = 200;
	
	// height of the preview section
	var previewHeight = 250;
	
	*new { ^super.new.init }
	
	*initClass {
		if(Platform.ideName == "scapp") {
			StartUp.add { this.createMenu }
		}
	}
	
	*createMenu {
		var menuGroup, startItem, preferencesItem, helpItem;
		menuGroup = SCMenuGroup(nil, "Chordtris", 13);
		startItem = SCMenuItem(menuGroup, "New Game");
		startItem.setShortCut("N");
		startItem.action = { this.newGame };
		
		pauseItem = SCMenuItem(menuGroup, "Pause");
		//pauseItem.setShortCut("P");
		pauseItem.enabled_(false);
		pauseItem.action = { this.pause };
		
		resumeItem = SCMenuItem(menuGroup, "Resume");
		//resumeItem.setShortCut("R");
		resumeItem.enabled_(false);
		resumeItem.action = { this.resume };
	
		preferencesItem = SCMenuItem(menuGroup, "Preferences");
		preferencesItem.setShortCut(",");
		preferencesItem.action = { this.openPreferences };
		
		//var menuGroup = SCMenuGroup(nil, "Help", 14);
		helpItem = SCMenuItem(menuGroup, "Help");
		helpItem.action_ { this.help };
		helpItem.setShortCut("D");
		
	}
	
	*newGame {
		if(instance.isNil) {
			// boot server if not booted already and create new game instance
			Server.default.waitForBoot({
				instance = Chordtris.new;
			});
		} {
			instance.newGame;
		}
	}
	
	*pause {
		if(instance.notNil) { instance.pause };
	}
	
	*resume {
		if(instance.notNil) { instance.resume };
	}
	
	*openPreferences {
		ChordtrisPreferenceDialog.new;
	}
	
	*help {
		//HelpBrowser.goTo(Platform.helpDir +/+ "Help.html");
		HelpBrowser.goTo(SCDoc.helpTargetDir +/+ "Other" +/+ "Chordtris.html");
	}
	
	init
	{
		this.initWindow;
		this.initScreens;
		this.initAnimation;
	}
	
	unregisterInstance {
		instance = nil;
	}
	
	setPauseMenuItemEnabled { |enabled|
		if(pauseItem.notNil) { { pauseItem.enabled_(enabled) }.defer };
	}
	
	setResumeMenuItemEnabled { |enabled|
		if(resumeItem.notNil) { { resumeItem.enabled_(enabled) }.defer };
	}
	
	newGame {
		gameScreen.reset;
	}
	
	pause {
		gameScreen.pause;
	}
	
	resume {
		gameScreen.resume;
	}
	
	initScreens {
		// Init screens
		//menuScreen = ChordtrisMenu.new(this);
		gameScreen = ChordtrisEngine.new(this, numRows, numColumns, squareSize);
		currentScreen = gameScreen;
	}
	
	initAnimation {
		var fps = 30;
		
		// TODO: set fps_ of view if QT is used
		thisProcess.setDeferredTaskInterval(fps.reciprocal);
		view.drawFunc = { currentScreen.animate };
		view.animate_(true);
		
	}
	
	initWindow {
		var bounds;
		var matrixWidth = numColumns * squareSize;
		var matrixHeight = numRows * squareSize;
		
		var bgcolor = Color(0.3, 0.3, 0.3);
		var yellow = Color.new(0.86, 0.73, 0.39);
		var darkGrey = Color(0.2, 0.2, 0.2);
		
		var boxSpace = 20;
		
		
		// create Window and Views
		window = Window("Chordtris", Rect(200, 100, matrixWidth + sidePanelWidth, matrixHeight)).front;
		window.background_(bgcolor);
		bounds = window.bounds;
		
		compositeView = CompositeView(window, Rect(0, 0, bounds.width, bounds.height));
		
		// main Tetris matrix view
		view = UserView(compositeView, Rect(0, 0, matrixWidth, bounds.height));
		view.background_(Color.black);
		
		
		// view to show next brick
		nextBrickView = UserView(compositeView, Rect(300, 0, sidePanelWidth, previewHeight));
		nextBrickView.background_(darkGrey);
		
		// widgets to show the scores
		StaticText(compositeView, Rect(matrixWidth,previewHeight + 10,sidePanelWidth,30)).font_(Font("Courier", 22)).align_(\center).stringColor_(Color.white).string_("Level:");
		
		levelBox = StaticText(compositeView, Rect(matrixWidth + boxSpace, previewHeight + 50, sidePanelWidth - (2*boxSpace), 40)).font_(Font("Courier", 30)).align_(\center).stringColor_(yellow).background_(darkGrey).string_(0);
		
		StaticText(compositeView, Rect(matrixWidth,previewHeight + 110,sidePanelWidth,30)).font_(Font("Courier", 22)).align_(\center).stringColor_(Color.white).string_("Lines:");
		
		lineBox = StaticText(compositeView, Rect(matrixWidth + boxSpace,previewHeight + 150, sidePanelWidth - (2*boxSpace), 40)).font_(Font("Courier", 30)).align_(\center).stringColor_(yellow).background_(darkGrey).string_(0);
		
		StaticText(compositeView, Rect(matrixWidth,previewHeight + 210,sidePanelWidth,30)).font_(Font("Courier", 22)).align_(\center).stringColor_(Color.white).string_("Score:");
		
		scoreBox = StaticText(compositeView, Rect(matrixWidth + boxSpace,previewHeight + 250, sidePanelWidth - (2*boxSpace), 40)).font_(Font("Courier", 30)).align_(\center).stringColor_(yellow).background_(darkGrey).string_(0);
		
		// Set key handling function
		window.view.keyDownAction = {|view, char, modifiers, unicode, keycode|
			currentScreen.handleKeyDown(view, char, modifiers, unicode, keycode);
		}
	}
}