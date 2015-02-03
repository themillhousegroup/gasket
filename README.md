gasket
======

Wraps the [Google Spreadsheet API (v3)](https://developers.google.com/google-apps/spreadsheets/)
for simpler consumption from Scala.

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
   [ Row ]
      |
      o
     Cell
```

```Row```s are an artificial collection representing some-or-all of the ```Cell```s on one row.

What Could I Use This For?
============

The sky's the limit, and if you're here, you probably already have an application in mind. But a few other ideas:
 
 - Use a Google Spreadsheet as a source of app configuration data
 - Crunch spreadsheet numbers using the power of Scala 
 - A straightforward and user-friendly way to feed tabular data into an application
  

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
     "com.themillhousegroup" %% "gasket" % "1.1.10"
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


#### Pushing individual values back into the Google Spreadsheet

Although `Cell` is immutable, it has an `update` method with the following signature:

   `def update(newValue: String): Future[Cell]`
   
That is, it performs the API call to update the remote spreadsheet, and returns a future version of the Cell with the desired value within. Because it's returning a `Future`, you can even make it a part of a for-comprehension - here's an example where the first cell with value "0" is changed to have value "EMPTY":

```
   for {
     acct <- Account(username, password)
     ss <- acct.spreadsheets
     ws <- ss("Example Spreadsheet").worksheets
     cells <- ws("Sheet3").cells
     newCell <- cells.filter(_.value == "0").head.update("EMPTY"")
   } yield newCell
```

#### Adding a row to a Worksheet

Again although `Worksheet` is immutable by design, it has an `addRows` method with the following signature:

   ```def addRows(newRows: Seq[Seq[(String, String)]]): Future[Worksheet]```
  
The argument to this method is a sequence of rows, where a row is a sequence of `(columnHeaderName -> cellValue)` tuples. The new data is "played in" to the remote API, and then a new fresh `Worksheet` containing the new rows at the bottom is returned in the `Future`.

Still To Come
===============
 - More updating methods
 - A Scala-idiomatic way of performing batched operations
 - More performance

Troubleshooting
===============

If you get a ```com.google.gdata.client.GoogleService$InvalidCredentialsException: Invalid credentials``` when trying to
create an ```Account```, the most likely cause is that your Google account has 2-factor authentication activated.

The best solution is to log into your account with a browser, go to Account -> Security -> App Passwords -> Settings and
add a special password just for Gasket / your app to use.

