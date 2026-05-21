# E-mail — pourquoi vous ne recevez rien

## Cause principale

Votre fichier avait encore :

```properties
spring.mail.password=REPLACE_WITH_APP_PASSWORD
```

Sans **vrai mot de passe d'application Gmail**, aucun e-mail ne part.

---

## Solution A — Demo validation (sans Gmail) — RECOMMANDE

Le projet utilise maintenant **`app.mail.mode=console`** :

- L'e-mail **complet** (feedback, note, commentaire) s'affiche dans la **console IntelliJ**
- Pas besoin de configurer Gmail pour la soutenance

**Apres redemarrage Spring Boot**, publiez une evaluation et regardez IntelliJ :

```
========== B2U-HUB — E-MAIL (MODE CONSOLE) ==========
Vers    : sassichahine68@gmail.com
Objet   : B2U-HUB — Nouvelle evaluation de ...
----------------------------------------
Bonjour Chahine Sassi,
... feedback ...
```

---

## Solution B — Vrais e-mails Gmail

1. https://myaccount.google.com/apppasswords → mot de passe d'application (16 caracteres)
2. Dans `application-local.properties` **decommentez** :

```properties
app.mail.mode=smtp
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=sassichahine68@gmail.com
spring.mail.password=abcdefghijklmnop
```

3. **Redemarrez** PiApplication
4. Test : **GET** `http://localhost:8081/api/mail/status`
5. Puis **POST** `http://localhost:8081/api/mail/test` body `{"to":"sassichahine68@gmail.com"}`

---

## Diagnostic rapide

**GET** `http://localhost:8081/api/mail/status`

| mailMode | Signification |
|----------|---------------|
| `console` | Mail dans les logs IntelliJ seulement |
| `smtp` + `gmailPasswordSet: true` | Pret pour Gmail reel |

---

## Checklist

- [ ] Spring Boot redemarre apres modification
- [ ] GET /api/mail/status → `mailEnabled: true`
- [ ] Publier evaluation avec `studentEmail` rempli
- [ ] Mode console → lire IntelliJ / Mode smtp → verifier Spam
