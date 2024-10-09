# The Driver Monitoring

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