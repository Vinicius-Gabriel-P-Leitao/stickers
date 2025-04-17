import { logger } from '@logs/Logger.js';
import { Command } from '../Command.js';
import { proto, WASocket } from 'baileys';
import { sendSticker } from '@services/SendSticker.js';
import { downloadSticker } from '@services/DownloadSticker.js';

export class StartStickerContext implements Command {
  private group_to_match = process.env.GROUP_TO_MATCH || undefined;
  startStickerContextLogger = logger.child({ class: 'StartStickerContext' });

  constructor(
    private jid: string,
    private sock: WASocket,
    private msg: proto.IWebMessageInfo
  ) {}

  async exec(): Promise<void> {
    // Mensagem para o grupo baseado no .env, caso não seja para o grupo verifica se a mensagem foi para você
    if (this.jid != undefined && this.jid.endsWith('@g.us') && this.group_to_match != undefined) {
      const infoSelectGroup = await this.sock.groupMetadata(this.jid).catch(() => null);

      if (!infoSelectGroup || !infoSelectGroup.subject.match(this.group_to_match)) {
        this.startStickerContextLogger.info(
          "Mensagem não enviada para o grupo 'Símios'. Ignorando..."
        );
        return;
      }

      const buffer = await downloadSticker(this.msg, this.sock);
      if (this.jid) {
        const infoSelectGroup = await this.sock.groupMetadata(this.jid).catch(() => null);

        if (infoSelectGroup?.subject?.match(this.group_to_match)) {
          sendSticker(this.sock, { jid: this.jid, bufferedImage: buffer });
        } else {
          this.startStickerContextLogger.error(
            "Erro ao enviar sticker para grupo: nome não bate com 'Símios'"
          );
        }
      } else {
        this.startStickerContextLogger.error('Erro: grupoSelecionado Jid está indefinido!');
      }
    } else if (!this.jid.endsWith('@g.us')) {
      try {
        const buffer = await downloadSticker(this.msg, this.sock);
        sendSticker(this.sock, { jid: this.jid, bufferedImage: buffer });
      } catch (error) {
        this.startStickerContextLogger.error(`Erro ao processar imagem: ${error}`);
      }
    }
  }
}
