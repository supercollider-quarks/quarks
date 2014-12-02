// initialise 
// (this file doesn't work if you don't add nickname) ..
(
q = ();
q.nickname = \4john; // .. so put your name here before using it
(Document.current.path.dirname ++ "/init_purloined_letters.scd").load;
);

// start new letter..
q.startLetter;


// or quit..
q.quitGame;


