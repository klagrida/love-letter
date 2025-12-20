package com.loveletter.network

object SupabaseConfig {
    // TODO: Replace with your Supabase project credentials
    // Get these from https://supabase.com/dashboard/project/YOUR_PROJECT/settings/api
    const val SUPABASE_URL = "https://YOUR_PROJECT.supabase.co"
    const val SUPABASE_ANON_KEY = "YOUR_ANON_KEY"

    // Table names
    const val TABLE_GAMES = "games"
    const val TABLE_PLAYERS = "players"
    const val TABLE_GAME_ACTIONS = "game_actions"
    const val TABLE_ROOMS = "rooms"

    // Realtime channels
    const val CHANNEL_GAME_PREFIX = "game:"
    const val CHANNEL_LOBBY = "lobby"
}

/*
SQL to create tables in Supabase:

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Rooms table for lobby
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    host_id TEXT NOT NULL,
    host_name TEXT NOT NULL,
    max_players INT DEFAULT 4,
    current_players INT DEFAULT 1,
    is_private BOOLEAN DEFAULT FALSE,
    password TEXT,
    status TEXT DEFAULT 'WAITING',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Games table
CREATE TABLE games (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    room_id UUID REFERENCES rooms(id) ON DELETE CASCADE,
    state JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Players table
CREATE TABLE players (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id TEXT NOT NULL,
    game_id UUID REFERENCES games(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    is_host BOOLEAN DEFAULT FALSE,
    is_connected BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Game actions table for history/replay
CREATE TABLE game_actions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    game_id UUID REFERENCES games(id) ON DELETE CASCADE,
    player_id TEXT NOT NULL,
    action_type TEXT NOT NULL,
    action_data JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable realtime for all tables
ALTER PUBLICATION supabase_realtime ADD TABLE rooms;
ALTER PUBLICATION supabase_realtime ADD TABLE games;
ALTER PUBLICATION supabase_realtime ADD TABLE players;
ALTER PUBLICATION supabase_realtime ADD TABLE game_actions;

-- RLS Policies
ALTER TABLE rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE games ENABLE ROW LEVEL SECURITY;
ALTER TABLE players ENABLE ROW LEVEL SECURITY;
ALTER TABLE game_actions ENABLE ROW LEVEL SECURITY;

-- Allow all operations for authenticated users (adjust as needed)
CREATE POLICY "Allow all for rooms" ON rooms FOR ALL USING (true);
CREATE POLICY "Allow all for games" ON games FOR ALL USING (true);
CREATE POLICY "Allow all for players" ON players FOR ALL USING (true);
CREATE POLICY "Allow all for game_actions" ON game_actions FOR ALL USING (true);

-- Function to update timestamp
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for games table
CREATE TRIGGER games_updated_at
    BEFORE UPDATE ON games
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();
*/
