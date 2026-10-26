# Development Plan — GreenshopApplication

Робочий чеклист. Відмічай `[x]` по завершенню кожного пункту. Детальний опис проєкту,
правила роботи та відомі проблеми — у `CLAUDE.md`.

## Етап 0 — Фундамент (ЗРОБЛЕНО)

- [x] README (огляд проєкту, стек, запуск, API)
- [x] Очищення pom.xml (видалено jbcrypt, Spring Security/Hibernate керує Boot, Stripe на стабільній версії)
- [x] Dockerfile для застосунку
- [x] docker-compose (застосунок + PostgreSQL)
- [x] Перевірка на закомічені секрети — не знайдено, `.env` в .gitignore

## Етап 1 — Виправлення авторизації (наступний крок, перед або разом із тестами)

Знайдено аудитом 23.10.2026, досі актуально. Кожен пункт — окрема гілка/коміт(и),
бо торкається кількох контролерів.

- [ ] Виправити IDOR: `UserCartController`, `UserCartItemController`, `UserOrderController`
      мають брати `userId` з автентифікованого principal (JWT), а не з path/query/body
- [ ] Виправити порядок правил у `SecurityConfig.securityFilterChain` — специфічні
      матчери по HTTP-методу мають йти ПЕРЕД загальним `hasAnyRole` матчером
- [ ] Додати глобальний `@ControllerAdvice`/`@ExceptionHandler` замість розрізнених try/catch
- [ ] Виправити `AuthService.authenticate()` — неправильний пароль повинен повертати 401,
      а не необроблений 500

## Етап 2 — Тестування та CI

- [ ] Unit-тести на сервісний шар (JUnit 5 + Mockito)
- [ ] Інтеграційні тести з Testcontainers (реальний PostgreSQL у контейнері)
- [ ] Тести на знайдені в Етапі 1 проблеми (IDOR regression-тести — переконатись, що фікс працює)
- [ ] GitHub Actions workflow: build + test на push/PR

## Етап 3 — Redis

- [ ] Підняти Redis у docker-compose
- [ ] Кешування каталогу товарів (`@Cacheable`/`@CacheEvict` на продуктах/категоріях)
- [ ] Перевірити інвалідацію кешу при зміні товару через management-ендпоінти

## Етап 4 — Kafka

- [ ] Підняти Kafka (+ Zookeeper або KRaft) у docker-compose
- [ ] Подія "order created" при оформленні замовлення
- [ ] Другий компонент-слухач, що обробляє цю подію (наприклад, лист підтвердження або лог)

## Етап 5 — Деплой в Azure

- [ ] Керована PostgreSQL в Azure
- [ ] Деплой застосунку (App Service або контейнер/ACR)
- [ ] Секрети через Azure-конфігурацію, не через .env у репо

## Етап 6 — React (базовий фронт)

- [ ] Мінімальний UI: каталог, кошик, логін/реєстрація
- [ ] Підключення до вже існуючого REST API
