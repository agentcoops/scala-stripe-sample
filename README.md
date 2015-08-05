Sample Stripe Scala Application
===============================

This is a sample Play application developed in Scala largely with the aim of providing a convenient playground for testing out the Stripe Java client. A medium-term goal is to provide a clean Scala wrapper around the Java client.

Configuration
-------------

To run the application you must first set the value of stripe.secret in conf/application.conf as well as enter your publishable key and connect application id in public/index.html. 

Usage
-----

The application can be run at localhost:9000 with the command: ./sbt run

You can get into a Scala console with the command: ./sbt console

In the latter case, the Stripe Java client and a few helper functions will already be loaded into scope.