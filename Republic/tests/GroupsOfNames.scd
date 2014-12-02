	// add a group of names to Republic r:
r.addGroup(\A, [\tbs, \izm3, \sara]);
r.groups;
r.replaceGroupNames([\adc, \A]);

	// on sending, the group is replaced with the real names.
	// could remove Duplicates - maybe later.
r.send([\adc, \A], \chat, \adc, \testNurDieDrei);
r.removeGroup(\a, [\b, \c, \d]);

(instrument: \boom, amp: 1, where: \A).play

(instrument: \boom, amp: 1, where: r.groups[\A].choose).play
