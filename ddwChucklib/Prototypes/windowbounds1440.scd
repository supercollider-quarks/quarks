
// set gui window bounds for mbp 1440x900 layout

~controlpanelBounds = switch(Platform.ideName.asSymbol)
	{ \scel } { Rect(668, 4, 650, 852) }
	{ Rect(534, 4, 916, 852) };
~mixerBounds = Rect(-6, -27, 546, 264);	// change
~codedocBounds = Rect(1, 260, 534, 397);
~postwindowBounds = Rect(1, 658, 675, 220);

// replace default definition with small skin (horizontal)
MixerChannelGUI.defaultDef = MixerGUIDef(Point(540, 25),
	[MixerMuteWidget, MixerRecordWidget, MixerNameWidget, MixerPresendWidget,
		MixerPanWidget, MixerLevelSliderH, MixerLevelNumber, MixerPostsendWidget,
		MixerOutbusWidget],
	[	Rect(5, 5, 20, 20),
		Rect(30, 5, 20, 20),
		Rect(55, 10, 60, 15),  // 115
		Rect(120, 0, 60, 20),  // 190
		Rect(185, 10, 50, 15), // 245
		Rect(240, 10, 100, 15), // 340
		Rect(345, 10, 50, 15), // 395
		Rect(400, 0, 70, 20),  // 470
		Rect(475, 10, 60, 15)  // 535
	]);

MixerChannelDef.at(\mix1x1).guidef = MixerGUIDef(Point(440, 25),
	[MixerMuteWidget, MixerRecordWidget, MixerNameWidget, MixerPresendWidget,
		MixerLevelSliderH, MixerLevelNumber, MixerPostsendWidget,
		MixerOutbusWidget],
	[	Rect(5, 5, 20, 20),
		Rect(30, 5, 20, 20),
		Rect(55, 10, 60, 15),  // 115
		Rect(120, 0, 60, 20),  // 190
		Rect(240, 10, 100, 15), // 340
		Rect(345, 10, 50, 15), // 395
		Rect(400, 0, 70, 20),  // 470
		Rect(475, 10, 60, 15)  // 535
	]);

MixerPresendWidget.defaultSliderBounds = Rect(0, 2, 60, 5);
MixerPresendWidget.defaultMenuBounds = Rect(0, 10, 60, 15);


// parameterize VP creation
switch(Platform.ideName.asSymbol)
	{ \scel } {
		~useScroll = true;
		~voicerColumns = 1;
		~voicerRows = 14;
	}
	{ \scapp } {
		~useScroll = false;
		~voicerColumns = 2;
		~voicerRows = 7;
	};

~voicersWith4Controls = 6;
