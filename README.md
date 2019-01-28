# CTCommands

Whitelist für Befehle & Textfile-Viewer

/ctext <fileName> [player|] // fileName = ohne ".txt" am ende

Der Befehl sendet den Inhalt der angegebenen Textdatei als Chat nachricht.
Wahlweise kann die Nachricht mit dem 2. Parameter an einen bestimmten oder alle Spieler im Netzwerk
gesendet werden.

Formatierungscodes mit '&' werden berücksichtigt.

Desweiteren können in der whitelist.yml festgelegt werden, welche Befehle für welche Permission-Gruppen sichtbar (Auto-Complete, Tab) und nutzbar sind.
Spieler mit der permission ctcommands.bypass werden von der Whitelist nicht beeinträchtigt.