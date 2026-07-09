<div align="center">

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [MAPE-K Control Loop](#-mape-k-control-loop)
- [Agents](#-agents)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [How It Works](#-how-it-works)
- [Results](#-results)
- [License](#-license)

---

## 🎯 Overview

This project implements an **autonomous Multi-Agent System (MAS)** using the **JADE framework** that monitors and auto-scales a **Hadoop cluster** deployed on **Google Cloud Platform**.

### The Problem

- Hadoop clusters face **unpredictable workload spikes** that cause CPU saturation and job queuing
- Cloud-native auto-scaling lacks **domain-specific intelligence** (Makespan awareness)
- Manual scaling is **slow, costly, and error-prone**

### The Solution

A **MAPE-K** based multi-agent architecture where specialized agents **autonomously detect saturation, make scaling decisions, and provision new cloud instances** — all in real-time with built-in safety mechanisms.

---

## 🏗 Architecture

The system follows a **distributed architecture** with agents communicating via **ACL messages** through the JADE platform:

```
┌──────────────────────────────────────────────────────────────┐
│                    JADE Platform (HadoopSystem)               │
│                                                               │
│  ┌─────────────┐    ACL INFORM    ┌──────────────┐           │
│  │  🔍 Agent    │ ──────────────► │  🧠 Agent     │           │
│  │  Moniteur    │   CPU metrics   │  Décideur     │           │
│  │  (on AWS)    │                 │  (local PC)   │           │
│  └─────────────┘                 └──────┬───────┘           │
│        ▲                                │                    │
│        │ OSHI                    ACL REQUEST                 │
│        │ metrics            "LANCER_WORKER"                  │
│  ┌─────┴───────┐                        │                    │
│  │  Hadoop      │                 ┌──────▼───────┐           │
│  │  Cluster     │                 │  ⚡ Agent     │           │
│  │  (GCP EC2)   │ ◄───────────── │  Actionneur   │           │
│  └─────────────┘   GCP API       │  (local PC)   │           │
│                    create VM      └──────────────┘           │
│                                                               │
│  ┌─────────────────────────────────────────────┐             │
│  │  📊 MonitoringGui (Dark Theme Dashboard)     │             │
│  │  ├── Makespan Progress Bar                   │             │
│  │  ├── Agent Status Table                      │             │
│  │  └── Decision Console Logs                   │             │
│  └─────────────────────────────────────────────┘             │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔄 MAPE-K Control Loop

The system implements the **MAPE-K** (Monitor, Analyze, Plan, Execute, Knowledge) autonomic computing model:

| Phase               | Agent               | Action                                              |
| ------------------- | ------------------- | --------------------------------------------------- |
| **Monitor**   | `MoniteurAgent`   | Collects real CPU metrics via OSHI every 10s        |
| **Analyze**   | `DecideurAgent`   | Computes Makespan (max CPU across cluster)          |
| **Plan**      | `DecideurAgent`   | Threshold check (>70%) + Cooldown management (180s) |
| **Execute**   | `ActionneurAgent` | Provisions new GCP Compute instances via Java SDK   |
| **Knowledge** | Shared Config       | Alert threshold (70%), cooldown delay, GCP config   |

<div align="center">
<img src="Projet CSMR/mape-k.jpg" alt="MAPE-K Loop" width="400"/>
</div>

---

## 🤖 Agents

### 🔍 MoniteurAgent

> Deployed on the **cloud worker node**

- Uses **OSHI** library for real hardware metrics (not simulated)
- Reads CPU load between ticks every **10 seconds**
- Sends metrics to `DecideurAgent` via ACL `INFORM` messages

### 🧠 DecideurAgent

> Runs on the **local main container**

- Maintains a `HashMap` of all agent CPU loads
- Calculates **Makespan** = max CPU load across the cluster
- Triggers scaling when Makespan **> 70%** with a **3-minute cooldown** to prevent cascading launches
- Updates the real-time **Monitoring GUI**

### ⚡ ActionneurAgent

> Runs on the **local main container**

- Receives `LANCER_WORKER` requests from the Décideur
- Provisions new **GCP Compute Engine** instances (e2-micro, Debian 11)
- Configures disk, network interface, and external NAT
- Sends `CONFIRM` message back to unlock the Décideur

### 📊 MonitoringGui

> Swing-based dark theme dashboard

- Real-time **progress bar** with dynamic color (green → orange → red)
- Agent **status table** showing CPU % and saturation alerts
- **Console log** with timestamped decision events (Matrix green on black)

---

## 🛠 Tech Stack

| Category                 | Technology                       |
| ------------------------ | -------------------------------- |
| **Language**       | Java 11                          |
| **MAS Framework**  | JADE 4.6.0                       |
| **Build**          | Maven (shade + exec plugins)     |
| **System Metrics** | OSHI 6.4.0                       |
| **Cloud SDK**      | Google Cloud Compute v1 (1.88.0) |
| **Infrastructure** | Terraform (GCP provider)         |
| **Visualization**  | Python (matplotlib)              |
| **Presentation**   | Python (python-pptx)             |

---

## 📁 Project Structure

```
CSMR/
├── README.md
├── .gitignore
├── csmr.docx
├── Méthodologie de la RechercheChap1 [Réparé].pptx
├── MINI PROJETS MASTER BIG DATA_025_026 (1).xlsx
│
└── Projet CSMR/
    ├── Rapport_CSMR.pdf                    # Full project report
    ├── Presentation_Hadoop_SMA_Premium.pptx
    ├── generate_ppt.py                     # Presentation generator
    ├── discours-CSMR.pdf                   # Speech notes
    ├── documentation.docx
    ├── Synthèse.pdf
    ├── *.png / *.jpg                       # Architecture diagrams
    │
    └── agenthadoopcloud/                   # Main Java project
        ├── pom.xml                         # Maven config
        └── src/
            ├── test.py                     # Latency analysis chart
            └── main/java/com/projet/hadoop/
                ├── MainContainer.java      # JADE platform entry point
                ├── MoniteurAgent.java      # CPU monitoring agent
                ├── DecideurAgent.java      # Decision-making agent
                ├── ActionneurAgent.java    # GCP cloud connector agent
                ├── MonitoringGui.java      # Real-time Swing dashboard
                └── main.tf                 # Terraform GCP infrastructure
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 11+** and **Maven 3.x**
- **Google Cloud** account with Compute Engine API enabled
- **Terraform** (optional, for infra provisioning)

### 1. Clone & Build

```bash
git clone https://github.com/mossaabbelazri/CSMR-Multi-Agent-Hadoop-Autoscaling.git
cd CSMR-Multi-Agent-Hadoop-Autoscaling/agenthadoopcloud
mvn clean package
```

### 2. Run the Main Container (Local PC)

```bash
mvn exec:java -Dexec.mainClass="com.projet.hadoop.MainContainer"
```

This starts the JADE platform with `DecideurAgent`, `ActionneurAgent`, the Monitoring GUI, and the JADE Sniffer.

### 3. Run the Worker Agent (On Cloud VM)

```bash
java -jar target/AgentHadoopCloud-1.0-SNAPSHOT.jar worker
```

### 4. Stress Test (Simulate CPU Saturation)

```bash
# On the cloud VM, run:
cat /dev/zero > /dev/null
```

Watch the dashboard detect the spike and auto-scale!

---

## 📈 Results

| Metric                     | Before SMA               | After SMA               |
| -------------------------- | ------------------------ | ----------------------- |
| **CPU Load**         | 100% (saturated)         | ~40% (stabilized)       |
| **Response Time**    | Manual (minutes)         | Automatic (~57s total)  |
| **Scaling Decision** | Human operator           | Autonomous agent        |
| **Resource Waste**   | High (over-provisioning) | Low (on-demand scaling) |

### Latency Breakdown

| Phase                | Time  |
| -------------------- | ----- |
| SMA Detection        | ~0.8s |
| JADE Message Routing | ~1.2s |
| GCP Instance Startup | ~55s  |

---




## 👤 Author

**Mossaab Belazri**

[![GitHub](https://img.shields.io/badge/GitHub-mossaabbelazri-181717?logo=github)](https://github.com/mossaabbelazri)

---

## 📄 License

This project was developed as part of the **Master Big Data & AI** program at **Université Ibn Tofaïl, Kénitra**.

---

<div align="center">
