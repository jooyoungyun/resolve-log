CREATE TABLE app_user (
    id uuid PRIMARY KEY,
    email varchar(254) NOT NULL UNIQUE,
    display_name varchar(30) NOT NULL,
    password_hash varchar(100) NOT NULL,
    created_at timestamp with time zone NOT NULL
);
CREATE TABLE goal (
    id uuid PRIMARY KEY,
    owner_id uuid NOT NULL REFERENCES app_user(id),
    title varchar(100) NOT NULL,
    reason varchar(1000) NOT NULL,
    category varchar(20) NOT NULL,
    start_date date NOT NULL,
    end_date date,
    archived boolean NOT NULL DEFAULT false,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CHECK (end_date IS NULL OR end_date >= start_date),
    CHECK (category IN ('HEALTH','STUDY','WORK','LIFE','FINANCE','OTHER'))
);
CREATE TABLE goal_weekday (
    goal_id uuid NOT NULL REFERENCES goal(id) ON DELETE CASCADE,
    weekday integer NOT NULL CHECK (weekday BETWEEN 1 AND 7),
    PRIMARY KEY (goal_id, weekday)
);
CREATE TABLE journal_entry (
    id uuid PRIMARY KEY,
    goal_id uuid NOT NULL REFERENCES goal(id) ON DELETE CASCADE,
    entry_date date NOT NULL,
    status varchar(20) NOT NULL CHECK (status IN ('DONE','PARTIAL','SKIPPED')),
    note varchar(4000) NOT NULL,
    minutes integer NOT NULL CHECK (minutes BETWEEN 0 AND 1440),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT ux_entry_goal_date UNIQUE (goal_id, entry_date)
);
CREATE INDEX ix_goal_owner_created ON goal (owner_id, created_at DESC);
CREATE INDEX ix_entry_date_goal ON journal_entry (entry_date, goal_id);

