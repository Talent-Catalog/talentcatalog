# Contributing to the Talent Catalog

First of all, thank you for considering contributing to the Talent Catalog. 

[Please read and abide by our Code of Conduct](CODE_OF_CONDUCT.md).

## Code Style ##
### Java ###
The style standard for this project is 
[Google Java Style](http://google.github.io/styleguide/javaguide.html). 
See, for example, [this posting](https://medium.com/swlh/configuring-google-style-guide-for-java-for-intellij-c727af4ef248) 
for how to set up the Intellij IDE for that style.

We also use [Lombok](https://projectlombok.org/setup/intellij) 
sparingly - primarily to make the code more readable by removing "boilerplate"
[Getters and Setters](https://projectlombok.org/features/GetterSetter), 
and to help with debugging by supplying meaningful class 
[toStrings](https://projectlombok.org/features/ToString).
But we try not to get too clever with it!

### Typescript ###

We aspire to following the 
[Google Typescript style](https://google.github.io/styleguide/tsguide.html).

### Angular ###

We aspire to following the [Angular style guide](https://angular.io/guide/styleguide).

### Copyright Headers ###

All source files must include a copyright header in the following format:

```
/*
 * Copyright (c) [YEAR] Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
```

The earliest copyright year that appears in file-level headers in this repository is 2024, even 
though the project itself originated earlier (2019). This is because, in 2024, we made a one-time 
decision to re-stamp the copyright year to 2024 across the repository. While this was not strictly 
required (as a file’s original year of first publication would also have been legally valid), it 
now serves as the baseline for our current convention.

From that point onward, the year in file headers is treated as the year a file was first introduced 
(or first stamped) under this convention, rather than as an exact record of its earliest historical 
origin.

Files introduced in 2025 correctly carry a 2025 year; new files introduced in 2026 will carry 2026, 
and so on. We do not retroactively adjust years for files that predate this baseline, as version 
control already provides a precise historical record and further mass updates would introduce 
unnecessary churn without legal or practical benefit.

This approach avoids the need for annual copyright-only update PRs and keeps the repository 
consistent, and in line with GPL licensing and common open-source conventions.

## Standard tools

See [the README for the standard tools we use for development](README.md).

In particular, we use Intellij to edit, build and run code as well as using
its Git and GitHub integration. Our standard Intellij configuration is 
checked into version control which imposes many of our standards.
It is certainly not necessary to use Intellij in order to contribute but is
probably the simplest way to go.


## Where do I go from here?

If you've noticed a bug or have a feature request, [make one][new issue]! It's
generally best if you get confirmation of your bug or approval for your feature
request this way before starting to code.

If you have a general question about the Talent Catalog, please contact us at 
support@talentcatalog.net, 
the issue tracker is only for software bugs and feature requests.

## Fork & create a branch

We [collaborate with issues and pull requests], using the [fork and pull model].

If this is something you think you can fix, then [fork talentcatalog] and create
a branch with a descriptive name.

A good branch name would be (where issue #325 is the one you're working on):

```sh
git checkout -b 325-add-french-translations
```

> **Note:** Please create your branch from `staging`. This branch contains the latest features 
> and code used in the staging environment, where active development and testing take place.

## Implement your fix or feature

At this point, you're ready to make your changes! Feel free to ask for help;
everyone is a beginner at first.

## Test your changes locally

See [the README for how to run locally](README.md).

## Get the style right

Your patch should follow the same conventions & pass the same code quality
checks as the rest of the project.

### Documentation

To improve code readability and maintainability we aspire to document the code with clear 
explanations of components, classes, functions and variables. The documentation standard we follow 
is **JSDoc** for Angular and **Javadoc** for Java code.

For JSDoc follow the JSDoc style using annotations like @description for useful descriptions of 
components and @example to provide code examples. For Javadoc follow the Javadoc style using @code 
for code examples, @link for useful links and html for easy to read formatting.

See:
[the JSDoc documentation](https://jsdoc.app/).
[the Javadoc documentation](https://docs.oracle.com/en/java/javase/17/docs/specs/javadoc/doc-comment-spec.html).

## Make a Pull Request

At this point, you should switch back to your staging branch and make sure it's
up to date with the ("upstream") talentcatalog staging branch:

Then update your feature branch from your local copy of staging, and push it!

Finally, go to GitHub and [make a Pull Request] 

Github Actions will run our test suite. Your PR won't be merged until all tests pass.

## Keeping your Pull Request updated

If a maintainer asks you to "merge" your PR, they're saying that a lot of code
has changed, and that you need to update your branch by merging the current
staging with it. Let the maintainer know if you have problems doing that.

## Merging a PR (maintainers only)

A PR can only be merged into staging by a maintainer if:

* It is passing all tests.
* It has been approved by at least two maintainers. If it was a maintainer who
  opened the PR, only one extra approval is needed.
* It has no requested changes.
* It is up to date with current staging.

Any maintainer is allowed to merge a PR if all of these conditions are
met.

## Making Postgres DB changes

We use [Flyway](https://www.red-gate.com/products/flyway/community/) to replicate DB changes across 
TC instances, facilitate proper version control and manage coordination between developers.

In broad outline, you will add a file to the db.migration folder containing the SQL command(s) 
for your desired changes to the schema. On next startup these will be applied to your local DB 
instance and logged by an entry in the flyway_schema_history table, ensuring that migrations are 
applied only once and in proper sequence on each TC instance.

### The Flyways branch

We utilise a 'Flyways' development branch to manage the sequence of DB migrations and thereby 
minimise the potential for build errors with a team of developers working independently.

When you are confident of the changes you wish to make, follow these steps, observing carefully the 
amendments in the following section, if they include the modification or dropping of an existing 
column or table:
1. Merge a current version of 'Staging' into your current branch and commit or shelve any changes. 
2. Check out the 'Flyways' branch. 
3. Merge a current version of 'Staging' into 'Flyways'. 
4. Add a file containing your SQL command(s) to the db.migration folder, observing the naming convention and taking the next number in sequence. 
5. Notify your fellow developers of the number you've claimed in the [#tool-tcsoftware-tech-int](https://refugeejobsmarket.slack.com/archives/C0583HJ9CHM) Slack channel. 
6. Commit and push your changes. 
7. Check out your working branch and merge 'Flyways' into it. 
8. Restart the Spring service and the changes will be applied to your local DB.
9. Check the 'Amend' box (if using IntelliJ) and commit and push your changes.
10. Open a PR to merge your working branch into Staging.

Observing these simple steps means that other developers needing to make DB changes can do so with 
minimal coordination and potential for time-consuming errors.

### Modifying or dropping existing columns or tables

When renaming or dropping an existing column or table, following the above process without amendment 
would break the build for other developers who don't yet have your code. It could also cause issues 
for TC users during deployment of a new release. For that reason, we separate the process across two 
release cycles, as follows.

#### Modifying
* Instead of directly editing a table or column, follow the usual process to create a _new_ version reflecting your desired changes.
* As appropriate, include in your migration the required SQL command(s) to transfer current data to the new version.
* Submit a PR for code that eliminates all references to the old version and points instead to your new version.

#### Dropping
* In the current release cycle you will only submit a PR for code that eliminates all references to the column or table to be dropped.

In both cases:
* Create a GitHub issue _marked for the next release_, to complete this process by submitting a migration to drop the redundant column or table. 
* Provide the necessary SQL commands and context, so a different developer could pick up your issue and action it without further analysis.
* In the next release cycle, the usual process can now be followed without risk of unwanted effects.

### Backing out changes

Sometimes at a late stage of working on a branch, you may need to back out changes that have already 
been added to the 'Flyways' branch and applied to your local DB and those of your fellow developers.

If no one else has built from the 'Flyways' branch, you can reverse the changes by directly editing 
your local DB schema, deleting the corresponding row from the flyway_schema_history table, and 
building again with the migration file removed or its content amended.

If the 'Flyways' branch has already been added to and built from by other developers, the safest 
course is to submit a new migration that amends the schema as required. In this event, create a new 
GitHub issue to provide the proper context for your PR.

## Shipping a release (maintainers only)

See [the README for how to do a release](README.md).


[new issue]: https://github.com/Talent-Catalog/talentcatalog/issues/new
[fork talentcatalog]: https://help.github.com/articles/fork-a-repo
[collaborate with issues and pull requests]: https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests
[fork and pull model]: https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/about-collaborative-development-models
[make a pull request]: https://help.github.com/articles/creating-a-pull-request
