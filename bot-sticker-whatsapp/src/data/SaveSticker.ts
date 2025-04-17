import { StickerRow } from '../types/StickerRow.js';
import { startSqlite } from '../core/StartSqlite.js';
import { logger } from '../logs/Logger.js';

export async function saveSticker(
  jid_created_sticker: string,
  base64_sticker: string
): Promise<StickerRow> {
  const saveStickerLogger = logger.child({ module: 'saveSticker' });

  const db = await startSqlite();
  return new Promise((resolve, reject) => {
    db.serialize(() => {
      db.run('BEGIN TRANSACTION;');

      db.run(
        `INSERT INTO stickers (jid_created_sticker, base64_sticker) VALUES (?, ?)`,
        [jid_created_sticker, base64_sticker],
        function (error) {
          if (error) return reject(error);
          const id = this.lastID;
          saveStickerLogger.info(`Arquivo salvo com ID ${id}`);

          // Callback para poder usar como log
          db.get(
            `SELECT * FROM stickers WHERE id = ?`,
            [id],
            (err, row: StickerRow | undefined) => {
              if (err) return reject(err);
              if (!row) return reject(new Error('Erro ao recuperar o sticker salvo'));
              resolve(row);
            }
          );
        }
      );

      db.run('COMMIT;');
    });
  });
}
