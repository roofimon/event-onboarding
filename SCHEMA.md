# Demonstrating Avro

The clearest demonstration is to show that RabbitMQ contains binary Avro—not
JSON—and that Apicurio holds the schema needed to decode it.

## 1. Start the infrastructure

```bash
docker compose up -d rabbitmq apicurio-registry
docker compose ps
```

Interfaces:

- RabbitMQ UI: <http://localhost:15672> (`guest` / `guest`)
- Apicurio Registry: <http://localhost:8081>

## 2. Start the backend

```bash
./gradlew :application:bootRun
```

The application connects to:

- RabbitMQ exchange: `event-onboarding.domain-events`
- Queue: `event-onboarding.credit-scoring-calculated`
- Routing key: `onboarding.credit-scoring.calculated`
- Apicurio API: `http://localhost:8081/apis/registry/v2`

## 3. Complete onboarding

Either use the Vue frontend:

```bash
cd frontend
npm run dev
```

Open <http://localhost:5173> and complete the wizard. The verification token
appears in the backend console.

Use profile values likely to pass scoring, such as:

```text
Salary: 120000
Years of experience: 7
```

When scoring runs, this call publishes the event:

```http
POST /api/onboarding/{applicationId}/score
```

## 4. Show the schema in Apicurio

Open the registry UI or API and demonstrate that the schema has been
auto-registered. The source schema is
[`CreditScoringCalculated.avsc`](infrastructure/src/main/resources/avro/CreditScoringCalculated.avsc).

It defines:

```text
applicationId
email
score
approved
step
occurredAt
```

This establishes that message structure is managed as an explicit schema rather
than inferred from JSON.

## 5. Show that the RabbitMQ message is binary

In RabbitMQ Management:

1. Open **Queues and Streams**.
2. Select `event-onboarding.credit-scoring-calculated`.
3. Open **Get messages**.
4. Retrieve one message.

The payload will not resemble this:

```json
{
  "applicationId": "...",
  "email": "...",
  "score": 55
}
```

Instead, it is compact binary data containing an Apicurio schema identifier
followed by the Avro-encoded record. The field names do not travel in every
message.

Set the acknowledgement mode to requeue if you want to preserve the message
during the demonstration.

## 6. Run the automated round-trip demonstration

The integration test is the strongest technical proof:

```bash
./gradlew test --tests '*CreditScoringEventRegistryRoundTripIT'
```

It demonstrates the complete path:

```text
Domain event
    -> Kotlin-to-Avro mapper
    -> Apicurio schema registration
    -> Avro binary encoding
    -> RabbitMQ
    -> schema lookup
    -> Avro decoding
    -> original event fields
```

Relevant files:

- [`AvroEventSerializer.kt`](infrastructure/src/main/kotlin/com/example/eventonboarding/infrastructure/messaging/AvroEventSerializer.kt)
- [`CreditScoringAvroMapper.kt`](infrastructure/src/main/kotlin/com/example/eventonboarding/infrastructure/messaging/CreditScoringAvroMapper.kt)
- [`RabbitDomainEventPublisher.kt`](infrastructure/src/main/kotlin/com/example/eventonboarding/infrastructure/messaging/RabbitDomainEventPublisher.kt)
- [`CreditScoringEventRegistryRoundTripIT.kt`](infrastructure/src/test/kotlin/com/example/eventonboarding/infrastructure/messaging/CreditScoringEventRegistryRoundTripIT.kt)

## Suggested presentation narrative

> Previously, RabbitMQ received self-describing JSON. Now the publisher maps the
> domain event to an Avro record. Apicurio registers and versions its schema,
> while RabbitMQ receives only a schema reference and compact binary values. A
> consumer uses that reference to retrieve the correct schema and reconstruct
> the record. This reduces payload size and creates an explicit compatibility
> contract between producers and consumers.
