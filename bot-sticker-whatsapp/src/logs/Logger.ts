// eslint-disable-next-line import/extensions
import { ILogger } from 'baileys/lib/Utils/logger';
import chalk from 'chalk';

type LogLevel = 'trace' | 'debug' | 'info' | 'warn' | 'error';

const levelColors = {
  TRACE: chalk.gray,
  DEBUG: chalk.cyan,
  INFO: chalk.blue,
  WARN: chalk.yellow,
  ERROR: chalk.red,
};

class CustomLogger implements ILogger {
  level: LogLevel = 'info';
  private context: Record<string, unknown>;

  constructor(context: Record<string, unknown> = {}) {
    this.context = context;
  }

  child(ctx: Record<string, unknown>): ILogger {
    return new CustomLogger({ ...this.context, ...ctx });
  }

  trace(obj: unknown, msg?: string) {
    console.trace(this.format('TRACE', msg), obj);
  }

  debug(msg: string) {
    console.debug(this.format('DEBUG', msg));
  }

  info(msg: string) {
    console.info(this.format('INFO', msg));
  }

  warn(msg: string) {
    console.warn(this.format('WARN', msg));
  }

  error(msg: string) {
    console.error(this.format('ERROR', msg));
  }

  private format(level: keyof typeof levelColors, msg?: string): string {
    const timestamp = chalk.gray(new Date().toISOString());
    const levelLabel = levelColors[level](`[${level}]`);

    const ctx = Object.entries(this.context)
      .map(([key, value]) => `${chalk.bold.red(key)}: ${chalk.bold.magenta(value)}`)
      .join(' ');

    return `${timestamp} ${levelLabel} ${ctx ? `[${ctx}]` : ''} ${msg ?? ''}`;
  }
}

export const logger = new CustomLogger();
