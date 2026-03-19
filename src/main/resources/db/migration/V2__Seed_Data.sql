-- V2__Seed_Data.sql

-- ============================================================
-- GENRES
-- ============================================================
INSERT INTO genres (name) VALUES
    ('Action'),
    ('Comedy'),
    ('Drama'),
    ('Horror'),
    ('Sci-Fi'),
    ('Thriller'),
    ('Romance'),
    ('Animation'),
    ('Documentary'),
    ('Fantasy');

-- ============================================================
-- ADMIN USER
-- Password: Admin@123  (BCrypt encoded)
-- ============================================================
INSERT INTO users (name, email, password, role) VALUES
    ('System Admin', 'admin@movieapp.com',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'ADMIN');

-- ============================================================
-- DEMO REGULAR USER
-- Password: User@123
-- ============================================================
INSERT INTO users (name, email, password, role) VALUES
    ('John Doe', 'john@example.com',
     '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AKnt5AV',
     'USER');

-- ============================================================
-- HALLS
-- ============================================================
INSERT INTO halls (name, total_rows, total_cols) VALUES
    ('Hall A', 10, 12),   -- 120 seats
    ('Hall B', 8, 10),    -- 80 seats
    ('Hall C', 6, 8);     -- 48 seats

-- ============================================================
-- SEATS for Hall A (10 rows x 12 cols)
-- ============================================================
INSERT INTO seats (hall_id, row_num, col_num, label)
SELECT
    (SELECT id FROM halls WHERE name = 'Hall A'),
    r,
    c,
    CHR(64 + r) || c
FROM generate_series(1, 10) AS r,
     generate_series(1, 12) AS c;

-- SEATS for Hall B (8 rows x 10 cols)
INSERT INTO seats (hall_id, row_num, col_num, label)
SELECT
    (SELECT id FROM halls WHERE name = 'Hall B'),
    r,
    c,
    CHR(64 + r) || c
FROM generate_series(1, 8) AS r,
     generate_series(1, 10) AS c;

-- SEATS for Hall C (6 rows x 8 cols)
INSERT INTO seats (hall_id, row_num, col_num, label)
SELECT
    (SELECT id FROM halls WHERE name = 'Hall C'),
    r,
    c,
    CHR(64 + r) || c
FROM generate_series(1, 6) AS r,
     generate_series(1, 8) AS c;

-- ============================================================
-- SAMPLE MOVIES
-- ============================================================
INSERT INTO movies (title, description, poster_url, duration_min, rating) VALUES
    ('Galactic Odyssey',
     'A breathtaking journey through the cosmos as a crew of astronauts discovers a mysterious alien civilization.',
     'https://placehold.co/400x600?text=Galactic+Odyssey',
     148, 'PG-13'),
    ('The Last Laugh',
     'A stand-up comedian navigates fame, heartbreak, and an unexpected rivalry in the cutthroat world of comedy.',
     'https://placehold.co/400x600?text=The+Last+Laugh',
     112, 'PG-13'),
    ('Shadows of Tomorrow',
     'A gripping thriller about a detective who uncovers a city-wide conspiracy with only 24 hours to stop it.',
     'https://placehold.co/400x600?text=Shadows+of+Tomorrow',
     126, 'R');

-- Movie genres
INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g
WHERE m.title = 'Galactic Odyssey' AND g.name IN ('Sci-Fi', 'Action');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g
WHERE m.title = 'The Last Laugh' AND g.name IN ('Comedy', 'Drama');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g
WHERE m.title = 'Shadows of Tomorrow' AND g.name IN ('Thriller', 'Drama');

-- ============================================================
-- SAMPLE SHOWTIMES (relative to now so they're always upcoming)
-- ============================================================
INSERT INTO showtimes (movie_id, hall_id, start_time, end_time, price)
SELECT
    (SELECT id FROM movies WHERE title = 'Galactic Odyssey'),
    (SELECT id FROM halls  WHERE name  = 'Hall A'),
    NOW() + INTERVAL '1 day',
    NOW() + INTERVAL '1 day' + INTERVAL '148 minutes',
    12.50;

INSERT INTO showtimes (movie_id, hall_id, start_time, end_time, price)
SELECT
    (SELECT id FROM movies WHERE title = 'Galactic Odyssey'),
    (SELECT id FROM halls  WHERE name  = 'Hall B'),
    NOW() + INTERVAL '2 days',
    NOW() + INTERVAL '2 days' + INTERVAL '148 minutes',
    12.50;

INSERT INTO showtimes (movie_id, hall_id, start_time, end_time, price)
SELECT
    (SELECT id FROM movies WHERE title = 'The Last Laugh'),
    (SELECT id FROM halls  WHERE name  = 'Hall C'),
    NOW() + INTERVAL '1 day' + INTERVAL '3 hours',
    NOW() + INTERVAL '1 day' + INTERVAL '3 hours' + INTERVAL '112 minutes',
    10.00;

INSERT INTO showtimes (movie_id, hall_id, start_time, end_time, price)
SELECT
    (SELECT id FROM movies WHERE title = 'Shadows of Tomorrow'),
    (SELECT id FROM halls  WHERE name  = 'Hall A'),
    NOW() + INTERVAL '3 days',
    NOW() + INTERVAL '3 days' + INTERVAL '126 minutes',
    11.00;
