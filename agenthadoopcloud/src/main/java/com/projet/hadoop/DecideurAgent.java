package com.projet.hadoop;

import java.util.Collections;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class DecideurAgent extends Agent {
    private HashMap<String, Double> chargesCluster = new HashMap<>();
    private final double SEUIL_ALERTE = 70.0;

    private MonitoringGui gui;

    // Sécurité : empêche de lancer 5 machines en 5 secondes
    private boolean extensionEnCours = false;
    private long dernierLancementTime = 0;
    private final long COOLDOWN_DELAY = 180000; // 3 minutes en millisecondes

    protected void setup() {
        System.out.println("Agent Decideur pret (Seuil: " + SEUIL_ALERTE + "%)");

        // Initialisation de l'Interface Graphique (Thème Sombre)
        SwingUtilities.invokeLater(() -> gui = new MonitoringGui());

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // Si on reçoit une confirmation de l'Actionneur que c'est fini
                    if (msg.getPerformative() == ACLMessage.CONFIRM) {
                        extensionEnCours = false;
                        if (gui != null)
                            gui.addLog(">>> Extension terminee sur AWS. Verrou leve.");
                        System.out.println(">>> Extension terminee. Verrou leve.");
                        return;
                    }

                    // Traitement des metrics CPU
                    double cpuRecu = Double.parseDouble(msg.getContent());
                    String agentName = msg.getSender().getLocalName();
                    chargesCluster.put(agentName, cpuRecu);

                    double makespan = Collections.max(chargesCluster.values());

                    // Mise à jour de la GUI
                    if (gui != null) {
                        gui.updateAgent(agentName, cpuRecu);
                        gui.updateMakespan(makespan);
                    }

                    System.out.println("Makespan actuel : " + String.format("%.2f", makespan) + "%");

                    // Logique de décision avec verrou
                    long currentTime = System.currentTimeMillis();
                    if (makespan > SEUIL_ALERTE && !extensionEnCours) {
                        if ((currentTime - dernierLancementTime) > COOLDOWN_DELAY) {
                            String logMsg = "!!! ALERTE SATURATION (" + String.format("%.2f", makespan)
                                    + "%) - Lancement d'un nouveau Worker !!!";
                            if (gui != null)
                                gui.addLog(logMsg);
                            System.out.println(logMsg);

                            dernierLancementTime = currentTime; // On met à jour l'heure du dernier lancement
                            extensionEnCours = true; // ON VERROUILLE

                            // CRÉATION ET ENVOI RÉEL DE L'ORDRE
                            ACLMessage ordre = new ACLMessage(ACLMessage.REQUEST);
                            ordre.addReceiver(new jade.core.AID("Actionneur", jade.core.AID.ISLOCALNAME));
                            ordre.setContent("LANCER_WORKER");
                            send(ordre);

                            if (gui != null)
                                gui.addLog("Ordre d'extension envoyé à l'Actionneur.");
                            System.out.println("Ordre d'extension envoyé à l'Actionneur.");
                        } else {
                            long attenteRestante = (COOLDOWN_DELAY - (currentTime - dernierLancementTime)) / 1000;
                            String logCool = "Saturation détectée, mais en période de refroidissement (encore "
                                    + attenteRestante + "s)";
                            if (gui != null)
                                gui.addLog(logCool);
                            System.out.println(logCool);
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }
}