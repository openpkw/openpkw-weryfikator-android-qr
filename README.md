#openpkw-weryfikator-android-qr [![Build Status](https://travis-ci.org/openpkw/openpkw-weryfikator-android-qr.svg?branch=master)](https://travis-ci.org/openpkw/openpkw-weryfikator-android-qr)

Instrukcja dodania pliku konfigurujacego url do backendu:
- lokalizacja pliku config.txt folder OpenPKW
- struktura pliku:
```xml
<openpkw-mobile>
  <backends>
    <backend>
       <id>1</id>
       <name></name>
       <description></description>
       <url>http://serwer1.com</url>
     </backend>
     <backend>
      <id>2</id>
      <name>Serwer 2</name>
      <description>Serwer 2 - weryfikatora</description>
      <url>http://serwer2.com</url>
     </backend>
     <defaults>
       <id>1</id>
     </defaults>
  </backends>
</openpkw-mobile>
```
- przykładowe url backendu zgodnie z powyzsza konfiguracja:

logowanie: http://serwer1.com/login

weryfikator qr: http://serwer1.com/qr

rejestracja uzytkownika: http://serwer1.com/users

Działanie aplikacji OpenPKW po zmianie algorytmu na ECDSA:

Pierwsze uruchomienie generacja kluczy dla algorytmu ECDSA, krzywa - secp256k1

1. Logowanie:

  Aplikacja generuje następujący request 

  [host]/login?password=client_password&scope=read+write&client_secret=secret&client_id=openpkw&username=client_email&grant_type=password

  Authorization: Basic [Base64("client_id:client_secret")]
  
  Odpowiedź serwera:
  {
  "access_token": "app_access_token",
  "token_type": "bearer",
  "refresh_token": "app_refresh_token",
  "expires_in": 10,
  "scope": "read write"
  }

  Poprawne logowanie -> Czas życia sesji 15 minut
  
  Rekonfiguracja URL: Ustawienia -> Adres URL logowania

2. Rejestracja użytkownika:

  Aplikacja wysyła imie, nazwisko, email i hasło oraz wygenerowany klucz publiczny ECDSA na serwer:

  URL: POST [host]/users/

  JSON:{"first_name":"user_first_name","last_name":"user_last_name","email":"user_email","password":"user_password","public_key":"ecdsa_public_key}
  
  Odpowiedź serwera:
  {
  "data": "app_data",
  "errorMessage": "app_error_message",
  "errorCode": "app_error_code"
  }
  
  Rekonfiguracja URL: Ustawienia -> Adres URL rej. użytkownika

3. Przesyłanie kodu QR na serwer:

  Aplikacja podpisuje zeskanowany kod QR kluczem prywatnym ECDSA i wysyla następujący request:

  POST [host]/qr
  
  Authorization: Bearer app_access_token

  JSON: {"qr":"scanned_qr","token":"ecdsa_qr_signature}
  
  Odpowiedź serwera:
  {
  "errorMessage": "app_error_message",
  "protocol": "app_protocol",
  "candidates": "app_candidates []"
  }
  
  Konfiguracj ID i Secret OAuth2.0: Ustawienia -> Skonfiguruj ID uzytkownika i haslo
