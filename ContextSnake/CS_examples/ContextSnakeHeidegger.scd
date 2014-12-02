//Martin Heidegger:
//DIE FRAGE NACH DER TECHNIK
//Vortrag, gehalten am 18. November 1953 im Auditorium Maximum der Technischen Hochschule M�nchen, in der Reihe 'Die K�nste im technischen Zeitalter', veranstaltet von der Bayerischen Akademie der Sch�nen K�nste unter Leitung des Pr�sidenten Emil Preetorius, im Druck erschienen in Band III des Jahrbuches der Akademie (Redaktion: Clemens Graf Podewils), R. Oldenbourg M�nchen 1954, S. 70 ff.

f = File("Heidegger_Text.scd".resolveRelative, "r"); a = f.readAllString; f.close;
a.size;
b = a.split($\n);
b = b.collect(_.split($ )).flatten(1).collect(_.select(_.isAlpha));
b.size;

d = ();
b.do { |word| var keyword = word.asSymbol; d.put(keyword, d[keyword] ? 0 + 1); };
d.size; // wortschatz
d.sum

e = SortedList[].function = {|a, b| a[1] > b[1] }; d.keysValuesDo { |k, v| e.add([k, v]); };
e.printAll;


c = ContextSnake([a], "Im", 10);
e = c.asStream;
e.next;
x = e.nextN(3000).join;

// example result:
"Im folgenden fragen wir nach der Herstellung von der Technik, solange wir uns auf diese Fragen nicht einlassen, bleibt die Kausalit�t und mit ihr das Instrumentale und anthropologische Bestimmung der Technik hei�en. Wer wollte leugnen, da� sie auch noch f�r die moderne Technik ist nicht das Wahre suchen. Wir m�ssen fragen: weshalb gibt es gerade vier Ursachen:
1. die causa formalis, die Form, die Gestalt, in die das Material eingeht; 3. die causa formalis, die Finalit�t, �berhaupt nicht mehr zur Kausalit�t ihrerseits in dem, was sie ist, ins Dunkel h�llt? Zwar tut man seit Jahrhunderten lehrt die Philosophie, es g�be vier Ursachen sind die unter sich zusammengeh�ren?
Solange wir nur das Technische vorstellen und betreiben, uns damit abfinden oder ihm ausweichen. �berall bleiben wir unfrei an die Technik der Herrschaft des Menschen. Beide Bestimmung der Technik der Herrschaft des Menschen. Beide Bestimmung der Technik sei kein blo�es Mittel, wie steht es dann mit dem Willen, sie zu meistern? Allein wir sagten doch, die instrumentale Bestimmung der Technik ist ein Tun des Menschen gefertigtes Mittel im Vergleich zum Wasserkraftwerk ist mit seinen Turbinen und Generatoren ein von Menschen gesetzten Zweck. Auch das Raketenflugzeug, auch die moderne Technik zutrifft, von der man sonst mit einem gewissen Recht behauptet, sie sei gegen�ber der �lteren handwerklichen Technik etwas durchaus Anderes und darum Neues. Auch das Raketenflugzeug, auch die moderne Technik zu bringen. Alles liegt daran, die Technik <geistig in die Hand bekommen>. Man will, wie es hei�t, die Technik spricht. Die instrumentale Bestimmung der Technik spricht. Die instrumentale Vorstellung, der man heute besonders gern huldigt, macht uns vollends blind gegen das Wesen der Technik �ffnet. Entsprechen wir diesem, dann verm�gen wir es, das Technische vorstellen und bedeutet dasjenige, was bewirkt, da� etwas im Erfolg so oder so ausf�llt. Die Lehre von den vier Ursachen wie ein Mittel ist und ein menschliches Tun. Zu dem, was uns aus seinem Wesen her angeht. Die richtig sei? Sie richtet sich offenkundig nach dem, was man vor Augen hat, wenn man von Technik sei richtig. Gewi�. Das Richtige hindurch das Richtige hindurch das Richtige stellt an dem, was sie sei. Jedermann kennt die beiden Aussagen, die unsere Frage beantworten. Die eine sagt: Technik ist ein Tun des Menschen. Beide Bestimmung der Technik sei richtig. Gewi�. Das Richtige hindurch das Richtige stellt an dem, was vorliegt, jedesmal irgend etwas Zutreffendes fest. Die Feststellung braucht jedoch, um richtig zu sein, das Vorliegende keineswegs in seinem Wesen zu enth�llen. Nur dort, wo solches Enth�llen geschieht, ereignet sich das Wahre suchen. Wir m�ssen fragen: weshalb gibt es gerade vier Ursachen? Was hei�t in Bezug auf die genannten vier eigentlich <Ursache> ? Woher bestimmt wird; 4. die causa formalis, die Finalit�t, �berhaupt nicht mehr zur Kausalit�t und mit ihr das Instrumentale und mit diesem die g�ngige Vorstellung"';

z = "Im folgenden fragen wir nach der Technik. Das Fragen baut an einem Weg. Darum ist es ratsam, vor allem auf den Weg zu achten und nicht an einzelnen S�tzen und Titeln h�ngenzubleiben. Der Weg ist ein Weg des Denkens. Alle Denkwege f�hren, mehr oder weniger vernehmbar, auf eine ungew�hnliche Weise durch die Sprache. Wir fragen nach der Technik und m�chten dadurch eine freie Beziehung zu ihr vorbereiten. Frei ist die Beziehung, wenn sie unser Dasein dem Wesen der Technik �ffnet. Entsprechen wir diesem, dann verm�gen wir es, das Technische in seiner Begrenzung zu erfahren. Die Technik ist nicht das gleiche wie das Wesen der Technik ganz und gar nichts Technisches. Wir erfahren darum niemals unsere Beziehung zum Wesen der Technik, solange wir nur das Technische vorstellen und betreiben, uns damit abfinden oder ihm ausweichen. �berall bleiben wir unfrei an die Technik gekettet, ob wir sie leidenschaftlich bejahen oder verneinen. Am �rgsten sind wir jedoch der Technik ausgeliefert, wenn wir sie als etwas Neutrales betrachten; denn diese Vorstellung, der man heute besonders gern huldigt, macht uns vollends blind gegen das Wesen der Technik.
Als das Wesen von etwas gilt nach alter Lehre jenes, was etwas ist. Wir fragen nach der Technik, wenn wir fragen, was sie sei. Jedermann kennt die beiden Aussagen, die unsere Frage beantworten. Die eine sagt: Technik ist ein Tun des Menschen. Beide Bestimmungen der Technik geh�ren zusammen. Denn Zwecke setzen, die Mittel daf�r beschaffen und ben�tzen, ist ein menschliches Tun, kann deshalb die instrumentale und anthropologische Bestimmung der Technik dunkel und grundlos. Man pflegt seit langem die Ursache als das Bewirkende vorzustellen. Wirken hei�t dabei: Erzielen von Erfolgen, Effekten. Die causa efficiens, die eine der vier Ursachen, bestimmt in ma�gebender Weise alle Kausalit�t. Das geht so weit, da� man die causa finalis, die Finalit�t, �berhaupt nicht mehr zur Kausalit�t rechnet. Causa, casus, geh�rt zum Zeitwort cadere, fallen und bedeutet dasjenige, was bewirkt, da� etwas im Erfolg so oder so ausf�llt. Die Lehre von den vier Ursachen wie eine sonnenklare Wahrheit vom Himmel gefallen. Indessen d�rfte es an der Zeit sein zu fragen: weshalb gibt es gerade vier Ursachen? Was hei�t in Bezug auf die genannten vier eigentlich <Ursache> ? Woher bestimmt sich der Ursachecharakter der vier Ursachen so einheitlich, da� sie zusammengeh�ren?
Solange wir uns auf diese Fragen nicht einlassen, bleibt die Kausalit�t und mit ihr das Instrumentale auf die vierfache Kausalit�t zur�ckf�hren. Wie aber, wenn sich die Kausalit�t ihrerseits in dem, was sie ist, ins Dunkel h�llt? Zwar tut man seit Jahrhunderten so, als sei die Lehre von den vier Ursachen wie eine sonnenklare Wahrheit vom Himmel gefallen. Indessen d�rfte es an der Zeit sein zu fragen: weshalb gibt es gerade vier Ursachen? Was hei�t in Bezug auf die genannten vier eigentlich <Ursache> ? Woher bestimmt sich der Ursachecharakter der vier Ursachen so einheitlich, da� sie"

c.longestSnippets(y);
c.longestSnippets(z);
c.longestSnippets(x);
