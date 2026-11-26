# Local commands that mirror CI (.github/workflows/ci.yml). Run `make help` for the list.
# Backend targets need JDK 21; web targets need Node 24 (.nvmrc); Docker targets need Docker.

.DEFAULT_GOAL := help
HAS_WEB := $(wildcard web/package.json)

.PHONY: help setup up down logs db-reset lint format test verify api-lint backend-lint backend-format backend-test backend-verify web-lint web-format web-test repo-lint

help: ## Show this list
	@grep -E '^[a-z-]+:.*## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*## "}; {printf "  %-16s %s\n", $$1, $$2}'

setup: ## One-time setup after cloning: git hooks and .env
	./scripts/setup.sh

up: ## Start PostgreSQL, Mailpit and the backend
	docker compose up --build

down: ## Stop all services
	docker compose down

logs: ## Follow backend logs
	docker compose logs -f backend

db-reset: ## Delete the local database volume; the next `make up` recreates schema and demo data
	docker compose down -v

lint: repo-lint backend-lint web-lint ## All linters, as in CI

format: backend-format web-format ## Auto-format all code

test: backend-test web-test ## Fast tests (no Docker needed)

verify: lint backend-verify web-test ## Everything CI runs, except Docker image and security scans

repo-lint: api-lint ## English-only check, Markdown lint and API contract lint
	./scripts/check-english.sh
	npx --yes markdownlint-cli2@0.24.2 "**/*.md"

api-lint: ## Lint api/openapi.yaml (Redocly)
	npx --yes @redocly/cli@2.82.0 lint api/openapi.yaml

backend-lint: ## Spotless check and Checkstyle
	cd backend && ./mvnw -B -q spotless:check checkstyle:check

backend-format: ## Apply google-java-format
	cd backend && ./mvnw -B -q spotless:apply

backend-test: ## Unit, slice and architecture tests
	cd backend && ./mvnw -B test -Dcheckstyle.skip

backend-verify: ## Full backend build incl. integration tests and coverage
	cd backend && ./mvnw -B verify

web-lint: ## ESLint, Prettier check and type check (skipped until web/ exists)
ifneq ($(HAS_WEB),)
	cd web && npm run lint && npm run format:check && npm run typecheck
endif

web-format: ## Prettier and ESLint --fix (skipped until web/ exists)
ifneq ($(HAS_WEB),)
	cd web && npm run format && npm run lint -- --fix
endif

web-test: ## Vitest (skipped until web/ exists)
ifneq ($(HAS_WEB),)
	cd web && npm test
endif
