{\rtf1\mac\ansicpg10000\cocoartf824\cocoasubrtf420
{\fonttbl\f0\fnil\fcharset77 Monaco;}
{\colortbl;\red255\green255\blue255;\red191\green0\blue0;\red0\green0\blue191;\red96\green96\blue96;
\red0\green115\blue0;}
\pard\tx560\tx1120\tx1680\tx2240\tx2800\tx3360\tx3920\tx4480\tx5040\tx5600\tx6160\tx6720\ql\qnatural

\f0\fs18 \cf2 /*	\
	GD_ToolboxWindow()\
	\
	select view(s) and apple-drag between windows to copy\
	select view(s) and apple-drag inside a window, or press c, to copy\
	press delete to delete\
		\
\
*/\cf0 \
\cf3 GD_PanelWindow\cf0 \
\{\
	\cf3 var\cf0  window,userview,>parent,isSelected=\cf3 false\cf0 ;\
	\
	\cf3 var\cf0  selection,<selectedViews,views;\
	\
	\cf3 var\cf0  <gridStep = 10,<gridOn = \cf3 false\cf0 ,dragging,indent,multipleDragBy;\
	\
	\cf3 var\cf0  resizeHandles,resizeFixed, dropX, dropY;\
	\
	\
	*new \{ \cf3 |bounds|\cf0  ^\cf3 super\cf0 .new.init(bounds) \}\
	\
	\
	init \{ \cf3 |bounds|\cf0 \
		window = \cf3 SCWindow\cf0 (\cf4 "Panel"\cf0 ,bounds).front;\
		views = \cf3 Array\cf0 .new;			\
		\cf3 this\cf0 .makeUserview;\
	\}\
	\
	gridOn_ \{ \cf3 |bool|\cf0 \
		gridOn = bool;\
		window.refresh;\
	\}\
	\
	gridStep_ \{ \cf3 |step|\cf0 \
		gridStep = step;\
		if(gridOn, \{ window.refresh \})\
	\}\
	\
	makeUserview \{\
		\cf3 var\cf0  w = window.bounds.width, h = window.bounds.height;\
		\
		userview !? \{ userview.remove \};\
		userview = \cf3 SCUserView\cf0 (window,\cf3 Rect\cf0 (0,0,w,h)).resize_(5);\
		\
		userview.beginDragAction = \{\
			\cf3 var\cf0  classes,rects;\
			\
			if ( selectedViews.size > 0,\{\
				#classes, rects = flop(selectedViews.collect(\{ \cf3 |view|\cf0 \
					[ view.class, view.bounds ]\
				\}));\
				\
				\cf3 GD_MultipleDrag\cf0 (classes,rects)\
			\})\
		\};\
		\
		userview.keyDownFunc = \{ \cf3 |v,c,m,u|\cf0  \cf3 this\cf0 .panelSelect.keyDown(c,u) \};\
		userview.mouseBeginTrackFunc = \{ \cf3 |v,x,y|\cf0  \cf3 this\cf0 .panelSelect.mouseDown(x,y) \};\
		userview.mouseEndTrackFunc = \{ \cf3 |v,x,y|\cf0  \cf3 this\cf0 .panelSelect.mouseUp \};\
		\
		userview.mouseOverAction = \{ \cf3 |v,x,y|\cf0  dropX = x; dropY = y \};\
		\
		userview.canReceiveDragHandler = \{\
			\cf3 SCView\cf0 .currentDrag.isKindOf( \cf3 GD_Drag\cf0  )\
		\};\
		\
		userview.receiveDragHandler = \{\
			\cf3 var\cf0  addedViews = \cf3 Array\cf0 .new;\
			\
			\cf3 SCView\cf0 .currentDrag.do(\{ \cf3 |class, rect|\cf0 \
				rect = rect.moveBy( dropX, dropY );\
				addedViews = addedViews.add( class.paletteExample(window,rect) );\
			\});\
			\
			views = views.addAll( addedViews );\
			\
			dragging = \cf3 true\cf0 ;\
			selectedViews = addedViews;\
			indent = dropX@dropY - views.last.bounds.origin;\
			\
			\cf3 this\cf0 .makeUserview.updateResizeHandles;\
			window.front.refresh;\
			\
			this.panelSelect\
		\};\
		\
		userview.mouseTrackFunc = \{ \cf3 |v,x,y|\cf0  \cf3 this\cf0 .drag(x,y) \};\
		userview.focus;\
		\
		\cf3 this\cf0 .initDrawFunc\
	\}\
	\
	initDrawFunc \{\
		userview.drawFunc = \{\
			\cf3 var\cf0  b,n,h,w;\
			\
			if(gridOn,\{\
				b = window.view.bounds;\
				h = b.height;\
				w = b.width;\
				\
				\cf3 Color\cf0 .yellow(1,0.4).set;\
				\
				n = h / gridStep;\
				(n-1).do(\{ \cf3 |i|\cf0  i=i+1*gridStep;\
					\cf3 Pen\cf0 .moveTo(0@i).lineTo(w@i).stroke;\
				\});\
				\
				n = w / gridStep;\
				(n-1).do(\{ \cf3 |i|\cf0  i=i+1*gridStep;\
					\cf3 Pen\cf0 .moveTo(i@0).lineTo(i@h).stroke;\
				\})\
			\});\
			\
			\cf3 Color\cf0 .blue.set;\
			\
			if(isSelected,\{\
				\cf3 Pen\cf0 .width_(4).strokeRect(window.bounds.moveTo(0,0));\
			\});\
			\
			selectedViews.do(\{ \cf3 |v|\cf0 \
				\cf3 Pen\cf0 .strokeRect(v.bounds)\
			\});\
			\cf3 Pen\cf0 .width_(1);\
			\
			resizeHandles.do(\{ \cf3 |r|\cf0 \
				\cf3 Pen\cf0 .fillRect(r)\
			\});\
			\
			if(selection.notNil, \{\
				\cf3 Pen\cf0 .strokeRect(selection.rect)\
			\});\
			\
		\}\
	\}\
	\
	deselect \{\
		isSelected = \cf3 false\cf0 ;\
		window.refresh\
	\}\
	\
	updateResizeHandles \{ \cf3 var\cf0  r,d=4;\
		resizeHandles = if( selectedViews.size == 1,\{\
			r = selectedViews.first.bounds;\
			[ r.leftTop, r.rightTop, r.rightBottom, r.leftBottom ]\
				.collect(\{ \cf3 |center|\cf0  \cf3 Rect\cf0 .aboutPoint(center,d,d) \})\
		\});\
		window.refresh\
	\}\
	\
	panelSelect \{\
		isSelected = \cf3 true\cf0 ;\
		if(parent.notNil,\
			\{ parent.panelSelect(\cf3 this\cf0 ) \})\
	\}\
	\
	\
	setResizeFixed \{ \cf3 |resizeHandle|\cf0 \
		\cf3 var\cf0  r = selectedViews.first.bounds,i = resizeHandles.indexOf(resizeHandle);\
		resizeFixed=r.perform([ \cf5 \\rightBottom\cf0 , \cf5 \\leftBottom\cf0 , \cf5 \\leftTop\cf0 , \cf5 \\rightTop\cf0  ][i])\
	\}\
	\
	\
	mouseDown \{ \cf3 |x,y|\cf0 \
		\cf3 var\cf0  view,p,handle;\
		\
		p = x@y;\
		\
		if( resizeHandles.notNil and: \{\
			(handle = resizeHandles.detect(\{ \cf3 |h|\cf0  h.containsPoint(p) \}) ).notNil\
		\},\
		\{\
			\cf3 this\cf0 .setResizeFixed(handle)\
		\},\
		\{\
			resizeFixed = \cf3 nil\cf0 ;\
			view = \cf3 this\cf0 .viewContainingPoint(p);\
			\
			dragging = view.notNil;\
			\
			if( dragging, \{\
				indent = p - view.bounds.origin;\
				\
				if( (selectedViews.size > 1) and: \
					\{ selectedViews.includes(view) \},\
				\{\
					multipleDragBy = view\
				\},\
				\{\
					multipleDragBy = \cf3 nil\cf0 ;\
					selectedViews = [ view ]\
				\})\
			\},\{\
				selectedViews = [];\
				selection = \cf3 GD_AreaSelection\cf0 (p)\
			\})\
		\});\
		\
		\cf3 this\cf0 .updateResizeHandles\
	\}\
	\
	drag \{ \cf3 |x,y|\cf0 \
		\cf3 var\cf0  view,f,p=x@y;\
		if( dragging, \{\
		\
			if( resizeFixed.isNil,\
			\{\
				if(multipleDragBy.notNil,\
				\{\
					f = p - ( multipleDragBy.bounds.origin + indent );\
					\
					selectedViews.do(\{ \cf3 |v|\cf0  \
						\cf3 this\cf0 .quantSetBounds(v,v.bounds.moveBy(f.x,f.y))\
					\})\
				\},\{\
					view = selectedViews.first;\
					\cf3 this\cf0 .quantSetBounds(view,view.bounds.moveToPoint(p-indent));\
					\
					\cf3 this\cf0 .updateResizeHandles\
				\})\
			\},\{\
				if(gridOn,\{ p = p.round(gridStep) \});\
				selectedViews.first.bounds = \cf3 Rect\cf0 .fromPoints(p,resizeFixed);\
				\cf3 this\cf0 .updateResizeHandles\
			\})\
		\},\
		\{\
			selection.mouseDrag(p);\
			selectedViews = views.select(\{ \cf3 |view|\cf0 \
				selection.selects(view.bounds)\
			\});\
			window.refresh\
		\})\
	\}\
	\
	mouseUp \{ \cf3 |x,y|\cf0 \
		if(selection.notNil,\{\
			selection = \cf3 nil\cf0 ; window.refresh\
		\})\
	\}\
	\
	keyDown \{ \cf3 |c,u|\cf0 \
		\cf3 var\cf0  newViews;\
		case (\
		\cf2 // delete\cf0 \
		\{u==127\}, \{\
			if(selectedViews.isEmpty.not,\{\
				selectedViews.do(\{ \cf3 |v|\cf0 \
					views.remove(v.remove);\
				\});\
				selectedViews=[];\
				\
				\cf3 this\cf0 .updateResizeHandles\
			\})\
		\},\
		\cf2 // clone\cf0 \
		\{(c==$c) or: (c==$C)\},\{\
			if(selectedViews.isEmpty.not,\{\
				newViews=selectedViews.collect(\{ \cf3 |v|\cf0 \
					v.class.paletteExample(window,v.bounds.moveBy(40,40))\
				\});\
				views=views++newViews;\
				selectedViews=newViews;\
				\
				\cf3 this\cf0 .makeUserview.updateResizeHandles\
			\})\
		\})\
	\}\
	\
	\
	quantSetBounds \{ \cf3 |view,rect|\cf0 \
		view.bounds=if(gridOn,\
			\{ rect.moveToPoint(rect.origin.round(gridStep)) \},\
			\{ rect \})\
	\}\
	\
	\
	viewContainingPoint \{ \cf3 |point|\cf0 \
		views.do(\{ \cf3 |view|\cf0 \
			if(view.bounds.containsPoint(point),\
				\{ ^view \})\
		\})\
		^\cf3 nil\cf0 \
	\}\
	\
	asCompileString \{\
		\cf3 var\cf0  str = \cf4 ""\cf0 ;\
		views.do(\{ \cf3 |v|\cf0  str = str ++ format(\cf4 "%.new(w,%);\\n"\cf0 ,v.class,v.bounds) \});\
		^format( \cf4 "(\\nvar w = SCWindow.new(\\"\cf0 \\\cf4 ",%).front;\\n%\\n)"\cf0 ,window.bounds,str )\
	\}\
	\
\}\
\
\cf3 GD_ToolboxWindow\cf0 \
\{\
	\cf3 var\cf0  window,viewPallatte,panels,selectedPanel;\
	\
	*new \{ ^\cf3 super\cf0 .new.init \}\
	\
	init\
	\{\
		\cf3 var\cf0  n = \cf3 GD_ViewPallatte\cf0 .viewList.size;\
		\cf3 var\cf0  vh = 24, vw = 146,gridNB,gridBut;\
		\
		\cf3 var\cf0  height = n + 4 * (vh + 2) +2, os;\
		\cf3 var\cf0  vw2 = div(vw,2);\
		\
		\cf3 var\cf0  funcButCol = \cf3 Color\cf0 .blue;\
		\
		window = \cf3 SCWindow\cf0 (\cf4 "GD"\cf0 ,\cf3 Rect\cf0 (50,800,vw+4,height)).front;\
		\
		viewPallatte = \cf3 GD_ViewPallatte\cf0 (window,\cf3 Rect\cf0 (2, 2, vw, vh));\
		\
		panels = \cf3 Array\cf0 .new;\
		\
		os = vh + 2 * n + 2;\
		\cf3 SCButton\cf0 (window,\cf3 Rect\cf0 (2,os,vw,vh)).states_([[\cf4 "NEW WINDOW"\cf0 ,\cf3 nil\cf0 ,funcButCol]])\
			.canFocus_(\cf3 false\cf0 ).action = \{\
				\cf3 var\cf0  panel = \cf3 GD_PanelWindow\cf0 .new(\cf3 Rect\cf0 (100,100,400,400)).parent_(\cf3 this\cf0 );\
				panel.gridStep_(gridNB.value);\
				panel.gridOn = gridBut.value==1;\
				\
				panels = panels.add(panel)\
		\};\
		\
		os = os + vh + 2;\
		\cf3 SCButton\cf0 (window,\cf3 Rect\cf0 (2,os,vw,vh)).states_([ [\cf4 "-> CODE"\cf0 ,\cf3 nil\cf0 ,funcButCol]])\
			.canFocus_(\cf3 false\cf0 ).action = \{ if ( selectedPanel.notNil, \
				\{ selectedPanel.asCompileString.postln  \} )\
		\};\
		\
		os = os + vh + 2;\
		\cf3 SCButton\cf0 (window,\cf3 Rect\cf0 (2,os,vw,vh)).states_([ [\cf4 "TEST"\cf0 ,\cf3 nil\cf0 ,funcButCol]])\
			.canFocus_(\cf3 false\cf0 ).action = \{\
				selectedPanel.asCompileString.interpret\
		\};\
		\
		os = os + vh + 2;\
		gridBut = \cf3 SCButton\cf0 (window,\cf3 Rect\cf0 (2,os,vw2,vh))\
			.canFocus_(\cf3 false\cf0 ).action = \{ \cf3 |v|\cf0 \
				gridNB.visible = v.value == 1;\
				panels.do(\{ \cf3 |panel|\cf0 \
					panel.gridOn_(v.value == 1).gridStep = gridNB.value;\
				\})\
		\};\
		\
		gridBut.states_([[\cf4 "Q ON"\cf0 ,\cf3 nil\cf0 ,funcButCol],[\cf4 "Q OFF"\cf0 ,\cf3 nil\cf0 ,funcButCol]]);\
\
		gridNB = \cf3 SCNumberBox\cf0 (window,\cf3 Rect\cf0 (2+vw2,os,vw2,vh))\
			.action = \{ \cf3 |v|\cf0  \
				v.value = v.value.asInt.clip(3,40);\
				panels.do(\{ \cf3 |panel|\cf0  panel.gridStep = v.value \})\
				\
		\};\
		\
		gridNB.align_(\cf5 \\center\cf0 ).value_(10).visible = \cf3 false\cf0 \
	\}\
	\
	panelSelect \{ \cf3 |panel|\cf0 \
		if( panel !== selectedPanel,\{\
			if(selectedPanel.notNil,\{ selectedPanel.deselect \});\
			selectedPanel = panel\
		\})\
	\}\
\}\
\
\cf3 GD_ViewPallatte\cf0 \
\{\
	\cf3 classvar\cf0  <viewList;\
	\
	*new \{ \cf3 |window,rect,parent|\cf0 \
		^\cf3 super\cf0 .new.init(window,rect,parent)\
	\}\
	\
	init \{ \cf3 |window,rect,parent|\cf0 \
		\cf3 var\cf0  x = rect.top, y = rect.left;\
		\cf3 var\cf0  bW = rect.width, bH = rect.height;\
		\
		viewList.do(\{ \cf3 |class,i|\cf0 \
			\cf3 var\cf0  drag = \cf3 GD_PallatteDrag\cf0 (class,\cf3 Rect\cf0 (0,0,100,20));\
			\cf3 SCDragSource\cf0 (window,\cf3 Rect\cf0 (x+2,i*(bH+2)+2+y,bW,bH))\
				.string_(class.asString).align_(\cf5 \\center\cf0 ).object_(drag)\
		\})\
	\}\
	\
	*initClass \{\
		viewList = [\
			\cf3 SCButton\cf0 ,\
			\cf3 SCStaticText\cf0 ,\
			\cf3 SCNumberBox\cf0 ,\
			\cf3 SCSlider\cf0 ,\
			\cf3 SCRangeSlider\cf0 ,\
			\cf3 SCMultiSliderView\cf0 ,\
			\cf3 SCPopUpMenu\cf0 ,\
			\cf3 SC2DTabletSlider\cf0 ,\
			\cf3 SC2DSlider\cf0 ,\
			\cf3 SCTabletView\cf0 ,\
			\cf3 SCEnvelopeView\cf0 ,\
			\cf3 SCDragBoth\cf0 ,\
			\cf3 SCDragSink\cf0 ,\
			\cf3 SCDragSource\cf0 ,\
			\cf2 Knob\cf0 \
			\cf2 //SCTextView,\cf0 \
			\cf2 //SCMovieView\cf0 \
		];\
	\}\
\}\
\
\cf3 GD_AreaSelection\cf0 \
\{\
	\cf3 var\cf0  click,round,<rect;\
	\
	*new \{ \cf3 |p,r|\cf0  r !? \{ p = round(p,r) \};\
		^\cf3 super\cf0 .newCopyArgs(p,r).mouseDrag(p)\
	\}\
	\
	mouseDrag \{ \cf3 |drag|\cf0 \
		round !? \{ drag = round(drag,round) \};\
		rect = \cf3 Rect\cf0 .fromPoints(click,drag)\
	\}\
	\
	selects \{ \cf3 |aRect|\cf0 \
		^rect.intersects(aRect)\
	\}\
\}\
\
\
GD_Drag \{ \}\
\
\cf3 GD_MultipleDrag : GD_Drag\cf0 \
\{\
	\cf3 var\cf0  classes,rects,minX, minY;\
	\
	*new \{ \cf3 |classes, rects|\cf0 \
		^\cf3 super\cf0 .newCopyArgs( classes, rects ).init\
	\}\
	\
	init \{\
		minX = \cf3 inf\cf0 ;\
		minY = \cf3 inf\cf0 ;\
		rects.do(\{ \cf3 |r|\cf0 \
			if ( r.left < minX, \{ minX = r.left \});\
			if ( r.top < minY, \{ minY = r.top \})\
		\});\
		minX = minX.neg;\
		minY = minY.neg;\
	\}\
	\
	do \{ \cf3 |func|\cf0 \
		classes.do(\{ \cf3 |class,i|\cf0 \
			func.( class, rects[ i ].moveBy( minX, minY ), i )\
		\})\
	\}\
\}\
\
\cf3 GD_PallatteDrag : GD_Drag\cf0 \
\{\
	\cf3 var\cf0  class, rect;\
	\
	*new \{ \cf3 |class, rect|\cf0 \
		^\cf3 super\cf0 .newCopyArgs( class, rect )\
	\}\
	\
	do \{ \cf3 |func|\cf0  func.(class,rect,0) \}\
	\
	asString \{ ^class.asString \}\
\}\
\
}