# Modality Toolkit

The *Modality Toolkit* is a library to facilitate accessing (hardware) controllers in SuperCollider.
It is designed and developed by the ModalityTeam, a group of people that see themselves as both developers and (advanced) users of SuperCollider.

The central idea behind the Modality-toolkit is to simplify creation of individual (electronic) instruments with SuperCollider, using controllers of various kinds. To this end, a common code interface, MKtl, is used for connecting  controllers from various sources (and protocols). These are atm. HID and MIDI; OSC, Serialport and GUI-based are planned to be integrated.

The name *Modality* arose from the idea to scaffold the creation of modal interfaces, i.e. to create interfaces where one physical controller can be used for different purposes and it is possible to *switch its functionality, even at runtime*.
It is our believe that integration of such on-the-fly remapping features helps to create a setup much more flexible, powerful, and interesting to play. 
Such a modal interface allows to cope with fast changes of overall direction as it can be necessary when e.g. improvising with musicians playing acoustic instruments.

## Installation and getting started

Either clone the git repository or download a static zip file to the SuperCollider Extensions folder:

+ **git** -- ```git clone https://github.com/ModalityTeam/Modality-toolkit.git Modality```
+ **zip** -- [zip file](https://github.com/ModalityTeam/Modality-toolkit/archive/master.zip) of the current repository head.

Evaluate ````Platform.userExtensionDir```` to get the path to the SuperCollider extension folder.

## Getting started

Please read the article on "Modality" in the SuperCollider help system.

## Associated Projects

+ [FPLib](https://github.com/miguel-negrao/FPLib) is a functional programming library for SuperCollider developed by Miguel Negrao and integrates into the modality toolkit.
+ [Unit Library](https://github.com/GameOfLife/Unit-Lib) provides high level abstractions on top of SuperCollider to help users without knowledge of computer programming languages to create (interactive) compositions.
+ [Controller Booklet](http://tai-studio.org/index.php/projects/controller-booklet/) is a set of paper templates for conceptualising, scribbling and experimenting with controllers and their typical layouts.

## ModalityTeam
+ [Jeff Carey](http://jeffcarey.foundation-one.org/)
+ [Bjoernar Habbestad](http://www.bek.no/~bjornar/)
+ [Marije Baalman](http://www.nescivi.eu/)
+ [Alberto de Campo](http://albertodecampo.net/)
+ [Wouter Snoei](http://www.woutersnoei.nl/)
+ [Till Bovermann](http://tai-studio.org/)
+ [Miguel Negrao](http://www.friendlyvirus.org/miguelnegrao/)
+ [Robert van Heumen](http://west28.nl/)
+ [Hannes Hoelzl](http://www.earweego.net/)


## Associated Organisations

+ [BEK](http://www.bek.no/), Bergen Center for Electronic Arts, is a non-profit organisation situated in Bergen, Norway, functioning as a national resource centre for work within the field of arts and new technology.
BEK works with both artistic and scientific research and development and puts into practice an amount of mixed artistic projects. It also practices an educational program that includes courses, workshops, talks and presentations.
+ [STEIM](http://steim.org/) (the STudio for Electro-Instrumental Music) is an independent electronic music center unique in its dedication to live performance. The foundation’s artistic and technical departments support an international community of performers, musicians, and visual artists, to develop unique instruments for their work. STEIM maintains a vibrant residency program whereby artists are provided with an artistic and technical environment in which concepts can be given concrete form. Ideas are catalysed by providing critical feedback grounded in professional experience. Finally, new creations are then exposed to a receptive responsive niche public at STEIM before being groomed for a larger audience.
+ [3DMIN](http://www.hybrid-plattform.org/en/projects/alle/34-aktuelle-projekte/663-design-development-and-dissemination-of-new-musical-instruments-4) (Design, Development and Dissemination of New Musical Instruments) is an interdisciplinary project between [UdK Berlin](http://www.udk-berlin.de/) and [TU Berlin](http://www.tu-berlin.de/) to develop new electronic musical instruments for contemporary music practice.

## Acknowledgements
Modality and its research meetings have kindly been supported by [BEK](http://www.bek.no/) and [STEIM](http://steim.org/).
