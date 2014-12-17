gasket
======

Wraps the [Google Spreadsheet API (v3)](https://developers.google.com/google-apps/spreadsheets/)
for simpler consumption from Scala.

The Java API as provided by Google is quite tedious to use, requiring a lot of boilerplate method-calling
combined with occasional "magic strings" to make it all work properly.

The intent of this library is to present a far-simpler view, where each level on the hierarchy of objects (see below)
is an appropriate type from ```scala.collection.immutable```, allowing idiomatic operations. Note that this is currently
a *READ-ONLY* implementation - no modifications are ever "pushed back" into the source spreadsheet(s).


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
   [ Row ]
      |
      o
     Cell
```

```Row```s are an artificial collection representing some-or-all of the ```Cell```s on one row.

Installation
============
Bring in the library by adding the following to your ```build.sbt```. 

  - The release repository: 

```
   resolvers ++= Seq(
     "Millhouse Bintray"  at "http://dl.bintray.com/themillhousegroup/maven"
   )
```
  - The dependency itself: 

```
   libraryDependencies ++= Seq(
     "com.themillhousegroup" %% "gasket" % "1.0.3"
   )

```



Implementation
==============

Because many of the API calls can take hundreds of milliseconds to complete, the Gasket API is non-blocking, returning
```Future```s from all methods. This lends itself quite naturally to working in ```for```-comprehension-style, as
shown in the examples below.
The way into the API is via the ```Account``` companion object, and the most useful class is probably ```Worksheet```,
where the contents of a worksheet can be sliced-and-diced.


Examples
========

(Taken from the [ForComprehensionSpec](https://github.com/themillhousegroup/gasket/blob/master/src/test/scala/com/themillhousegroup/gasket/integration/ForComprehensionSpec.scala)
which contains several more examples):

#### Getting the content of all the cells in a worksheet as a ```Future[Seq[String]]```:

   ```
       val futureCellContents =
           for {
             acct      <- Account(username, password)
             ss        <- acct.spreadsheets
             ws        <- ss("Example Spreadsheet").worksheets
             cells     <- ws("Sheet1").cells
             contents  = cells.map(_.value)
           } yield contents
   ```

#### Getting a rectangular block of cells in a worksheet as a ```Future[Seq[Row]]```:

   ```
      val futureRows =
        for {
          acct <- Account(username, password)
          ss <- acct.spreadsheets
          ws <- ss("Example Spreadsheet").worksheets
          rows <- ws("Sheet1").block(1 to 2, 2 to 3)
        } yield rows
   ```

This sequence of commands, if applied to a source worksheet that looks like this:
```
   1  2  3
   4  5  6
   7  8  9
```

Will return 2 ```Row``` objects:

Row: ```2 3```
Row: ```5 6```


Troubleshooting
===============

If you get a ```com.google.gdata.client.GoogleService$InvalidCredentialsException: Invalid credentials``` when trying to
create an ```Account```, the most likely cause is that your Google account has 2-factor authentication activated.

The best solution is to log into your account with a browser, go to Account -> Security -> App Passwords -> Settings and
add a special password just for Gasket / your app to use.

