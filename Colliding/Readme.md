Colliding
----------
A simplified environment for learning and live coding


###requirements

- Freesound2

- TabbedView2


###motivation

Supercollider is an incredibly powerful environment, but sometimes a more constrained interface can be useful. Colliding is a simplified environment for learning and playing with SuperCollider, it tries to avoid one of the major sources of confusion for beginners: aynchronous operations happening between language and server. Many of these are implemented through a graphical interface. This simplicity is also useful for live situations, so that the live coder can focus on sound generation. Colliding is currently focused on building synthdefs, although an experimental "advanced" mode allows any playable code to be run via NodeProxy. Other restrictions include: there is only one Server (internal), and up to 8 tabs and 8 buffers can be used. 


### basic operation

To start colliding, create an instance in the SuperCollider IDE:

```
Colliding.new // basic

Colliding.new(1) // "advanced" mode

Colliding.new(0,"<freesound_api_key>") // basic with freesound 
```

In order to use Freesound, an API key is required. Obtain one here:
http://www.freesound.org/api/apply/


A tab is created using the "+" button. Each tab contains the code to create a synth definition. To play a synth with that code, press "▸"  or cmd/ctrl-return. Press "■" cmd/ctrl-backspace to stop it. To compile the code without playing, press "✓".
In basic mode, the code you write in each tab is the same that you would write in a synth definition, but some arguments are defined for you.


### qwerty
An isomorphic qwerty keyboard can be used in basic mode to control synths with the computer keyboard. This is open with "⌨". Be aware that clicking on the widget keys does nothing, only the physical keys work.
Note that when using the keyboard you are expected to trigger synths with an envelope, otherwise your synth will keep playing forever. The keyboard and the slider of each tab provide some predefined arguments:

- freq and key are controlled by the qwerty keyboard. 
- amp is controlled by the big slider at the right of each tab.
- gate is the trigger sent when a key is pressed, used to trigger envelopes.


### buffers
Up to 8 buffers can be managed via graphical interface, using the buttons at the right. Each button corresponds to the buffer number. For each buffer you can load an audio file, download a sound from freesound.org or allocate a buffer. Short buffers can also converted to wavetable for using with wavetable oscillators, but use this only if you know what you are doing.


### project management
Colliding  includes some primitive project management functionality. Each project is composed of a bunch of .cld files, which are just text files with the content of each tab, and an audio folder for sound files. Each folder in the audio folder corresponds to one of the 8 buffers. Click "⇊" to create the project or to save its current state. Click "⇈" to open a project by selecting any of its .cld files. Note that once a project is open, you can't open another one. This is a provisional safeguard to avoid mixing or overwriting projects. Similarly, you can't open projects if you have text in any tab.
