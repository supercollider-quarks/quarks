/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
UGlobalGain automatically creates a gain stage for U (unit), which is controlled via a global setting.

UGlobalGain.kr It creates the following private controls automatically:

	u_globalGain (0) : the global level for all U's that have this in their path
	
UGlobalGain is also incorporated in UEnv.

UGlobalGain.gui creates a gui for the global gain. It sends its values to the rootnode of each
server in the UServerCenter, so that all currently active units get the correct settings.
 
*/

UGlobalGain {
	
	classvar <gain = 0;
	classvar <>view;
	
	*gain_ { |new|
		gain = (new ? gain);
		this.changed( \gain, gain );
		this.update;
	}
	
	*kr { |use = 1|
		Udef.addBuildSpec( ArgSpec( \u_globalGain, UGlobalGain, UGlobalGain, true ) );
		^(\u_globalGain.kr( gain ) * use).dbamp;	
	}
	
	*gui { |parent, bounds|
		if( view.isNil or: { view.view.isClosed } ) {
			bounds = bounds ?? { 
				Rect( 
					Window.screenBounds.width - 100, 
					0, 
					100, 
					Window.screenBounds.height - 150 
			); };
			RoundView.useWithSkin( UChainGUI.skin, {
				view = EZSmoothSlider( parent, bounds,
					controlSpec: [ -60, 36, \lin, 0, -12, "db" ],
					labelHeight: 50
				).value_( gain ).action_({ |vw| this.gain = vw.value });
				view.view.resize_(5);
				view.numberView.autoScale_( true )
					.scroll_step_( 1 )
					.formatFunc_({ |value| value.round(1) });
				view.sliderView.mode_( \move );
			});
			^view;
		} {
			^view.front;
		};
	}
	
	*asUGenInput { ^gain.asUGenInput }
	*asControlInput { ^gain.asControlInput }
	*asOSCArgEmbeddedArray { | array| ^gain.asOSCArgEmbeddedArray(array) }
	
	*update { |obj, what ... args|
		if( view.notNil and: { view.view.isClosed.not } ) {
			view.value = gain;
		};
		ULib.servers.do({ |item|
			if( item.class.name == 'LoadBalancer' ) {
				item.servers.do({ |srv|
					this.sendServer( srv );
				});
			} {
				this.sendServer( item );
			};	
		});
	}
	
	*sendServer { |server|
		RootNode( server ).set( \u_globalGain, this );
	}
	
	// double as Spec
	*new { ^this } // only use as class
	
	*asSpec { ^this }
	
	*constrain { ^this }
	
	*default {  ^this }
	
	*massEditSpec { ^nil }
	
	*findKey {
		^Spec.specs.findKeyForValue(this);
	}
}
