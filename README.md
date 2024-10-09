# The Driver Monitoring

## Objectif

Une étude scientifique révèle que de nombreux conducteurs de poids lourds manquent de sommeil. Plus d'un conducteur de poids lourds sur quatre (28 %) dort moins de 6 heures avant de prendre la route pour un long trajet. Près d'un sur trois (30 %) s'estime susceptible d'avoir un accident à cause de la somnolence (Source : Vinci Autoroutes). 

Le but de ce projet est d'assister les conducteurs de bus/camions lors de leurs trajets quotidiens afin de monitorer, anticiper et réagir à la somnolence au volant. Cela permetterai Pour cela, nous utiliserons deux dispositifs afin de récolter les données nécessaires à notre analyse, une smart watch et un télephone portable. La smart watch récuperera les données liées au rythme cardiaque du conducteur afin de déterminer une éventuelle phase de somnolence, auquel cas la montre enverra des vibrations/alertes sonores. Le telephone portable lui, devra être posé sur un support fixe et récoltera toutes les données liées à la conduite à savoir la trajectoire et l'acceleration grâce au gyroscope, GPS et accéléromètre.

### Smart Watch

##### 1. Fréquence cardiaque (HR)

- **Description** : Une baisse progressive de la fréquence cardiaque peut indiquer une transition vers la somnolence.
- **Utilisation** : Une surveillance en temps réel permet de suivre l'état de vigilance.

##### 2. Variabilité de la fréquence cardiaque (HRV)

- **Description** : La variabilité des intervalles entre les battements cardiaques reflète l’équilibre entre le système nerveux sympathique (excitation) et parasympathique (relaxation).
- **Utilisation** : Une diminution de la HRV peut signaler une diminution de l'éveil ou une fatigue croissante.

#### 3. Données de vigilance grâce à des jeux ou tests

- **Description** : Certaines applications proposent des tests rapides de vigilance qui mesurent la rapidité de réaction via l'écran de la montre.
- **Utilisation** : Intégration d'exercices de vigilance qui peuvent être effectués périodiquement.


### Smart Phone

#### 1. Accéléromètre

- **Description** : Le capteur mesure les mouvements du téléphone, ce qui peut refléter des mouvements brusques ou une conduite imprécise due à la somnolence.
- **Utilisation** : Détection de micro-sommets de mouvements indiquant un relâchement de la vigilance ou des changements de posture du conducteur.

#### 2. Données GPS

- **Description** : Utilisation du GPS pour détecter les changements brusques dans la conduite, comme des déviations de la trajectoire, des arrêts inattendus ou une conduite erratique.
- **Utilisation** : Un comportement de conduite irrégulier peut indiquer que le conducteur n'est plus complètement alerte.

## Credential

### InfluxDB

username: driver   
password: driver_password

### Grafana

user: driver  
password: driver_password


# STEPS 
# 1 : Start your docker desktop engine
# 2 : docker compose up --build -d  
## => if there is any problem just delete the containers in docker desktop
# 3 : Start Node Red : go to http://localhost:1880/
# 4 : Go to MIT APP INVITOR => Project => Import project (.aia) => DriverMonitor.aia
# 5 : Start Grafana : go to http://localhost:3000/ => Enter Credentials (ils sont en haut)
# 6 : Start InfluxDB : go to http://localhost:8086/ => Enter Credentials (ils sont en haut)
# 7 : Start the good work !
### Étapes de développement

- Première étape : Bouton qui déclenche la "journée" du chauffeur. Cela signifie que le chauffeur déclenche la récupération des données de rythme cardiaque et de l'accélération (accéléromètre) ainsi qu'un chronomètre. Ces données doivent être simulées dans un premier temps jusqu'à avoir une montre connectée. Ces données sont graphées sur Grafana. Au bout de deux heures, le chauffeur est invité à faire une pause.
