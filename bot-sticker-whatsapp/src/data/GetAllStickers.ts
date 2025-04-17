import { StickerRow } from '../types/StickerRow.js';
import { startSqlite } from '../core/StartSqlite.js';

export async function getAllStickers(): Promise<StickerRow> {
  const db = await startSqlite();

  return new Promise((resolve, reject) => {
    db.serialize(() => {
      db.run('BEGIN TRANSACTION;');

      db.all(`SELECT * FROM stickers`, [], (error: Error, rows: StickerRow) => {
        if (error) return reject(error);
        resolve({
          id: rows.id,
          jid_created_sticker: rows.jid_created_sticker,
          base64_sticker: rows.base64_sticker,
        });
      });

      db.run('COMMIT;');
    });
  });
}
