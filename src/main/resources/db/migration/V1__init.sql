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