# Development Plan — GreenshopApplication

Робочий чеклист. Відмічай `[x]` по завершенню кожного пункту. Детальний опис проєкту,
правила роботи та відомі проблеми — у `CLAUDE.md`.

## Етап 0 — Фундамент (ЗРОБЛЕНО)

- [x] README (огляд проєкту, стек, запуск, API)
- [x] Очищення pom.xml (видалено jbcrypt, Spring Security/Hibernate керує Boot, Stripe на стабільній версії)
- [x] Dockerfile для застосунку
- [x] docker-compose (застосунок + PostgreSQL)
- [x] Перевірка на закомічені секрети — не знайдено, `.env` в .gitignore

## Етап 1 — Виправлення авторизації (ЗРОБЛЕНО, гілка `fix/authorization-idor`)

Знайдено аудитом 23.10.2026, виправлено 26.10.2026 і перевірено живими запитами
(докладний звіт — у розмові з Claude).

- [x] Виправити IDOR: `UserCartController`, `UserCartItemController`, `UserOrderController`
      тепер беруть `userId` з автентифікованого principal (JWT), а не з path/query/body;
      для ендпоінтів, де потрібен `cartId` (total-price, remove/update item), додано
      явну перевірку власності кошика
- [x] Виправити порядок правил у `SecurityConfig.securityFilterChain` — матчери по
      HTTP-методу тепер йдуть ПЕРЕД загальним `hasAnyRole` матчером
- [x] Додати глобальний `GlobalExceptionHandler` (`@RestControllerAdvice`) замість
      розрізнених try/catch
- [x] Побічний ефект глобального handler-а: `AuthService.authenticate()` з неправильним
      паролем тепер повертає 401, а не необроблений 500 (live-tested)
- [x] (бонус, знайдено під час фіксу) Піднято `springdoc-openapi` 2.1.0 → 2.8.17 —
      стара версія ламала `/v3/api-docs` щойно з'явився власний `@ControllerAdvice`

- [x] `GET /user/carts/my-cart` та `GET /management/carts/{id}/cart` кидали
      `LazyInitializationException` на `cart.getItems()` (open-in-view=false,
      жоден з методів не був `@Transactional`). Виправлено через `JOIN FETCH`
      у `CartRepository.findByUserId`/`findByIdWithItems`.
- [x] (знайдено під час реордеру правил) `SecurityConfig` порівнював
      `Permission.XXX.name()` ("ADMIN_READ") замість `.getPermission()`
      ("admin:read") — гранулярні перевірки НІКОЛИ не могли спрацювати, навіть
      після виправлення порядку правил. Виправлено на `.getPermission()`.

## Етап 2 — Тестування та CI (ЗРОБЛЕНО, гілка `fix/authorization-idor`)

- [x] Testcontainers (реальний PostgreSQL у контейнері) — `AbstractIntegrationTest`,
      singleton-container паттерн (щоб контейнер не гасився між тестовими класами)
- [x] Unit-тести на сервісний шар (JUnit 5 + Mockito): `CartServiceTest`, `AuthServiceTest`
- [x] Regression-тести на фікси з Етапу 1: `AuthenticationIntegrationTest`,
      `CartOwnershipIntegrationTest`, `ManagementAuthorizationIntegrationTest`
- [x] GitHub Actions workflow (`.github/workflows/ci.yml`): `mvn verify` на push/PR до master
- [x] (знайдено під час написання тестів, виправлено) `/auth/register` повертав 500
      замість 409 на дублікат email — `AuthService.register()` не перевіряв
      `existsByEmail` перед вставкою
- [x] (знайдено під час написання тестів, виправлено) JWT не мав `jti` — два токени
      для одного юзера в межах однієї секунди (register одразу після authenticate)
      були побайтово ідентичні й падали на UNIQUE-constraint колонки `token` → 500

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
