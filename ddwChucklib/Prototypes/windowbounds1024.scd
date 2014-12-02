
// set gui window bounds for old 1024x768 layout

if(Window.screenBounds.height == 600) {
	~controlpanelBounds = Rect(428, 11, 762, 570);
	~useShortBrowser = true;
	~serverGui = false;
} {
	~controlpanelBounds = Rect(428, 11, 762, 713);
};

~mixerBounds = Rect(-7, -26, 435, 262);
~codedocBounds = Rect(2, 259, 432, 316);
~postwindowBounds = Rect(2, 574, 712, 172);

// mixing board gui def

// replace default definition with small skin (horizontal)
MixerChannelGUI.defaultDef = MixerGUIDef(Point(440, 25),
	[MixerMuteWidget, MixerRecordWidget, MixerNameWidget, MixerPresendWidget,
		MixerPanWidget, MixerLevelSliderH, MixerLevelNumber, MixerPostsendWidget,
		MixerOutbusWidget],
	[	Rect(5, 5, 15, 20),
		Rect(25, 5, 15, 20),
		Rect(45, 10, 50, 15),
		Rect(100, 0, 50, 20),
		Rect(155, 10, 40, 15),
		Rect(200, 10, 65, 15),
		Rect(270, 10, 40, 15),
		Rect(315, 0, 50, 20),
		Rect(370, 10, 50, 15)
	]);

MixerChannelDef.at(\mix1x1).guidef = MixerGUIDef(Point(440, 25),
	[MixerMuteWidget, MixerRecordWidget, MixerNameWidget, MixerPresendWidget,
		MixerLevelSliderH, MixerLevelNumber, MixerPostsendWidget,
		MixerOutbusWidget],
	[	Rect(5, 5, 15, 20),
		Rect(25, 5, 15, 20),
		Rect(45, 10, 50, 15),
		Rect(100, 0, 50, 25),
		Rect(200, 10, 65, 15),
		Rect(270, 10, 40, 15),
		Rect(315, 0, 50, 20),
		Rect(370, 10, 50, 15)
//		Rect(200, 10, 80, 15),	// old, deprecated
//		Rect(285, 10, 40, 15),
//		Rect(330, 0, 50, 25),
//		Rect(385, 10, 50, 15)
	]);

MixerPresendWidget.defaultSliderBounds = Rect(0, 2, 50, 5);
MixerPresendWidget.defaultMenuBounds = Rect(0, 10, 50, 15);


// parameterize VP creation: multiple columns?

~voicerColumns = 1;
~voicerRows = 14;
~voicersWith4Controls = 6;
~useScroll = true;
