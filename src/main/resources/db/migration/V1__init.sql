CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE ticket (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        category_id BIGINT NOT NULL REFERENCES category(id),
                        priority VARCHAR(20) NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        due_at TIMESTAMP WITH TIME ZONE,
                        closed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE status_history (
                                id BIGSERIAL PRIMARY KEY,
                                ticket_id BIGINT NOT NULL REFERENCES ticket(id),
                                from_status VARCHAR(20),
                                to_status VARCHAR(20) NOT NULL,
                                changed_at TIMESTAMP WITH TIME ZONE NOT NULL
);