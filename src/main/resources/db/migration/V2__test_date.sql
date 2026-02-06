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