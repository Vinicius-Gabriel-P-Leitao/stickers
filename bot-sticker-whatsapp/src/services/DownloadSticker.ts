import { downloadMediaMessage, proto, WASocket } from 'baileys';
import { logger } from '../logs/Logger.js';

export const downloadSticker = async (msg: proto.IWebMessageInfo, sock: WASocket) => {
  return await downloadMediaMessage(
    msg,
    'buffer',
    {},
    {
      logger,
      reuploadRequest: sock.updateMediaMessage,
    }
  );
};
