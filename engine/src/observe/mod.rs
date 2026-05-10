use rusqlite::{Connection, params};

pub struct EventLogger {
    conn: Connection,
}

impl EventLogger {
    pub fn new(db_path: &str) -> Result<Self, Box<dyn std::error::Error>> {
        let conn = Connection::open(db_path)?;
        conn.execute_batch("
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_type TEXT NOT NULL,
                event_data TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                session_id TEXT,
                project_path TEXT
            );
            CREATE INDEX IF NOT EXISTS idx_events_type ON events(event_type);
            CREATE INDEX IF NOT EXISTS idx_events_timestamp ON events(timestamp DESC);
        ")?;
        Ok(Self { conn })
    }

    pub fn log_event(&self, event_type: &str, data: &str) -> Result<i64, Box<dyn std::error::Error>> {
        let now = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)?
            .as_secs() as i64;
        self.conn.execute(
            "INSERT INTO events (event_type, event_data, timestamp) VALUES (?1, ?2, ?3)",
            params![event_type, data, now],
        )?;
        Ok(self.conn.last_insert_rowid())
    }

    pub fn log_llm_request(&self, model: &str, tokens_in: usize, tokens_out: usize, duration_ms: u64, success: bool) -> Result<i64, Box<dyn std::error::Error>> {
        self.log_event("llm_request", &serde_json::json!({
            "model": model,
            "tokens_in": tokens_in,
            "tokens_out": tokens_out,
            "duration_ms": duration_ms,
            "success": success,
        }).to_string())
    }

    pub fn log_file_edit(&self, path: &str, source: &str, lines_added: usize, lines_removed: usize) -> Result<i64, Box<dyn std::error::Error>> {
        self.log_event("file_edit", &serde_json::json!({
            "path": path,
            "source": source,
            "lines_added": lines_added,
            "lines_removed": lines_removed,
        }).to_string())
    }

    pub fn log_tool_call(&self, tool: &str, success: bool, duration_ms: u64) -> Result<i64, Box<dyn std::error::Error>> {
        self.log_event("tool_call", &serde_json::json!({
            "tool": tool,
            "success": success,
            "duration_ms": duration_ms,
        }).to_string())
    }

    pub fn log_error(&self, error_type: &str, message: &str) -> Result<i64, Box<dyn std::error::Error>> {
        self.log_event("error", &serde_json::json!({
            "type": error_type,
            "message": message,
        }).to_string())
    }

    pub fn get_stats(&self, since_timestamp: i64) -> Result<EventStats, Box<dyn std::error::Error>> {
        let mut stats = EventStats::default();

        stats.total_events = self.conn.query_row(
            "SELECT COUNT(*) FROM events WHERE timestamp >= ?1",
            params![since_timestamp],
            |row| row.get(0),
        )?;

        stats.llm_requests = self.conn.query_row(
            "SELECT COUNT(*) FROM events WHERE event_type = 'llm_request' AND timestamp >= ?1",
            params![since_timestamp],
            |row| row.get(0),
        )?;

        stats.total_tokens_in = self.conn.query_row(
            "SELECT COALESCE(SUM(CAST(json_extract(event_data, '$.tokens_in') AS INTEGER)), 0) FROM events WHERE event_type = 'llm_request' AND timestamp >= ?1",
            params![since_timestamp],
            |row| row.get::<_, i64>(0),
        )? as usize;

        stats.total_tokens_out = self.conn.query_row(
            "SELECT COALESCE(SUM(CAST(json_extract(event_data, '$.tokens_out') AS INTEGER)), 0) FROM events WHERE event_type = 'llm_request' AND timestamp >= ?1",
            params![since_timestamp],
            |row| row.get::<_, i64>(0),
        )? as usize;

        stats.file_edits = self.conn.query_row(
            "SELECT COUNT(*) FROM events WHERE event_type = 'file_edit' AND timestamp >= ?1",
            params![since_timestamp],
            |row| row.get(0),
        )?;

        stats.ai_edits = self.conn.query_row(
            "SELECT COUNT(*) FROM events WHERE event_type = 'file_edit' AND json_extract(event_data, '$.source') = 'ai' AND timestamp >= ?1",
            params![since_timestamp],
            |row| row.get(0),
        )?;

        stats.user_edits = self.conn.query_row(
            "SELECT COUNT(*) FROM events WHERE event_type = 'file_edit' AND json_extract(event_data, '$.source') = 'user' AND timestamp >= ?1",
            params![since_timestamp],
            |row| row.get(0),
        )?;

        stats.errors = self.conn.query_row(
            "SELECT COUNT(*) FROM events WHERE event_type = 'error' AND timestamp >= ?1",
            params![since_timestamp],
            |row| row.get(0),
        )?;

        Ok(stats)
    }
}

#[derive(Debug, Default)]
pub struct EventStats {
    pub total_events: i64,
    pub llm_requests: i64,
    pub total_tokens_in: usize,
    pub total_tokens_out: usize,
    pub file_edits: i64,
    pub ai_edits: i64,
    pub user_edits: i64,
    pub errors: i64,
}
