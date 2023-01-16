# vertx-web-client-4aws

---

## What is it?
[Vertx][vertx] 4+ compatible web client that supports AWS [Signature Version 4][sigv4]. 

## What is it for ?
Most common use case is signing HTTP requests to AWS API Gateway secured with IAM. 

## How do I use it?
Include dependency in your pom.xml
```xml
<dependency>
    <groupId>com.venikkin</groupId>
    <artifactId>vertx-web-client-4aws</artifactId>
    <version>0.2</version>
</dependency>
```
or build.gradle
```groovy
implementation 'com.venikkin:vertx-web-client-4aws:0.1'
```
and use in your code
```java
ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create("aws-profile");
AwsSigningOptions signingOptions = new AwsSigningOptions()
    .setCredentialsProvider(credentialsProvider)
    .setRegion(Region.EU_WEST_1)
    .setClock(Clock.systemUTC());
Vertx vertx = Vertx.vertx();
WebClientOptions webClientOptions = new WebClientOptions()
    .setDefaultHost("123456.execute-api.eu-west-1.amazonaws.com")
    .setDefaultPort(443)
    .setSsl(true));
        
    
// You can initiate this web client with signing and web options directly 
WebClient signingClient = AwsSigningWebClient.create(vertx, webClientOptions, signingOptions);
    
// Or you can wrap existing web client 
WebClient vanillaClient = WebClient.create(vertx, webClientOptions);
WebClient signingWrapper = AwsSigningWebClient.create(vanillaClient, signingOptions);
    
// Use as you would use Vertx web client 
signingClient.get("/latest/petshop")
    .send(response -> System.out.println(response.bodyAsString()));
```

## Limitations
Currently, it doesn't support multipart form uploads (`sendMultipartForm`) and streams (`sendStream`). 
In that case payload is sent unsigned. Support might be added in the future. 

## How do I build it? 
In order to build the project without running tests, clone it and execute `./gradlew build -x test`. 

Tests will require you to have an AWS account, and golang (1.16+), and serverless (2.28+) installed. 
* Set up an AWS profile called `aws-test` in `eu-west-1` region
* Run `./gradlew deployTestStack`
* Build the project with verification `./gradlew build`
* Clean up the stack `./gradlew removeTestStack`

Please note that AWS will charge you for running API Gateway unless you have a free tier available. 
Although stack deletion should remove most of the resources, make sure that you stack is deleted correctly 
and S3 doesn't contain leftovers from serverless deployment.   

[vertx]: https://vertx.io/
[sigv4]: https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html
