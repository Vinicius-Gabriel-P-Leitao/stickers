import { logger } from '@logs/Logger.js';
import { MessageUpsertType, WAMessage, WASocket } from 'baileys';
import 'dotenv/config.js';
import { EndStickerLotsContext } from '../context/EndStickerLotsContext.js';
import { ReceiveImagesToLotsContext } from '../context/ReceiveImagesToLotsContext.js';
import { StartStickerContext } from '../context/StartStickerContext.js';
import { StartStickerLotsContext } from '../context/StartStickerLotsContext.js';

export function startStickerListener(sock: WASocket) {
  const startMessageListenerLogger = logger.child({ module: 'startStickerListener' });

  const stickerSessions: { [jid: string]: Buffer[] } = {};
  // prettier-ignore
  const sessionTimers: { [jid: string]: { notifyTimer: NodeJS.Timeout; endTimer: NodeJS.Timeout; }; } = {};

  sock.ev.on(
    'messages.upsert',
    async ({ messages, type }: { messages: WAMessage[]; type: MessageUpsertType }) => {
      if (type !== 'notify') return;

      const msg = messages[0];
      if (!msg.message || !msg.key.remoteJid) return;
      const jid = msg.key.remoteJid;

      if (msg.message?.imageMessage) {
        const caption = msg.message.imageMessage.caption?.toLowerCase();

        if (caption && caption.trim() === 'sticker') {
          startMessageListenerLogger.debug(`Evento de sticker executado pelo jid: ${jid}`);
          const sticker = new StartStickerContext(jid, sock, msg);
          await sticker.exec();
        }

        if (caption && caption.trim() === 'sticker lots') {
          const stickerLots = new StartStickerLotsContext(jid, sock, stickerSessions);
          await stickerLots.exec();
        }

        if (caption?.toLowerCase() === 'end') {
          startMessageListenerLogger.debug(`Evento de end executado pelo jid: ${jid}`);
          const endStickerLotsContext = new EndStickerLotsContext(jid, sock, msg, stickerSessions);
          await endStickerLotsContext.exec();
        }

        if (msg.message?.imageMessage) {
          if (stickerSessions[jid]) {
            // prettier-ignore
            const receiveImagesToLotsContext = new ReceiveImagesToLotsContext(jid, sock, msg, stickerSessions, sessionTimers);
            await receiveImagesToLotsContext.exec();
          }
        }
      }
    }
  );
}
