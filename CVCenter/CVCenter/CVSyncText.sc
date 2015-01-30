/* (c) 2010-2013 Stefan Nussbaumer */
/*
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
*/

// connect TextFields and TextViews to CVs
// <view>.string must be an array of numbers

CVSyncText : CVSync {
	classvar <>initDelay = 0.2, <>valRound=0.01;

	// add to CV's viewDictionary
	*initClass {
		var class, connectDictionary;

		connectDictionary = (textView: this, textField: this, staticText: this);

		{ CV.viewDictionary !? {
			GUI.schemes.do({ |scheme|
				#[textView, textField, staticText].collect({ |name|
					if((class = scheme.perform(name)).notNil, {
						if(Main.versionAtLeast(3, 7), { class = class.superclass });
						CV.viewDictionary.put(class, connectDictionary[name]);
					})
				})
			});
		}}.defer(initDelay);
	}

	update { | changer, what ... moreArgs |
		switch( what,
			\synch, { defer { view.string = cv.value.collect(_.round(valRound)).asCompileString }; }
		);
	}

	value {
		var arr = view.string.interpret;
		if(arr.isKindOf(SequenceableCollection) and:{
			arr.flat.select(_.isNumber).size == arr.flat.size
		}, {
			cv.value = arr.flat;
		})
	}

}