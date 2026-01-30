# Demo project for R2DBC and coroutines

This repository is a small demo of the with reactive R2DBC drivers and coroutines in Spring Boot.

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


## Notes


## Links 
* Forked from the https://github.com/fyrkov/outbox-demo
