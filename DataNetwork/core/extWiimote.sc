+ WiiMote {

	addToNetwork{ |network,id1,id2|
		var updNetwork1,updNetwork2;

		updNetwork1 = {
			network.setData( id1, this.remote_motion ++ this.remote_buttons ++ this.remote_led ++ battery )
		};

		([ \ax, \ay, \az, \ao ]++
			[ \bA, \bB, \bOne, \bTwo, \bMinus, \bHome, \bPlus, \bUp, \bDown, \bLeft, \bRight ] ).do{ |it,i|
				network.add( (\wii_++it).asSymbol, [id1,i] );
				this.setAction( it, updNetwork1 );
			};

		this.remote_led.do{ |it,i| network.add( (\wii_led ++ i).asSymbol, [id1,i+15] );
		};

		network.add( \wii_battery, [id1,19] );
		network.add( \wiimote, id1);
		
		switch( ext_type,
			1, {
				network.add( \wii_nunchuk, id2);
				
				updNetwork2 = {
					network.setData( id2, this.nunchuk_motion ++ this.nunchuk_stick ++ this.nunchuk_buttons );
				};
				[ \nax, \nay, \naz, \nao, \nsx, \nsy, \nbZ, \nbC ].do{ |it,i|
					network.add( (\wii_++it).asSymbol, [id2,i] );
					this.setAction( it, updNetwork2 );
				};
			},
			2, {
				network.add( \wii_classic, id2);
				updNetwork2 = { network.setData( id2, this.classic_buttons ++ this.classic_stick1 ++ this.classic_stick2 ++ this.classic_analog );
				};
				[ \cbX, \cbY, \cbA, \cbB, \cbL, \cbR, \cbZL, \cbZR, \cbUp, \cbDown, \cbLeft, \cbRight, \cbMinus, \cbHome, \cbPlus, \csx1, \csy1, \csx2, \csy2, \caleft, \caright  ].do{ |it,i|
					network.add( (\wii_++it).asSymbol, [id2,i] );
					this.setAction( it, updNetwork2 );
				}
			}
		);

		
	}

}