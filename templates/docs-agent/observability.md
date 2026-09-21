---
status: draft
template_version: 1.0.1
content_version: 1.0.0
summary: How to instrument new functionality in <REPO_NAME> — metrics, alarms, and observability validation.
last_reviewed: null
---

# Observability

> See also: platform and cross-repo conventions in `docs/observability.md` (product level).

## Custom metrics

<PLACEHOLDER: metrics this repo currently exposes (name, type, tags);
naming convention; which library/annotation to use when adding a new one.>

## Adding alarms for new functionality

<PLACEHOLDER: ordered steps — define metric → configure threshold → register alarm
in config-repo/IaC; naming convention; who to notify.>

## Dashboards

<PLACEHOLDER: repo-specific dashboard URL or panel; how to add a new panel.>

## Validating a built feature via observability

<PLACEHOLDER: which log lines / metrics to watch when smoke-testing in ENV;
query examples for the log aggregator.>
