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
UGlobalEQ automatically creates an equaliser for U (unit), which is controlled via a global setting.

It creates the following private controls automatically:

	u_globalEQ_setting (EQSetting) : the setting of the eq
	u_globalEQ_bypass (0) : a bypass option, adjustable per unit

UGlobalEQ.gui creates a gui for the global EQ. It sends its values to the rootnode of each
server in the UServerCenter, so that all currently active units get the correct settings.
 
*/

UGlobalEQ {
	
	classvar <eqSetting;
	classvar <>ctrl;
	classvar <>view;
	classvar <>presets;
	
	*initClass {
		Class.initClassTree(EQSetting);
		this.eqSetting = EQSetting(); // the default eq setting (may change later)
	}
	
	*eqSetting_ { |new|
		eqSetting.removeDependant( this );
		eqSetting = new;
		eqSetting.addDependant( this );
	}
	
	*ar { |in|
		var setting, bypass;
		
		Udef.addBuildSpec( ArgSpec( \u_globalEQ_setting, UGlobalEQ, UGlobalEQ, true ) );
		setting = \u_globalEQ_setting.kr( eqSetting.asControlInput );
		
		Udef.addBuildSpec( ArgSpec( \u_globalEQ_bypas, false, BoolSpec(false), true ) );
		bypass = \u_globalEQ_bypass.kr( 0 );
		
		^if( bypass, in, eqSetting.ar( in, setting ) );
	}
	
	*gui { |parent, bounds|
		if( view.isNil or: { view.view.isClosed } ) {
			RoundView.useWithSkin( UChainGUI.skin, {
				view = EQView( parent ? "UGlobalEQ", bounds ? Rect(405, 10, 332, 165), eqSetting, presets );
			});
			^view;
		} {
			^view.front;
		};
	}
	
	*asUGenInput { ^eqSetting.asUGenInput }
	*asControlInput { ^eqSetting.asControlInput }
	*asOSCArgEmbeddedArray { | array| ^eqSetting.asOSCArgEmbeddedArray(array) }
	
	*update { |obj, what ... args|
		if( what == \setting ) {
			ULib.servers.do({ |item|
				if( item.class.name == 'LoadBalancer' ) {
					item.servers.do({ |srv|
						this.sendServer( srv );
					});
				} {
					this.sendServer( item );
				};	
			});
		};
	}
	
	*sendServer { |server|
		RootNode( server ).set( \u_globalEQ_setting, this );
	}
	
	*doesNotUnderstand { |selector ...args|
		var res;
		res = eqSetting.perform( selector, *args );
		if( res != eqSetting ) { ^res }
	}
	
	// double as Spec
	*new { ^this } // only use as class
	
	*asSpec { ^this }
	
	*constrain { ^this } // whatever comes in; UGlobalEQ comes out
	
	*default { ^this }
	
	*massEditSpec { ^nil }
	
	*findKey {
		^Spec.specs.findKeyForValue(this);
	}
}


