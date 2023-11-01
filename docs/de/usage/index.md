---
title: Benutzung
---

Standardmäßig können Changesets auch ohne Signatur auf ein Repository gepushed werden.
Mit dem Signature Check Plugin kann dies verhindert werden, sodass nur signierte Changesets gepushed werden können.

Das konkrete Verhalten der Signaturprüfung lässt sich dabei auf globaler, Namespace und Repository Ebene einstellen.
Dabei ist zunächst nur die globale Einstellung aktiv.
Allerdings können die Namespace-Einstellungen explizit die Globale überschreiben.
Dann ist für das Namespace nicht mehr die Globale sondern die jeweilige Namespace-Einstellung aktiv.
Analog dazu kann auch die Repository-Einstellung, die Globale und Namespace-Einstellung überschreiben.
Beim überschreiben werden die Einstellungen nicht zusammengeführt, stattdessen gilt nur die jetzt aktive Einstellung.

Um das Überschreiben zu verhindern, kann man in der Namespace-Einstellung alle Repository-Einstellungen explizit deaktivieren.
Dadurch werden die Einstellungen für alle Repositorys deaktiviert, die zum jeweiligen Namespace gehören.
Dies gilt auch wenn die Repository-Einstellung die Namespace-Einstellung überschreiben sollte.
Analog dazu kann man in der globalen Einstellung auch alle Namespace- und Repository-Einstellungen explizit deaktivieren.
Dadurch wäre nur die globale Einstellung aktiv.

## Globale Einstellung

Um die globale Einstellung zu öffnen, muss man über die primäre Navigation die Seite `Administration` öffnen.
Und im Anschluss über die sekundäre Navigation zu `Einstellungen` und dem Unterpunkt `Signaturprüfung` navigieren.
Sollten die entsprechenden Optionen in der Navigation nicht vorhanden sein, dann fehlen die benötigten [Berechtigungen](#Berechtigungen).

Bei der globalen Einstellung kann man zunächst mittels Checkbox festlegen, ob die Namespace- und Repository-Einstellungen explizit deaktiviert werden sollen.
Als nächstes kann man mittels Checkbox festlegen, ob bei einem Push geprüft werden soll, dass alle Changesets über eine gültige Signatur verfügen.
Sobald die Prüfung aktiviert ist, werden weitere Einstellungsmöglichkeiten für den Benutzer sichtbar.

Dadurch kann der Benutzer festlegen, mittels Chip Input, welche Branches bei einem Push vor unsignierten Changesets geschützt werden sollen.
Falls das Chip Input leer ist, dann werden alle Branches geschützt.
Ansonsten werden nur die angegebenen Branches geschützt.

Als nächstes kann der Benutzer via Radiobuttons festlegen, welche GPG Signaturen zugelassen sind.
Es können entweder beliebige Signaturen gestatten sein oder es muss die Signatur eines SCM-Manager Users sein.
Wenn beliebige Signaturen gestattet sind und der verwendete Public Key einer Signatur dem SCM-Manager bekannt ist,
dann muss die Signatur auch zum Inhalt des jeweiligen Changesets passen, da ansonsten der Push mit diesem Changeset abgelehnt wird.
Ist der Public Key nicht bekannt, dann ist es nicht möglich zu überprüfen ob die Signatur korrekt ist.
In diesem Fall reicht es, wenn das Changeset über eine Signatur verfügt.
Wenn ausschließlich Signaturen von SCM-Manager Usern gestattet sind, dann wird bei jeder Signatur geprüft,
ob der Public Key zu einem User gehört und ob die Signatur mit dem Changeset übereinstimmt.
Ansonsten würde der Push mit dem jeweiligen Changeset abgelehnt werden.

Zum Schluss muss der Benutzer seine Einstellungen mit dem `Speichern`-Button noch speichern.

## Namespace-Einstellung

Um die Namespace-Einstellung zu öffnen muss man zunächst auf die `Repositories` Seite über die primär Navigation gelangen.
Hier sieht man eine Übersicht über alle Namespaces und die darin enthaltenen Repositorys.
Neben jedem Namespace befindet sich ein Button mit dem man die Seite mit den Einstellungen zu diesem Namespace öffnen kann.
Auf dieser Seite kann man über die sekundäre Navigation mit dem Unterpunkt `Signaturprüfung` die Seite zur Namespace-Einstellung öffnen.
Sollten die entsprechenden Optionen in der Navigation nicht vorhanden sein, dann fehlen die benötigten [Berechtigungen](#Berechtigungen) oder die Namespace-Einstellung wurde global deaktiviert.

Bei der Namespace-Einstellung kann man mittels Checkbox zunächst festlegen, ob die globale Einstellung überschrieben werden soll.
Dies würde nur das jeweilige Namespace betreffen.
Sobald die globale Einstellung überschrieben werden soll, dann werden dem Benutzer weitere Einstellungsmöglichkeiten gezeigt.
Mit der nächsten Einstellung kann der Benutzer via Checkbox festlegen, ob die Repository-Einstellungen explizit deaktiviert werden sollen.
Dies würde nur die Repositorys betreffen, die im jeweiligen Namespace liegen.
Die anschließenden Einstellungsmöglichkeiten sind analog zu der globalen Einstellung.

## Repository-Einstellung

Um die Repository-Einstellung zu öffnen muss man zunächst auf die `Repositories` Seite über die primär Navigation gelangen.
Hier sieht man eine Übersicht über alle Namespaces und die darin enthaltenen Repositorys.
Anschließend muss das Repository angeklickt werden, welches eingestellt werden soll, dadurch öffnet sich die Seite des Repositorys.
Mit der sekundären Navigation und dem Unterpunkt `Signaturprüfung` im Reiter `Einstellungen` kann die Repository-Einstellung geöffnet werden.
Sollten die entsprechenden Optionen in der Navigation nicht vorhanden sein, dann fehlen die benötigten [Berechtigungen](#Berechtigungen) oder die Repository-Einstellung wurde global oder durch den Namespace deaktiviert.

Zunächst kann der Benutzer mittels Checkbox festlegen, ob die Einstellungen von diesem Repository die globale und jeweilige Namespace-Einstellung überschreiben soll.
Bei einer Überschreibung werden weitere Einstellungsmöglichkeiten analog zur globalen Einstellung angezeigt.

## Berechtigungen

Um die Einstellungen lesen oder verändern zu können benötigt es die jeweilige Berechtigung.
Für die globale Einstellung werden mindestens die `Basis für Administration` und die `Signaturprüfung konfigurieren` Berechtigungen benötigt.
Für die Namespace-Einstellung muss die Berechtigung `Berechtigungen auf Namespaces lesen` global vorhanden sein und für das jeweilige Namespace die Berechtigung `Signaturprüfung konfigurieren`.
Für die Repository-Einstellung muss analog zur Namespace-Einstellung die gleichen Berechtigungen auf Namespace oder Repository Ebene vergeben sein.
