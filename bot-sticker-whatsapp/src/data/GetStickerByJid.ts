import { StickerRow } from '../types/StickerRow.js';
import { startSqlite } from '../core/StartSqlite.js';

export async function getStickerByJid(jid_created_sticker: string): Promise<StickerRow> {
  const db = await startSqlite();

  return new Promise((resolve, reject) => {
    db.serialize(() => {
      db.run('BEGIN TRANSACTION;');

      // prettier-ignore
      db.get(
        `SELECT * FROM stickers WHERE jid_created_sticker = ? ORDER BY id DESC LIMIT 1`,[jid_created_sticker],
        (error, row: StickerRow) => {
          if (error) return reject(error);
          resolve({
            id: row.id,
            jid_created_sticker: row.jid_created_sticker,
            base64_sticker: row.base64_sticker,
          });
        }
      );

      db.run('COMMIT;');
    });
  });
}
