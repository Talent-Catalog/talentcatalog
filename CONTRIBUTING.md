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


## Standard tools

See [the README for the standard tools we use for development](README.md).

In particular, we use Intellij to edit, build and run code as well as using
its Git and GitHub integration. Our standard Intellij configuration is 
checked into version control which imposes many of our standards.
It is certainly not necessary to use Intellij in order to contribute that is
probably the simplest way to go.


## Where do I go from here?

If you've noticed a bug or have a feature request, [make one][new issue]! It's
generally best if you get confirmation of your bug or approval for your feature
request this way before starting to code.

If you have a general question about TBB, please contact us at 
contact@talentbeyondboundaries.org, 
the issue tracker is only for software bugs and feature requests.

## Fork & create a branch

We [collaborate with issues and pull requests], using the [fork and pull model].

If this is something you think you can fix, then [fork talentcatalog] and create
a branch with a descriptive name.

A good branch name would be (where issue #325 is the one you're working on):

```sh
git checkout -b 325-add-french-translations
```

## Implement your fix or feature

At this point, you're ready to make your changes! Feel free to ask for help;
everyone is a beginner at first.

## Test your changes locally

See [the README for how to run locally](README.md).

## Get the style right

Your patch should follow the same conventions & pass the same code quality
checks as the rest of the project.

## Make a Pull Request

At this point, you should switch back to your master branch and make sure it's
up to date with the ("upstream") tbbtalentv2 master branch:

Then update your feature branch from your local copy of master, and push it!

Finally, go to GitHub and [make a Pull Request] 

Github Actions will run our test suite. Your PR won't be merged until all tests pass.

## Keeping your Pull Request updated

If a maintainer asks you to "merge" your PR, they're saying that a lot of code
has changed, and that you need to update your branch by merging the current 
master with it. Let the maintainer know if you have problems doing that.

## Merging a PR (maintainers only)

A PR can only be merged into master by a maintainer if:

* It is passing all tests.
* It has been approved by at least two maintainers. If it was a maintainer who
  opened the PR, only one extra approval is needed.
* It has no requested changes.
* It is up to date with current master.

Any maintainer is allowed to merge a PR if all of these conditions are
met.

## Shipping a release (maintainers only)

See [the README for how to do a release](README.md).


[new issue]: https://github.com/talentbeyondboundaries/tbbtalentv2/issues/new
[fork TbbTalent]: https://help.github.com/articles/fork-a-repo
[collaborate with issues and pull requests]: https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests
[fork and pull model]: https://docs.github.com/en/github/collaborating-with-issues-and-pull-requests/about-collaborative-development-models
[make a pull request]: https://help.github.com/articles/creating-a-pull-request
