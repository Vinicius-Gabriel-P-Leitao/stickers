import { WASocket } from 'baileys';
import sharp from 'sharp';
import { Sticker, StickerTypes } from 'wa-sticker-formatter';
import { StickerRow } from '../types/StickerRow.js';
import { saveSticker } from '../data/SaveSticker.js';
import { logger } from '../logs/Logger.js';

type sendStickerObject = {
  jid: string;
  imagePath?: string;
  bufferedImage?: Buffer[];
};

export async function sendStickersLots(
  sock: WASocket,
  { jid, imagePath, bufferedImage }: sendStickerObject
) {
  const sendStickersLotsLogger = logger.child({ module: 'sendSticker' });

  // Cria um array buffer para os stickers
  const stickerBuffers: Sticker[] = [];

  if (!imagePath && !bufferedImage) {
    throw new Error("É necessário fornecer 'imagePath' ou 'buffer'");
  }

  if (bufferedImage !== undefined) {
    for (const imageSource of bufferedImage) {
      const buffer = await sharp(imageSource).resize(512, 512).webp().toBuffer();

      const sticker = new Sticker(buffer, {
        type: StickerTypes.FULL,
        pack: 'bot-sticker-vinícius',
        author: 'Vinícius Gabriel',
      });
      stickerBuffers.push(sticker);

      // Salva sticker no sqlite3
      const base64 = buffer.toString('base64');
      const dataUrl: string = `data:image/webp;base64,${base64}`.trim();

      let uniqueSticker: StickerRow;
      try {
        uniqueSticker = await saveSticker(jid, dataUrl);
        if (!uniqueSticker) {
          sendStickersLotsLogger.warn(`Sticker não encontrado no banco para o jid ${jid}`);
          return;
        }

        sendStickersLotsLogger.info(
          '\n' +
            JSON.stringify(
              {
                sticker: {
                  id: uniqueSticker.id,
                  jid_created_sticker: uniqueSticker.jid_created_sticker,
                  base64_sticker: uniqueSticker.base64_sticker.substring(0, 50) + '...',
                },
              },
              null,
              2
            )
        );
      } catch (err) {
        sendStickersLotsLogger.error(`Erro ao buscar sticker no banco: ${err}`);
        return;
      }
    }
  }

  for (const sticker of stickerBuffers) {
    await sock.sendMessage(jid, await sticker.toMessage());
  }
}
// const stickers = await getStickers();
// console.log('Todos os stickers:', stickers);
