# 🚀 Multi-Agent System for Hadoop Cluster Auto-scaling on Cloud

**Conception et Implémentation d'un Système Multi-Agents pour l'Auto-scaling d'un Cluster Hadoop**

[![Java](https://img.shields.io/badge/Java-11-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JADE](https://img.shields.io/badge/JADE-4.6.0-00897B?style=for-the-badge)](https://jade.tilab.com/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Terraform](https://img.shields.io/badge/Terraform-IaC-844FBA?style=for-the-badge&logo=terraform&logoColor=white)](https://www.terraform.io/)
[![GCP](https://img.shields.io/badge/Google_Cloud-Compute-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white)](https://cloud.google.com/)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [MAPE-K Control Loop](#-mape-k-control-loop)
- [Agents](#-agents)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Results](#-results)
- [Author](#-author)
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
│  │  (on Cloud)  │                 │  (local PC)   │           │
│  └─────────────┘                 └──────┬───────┘           │
│        ▲                                │                    │
│        │ OSHI                    ACL REQUEST                 │
│        │ metrics            "LANCER_WORKER"                  │
│  ┌─────┴───────┐                        │                    │
│  │  Hadoop      │                 ┌──────▼───────┐           │
│  │  Cluster     │                 │  ⚡ Agent     │           │
│  │  (GCP)       │ ◄───────────── │  Actionneur   │           │
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
| **Visualization**  | <br />Python (matplotlib)        |
|                          |                                  |

---

## 📁 Project Structure

```
Multi-Agent-Hadoop-Autoscaling/
├── README.md
├── .gitignore
│
└── agenthadoopcloud/
    ├── pom.xml                             # Maven build config
    ├── dependency-reduced-pom.xml
    └── src/
        ├── test.py                         # Latency analysis chart
        └── main/java/com/projet/hadoop/
            ├── MainContainer.java          # JADE platform entry point
            ├── MoniteurAgent.java          # CPU monitoring agent
            ├── DecideurAgent.java          # Decision-making agent
            ├── ActionneurAgent.java        # GCP cloud connector agent
            ├── MonitoringGui.java          # Real-time Swing dashboard
            └── main.tf                     # Terraform GCP infrastructure
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 11+** and **Maven 3.x**
- **Google Cloud** account with Compute Engine API enabled
- **Terraform** (optional, for infra provisioning)

### 1. Clone & Build

```bash
git clone https://github.com/mossaabbelazri/Multi-Agent-Hadoop-Autoscaling.git
cd Multi-Agent-Hadoop-Autoscaling/agenthadoopcloud
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

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.

---

**Built with ☕ Java, 🤖 JADE, and ☁️ Google Cloud**



