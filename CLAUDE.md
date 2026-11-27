# Project standards

Shared standards come from **pinq-doq**, vendored under `.pinq-doq/` and copied into `.claude/rules/` (auto-loaded — do not @import the scoped files). Skills are in `.claude/skills/`. Code-gen scripts: `.pinq-doq/scripts/`. Deep references: `.pinq-doq/references/`.

To pull newer standards later, tell Claude: **update rules**.

pinq-doq is managed by the pinqdoq CLI here: `.pinq-doq/`, `.claude/rules/` and the `pinq_*` skills are
ignored local copies. If `.claude/rules/common.md` is missing, stop and set up the CLI
(https://github.com/pinqponq/pinqdoq, `tasks/migrate-to-cli.md`) before writing code. Never add the
submodule back and never edit the copies.
