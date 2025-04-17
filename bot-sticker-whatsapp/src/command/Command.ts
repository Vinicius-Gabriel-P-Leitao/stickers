export interface Command {
  exec(): Promise<void>;
}
