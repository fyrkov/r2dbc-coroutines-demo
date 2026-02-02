# Demo project for R2DBC and coroutines

This repository is a small demo of the with reactive R2DBC drivers and coroutines in Spring Boot.


## Notes

#### Jooq
Jooq supports R2DBC drivers since v3.15 https://blog.jooq.org/reactive-sql-with-jooq-3-15-and-r2dbc/

`spring-boot-starter-jooq` is not compatible, uses JDBC.

A new DSL Context object has to be provided like
```kotlin
implementation("org.jooq:jooq")
implementation("org.jooq:jooq-kotlin-coroutines")
```
```kotlin
@Bean
fun dslContext(cf: io.r2dbc.spi.ConnectionFactory): org.jooq.DSLContext =
    DSL.using(cf, SQLDialect.POSTGRES)
```

#### Flyway
Flyway has to be configured separately now to have its own separate jdbc connection from configs like:
```yaml
spring:
  flyway:
    url: jdbc:postgresql://...
```

#### Testcontainers
Testcontainers have to be configured to work with r2dbc:
```kotlin
testImplementation("org.testcontainers:testcontainers-r2dbc")
```

Now when the preparation is ready, let’s see how the code changes?
In jOOQ reactive mode (R2DBC), a jOOQ Query implements org.reactivestreams.Publisher<Record>.
Publisher comes from Reactive Streams, which is a tiny, standalone spec:


```kotlin
    fun insert(aggregateType: String, aggregateId: String, payload: String): Long {
        return dsl.insertInto(table("outbox"))
            ...
            .fetchSingle()
```
becomes a suspending function
```kotlin
    suspend fun insert(aggregateType: String, aggregateId: String, payload: String): Long {
        return dsl.insertInto(table)
            ...
            .awaitFirst()
```

A select query
```kotlin
    fun selectUnpublished(limit: Int): List<OutboxRecord> {
        return dsl.selectFrom(table)
            .where(field("published_at").isNull())
            .orderBy(field("id"))
            .limit(limit)
            .fetch { deser(it) }
    }
```
becomes either a suspending function
```kotlin
    suspend fun selectUnpublished(limit: Int): List<OutboxRecord> {
    val query = dsl.selectFrom(table("outbox"))
        .where(field("published_at").isNull())
        .orderBy(field("id"))
        .limit(limit)
    return Flux.from(query)
        .map { deser(it) }
        .collectList()
        .awaitSingle()
}
```
or a function that returns a Flow
```kotlin
    fun selectUnpublished2(limit: Int): Flow<OutboxRecord> {
    val query = dsl.selectFrom(table("outbox"))
        .where(field("published_at").isNull())
        .orderBy(field("id"))
        .limit(limit)
    return Flux.from(query)
        .asFlow()
        .map { deser(it) }
}
```

#### Transaction management
[Since Spring 5.3](https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-5.3-Release-Notes), the Spring `@Transactional` is aware of Kotlin Coroutines.
When a suspend function is marked `@Transactional`, Spring correctly manages the transaction context within the CoroutineContext.
NB: `@Transactional` in tests still is loking for JDBC Data source and does not work correctly. 

####  Scheduling
Starting with Spring Framework 6.1, `@Scheduled` officially supports Kotlin suspend functions.

## How to run locally

### Dependencies

* JDK >= 21
* Docker

For running locally, start DB:

```bash
docker compose up -d
```

Fixed port 15433 is used which must be available!

Start the app:

```
./gradlew bootRun
```

Access the app at http://localhost:8080/

## Data model

| Column           | Type           | Nullable | Description                                   |
|:-----------------|:---------------|:---------|:----------------------------------------------|
| `id`             | `bigint`       | No       | Primary key, auto-generated                   |
| `aggregate_type` | `varchar(255)` | No       | Type of the aggregate                         |
| `aggregate_id`   | `varchar(255)` | No       | ID of the aggregate                           |
| `payload`        | `jsonb`        | No       | Event data in JSON format                     |
| `created_at`     | `timestamptz`  | No       | Creation timestamp                            |
| `published_at`   | `timestamptz`  | Yes      | Publication timestamp (NULL if not published) |

The table is partitioned by `published_at` to efficiently manage published and unpublished records.

## What is not in the scope of this POC


## Links 
* Forked from the https://github.com/fyrkov/outbox-demo
* Introduction of R2DBC to Jooq: https://blog.jooq.org/reactive-sql-with-jooq-3-15-and-r2dbc/
