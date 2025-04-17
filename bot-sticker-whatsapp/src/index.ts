import 'dotenv/config.js';
import express, { Request, Response } from 'express';
import { startBot } from './core/StartBot.js';
import { logger } from './logs/Logger.js';

const indexLogger = logger.child({ module: 'index' });

// Servidor express fake para poder funcionar na infra do RENDER
const app = express();
const port = process.env.PORT || 3000;

try {
  startBot();

  app.get('/', (req: Request, res: Response) => {
    res.json({ status: 'O bot está sendo executado!' });
  });
} catch (error) {
  indexLogger.error(error);

  app.get('/', (req: Request, res: Response) => {
    res.json({ status: 'O bot não está sendo executado!' });
  });
} finally {
  app.listen(port, () => {
    indexLogger.info(`🚀 App escutando no link http://localhost:${port}`);
  });
}
