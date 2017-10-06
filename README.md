Linthumbnail Web service :
=========================
LinThumbnail Web Service is a free software. This makes it possible to have an remote access to Linthumbnail application, using the Web service REST.


Dependences
-----------
The project is based on Dropwizard framework.

 * Linthumbnail 2.0
 * Dropwizard 1.1.1
 * Apache CXF 3.1.11

Remote Access :
-------------
To communicate with the service remotely, you have two request kind to send to this link : /linthumbnail

 * GET (/linthumbnail?mimeType=) :

  The GET request should be have a mimeType value in parameter, that makes you to test if Linthumbnail can generate the thumbnail for this file, before sending it.
  The server return a response 204 if success or 405 for not allowed request.


 * POST :

 The POST request should be have a mimeType and the file in parameters, the server process the request and return if ok three thumbnails (SMALL.png, MEDIUM.png and LARGE.png) to the client, or 500 if it failed.
