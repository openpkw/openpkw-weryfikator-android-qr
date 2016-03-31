Działanie aplikacji OpenPKW po zmianie algorytmu na ECDSA:

Pierwsze uruchomienie generacja kluczy dla algorytmu ECDSA, krzywa - secp256k1

1. Logowanie:

  Aplikacja generuje następujący request 

  [host]/openpkw/api/login?password=client_password&scope=read+write&client_secret=secret&client_id=openpkw&username=client_email&grant_type=password

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

  URL: POST [host]/openpkw/users/

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

  POST [host]/openpkw/api/qr
  
  Authorization: Bearer app_access_token

  JSON: {"qr":"scanned_qr","token":"ecdsa_qr_signature}
  
  Odpowiedź serwera:
  {
  "errorMessage": "app_error_message",
  "protocol": "app_protocol",
  "candidates": "app_candidates []"
  }

  Rekonfiguracja URL: Ustawienia -> Adres URL weryfikatora QR
  
  Konfiguracj ID i Secret OAuth2.0: Ustawienia -> Skonfiguruj ID uzytkownika i haslo
