# Input file: input.txt

        ##### Java Runtime Environment installieren (JRE)
        ##### https://javadl.oracle.com/webapps/download/AutoDL?BundleId=252627_99a6cb9582554a09bd4ac60f73f9b8e6
        ##### In Downloads gehen und installieren
        #####
        ##### Eingabeaufforderung cmd aufrufen, mit cd pfad in Verzeichnis der Anwendung wechseln
        ##### java -jar TC83LK-1.0.jar ausführen
        
        ##### Add players at begin of the season
        ##### Name, LK
        ADD Sandra, 6.2
        ADD Tina, 19.8
        ADD Monika, 25.0
        ADD Karin, 21.1
        ADD Rao, 21.1
        ADD Lisa, 24.5
        ADD Christine, 24.6
        
        CALIBRATE
        
        ##### Add players during the season
        ADD Nicole, 22.8
        
        ##### Matches
        ##### Date, Winner, Loser
        MATCH 2025-10-10 15:30, Sandra,Lisa
        MATCH 2025-10-10 15:31, Christine,Lisa
        MATCH 2025-10-05 15:30, Lisa, Sandra
        MATCH 2025-10-10 15:30, Tina,Sandra
        MATCH 2025-10-10 15:30, Rao,Sandra
        MATCH 2025-10-10 15:30, Rao,Tina
        MATCH 2025-10-10 15:30, Tina,Rao
        MATCH 2025-10-08 15:30, Rao,Tina
        MATCH 2025-10-10 15:30, Tina,Rao
        MATCH 2025-10-10 15:30, -,Tina
        MATCH 2025-10-10 15:30, Karin,Sandra
        MATCH 2025-10-10 15:30, Sandra,Karin
        MATCH 2025-10-10 15:30, Rao,Karin
        MATCH 2025-10-10 15:30, Monika,Sandra
        MATCH 2025-10-10 15:30, Monika,Karin
        MATCH 2025-10-10 15:30, Monika,Rao
        MATCH 2025-10-10 15:30, Monika,Sandra
        MATCH 2025-10-10 15:30, Monika,Karin
        MATCH 2025-10-10 15:30, -,Monika
