provider "google" {
  project = "hadoop-jade" # À adapter avec ton ID de projet
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_compute_firewall" "hadoop_jade_sg" {
  name    = "hadoop-jade-sg-v1"
  network = "default"

  # Allow SSH, JADE, and Hadoop ports
  allow {
    protocol = "tcp"
    ports    = ["22", "1099", "8088", "9870"]
  }

  source_ranges = ["0.0.0.0/0"]
}

resource "google_compute_instance" "hadoop_master" {
  name         = "hadoop-master"
  machine_type = "e2-micro"
  allow_stopping_for_update = true

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-11" # Ou une image personnalisée Hadoop
    }
  }

  network_interface {
    network = "default"
    access_config {
      # Ephemeral IP
    }
  }

  tags = ["hadoop-master"]

  metadata = {
    ssh-keys = "ubuntu:TA_CLE_PUBLIQUE_ICI" # Optionnel pour accès SSH
  }
}