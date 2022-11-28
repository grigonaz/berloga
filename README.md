# Studentský portal BERLOGA

Hlavní podstatou projektu BERLOGA je studentský portál, který poskytuje svým uživatelům různé funkce užitečné pro studium a pohodlné způsoby komunikace. Hlavními vlastnostmi projektu jsou group messaging, sdílení souborů, kalendář, rozvrh hodin a diskuzní fórum.   

Studentský portál poskytuje možnost vytváření stránek ke každému předmětu, které obsahují informace o samotném předmětu a záznamy z přednášek či seminářů. Diskuzní fórum umožní pokládat dotazy a dostávat odpovědi jak od žáků, tak od učitelů. Každý student má vlastní sekci s kalendářem obsahujícím studijní rozvrh a možností vytváření dalších aktivit a připomínek.   

Systém má několik rolí pro přihlášeného uživatele: moderátor, učitel, student a některé stránky bude moci prohlížet i nepřihlášený uživatel. V závislosti na roli uživatele (ne)budou k dispozici některé funkce portálu. Uživatelé mohou mít několik rolí zároveň (např. učitel může být zároveň moderátorem).   

## Hlavní funkcionalita

| Funkce | Kde se nachází | Z kolika % je hotová nebo popis | Detaily |
|--------|----------------|----------------------|---------|
| Java + Spring | Všude | 100% | Java 1.8, Spring 2.3.0 a vyšší |
| Readme v gitu | V root složce gitlabu | 100% |  |
| **Design patterny** |  | 100% |  |
|  | **Název** | **Kde se nachází** | **Detaily** |
| | *Builder* | Ve většině DTO v package `cz.cvut.fel.berloga.controller.dto`<br/> a v entitách |   |
| | *Inversion of control* | Spring IoC  |   |
| | *DTO pattern* | V package `cz.cvut.fel.berloga.controller.dto` |   |
| | *Lazy initialization* | Spring hibernate persistance |  `fetch = LAZY` |
| | *Interceptor* | V package `cz.cvut.fel.berloga.controller.config` <br/>  `cz.cvut.fel.berloga.controller.interceptors` |  Spring interceptory |
| Inicializace Apky | Service `cz.cvut.fel.berloga.service.StartupService` | 100% |  |
| Společná DB | Externí aplikace | 100% | PostgreSQL verze >= 9 |
| Cache | Externí aplikace + konfigurace v package <br/> `cz.cvut.fel.berloga.config` <br/> a použití v `ChatServise` | Cachování entit `ChatEntity` | Problematické nasazení, použit HazelCast |
| Interceptory |  | 100% | Logovací systém |
| ElasticSearch | Externí aplikace, konfigurace v <br/> `cz.cvut.fel.berloga.config` <br/> použito v `ForumService` | Vyhledávání v názvech `QuestionForumEntity` |  |
| JUnit a Jupiter testy | V balíčku `cz.cvut.fel.berloga.controller` ve složce `src/test` | Většina základních testů hotovo | Nespouští se automaticky<br/> při buildu, moc času, musí se ručně <br/>(v IDEI pravým na složku tests, <br/>a spustit testy) |
| REST API | V balíčku `cz.cvut.fel.berloga.controller` | 100% | Viz další řádek |
| Swagger doc generátor | Export do složky `/target/swagger/` | 100% | Slouží k náhledu api a k generování frontendu |
| OpenApi Angular generátor | Export do složky `/target/angular-api-client/` | 100% | Generuje kompletní api pro FE |
| 3-Layer architektura | Celý program | 100% |  |

## Inicializační postup
### Backend
1) Stáhnout PostgreSQL verzi 11 a vyšší (www.postgresql.org/download/)
2) Při instalaci vybrat instalcai pgAdmin
3) Spustit pgAdmin, vytvořit nového uživatele (username: 'berloga', password: 'berloga')
4) Vytvořit novou DB s nazvem berloga a přidat uživatele, vytvořeného výše
5) Stáhnout ElasticSearch (https://www.elastic.co/downloads/elasticsearch)
6) Spustit ElasticSearch pomocí elasticsearch.bat (Windows) nebo elasticsearch.exec (macOS)
7) Pomocí IDE, napřiklad IntelliJ IDEA otevřit projekt a spustit ho ve třide BerlogaApplication

### Frontend
1) Stáhnout a nainstalovát Maven (https://maven.apache.org/download.cgi)
2) Stáhnout a nainstalovát NodeJS (https://nodejs.org/en/download/)
3) Nainstalovát angular CLI pomoci příkazu npm install -g @angular/cli
4) Spustit ve složce berloga build.bat
5) Spusit příkaz 'npm i' ve složce frontend
6) Ve stejne složce spustit příkaz 'ng serve'
