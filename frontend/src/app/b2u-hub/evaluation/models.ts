export interface ProjetRef {
  id: string;
  titre: string;
  etudiantId: string;
  etudiantNom: string;
  etudiantEmail: string;
  entrepriseNom: string;
  dateFin: string;
}

/** Évaluation laissée par une entreprise sur un étudiant après un projet */
export interface EvaluationEntrepriseEtudiant {
  id: string;
  projetId: string;
  projetTitre: string;
  etudiantNom: string;
  noteGlobale: number;
  qualiteLivraisons: number;
  communication: number;
  professionnalisme: number;
  commentaire: string;
  date: string;
}

/** Évaluation laissée par un étudiant sur l’entreprise / l’expérience projet */
export interface EvaluationEtudiantEntreprise {
  id: string;
  projetId: string;
  projetTitre: string;
  entrepriseNom: string;
  noteExperience: number;
  clariteBrief: number;
  accompagnement: number;
  recommanderait: boolean;
  commentaire: string;
  date: string;
}

/** Réponse courte d’un étudiant à un feedback entreprise (maquette frontend) */
export interface ReponseFeedback {
  id: string;
  evaluationId: string;
  texte: string;
  date: string;
}
