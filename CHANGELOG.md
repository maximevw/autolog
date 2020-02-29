# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Add `@Mask` annotation to apply a masking pattern to the annotated method arguments when they are logged thanks to the
annotations `AutoLogMethodInput` or `AutoLogMethodInOut`.
### Deprecated
- Deprecate `Log4jAdapter` (since Log4J library is deprecated and now classified as highly vulnerable, see
[CVE-2019-17571](https://nvd.nist.gov/vuln/detail/CVE-2019-17571">)).

## [1.0.0] - 2020-01-06
### Added
- Initial release.

[Unreleased]: https://github.com/maximevw/autolog/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/maximevw/autolog/releases/tag/v1.0.0
