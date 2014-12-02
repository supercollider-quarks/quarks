/*
	Filename: main.c
	created: 13.9.2005 

	This commandline tool is intended to provide simple headtracking features to 
	SuperCollider3 applications such as AmbIEM. It uses the ARToolKit for visual
	pattern recognition and transmitts the data using OpenSound Control packages

	Copyright (C) IEM 2005, Christopher Frauenberger [frauenberger@iem.at] 

	This program is free software; you can redistribute it and/or 
	modify it under the terms of the GNU General Public License 
	as published by the Free Software Foundation; either version 2 
	of the License, or (at your option) any later version. 

	This program is distributed in the hope that it will be useful, 
	but WITHOUT ANY WARRANTY; without even the implied warranty of 
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
	GNU General Public License for more details. 

	You should have received a copy of the GNU General Public License 
	along with this program; if not, write to the Free Software 
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 

	IEM - Institute of Electronic Music and Acoustics, Graz 
	Inffeldgasse 10/3, 8010 Graz, Austria 
	http://iem.at
*/

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

#ifdef _WIN32
	#include <windows.h>
	#include <winsock2.h>
#else
	#include <sys/socket.h>
	#include <netinet/in.h>
	#include <sys/types.h>
#endif

#ifndef __APPLE__
	#include <GL/gl.h>
	#include <GL/glut.h>
#else
	#include <OpenGL/gl.h>
	#include <GLUT/glut.h>
#endif

#include <AR/config.h>
#include <AR/video.h>
#include <AR/param.h>
#include <AR/ar.h>
#include <AR/gsub.h>

#include "OSC-client.h"

#ifdef _WIN32
	char			*vconf = "flipV,showDlg"; // see video.h for a list of supported parameters
#else
	char			*vconf = "";
#endif

int             xsize, ysize;
int             thresh = 100;
int             count = 0;

char			*cparam_name    = "./camera_para.dat";
ARParam         cparam;

char			*patt_name = "./patt.hiro";
int             patt_id;
double          patt_width     = 80.0;
double          patt_center[2] = {0.0, 0.0};
double          patt_trans[3][4];

int sockfd;
struct sockaddr_in serverAddress;
OSCbuf *oscbuf;
	
static void   init(void);
static void   cleanup(void);
static void   mainLoop(void);
static void   keyEvent(void);
static void	  sendOSC(double, double, double);

int main(int argc, char **argv)
{
	int port;
	char *server;
	OSCbuf myBuf;
	char bytes[1000];

	if (argc > 2) { port = atoi(argv[2]); }
	else { port = 57120; };
	if (argc > 1) { strcpy(server, argv[1]); }
	else { server = "127.0.0.1"; };

	printf("Connecting to server %s:%i \n", server, port);

    // init the AR stuff and GLUT to be able to see the captured image in the setup dialog
	int ac = 0; char *av = "";
	glutInit(&ac, av);
	init();

	// init networking connection
	if ( (sockfd = socket( AF_INET, SOCK_DGRAM, 0 )) < 0 ) {
		printf("Cant create socket, exiting.\n");
		exit(1);
	}
	bzero( &serverAddress, sizeof(serverAddress) );
	serverAddress.sin_family = AF_INET;
	serverAddress.sin_port = htons( port );
	inet_pton( AF_INET, server , &serverAddress.sin_addr );

	// init the OSC Buffer
	oscbuf = &myBuf;
	OSC_initBuffer(oscbuf, 1000, bytes);

    arVideoCapStart();
    argMainLoop( NULL, keyEvent, mainLoop );
	return (0);
}

/* keyEvent */
static void keyEvent(void) 
{

};

/* main loop */
static void mainLoop(void)
{
    ARUint8         *dataPtr;
    ARMarkerInfo    *marker_info;
    int             marker_num;
    int             j, k;
	int i,u;
	
    /* grab a vide frame */
    if( (dataPtr = (ARUint8 *)arVideoGetImage()) == NULL ) {
        arUtilSleep(2);
        return;
    }
    if( count == 0 ) arUtilTimerReset();
    count++;

    argDrawMode2D();
    argDispImage( dataPtr, 0,0 );

    /* detect the markers in the video frame */
    if( arDetectMarker(dataPtr, thresh, &marker_info, &marker_num) < 0 ) {
        cleanup();
        exit(0);
    }

    arVideoCapNext();

    /* check for object visibility */
    k = -1;
    for( j = 0; j < marker_num; j++ ) {
        if( patt_id == marker_info[j].id ) {
            if( k == -1 ) k = j;
            else if( marker_info[k].cf < marker_info[j].cf ) k = j;
        }
    }
    if( k == -1 ) {
        argSwapBuffers();
        return;
    }

    /* get the transformation between the marker and the real camera */
    arGetTransMat(&marker_info[k], patt_center, patt_width, patt_trans);

	// calculate the three rotatioin from the patt_trans matrix
	// this is the result of a mind cracking research in transformation matrices
	// see http://fly.cc.fer.hr/~unreal/theredbook/appendixg.html for details
	double r31, r11, r33;
	double alpha, beta, gamma;
	r31 = patt_trans[0][2];
	r11 = patt_trans[0][0];
	r33 = patt_trans[2][2];
	
	beta = asin(r31);
	alpha = acos(r33/cos(beta));
	gamma = acos(r11/cos(beta));
	
	// send the data through OSC
	// if we are confident enough that the marker is detected
	if (marker_info[k].cf > 0.8) {
		sendOSC(alpha, beta, gamma);	
	}
	argSwapBuffers();
}

static void init( void )
{
    ARParam  wparam;

    /* open the video path */
    if( arVideoOpen( vconf ) < 0 ) exit(0);
    /* find the size of the window */
    if( arVideoInqSize(&xsize, &ysize) < 0 ) exit(0);
    printf("Image size (x,y) = (%d,%d)\n", xsize, ysize);

    /* set the initial camera parameters */
    if( arParamLoad(cparam_name, 1, &wparam) < 0 ) {
        printf("Camera parameter load error !!\n");
        exit(0);
    }
    arParamChangeSize( &wparam, xsize, ysize, &cparam );
    arInitCparam( &cparam );
    printf("*** Camera Parameter ***\n");
    arParamDisp( &cparam );

    if( (patt_id=arLoadPatt(patt_name)) < 0 ) {
        printf("pattern load error !!\n");
        exit(0);
    }

    /* open the graphics window */
    argInit( &cparam, 1.0, 0, 0, 0, 0 );
}


/* sendOSC called in the mainLoop to send all the data via OSC connection */
static void sendOSC(double alpha, double beta, double gamma) 
{
	int i;
	OSCTimeTag tt;
    tt = OSCTT_CurrentTime();
	
    OSC_resetBuffer(oscbuf);

    if (OSC_openBundle(oscbuf, tt)) {
		printf("** OSC ERROR: %s\n", OSC_errorMessage);
    }

    if (OSC_writeAddressAndTypes(oscbuf, "/client", ",sfff")) {
        printf("** OSC ERROR: %s\n", OSC_errorMessage);
    }
	
    if (OSC_writeStringArg(oscbuf, "headtracker")) {
        printf("** OSC ERROR: %s\n", OSC_errorMessage);
    }
	
    if (OSC_writeFloatArg(oscbuf, alpha)) {
        printf("** OSC ERROR: %s\n", OSC_errorMessage);
    }

    if (OSC_writeFloatArg(oscbuf, beta)) {
        printf("** OSC ERROR: %s\n", OSC_errorMessage);
    }

    if (OSC_writeFloatArg(oscbuf, gamma)) {
        printf("** OSC ERROR: %s\n", OSC_errorMessage);
    }

    OSC_closeAllBundles(oscbuf);

	if (OSC_isBufferDone(oscbuf)) 
	{
		sendto(sockfd, OSC_getPacket(oscbuf), OSC_packetSize(oscbuf), 0, &serverAddress, sizeof(serverAddress));
	}
	else {
        printf("** OSC ERROR: %s\n", OSC_errorMessage);
    }

}


/* cleanup function called when program exits */
static void cleanup(void)
{
    arVideoCapStop();
    arVideoClose();
    argCleanup();
}
