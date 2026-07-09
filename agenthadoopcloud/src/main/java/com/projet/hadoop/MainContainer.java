package com.projet.hadoop;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainContainer {
    public static void main(String[] args) {
        try {
            // 1. Initialisation de l'environnement JADE
            Runtime rt = Runtime.instance();
            // 3. Détection du mode (Par défaut: Main, Argument: worker)
            boolean isWorker = args.length > 0 && args[0].equalsIgnoreCase("worker");
            
            AgentContainer container;
            if (isWorker) {
                // --- MODE WORKER (Sur AWS) : Container Périphérique ---
                Profile pWorker = new ProfileImpl();
                pWorker.setParameter(Profile.PLATFORM_ID, "HadoopSystem");
                pWorker.setParameter(Profile.MAIN_HOST, "127.0.0.1"); 
                pWorker.setParameter(Profile.MAIN_PORT, "1099");
                pWorker.setParameter(Profile.LOCAL_HOST, "127.0.0.1");
                pWorker.setParameter(Profile.LOCAL_PORT, "1200"); // Port different pour ne pas gener le tunnel
                
                container = rt.createAgentContainer(pWorker);
                
                AgentController acMoniteur = container.createNewAgent("Moniteur", "com.projet.hadoop.MoniteurAgent", null);
                acMoniteur.start();
                System.out.println(">>> Agent Moniteur connecte au PC via Tunnel (Port 1200).");
            } else {
                // --- MODE PRINCIPAL (Sur votre PC) : Main Container ---
                Profile p = new ProfileImpl();
                p.setParameter(Profile.PLATFORM_ID, "HadoopSystem");
                p.setParameter(Profile.MAIN_HOST, "127.0.0.1");
                p.setParameter(Profile.MAIN_PORT, "1099");
                p.setParameter(Profile.LOCAL_HOST, "127.0.0.1");
                p.setParameter(Profile.LOCAL_PORT, "1099");
                p.setParameter(Profile.GUI, "true");
                
                container = rt.createMainContainer(p);

                AgentController acDecideur = container.createNewAgent("Decideur", "com.projet.hadoop.DecideurAgent", null);
                acDecideur.start();

                AgentController acActionneur = container.createNewAgent("Actionneur", "com.projet.hadoop.ActionneurAgent", null);
                acActionneur.start();

                // 4. Lancement AUTOMATIQUE du Sniffer (Local)
                Thread.sleep(1000);
                Object[] snifferArgs = {"Moniteur;Decideur;Actionneur"};
                AgentController acSniffer = container.createNewAgent("sniffer", "jade.tools.sniffer.Sniffer", snifferArgs);
                acSniffer.start();

                System.out.println(">>> Plateforme JADE (Main) lancee sur localhost:1099. En attente du Moniteur...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
