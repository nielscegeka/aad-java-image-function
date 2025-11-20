Azure Animal Directory - Technical Meetup 27/11/2025

Deze function is bedoeld om op Azure Functions losstaand te deployen via de aanwezige maven plugin. 
Deze function wordt aangeroepen door een externe Spring Boot Applicatie als deze bepaald heeft via EntraID dat deze de juiste permissies heeft.
Achterliggend gaat deze function praten met een Azure OpenAI instantie die images genereert op basis van de userinput.

Voorbeeld (Inkomend naar de function is een GET request, de request naar de OpenAI instantie is een POST request)
Input: "prompt": "gorilla"
Output: "data": [{ "image": "data:image/png;base64," + base64 }]

Om deze te connecteren met de Spring Boot applicatie volstaat de juiste netwerk configuratie/permissies en het toevoegen van onderstaande URL in de configuratie van de Spring Boot App:
https://<dummy-function>.azurewebsites.api/api/generateImage

Voor het deployment zijn er verschillende stappen te gaan die vooraleer je de maven plugin kan laten draaien:
1. Maak een resource groep aan (Organisatie van resources).
2. Maak een Azure OpenAI Service aan.
3. Open de Azure OpenAI om naar de Foundry Portal te gaan.
4. Ga naar de playgrounds en selecteer Images > Create a deployment.
5. Selecteer dall-e 3 image, en kies de resource location (dit is sterk afhankelijk van je subscription free = 1 per dag!, vandaar ook de "dev mode")
6. Eens je de API key en de url hiervan hebt zet deze in pom.xml (plugin) en de config.properties
7. Run dit maven command mvn -X clean package azure-functions:package azure-functions:deploy -DAZURE_RG=<resource-group> -DAZURE_FN_APP=<function-name>

Resource-group is de verzameling van resources, 2e stap in de hiërarchie onder Subscriptions

Stappen die de plugin maakt
1. Azure gaat je proberen authenticeren via browser, selecteer het juiste account.
2. Aanmaken van een Service Plan (Soort van organisatie van resources in functie van billing).
3. Aanmaken van een Storage Container (Opslag van metrics en zaken in die aard, moest je een function hebben die persisteert zal het naar die storage container zijn).
   
(Optioneel): Als je de functie losstaand van de flow wil testen kan je dat via Azure Portal. Kan je door volgende stappen:
A. Ga naar je Function > API > Cors > Voeg https://portal.azure.com toe aan je Cors configuratie
B. Ga naar je Function > generateImage (Function Name) > Test/Run > Selecteer GET als methode > Vul in query params 'animal' 'dog' in > Run

? Kan je gebruik maken van een bestaand Service Plan ? Ja, binnen deze instructie/de demo wordt daar gebruik van gemaakt. Indien de plugin <function-name>-plan niet terugvindt als Service Plan wordt deze automatisch aangemaakt.
? Kan je gebruik maken van een bestaande Storage Container ? Normaal wel, dit project is daar niet op voorzien omdat ik het nog niet gevonden heb.
? Waarom de Dev_mode ? Om de functie as-is te kunnen testen, door het onderstaande AI service probleem heb ik dit als backup bedacht.
? Waarom geen Spring Boot App ? Als we specifiek gebruik willen maken van Azure Functions voor deployment (kost, opzettijd, ...) is dit sneller en minder efficient dan Spring Boot.
? Is dit een finaal product ? Nee, alles kan beter. Suggesties zijn ten alle tijden welkom.

! Let op als je een AI service verkeerd geconfigueerd, dit kan tot 48u duren tot de plek in bepaalde subscription tiers terug vrij is voor een nieuwe aan te maken !