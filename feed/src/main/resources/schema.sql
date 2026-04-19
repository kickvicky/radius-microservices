-- Enable PostGIS extension (required for geography type)
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE IF NOT EXISTS posts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    tag VARCHAR(50),
    location geography(Point, 4326) NOT NULL,
    upvote_count INT DEFAULT 0,
    downvote_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Spatial index for fast ST_DWithin radius queries
CREATE INDEX IF NOT EXISTS idx_posts_location ON posts USING GIST(location);