import { Boom } from '@hapi/boom';
import {
  Browsers,
  ConnectionState,
  delay,
  DisconnectReason,
  makeWASocket,
  useMultiFileAuthState,
} from 'baileys';
import 'dotenv/config.js';
import { rm } from 'fs/promises';
import { startStickerListener } from '../command/task/StartStickerListener.js';
import { logger } from '../logs/Logger.js';

export async function startBot() {
  const startBotLogger = logger.child({ module: 'startBot' });

  const { state, saveCreds } = await useMultiFileAuthState('./resource/auth');

  const sock = makeWASocket({
    auth: state,
    browser: Browsers.baileys("Desktop"),
    printQRInTerminal: true,
  });

  sock.ev.on('creds.update', saveCreds);

  sock.ev.on('connection.update', async (update: Partial<ConnectionState>) => {
    const { connection, lastDisconnect } = update;

    if (connection === 'close') {
      try {
        if (
          connection === 'close' &&
          (lastDisconnect?.error as Boom)?.output?.statusCode !== DisconnectReason.loggedOut
        ) {
          await startBot();
        } else {
          await rm('./resource/auth', { recursive: true, force: true });
          startBotLogger.debug('Pasta auth deletada com sucesso.');

          startBotLogger.warn('⏳ Aguardando 15s até a nova conexão!');
          await delay(15000);

          await startBot();
        }
      } catch (error) {
        startBotLogger.error(`Erro ao realizar login: ${error}`);
      }
    }

    if (connection === 'open') {
      startBotLogger.info('✅ Bot conectado com sucesso!');
      startStickerListener(sock);
    }
  });
}
