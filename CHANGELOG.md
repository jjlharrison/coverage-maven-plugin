# Changelog

## [0.4.0] - 2019-09-04

- First open source version:
    - Removed dependencies on internal libraries.
    - Upgraded most plugins and dependencies to latest versions.

## [0.3.1] - 2019-03-07

- Fix some lines falsely considered as uncovered when calculating coverage percentage.

    This happened when JaCoCo reports a line with both covered and uncovered instructions.

## [0.3.0] - 2019-02-08

- XML report file is now generated when there are no changes (coverage values are set to 100%).

- Add JAXB dependencies to support Java 9+.

## [0.2.0] - 2019-02-07

- Add `report` and `check` goals to measure test coverage for changed code and optionally fail build if coverage isn't high enough. 

## [0.1.0] - 2018-11-23

- Initial implementation of `update-coverage-requirement` Mojo. 

[0.4.0]: https://github.com/jjlharrison/coverage-maven-plugin/compare/0.3.1...0.4.0
[0.3.1]: https://github.com/jjlharrison/coverage-maven-plugin/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/jjlharrison/coverage-maven-plugin/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/jjlharrison/coverage-maven-plugin/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/jjlharrison/coverage-maven-plugin/releases/tag/0.1.0