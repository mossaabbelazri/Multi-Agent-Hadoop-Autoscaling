package com.projet.hadoop;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.AccessConfig;

public class ActionneurAgent extends Agent {

    // Configuration GCP
    private final String PROJECT_ID = "hadoop-jade"; // À modifier
    private final String ZONE = "us-central1-a"; // À modifier
    private final String MACHINE_TYPE = "zones/" + ZONE + "/machineTypes/e2-micro";
    // Image contenant Hadoop (ou une base comme debian-11)
    private final String SOURCE_IMAGE = "projects/debian-cloud/global/images/family/debian-11";
    private final String NETWORK = "global/networks/default";

    private int compteurWorker = 1;

    protected void setup() {
        System.out.println("Agent Actionneur (Cloud Connector GCP) pret.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                // Attente d'un message REQUEST de la part du Decideur
                ACLMessage msg = receive();

                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST && msg.getContent().equals("LANCER_WORKER")) {
                        System.out.println("Ordre recu. Pilotage GCP...");

                        lancerInstanceWorker("worker-" + compteurWorker);
                        compteurWorker++;

                        // Informer le decideur que c'est fait pour liberer le verrou
                        ACLMessage confirmation = new ACLMessage(ACLMessage.CONFIRM);
                        confirmation.addReceiver(msg.getSender());
                        confirmation.setContent("TERMINE");
                        send(confirmation);
                    }
                } else {
                    block();
                }
            }
        });
    }

    public void lancerInstanceWorker(String nomWorker) {
        try (InstancesClient instancesClient = InstancesClient.create()) {

            AttachedDisk disk = AttachedDisk.newBuilder()
                    .setBoot(true)
                    .setAutoDelete(true)
                    .setType(AttachedDisk.Type.PERSISTENT.toString())
                    .setInitializeParams(
                            AttachedDiskInitializeParams.newBuilder()
                                    .setSourceImage(SOURCE_IMAGE)
                                    .build())
                    .build();

            NetworkInterface networkInterface = NetworkInterface.newBuilder()
                    .setName(NETWORK)
                    .addAccessConfigs(AccessConfig.newBuilder().setName("External NAT").build())
                    .build();

            Instance instanceResource = Instance.newBuilder()
                    .setName(nomWorker)
                    .setMachineType(MACHINE_TYPE)
                    .addDisks(disk)
                    .addNetworkInterfaces(networkInterface)
                    .build();

            System.out.println("Creation de l'instance GCP en cours : " + nomWorker);

            InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
                    .setProject(PROJECT_ID)
                    .setZone(ZONE)
                    .setInstanceResource(instanceResource)
                    .build();

            instancesClient.insertAsync(insertInstanceRequest).get();

            System.out.println(">>> Succes : " + nomWorker + " a ete cree avec succes sur Google Cloud.");
        } catch (Exception e) {
            System.err.println("Erreur GCP : " + e.getMessage());
            e.printStackTrace();
        }
    }
}