# ADR-0016: Keep the catalog `Service` entity name; qualify Spring's `@Service` annotation instead

- **Status:** Proposed
- **Date:** 2026-11-22
- **Feature:** [docs/features/catalog.md](../features/catalog.md)

## Context

B1 named the catalog entity `Service` (SRS Table 9, F3), matching the SRS vocabulary; AGENT_RULES
section 12 bans inventing synonyms for domain terms, so renaming it is not a free option. B4 adds a
business/application service class in the same package (`catalog`) that must be annotated with
Spring's `org.springframework.stereotype.Service` and also reference the `Service` entity by its
simple name in the same file, for example a method that takes or returns `List<Service>`. Importing
`org.springframework.stereotype.Service` shadows the package-local `Service` entity throughout that
file, which breaks any such method signature.

## Decision

Keep the entity named `Service`. In the one file per package that needs both, annotate the class with
the fully-qualified annotation (`@org.springframework.stereotype.Service`) and do not import
`org.springframework.stereotype.Service`, so the bare name `Service` keeps referring to the entity
everywhere in that file. Apply the same pattern anywhere else in `catalog` that hits the same clash.

## Consequences

- The SRS vocabulary stays intact; no synonym is invented for "service".
- One line (the fully-qualified annotation) looks slightly unusual next to the rest of the codebase,
  but needs no further workaround and costs nothing at call sites.
- Future catalog classes that need both an application service and the `Service` entity type must
  remember the same pattern; worth a callout in review.

## Alternatives considered

- Rename the entity (`CatalogService`, `OfferedService`, ...): rejected, breaks the SRS vocabulary
  rule and only moves the same clash onto the new name versus the Spring annotation.
- Import `org.springframework.stereotype.Service` and fully-qualify the entity
  (`com.smartappointment.catalog.Service`) instead: also workable, but the entity type is referenced
  far more often per file than the annotation, so qualifying the annotation (used once, at the class
  declaration) reads cleaner.
