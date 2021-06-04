# Matt Farmer's Repo Policy Utility

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/66af2fc4ddd647a7a3788397202d337b)](https://app.codacy.com/gh/farmdawgnation/repo-policy?utm_source=github.com&utm_medium=referral&utm_content=farmdawgnation/repo-policy&utm_campaign=Badge_Grade_Settings)

Welcome to the *Repo Policy Utility*. This tool is for everyone who is tired of
having to manually validate GitHub Repository settings across tens or hundreds
of GitHub Repositories in your organization. You can define a **policy** for
how your repositories should be configured, and apply that policy using
**repo labels** on GitHub. A policy is a YAML file that describes the desired
state of particular settings. Settings in the policy can be omitted for settings
where you don't want to have the policy to have an opinion at all.

## Requirements

* Java 11 (AdoptOpenJDK 11 preferred)

## Downloading

You can download the latest JAR file from the releases page for each release.

## Usage

The Repo Policy Utility requires GitHub authentication to be set in environment
variables. The following environment variables are needed:

|Name           | Required | Description                              |
|---------------|----------|------------------------------------------|
|GITHUB_USERNAME|Y         |The username for the utility to run under.|
|GITHUB_OAUTH   |Y         |The access token for the utility to use.  |
|GITHUB_ENDPOINT|N         |API Endpoint to use if using Enterprise.  |

With these environment variables defined you can run the utility in either
**validate mode** or **enforce mode**. If you're running as a part of an
automated process it is **STRONGLY** recommended to run that action in
the validate mode and only use enforce for those cases where you know
you want to make the changes to the repository.

### Validate Mode

Usage for validate mode is as follows:

```bash
$ java -jar repo-policy-all.jar validate my-policy.yaml
```

### Enforce mode

Usage for enforce mode is as follows:

```bash
$ java -jar repo-policy-all.jar enforce my-policy.yaml
```

## Policy Schema

Policies are YAML files that describe the state of the GitHub settings for
the repository and its branches.

The following is an example policy:

```yaml
---
# Some Policy metadata
name: Example Policy
version: 1.0
author: Engineering Leaders <eng-leaders@example.org>
# Rules associated with the policy
rules:
- org: ExampleOrg
  topic: example-topic
  # Rules match on a topic set on the repository and apply repo
  # and branch settings. You can omit the topic parameter or set
  # it to null to have this apply to all repositories owned by a
  # particular owner.
  repo:
    license_key: mit
    delete_branch_on_merge: true
    visibility: public
    features:
      issues: true
      projects: false
      wiki: false
      vulnerability_alerts: true
    default_branch: main
    collaborators:
    - OrgName/engineering-team
  branches:
  - branch: main
    protection:
      enabled: true
      required_checks:
      - "unit-tests"
      dismiss_stale_reviews: true
      include_admins: true
      require_up_to_date: true
      require_code_owner_reviews: true
      required_review_count: 2
      restrict_push_access: false
      restrict_review_dismissals: true
      push_teams:
      - "AnchorTab/engineering"
      push_users:
      - "farmdawgnation"
      review_dismissal_users:
      - "farmdawgnation"
```

A few things to note about how this works:
* Most keys can be omitted if you don't want the policy to have an
  opinion on the setting.
* Required checks, teams, and users keys are a non-exclusive check, so
  they will pass validation as long as _at least those people_ are
  mentioned. However, enforcement may be exclusive due to nuances in the
  way GitHub's API works. This means users not mentioned in the policy
  could lose privileges during an enforcement action.
  
## License

This project is licensed under the Apache 2.0 License.

## Author

This tool is written and maintained by Matt Farmer in his spare time.
When not chasing his tiny clone around, Matt can often be found doing
engineery things for Greenlight Financial Technology.
