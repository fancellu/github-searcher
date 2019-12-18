# github-searcher

Example Scala code showing processing of JSON repo data from Github

## Functionality

  1. Take a single argument specifying the search term, and error out if one is not provided
  2. Using the [Github API](https://developer.github.com/v3/) as a starting point, find all the repositories that have a **description** containing the given search term **as a full phrase** (not just the words individually). To avoid hitting the API rate limit, you should only load **up to 1000** results.
  3. Filter out any repos with an empty "language" (`null` or empty String)
  4. Group the remaining list of repos by "language", and count the number of occurrences for each
  5. Sort the languages by occurrence descending
  6. Output a line for each result, in the `{language}: {count}` format
  7. After the results, on a separate line, output the total number of search results in the format: `=> {total_count} total result(s) found`

## Dependencies

Using play-ws-standalone and play-ws-standalone-json

## To run

`sbt "run xslt"` or some such
