import { env } from './.env';

export const environment = {
  production: false,
  version: env['npm_package_version'] + '-dev',
  defaultLanguage: 'fr-FR',
  supportedLanguages: ['de-DE', 'en-US', 'es-ES', 'fr-FR', 'it-IT'],
  apiUrl: 'http://localhost:8080',
};
