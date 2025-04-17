import { logger } from '@logs/Logger.js';
import { WASocket } from 'baileys';
import { Command } from '../Command.js';

export class StartStickerLotsContext implements Command {
  private group_to_match = process.env.GROUP_TO_MATCH || undefined;
  contextStickerLotsLogger = logger.child({ class: 'StartStickerLotsContext' });

  constructor(
    private jid: string,
    private sock: WASocket,
    private stickerSessions: { [jid: string]: Buffer[] }
  ) {}

  public async exec(): Promise<void> {
    // Inicia uma sessão para o lote de stickers
    if (this.jid != undefined && this.jid.endsWith('@g.us') && this.group_to_match != undefined) {
      if (!this.stickerSessions[this.jid]) {
        const stickersBuffers = this.stickerSessions[this.jid];

        if (stickersBuffers) {
          this.contextStickerLotsLogger.info(
            `Sessão de lote de stickers iniciada para: ${this.jid}`
          );

          await this.sock.sendMessage(this.jid, {
            text: 'Sessão iniciada! Envie agora as imagens para o lote de figurinhas. Digite "end" quando terminar.',
          });
        } else {
          await this.sock.sendMessage(this.jid, {
            text: 'Você já está em uma sessão ativa. Envie "end" para encerrar.',
          });
        }
      }
    } else if (!this.jid.endsWith('@g.us')) {
      if (!this.stickerSessions[this.jid]) {
        this.stickerSessions[this.jid] = [];
        this.contextStickerLotsLogger.info(`Sessão de lote de stickers iniciada para: ${this.jid}`);

        await this.sock.sendMessage(this.jid, {
          text: '> Sessão iniciada! Envie agora as imagens para o lote de figurinhas. Digite na legenda da ultima imagem "end" quando terminar.',
        });
      } else {
        await this.sock.sendMessage(this.jid, {
          text: '> Você já está em uma sessão ativa. Envie "end" para encerrar.',
        });
      }
    }
  }
}
