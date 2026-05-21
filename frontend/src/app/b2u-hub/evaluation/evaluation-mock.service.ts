import { Injectable, signal } from '@angular/core';
import type {
  EvaluationEntrepriseEtudiant,
  EvaluationEtudiantEntreprise,
  ProjetRef,
  ReponseFeedback,
} from './models';

@Injectable({ providedIn: 'root' })
export class EvaluationMockService {
  readonly projetsTermines = signal<ProjetRef[]>([
    {
      id: 'p-chahine',
      titre: 'Plateforme B2U-HUB — Evaluation & Feedback',
      etudiantId: 'e-chahine',
      etudiantNom: 'Chahine Sassi',
      etudiantEmail: 'sassichahine68@gmail.com',
      entrepriseNom: 'TechNova SAS',
      dateFin: '2026-05-21',
    },
    {
      id: 'p1',
      titre: 'Refonte landing page produit',
      etudiantId: 'e1',
      etudiantNom: 'Samira Benali',
      etudiantEmail: 'samira.benali@etudiant.tn',
      entrepriseNom: 'TechNova SAS',
      dateFin: '2026-04-12',
    },
    {
      id: 'p2',
      titre: 'Automatisation reporting Excel',
      etudiantId: 'e2',
      etudiantNom: 'Lucas Martin',
      etudiantEmail: 'lucas.martin@etudiant.tn',
      entrepriseNom: 'FinanceHub',
      dateFin: '2026-03-28',
    },
    {
      id: 'p3',
      titre: 'Chatbot support client (IA conversationnelle)',
      etudiantId: 'e3',
      etudiantNom: 'Youssef Trabelsi',
      etudiantEmail: 'youssef.trabelsi@etudiant.tn',
      entrepriseNom: 'InnoSoft',
      dateFin: '2026-05-10',
    },
    {
      id: 'p4',
      titre: 'Marketplace e-commerce artisanat local',
      etudiantId: 'e4',
      etudiantNom: 'Ines Gharbi',
      etudiantEmail: 'ines.gharbi@etudiant.tn',
      entrepriseNom: 'ArtisanConnect',
      dateFin: '2026-05-18',
    },
  ]);

  readonly evaluationsEntreprise = signal<EvaluationEntrepriseEtudiant[]>([
    {
      id: 'ev1',
      projetId: 'p0',
      projetTitre: 'Audit SEO e-commerce',
      etudiantNom: 'Samira Benali',
      noteGlobale: 5,
      qualiteLivraisons: 5,
      communication: 4,
      professionnalisme: 5,
      commentaire:
        'Livrables propres, respect des délais. Très bonne communication sur les blocages.',
      date: '2026-02-01',
    },
  ]);

  readonly evaluationsEtudiant = signal<EvaluationEtudiantEntreprise[]>([
    {
      id: 'ee1',
      projetId: 'p1',
      projetTitre: 'Refonte landing page produit',
      entrepriseNom: 'TechNova SAS',
      noteExperience: 4,
      clariteBrief: 5,
      accompagnement: 4,
      recommanderait: true,
      commentaire: 'Brief clair, disponibilité pour les questions.',
      date: '2026-04-15',
    },
  ]);

  readonly reponsesFeedback = signal<ReponseFeedback[]>([]);

  ajouterEvaluationEntreprise(ev: Omit<EvaluationEntrepriseEtudiant, 'id' | 'date'>) {
    const id = 'ev-' + Date.now();
    const date = new Date().toISOString().slice(0, 10);
    this.evaluationsEntreprise.update((list) => [{ ...ev, id, date }, ...list]);
  }

  ajouterEvaluationEtudiant(ev: Omit<EvaluationEtudiantEntreprise, 'id' | 'date'>) {
    const id = 'ee-' + Date.now();
    const date = new Date().toISOString().slice(0, 10);
    this.evaluationsEtudiant.update((list) => [{ ...ev, id, date }, ...list]);
  }

  ajouterReponseFeedback(r: Omit<ReponseFeedback, 'date' | 'id'>) {
    const id = 'rep-' + Date.now();
    const date = new Date().toISOString().slice(0, 10);
    this.reponsesFeedback.update((list) => [{ ...r, id, date }, ...list]);
  }

  moyenneNotesEtudiant(): number {
    const list = this.evaluationsEntreprise();
    if (!list.length) return 0;
    const sum = list.reduce((a, e) => a + e.noteGlobale, 0);
    return Math.round((sum / list.length) * 10) / 10;
  }
}
