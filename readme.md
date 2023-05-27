----------Założenia----------
"Stacja Benzynowa"
StacjaBenzynowa posiada dystrybutory i myjnie.
StacjaBenzynowa przechowuje kolejki do każdego dystrybutora i myjnie z osobna.

"Kasy"
Kasy jest podobna w założeniach do StacjaBenzynowa.
Kasy przechowuje jedną kolejkę do jednej kasy.

"Klienci"
Klienci odpoweidzialny jest za generowanie, przesyłanie i zbieranie danych o Klientach.

----------Cykl życia klienta----------
1. "Klienci" informują nowo powstałym kliencie (clientId, preferencje)
"StacjaBenzynowa" odpowida na podstawie preferencji do którego obiektu się ustawia,
jakie ma miejsce w kolejce, (tworzy sobie kolejke i ustawia tam klientów) zwraca info do "Klienci"
"Klienci" aktualizuje obiekt klienta co do miejsca w kolejce

2. "StacjaBenzynowa" informuje o skończeniu obsługi klienta, przesuwa klientow w lokalnej kolejce
"Klienci" aktualizuje numer w kolejce zakolejkowanych klientow a obsluzonego przesylaja informacje
dotyczące prefernecji.
"StacjaBenzeynowa" podstawie preferencji przypisuje do kolejnego obiektu/przesyla do "Kasy"

3. Adekwatna sytuacja jak w 2 zachodzi w "Kasy"
"Klienci" odbieraja informaacje o exitTime klienta




