CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE sights (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    location GEOMETRY(Point, 4326),
    average_rating DOUBLE PRECISION DEFAULT 0.0,
    reviews_count INTEGER DEFAULT 0
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    sight_id UUID REFERENCES sights(id),
    user_id UUID REFERENCES users(id),
    rating INTEGER,
    text TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE
);