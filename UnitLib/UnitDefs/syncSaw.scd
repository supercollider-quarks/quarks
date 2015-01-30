/*
\syncSaw

A synced saw wave generator.
http://en.wikipedia.org/wiki/Oscillator_sync

syncFreq: fundamental frequency (Hz)
sawFreq: frequency of synced saw - prefferably > syncFreq (Hz)
amp: amplitude (0-1)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \sync_saw, { |syncFreq = 440, sawFreq = 440, amp = 0.1|
	UMixOut.ar( 0, SyncSaw.ar( syncFreq, sawFreq, amp ), 0, true ) 
} ).category_( \oscillator )
	.setSpec( \syncFreq, FreqSpec( 2, 20000 ) )
	.setSpec( \sawFreq, FreqSpec( 2, 20000 ) );