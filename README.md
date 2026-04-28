# ClaudeChat — Plugin Minecraft

Plugin do Paper 1.21.1 umożliwiający graczom rozmowę z Claude AI bezpośrednio z czatu gry.

## Funkcje

- Komenda `/claude <pytanie>` — wysyła pytanie do Claude i zwraca odpowiedź tylko pytającemu graczowi
- Claude "wie" na jakim serwerze jest — nazwa i opis serwera trafiają do system prompt
- Cooldown między pytaniami — konfigurowalny, chroni przed spamem API
- System uprawnień — dostęp tylko dla graczy z odpowiednim permissionem
- W pełni konfigurowalne wiadomości, prefix odpowiedzi i model Claude'a

## Wymagania

- Java 21+
- [Paper](https://papermc.io/) 1.21.1
- Klucz API Anthropic ([console.anthropic.com](https://console.anthropic.com/))

## Instalacja

1. Pobierz `ClaudeChat-1.0.0.jar` z [Releases](../../releases)
2. Wrzuć plik do folderu `plugins/` na serwerze
3. Uruchom serwer — plugin wygeneruje `plugins/ClaudeChat/config.yml`
4. Uzupełnij `api-key` w config.yml swoim kluczem Anthropic
5. Zrestartuj serwer lub przeładuj pluginy

## Budowanie ze źródeł

Wymagany Maven i JDK 21.

```bash
git clone https://github.com/TWOJ-USERNAME/claude-minecraft-plugin.git
cd claude-minecraft-plugin
mvn package
```

JAR znajdziesz w `target/ClaudeChat-1.0.0.jar`.

## Konfiguracja

Plik `plugins/ClaudeChat/config.yml`:

```yaml
# Klucz API Anthropic
api-key: "WSTAW-TUTAJ-SWOJ-KLUCZ-API"

# Model Claude
# claude-haiku-4-5-20251001  — najszybszy i najtańszy (zalecany)
# claude-sonnet-4-6          — mądrzejszy, wolniejszy
# claude-opus-4-7            — najpotężniejszy, najdroższy
model: "claude-haiku-4-5-20251001"

# Cooldown między pytaniami (sekundy)
cooldown-seconds: 30

# Limit znaków w pytaniu
max-question-length: 500

# Prefix odpowiedzi (obsługuje kody kolorów &)
chat-prefix: "&b[Claude] &f"

# Informacje o serwerze przekazywane do Claude
server-name: "Mój Serwer Minecraft"
server-description: "Serwer survivalowy na Paper 1.21.1"

# Dodatkowe instrukcje dla Claude (opcjonalne)
system-prompt-extra: ""
```

### Dostosowanie wiadomości

Wszystkie wiadomości widoczne dla graczy są konfigurowalne w sekcji `messages:` w config.yml. Obsługują kody kolorów Minecraft (`&a`, `&b`, `&c` itp.).

## Uprawnienia

| Permission | Opis | Domyślnie |
|---|---|---|
| `claude.use` | Pozwala używać `/claude` | OP |
| `claude.bypass-cooldown` | Pomija cooldown | OP |

Aby dać dostęp wszystkim graczom (np. przez LuckPerms):

```bash
/lp group default permission set claude.use true
```

## Użycie

```
/claude <pytanie>
```

Przykłady:

```
/claude Jak zrobić portal do Netheru?
/claude Jakie są zasady tego serwera?
/claude Ile drewna potrzebuję do zbudowania domku 5x5?
```

Odpowiedź widzi tylko gracz który zadał pytanie. Każde pytanie jest niezależne — plugin nie przechowuje historii rozmowy.

## Koszty API

Plugin używa API Anthropic, które jest płatne. Szacunkowy koszt przy modelu Haiku:

| Użycie | Koszt |
|---|---|
| 1 pytanie (~200 tokenów) | ~$0.00005 |
| 1000 pytań | ~$0.05 |

Szczegółowe cenniki: [anthropic.com/pricing](https://www.anthropic.com/pricing)
