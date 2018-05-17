## Introduction

This is a REST-service for validate JSON documents against JSON Schemas.

This service allow users to upload JSON Schemas and store them at unique URI and then validate JSON documents against these URIs.

When a JSON document with null value are supplied, those associated keys are removed first before JSON document before validating it.

## Main tools

* [Play framework 2.6.13](https://www.playframework.com)
* [Play JSON Schema Validator 0.9.4](https://github.com/eclipsesource/play-json-schema-validator)

## Running 

    sbt run

## Running the tests

    sbt test

## Usage
 
##### Upload a JSON Schema with unique `SCHEMAID`
```curl http://localhost:9000/schema/SCHEMAID -X POST -d @file.json```

##### Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```curl http://localhost:9000/validate/SCHEMAID -X POST -d @file.json```

##### Download a JSON Schema with unique `SCHEMAID`
```curl http://localhost:9000/schema/SCHEMAID -X GET```



## Endpoint mapping

Each endpoint is mapped to the corresponding action method. 

```
POST    /schema/:id                 controllers.HomeController.upload(id:String)
POST    /validate/:id               controllers.HomeController.validate(id:String)
GET     /schema/:id                 controllers.HomeController.download(id:String)
```

## Online Demo instance

You can check the running instance at 18.191.103.133

Example : 
``` 
$ curl 18.191.103.133:80/schema/5 -X POST -d @test/schema/valid-schema.json 
```
Response: 
```
{"action":"uploadSchema","id":"5","status":"success"}
```