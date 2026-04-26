# AI Tools Configuration Schema

## Introduzione

Gli **ai_tools** sono tool dinamici caricati dal database che l'IA assistant può invocare. Ogni tool è definito dai seguenti campi:

- **name**: identificativo unico del tool (esempio: "getProductsByTenant")
- **method_name**: nome del metodo esposto all'AI (se vuoto, usa name)
- **description**: descrizione che l'AI legge per capire cosa fa il tool
- **sql_query**: query SELECT SQL che il tool esegue con i parametri ricevuti
- **parameters_schema**: JSON Schema che descrive i parametri che l'AI deve passare
- **active**: booleano che abilita/disabilita il tool

## Regole critiche

1. **Ordine dei parametri**: l'ordine delle proprietà in `parameters_schema.properties` DEVE corrispondere all'ordine dei placeholder `?` nella `sql_query`
2. **Tipi coerenti**: i tipi dichiarati in parameters_schema devono essere coerenti con il binding SQL
3. **JSON Schema**: parameters_schema DEVE essere un JSON Schema valido di tipo `object`

## Formato JSON per il form ai_tools

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "title": "AI Tool Definition",
  "properties": {
    "uuid": {
      "type": "string",
      "description": "Identificativo univoco (generato automaticamente se assente)"
    },
    "name": {
      "type": "string",
      "pattern": "^[a-zA-Z_][a-zA-Z0-9_]*$",
      "minLength": 1,
      "maxLength": 255,
      "description": "Nome univoco del tool (snake_case)"
    },
    "method_name": {
      "type": ["string", "null"],
      "pattern": "^[a-zA-Z_][a-zA-Z0-9_]*$|^$",
      "maxLength": 255,
      "description": "Nome del metodo esposto all'AI (opzionale, default: name)"
    },
    "description": {
      "type": ["string", "null"],
      "maxLength": 4000,
      "description": "Descrizione che l'AI legge per capire il tool"
    },
    "sql_query": {
      "type": "string",
      "minLength": 1,
      "maxLength": 10000,
      "description": "Query SELECT con placeholders (?)"
    },
    "parameters_schema": {
      "type": ["string", "null"],
      "description": "JSON Schema valido che descrive i parametri (object type)"
    },
    "active": {
      "type": "boolean",
      "default": false,
      "description": "Se true, il tool è disponibile all'AI"
    }
  },
  "required": ["name", "sql_query", "active"],
  "additionalProperties": false
}
```

## Esempi di parameters_schema

### Esempio 1: Query con 3 parametri

**SQL nel campo sql_query:**
```sql
SELECT uuid, name, price, tenant_id
FROM products
WHERE tenant_id = ? AND status = ? AND price > ?
ORDER BY created_at DESC
LIMIT 20
```

**JSON per parameters_schema:**
```json
{
  "type": "object",
  "description": "Filtra prodotti per tenant, status e prezzo minimo",
  "properties": {
    "tenant_id": {
      "type": "string",
      "description": "UUID del tenant"
    },
    "status": {
      "type": "string",
      "enum": ["draft", "published", "archived"],
      "description": "Stato del prodotto"
    },
    "min_price": {
      "type": "number",
      "minimum": 0,
      "description": "Prezzo minimo (in centesimi)"
    }
  },
  "required": ["tenant_id", "status"],
  "additionalProperties": false
}
```

**Binding:**
1. tenant_id (string) → primo `?`
2. status (string) → secondo `?`
3. min_price (number) → terzo `?`

### Esempio 2: Query senza parametri

**SQL nel campo sql_query:**
```sql
SELECT id, denomination FROM currencies
WHERE active = true
ORDER BY denomination ASC
```

**JSON per parameters_schema:**
```json
{
  "type": "object",
  "properties": {},
  "required": [],
  "additionalProperties": false
}
```

### Esempio 3: Query con parametri opzionali

**SQL nel campo sql_query:**
```sql
SELECT * FROM users
WHERE organization_id = ?
  AND (full_name ILIKE ? OR email ILIKE ?)
ORDER BY created_at DESC
```

**JSON per parameters_schema:**
```json
{
  "type": "object",
  "properties": {
    "organization_id": {
      "type": "string",
      "description": "ID dell'organizzazione"
    },
    "search_query": {
      "type": "string",
      "minLength": 0,
      "maxLength": 255,
      "description": "Ricerca per nome o email (case-insensitive)"
    }
  },
  "required": ["organization_id"],
  "additionalProperties": false
}
```

**Nota:** `search_query` viene usato due volte nella query (ILIKE due volte), quindi il provider lo ripeterà per entrambi i placeholder.

## Validazione lato Frontend

### Schema JSON completo per la form

Usa questo schema per validare l'intero form ai_tools:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "title": "AI Tool Form Validation",
  "properties": {
    "name": {
      "type": "string",
      "pattern": "^[a-zA-Z_][a-zA-Z0-9_]*$",
      "minLength": 1,
      "maxLength": 255
    },
    "method_name": {
      "type": ["string", "null"],
      "pattern": "^[a-zA-Z_][a-zA-Z0-9_]*$|^$",
      "maxLength": 255
    },
    "description": {
      "type": ["string", "null"],
      "maxLength": 4000
    },
    "sql_query": {
      "type": "string",
      "minLength": 10,
      "pattern": "^\\s*SELECT\\s+",
      "maxLength": 10000
    },
    "parameters_schema": {
      "type": ["string", "null"]
    },
    "active": {
      "type": "boolean"
    }
  },
  "required": ["name", "sql_query"],
  "additionalProperties": false
}
```

### Validazioni aggiuntive (logica custom)

1. **Verifica parameters_schema JSON:** Se `parameters_schema` è non-null/non-vuoto, deve essere un JSON valido di tipo `object`
2. **Conta placeholders:** Numero di `?` in `sql_query` deve corrispondere a numero di proprietà in `parameters_schema.properties`
3. **Nome univoco:** `name` non deve essere già usato da un altro ai_tool (check verso il backend)
4. **Method name:** Se `method_name` è vuoto/null, la GUI deve usare `name` come fallback

## Validazione lato Backend

Il provider backend valida automaticamente:
- `parameters_schema` JSON parsing
- Ordine dei parametri durante READ_VALUE
- Binding PreparedStatement con parametri ordinati


## Best Practices

1. **Usa descrizioni chiare**: l'IA le legge per capire quando invocare il tool
2. **Nomi descrittivi**: `search_users_by_email` è meglio di `q1`
3. **SQL sicuro**: usa sempre parametri (?), mai string interpolation
4. **Test dei placeholder**: verifica che `SELECT ... WHERE x = ? AND y = ?` abbia esattamente 2 proprietà nello schema
5. **Tipi SQL appropriati**: 
   - string → VARCHAR/TEXT
   - number/integer → INT/BIGINT/DECIMAL
   - boolean → BOOLEAN/BIT

