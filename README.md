# cargo(s)

Microservices for cargo recognition, name this to avoid project cargo in Rust.

## Execution
We provide kubernetes deployment files, checkout `kubernetes` folder for more detail.

## CLI
```bash
# Installation
cd ectl
npm install
npm link

# General view usage
ectl -h
# Deploy DSL source file.
# By default engine address is localhost:8080 if missing argument.
ectl -e <engine_address> deploy flows/test.flow
```

## Sample flow DSL
```
# Use '#' for simple comment
# Define a simple http source.
source SampleHTTPSource {
  proto HTTP
  path /api
  method POST  
}

# Define a MQTT service
service SampleMQTTService {
  proto MQTT
  # MQTT path is split using <address>@@<topic> 
  path mqtt.broker.place@@api
}

# Flows source to service
SampleHTTPSource ~> SampleMQTTService
```

## Available routes and definitions:

### cargo-cls

```bash
# listing all docs in mongo
GET /api/tag

# post a tag to drives the concatenate actions.
# { tag: _ }
POST /api/tag

# insert an ident, a.k.a tag + cls
# { tag: _, cls: _ }
POST /api/ident
```


