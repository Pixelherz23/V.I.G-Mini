https://editor.swagger.io/


swagger: "2.0"
info:
  description: "Beschreibung"
  version: "1.0.0"
  title: "V.I.G-Mini"
  license:
    name: "Apache 2.0"
    url: "http://www.apache.org/licenses/LICENSE-2.0.html"
tags:
- name: "user"
  description: ""
- name: "settings"
  description: "hole einstellungen"
- name: "greenhouse"
  description: "Beschreibung"
- name: "information"
  description: "Beschreibung"
  externalDocs:
    description: "Find out more about our store"
    url: "http://swagger.io"
schemes:
- "https"
- "http"
paths:
  /new:
    post:
      tags:
      - "information"
      summary: "erhalte Neuigkeiten"
      
  /help:
    get:
      tags:
      - "information"
      summary: "Gibt informationen zurück"
      description: ""
      operationId: "getInventory"
      produces:
      - "application/json"
      parameters: []
      responses:
        "200":
          description: "successful operation"
          schema:
            type: "object"
            additionalProperties:
              type: "integer"
              format: "int32"
      security:
      - api_key: []
        
  /all/{e-mail}:
    get:
      tags:
      - "greenhouse"
      summary: "gibt alle gewächshäuser des Nutzers (email) zurück"
      description: ""
      produces:
      - "application/json"
      parameters: []
      responses:
        "200":
          description: "successful operation"
          schema:
            type: "object"
            additionalProperties:
              type: "integer"
              format: "int32"
      security:
      - api_key: []
      
  /measurements/now/{product_key}:
    get:
      tags:
      - "greenhouse"
      summary: "gibt die letzten Messwerte zurück "
      description: ""
      produces:
      - "application/json"
      parameters: []
      responses:
        "200":
          description: "successful operation"
          schema:
            type: "object"
            additionalProperties:
              type: "integer"
              format: "int32"
      security:
      - api_key: []
      
  /measurements/week/{product_key}:
    get:
      tags:
      - "greenhouse"
      summary: "gibt die Messwerte der letzten Woche zurück "
      description: ""
      produces:
      - "application/json"
      parameters: []
      responses:
        "200":
          description: "successful operation"
          schema:
            type: "object"
            additionalProperties:
              type: "integer"
              format: "int32"
      security:
      - api_key: []
              
  /measurements/month/{product_key}:
    get:
      tags:
      - "greenhouse"
      summary: "gibt die Messwerte des letzten Monats zurück"
      description: ""
      produces:
      - "application/json"
      parameters: []
      responses:
        "200":
          description: "successful operation"
          schema:
            type: "object"
            additionalProperties:
              type: "integer"
              format: "int32"
      security:
      - api_key: []  
      
  /measurements/new{product_key}:
    post:
      tags:
      - "greenhouse"
      summary: "neue Messwerte eintragen"
      description: ""
      produces:
      - "application/json"
      parameters: []
      responses:
        "200":
          description: "successful operation"
          schema:
            type: "object"
            additionalProperties:
              type: "integer"
              format: "int32"
      security:
      - api_key: []  
  /activate{product_key, e-mail}:
    get:
      tags:
      - "greenhouse"
      summary: "gewächshaus freischalten und mit Konto verknüpfen"
      description: ""
      produces:
      - "application/json"
      parameters: []
      responses:
        "200":
          description: "successful operation"
          schema:
            type: "object"
            additionalProperties:
              type: "integer"
              format: "int32"
      security:
      - api_key: []  
      
  /update/name{product_key, name}:
    get:
      tags:
      - "greenhouse"
      summary: "Namen des Gewächshaus ändern"
      description: ""
      produces:
      - "application/json"
      parameters: []
      responses:
        "200":
          description: "successful operation"
          schema:
            type: "object"
            additionalProperties:
              type: "integer"
              format: "int32"
      security:
      - api_key: []  
      
  /update{product_key}:
    get:
      tags:
      - "settings"
      summary: "Gewächshauseinstellungen ändern"
      description: ""
      produces:
      - "application/json"
      parameters: []
      responses:
        "200":
          description: "successful operation"
          schema:
            type: "object"
            additionalProperties:
              type: "integer"
              format: "int32"
      security:
      - api_key: []  
      
  /soil-moisture/time{product_key}:
    get:
      tags:
      - "settings"
      summary: "gebe Zeiteintellung zurück"
      description: ""
      
  /soil-moisture/value{product_key}:
    get:
      tags:
      - "settings"
      summary: ""
      description: ""
  
  /settings/light{product_key}:
    get:
      tags:
      - "settings"
      summary: "setze Licht"
      description: ""
      
  /new{firstname, lastname, email, password}:
    get:
      tags:
      - "user"
      summary: "erstelle user"
      description: ""
      
  /information/update{firstname, lastname, email, password, new_password}:
    get:
      tags:
      - "user"
      summary: "nutzerdaten aktualisieren"
      description: ""
      
  /login{ email, password}:
    get:
      tags:
      - "user"
      summary: ""
      description: ""
      
  /information/first-name{ email}:
    get:
      tags:
      - "user"
      summary: "get first"
      description: ""
  
  /information/last-name{ email}:
    get:
      tags:
      - "user"
      summary: "get lastname"
      description: ""
