Uzasadnienie rozwiązania:
Wykonując to zadanie chciałem nauczyć się czegoś nowego - pogłębiłem znajomość Apache HttpClient. Dlatego zaimplementowałem RateLimiter jako moduł do Apache HttpClient i przy okazji użyłem cache.

Pozostaje do zrobienia (niewysoki priorytet):
- więcej testów
- cache po stronie API (ETag&Co)
- dokumentacja API używając np. OpenAPI (wygenerowanie OpenAPI yaml z kodu, lub odwrotnie) oraz opcjonalnie sandbox (statyczny JS + OpenAPI yaml  lub Springfox)
- minifikacja JDK uzywając jlink (nie zostało zrobione z powodu braku oficjalnego wsparcia ze strony Spring)  
- Rozproszony cache dla repo info odpowiedni punkt wejściowy został zaznaczony

Myślę ze rozwiązanie było by bardziej zwinne gdybym użył "microframeworków" np. micronaut, co prawda kosztem utrzymania.

Budowanie packi ``` ./gradlew build docker ```
Deployment z docker image ustawienia poprzez ENV (więcej: ``` application.properties ``` i  ``` SetupContainerSpec ```)