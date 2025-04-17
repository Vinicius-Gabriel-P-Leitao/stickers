import { WASocket } from 'baileys';
import fs from 'fs';
import sharp from 'sharp';
import { Sticker, StickerTypes } from 'wa-sticker-formatter';
import { StickerRow } from '../types/StickerRow.js';
import { saveSticker } from '../data/SaveSticker.js';
import { logger } from '../logs/Logger.js';

type sendStickerObject = {
  jid: string;
  imagePath?: string;
  bufferedImage?: Buffer;
};

const isAnimatedWebp = (buffer: Buffer) => {
  return buffer.includes(Buffer.from('VP8X')) && (buffer[20] & 0x02) === 0x02;
};

// Converte a figurinha e envia
export async function sendSticker(
  sock: WASocket,
  { jid, imagePath, bufferedImage }: sendStickerObject
) {
  const sendStickerLogger = logger.child({ module: 'sendSticker' });

  if (!imagePath && !bufferedImage) {
    throw new Error("É necessário fornecer 'imagePath' ou 'buffer'");
  }

  const buffer = bufferedImage ?? fs.readFileSync(imagePath!);
  let stickerBuffer: Buffer;

  if (isAnimatedWebp(buffer)) {
    stickerBuffer = buffer;
  } else {
    stickerBuffer = await sharp(buffer).resize(512, 512).webp().toBuffer();
  }

  // Salva sticker no sqlite3
  const base64 = stickerBuffer.toString('base64');
  const dataUrl: string = `data:image/webp;base64,${base64}`.trim();

  let uniqueSticker: StickerRow;
  try {
    uniqueSticker = await saveSticker(jid, dataUrl);
    if (!uniqueSticker) {
      sendStickerLogger.warn(`Sticker não encontrado no banco para o jid ${jid}`);
      return;
    }

    sendStickerLogger.info(
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
    sendStickerLogger.error(`Erro ao buscar sticker no banco: ${err}`);
    return;
  }

  const sticker = new Sticker(stickerBuffer, {
    type: StickerTypes.FULL,
    pack: 'bot-sticker-vinícius',
    author: 'Vinícius Gabriel',
  });

  await sock.sendMessage(jid, await sticker.toMessage());
  await sock.sendMessage(jid, { text: 'Figurinha gerada!' });
  sendStickerLogger.info(`Figurinha enviada para ${jid}`);
}
// const stickers = await getStickers();
// console.log('Todos os stickers:', stickers);
