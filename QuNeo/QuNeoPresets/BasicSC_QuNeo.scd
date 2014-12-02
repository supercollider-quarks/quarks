/*
QuNeoStartup File by Jonathan Siemasko - Email: schemawound@yahoo.com - Web: http://schemawound.com/
*/

/*
This code will create a QuNeo object that matches the BasicSC_QuNeo.quneopreset file.
To use it:
1) Run the QuNeo Editor software.
2) Click one of the controls to activate the menus.
3) Use the preset dropdown to pick the QuNeo preset you wish to overwrite.
4) File-> Import Preset.
5) Navigate to \QuNeo\QuNeoPresets\BasicSC_QuNeo.quneopreset within your Supercollider extentions directory.
6) Click "Save Preset"
7) Click "Update Preset"
8) RUn this code from within Supercollider.  I recommend adding it to your startup file.
*/

~quNeo = QuNeo();
//Pads
~quNeo.createDrumPad(00, 00, 00, 01, 02, 15);
~quNeo.createDrumPad(01, 01, 03, 04, 05, 15);
~quNeo.createDrumPad(02, 02, 06, 07, 08, 15);
~quNeo.createDrumPad(03, 03, 09, 10, 11, 15);
~quNeo.createDrumPad(04, 04, 12, 13, 14, 15);
~quNeo.createDrumPad(05, 05, 15, 16, 17, 15);
~quNeo.createDrumPad(06, 06, 18, 19, 20, 15);
~quNeo.createDrumPad(07, 07, 21, 22, 23, 15);
~quNeo.createDrumPad(08, 08, 24, 25, 26, 15);
~quNeo.createDrumPad(09, 09, 27, 28, 29, 15);
~quNeo.createDrumPad(10, 10, 30, 31, 32, 15);
~quNeo.createDrumPad(11, 11, 33, 34, 35, 15);
~quNeo.createDrumPad(12, 12, 36, 37, 38, 15);
~quNeo.createDrumPad(13, 13, 39, 40, 41, 15);
~quNeo.createDrumPad(14, 14, 42, 43, 44, 15);
~quNeo.createDrumPad(15, 15, 45, 46, 47, 15);
~quNeo.createDrumPad(15, 16, 45, 46, 47, 15);
//Horizontal Sliders
~quNeo.createHSlider(00, 39, 092, 093, 15);
~quNeo.createHSlider(01, 43, 100, 101, 15);
~quNeo.createHSlider(02, 47, 108, 109, 15);
~quNeo.createHSlider(03, 51, 116, 117, 15);
~quNeo.createHSlider(04, 40, 094, 095, 15);
~quNeo.createHSlider(05, 44, 102, 103, 15);
~quNeo.createHSlider(06, 48, 110, 111, 15);
~quNeo.createHSlider(07, 52, 118, 119, 15);
~quNeo.createHSlider(08, 41, 096, 097, 15);
~quNeo.createHSlider(09, 45, 104, 105, 15);
~quNeo.createHSlider(10, 49, 112, 113, 15);
~quNeo.createHSlider(11, 53, 120, 121, 15);
~quNeo.createHSlider(12, 42, 098, 099, 15);
~quNeo.createHSlider(13, 46, 106, 107, 15);
~quNeo.createHSlider(14, 50, 114, 115, 15);
~quNeo.createHSlider(15, 54, 122, 123, 15);
//Vertical Sliders
~quNeo.createVSlider(00, 20, 60, 61, 15);
~quNeo.createVSlider(01, 24, 68, 69, 15);
~quNeo.createVSlider(02, 28, 76, 77, 15);
~quNeo.createVSlider(03, 32, 84, 85, 15);
~quNeo.createVSlider(04, 21, 62, 63, 15);
~quNeo.createVSlider(05, 25, 70, 71, 15);
~quNeo.createVSlider(06, 29, 78, 79, 15);
~quNeo.createVSlider(07, 33, 86, 87, 15);
~quNeo.createVSlider(08, 22, 64, 65, 15);
~quNeo.createVSlider(09, 26, 72, 73, 15);
~quNeo.createVSlider(10, 30, 80, 81, 15);
~quNeo.createVSlider(11, 34, 88, 89, 15);
~quNeo.createVSlider(12, 23, 66, 67, 15);
~quNeo.createVSlider(13, 27, 74, 75, 15);
~quNeo.createVSlider(14, 31, 82, 83, 15);
~quNeo.createVSlider(15, 35, 90, 91, 15);
//Long Sliders
~quNeo.createLSlider(00, 16, 48, 49, 50, 15);
~quNeo.createLSlider(01, 17, 51, 52, 53, 15);
~quNeo.createLSlider(02, 18, 54, 55, 56, 15);
~quNeo.createLSlider(03, 19, 57, 58, 59, 15);
//Buttons
~quNeo.createButton(00, 36, 124, 15);
~quNeo.createButton(01, 37, 125, 15);
~quNeo.createButton(02, 38, 126, 15);
//Circle Controls
~quNeo.createCircle(00, 00, 00, 01, 14);
~quNeo.createCircle(01, 04, 08, 09, 14);
~quNeo.createCircle(02, 01, 02, 03, 14);
~quNeo.createCircle(03, 05, 10, 11, 14);
~quNeo.createCircle(04, 02, 04, 05, 14);
~quNeo.createCircle(05, 06, 12, 13, 14);
~quNeo.createCircle(06, 03, 06, 07, 14);
~quNeo.createCircle(07, 07, 14, 15, 14);