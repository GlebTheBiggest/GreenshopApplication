# Development Plan — GreenshopApplication

Робочий чеклист. Відмічай `[x]` по завершенню кожного пункту. Детальний опис проєкту,
правила роботи та відомі проблеми — у `CLAUDE.md`.

## Етап 0 — Фундамент (ЗРОБЛЕНО)

- [x] README (огляд проєкту, стек, запуск, API)
- [x] Очищення pom.xml (видалено jbcrypt, Spring Security/Hibernate керує Boot, Stripe на стабільній версії)
- [x] docker-compose (PostgreSQL, пізніше доповнено Redis/Kafka/MailHog — див. Етапи 3-4)
- [x] Перевірка на закомічені секрети — не знайдено, `.env` в .gitignore

**Виправлено 27.10.2026**: цей розділ помилково позначав "Dockerfile для застосунку" як
зроблений. Dockerfile і `.dockerignore` справді були написані 24.10.2026, але на окремій
гілці `feature/docker-app-image`, яка ніколи не була змержена ні в `master`, ні в
`fix/authorization-idor` — у робочій гілці файлу не існувало. Переписано заново й
перевірено в межах Етапу 5 (нижче).

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

## Етап 3 — Redis (ЗРОБЛЕНО, гілка `fix/authorization-idor`)

- [x] Підняти Redis у docker-compose (`redis:7-alpine`, без пароля для локальної розробки)
- [x] `CacheConfig` (`@EnableCaching` + `RedisCacheConfiguration`): TTL 10 хв, JSON-серіалізація
      через `GenericJackson2JsonRedisSerializer`, null-значення не кешуються
- [x] Кешування каталогу товарів: `@Cacheable` на всіх read-методах `ProductService`/
      `CategoryService`, `@CacheEvict(allEntries = true)` на всіх write-методах
- [x] (знайдено під час підготовки до кешування) `Image.product` не мав `@JsonIgnore` —
      цикл `Product → images → Image → product → images → ...` впав би в
      `StackOverflowError` щойно товар з картинкою потрапив би в кеш. Виправлено за
      тим же принципом, що вже був у `Category.products`.
- [x] Перевірено живими запитами через `docker exec greenshop_redis redis-cli`: перший
      `GET /user/products/all` — похід у Postgres (~380мс) і запис `products::all` у
      Redis; повторний — з кешу (~20мс); `POST /management/products/add` чистить
      `products::all`, наступний `GET` перебудовує кеш вже з новим товаром.
- [x] Перевірено, що відсутність Redis не ламає існуючі тести/CI (ліниве підключення
      Lettuce) — жодних змін у `.github/workflows/ci.yml` не потрібно, поки тестів на
      саме кешування немає.

**Знайдено 26.10.2026 під час загальної перевірки всього проєкту (повний сценарій
користувача: реєстрація → кошик → замовлення), виправлено на тій же гілці:**

- [x] `GET /management/carts/{id}/cart` падав так само, як `/user/carts/my-cart` — той самий
      `CartRepository`-фікс закрив обидва місця одним рухом.
- [x] Справжня дірка в Етапі 3: Hibernate зберігає будь-яку `@OneToMany`-колекцію у своєму
      класі `PersistentSet`, навіть уже завантажену. `GenericJackson2JsonRedisSerializer`
      записував саме цей клас у JSON (`"images":["org.hibernate.collection.spi.
      PersistentSet",[]]`), і при читанні з кешу Jackson через рефлексію створював
      "пусту оболонку" цього класу без прив'язки до Hibernate-сесії — вона вважала
      себе незавантаженою й падала на першому ж читанні. Це був справжній баг Етапу 3,
      просто попередня "жива перевірка" ніколи не торкалась кеш-хіту з реальними даними
      всередині конвертації в DTO. Виправлено додаванням `jackson-datatype-hibernate6`
      (`Hibernate6Module` + `Feature.REPLACE_PERSISTENT_COLLECTIONS`) — тепер колекції
      кешуються як звичайний `java.util.HashSet`.
- [x] `OrderService.createOrderItems` перевіряв/списував залишок товару через
      **закешований** `ProductService.getProductById` — застарілі (до 10 хв) дані для
      перевірки залишку це вже окрема проблема коректності, а не лише виняток. Виправлено:
      перевірка залишку йде напряму через `ProductRepository`, повз кеш.

## Етап 4 — Kafka (ЗРОБЛЕНО, гілка `fix/authorization-idor`)

- [x] Kafka у docker-compose в **KRaft-режимі** (без Zookeeper — сучасний, рекомендований
      Apache спосіб для одного локального брокера) + **MailHog** (fake SMTP, веб-інтерфейс
      на `:8025`)
- [x] `OrderCreatedEvent` (orderId, userEmail, totalAmount, itemCount, orderDate) публікується
      через `KafkaTemplate` у `OrderService.placeOrder` одразу після збереження замовлення
- [x] Другий компонент — `OrderCreatedListener` (`@KafkaListener`, окрема consumer group
      `greenshop-order-notifications`) — реально надсилає лист-підтвердження через
      `EmailService`/`JavaMailSender` (`spring-boot-starter-mail` нарешті використано,
      раніше лежав невикористаним ще з першого коміту) на MailHog
- [x] Перевірено живими запитами: `POST /user/orders/create-with-payment` → в логах з'являється
      `Received order-created event for order N` → у MailHog (`GET /api/v2/messages`) реально
      з'являється лист із правильним темою/текстом/отримувачем/відправником
- [x] Тестовий конфіг (`src/test/resources/application.yml`) потребував `spring.mail.host/port` —
      без цього Boot не створює бін `JavaMailSender`, і весь `@SpringBootTest`-контекст падав
- [x] Відома, свідомо не вирішена межа: публікація в Kafka — не частина транзакції БД
      (dual-write problem). Якщо Kafka недосяжний у момент `placeOrder`, замовлення все одно
      створиться, але подія загубиться. Повне рішення (transactional outbox) — поза обсягом
      цього етапу.

## Етап 5 — Деплой в Azure (ЗРОБЛЕНО 27.10.2026)

Обраний підхід: контейнер (Docker-образ через Azure Container Registry → App Service for
Containers), не напряму `.jar` на App Service — проєкт і так уже активно працює з Docker
(Redis/Kafka/MailHog в docker-compose), тож контейнеризація власного застосунку логічно
продовжує ту саму навичку й дає досвід саме з образами/реєстрами, які реально
використовують компанії.

- [x] `Dockerfile` для застосунку (multi-stage: `eclipse-temurin:21-jdk-alpine` для збірки
      через `./mvnw clean package -DskipTests`, потім `eclipse-temurin:21-jre-alpine` лише
      з готовим `.jar` — щоб фінальний образ не тягнув компілятор) і `.dockerignore`
      (виключає `.env`, `.git`, `target`, `*.md`). Перевірено: `docker build` проходить,
      контейнер з цього образу реально стартує, підключається до Postgres/Redis/Kafka/
      MailHog з `docker-compose.yml` і успішно обробляє `POST /auth/register` (200 + JWT).
- [x] Azure CLI встановлено локально, `az login` виконано (підписка "Azure subscription 1")
- [x] Resource group `greenshop-rg` створено в регіоні `polandcentral`
- [x] Керована PostgreSQL: Azure Database for PostgreSQL — Flexible Server `greenshop-pg`
      (SKU `Standard_B1ms`, Burstable, 32 GB, PostgreSQL 15, регіон `polandcentral`), база
      `greenshop_db`, файрвол дозволяє "усі Azure-сервіси" (`0.0.0.0` sentinel) + окреме
      правило для домашньої IP для прямої перевірки через `psql`.
- [x] Azure Container Registry `greenshopacr26` (Basic). `az acr build` (хмарна збірка)
      виявилась заблокована Azure для нових підписок (захист від зловживань безкоштовною
      обчислювальною потужністю для "ACR Tasks") — обійдено: образ зібрано локально
      (`docker build`) і відправлено (`docker push`) напряму в реєстр.
- [x] App Service Plan `greenshop-plan` (Linux, B1) — квота `B1` в `polandcentral` і
      `westeurope` виявилась 0 для нової підписки (типове обмеження перших днів підписки);
      спрацював регіон `swedencentral`. Web App `greenshop-backend` створено з образу
      `greenshopacr26.azurecr.io/greenshop-app:v1`; автентифікація до ACR — через
      **Managed Identity** застосунку + роль `AcrPull`, без пароля реєстру. `WEBSITES_PORT=8080`
      виставлено явно (контейнер слухає 8080, Linux App Service за замовчуванням чекає 80).
- [x] Секрети через Azure App Service Configuration (Application Settings), не `.env`:
      `DB_URL/DB_USERNAME/DB_PASSWORD` → на `greenshop-pg`; `JWT_SECRET_KEY` —
      **новий, окремий від локального** секрет (кожне середовище має власний); `STRIPE_*`
      (тестові ключі), `SERVER_PORT`, `API_PREFIX` — ті самі значення, що й локально.
      `REDIS_HOST`/`KAFKA_BOOTSTRAP_SERVERS`/`MAIL_HOST` — поставлено заглушки (`localhost`),
      бо Redis/Kafka/SMTP у хмарі ще не підняті (окремі платні Azure-сервіси, поза обсягом
      цього етапу) — застосунок стартує нормально (Lettuce/Kafka-клієнт лаються в фоні, не
      валять старт, перевірено ще в Етапі 3-4), просто кешування завжди "промахується", а
      Kafka-подія/лист при замовленні не надсилаються (той самий dual-write-пробіл, тепер і
      в хмарі).
- [x] Перевірено живими запитами на `https://greenshop-backend.azurewebsites.net`:
      `POST /auth/register` (200 + JWT), `POST /auth/authenticate` (200 + JWT), дані реально
      лежать у `greenshop-pg` (перевірено прямим `psql`-запитом), Swagger UI та
      `/v3/api-docs` віддають 200.

**Нагадування про вартість**: `greenshop-pg` (Burstable B1ms) і `greenshop-plan` (Basic B1) —
обидва платні, що тікають постійно, доки запущені. Щоб не платити між демонстраціями:
`az postgres flexible-server stop --resource-group greenshop-rg --name greenshop-pg` (і
`...start` назад), або просто `az group delete --name greenshop-rg` щоб прибрати геть усе
одною командою й перестворити пізніше з нуля.

**Статус 27.10.2026: тимчасово знято.** `az group delete --name greenshop-rg` виконано —
`greenshop-rg` разом з усім вмістом (App Service, App Service Plan, Postgres, ACR з
образом) видалено, оплата зупинена. Деплой не "ЗРОБЛЕНО" в сенсі "живий" — це
задокументований спосіб, перевірений і готовий до повторення, коли дійдемо до
інтеграції з фронтом (Етап 6) і знову знадобиться хмара. Кроки нижче (та `az`-команди
вище) залишаються чинним рунбуком для повторного розгортання з нуля.

## Етап 6 — React (базовий фронт)

- [ ] Мінімальний UI: каталог, кошик, логін/реєстрація
- [ ] Підключення до вже існуючого REST API
