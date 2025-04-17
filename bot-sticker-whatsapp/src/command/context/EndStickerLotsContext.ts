import { logger } from '@logs/Logger.js';
import { downloadSticker } from '@services/DownloadSticker.js';
import { sendStickersLots } from '@services/SendStickersLots.js';
import { proto, WASocket } from 'baileys';
import { Command } from '../Command.js';

export class EndStickerLotsContext implements Command {
  endStickerLotsContextLogger = logger.child({ class: 'EndStickerLotsContext' });

  constructor(
    private jid: string,
    private sock: WASocket,
    private msg: proto.IWebMessageInfo,
    private stickerSessions: { [jid: string]: Buffer[] }
  ) {}

  async exec(): Promise<void> {
    // Finaliza seção de criação de lotes
    try {
      if (this.stickerSessions[this.jid]) {
        const stickersBuffers = this.stickerSessions[this.jid];
        const finalStickerBuffer = await downloadSticker(this.msg, this.sock);

        this.stickerSessions[this.jid].push(finalStickerBuffer);
        await this.sock.sendMessage(this.jid, {
          text: `> Imagens adicionada ao lote: ${this.stickerSessions[this.jid].length}`,
        });

        delete this.stickerSessions[this.jid];

        if (stickersBuffers && stickersBuffers.length > 0) {
          await this.sock.sendMessage(this.jid, {
            text: '> Lote finalizado, enviando figurinhas...',
          });

          await sendStickersLots(this.sock, { jid: this.jid, bufferedImage: stickersBuffers });
        }
      } else {
        await this.sock.sendMessage(this.jid, {
          text: '> Nenhuma imagem foi adicionada ao lote, ou você não iniciou o lote!',
        });
      }
    } catch (error) {
      this.endStickerLotsContextLogger.error(error);
    }
  }
}
