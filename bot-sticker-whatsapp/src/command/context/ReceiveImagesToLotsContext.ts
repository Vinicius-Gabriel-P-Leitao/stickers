import { logger } from '@logs/Logger.js';
import { downloadSticker } from '@services/DownloadSticker.js';
import { proto, WASocket } from 'baileys';
import { Command } from '../Command.js';
import { sendStickersLots } from '@services/SendStickersLots.js';

export class ReceiveImagesToLotsContext implements Command {
  receiveImagesToLotsContextLogger = logger.child({ class: 'ReceiveImagesToLotsContext' });

  constructor(
    private jid: string,
    private sock: WASocket,
    private msg: proto.IWebMessageInfo,
    private stickerSessions: { [jid: string]: Buffer[] },
    private sessionTimers: {
      [jid: string]: { notifyTimer: NodeJS.Timeout; endTimer: NodeJS.Timeout };
    }
  ) {}

  async exec(): Promise<void> {
    // Caso uma imagem seja enviada durante a seção, garantido que seja durante devido a obrigatoriedade de um JDI no map
    if (this.stickerSessions[this.jid]) {
      try {
        const buffer = await downloadSticker(this.msg, this.sock);
        this.stickerSessions[this.jid].push(buffer);

        // prettier-ignore
        this.receiveImagesToLotsContextLogger.debug(`Evento de adicionar mais ao sticker lots executado pelo jid: ${this.jid}`);

        if (this.stickerSessions[this.jid]) {
          if (this.sessionTimers[this.jid]) {
            clearTimeout(this.sessionTimers[this.jid].notifyTimer);
            clearTimeout(this.sessionTimers[this.jid].endTimer);
          }

          this.sessionTimers[this.jid] = {
            notifyTimer: setTimeout(() => {
              if (this.stickerSessions[this.jid]) {
                this.sock.sendMessage(this.jid, {
                  text: `> Imagens já adicionadas ao lote: ${this.stickerSessions[this.jid].length}`,
                });
              }
            }, 15000),

            endTimer: setTimeout(async () => {
              if (this.stickerSessions[this.jid]) {
                this.sock.sendMessage(this.jid, {
                  text: `> Tempo expirado! Enviando imagens do lote...`,
                });

                // prettier-ignore
                await sendStickersLots(this.sock, { jid: this.jid, bufferedImage: this.stickerSessions[this.jid] });

                delete this.stickerSessions[this.jid];
                delete this.sessionTimers[this.jid];
              }
            }, 25000),
          };
        }
      } catch (error) {
        this.receiveImagesToLotsContextLogger.error(`Erro ao processar imagem do lote: ${error}`);
        await this.sock.sendMessage(this.jid, {
          text: 'Erro ao processar a imagem. Tente novamente.',
        });
      }
    }
  }
}
