import { HttpErrorResponse } from '@angular/common/http';

export function formatHttpError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Connexion impossible : démarrez Spring Boot sur le port 8081, puis relancez npm start.';
    }
    if (err.status === 404) {
      return 'Endpoint IA introuvable (404). Arrêtez le backend et relancez-le (mvnw spring-boot:run) pour charger AiFeedbackController.';
    }
    const body = err.error;
    if (typeof body === 'string' && body.length) return body;
    if (body && typeof body === 'object' && 'message' in body) {
      return String((body as { message: string }).message);
    }
    return `Erreur serveur (${err.status}).`;
  }
  if (err instanceof Error) return err.message;
  return 'Erreur inattendue lors de l’appel API.';
}
