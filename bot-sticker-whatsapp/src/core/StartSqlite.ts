import { existsSync, mkdirSync } from 'fs';
import path from 'path';
import sqlite3 from 'sqlite3';
import { fileURLToPath } from 'url';
import { logger } from '../logs/Logger.js';

export async function startSqlite(): Promise<sqlite3.Database> {
  const startSqliteLogger = logger.child({ module: 'saveSticker' });

  const __filename = fileURLToPath(import.meta.url);
  const __dirname = path.dirname(__filename);
  const dbPath = path.resolve(__dirname, '../../resource/db/sticker.db');

  if (!existsSync(path.dirname(dbPath))) {
    mkdirSync(path.dirname(dbPath), { recursive: true });
  }

  const db = new sqlite3.Database(dbPath, (err) => {
    if (err) {
      startSqliteLogger.error('Erro ao abrir o banco de dados:', err.message);
    }
  });

  db.run(
    `CREATE TABLE IF NOT EXISTS stickers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            jid_created_sticker TEXT NOT NULL,
            base64_sticker TEXT NOT NULL 
        );`,
    (error) => {
      if (error) {
        startSqliteLogger.error('Erro ao criar a tabela stickers:', error.message);
      } else {
        startSqliteLogger.info('Tabela "stickers" criada com sucesso ou já existente.');
      }
    }
  );

  return db;
}
