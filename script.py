import http.client as cl
import argparse
import json
from datetime import datetime, timedelta

"""======================================================

Variables Globales et Class

======================================================"""

FORMAT_DATE : str = "%d/%m/%YT%H:%M:%S" # Format de Date à parser
EPOCH : datetime = datetime(1970, 1, 1) + timedelta(hours=2) # La date à l'Unix Time

HOST : str = "localhost" # L'adresse à envoyer la requête

MINIMUM_NUMBER_CARDIAQUE : int = 1
MAXIMUM_NUMBER_CARDIAQUE : int = MINIMUM_NUMBER_CARDIAQUE + 1
MINIMUM_NUMBER_ACCELEROMETER : int = 3
MAXIMUM_NUMBER_ACCELEROMETER :int = MINIMUM_NUMBER_ACCELEROMETER + 1

URL_INFLUX : str = f"/api/v2/delete?org=myorg&bucket=mybucket"
HEADER_INFLUX : dict[str, str] =  {
    "Authorization": f"Token myadmintoken",
    "Content-Type": "application/json"
}

class ConvertDate:
    """
    Classe static pouvant renvoyer convertir date et la renvoyer
    """

    def strNow() -> str:
        """
        Renvoie la date actuel en string

        Args:
            a (int, float): Le premier nombre à ajouter.
            b (int, float): Le second nombre à ajouter.

        Returns:
            str: La date actuel
        """

        return datetime.now().strftime(FORMAT_DATE)
    
    def convertToTimeStamp(date: str) -> int:
        """
        Change la date mise en paramètre en timestamp

        Args:
            date (str): La date

        Returns:
            int: Le timestamp de la date mise en paramètre
        """
            
        date_object : datetime = datetime.strptime(date, FORMAT_DATE)
        secondsSinceEpoch : float = (date_object - EPOCH).total_seconds()
        return int(secondsSinceEpoch * 1000)
    
"""======================================================

Méthode script

======================================================"""

def sendRequest(methodParam : str) -> None:
    """
        Envoie une requête sur NodeRed en fonction de la méthode et de ces paramètres

        Args:
            methodParam (str): méthode et paramètre avec des '/' entre chacun
    """

    conn = cl.HTTPConnection(HOST, 1880)
    conn.request("GET", "/app/" + methodParam)
    conn.close()

def deleteMeasurementRequest(measurementName: str) -> None:
    """
        Supprime un measurement sur InfluxDb

        Args:
            measurementName (str): measurement à supprimer
    """
        
    data : dict[str, str] = {
        "start": "1970-01-01T00:00:00Z",
        "stop": "2024-12-31T23:59:59Z",
        "predicate": f"_measurement=\"{measurementName}\""
    }

    conn : cl.HTTPConnection = cl.HTTPConnection(HOST, 8086)
    conn.request("POST", URL_INFLUX, body=json.dumps(data), headers=HEADER_INFLUX)
    conn.close()

def handler_cardiaque(frequency: int, date : str) -> None :
    """
        Méthode pour gérer la partie cardiaque

        Args:
            frequency (int): la fréquence Cardiaque
            date (str): date de la donnée
    """
        
    timestamp : int = ConvertDate.convertToTimeStamp(date)
    sendRequest(f"cardiaque/{frequency}/{timestamp}")

def handler_accelerometer(x : float, y : float, z : float, date : str) -> None:
    """
        Méthode pour gérer la partie accelerometre

        Args:
            x (float): accelerometre x
            y (float): accelerometre y
            z (float): accelerometre z
            date (str): date de la donnée
    """

    timestamp : int = ConvertDate.convertToTimeStamp(date)
    sendRequest(f"accelerometer/{x}/{y}/{z}/{timestamp}")

def handlerSwitchOptionFile(line: str) -> None:
    """
        Géstionnaire option d'une ligne d'un fichier

        Args:
            line (str): ligne d'un fichier
    """

    argsLine: list[str] = line.split(' ')
    lenLine: int = len(argsLine)

    if argsLine[0].startswith('c'):
        if lenLine < MINIMUM_NUMBER_CARDIAQUE:
            raise TypeError("Cardiaque need one argument")
        handler_cardiaque(argsLine[1], argsLine[2] if lenLine > 2 else ConvertDate.strNow())
    elif argsLine[0].startswith('a'):
        if lenLine < MINIMUM_NUMBER_ACCELEROMETER:
            raise TypeError("Accelerometer need three argument")
        handler_cardiaque(argsLine[1], argsLine[2], argsLine[3], argsLine[4] if lenLine > MAXIMUM_NUMBER_ACCELEROMETER else ConvertDate.strNow())
    else:
        raise TypeError("Method Not Exist")
    

def readFile(nameFile : str) -> None:
    """
        Lis un fichier possédant les données mocker

        Args:
            nameFile (str): le nom du fichier
    """

    try:
        i: int = 1
        with open(nameFile, 'r') as file:
            for line in file:
                lineStrip : str = line.strip()
                print(f"{i} : {lineStrip}")
                handlerSwitchOptionFile(lineStrip)
                i += 1
    except FileNotFoundError:
        print("The file is not found")
    except TypeError as err:
        print(f"Error : {err}")

def handlerArgsOption(args: argparse.Namespace) -> None:
    """
        Gestionnaire option ligne de commande

        Args:
            args (argparse.Namespace): Les arguments récupérées de la ligne de commande
    """
        
    dateStr : str = args.t if args.t is not None else ConvertDate.strNow()

    if args.c is not None:
        handler_cardiaque(args.c, dateStr)
    elif args.a is not None:
        handler_accelerometer(args.a[0], args.a[1], args.a[2], dateStr)
    elif args.file is not None:
        readFile(args.file)
    elif args.delete is not None:
        deleteMeasurementRequest(args.delete)
    else:
        raise TypeError("No argument")


if __name__ == '__main__':
    parser: argparse.ArgumentParser = argparse.ArgumentParser()
    parser.add_argument("-c", help="To sent a request cardiaque", type=int)
    parser.add_argument("-a", nargs=3, help="To sent a request accelerometer", type=float)
    parser.add_argument("-t", help="To add a date", type=str)
    parser.add_argument("--delete",  help="To delete a measurement", type=str)
    parser.add_argument("--file", help="launch a script", type=str)
    args: argparse.Namespace = parser.parse_args()
    handlerArgsOption(args)
