-- V1__Initial_Schema.sql

-- ============================================================
-- EXTENSIONS (must come first)
-- ============================================================
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ============================================================
-- USERS & ROLES
-- ============================================================
CREATE TABLE users (
                       id          BIGSERIAL PRIMARY KEY,
                       name        VARCHAR(100) NOT NULL,
                       email       VARCHAR(150) NOT NULL UNIQUE,
                       password    VARCHAR(255) NOT NULL,
                       role        VARCHAR(20)  NOT NULL DEFAULT 'USER',
                       created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- GENRES
-- ============================================================
CREATE TABLE genres (
                        id   BIGSERIAL PRIMARY KEY,
                        name VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================================
-- MOVIES
-- ============================================================
CREATE TABLE movies (
                        id           BIGSERIAL PRIMARY KEY,
                        title        VARCHAR(200) NOT NULL,
                        description  TEXT,
                        poster_url   VARCHAR(500),
                        duration_min INT NOT NULL,
                        rating       VARCHAR(10),
                        created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE movie_genres (
                              movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
                              genre_id BIGINT NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
                              PRIMARY KEY (movie_id, genre_id)
);

-- ============================================================
-- HALLS (screens / auditoriums)
-- ============================================================
CREATE TABLE halls (
                       id         BIGSERIAL PRIMARY KEY,
                       name       VARCHAR(100) NOT NULL,
                       total_rows INT NOT NULL,
                       total_cols INT NOT NULL
);

-- ============================================================
-- SEATS
-- ============================================================
CREATE TABLE seats (
                       id      BIGSERIAL PRIMARY KEY,
                       hall_id BIGINT NOT NULL REFERENCES halls(id) ON DELETE CASCADE,
                       row_num INT    NOT NULL,
                       col_num INT    NOT NULL,
                       label   VARCHAR(10) NOT NULL,
                       UNIQUE (hall_id, row_num, col_num)
);

-- ============================================================
-- SHOWTIMES
-- btree_gist is required for the EXCLUDE constraint mixing
-- bigint (hall_id) and tsrange columns.
-- ============================================================
CREATE TABLE showtimes (
                           id         BIGSERIAL PRIMARY KEY,
                           movie_id   BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
                           hall_id    BIGINT NOT NULL REFERENCES halls(id),
                           start_time TIMESTAMP NOT NULL,
                           end_time   TIMESTAMP NOT NULL,
                           price      NUMERIC(10,2) NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                           CONSTRAINT no_overlapping_showtimes
                               EXCLUDE USING gist (
            hall_id WITH =,
            tsrange(start_time, end_time) WITH &&
        )
);

CREATE INDEX idx_showtimes_start_time ON showtimes(start_time);
CREATE INDEX idx_showtimes_movie_id   ON showtimes(movie_id);

-- ============================================================
-- RESERVATIONS
-- ============================================================
CREATE TABLE reservations (
                              id          BIGSERIAL PRIMARY KEY,
                              user_id     BIGINT NOT NULL REFERENCES users(id),
                              showtime_id BIGINT NOT NULL REFERENCES showtimes(id),
                              status      VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
                              total_price NUMERIC(10,2) NOT NULL,
                              created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reservations_user_id     ON reservations(user_id);
CREATE INDEX idx_reservations_showtime_id ON reservations(showtime_id);

-- ============================================================
-- RESERVATION SEATS
-- ============================================================
CREATE TABLE reservation_seats (
                                   id             BIGSERIAL PRIMARY KEY,
                                   reservation_id BIGINT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
                                   seat_id        BIGINT NOT NULL REFERENCES seats(id),
                                   showtime_id    BIGINT NOT NULL REFERENCES showtimes(id),

                                   UNIQUE (seat_id, showtime_id)
);

CREATE INDEX idx_reservation_seats_showtime ON reservation_seats(showtime_id);