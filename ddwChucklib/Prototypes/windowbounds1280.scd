
// set gui window bounds for acer 1280x1024 layout

~controlpanelBounds = Rect(676, 4, 650, 976);
~mixerBounds = Rect(-1, -26, 676, 264);	// change
~codedocBounds = Rect(1, 261, 674, 450);
~postwindowBounds = Rect(1, 712, 674, 290);

// replace default definition with small skin (horizontal)
MixerChannelGUI.defaultDef = MixerGUIDef(Point(685, 25),
	[MixerMuteWidget, MixerRecordWidget, MixerNameWidget, MixerPresendWidget,
		MixerPanWidget, MixerLevelSliderH, MixerLevelNumber, MixerPostsendWidget,
		MixerOutbusWidget],
	[	Rect(5, 5, 20, 20),
		Rect(30, 5, 20, 20),
		Rect(55, 10, 70, 15),  // 115
		Rect(130, 0, 75, 20),  // 190
		Rect(210, 10, 80, 15), // 245
		Rect(295, 10, 160, 15), // 340
		Rect(460, 10, 50, 15), // 395
		Rect(515, 0, 75, 20),  // 470
		Rect(595, 10, 80, 15)  // 535
	]);

MixerChannelDef.at(\mix1x1).guidef = MixerGUIDef(Point(685, 25),
	[MixerMuteWidget, MixerRecordWidget, MixerNameWidget, MixerPresendWidget,
		MixerLevelSliderH, MixerLevelNumber, MixerPostsendWidget,
		MixerOutbusWidget],
	[	Rect(5, 5, 20, 20),
		Rect(30, 5, 20, 20),
		Rect(55, 10, 70, 15),  // 115
		Rect(130, 0, 75, 20),  // 190
		Rect(295, 10, 160, 15), // 340
		Rect(460, 10, 50, 15), // 395
		Rect(515, 0, 75, 20),  // 470
		Rect(595, 10, 80, 15)  // 535
	]);

MixerPresendWidget.defaultSliderBounds = Rect(0, 2, 75, 5);
MixerPresendWidget.defaultMenuBounds = Rect(0, 10, 75, 15);


// parameterize VP creation: multiple columns?

//~voicerColumns = 1;
//~voicerRows = 8;
//~voicersWith4Controls = 4;


// experimental, using scroll view

~voicerColumns = 1;
~voicerRows = 14;
~voicersWith4Controls = 6;
~useScroll = true;
