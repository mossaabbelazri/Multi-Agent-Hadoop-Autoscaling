package com.projet.hadoop;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class MoniteurAgent extends Agent {
    private SystemInfo si = new SystemInfo();
    private CentralProcessor processor = si.getHardware().getProcessor();
    private long[] oldTicks = new long[CentralProcessor.TickType.values().length];

    protected void setup() {
        System.out.println("Agent Moniteur [" + getLocalName() + "] lance sur AWS.");

        // On envoie les metrics toutes les 10 secondes (10000ms)
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                // Lecture de la charge CPU RÉELLE
                double cpuLoad = processor.getSystemCpuLoadBetweenTicks(oldTicks) * 100;
                oldTicks = processor.getSystemCpuLoadTicks();

                System.out.println("Charge CPU actuelle (Réelle) : " + String.format("%.2f", cpuLoad) + "%");

                // Envoi de la donnée à l'Agent Decideur
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(new jade.core.AID("Decideur", jade.core.AID.ISLOCALNAME));
                msg.setContent(String.valueOf(cpuLoad));
                send(msg);
            }
        });
    }
}