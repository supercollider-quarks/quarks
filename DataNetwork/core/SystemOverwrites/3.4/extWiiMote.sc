+ WiiMote {

	// this will be in the main distro from the next release (3.5)...
	*devicesMap{
		^(
			wii_mote: [
				\ax, \ay, \az, \ao,
				\bA, \bB, \bOne, \bTwo,
				\bMinus, \bHome, \bPlus, \bUp, \bDown, \bLeft, \bRight,
				\led1, \led2, \led3, \led4,
				\battery
			],
			wii_nunchuk: [
				\nax, \nay, \naz, \nao, \nsx, \nsy, \nbZ, \nbC
			],
			wii_classic: [
				\cbX, \cbY, \cbA, \cbB, \cbL, \cbR, 
				\cbZL, \cbZR,
				\cbUp, \cbDown, \cbLeft, \cbRight,
				\cbMinus, \cbHome, \cbPlus,
				\csx1, \csy1, \csx2, \csy2,
				\caleft, \caright
			]
		)
	}
}