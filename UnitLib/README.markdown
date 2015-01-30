Unit Lib
===============================================================================
A project of the [Game Of Life Foundation](http://gameoflife.nl/en).

![](https://github.com/GameOfLife/Unit-Lib/raw/master/HelpSource/Classes/ULib%20screenshot.png)

The Unit Library is a system that provides high level abstractions on top of the SuperCollider language. These abstractions simplify the process of creating synthesis units, preparing the resources needed to play those units, patching multiple units and assigning time events (with a start time and duration) to a group of units.
The library was created to serve as the backbone of the Game of Life Foundation's WFSCollider V2.0, a spatialization software entirely based on SuperCollider that allows users without knowledge of computer programming languages to create spatial compositions for playback using the Wave Field Synthesis spatialization technique.  

The basic element of the Library is the unit (U) and corresponding unit def (UDef), that play a similar role to Synth and SynthDef. Upon creation each UDef is added to a global library, and from then on units can be created only by using the name `(e.g. Udef(\sine,{ |freq=440| UOut.ar(0, SinOsc.ar(freq) ) }); U(\sine) )`.  The arguments of the unit are persistent and can be set and retrieved locally (without contacting the server), even if the unit is not playing in the server(s). 

Specs can be assigned to the arguments of each Unit. Arguments are not restrained to just float numbers, they can be a Point, a Rect, or even more complex objects  such as BufSndFile that represent a sound file loaded into a buffer. There are also GUI's defined for each type of spec, which allow for automatic creation of a GUI view for each unit.

Units can be grouped together in chains using the UChain object (e.g. `UChain(U(\sine), U(\output))` ). Each chain is a rack of units, where the output of each unit can be routed to the following unit. The routing is done using private busses, and is erased after each chain, so the output of a unit of one chain can never interfere with a unit in a different chain. Units in this context can be thought of effects or plugins in a DAW, although the full flexibility of SuperCollider is available. To get input from and output to other units in the chain the UIn and UOut pseudo-ugens are used. These ugens create automatic names for controls that allow changing the private bus number. This then allows easy re-patching of the units from a gui.

Chains can be organized in time using a UScore. Each chain is also an event (inherits from UEvent) with a start time, duration and fade in and fade out times. Scores are played in real-time, and take care of preparing the events during for play playback (buffer loading, etc). The library provides a  ScoreEditor GUI which works mostly as modern DAW:  the start time can be changed by dragging the event on the timeline and the duration by dragging the end of the event. Many operations are available such as copy/paste, undo/redo, split, trim, duplicate, etc. Scores can have other scores as events, allowing for indefinite nesting of scores inside scores.

Before playback both units, chains and scores have to prepared. This is done by calling '.prepare' on either of them (preparing a score prepares all the chains within). All the preparation is then automatically taken care of, since the "smart" arguments, such as BufSndFile, know how to prepare themselves.

For more complex uses, where a UDef does not provide the necessary functionality (e.g. multiple synths per unit) a FreeUDef class is available. By defining the 'createSynthFunc' function it's possible to have arbitrarily complex logic before instantiating the actual synth(s).

The library comes with a comprehensive set of GUI's that allow for manipulation of all of the objects graphically, so that a person can use the system without SuperCollider programming knowledge. Experienced SuperCollider users can on the other hand use the system entirelly from code.

## System Requirements ##

 - SuperCollider 3.5 or higher
 - PopUpTreeMenu quark (only required for Qt)
 - VectorSpace quark
 - wslib quark

## Installation ##


1) Go to https://github.com/GameOfLife/Unit-Lib/tags, download zip for latest version (currently v.0.1) and place in extensions folder.

or 

2) Clone with git: ‘git clone git://github.com/GameOfLife/Unit-Lib.git’

## Git Repo##

[github](https://github.com/GameOfLife/Unit-Lib)

## Basic Usage ##
```supercollider

ULib.startup;

UChain(\sine, \output ).gui

UScore( UChain(\sine, \output ) ).gui

```
## Acknowledgments ##
Unit Lib was developed by Miguel Negrão and Wouter Snoei as a project of the Game of Life Foundation.

## License ##
Unit Lib is licensed under the GNU GENERAL PUBLIC LICENSE Version 3.  

