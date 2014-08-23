gasket
======

Wraps the [Google Spreadsheet API (v3)](https://developers.google.com/google-apps/spreadsheets/) for simpler consumption from Scala.

The Java API as provided by Google is quite tedious to use, requiring a lot of boilerplate method-calling
combined with occasional "magic strings" to make it all work properly.

The intent of this library is to present a far-simpler view, where each level on the hierarchy of objects (see below)
is an appropriate type from ```scala.collection.immutable```, allowing idiomatic operations.


The Gasket Hierarchy
====================
```
   Account
      |
      o
  Spreadsheet
      |
      o
   Worksheet
      |
      o
     Row
      |
      o
     Cell
```

Troubleshooting
===============

If you get a ```Failure(com.google.gdata.client.GoogleService$InvalidCredentialsException: Invalid credentials)``` when trying to
create an ```Account```, the most likely cause is that your Google account has 2-factor authentication activated.

The best solution is to log into your account with a browser, go to Account -> Security -> App Passwords -> Settings and
add a special password just for Gasket / your app to use.

