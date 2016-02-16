Działanie aplikacji OpenPKW po dodaniu OAUTH 2.0:

1. Logowanie:

  Aplikacja generuje następujący request 

  [host]/openpkw/api/login?password=client_password&scope=read+write&client_secret=secret&client_id=openpkw&username=client_email&grant_type=password

  Authorization: Basic [Base64("client_id:client_secret")]
  
  Odpowiedź serwera:
  {
  "access_token": "app_access_token",
  "token_type": "bearer",
  "refresh_token": "app_refresh_token",
  "expires_in": 1800,
  "scope": "read write"
  }

  Rekonfiguracja URL: Ustawienia -> Adres URL logowania

2. Rejestracja użytkownika:

  Aplikacja wysyła imie, nazwisko, email i hasło na serwer:

  URL: POST [host]/openpkw/users/

  JSON:{"first_name":"user_first_name","last_name":"user_last_name","email":"user_email","password":"user_password"}
  
  Odpowiedź serwera:
  {
  "data": "app_data",
  "errorMessage": "app_error_message",
  "errorCode": "app_error_code"
  }

  Rekonfiguracja URL: Ustawienia -> Adres URL rej. użytkownika

3. Przesyłanie kodu QR na serwer:

  Aplikacja generuje następujący request:

  POST [host]/openpkw/api/qr
  
  Authorization: Bearer app_access_token

  JSON: {"token":"app_access_token,"qr":"scanned_qr"}
  
  Odpowiedź serwera:
  {
  "errorMessage": "app_error_message",
  "protocol": "app_protocol",
  "candidates": "app_candidates []"
  }

  Rekonfiguracja URL: Ustawienia -> Adres URL weryfikatora QR
