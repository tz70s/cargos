# cargo(s)

Microservices for cargo recognition, name this to avoid project cargo in Rust.

## Execution
Not available yet.

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


