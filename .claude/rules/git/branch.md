# Branch Strategy

## Structure

main -> develop -> feature/{name}

- main: production only
- develop: integration branch
- feature/{name}: feature work

## Naming

- lowercase + hyphen
- start with verb (add-, fix-, refactor-)

Examples:

- feature/add-jwt-auth
- feature/account-encryption
- feature/budget-chain-handler

## Merge

- feature -> develop: PR
- develop -> main: PR before deploy
- No direct push to main/develop
