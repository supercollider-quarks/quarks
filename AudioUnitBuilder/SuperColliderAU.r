/*
	SuperColliderAU Copyright (c) 2006 Gerard Roma.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */


#define RES_ID			1000
#define COMP_TYPE		'@@COMP_TYPE@@'
#define COMP_SUBTYPE	'@@COMP_SUBTYPE@@'
#define COMP_MANUF		'SCAU'	

#define VERSION			0x00000005
#define NAME			"SuperColliderAU: @@NAME@@"
#define DESCRIPTION		"@@NAME@@"
#define ENTRY_POINT		"SuperColliderAUEntry"

#define UseExtendedThingResource 1
#include <CoreServices/CoreServices.r>

// this is a define used to indicate that a component has no static data that would mean 
// that no more than one instance could be open at a time - never been true for AUs
#ifndef cmpThreadSafeOnMac
#define cmpThreadSafeOnMac	0x10000000
#endif

#define TARGET_REZ_MAC_PPC        1
#define TARGET_REZ_MAC_X86        1
#define TARGET_REZ_FAT_COMPONENTS	1
#define Target_PlatformType			platformPowerPCNativeEntryPoint
#define Target_SecondPlatformType	platformIA32NativeEntryPoint
#define Target_CodeResType		'dlle'
#define TARGET_REZ_USE_DLLE		1

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

resource 'STR ' (RES_ID, purgeable) {
	NAME
};

resource 'STR ' (RES_ID + 1, purgeable) {
	DESCRIPTION
};

resource 'dlle' (RES_ID) {
	ENTRY_POINT
};

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

resource 'thng' (RES_ID, NAME) {
	COMP_TYPE,
	COMP_SUBTYPE,
	COMP_MANUF,
	0, 0, 0, 0,								//	no 68K
	'STR ',	RES_ID,
	'STR ',	RES_ID + 1,
	0,	0,			/* icon */
	VERSION,
	componentHasMultiplePlatforms | componentDoAutoVersion,
	0,
	{
		cmpThreadSafeOnMac, 
		Target_CodeResType, RES_ID,
		Target_PlatformType,
#if TARGET_REZ_FAT_COMPONENTS
		cmpThreadSafeOnMac, 
		Target_CodeResType, RES_ID,
		Target_SecondPlatformType,
#endif
	}
};

#undef RES_ID
#undef COMP_TYPE
#undef COMP_SUBTYPE
#undef COMP_MANUF
#undef VERSION
#undef NAME
#undef DESCRIPTION
#undef ENTRY_POINT