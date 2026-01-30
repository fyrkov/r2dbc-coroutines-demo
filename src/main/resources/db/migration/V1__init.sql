create table outbox
(
    id             bigint generated always as identity not null,
    aggregate_type varchar(255)                        not null,
    aggregate_id   varchar(255)                        not null,
    payload        jsonb                               not null,
    created_at     timestamptz                         not null default now(),
    published_at   timestamptz
) partition by list (published_at);

create table outbox_unpublished partition of outbox for values in (null);
create table outbox_published partition of outbox default;

create index idx on outbox_unpublished (id);

-- TEST DATA

-- Published records
insert into outbox (aggregate_type, aggregate_id, payload, created_at, published_at)
select 'test_type',
       'test_id_' || i,
       '{}'::jsonb, now(),
       now()
from generate_series(1, 10000) as i;

-- Unpublished records
insert into outbox (aggregate_type, aggregate_id, payload, created_at, published_at)
select 'test_type',
       'test_id_' || i,
       '{}'::jsonb, now(),
       null
from generate_series(1, 1000) as i;

-- Important to refresh stats so that the PG planner sees it immediately
analyze outbox;