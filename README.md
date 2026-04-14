# Currency Exchange Service

Сервис для обмена валют с автоматическим обновлением курсов от Центрального Банка России.

## О проекте

REST API приложение для конвертации валют и получения актуальных курсов.
Курсы валют автоматически обновляются каждый час из официального источника ЦБ РФ.

### Функциональность

- Конвертация рублей в любую валюту по актуальному курсу
- Получение списка всех доступных валют
- Получение подробной информации о конкретной валюте
- Автоматическое обновление курсов валют каждый час
- Добавление новых валют вручную

### Технологии

- Java 17
- Spring Boot 2.7
- Spring Data JPA (Hibernate)
- PostgreSQL
- Liquibase (миграции БД)
- JAXB (парсинг XML от ЦБ РФ)
- Maven
- Docker
- Lombok
- Mapstruct

## Запуск проекта

### Требования

- JDK 17
- Docker
- Maven

### Шаг 1: Запуск PostgreSQL в Docker

```bash
docker run -p 5433:5432 --name postgres -e POSTGRES_PASSWORD=postgres -d postgres
```

**Важно:** В проекте используется порт 5433 для PostgreSQL, чтобы избежать конфликта с локально установленной БД.

Пользователь для подключения к контейнеру `postgres`.

### Шаг 2: Запуск приложения

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080`

## API Endpoints

### Получить все валюты

```http
GET /api/currency/
```

Ответ:


```json
{
  "currencies": [
    {"name": "Доллар США", "value": 93.5224},
    {"name": "Евро", "value": 99.5534}
  ]
}
```

### Получить валюту по ID

```http
GET /api/currency/{id}
```

Ответ:

```json
{
  "id": 1333,
  "name": "Доллар США",
  "nominal": 1,
  "value": 93.5224,
  "isoNumCode": 840,
  "isoCharCode": "USD"
}
```

### Конвертировать рубли в валюту

```http
GET /api/currency/convert?value={сумма}&numCode={код валюты}
```

Пример: `GET /api/currency/convert?value=100&numCode=840`

Ответ: `9352.24`

### Добавить новую валюту

```http
POST /api/currency/create
Content-Type: application/json

{
  "name": "Японская йена",
  "nominal": 1,
  "value": 0.62,
  "isoNumCode": 392,
  "isoCharCode": "JPY"
  }
```

## Особенности реализации

### Интеграция с ЦБ РФ

*   Автоматическая загрузка курсов 54 валют каждый час

*   Парсинг XML через JAXB

*   URL для загрузки настраивается в `application.yml`

*   Полное логирование операций загрузки


### Управление базой данных

*   Миграции через Liquibase

*   Автоматическое создание таблиц при старте

*   Стратегия обновления: обновление существующих записей, создание новых


### Планировщик задач

*   Обновление курсов каждый час с помощью `@Scheduled`

*   Первоначальная загрузка при старте приложения через `@PostConstruct`


## Конфигурация

`application.yml`:

```yaml
server:
  port: 8080
  
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
    
cbr:
  url: http://www.cbr.ru/scripts/XML_daily.asp
```

## 👨‍💻 Автор

Фёдор Вянцкус

*   GitHub: [@vyanckus](https://github.com/vyanckus)

*   Email: vyanckus@mail.ru