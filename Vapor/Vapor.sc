Vapor {
	var <ip, <port, controls, labels, <outAddr, <pongResp, <listenResp;
	var <mode, <fade, <viscosity, <diffusion, <speed, <gravity, <colorChange, <hue;
	var <reflectX, <reflectY, <reflectCenter;
	var <>newTouchFunc, <>touchMovedFunc, <>touchEndedFunc, <>cloudFunc, <>bombFunc, <>rotationFunc, <>pinchFunc, <>accelFunc;
	var responders, hashID;
	classvar tmpFunc;
	
	*new {arg ip, port = 51010;
		^super.newCopyArgs(ip, port).initVapor;
	}
	
	initVapor	 {
		hashID = this.hash.abs;
		controls = Dictionary.new;
		labels = Dictionary.new;
		outAddr = NetAddr(ip, port);	
	}
	
	learnIP {
		var func;
		func = {arg time, addr, msg;
			"Listening for a message from Vapor!".postln;
			((addr.ip != "127.0.0.1") and: {addr.ip != "0.0.0.0"}).if({
				addr.ip.postln;
				this.ip_(addr.ip);
				thisProcess.removeOSCFunc(func);
			})
		};	
		thisProcess.addOSCFunc(func);
	}
	
	listen {arg action, remove = true;
		"Listen for '/vaporPing' from Vapor".postln;
		listenResp = OSCresponderNode(nil, '/vaporPing', {arg time, resp, msg, addr;
			"Heard Vapor!".postln;
			ip.isNil.if({
				this.ip_(addr.ip);
			});
			action.value;
			remove.if({resp.remove});
		});	
		listenResp.add;
	}
	
	ping {
		pongResp.isNil.if({
			pongResp = OSCresponderNode(nil, '/pong', {arg time, resp, msg, addr;
				"Vapor heard you!".postln;
				[time, resp, msg, addr].postln;
				pongResp = nil;
				resp.remove;
			}).add;
		});
		this.sendMsg(\ping);
	}
	
	ip_ {arg newIP;
		ip = newIP;
		ip.postln;
		outAddr = NetAddr(ip, port);
	}
	
	*showComputerIP {
		var before = NetAddr.broadcastFlag;
		NetAddr.broadcastFlag = true;
		OSCresponder(nil, '/getMyIP', { arg t,r,msg,addr;
			NetAddr.broadcastFlag = before;
			("Set the IP in Vapor to: " ++ addr.ip ++ " and port: "++NetAddr.langPort).postln;
			r.remove;
		}).add;
	
		NetAddr("255.255.255.255", NetAddr.langPort).sendMsg('/getMyIP');
	}
	
	*dumpAllOSC {arg bool = true;
		bool.if({ 
				thisProcess.addOSCRecvFunc(tmpFunc = { |time, replyAddr, port, msg| 
				if(msg[0] != '/status.reply') {
					"At time %s received message % from % on port %\n".postf( time, msg, replyAddr )
				}  
			})
		}, {
			thisProcess.removeOSCRecvFunc(tmpFunc)
		});
	}
	
	sendMsg {arg ... args;
		outAddr.sendMsg(*args);
	}
	
	touch {arg x, y;
		this.sendMsg(\vaporTouch, x, y);	
	}
	
	cloud {arg x, y;
		this.sendMsg(\vaporCloud, x, y);
	}
	
	bomb {arg x, y, strength = 10000;
		this.sendMsg(\vaporBomb, x, y, strength);
	}
	
	pic {
		this.sendMsg(\vaporPic);	
	}
	
	mode_ {arg modeIn;
		mode = modeIn;
		(mode < 1).if({
			mode = 1;
		});
		(mode > 10).if({
			mode = 10;
		});
		mode = mode.round;
		this.sendMsg(\vaporMode, mode);
	}

	fade_ {arg fadeIn;
		fade = fadeIn;
		(fade < 0.01).if({
			fade = 0.01;
		});
		(fade > 0.99).if({
			fade = 0.99;
		});
		this.sendMsg(\vaporFade, fade);
	}

	viscosity_ {arg viscosityIn;
		viscosity = viscosityIn;
		(viscosity < 0.01).if({
			viscosity = 0.01;
		});
		(viscosity > 0.99).if({
			viscosity = 0.99;
		});
		this.sendMsg(\vaporViscosity, viscosity);
	}	
	
	diffusion_ {arg diffusionIn;
		diffusion = diffusionIn;
		(diffusion < 0.01).if({
			diffusion = 0.01;
		});
		(diffusion > 0.99).if({
			diffusion = 0.99;
		});
		this.sendMsg(\vaporDiffusion, diffusion);
	}
	
	speed_ {arg speedIn;
		speed = speedIn;
		(speed < 0.01).if({
			speed = 0.01;
		});
		(speed > 0.99).if({
			speed = 0.99;
		});
		this.sendMsg(\vaporSpeed, speed);
	}
	
	gravity_ {arg gravityIn;
		gravity = gravityIn;
		(gravity < -1).if({
			gravity = -1;
		});
		(gravity > 1).if({
			gravity = 1;
		});
		this.sendMsg(\vaporGravity, gravity);
	}
	
	colorChange_ {arg colorChangeIn;
		colorChange = colorChangeIn;
		(colorChange < -1).if({
			colorChange = -1;
		});
		(colorChange > 1).if({
			colorChange = 1;
		});
		this.sendMsg(\vaporColorChange, colorChange);
	}
	
	hue_ {arg hueIn;
		hue = hueIn;
		hue = hue % 360.0;
		this.sendMsg(\vaporHue, hue);
	}
	
	reflectX_ {arg reflectXIn;
		reflectX = reflectXIn.asBoolean;
		this.sendMsg(\vaporReflectX, reflectX);
	}

	reflectY_ {arg reflectYIn;
		reflectY = reflectYIn.asBoolean;
		this.sendMsg(\vaporReflectY, reflectY);
	}
	
	reflectCenter_ {arg reflectCenterIn;
		reflectCenter = reflectCenterIn.asBoolean;
		this.sendMsg(\vaporReflectCenter, reflectCenter);
	}	
	
	addResponders {
		responders = [
			OSCdef(\vaporNewTouch ++ hashID, {arg msg, time, addr, recvPort;
				var touchID;
				(addr.ip == ip).if({
					touchID = msg[3];
					newTouchFunc.value(msg, time, addr, recvPort, touchID);
			})
			}, \vaporNewTouch),
			OSCdef(\vaporTouchMoved ++ hashID, {arg msg, time, addr, recvPort;
				var touchID, oldTouchData;
				(addr.ip == ip).if({
					touchID = msg[5];
					touchMovedFunc.value(msg, time, addr, recvPort, touchID);
			})
			}, \vaporTouchMoved),
			OSCdef(\vaporTouchEnded ++ hashID, {arg msg, time, addr, recvPort;
				var touchID, oldTouchData;
				(addr.ip == ip).if({
					touchID = msg[5];
					touchEndedFunc.value(msg, time, addr, recvPort, touchID);
			})
			}, \vaporTouchEnded),
			OSCdef(\vaporCloud ++ hashID, {arg msg, time, addr, recvPort;				(addr.ip == ip).if({
					cloudFunc.value(msg, time, addr, recvPort);
			})
			}, \vaporCloud),
			OSCdef(\vaporBomb ++ hashID, {arg msg, time, addr, recvPort;				(addr.ip == ip).if({
					bombFunc.value(msg, time, addr, recvPort);
			})
			}, \vaporBomb),
			OSCdef(\vaporRotation ++ hashID, {arg msg, time, addr, recvPort;				(addr.ip == ip).if({
					rotationFunc.value(msg, time, addr, recvPort);
			})
			}, \vaporRotation),
			OSCdef(\vaporPinch ++ hashID, {arg msg, time, addr, recvPort;				(addr.ip == ip).if({
					pinchFunc.value(msg, time, addr, recvPort);
			})
			}, \vaporPinch),
			OSCdef(\vaporAccel ++ hashID, {arg msg, time, addr, recvPort;				(addr.ip == ip).if({
					accelFunc.value(msg, time, addr, recvPort);
			})
			}, \vaporAccel)
		];
	}
	
	removeResponders {
		responders.do({arg thisResp; thisResp.free;});
	}
	
	
}